package rocks.appconcept.javatools.parser.peg;

/**
 * @author yanimetaxas
 */
public class AstCharacterClassifier extends AstBase {

    @FunctionalInterface
    public interface Classifier {
        boolean check(int c);
    }

    private final Classifier classifier;
    private final int maxOccurrences;

    public AstCharacterClassifier(Classifier classifier, int maxOccurrences) {
        this.classifier = classifier;
        this.maxOccurrences = maxOccurrences;
    }

    @Override
    public Output parse(Input input) {

        String data = input.input;
        int length = data.length();
        int end = input.position;
        int limit = maxOccurrences;
        while (limit > 0 && end < length && classifier.check(data.codePointAt(end))) {
            limit--;
            end++;
        }

        if (end > input.position) {
            String substring = data.substring(input.position, end);
            input.position = end;
            return new Output(substring);
        }

        return FAILED;
    }
}
