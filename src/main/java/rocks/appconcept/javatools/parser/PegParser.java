package rocks.appconcept.javatools.parser;

import rocks.appconcept.javatools.parser.peg.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Parsing Expression Grammar.
 *
 * @author yanimetaxas
 */
public abstract class PegParser {

    /**
     * Constant value returned by {@link #parse(Input)} to reflect that the parser failed.
     */
    public static final Output FAILED = new Output("FAILED");

    /**
     * Parse the specified input. This method can return {@link #FAILED} if the input couldn't be parsed.
     * The {@link Input#position} field will be updated to reflect how much of the input was consumed by the parser.
     * A typical caller would ensure that the result is not {@link #FAILED} and that {@link Input#position} is equal to
     * the length of {@link Input#input}.
     * @param input The input to parse.
     * @return The result of parsing the object. Will return {@link #FAILED} if the input couldn't be parsed.
     */
    public abstract Output parse(Input input);

    public PegParser asString() {
        return new AstWrapAsString(this);
    }

    public PegParser asList() {
        return new AstWrapAsList(this, null);
    }

    public PegParser asList(Predicate<Object> filter) {
        return new AstWrapAsList(this, filter);
    }

    public PegParser constant(Object constant) {
        return new AstWrapAsConstant(this, constant);
    }

    public PegParser make(Class<?> aClass, String ... property) {
        return new AstWrapAsNewObject(this, aClass, property);
    }

    public PegParser set(String property, String value) {
        return new AstWrapAsSetProperty(this, property, value);
    }

    public PegParser pick(int ... index) {
        return new AstWrapPick(this, index);
    }


    public static abstract class Resolvable extends AstBase {
        public abstract void resolve(PegParser resolution);
    }

    public static Resolvable call() {
        return new AstCall();
    }
    public static PegParser character(String characters) {
        return (characters.length() == 1) ? new AstSingleCharacter(characters.charAt(0)) : new AstCharacterClass(characters);
    }
    public static PegParser character(AstCharacterClassifier.Classifier classifier) {
        return new AstCharacterClassifier(classifier, 1);
    }
    public static PegParser characters(AstCharacterClassifier.Classifier classifier) {
        return new AstCharacterClassifier(classifier, Integer.MAX_VALUE);
    }
    public static PegParser string(char quote) {
        return new AstQuotedString(quote);
    }
    public static PegParser digit() {
        return new AstDigits(1);
    }
    public static PegParser digits() {
        return new AstDigits(Integer.MAX_VALUE);
    }
    public static PegParser whitespace(boolean optional) {
        return new AstWhitespace(optional);
    }
    public static PegParser optional(PegParser inner) {
        return new AstOptional(inner);
    }
    public static PegParser not(PegParser inner) {
        return new AstNot(inner);
    }
    public static PegParser options(PegParser ... options) {
        return new AstOptions(options);
    }
    public static PegParser dot() {
        return new AstDot();
    }
    public static PegParser sequence(PegParser ... sequence) {
        return new AstSequence(sequence);
    }
    public static PegParser list(PegParser element, PegParser separator) {
        return new AstList(element, separator);
    }
    public static PegParser repeatPlus(PegParser inner) {
        return new AstRepeatPlus(inner);
    }
    public static PegParser repeatStar(PegParser inner) {
        return new AstRepeatStar(inner);
    }
    public static PegParser literal(String literal) {
        return (literal.length() == 1) ? new AstSingleCharacter(literal.charAt(0)) : new AstLiteral(literal);
    }
    public static PegParser literals(Collection<String> literals) {
        PegParser[] parsers = literals.stream().collect(Collectors.groupingBy(String::length)).entrySet().stream().sorted(Comparator.comparing(e -> -e.getKey())).flatMap(r -> r.getValue().stream()).map(PegParser::literal).toArray(PegParser[]::new);
        return options(parsers);
    }

    public static PegParser repeatStarUntil(PegParser element, PegParser until) {
        return sequence(repeatStar(sequence(not(until), element).pick(1)).asList(), until);
    }
    public static PegParser repeatPlusUntil(PegParser element, PegParser until) {
        return sequence(repeatPlus(sequence(not(until), element).pick(1)).asList(), until);
    }
    public static Input input(String input) {
        return new Input(input, 0);
    }
}
