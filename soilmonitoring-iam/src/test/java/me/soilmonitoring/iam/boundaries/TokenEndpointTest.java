package me.soilmonitoring.iam.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import me.soilmonitoring.iam.security.AuthorizationCode;
import me.soilmonitoring.iam.security.JwtManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
class TokenEndpointTest {

    @Inject
    private PhoenixIAMManager phoenixIAMManager;

    @Inject
    private JwtManager jwtManager;

    @Inject
    private TokenEndpoint tokenEndpoint;

    private final Set<String> supportedGrantTypes = Set.of("authorization_code", "refresh_token");

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(TokenEndpoint.class, PhoenixIAMManager.class, JwtManager.class, AuthorizationCode.class, Identity.class, Tenant.class)
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");
    }

    @Test
    void token_shouldHandleAuthorizationCodeGrant() throws Exception {
        AuthorizationCode decoded = new AuthorizationCode(
                "tenant123", "user123", "read write", System.currentTimeMillis() + 3600000, "http://redirect.uri"
        );

        Identity identity = new Identity();
        identity.setUsername(decoded.identityUsername());
        phoenixIAMManager.saveIdentity(identity);

        Tenant tenant = new Tenant();
        tenant.setName(decoded.tenantName());
        phoenixIAMManager.saveTenant(tenant);

        Response response = tokenEndpoint.token("authorization_code", "authCode", "codeVerifier");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("Bearer", entity.get("token_type"));
        assertNotNull(entity.get("access_token"));
        assertNotNull(entity.get("refresh_token"));
    }

    @Test
    void token_shouldReturnBadRequestForMissingGrantType() {
        Response response = tokenEndpoint.token(null, "authCode", "codeVerifier");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("grant_type is required"));
    }

    @Test
    void token_shouldReturnBadRequestForUnsupportedGrantType() {
        Response response = tokenEndpoint.token("invalid_grant", "authCode", "codeVerifier");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("unsupported_grant_type"));
    }

    @Test
    void token_shouldHandleRefreshTokenGrant() throws Exception {
        jwtManager.verifyToken("authCode");
        jwtManager.verifyToken("codeVerifier");

        Response response = tokenEndpoint.token("refresh_token", "authCode", "codeVerifier");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("Bearer", entity.get("token_type"));
        assertNotNull(entity.get("access_token"));
        assertNotNull(entity.get("refresh_token"));
    }

    @Test
    void token_shouldReturnUnauthorizedForInvalidRefreshToken() {
        jwtManager.verifyToken("authCode");
        jwtManager.verifyToken("codeVerifier");

        Response response = tokenEndpoint.token("refresh_token", "authCode", "codeVerifier");

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Can't get token"));
    }
}