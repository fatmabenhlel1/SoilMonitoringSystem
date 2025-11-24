package me.soilmonitoring.iam.security;

import jakarta.annotation.Priority;
import jakarta.ejb.EJBException;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonString;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.Config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Arrays;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private final Config config;
    private final JwtManager jwtManager;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Inject
    public AuthenticationFilter(Config config, JwtManager jwtManager) {
        this.config = config;
        this.jwtManager = jwtManager;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            var claims = jwtManager.verifyToken(token);
            if (!claims.isEmpty()) {
                String[] roles = Json.createReader(new StringReader(claims.get(config.getValue("jwt.claim.roles", String.class))))
                        .readArray()
                        .getValuesAs(JsonString.class)
                        .stream()
                        .map(JsonString::getString)
                        .toArray(String[]::new);

                Principal userPrincipal = new CallerPrincipal(claims.get("sub"));
                boolean isSecure = requestContext.getSecurityContext().isSecure();

                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return userPrincipal;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return Arrays.asList(roles).contains(role);
                    }

                    @Override
                    public boolean isSecure() {
                        return isSecure;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return AUTHENTICATION_SCHEME;
                    }
                });
            }
        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME + " realm=\"" + config.getValue("mp.jwt.realm", String.class) + "\"")
                        .build());
    }
}