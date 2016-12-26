package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWhitespace extends AstBase {

    private final boolean optional;

    public AstWhitespace(boolean optional) {
        this.optional = optional;
    }

    @Override
    public Output parse(Input input) {

        String data = input.input;
        int length = data.length();
        int end = input.position;
        while (end < length && Character.isWhitespace(data.charAt(end))) end ++;

        if (optional || end > input.position) {
            String substring = data.substring(input.position, end);
            input.position = end;
            return new Output(substring);
        }
        return PegParser.FAILED;
    }

}
