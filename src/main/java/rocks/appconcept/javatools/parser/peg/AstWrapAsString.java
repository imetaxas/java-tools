package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWrapAsString extends AstBase {

  private final PegParser inner;

  public AstWrapAsString(PegParser inner) {
    this.inner = inner;
  }

  @Override
  public Output parse(Input input) {
    int start = input.position;
    Output parse = inner.parse(input);
    if (parse != FAILED) {
      return new Output(input.input.substring(start, input.position));
    }
    return parse;
  }
}
