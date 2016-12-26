package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstOptions extends AstBase {

    private final PegParser[] options;

    public AstOptions(PegParser... options) {
        this.options = options;
    }

    @Override
    public Output parse(Input input) {
        Input.Point start = input.getPoint();
        if (isFailureCached(start)) return FAILED;
        int push = input.position;
        for (PegParser option : options) {
            Output parse = option.parse(input);
            if (parse != FAILED) return parse;
            input.position = push;
        }
        return cacheFailure(start);
    }
}
