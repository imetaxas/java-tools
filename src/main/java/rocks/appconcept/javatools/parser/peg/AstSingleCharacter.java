package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
* @author yanimetaxas
*/
public class AstSingleCharacter extends AstBase {

    private final char character;
    private final String string;

    public AstSingleCharacter(char character) {
        this.character = character;
        this.string = Character.toString(character);
    }

    @Override
    public Output parse(Input input) {
        if (input.has(1)) {
            if (input.input.charAt(input.position) == character) {
                input.position += 1;
                return new Output(string);
            }
        }
        return PegParser.FAILED;
    }
}
