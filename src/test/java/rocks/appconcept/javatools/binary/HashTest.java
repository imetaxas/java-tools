package rocks.appconcept.javatools.binary;

import junit.framework.TestCase;
import rocks.appconcept.javatools.CoverageTool;

/**
 * @author yanimetaxas
 */
public class HashTest extends TestCase {

  public void testHappyPaths() throws Exception {
    CoverageTool.callPrivateConstructor(Hash.class);

    String hex = Hash.hex("yani".getBytes());
    assertEquals("79616e69", hex);

    String sha1 = Hash.sha1hex("yani".getBytes());
    assertEquals("99a123b54e4c74b11c40ce5934bc137b2fbf2531", sha1);

    String sha256 = Hash.sha256hex("yani".getBytes());
    assertEquals("f8fa8a82e9e8e4deffee0b52ec843f63f69990291a7316a57cdb552b70ffe99f", sha256);

    String md5 = Hash.md5hex("yani".getBytes());
    assertEquals("080840925a7e2087673145d83918c658", md5);
  }
}