package me.soilmonitoring.iam.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import me.soilmonitoring.iam.services.IdentityService;

import java.util.Map;
import java.util.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private IdentityService identityService;

    @POST
    @Path("/register")
    public Response register(Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");

            if (username == null || password == null || email == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Missing required fields\"}")
                        .build();
            }

            identityService.registerIdentity(username, password, email);

            logger.info("✅ User registered: " + username);
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\":\"User registered successfully. Check email for activation code.\"}")
                    .build();

        } catch (Exception e) {
            logger.severe("Registration error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/activate")
    public Response activate(Map<String, String> request) {
        try {
            String code = request.get("code");

            if (code == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Activation code required\"}")
                        .build();
            }

            identityService.activateIdentity(code);

            return Response.ok("{\"message\":\"Account activated successfully\"}")
                    .build();

        } catch (Exception e) {
            logger.severe("Activation error: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}