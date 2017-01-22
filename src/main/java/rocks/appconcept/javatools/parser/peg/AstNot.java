package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstNot extends AstBase {

  private static final Output SUCCESS = new Output(null);
  private final PegParser inner;

  public AstNot(PegParser inner) {
    this.inner = inner;
  }

  @Override
  public Output parse(Input input) {
    int push = input.position;
    Output parse = inner.parse(input);
    input.position = push;
    if (parse == FAILED) {
      return SUCCESS;
    }
    return FAILED;
  }
}
