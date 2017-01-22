package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstWrapAsConstant extends AstBase {

  private final PegParser inner;
  private final Object constant;

  public AstWrapAsConstant(PegParser inner, Object constant) {
    this.inner = inner;
    this.constant = constant;
  }

  @Override
  public Output parse(Input input) {
    Output parse = inner.parse(input);
    if (parse != PegParser.FAILED) {
      return new Output(constant);
    }
    return parse;
  }
}
