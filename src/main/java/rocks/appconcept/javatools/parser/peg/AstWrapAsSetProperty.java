package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWrapAsSetProperty extends AstBase {
    private final PegParser inner;
    private final String property;
    private final String value;

    public AstWrapAsSetProperty(PegParser inner, String property, String value) {
        this.inner = inner;
        this.property = property;
        this.value = value;
    }

    @Override
    public Output parse(Input input) {
        Output parse = inner.parse(input);
        if (parse != FAILED) {
            Object object = parse.object;
            try {
                object.getClass().getField(property).set(object, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return parse;
    }
}
