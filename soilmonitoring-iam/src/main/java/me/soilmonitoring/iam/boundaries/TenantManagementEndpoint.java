package me.soilmonitoring.iam.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.iam.controllers.managers.PhoenixIAMManager;
import me.soilmonitoring.iam.entities.Tenant;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/tenants")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TenantManagementEndpoint {

    @Inject
    private Logger logger;

    @Inject
    private PhoenixIAMManager manager;

    /**
     * Register a new OAuth2 client (tenant)
     */
    @POST
    @Path("/register")
    public Response registerTenant(Map<String, String> request) {
        try {
            String name = request.get("name");
            String redirectUri = request.get("redirectUri");
            String scopes = request.get("scopes");

            if (name == null || redirectUri == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Missing required fields: name, redirectUri\"}")
                        .build();
            }

            // Create tenant
            Tenant tenant = new Tenant();
            tenant.setId(UUID.randomUUID().toString());
            tenant.setName(name);
            tenant.setSecret("not-used-with-pkce");
            tenant.setRedirectUri(redirectUri);
            tenant.setAllowedRoles(Long.MAX_VALUE); // All roles
            tenant.setRequiredScopes(scopes != null ? scopes : "openid profile email");
            tenant.setSupportedGrantTypes("authorization_code refresh_token");
            tenant.setVersion(0L);

            manager.saveTenant(tenant);

            logger.info("✅ Tenant registered: " + name);

            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\":\"Tenant registered\",\"client_id\":\"" + name + "\"}")
                    .build();

        } catch (Exception e) {
            logger.severe("Tenant registration error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Get tenant by name
     */
    @GET
    @Path("/{name}")
    public Response getTenant(@PathParam("name") String name) {
        try {
            Tenant tenant = manager.findTenantByName(name);
            return Response.ok(tenant).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Tenant not found\"}")
                    .build();
        }
    }
}