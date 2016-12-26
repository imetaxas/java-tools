package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstSequence extends AstBase {

    private final PegParser[] sequence;

    public AstSequence(PegParser... sequence) {
        this.sequence = sequence;
    }

    @Override
    public Output parse(Input input) {
        Input.Point start = input.getPoint();
        if (isFailureCached(start)) return FAILED;
        Output res = new Output();
        for (PegParser node : sequence) {
            Output parse = node.parse(input);
            if (parse == FAILED) return cacheFailure(start);
            res.list.add(parse);
        }
        return res;
    }
}
