package rocks.appconcept.javatools.reflection.maker;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import rocks.appconcept.javatools.reflection.ReflectionUtils;
/**
 * @author yanimetaxas
 * @since 30-Jan-18
 */
public class SubclassMakerTest {

  @Test
  public void testMakeSubclass() throws Exception {
    byte[] classFilename = SubclassMaker.makeSubclass("rocks.appconcept.javatools.reflection.maker.ClassFile", Class.forName("java.lang.Object"));

    //System.out.println(new String(classFilename));

    assertEquals(ReflectionUtils.getClassNameFromByteCode(classFilename), "rocks.appconcept.javatools.reflection.maker.ClassFile");
  }
}