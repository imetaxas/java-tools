package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstDot extends AstBase {

  @Override
  public Output parse(Input input) {
    if (input.has(1)) {
      return new Output(Character.toString(input.input.charAt(input.position++)));
    }
    return PegParser.FAILED;
  }
}
