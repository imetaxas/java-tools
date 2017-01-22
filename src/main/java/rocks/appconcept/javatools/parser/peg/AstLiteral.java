package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstLiteral extends AstBase {

  private final String value;

  public AstLiteral(String value) {
    this.value = value;
  }

  @Override
  public Output parse(Input input) {
    if (input.has(value.length()) && input.input.startsWith(value, input.position)) {
      input.position += value.length();
      return new Output(value);
    }
    return PegParser.FAILED;
  }
}
