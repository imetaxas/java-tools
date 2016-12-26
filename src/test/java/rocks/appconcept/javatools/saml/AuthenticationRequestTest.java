package rocks.appconcept.javatools.saml;

import junit.framework.TestCase;

/**
 * @author yanimetaxas
 */
public class AuthenticationRequestTest extends TestCase {

    public void testHappy() throws Exception {

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("abc123", "entity", "http://localhost/acs", "http://remote/idp");

        String redirectParameter = authenticationRequest.getRedirectParameter();
        String postHtml = authenticationRequest.getPostHtml("relay");

        assertNotNull(redirectParameter);
        assertNotNull(postHtml);
    }
}