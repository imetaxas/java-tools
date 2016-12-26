package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
* @author yanimetaxas
*/
public class AstOptional extends AstBase {

    private final PegParser node;

    public AstOptional(PegParser node) {
        this.node = node;
    }

    @Override
    public Output parse(Input input) {
        Output result = new Output();
        int push = input.position;
        Output extra = node.parse(input);
        if (extra == PegParser.FAILED) {
            input.position = push;
        } else {
            result.list.add(extra);
        }
        return result;
    }
}
