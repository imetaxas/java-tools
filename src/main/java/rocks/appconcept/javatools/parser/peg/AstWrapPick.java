package rocks.appconcept.javatools.parser.peg;

import java.util.ArrayList;
import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWrapPick extends AstBase {

  private final PegParser outer;
  private final int[] indexes;

  public AstWrapPick(PegParser outer, int... indexes) {
    this.outer = outer;
    this.indexes = indexes;
  }

  @Override
  public Output parse(Input input) {
    Output parse = outer.parse(input);
    if (parse != FAILED) {
      if (indexes.length == 1) {
        int index = indexes[0];
        Object object = index >= parse.list.size() ? null : parse.list.get(index).object;
        return new Output(object);
      } else {
        ArrayList<Object> list = new ArrayList<>();
        for (int i : indexes) {
          list.add(parse.list.get(i).object);
        }
        return new Output(list);
      }
    }
    return parse;
  }
}
