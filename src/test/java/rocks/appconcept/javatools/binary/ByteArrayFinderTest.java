package rocks.appconcept.javatools.binary;

import java.util.List;
import junit.framework.TestCase;
import rocks.appconcept.javatools.CoverageTool;

/**
 * @author yanimetaxas
 */
public class ByteArrayFinderTest extends TestCase {

  public void testHappyPath() throws Exception {
    CoverageTool.callPrivateConstructor(ByteArrayFinder.class);

    byte[] pattern = {1, 2, 3};
    byte[] data = {0, 0, 0, 0, 1, 2, 3, 0, 0, 0, 1, 2, 1, 2, 1, 1, 1, 3, 1, 3, 1, 2, 3};

    List<Integer> allIndexes = ByteArrayFinder.findAllIndexes(pattern, data);
    assertEquals(4, allIndexes.remove(0).intValue());
    assertEquals(20, allIndexes.remove(0).intValue());
    assertTrue(allIndexes.isEmpty());
  }

  public void testNoExistence() throws Exception {
    byte[] pattern = {1, 2, 3};
    byte[] data = {0, 0, 0, 0};
    List<Integer> allIndexes = ByteArrayFinder.findAllIndexes(pattern, data);
    assertEquals(0, allIndexes.size());
  }

  public void testStrangePattern() throws Exception {
    byte[] pattern = {0, 0, 0, 1, 2, 3, 0, 0, 0};
    byte[] data = {0, 0, 0, 0};
    List<Integer> allIndexes = ByteArrayFinder.findAllIndexes(pattern, data);
    assertEquals(0, allIndexes.size());
  }
}