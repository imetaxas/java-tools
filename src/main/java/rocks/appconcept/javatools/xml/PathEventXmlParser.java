package rocks.appconcept.javatools.xml;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

/**
 * @author yanimetaxas
 */
public abstract class PathEventXmlParser {

    private static final ByteArrayInputStream EMPTY_BYTE_STREAM = new ByteArrayInputStream(new byte[0]);

    protected void beforeOpen(String path) {
    }

    protected void attribute(String path, String attribute, String value) {
    }

    protected void afterOpen(String path) {
    }

    protected void text(String path, String value) {
    }

    protected void afterClose(String path) {
    }

    public void parse(Reader reader) throws IOException {
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            saxParserFactory.setValidating(false);
            SAXParser saxParser = saxParserFactory.newSAXParser();

            saxParser.parse(new InputSource(reader), new InternalHandler());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private final class InternalHandler extends DefaultHandler {
        private Deque<String> paths = new ArrayDeque<>(Collections.singletonList(""));
        private String currentPath = "";
        private StringBuilder currentText = new StringBuilder();
        private boolean allowText = false;

        public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
            return new InputSource(EMPTY_BYTE_STREAM);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentPath = currentPath + "/" + qName;
            paths.push(currentPath);
            beforeOpen(currentPath);

            int attributeCount = attributes.getLength();
            for (int i = 0; i < attributeCount; i++) {
                attribute(currentPath, attributes.getLocalName(i), attributes.getValue(i));
            }

            afterOpen(currentPath);
            currentText.setLength(0);
            allowText = true;
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (allowText) {
                currentText.append(ch, start, length);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (currentText.length() > 0) {
                text(currentPath, currentText.toString());
                currentText.setLength(0);
            }
            allowText = false;
            afterClose(currentPath);
            paths.pop();
            currentPath = paths.peek();
        }
    }
}
