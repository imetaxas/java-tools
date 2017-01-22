package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstRepeatStar extends AstBase {

  private final PegParser node;

  public AstRepeatStar(PegParser node) {
    this.node = node;
  }

  @Override
  public Output parse(Input input) {
    Output res = new Output();
    while (true) {
      int push = input.position;
      Output extra = node.parse(input);
      if (extra == PegParser.FAILED) {
        input.position = push;
        return res;
      }
      res.list.add(extra);
    }
  }
}
