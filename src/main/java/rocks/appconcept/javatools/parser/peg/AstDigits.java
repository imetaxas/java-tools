package rocks.appconcept.javatools.parser.peg;

import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public class AstDigits extends AstBase {

  private final int maxOccurrences;

  public AstDigits(int maxOccurrences) {
    this.maxOccurrences = maxOccurrences;
  }

  @Override
  public Output parse(Input input) {

    String data = input.input;
    int length = data.length();
    int end = input.position;
    int limit = maxOccurrences;
    while (limit > 0 && end < length && Character.isDigit(data.charAt(end))) {
      limit--;
      end++;
    }

    if (end > input.position) {
      String substring = data.substring(input.position, end);
      input.position = end;
      return new Output(substring);
    }

    return PegParser.FAILED;
  }
}
