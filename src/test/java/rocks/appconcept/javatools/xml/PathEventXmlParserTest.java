package rocks.appconcept.javatools.xml;

import junit.framework.TestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yanimetaxas
 */
public class PathEventXmlParserTest extends TestCase {

    public void testVerifyEvents() throws IOException, SAXException, ParserConfigurationException {
        String xml = "<start>            <sub attr=\"attr&amp;val\" attr2=\"attrval2\">text</sub>           </start>";
        final List<String> expected = new ArrayList<String>(Arrays.asList(
                "beforeOpen:/start",
                "afterOpen:/start",
                "beforeOpen:/start/sub",
                "attribute:/start/sub@attr=attr&val",
                "attribute:/start/sub@attr2=attrval2",
                "afterOpen:/start/sub",
                "text:/start/sub=text",
                "afterClose:/start/sub",
                "afterClose:/start"
        ));
        new PathEventXmlParser() {
            private void expect(String event) {
                if (expected.isEmpty()) fail("Too many events (" + event + ")");
                assertEquals("Incorrect event", expected.remove(0), event);
            }

            protected void beforeOpen(String path) {
                expect("beforeOpen:" + path);
            }

            protected void afterOpen(String path) {
                expect("afterOpen:" + path);
            }

            protected void afterClose(String path) {
                expect("afterClose:" + path);
            }

            protected void text(String path, String value) {
                expect("text:" + path + "=" + value);
            }

            protected void attribute(String path, String attribute, String value) {
                expect("attribute:" + path + "@" + attribute + "=" + value);
            }
        }.parse(new StringReader(xml));
    }

    public void testVerifyHugeString() throws IOException, SAXException, ParserConfigurationException {
        final StringBuilder sb = new StringBuilder("        0123456789012345678901234567890123456789012345678901234567890123\n");
        while (sb.length() < 1024 * 100) sb.append(sb);
        new PathEventXmlParser() {
            protected void text(String path, String value) {
                assertEquals(sb.toString(), value);
            }
        }.parse(new StringReader("<xml>" + sb.toString() + "</xml>"));
    }

    public void testXmlWithDTDReference() throws Exception {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE pricing-section SYSTEM \"/data/mks/marketing-data.dtd\" []>\n" +
                "<?pubinf sec-prc;bcsd;SD Series;ftl;01/24/2014;bcsd1;EN;20140110_154111;4.00?>\n" +
                "<?mgrps bcsd1?>\n" +
                "<?models 2;108SD=m176;114SD=m177?>\n" +
                "<pricing-section section-nbr=\"11\" section-name=\"EngEquip\" />\n";
        new PathEventXmlParser() {
            protected void text(String path, String value) {
            }
        }.parse(new StringReader(xml));

    }
}
