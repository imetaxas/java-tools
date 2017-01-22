package rocks.appconcept.javatools.xml;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import junit.framework.TestCase;

/**
 * @author yanimetaxas
 */
public class DomWrapperTest extends TestCase {

  public void testHappyPaths() throws Exception {

    String xml = "<test xmlns=\"testuri\" xmlns:a=\"testuri2\" a:spud=\"dill\" spud=\"sill\"><child/><child/><single>teext</single></test>";

    DomWrapper el = DomWrapper
        .parseNS(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
    assertNotNull(el.element());

    assertTrue(el.hasName("testuri", "test"));

    assertEquals(2, el.getChildren("testuri", "child").size());
    assertNotNull(el.getOnlyNamedChild("testuri", "single"));
    assertEquals("teext", el.getOnlyNamedChild("testuri", "single").getElementText());
    assertEquals("dill", el.getAttributeValue("testuri2", "spud"));
    assertEquals("sill", el.getAttributeValue("spud"));

    assertTrue(el.toString().contains("<test"));
  }

  public void testErrorHandling() throws Exception {
    String xml = "<test xmlns=\"testuri\"><double/><double/></test>";
    DomWrapper el = DomWrapper
        .parseNS(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
    try {
      el.getOnlyNamedChild("testuri", "double");
      fail();
    } catch (RuntimeException ignored) {
    }

    assertNull(DomWrapper.wrap(null));
  }

  public void testWithoutNamespace() throws Exception {
    String xml = "<test><double/><double/><spunk/></test>";
    DomWrapper el = DomWrapper
        .parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
    assertEquals(2, el.getChildren("double").size());
    assertNotNull(el.getOnlyNamedChild("spunk"));
    try {
      el.getOnlyNamedChild("double");
      fail();
    } catch (Exception ignored) {
    }
  }

  public void testGetMissingAttributes() throws Exception {
    {
      // missing attributes should give null and not empty string
      String xml = "<test existing=\"value\"></test>";
      DomWrapper el = DomWrapper
          .parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
      assertNull(el.getAttributeValue("missing"));
      assertEquals("value", el.getAttributeValue("existing"));
    }
    {
      // missing attributes should give null and not empty string
      String xml = "<test xmlns=\"testuri\" xmlns:a=\"testuri2\" a:existing=\"value\"></test>";
      DomWrapper el = DomWrapper
          .parseNS(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
      assertNull(el.getAttributeValue("testuri", "missing"));
      assertNull(el.getAttributeValue("testuri2", "missing"));
      assertNull(el.getAttributeValue("testuri", "existing"));
      assertEquals("value", el.getAttributeValue("testuri2", "existing"));
    }
  }
}