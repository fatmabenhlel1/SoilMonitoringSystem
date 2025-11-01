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

@Startup /*class charged automatically in wildfly*/
@Singleton /*impose an order of execution*/
@LocalBean /*only locally accessable*/
public class JwtManager {
    private final static String curve = "Ed25519";
    private final static KeyPairGenerator keyPairGenerator;
    private final static Signature signatureAlgorithm;

    static {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(curve);
            signatureAlgorithm = Signature.getInstance(curve);
        } catch (NoSuchAlgorithmException e) {
            throw new EJBException(e);
        }
    }


    private final Map<String, KeyPair> cachedKeyPairs = new HashMap<>();
    private final Map<String, Long> keyPairExpires = new HashMap<>();
    private final long keyPairLifeTime = 10800;
    private final long jwtLifeTime = 1020;
    private final long maxCacheSize = 3;
    private final Set<String> audiences = Set.of("urn:cot-app-sec:www","urn:cot-app-sec:admin","urn:cot-app-sec:api");
    private final String issuer = "urn:cot-app-sec:iam";

    private void generateKeyPair() {
        var kid = UUID.randomUUID().toString();
        keyPairExpires.put(kid, Instant.now().getEpochSecond()+keyPairLifeTime);
        cachedKeyPairs.put(kid, keyPairGenerator.generateKeyPair());
    }

    private Optional<Map.Entry<String, KeyPair>> getKeyPair() {
        cachedKeyPairs.entrySet().removeIf(e -> isPublicKeyExpired(e.getKey()));
        while(cachedKeyPairs.entrySet().stream().filter(e -> privateKeyHasNotExpired(e.getKey())).count() < maxCacheSize) {
            generateKeyPair();
        }
        return cachedKeyPairs.entrySet().stream().filter(e -> privateKeyHasNotExpired(e.getKey())).findAny();
    }

    private boolean isPublicKeyExpired(String kid) {
        return Instant.now().getEpochSecond() > (keyPairExpires.get(kid)+jwtLifeTime);
    }

    private boolean privateKeyHasNotExpired(String kid) {
        return Instant.now().getEpochSecond() <= keyPairExpires.get(kid);
    }

    @PostConstruct
    public void init() {
        while(cachedKeyPairs.entrySet().stream().filter(e -> privateKeyHasNotExpired(e.getKey())).count() < maxCacheSize) {
            generateKeyPair();
        }
    }

    public String generateToken(String tenantId,String subject,String approvedScopes, String[] roles) {
        try {
            var keyPair = getKeyPair().orElseThrow();
            var privateKey = keyPair.getValue().getPrivate();
            signatureAlgorithm.initSign(privateKey);
            var header = Json.createObjectBuilder()
                    .add("typ","JWT")
                    .add("alg",privateKey.getAlgorithm())
                    .add("kid",keyPair.getKey())
                    .build().toString();
            var now = Instant.now();
            var rolesJab = Json.createArrayBuilder();
            for(var role : roles) {
                rolesJab.add(role);
            }
            var audiencesJab = Json.createArrayBuilder();
            for(var audience : audiences) {
                audiencesJab.add(audience);
            }
            var payload = Json.createObjectBuilder()
                    .add("iss",issuer)
                    .add("aud",audiencesJab)
                    .add("tenant-id",tenantId)
                    .add("sub",subject)
                    .add("upn",subject)
                    .add("scope",approvedScopes)
                    .add("groups",rolesJab)
                    .add("exp",now.getEpochSecond()+jwtLifeTime)
                    .add("iat",now.getEpochSecond())
                    .add("nbf",now.getEpochSecond())
                    .add("jti",UUID.randomUUID().toString())
                    .build().toString();
            var toSign = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes())
                    +"."+
                    Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());
            signatureAlgorithm.update(toSign.getBytes(StandardCharsets.UTF_8));
            return toSign+"."+Base64.getUrlEncoder().withoutPadding().encodeToString(signatureAlgorithm.sign());
        } catch (InvalidKeyException | SignatureException | NoSuchElementException e) {
            throw new EJBException(e);
        }
    }

    public Map<String,String> verifyToken(String token) {
        var parts = token.split("\\.");
        var header = Json.createReader(new StringReader(new String(Base64.getUrlDecoder().decode(parts[0])))).readObject();
        var kid = header.getString("kid");
        if(kid == null) {
            throw new EJBException("Invalid token");
        }
        var keyPair = cachedKeyPairs.get(kid);
        if(keyPair == null) {
            return Collections.emptyMap();
        }
        try {
            signatureAlgorithm.initVerify(keyPair.getPublic());
            signatureAlgorithm.update((parts[0]+"."+parts[1]).getBytes(StandardCharsets.UTF_8));
            if(!signatureAlgorithm.verify(Base64.getUrlDecoder().decode(parts[2]))) {
                return Collections.emptyMap();
            }
            var payload = Json.createReader(new StringReader(new String(Base64.getUrlDecoder().decode(parts[1])))).readObject();
            var exp = payload.getJsonNumber("exp");
            if(exp == null) {
                throw new EJBException("Invalid token");
            }
            if(Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now())) {
                return Collections.emptyMap();
            }
            return Map.of("tenant-id",payload.getString("tenant-id"),
                    "sub",payload.getString("sub"),
                    "upn",payload.getString("upn"),
                    "scope",payload.getString("scope"),
                    "groups",payload.getJsonArray("groups").toString());
        } catch (InvalidKeyException | SignatureException e) {
            throw new EJBException(e);
        }
    }

    public JsonObject getPublicKeyAsJWK(String kid) {
        var keyPair = cachedKeyPairs.get(kid);
        if(keyPair == null) {
            throw new EJBException("Invalid kid");
        }
        var encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(keyPair.getPublic().getEncoded());
        return Json.createObjectBuilder()
                .add("kty","EC")
                .add("crv",curve)
                .add("kid",kid)
                .add("x",encoded.substring(16))
                .build();
    }
}
