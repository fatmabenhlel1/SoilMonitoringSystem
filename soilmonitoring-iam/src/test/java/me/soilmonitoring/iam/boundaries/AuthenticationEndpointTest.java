package me.soilmonitoring.iam.boundaries;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import me.soilmonitoring.iam.security.Argon2Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationEndpointTest {

    @InjectMocks
    private AuthenticationEndpoint endpoint;

    @Mock
    private PhoenixIAMManager phoenixIAMManager;

    @Mock
    private UriInfo uriInfo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authorize_whenClientIdIsMissing_returnsBadRequest() {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        when(uriInfo.getQueryParameters()).thenReturn(params);

        Response response = endpoint.authorize(uriInfo);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid client_id"));
    }

    @Test
    void authorize_whenTenantNotFound_returnsBadRequest() {
        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("client_id", "unknown-client");
        when(uriInfo.getQueryParameters()).thenReturn(params);

        when(phoenixIAMManager.findTenantByName("unknown-client")).thenReturn(null);

        Response response = endpoint.authorize(uriInfo);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Invalid client_id"));
    }

    @Test
    void authorize_whenCodeChallengeMethodIsInvalid_returnsBadRequest() {
        Tenant tenant = mock(Tenant.class);
        when(tenant.getName()).thenReturn("my-client");
        when(tenant.getSupportedGrantTypes()).thenReturn(String.valueOf(new HashSet<>(Set.of("authorization_code"))));
        when(tenant.getRedirectUri()).thenReturn("https://client.example/callback");

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("client_id", tenant.getName());
        params.putSingle("redirect_uri", tenant.getRedirectUri());
        params.putSingle("response_type", "code");
        params.putSingle("code_challenge_method", "plain");
        when(uriInfo.getQueryParameters()).thenReturn(params);

        when(phoenixIAMManager.findTenantByName(tenant.getName())).thenReturn(tenant);

        Response response = endpoint.authorize(uriInfo);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("code_challenge_method must be 'S256'"));
    }

    @Test
    void login_whenPasswordMatchesAndGrantPresent_redirectsToClient() throws Exception {
        String username = "alice";
        String rawPassword = "password";
        Identity identity = mock(Identity.class);
        when(identity.getPassword()).thenReturn("$argon2$fakeHash");
        when(phoenixIAMManager.findIdentityByUsername(username)).thenReturn(identity);

        Grant grant = mock(Grant.class);
        when(grant.getApprovedScopes()).thenReturn("read write");
        when(phoenixIAMManager.findGrant(eq("my-client"), anyString())).thenReturn(Optional.of(grant));

        String cookieValue = "my-client#read$https://client.example/callback";
        Cookie cookie = new Cookie(AuthenticationEndpoint.CHALLENGE_RESPONSE_COOKIE_ID, cookieValue);

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("response_type", "code");
        params.putSingle("code_challenge", "aCodeChallenge");
        params.putSingle("state", "XYZ");
        when(uriInfo.getQueryParameters()).thenReturn(params);

        try (MockedStatic<Argon2Utility> mocked = mockStatic(Argon2Utility.class)) {
            mocked.when(() -> Argon2Utility.check(identity.getPassword(), rawPassword.toCharArray())).thenReturn(true);

            Response response = endpoint.login(cookie, username, rawPassword, uriInfo);

            assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
            Object location = response.getMetadata().getFirst("Location");
            assertNotNull(location);
            String locStr = location.toString();
            assertTrue(locStr.contains("code="));
            assertTrue(locStr.contains("state=XYZ"));
        }
    }

    @Test
    void login_whenPasswordMismatch_redirectsWithError() throws Exception {
        String username = "bob";
        String rawPassword = "bad";
        Identity identity = mock(Identity.class);
        when(identity.getPassword()).thenReturn("$argon2$hash");
        when(phoenixIAMManager.findIdentityByUsername(username)).thenReturn(identity);

        String cookieValue = "client#scope$https://client.example/callback";
        Cookie cookie = new Cookie(AuthenticationEndpoint.CHALLENGE_RESPONSE_COOKIE_ID, cookieValue);

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        when(uriInfo.getQueryParameters()).thenReturn(params);

        try (MockedStatic<Argon2Utility> mocked = mockStatic(Argon2Utility.class)) {
            mocked.when(() -> Argon2Utility.check(identity.getPassword(), rawPassword.toCharArray())).thenReturn(false);

            Response response = endpoint.login(cookie, username, rawPassword, uriInfo);

            assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
            Object location = response.getMetadata().getFirst("Location");
            assertNotNull(location);
            String locStr = location.toString();
            assertTrue(locStr.contains("error"));
        }
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