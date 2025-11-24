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
        jwtManager.init();
    }

    @Test
    void generateToken_ShouldReturnValidJWT() {
        String token = jwtManager.generateToken("tenant1", "user1", "read write", new String[]{"admin", "user"});
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void verifyToken_ShouldReturnClaimsForValidToken() {
        String token = jwtManager.generateToken("tenant1", "user1", "read write", new String[]{"admin", "user"});
        Map<String, String> claims = jwtManager.verifyToken(token);

        assertEquals("tenant1", claims.get("tenant-id"));
        assertEquals("user1", claims.get("sub"));
        assertTrue(claims.get("groups").contains("admin"));
        assertTrue(claims.get("groups").contains("user"));
    }

    @Test
    void verifyToken_ShouldReturnEmptyMapForInvalidToken() {
        String invalidToken = "invalid.token.signature";
        Map<String, String> claims = jwtManager.verifyToken(invalidToken);
        assertTrue(claims.isEmpty());
    }

    @Test
    void getPublicKeyAsJWK_ShouldReturnValidJWK() {
        String kid = jwtManager.getKeyPair().orElseThrow().getKey();
        JsonObject jwk = jwtManager.getPublicKeyAsJWK(kid);

        assertNotNull(jwk);
        assertEquals("EC", jwk.getString("kty"));
        assertEquals("Ed25519", jwk.getString("crv"));
        assertEquals(kid, jwk.getString("kid"));
    }
}
