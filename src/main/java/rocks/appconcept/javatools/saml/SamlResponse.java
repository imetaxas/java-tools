package rocks.appconcept.javatools.saml;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import rocks.appconcept.javatools.xml.DomWrapper;

/**
 * @author yanimetaxas
 */
public class SamlResponse {

  private static final String SAMLP = "urn:oasis:names:tc:SAML:2.0:protocol";
  private static final String SAML = "urn:oasis:names:tc:SAML:2.0:assertion";

  public String issuer;
  public Map<String, List<String>> attributes;
  public String username;
  public boolean isValid = false;
  public String errorMessage;

  public static SamlResponse parse(String samlResponse, String certificate, String privateKey) {
    try {
      // parse document
      DomWrapper root = DomWrapper
          .parseNS(new ByteArrayInputStream(parseBase64EncodedResponse(samlResponse)));
      if (!root.hasName(SAMLP, "Response")) {
        return fail("Invalid saml response rootnode");
      }
      // require signature for entire response
      if (certificate != null && !root.validateSignature(certificate)) {
        return fail("Missing signature for response");
      }

      if (privateKey != null && root.getOnlyNamedChild(SAML, "Assertion") != null) {
        return fail("Private key specified but unencrypted assertion received");
      }

      DomWrapper encryptedAssertion = root.getOnlyNamedChild(SAML, "EncryptedAssertion");
      if (encryptedAssertion != null) {
        if (privateKey == null) {
          return fail("Encrypted assertion received, but no private key specified");
        }
        encryptedAssertion.decryptElement(privateKey);
      }

      // verify issuer
      String issuer = root.getOnlyNamedChild(SAML, "Issuer").getElementText();

      String status = root.getOnlyNamedChild(SAMLP, "Status").getOnlyNamedChild(SAMLP, "StatusCode")
          .getAttributeValue("Value");
      if (!status.equals("urn:oasis:names:tc:SAML:2.0:status:Success")) {
        return fail("Invalid saml response status (" + status + ")");
      }

      // check that there is only one assertion
      DomWrapper assertion = root.getOnlyNamedChild(SAML, "Assertion");

      // check that there is only one subject
      DomWrapper subject = assertion.getOnlyNamedChild(SAML, "Subject");

      // handle encrypted id
      DomWrapper encryptedID = subject.getOnlyNamedChild(SAML, "EncryptedID");
      if (encryptedID != null) {
        encryptedID.decryptElement(privateKey);
      }

      // check that there is only one name id
      DomWrapper nameID = subject.getOnlyNamedChild(SAML, "NameID");
      if (nameID == null) {
        return fail("No NameID element found");
      }
      String nameid = nameID.getElementText();

      Map<String, List<String>> attributes = new HashMap<>();
      DomWrapper attributeStatement = assertion.getOnlyNamedChild(SAML, "AttributeStatement");
      for (DomWrapper attribute : attributeStatement.getChildren(SAML, "Attribute")) {
        String attributeName = attribute.getAttributeValue("Name");
        attributes.put(attributeName,
            attribute.getChildren(SAML, "AttributeValue").stream().map(DomWrapper::getElementText)
                .collect(Collectors.toList()));
      }

      return success(issuer, nameid, attributes);

    } catch (Exception exception) {
      return fail("Exception: " + exception.getMessage());
    }
  }

  private static byte[] parseBase64EncodedResponse(String samlResponse) {
    samlResponse = samlResponse.replaceAll("[^A-Za-z0-9+/=]", "");
    return Base64.getDecoder().decode(samlResponse);
  }


  private static SamlResponse fail(String errorMessage) {
    SamlResponse parsedSamlResponse = new SamlResponse();
    parsedSamlResponse.errorMessage = errorMessage;
    return parsedSamlResponse;
  }

  private static SamlResponse success(String issuer, String nameid,
      Map<String, List<String>> attributes) {
    SamlResponse response = new SamlResponse();
    response.issuer = issuer;
    response.username = nameid;
    response.attributes = attributes;
    response.isValid = true;
    return response;
  }
}
