package me.soilmonitoring.iam.security;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJBException;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

@Startup
@Singleton
@LocalBean
public class JwtManager {

    private static final String CURVE = "Ed25519";
    private static final KeyPairGenerator keyPairGenerator;
    private static final Signature signatureAlgorithm;

    static {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(CURVE);
            signatureAlgorithm = Signature.getInstance(CURVE);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException(e);
        }
    }

    private final Map<String, KeyPair> cachedKeyPairs = new HashMap<>();
    private final Map<String, Long> keyPairExpires = new HashMap<>();
    private final long keyPairLifeTime = 10800; // seconds
    private final long jwtLifeTime = 1020; // seconds
    private final long maxCacheSize = 3;
    private final Set<String> audiences = Set.of(
            "urn:cot-app-sec:www",
            "urn:cot-app-sec:admin",
            "urn:cot-app-sec:api"
    );
    private final String issuer = "urn:cot-app-sec:iam";

    @PostConstruct
    public void init() {
        while (cachedKeyPairs.entrySet().stream().filter(this::privateKeyHasNotExpired).count() < maxCacheSize) {
            generateKeyPair();
        }
    }

    private void generateKeyPair() {
        String kid = UUID.randomUUID().toString();
        keyPairExpires.put(kid, Instant.now().getEpochSecond() + keyPairLifeTime);
        cachedKeyPairs.put(kid, keyPairGenerator.generateKeyPair());
    }

    Optional<Map.Entry<String, KeyPair>> getKeyPair() {
        cachedKeyPairs.entrySet().removeIf(e -> isPublicKeyExpired(e.getKey()));
        while (cachedKeyPairs.entrySet().stream().filter(this::privateKeyHasNotExpired).count() < maxCacheSize) {
            generateKeyPair();
        }
        return cachedKeyPairs.entrySet().stream().filter(this::privateKeyHasNotExpired).findAny();
    }

    private boolean isPublicKeyExpired(String kid) {
        return Instant.now().getEpochSecond() > (keyPairExpires.get(kid) + jwtLifeTime);
    }

    private boolean privateKeyHasNotExpired(Map.Entry<String, KeyPair> entry) {
        String kid = entry.getKey();
        return Instant.now().getEpochSecond() <= keyPairExpires.get(kid);
    }

    public String generateToken(String tenantId, String subject, String approvedScopes, String[] roles) {
        try {
            Map.Entry<String, KeyPair> keyPairEntry = getKeyPair().orElseThrow();
            PrivateKey privateKey = keyPairEntry.getValue().getPrivate();
            signatureAlgorithm.initSign(privateKey);

            JsonObject header = Json.createObjectBuilder()
                    .add("typ", "JWT")
                    .add("alg", privateKey.getAlgorithm())
                    .add("kid", keyPairEntry.getKey())
                    .build();

            var now = Instant.now();
            var rolesArray = Json.createArrayBuilder();
            for (String role : roles) rolesArray.add(role);

            var audiencesArray = Json.createArrayBuilder();
            for (String audience : audiences) audiencesArray.add(audience);

            JsonObject payload = Json.createObjectBuilder()
                    .add("iss", issuer)
                    .add("aud", audiencesArray)
                    .add("tenant-id", tenantId)
                    .add("sub", subject)
                    .add("upn", subject)
                    .add("scope", approvedScopes)
                    .add("groups", rolesArray)
                    .add("exp", now.getEpochSecond() + jwtLifeTime)
                    .add("iat", now.getEpochSecond())
                    .add("nbf", now.getEpochSecond())
                    .add("jti", UUID.randomUUID().toString())
                    .build();

            String toSign = Base64.getUrlEncoder().withoutPadding().encodeToString(header.toString().getBytes(StandardCharsets.UTF_8))
                    + "."
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));

            signatureAlgorithm.update(toSign.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureAlgorithm.sign());

            return toSign + "." + signature;

        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    public Map<String, String> verifyToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return Collections.emptyMap();

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            JsonObject header = Json.createReader(new StringReader(headerJson)).readObject();
            String kid = header.getString("kid", null);
            if (kid == null) return Collections.emptyMap();

            KeyPair keyPair = cachedKeyPairs.get(kid);
            if (keyPair == null) return Collections.emptyMap();

            signatureAlgorithm.initVerify(keyPair.getPublic());
            signatureAlgorithm.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
            if (!signatureAlgorithm.verify(Base64.getUrlDecoder().decode(parts[2]))) {
                return Collections.emptyMap();
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonObject payload = Json.createReader(new StringReader(payloadJson)).readObject();

            var exp = payload.getJsonNumber("exp");
            if (exp == null || Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now())) {
                return Collections.emptyMap();
            }

            return Map.of(
                    "tenant-id", payload.getString("tenant-id", ""),
                    "sub", payload.getString("sub", ""),
                    "upn", payload.getString("upn", ""),
                    "scope", payload.getString("scope", ""),
                    "groups", payload.getJsonArray("groups").toString()
            );

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public JsonObject getPublicKeyAsJWK(String kid) {
        KeyPair keyPair = cachedKeyPairs.get(kid);
        if (keyPair == null) throw new EJBException("Invalid kid");

        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded());
        return Json.createObjectBuilder()
                .add("kty", "EC")
                .add("crv", CURVE)
                .add("kid", kid)
                .add("x", encoded.substring(Math.min(16, encoded.length())))
                .build();
    }
}
