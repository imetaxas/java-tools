package rocks.appconcept.javatools.parser.peg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWrapAsList extends AstBase {

  private final Predicate<Object> filter;
  private final PegParser inner;

  public AstWrapAsList(PegParser inner, Predicate<Object> filter) {
    this.inner = inner;
    this.filter = filter == null ? (item -> true) : filter;
  }

  @Override
  public Output parse(Input input) {
    Output parse = inner.parse(input);
    if (parse != PegParser.FAILED) {
      List<Object> list = new ArrayList<>();
      if (parse.list != null) {
        list.addAll(parse.list.stream().map(output -> output.object).filter(filter)
            .collect(Collectors.toList()));
      } else {
        list.add(parse.object);
      }
      return new Output(list);
    }
    return parse;
  }
}
