package rocks.appconcept.javatools.saml;

import rocks.appconcept.javatools.io.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * @author yanimetaxas
 */
public class AuthenticationRequest {

    private final String id;
    private final String entityId;
    private final String acsUrl;
    private final String destination;

    public AuthenticationRequest(String id, String entityId, String acsUrl, String destination) {
        this.id = id;
        this.entityId = entityId;
        this.acsUrl = acsUrl;
        this.destination = destination;
    }

    public String getRedirectParameter() {
        try {
            byte[] deflated = StreamUtils.captureBytes((stream) -> {
                DeflaterOutputStream deflater = new DeflaterOutputStream(stream, new Deflater(9, true));
                deflater.write(getRequestXml());
                deflater.close();
            });
            return Base64.getEncoder().encodeToString(deflated);
        } catch (IOException ioe) {
            throw new RuntimeException("Unexpected IOException", ioe);
        }
    }

    public String getPostHtml(String relayState) {
        String html = "<!doctype html><body onload=\"document.forms[0].submit()\">";
        html += "<form id=\"form\" action=\"" + destination + "\" method=\"POST\">";
        html += "<input type=\"hidden\" name=\"SAMLRequest\" value=\"" + Base64.getEncoder().encodeToString(getRequestXml()) + "\">";
        html += "<input type=\"hidden\" name=\"RelayState\" value=\"" + relayState + "\">";
        html += "<noscript><input type=\"submit\" value=\"Continue\"></noscript>";
        html += "</form>";
        html += "</body>";
        return html;
    }

    public byte[] getRequestXml() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String issueInstant = String.format("%04d-%02d-%02dT%02d:%02d:%02dZ",
                utc.get(Calendar.YEAR), utc.get(Calendar.MONTH) + 1, utc.get(Calendar.DATE),
                utc.get(Calendar.HOUR_OF_DAY), utc.get(Calendar.MINUTE), utc.get(Calendar.SECOND));

        return ("<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"" +
                " ID=\"_" + escapeXml(id) + "\"" +
                " Version=\"2.0\"" +
                " IssueInstant=\"" + escapeXml(issueInstant) + "\"" +
                " Destination=\"" + escapeXml(destination) + "\"" +
                " AssertionConsumerServiceURL=\"" + escapeXml(acsUrl) + "\"" + // optional
                ">" +
                "<saml:Issuer>" + escapeXml(entityId) + "</saml:Issuer>" +
                "</samlp:AuthnRequest>").getBytes(StandardCharsets.UTF_8);
    }


    public static String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("\'", "&apos;");
    }
}
