package me.soilmonitoring.iam.security;

import jakarta.annotation.Priority;
import jakarta.ejb.EJBException;
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
import org.eclipse.microprofile.config.ConfigProvider;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.StringReader;
import java.security.Principal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Config config = ConfigProvider.getConfig();
    private static final String REALM = config.getValue("mp.jwt.realm",String.class);

    private static final String CLAIM_ROLES = config.getValue("jwt.claim.roles",String.class);
    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Get the Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader
                .substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            // Validate the token
            InitialContext context = new InitialContext();
            JwtManager manager = (JwtManager) context.lookup("java:module/JwtManager");//the last value in the jndi name must match the name of the EJB class managing your JWT
            var claims = manager.verifyToken(token);
            if(!claims.isEmpty()){
                final var roles = Json.createReader(new StringReader(claims.get(CLAIM_ROLES))).readArray().getValuesAs(JsonString.class).stream().map(JsonString::getString).toList().toArray(new String[0]);
                final Principal userPrincipal = new CallerPrincipal(claims.get("sub"));
                final boolean isSecure = requestContext.getSecurityContext().isSecure();

                IdentityUtility.iAm(claims.get("sub"));

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
        } catch (EJBException | NamingException e) {
            abortWithUnauthorized(requestContext);
        }
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        // Check if the Authorization header is valid
        // It must not be null and must be prefixed with "Bearer" plus a whitespace
        // The authentication scheme comparison must be case-insensitive
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        // Abort the filter chain with a 401 status code response
        // The WWW-Authenticate header is sent along with the response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
                        .build());
    }
}
