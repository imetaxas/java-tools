package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
* @author yanimetaxas
*/
public class AstRepeatPlus extends AstBase {

    private final PegParser node;

    public AstRepeatPlus(PegParser node) {
        this.node = node;
    }

    @Override
    public Output parse(Input input) {
        Input.Point start = input.getPoint();
        if (isFailureCached(start)) return PegParser.FAILED;
        Output res = new Output();
        Output required = node.parse(input);
        if (required != PegParser.FAILED) {
            res.list.add(required);
            while (true) {
                int push = input.position;
                Output extra = node.parse(input);
                if (extra == PegParser.FAILED) {
                    input.position = push;
                    return res;
                }
                res.list.add(extra);
            }
        }
        return cacheFailure(start);
    }
}
