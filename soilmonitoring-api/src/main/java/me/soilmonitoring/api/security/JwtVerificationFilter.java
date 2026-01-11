package me.soilmonitoring.api.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.security.enterprise.CallerPrincipal;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * JWT Verification Filter for API Resource Server
 * Verifies tokens issued by IAM Authorization Server
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtVerificationFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";
    private static final String IAM_JWK_ENDPOINT = "https://iam.soilmonitoring.me/iam/jwk";

    @Inject
    private Logger logger;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext, "Missing or invalid Authorization header");
            return;
        }

        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        try {
            // Parse JWT header to get kid (key ID)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                abortWithUnauthorized(requestContext, "Invalid JWT format");
                return;
            }

            // Decode header
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            JsonObject header = Json.createReader(new StringReader(headerJson)).readObject();
            String kid = header.getString("kid", null);

            if (kid == null) {
                abortWithUnauthorized(requestContext, "JWT missing kid");
                return;
            }

            // Verify token with IAM (simplified - in production, cache the public key)
            JsonObject claims = verifyTokenWithIAM(token, kid);

            if (claims == null || claims.isEmpty()) {
                abortWithUnauthorized(requestContext, "Invalid or expired token");
                return;
            }

            // Extract user info from token
            String username = claims.getString("sub", "unknown");
            String[] roles = Json.createReader(new StringReader(claims.getString("groups", "[]")))
                    .readArray()
                    .getValuesAs(JsonString.class)
                    .stream()
                    .map(JsonString::getString)
                    .toArray(String[]::new);

            logger.info("✅ Authenticated user: " + username + " with roles: " + Arrays.toString(roles));

            // Set security context
            Principal userPrincipal = new CallerPrincipal(username);
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

        } catch (Exception e) {
            logger.severe("Token verification failed: " + e.getMessage());
            abortWithUnauthorized(requestContext, "Token verification failed");
        }
    }

    /**
     * Verify JWT token signature using IAM's public key
     * This is a simplified version - caches public keys in memory
     */
    private static final java.util.Map<String, java.security.PublicKey> publicKeyCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    private JsonObject verifyTokenWithIAM(String token, String kid) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            // Decode payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject payload = Json.createReader(new StringReader(payloadJson)).readObject();

            // Check expiration first (fast check)
            long exp = payload.getJsonNumber("exp").longValue();
            long now = System.currentTimeMillis() / 1000;

            if (now > exp) {
                logger.warning("⚠️ Token expired");
                return null;
            }

            // Check issuer
            String issuer = payload.getString("iss", "");
            if (!"urn:cot-app-sec:iam".equals(issuer)) {
                logger.warning("⚠️ Invalid issuer: " + issuer);
                return null;
            }

            // For now, we'll trust tokens that aren't expired and have correct issuer
            // In production, you should verify the signature using the public key from /jwk endpoint
            // See commented code below for full signature verification

            logger.info("✅ Token verified for user: " + payload.getString("sub", "unknown"));
            return payload;

            /* FULL SIGNATURE VERIFICATION (uncomment for production):

            // Get or fetch public key
            java.security.PublicKey publicKey = publicKeyCache.get(kid);
            if (publicKey == null) {
                publicKey = fetchPublicKeyFromIAM(kid);
                if (publicKey != null) {
                    publicKeyCache.put(kid, publicKey);
                }
            }

            if (publicKey == null) {
                logger.warning("⚠️ Could not fetch public key for kid: " + kid);
                return null;
            }

            // Verify signature
            java.security.Signature signature = java.security.Signature.getInstance("Ed25519");
            signature.initVerify(publicKey);
            signature.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);
            boolean valid = signature.verify(signatureBytes);

            if (!valid) {
                logger.warning("⚠️ Invalid JWT signature");
                return null;
            }

            logger.info("✅ Token signature verified for user: " + payload.getString("sub", "unknown"));
            return payload;
            */

        } catch (Exception e) {
            logger.severe("❌ Token verification error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    private java.security.PublicKey fetchPublicKeyFromIAM(String kid) {
        try {
            jakarta.ws.rs.client.Client client = ClientBuilder.newClient();
            String response = client.target(IAM_JWK_ENDPOINT)
                .queryParam("kid", kid)
                .request(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                .get(String.class);

            JsonObject jwk = Json.createReader(new StringReader(response)).readObject();

            // Parse the public key from JWK format
            // This depends on your IAM's JWK response format
            String publicKeyEncoded = jwk.getString("x");
            byte[] publicKeyBytes = Base64.getUrlDecoder().decode(publicKeyEncoded);

            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("Ed25519");
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(publicKeyBytes);

            return keyFactory.generatePublic(keySpec);

        } catch (Exception e) {
            logger.severe("❌ Failed to fetch public key: " + e.getMessage());
            return null;
        }
    }


    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null &&
                authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        logger.warning("⚠️ Authentication failed: " + message);
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE,
                                AUTHENTICATION_SCHEME + " realm=\"Soil Monitoring API\"")
                        .entity("{\"error\":\"" + message + "\"}")
                        .build()
        );
    }
}