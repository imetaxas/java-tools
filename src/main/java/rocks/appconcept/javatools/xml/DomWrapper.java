package rocks.appconcept.javatools.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.XMLConstants;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author yanimetaxas
 */
public class DomWrapper {

  private Element node;

  public DomWrapper(Element node) {
    this.node = node;
  }

  public boolean hasName(String namespaceURI, String localName) {
    return localName.equals(node.getLocalName()) && namespaceURI.equals(node.getNamespaceURI());
  }

  public boolean hasName(String localName) {
    return localName.equals(node.getLocalName());
  }

  public DomWrapper getOnlyNamedChild(String namespaceURI, String localName) {
    NodeList childNodes = node.getChildNodes();
    Element child = null;
    for (int i = childNodes.getLength() - 1; i >= 0; i--) {
      Node item = childNodes.item(i);
      if (item.getNodeType() == Node.ELEMENT_NODE) {
        if (item.getLocalName().equals(localName) && item.getNamespaceURI().equals(namespaceURI)) {
          if (child != null) {
            throw new RuntimeException("Multiple nodes with local name " + localName);
          }
          child = (Element) item;
        }
      }
    }
    return wrap(child);
  }

  public DomWrapper getOnlyNamedChild(String localName) {
    NodeList childNodes = node.getChildNodes();
    Element child = null;
    for (int i = childNodes.getLength() - 1; i >= 0; i--) {
      Node item = childNodes.item(i);
      if (item.getNodeType() == Node.ELEMENT_NODE) {
        if (localName.equals(item.getNodeName())) {
          if (child != null) {
            throw new RuntimeException("Multiple nodes with local name " + localName);
          }
          child = (Element) item;
        }
      }
    }
    return wrap(child);
  }

  public List<DomWrapper> getChildren(String namespaceURI, String localName) {
    NodeList childNodes = node.getChildNodes();
    List<DomWrapper> nodes = new ArrayList<>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getNodeType() == Node.ELEMENT_NODE) {
        if (localName.equals(item.getLocalName()) && namespaceURI.equals(item.getNamespaceURI())) {
          nodes.add(wrap((Element) item));
        }
      }
    }
    return nodes;
  }

  public List<DomWrapper> getChildren(String localName) {
    NodeList childNodes = node.getChildNodes();
    List<DomWrapper> nodes = new ArrayList<>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getNodeType() == Node.ELEMENT_NODE) {
        if (localName.equals(item.getNodeName())) {
          nodes.add(wrap((Element) item));
        }
      }
    }
    return nodes;
  }

  public List<DomWrapper> getChildren() {
    NodeList childNodes = node.getChildNodes();
    List<DomWrapper> nodes = new ArrayList<>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node item = childNodes.item(i);
      if (item.getNodeType() == Node.ELEMENT_NODE) {
        nodes.add(wrap((Element) item));
      }
    }
    return nodes;
  }

  public String getElementText() {
    return node.getFirstChild().getNodeValue();
  }

  public String getAttributeValue(String namespaceURI, String name) {
    Attr attributeNodeNS = node.getAttributeNodeNS(namespaceURI, name);
    return attributeNodeNS == null ? null : attributeNodeNS.getTextContent();
  }

  public String getAttributeValue(String name) {
    Attr attributeNode = node.getAttributeNode(name);
    return attributeNode == null ? null : attributeNode.getTextContent();
  }

  public Element element() {
    return node;
  }

  public boolean validateSignature(String certificate)
      throws CertificateException, XMLSignatureException, MarshalException {
    return XmlSecurity.validateSignature(node, certificate);
  }

  public void decryptElement(String privateKey)
      throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, SAXException, ParserConfigurationException, InvalidKeySpecException, IllegalBlockSizeException {
    XmlSecurity.decryptElement(node, privateKey);
  }

  public String toString() {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(node), new StreamResult(writer));
      return writer.toString();
    } catch (Exception e) {
      return "Unable to stringify xml: " + e.getClass().getName() + ": " + e.getMessage();
    }
  }

  public static DomWrapper wrap(Element element) {
    return element == null ? null : new DomWrapper(element);
  }

  public static DomWrapper parseNS(InputStream is)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setNamespaceAware(true);
    DocumentBuilder builder = dbf.newDocumentBuilder();
    Document doc = builder.parse(is);
    return wrap(doc.getDocumentElement());
  }

  public static DomWrapper parse(InputStream is)
      throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setNamespaceAware(false);
    DocumentBuilder builder = dbf.newDocumentBuilder();
    Document doc = builder.parse(is);
    return wrap(doc.getDocumentElement());
  }
}
