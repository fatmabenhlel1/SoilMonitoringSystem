package me.soilmonitoring.iam.security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public record AuthorizationCode(String tenantName, String identityUsername,
                                String approvedScopes, Long expirationDate,
                                String redirectUri) {

    private static final SecretKey key;
    private static final String codePrefix = "urn:phoenix:code";
    private static final String SEPARATOR = ".";

    static {
        try {
            key = KeyGenerator.getInstance("CHACHA20").generateKey();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate authorization code with PKCE
     * @param codeChallenge The SHA-256 hash of the code_verifier (base64url encoded)
     */
    public String getCode(String codeChallenge) throws Exception {
        String codeId = UUID.randomUUID().toString();

        JsonObject payloadJson = Json.createObjectBuilder()
                .add("tenantName", tenantName)
                .add("identityUsername", identityUsername)
                .add("approvedScopes", approvedScopes)
                .add("expirationDate", expirationDate)
                .add("redirectUri", redirectUri)
                .build();

        String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.toString().getBytes(StandardCharsets.UTF_8));

        // Encrypt the code_challenge (NOT the code_verifier)
        byte[] encryptedChallenge = ChaCha20Poly1305.encrypt(
                codeChallenge.getBytes(StandardCharsets.UTF_8), key
        );
        String encryptedChallengeB64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(encryptedChallenge);

        return codePrefix + SEPARATOR + codeId + SEPARATOR + payloadB64 + SEPARATOR + encryptedChallengeB64;
    }

    /**
     * Decode and validate authorization code with PKCE
     * @param authorizationCode The authorization code
     * @param codeVerifier The original code_verifier (unhashed)
     */
    public static AuthorizationCode decode(String authorizationCode, String codeVerifier) throws Exception {
        String[] parts = authorizationCode.split("\\Q" + SEPARATOR + "\\E");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid encoded code format");
        }

        // Decrypt the stored code_challenge
        String encryptedChallengeB64 = parts[3];
        byte[] decryptedChallenge = ChaCha20Poly1305.decrypt(
                Base64.getUrlDecoder().decode(encryptedChallengeB64), key);
        String storedCodeChallenge = new String(decryptedChallenge, StandardCharsets.UTF_8);

        // Hash the provided code_verifier using SHA-256
        String computedCodeChallenge = computeCodeChallenge(codeVerifier);

        // Compare the computed challenge with the stored challenge
        if (!storedCodeChallenge.equals(computedCodeChallenge)) {
            throw new IllegalArgumentException("Code verifier does not match the code challenge");
        }

        // Decode the payload
        String payloadB64 = parts[2];
        String payloadJsonStr = new String(Base64.getUrlDecoder().decode(payloadB64), StandardCharsets.UTF_8);
        JsonObject payload = Json.createReader(new java.io.StringReader(payloadJsonStr)).readObject();

        return new AuthorizationCode(
                payload.getString("tenantName"),
                payload.getString("identityUsername"),
                payload.getString("approvedScopes"),
                payload.getJsonNumber("expirationDate").longValue(),
                payload.getString("redirectUri")
        );
    }

    /**
     * Compute SHA-256 code challenge from code verifier (S256 method)
     */
    private static String computeCodeChallenge(String codeVerifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static class ChaCha20Poly1305 {
        private static final String ENCRYPT_ALGO = "ChaCha20-Poly1305";
        private static final int NONCE_LEN = 12;

        public static byte[] encrypt(byte[] pText, SecretKey key) throws Exception {
            byte[] nonce = getNonce();
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            IvParameterSpec iv = new IvParameterSpec(nonce);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedText = cipher.doFinal(pText);

            ByteBuffer bb = ByteBuffer.allocate(encryptedText.length + NONCE_LEN)
                    .put(encryptedText)
                    .put(nonce);
            return bb.array();
        }

        public static byte[] decrypt(byte[] cText, SecretKey key) throws Exception {
            ByteBuffer bb = ByteBuffer.wrap(cText);
            byte[] encryptedText = new byte[cText.length - NONCE_LEN];
            byte[] nonce = new byte[NONCE_LEN];
            bb.get(encryptedText);
            bb.get(nonce);

            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            IvParameterSpec iv = new IvParameterSpec(nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return cipher.doFinal(encryptedText);
        }

        private static byte[] getNonce() {
            byte[] nonce = new byte[NONCE_LEN];
            new SecureRandom().nextBytes(nonce);
            return nonce;
        }
    }
}