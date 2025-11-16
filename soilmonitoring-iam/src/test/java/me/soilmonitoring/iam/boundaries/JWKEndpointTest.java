package me.soilmonitoring.iam.boundaries;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.iam.security.JwtManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
class JWKEndpointTest {

    @Inject
    private JWKEndpoint jwkEndpoint;

    @Inject
    private JwtManager jwtManager;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(JWKEndpoint.class, JwtManager.class)
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");
    }

    @Test
    void getPublicVerificationKey_shouldReturnPublicKeyWhenValidKid() throws Exception {
        String kid = "validKid";

        // Assuming JwtManager is properly configured to return a public key for the given kid
        JsonObject publicKey = jwtManager.getPublicKeyAsJWK(kid);

        Response response = jwkEndpoint.getPublicVerificationKey(kid);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(publicKey, response.getEntity());
    }

    @Test
    void getPublicVerificationKey_shouldReturnBadRequestWhenExceptionThrown() throws Exception {
        String kid = "invalidKid";

        Response response = jwkEndpoint.getPublicVerificationKey(kid);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Key not found"));
    }
}