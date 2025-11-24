package me.soilmonitoring.iam.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import me.soilmonitoring.iam.security.Argon2Utility;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ArquillianExtension.class)
class AuthenticationEndpointTest {

    @Inject
    private AuthenticationEndpoint endpoint;

    @Inject
    private PhoenixIAMManager phoenixIAMManager;

    @Inject
    private Argon2Utility argon2Utility;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(AuthenticationEndpoint.class, PhoenixIAMManager.class, Argon2Utility.class,
                        Tenant.class, Identity.class, Grant.class)
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");
    }

    @Test
    void authorize_whenClientIdIsMissing_returnsBadRequest() {
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        Response response = endpoint.authorize(uriInfo);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid client_id"));
    }

    @Test
    void authorize_whenTenantNotFound_returnsBadRequest() {
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.getFirst("client_id")).thenReturn("unknown-client");

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        Response response = endpoint.authorize(uriInfo);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid client_id"));
    }

    @Test
    void authorize_whenCodeChallengeMethodIsInvalid_returnsBadRequest() {
        Tenant tenant = new Tenant();
        tenant.setName("my-client");
        tenant.setSupportedGrantTypes("authorization_code");
        tenant.setRedirectUri("https://client.example/callback");
        phoenixIAMManager.saveTenant(tenant);

        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.getFirst("client_id")).thenReturn(tenant.getName());
        when(params.getFirst("redirect_uri")).thenReturn(tenant.getRedirectUri());
        when(params.getFirst("response_type")).thenReturn("code");
        when(params.getFirst("code_challenge_method")).thenReturn("plain");

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        Response response = endpoint.authorize(uriInfo);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("code_challenge_method must be 'S256'"));
    }

    @Test
    void login_whenPasswordMatchesAndGrantPresent_redirectsToClient() throws Exception {
        Tenant tenant = new Tenant();
        tenant.setName("my-client");
        phoenixIAMManager.saveTenant(tenant);

        Identity identity = new Identity();
        identity.setUsername("alice");
        identity.setPassword(argon2Utility.hash("password".toCharArray()));
        phoenixIAMManager.saveIdentity(identity);

        Grant grant = new Grant();
        grant.setApprovedScopes("read write");
        phoenixIAMManager.saveGrant(grant);

        String cookieValue = "my-client#read$https://client.example/callback";
        Cookie cookie = new Cookie(AuthenticationEndpoint.CHALLENGE_RESPONSE_COOKIE_ID, cookieValue);

        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.getFirst("response_type")).thenReturn("code");
        when(params.getFirst("code_challenge")).thenReturn("aCodeChallenge");
        when(params.getFirst("state")).thenReturn("XYZ");

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        Response response = endpoint.login(cookie, "alice", "password", uriInfo);

        assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
        Object location = response.getMetadata().getFirst("Location");
        assertNotNull(location);
        String locStr = location.toString();
        assertTrue(locStr.contains("code="));
        assertTrue(locStr.contains("state=XYZ"));
    }

    @Test
    void grantConsent_whenNoApprovedScopes_redirectsWithError() {
        String cookieValue = "client#requested$https://client.example/callback";
        Cookie cookie = new Cookie(AuthenticationEndpoint.CHALLENGE_RESPONSE_COOKIE_ID, cookieValue);

        Response response = endpoint.grantConsent(cookie, "", "YES", "bob");

        assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
        Object location = response.getMetadata().getFirst("Location");
        assertNotNull(location);
        assertTrue(location.toString().contains("error"));
    }
}