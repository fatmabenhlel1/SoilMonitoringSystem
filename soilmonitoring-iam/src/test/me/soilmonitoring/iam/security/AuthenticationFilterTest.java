package me.soilmonitoring.iam.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private Config config;

    @Mock
    private JwtManager jwtManager;

    private AuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock configuration values
        when(config.getValue("mp.jwt.realm", String.class)).thenReturn("test-realm");
        when(config.getValue("jwt.claim.roles", String.class)).thenReturn("roles");

        // Initialize the filter with mocked dependencies
        filter = new AuthenticationFilter(config, jwtManager);
    }

    @Test
    void filter_withInvalidAuthorizationHeader_abortsWithUnauthorized() {
        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        filter.filter(requestContext);

        verify(requestContext).abortWith(argThat(response ->
                response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    void filter_withValidToken_setsSecurityContext() throws Exception {
        String token = "validToken";
        String authorizationHeader = "Bearer " + token;

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(authorizationHeader);
        when(requestContext.getSecurityContext()).thenReturn(mock(SecurityContext.class));

        when(jwtManager.verifyToken(token)).thenReturn(Map.of(
                "sub", "testUser",
                "roles", "[\"admin\", \"user\"]"
        ));

        filter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
        verify(requestContext).setSecurityContext(argThat(securityContext ->
                securityContext.getUserPrincipal().getName().equals("testUser") &&
                        securityContext.isUserInRole("admin") &&
                        securityContext.isUserInRole("user")
        ));
    }

    @Test
    void filter_withInvalidToken_abortsWithUnauthorized() throws Exception {
        String token = "invalidToken";
        String authorizationHeader = "Bearer " + token;

        when(requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn(authorizationHeader);

        when(jwtManager.verifyToken(token)).thenThrow(new RuntimeException("Invalid token"));

        filter.filter(requestContext);

        verify(requestContext).abortWith(argThat(response ->
                response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }
}