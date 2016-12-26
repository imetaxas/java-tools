package rocks.appconcept.javatools.xml;

import junit.framework.TestCase;

import java.io.StringReader;

/**
 * @author yanimetaxas
 */
public class XmlReducerTest extends TestCase {

    public void testBasicUsage() throws Exception {

        XmlReducer reducer = new XmlReducer();

        Object node = reducer.build(new StringReader("<root><element>text</element></root>"));
        assertTrue(node instanceof XmlReducerNode);

        Object build = reducer.build(new StringReader("<root><inner>innertext</inner></root>"), new TestReductionHandler());
        assertEquals("hello: innertext", build);
    }

    public static class TestReductionHandler extends ReductionHandlerBase {
        @SuppressWarnings("unused")
        public String reduceRoot(XmlReducerNode node) {
            return "hello: " + node.getText("inner");
        }
    }
}