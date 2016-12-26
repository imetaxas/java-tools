package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstList extends AstBase {

    private final PegParser element;
    private final PegParser separator;

    public AstList(PegParser element, PegParser separator) {
        this.element = element;
        this.separator = separator;
    }

    @Override
    public Output parse(Input input) {

        Output head = element.parse(input);
        if (head == FAILED) return FAILED;

        Output result = new Output();
        result.list.add(head);

        while (true) {
            int push = input.position;
            Output sep = separator.parse(input);
            if (sep == FAILED) {
                input.position = push;
                return result;
            }

            head = element.parse(input);
            if (head == FAILED) return FAILED;
            result.list.add(head);
        }
    }
}
