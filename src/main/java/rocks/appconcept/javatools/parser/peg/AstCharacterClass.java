package rocks.appconcept.javatools.parser.peg;

/**
 * @author yanimetaxas
 */
public class AstCharacterClass extends AstBase {

  private final String characters;

  public AstCharacterClass(String character) {
    this.characters = character;
  }

  @Override
  public Output parse(Input input) {
    if (input.has(1)) {
      char ch = input.input.charAt(input.position);
      if (characters.indexOf(ch) >= 0) {
        input.position += 1;
        return new Output(Character.toString(ch));
      }
    }
    return FAILED;
  }
}
