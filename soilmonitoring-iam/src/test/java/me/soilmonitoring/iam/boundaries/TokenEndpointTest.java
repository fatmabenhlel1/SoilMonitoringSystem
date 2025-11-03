package me.soilmonitoring.iam.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.security.AuthorizationCode;
import me.soilmonitoring.iam.security.JwtManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenEndpointTest {

    @Mock
    private PhoenixIAMManager phoenixIAMManager;

    @Mock
    private JwtManager jwtManager;

    @InjectMocks
    private TokenEndpoint tokenEndpoint;

    private final Set<String> supportedGrantTypes = Set.of("authorization_code", "refresh_token");

    @BeforeEach
    void setUp() {
        // Initialize mocks if necessary
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
    void token_shouldHandleAuthorizationCodeGrant() throws Exception {
        AuthorizationCode decoded = mock(AuthorizationCode.class);
        when(decoded.tenantName()).thenReturn("tenant123");
        when(decoded.identityUsername()).thenReturn("user123");
        when(decoded.approvedScopes()).thenReturn("read write");

        when(AuthorizationCode.decode("authCode", "codeVerifier")).thenReturn(decoded);
        when(phoenixIAMManager.getRoles("user123")).thenReturn(new String[]{"role1", "role2"});
        when(jwtManager.generateToken(anyString(), anyString(), anyString(), any(String[].class)))
                .thenReturn("accessToken", "refreshToken");

        Response response = tokenEndpoint.token("authorization_code", "authCode", "codeVerifier");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("Bearer", entity.get("token_type"));
        assertEquals("accessToken", entity.get("access_token"));
        assertEquals("refreshToken", entity.get("refresh_token"));
    }

    @Test
    void token_shouldHandleRefreshTokenGrant() throws Exception {
        when(jwtManager.verifyToken("authCode")).thenReturn(Map.of("tenant_id", "tenant123", "scope", "read write", "groups", "[\"role1\"]", "sub", "user123"));
        when(jwtManager.verifyToken("codeVerifier")).thenReturn(Map.of("tenant_id", "tenant123", "scope", "read write", "sub", "user123"));
        when(jwtManager.generateToken(anyString(), anyString(), anyString(), any(String[].class)))
                .thenReturn("newAccessToken", "newRefreshToken");

        Response response = tokenEndpoint.token("refresh_token", "authCode", "codeVerifier");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("Bearer", entity.get("token_type"));
        assertEquals("newAccessToken", entity.get("access_token"));
        assertEquals("newRefreshToken", entity.get("refresh_token"));
    }

    @Test
    void token_shouldReturnUnauthorizedForInvalidRefreshToken() {
        when(jwtManager.verifyToken("authCode")).thenReturn(Map.of());
        when(jwtManager.verifyToken("codeVerifier")).thenReturn(Map.of());

        Response response = tokenEndpoint.token("refresh_token", "authCode", "codeVerifier");

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Can't get token"));
    }
}