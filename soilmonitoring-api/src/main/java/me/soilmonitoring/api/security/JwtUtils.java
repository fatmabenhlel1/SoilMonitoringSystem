package me.soilmonitoring.api.security;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtUtils {

    public static String extractKid(String token) {
        String header = token.split("\\.")[0];
        String json = new String(Base64.getUrlDecoder().decode(header));
        JsonObject obj = Json.createReader(new StringReader(json)).readObject();
        return obj.getString("kid", null);
    }

    public static JsonObject decodePayload(String token) {
        String payload = token.split("\\.")[1];
        String json = new String(Base64.getUrlDecoder().decode(payload));
        return Json.createReader(new StringReader(json)).readObject();
    }

    public static boolean verifySignature(String token, PublicKey key) {
        try {
            String[] parts = token.split("\\.");
            String headerAndPayload = parts[0] + "." + parts[1];
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(key);
            s.update(headerAndPayload.getBytes());
            return s.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public static PublicKey jwkToPublicKey(JsonObject jwk) throws Exception {
        String n = jwk.getString("n");
        String e = jwk.getString("e");

        byte[] modulusBytes   = Base64.getUrlDecoder().decode(n);
        byte[] exponentBytes  = Base64.getUrlDecoder().decode(e);

        String pem = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJB" +
                Base64.getEncoder().encodeToString(modulusBytes) +
                Base64.getEncoder().encodeToString(exponentBytes);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.getDecoder().decode(pem));
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
