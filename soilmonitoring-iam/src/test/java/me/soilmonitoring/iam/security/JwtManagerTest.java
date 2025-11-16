package me.soilmonitoring.iam.security;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtManagerTest {

    private JwtManager jwtManager;

    @BeforeEach
    void setUp() {
        jwtManager = new JwtManager();
        jwtManager.init(); // Initialize the key pairs
    }

    @Test
    void init_generatesKeyPairs() {
        // Verify that the key pairs are generated during initialization
        assertFalse(jwtManager.getPublicKeyAsJWK(jwtManager.getKeyPair().orElseThrow().getKey()).isEmpty(),
                "Key pairs should be generated during initialization");
    }

    @Test
    void generateToken_createsValidToken() {
        String tenantId = "tenant1";
        String subject = "user1";
        String approvedScopes = "read write";
        String[] roles = {"admin", "user"};

        String token = jwtManager.generateToken(tenantId, subject, approvedScopes, roles);

        assertNotNull(token, "Generated token should not be null");
        assertTrue(token.split("\\.").length == 3, "Token should have three parts (header, payload, signature)");
    }

    @Test
    void verifyToken_validTokenReturnsClaims() {
        String tenantId = "tenant1";
        String subject = "user1";
        String approvedScopes = "read write";
        String[] roles = {"admin", "user"};

        String token = jwtManager.generateToken(tenantId, subject, approvedScopes, roles);
        Map<String, String> claims = jwtManager.verifyToken(token);

        assertNotNull(claims, "Claims should not be null");
        assertEquals(tenantId, claims.get("tenant-id"), "Tenant ID should match");
        assertEquals(subject, claims.get("sub"), "Subject should match");
        assertEquals(approvedScopes, claims.get("scope"), "Scopes should match");
        assertTrue(claims.get("groups").contains("admin"), "Roles should include 'admin'");
        assertTrue(claims.get("groups").contains("user"), "Roles should include 'user'");
    }

    @Test
    void verifyToken_invalidTokenReturnsEmptyMap() {
        String invalidToken = "invalid.token.signature";

        Map<String, String> claims = jwtManager.verifyToken(invalidToken);

        assertTrue(claims.isEmpty(), "Claims should be empty for an invalid token");
    }

    @Test
    void getPublicKeyAsJWK_returnsValidJWK() {
        String kid = jwtManager.getKeyPair().orElseThrow().getKey();
        JsonObject jwk = jwtManager.getPublicKeyAsJWK(kid);

        assertNotNull(jwk, "JWK should not be null");
        assertEquals("EC", jwk.getString("kty"), "Key type should be 'EC'");
        assertEquals("Ed25519", jwk.getString("crv"), "Curve should be 'Ed25519'");
        assertEquals(kid, jwk.getString("kid"), "Key ID should match");
    }
}