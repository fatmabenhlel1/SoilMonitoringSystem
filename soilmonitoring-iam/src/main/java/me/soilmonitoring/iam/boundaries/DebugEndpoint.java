package me.soilmonitoring.iam.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.iam.security.AuthorizationCode;

import java.util.logging.Logger;

/**
 * Debug endpoint to help diagnose PKCE issues
 * REMOVE THIS IN PRODUCTION!
 */
@Path("/debug")
@RequestScoped
public class DebugEndpoint {

    private static final Logger logger = Logger.getLogger(DebugEndpoint.class.getName());

    @POST
    @Path("/decode-code")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response debugDecodeCode(@FormParam("code") String authCode,
                                    @FormParam("code_verifier") String codeVerifier) {
        try {
            logger.info("=== DEBUG: Attempting to decode authorization code ===");
            logger.info("Code (first 50 chars): " + authCode.substring(0, Math.min(50, authCode.length())));
            logger.info("Code verifier (first 20 chars): " + codeVerifier.substring(0, Math.min(20, codeVerifier.length())));

            AuthorizationCode decoded = AuthorizationCode.decode(authCode, codeVerifier);

            String result = String.format(
                    "{\"success\":true,\"tenant\":\"%s\",\"user\":\"%s\",\"scopes\":\"%s\",\"redirectUri\":\"%s\"}",
                    decoded.tenantName(),
                    decoded.identityUsername(),
                    decoded.approvedScopes(),
                    decoded.redirectUri()
            );

            logger.info("✅ Successfully decoded!");
            return Response.ok(result).build();

        } catch (Exception e) {
            logger.severe("❌ Decode failed: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();

            String error = String.format(
                    "{\"success\":false,\"error\":\"%s\",\"message\":\"%s\"}",
                    e.getClass().getSimpleName(),
                    e.getMessage().replace("\"", "'")
            );

            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }
}