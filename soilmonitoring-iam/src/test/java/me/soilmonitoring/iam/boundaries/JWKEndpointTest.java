package me.soilmonitoring.iam.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.iam.security.JwtManager;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JWKEndpointTest {

    @Mock
    private JwtManager jwtManager;

    @InjectMocks
    private JWKEndpoint jwkEndpoint;

    @Test
    void getPublicVerificationKey_shouldReturnPublicKeyWhenValidKid() throws Exception {
        String kid = "validKid";
        String publicKey = "{\"kty\":\"RSA\",\"kid\":\"validKid\"}";

        when(jwtManager.getPublicKeyAsJWK(kid)).thenReturn(publicKey);

        Response response = jwkEndpoint.getPublicVerificationKey(kid);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(publicKey, response.getEntity());
        verify(jwtManager, times(1)).getPublicKeyAsJWK(kid);
    }

    @Test
    void getPublicVerificationKey_shouldReturnBadRequestWhenExceptionThrown() throws Exception {
        String kid = "invalidKid";
        String errorMessage = "Key not found";

        when(jwtManager.getPublicKeyAsJWK(kid)).thenThrow(new Exception(errorMessage));

        Response response = jwkEndpoint.getPublicVerificationKey(kid);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(errorMessage, response.getEntity());
        verify(jwtManager, times(1)).getPublicKeyAsJWK(kid);
    }
}