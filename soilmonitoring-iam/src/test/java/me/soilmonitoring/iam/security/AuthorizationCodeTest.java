package me.soilmonitoring.iam.security;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationCodeTest {

    @Test
    void getCode() throws Exception {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        String codeChallenge = Base64.getEncoder().encodeToString("testChallenge".getBytes());

        String generatedCode = code.getCode(codeChallenge);

        assertNotNull(generatedCode, "Generated code should not be null");
        assertTrue(generatedCode.startsWith("urn:phoenix:code:"), "Code should have the correct prefix");
    }

    @Test
    void decode() throws Exception {
        AuthorizationCode originalCode = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        String codeVerifier = "testVerifier";
        String codeChallenge = Base64.getEncoder().encodeToString(codeVerifier.getBytes());
        String generatedCode = originalCode.getCode(codeChallenge);

        AuthorizationCode decodedCode = AuthorizationCode.decode(generatedCode, codeVerifier);

        assertNotNull(decodedCode, "Decoded code should not be null");
        assertEquals(originalCode.tenantName(), decodedCode.tenantName(), "Tenant name should match");
        assertEquals(originalCode.identityUsername(), decodedCode.identityUsername(), "Identity username should match");
        assertEquals(originalCode.approvedScopes(), decodedCode.approvedScopes(), "Approved scopes should match");
        assertEquals(originalCode.expirationDate(), decodedCode.expirationDate(), "Expiration date should match");
        assertEquals(originalCode.redirectUri(), decodedCode.redirectUri(), "Redirect URI should match");
    }

    @Test
    void tenantName() {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        assertEquals("tenant1", code.tenantName(), "Tenant name should be retrieved correctly");
    }

    @Test
    void identityUsername() {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        assertEquals("user1", code.identityUsername(), "Identity username should be retrieved correctly");
    }

    @Test
    void approvedScopes() {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        assertEquals("read write", code.approvedScopes(), "Approved scopes should be retrieved correctly");
    }

    @Test
    void expirationDate() {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        assertEquals(1699999999L, code.expirationDate(), "Expiration date should be retrieved correctly");
    }

    @Test
    void redirectUri() {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        assertEquals("http://localhost", code.redirectUri(), "Redirect URI should be retrieved correctly");
    }
}