package rocks.appconcept.javatools.parser.peg;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import rocks.appconcept.javatools.parser.PegParser;

/**
 * @author yanimetaxas
 */
public abstract class AstBase extends PegParser {

  private final Map<String, BitSet> bits = new HashMap<>();

  boolean isFailureCached(Input.Point point) {
    return bits.containsKey(point.getText()) && bits.get(point.getText()).get(point.getPosition());
  }

  @SuppressWarnings("SameReturnValue")
  Output cacheFailure(Input.Point point) {
    if (!bits.containsKey(point.getText())) {
      bits.put(point.getText(), new BitSet());
    }
    bits.get(point.getText()).set(point.getPosition());
    return FAILED;
  }
}
