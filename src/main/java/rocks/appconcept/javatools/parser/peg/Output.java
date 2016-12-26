package rocks.appconcept.javatools.parser.peg;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yanimetaxas
 */
public class Output {
    public final Object object;
    public final List<Output> list;

    public Output() {
        object = null;
        list = new ArrayList<>();
    }

    public Output(Object constant) {
        this.object = constant;
        this.list = null;
    }

    public String toString() {
        return list != null ? list.toString() : String.valueOf(object);
    }

}
