package me.soilmonitoring.api.security;

import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jwt.SignedJWT;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class JwtValidator {

    @Inject
    private Logger logger;

    @Inject
    @ConfigProperty(name = "iam.jwk.url")
    private String jwkUrl;

    @Inject
    @ConfigProperty(name = "iam.issuer")
    private String expectedIssuer;

    private final Map<String, OctetKeyPair> keyCache = new HashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Map<String, Object> validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Extract Key ID from token header
            String kid = signedJWT.getHeader().getKeyID();
            if (kid == null) {
                logger.warning("JWT missing kid header");
                return null;
            }

            // Get public key from IAM service (or cache)
            OctetKeyPair jwk = getPublicKey(kid);

            // Verify signature
            if (!signedJWT.verify(new com.nimbusds.jose.crypto.Ed25519Verifier(jwk))) {
                logger.warning("JWT signature verification failed");
                return null;
            }

            var claims = signedJWT.getJWTClaimsSet();

            // Check expiration
            if (claims.getExpirationTime().before(java.util.Date.from(Instant.now()))) {
                logger.warning("JWT token expired");
                return null;
            }

            // Check issuer
            if (!expectedIssuer.equals(claims.getIssuer())) {
                logger.warning("Invalid issuer: " + claims.getIssuer());
                return null;
            }

            // Build claims map
            Map<String, Object> result = new HashMap<>();
            result.put("sub", claims.getSubject());
            result.put("tenant-id", claims.getStringClaim("tenant-id"));
            result.put("scope", claims.getStringClaim("scope"));
            result.put("groups", claims.getStringListClaim("groups"));

            logger.info("✅ Token validated for user: " + claims.getSubject());
            return result;

        } catch (Exception e) {
            logger.severe("JWT validation error: " + e.getMessage());
            return null;
        }
    }

    private OctetKeyPair getPublicKey(String kid) throws Exception {
        // Check cache first
        if (keyCache.containsKey(kid)) {
            return keyCache.get(kid);
        }

        // Fetch JWK from IAM
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(jwkUrl + "?kid=" + kid))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to fetch JWK: " + response.statusCode());
        }

        // Parse OctetKeyPair
        OctetKeyPair jwk = OctetKeyPair.parse(response.body());

        // Cache it
        keyCache.put(kid, jwk);
        return jwk;
    }
}
