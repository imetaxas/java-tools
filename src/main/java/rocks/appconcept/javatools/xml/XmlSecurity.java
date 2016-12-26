package rocks.appconcept.javatools.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrappers for xml signature validation and xml decryption.
 *
 * @author yanimetaxas
 */
public class XmlSecurity {

    private static final String XMLENC = "http://www.w3.org/2001/04/xmlenc#";
    private static final String XMLDSIG = "http://www.w3.org/2000/09/xmldsig#";

    public static boolean validateSignature(Element element, String certificate) throws CertificateException, MarshalException, XMLSignatureException {
        Certificate cert = parseCertificate(certificate);

        element.setIdAttributeNS(null, "ID", true);

        // verify signatures using the expected certificate
        NodeList nl = element.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            return false;
        }
        // check all signatures
        for (int i = 0; i < nl.getLength(); i++) {
            DOMValidateContext valContext = new DOMValidateContext(KeySelector.singletonKeySelector(cert.getPublicKey()), nl.item(i));
            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = factory.unmarshalXMLSignature(valContext);
            boolean coreValidity = signature.validate(valContext);
            if (!coreValidity) {
                throw new RuntimeException("Invalid signature");
            }

            //noinspection unchecked
            for (Reference reference : (List<Reference>) signature.getSignedInfo().getReferences()) {
                if (reference.getURI().equals("#" + element.getAttribute("ID"))) {
                    return true;
                }
            }
        }
        throw new RuntimeException("Signature doesn't include element");
    }

    public static void decryptElement(Element encryptedElement, String privateKeyString) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ParserConfigurationException, IOException, SAXException {
        Element encryptedDataDOM = getOnlyNamedChild(encryptedElement, XMLENC, "EncryptedData");
        Element encKeyElement = getOnlyNamedElementOrNull(encryptedDataDOM, XMLENC, "EncryptedKey");
        if (encKeyElement == null)
            encKeyElement = getOnlyNamedElement(encryptedElement, XMLENC, "EncryptedKey");

        // validate our private key with the certificate for the encrypted key
        PrivateKey privateKey = parseRSAKey(privateKeyString);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(((RSAPrivateCrtKey)privateKey).getModulus(), ((RSAPrivateCrtKey)privateKey).getPublicExponent()));
        Element X509Certificate = getOnlyNamedElementOrNull(encryptedDataDOM, XMLDSIG, "X509Certificate");
        if (X509Certificate != null) {
            Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(X509Certificate.getFirstChild().getNodeValue().replaceAll("[^A-Za-z0-9+/=]", ""))));
            if (!(cert.getPublicKey().equals(publicKey)))
                throw new RuntimeException("Specified private key does not match certificate from request");
        }

        // decrypt the random key using our specifiec private key
        Element keyCipherValue = getOnlyNamedElement(encKeyElement, XMLENC, "CipherValue");
        Algorithm keyEncryptionAlgo = Algorithm.getAlgorithm(getOnlyNamedChild(encKeyElement, XMLENC, "EncryptionMethod").getAttribute("Algorithm"));
        Cipher keyCipher = Cipher.getInstance(keyEncryptionAlgo.ciphertype);
        if (keyEncryptionAlgo.uri.equals("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p")) {
            // DR: we only support sha-1 here, other digest methods are actually allowed according to xmlsec, but I haven't seen it for SAML2 though
            keyCipher.init(Cipher.DECRYPT_MODE, privateKey, new OAEPParameterSpec("SHA-1", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT));
        } else {
            keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
        }
        byte[] keyBytes = keyCipher.doFinal(Base64.getDecoder().decode(keyCipherValue.getFirstChild().getNodeValue().replaceAll("[^A-Za-z0-9+/=]", "")));

        // decrypt the data using the newly decrypted key
        Element dataCipherValue = getOnlyNamedChild(getOnlyNamedChild(encryptedDataDOM, XMLENC, "CipherData"), XMLENC, "CipherValue");
        Algorithm dataEncryptionAlgo = Algorithm.getAlgorithm(getOnlyNamedChild(encryptedDataDOM, XMLENC, "EncryptionMethod").getAttribute("Algorithm"));
        Cipher dataCipher = Cipher.getInstance(dataEncryptionAlgo.ciphertype);
        byte[] ivbytes = new byte[dataCipher.getBlockSize()];
        new SecureRandom().nextBytes(ivbytes);
        dataCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, 0, Math.min(dataEncryptionAlgo.keySize / 8, keyBytes.length), dataEncryptionAlgo.keytype), new IvParameterSpec(ivbytes));
        byte[] dataBytes = dataCipher.doFinal(Base64.getDecoder().decode(dataCipherValue.getFirstChild().getNodeValue().replaceAll("[^A-Za-z0-9+/=]", "")));

        // parse the data as XML and insert back into the document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Element resultElement = builder.parse(new ByteArrayInputStream(dataBytes, dataCipher.getBlockSize(), dataBytes.length - dataCipher.getBlockSize())).getDocumentElement();
        encryptedElement.getParentNode().appendChild(encryptedElement.getOwnerDocument().importNode(resultElement, true));
    }

    private static Element getOnlyNamedElement(Element parent, String namespace, String localname) {
        NodeList elementsByTagNameNS = parent.getElementsByTagNameNS(namespace, localname);
        if (elementsByTagNameNS.getLength() > 1) throw new RuntimeException("Multiple elements " + namespace + ":" + localname);
        if (elementsByTagNameNS.getLength() == 0) throw new RuntimeException("Missing element " + namespace + ":" + localname);
        return (Element) elementsByTagNameNS.item(0);
    }

    private static Element getOnlyNamedElementOrNull(Element parent, String namespace, String localname) {
        NodeList elementsByTagNameNS = parent.getElementsByTagNameNS(namespace, localname);
        if (elementsByTagNameNS.getLength() > 1) return null;
        if (elementsByTagNameNS.getLength() == 0) return null;
        return (Element) elementsByTagNameNS.item(0);
    }

    private static Element getOnlyNamedChild(Element parent, String namespace, String localname) {
        NodeList childNodes = parent.getChildNodes();
        Element child = null;
        for (int i = childNodes.getLength()-1; i >= 0; i--) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getNamespaceURI().equals(namespace) && item.getLocalName().equals(localname)) {
                    if (child != null) {
                        throw new RuntimeException("Multiple nodes with local name " + localname);
                    }
                    child = (Element) item;
                }
            }
        }
        return child;
    }

    public static PrivateKey parseRSAKey(String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String cleanedKey = key;
        cleanedKey = cleanedKey.replace("-----BEGIN PRIVATE KEY-----", "");
        cleanedKey = cleanedKey.replace("-----END PRIVATE KEY-----", "");
        cleanedKey = cleanedKey.replaceAll("[^A-Za-z0-9+/=]", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(cleanedKey)));
    }

    public static Certificate parseCertificate(String cert) throws CertificateException {
        String cleanedCert = cert;
        cleanedCert = cleanedCert.replace("-----BEGIN CERTIFICATE-----", "");
        cleanedCert = cleanedCert.replace("-----END CERTIFICATE-----", "");
        cleanedCert = cleanedCert.replaceAll("[^A-Za-z0-9+/=]", "");
        return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cleanedCert)));
    }

    /**
     * Supported algorithms for XML Security (mainly for encrypted assertions in SAML2) and their interpretations for use with the Cipher class.
     */
    public static class Algorithm {
        public String uri;
        public String keytype;
        public String ciphertype;
        public int keySize;
        private static Map<String, Algorithm> index = new HashMap<>();

        static {
            new Algorithm("http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES", "AES/CBC/ISO10126Padding", 128);
            new Algorithm("http://www.w3.org/2001/04/xmlenc#aes192-cbc", "AES", "AES/CBC/ISO10126Padding", 192);
            new Algorithm("http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES", "AES/CBC/ISO10126Padding", 256);
            new Algorithm("http://www.w3.org/2001/04/xmlenc#tripledes-cbc", "DESede", "DESede/CBC/ISO10126Padding", 192);
            new Algorithm("http://www.w3.org/2001/04/xmlenc#rsa-1_5", null, "RSA/ECB/PKCS1Padding", 0);
            new Algorithm("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", null, "RSA/ECB/OAEPPadding", 0);
        }

        private Algorithm(String uri, String keytype, String ciphertype, int keySize) {
            this.uri = uri;
            this.keytype = keytype;
            this.ciphertype = ciphertype;
            this.keySize = keySize;
            index.put(this.uri, this);
        }

        public static Algorithm getAlgorithm(String keyEncAlgo) {
            if (!index.containsKey(keyEncAlgo))
                throw new UnsupportedOperationException("Unsupported algorithm: " + keyEncAlgo);
            return index.get(keyEncAlgo);
        }
    }
}
