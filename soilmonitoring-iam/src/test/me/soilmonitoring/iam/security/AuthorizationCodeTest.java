package me.soilmonitoring.iam.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthorizationCodeTest {

    @Test
    void getCode_generatesNonNullCode() throws Exception {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        String codeChallenge = "testChallenge";

        String generatedCode = code.getCode(codeChallenge);

        assertNotNull(generatedCode);
        assertTrue(generatedCode.startsWith("urn:phoenix:code."));
        assertEquals(4, generatedCode.split("\\.").length, "Code should contain 4 parts");
    }

    @Test
    void decode_shouldReturnOriginalAuthorizationCode() throws Exception {
        AuthorizationCode originalCode = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        String codeVerifier = "testVerifier";

        String generatedCode = originalCode.getCode(codeVerifier);
        AuthorizationCode decodedCode = AuthorizationCode.decode(generatedCode, codeVerifier);

        assertEquals(originalCode.tenantName(), decodedCode.tenantName());
        assertEquals(originalCode.identityUsername(), decodedCode.identityUsername());
        assertEquals(originalCode.approvedScopes(), decodedCode.approvedScopes());
        assertEquals(originalCode.expirationDate(), decodedCode.expirationDate());
        assertEquals(originalCode.redirectUri(), decodedCode.redirectUri());
    }

    @Test
    void decode_withWrongCodeVerifier_shouldThrow() throws Exception {
        AuthorizationCode code = new AuthorizationCode("tenant1", "user1", "read write", 1699999999L, "http://localhost");
        String correctVerifier = "correctVerifier";
        String wrongVerifier = "wrongVerifier";

        String generatedCode = code.getCode(correctVerifier);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                AuthorizationCode.decode(generatedCode, wrongVerifier));
        assertEquals("Code verifier does not match the code challenge", ex.getMessage());
    }
}
