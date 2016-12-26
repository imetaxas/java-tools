package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
* @author yanimetaxas
*/
public class AstCall extends PegParser.Resolvable {

    private PegParser callee;

    @Override
    public Output parse(Input input) {
        return callee.parse(input);
    }

    @Override
    public void resolve(PegParser resolution) {
        this.callee = resolution;
    }
}
