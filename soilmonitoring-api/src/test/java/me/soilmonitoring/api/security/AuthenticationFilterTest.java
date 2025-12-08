package me.soilmonitoring.api.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFilterTest {

    private AuthenticationFilter filter;
    private JwtValidator jwtValidator;
    private ContainerRequestContext requestContext;

    @BeforeEach
    void setup() {
        filter = new AuthenticationFilter();
        jwtValidator = mock(JwtValidator.class);
        requestContext = mock(ContainerRequestContext.class);

        // injection par refexion
        inject(filter, "jwtValidator", jwtValidator);
        inject(filter, "logger", Logger.getLogger("TestLogger"));
    }

    // Utilitaire pour injecter un field private
    private void inject(Object target, String field, Object val) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, val);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void testMissingAuthorizationHeader() {
        when(requestContext.getHeaderString("Authorization")).thenReturn(null);

        filter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());

        assertEquals(401, captor.getValue().getStatus());
    }

    @Test
    void testInvalidToken() {
        when(requestContext.getHeaderString("Authorization")).thenReturn("Bearer abc");
        when(jwtValidator.validateToken("abc")).thenReturn(null);

        filter.filter(requestContext);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(captor.capture());

        assertEquals(401, captor.getValue().getStatus());
    }

    @Test
    void testValidToken() {
        when(requestContext.getHeaderString("Authorization")).thenReturn("Bearer goodtoken");

        when(jwtValidator.validateToken("goodtoken")).thenReturn(
                Map.of(
                        "sub", "user123",
                        "tenant-id", "tenantA",
                        "scope", "read write",
                        "groups", "admin"
                )
        );

        filter.filter(requestContext);

        verify(requestContext).setProperty("userId", "user123");
        verify(requestContext).setProperty("tenantId", "tenantA");
        verify(requestContext).setProperty("userScopes", "read write");
        verify(requestContext).setProperty("userRoles", "admin");
    }
}
