package me.soilmonitoring.api.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;

import java.io.StringReader;
import java.security.PublicKey;

@ApplicationScoped
public class JwtValidator {

    private PublicKey cachedKey;
    private String cachedKid;

    public PublicKey getKey(String kid) throws Exception {

        if (cachedKey != null && cachedKid.equals(kid)) {
            return cachedKey;
        }

        String jwkStr = ClientBuilder.newClient()
                .target("http://localhost:8081/jwk")  // URL de ton IAM
                .queryParam("kid", kid)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);

        JsonObject jwk = Json.createReader(new StringReader(jwkStr)).readObject();

        PublicKey key = JwtUtils.jwkToPublicKey(jwk);

        cachedKid = kid;
        cachedKey = key;
        return key;
    }
}
