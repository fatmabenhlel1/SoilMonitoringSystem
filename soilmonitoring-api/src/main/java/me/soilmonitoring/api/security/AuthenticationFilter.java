package me.soilmonitoring.api.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;
import java.util.logging.Logger;

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    @Inject
    private Logger logger;

    @Inject
    private JwtValidator jwtValidator;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warning("Missing or invalid Authorization header");
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\":\"Missing or invalid token\"}")
                            .build()
            );
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        Map<String, Object> claims = jwtValidator.validateToken(token);

        if (claims == null) {
            logger.warning("Token validation failed");
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{\"error\":\"Invalid or expired token\"}")
                            .build()
            );
            return;
        }

        // Store user info in request context for later use
        requestContext.setProperty("userId", claims.get("sub"));
        requestContext.setProperty("tenantId", claims.get("tenant-id"));
        requestContext.setProperty("userScopes", claims.get("scope"));
        requestContext.setProperty("userRoles", claims.get("groups"));

        logger.info("✅ Authenticated user: " + claims.get("sub"));
    }
}