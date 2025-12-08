package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.FieldRepository;
import me.soilmonitoring.api.entities.Field;
import me.soilmonitoring.api.security.Secured;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/fields")
@Secured
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FieldResource {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

    @Inject
    private FieldRepository fieldRepository;

    @GET
    @Path("/user/{userId}")
    public Response getUserFields(@PathParam("userId") String userId) {
        try {
            List<Field> fields = manager.getUserFields(userId);
            return Response.ok(fields).build();
        } catch (Exception e) {
            logger.severe("Error getting user fields: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving fields").build();
        }
    }

    @GET
    @Path("/{fieldId}")
    public Response getFieldById(@PathParam("fieldId") String fieldId) {
        try {
            Field field = manager.findFieldById(fieldId);
            return Response.ok(field).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Field not found").build();
        } catch (Exception e) {
            logger.severe("Error getting field: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving field").build();
        }
    }

    @POST
    public Response createField(Field field, @Context SecurityContext securityContext) {
        try {
            field.setId(UUID.randomUUID().toString());

            // ✅ ADD THIS: Get username from JWT token
            String username = securityContext.getUserPrincipal().getName();
            field.setUserId(username);  // Set username as userId

            field.setCreatedAt(LocalDateTime.now());
            Field savedField = fieldRepository.save(field);
            logger.info("Field created: " + savedField.getId() + " for user: " + username);
            return Response.status(Response.Status.CREATED).entity(savedField).build();
        } catch (Exception e) {
            logger.severe("Error creating field: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating field").build();
        }
    }

    @PUT
    @Path("/{fieldId}")
    public Response updateField(@PathParam("fieldId") String fieldId, Field field) {
        try {
            Field existingField = manager.findFieldById(fieldId);
            field.setId(fieldId);
            field.setCreatedAt(existingField.getCreatedAt());
            Field updatedField = fieldRepository.save(field);
            logger.info("Field updated: " + fieldId);
            return Response.ok(updatedField).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Field not found").build();
        } catch (Exception e) {
            logger.severe("Error updating field: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating field").build();
        }
    }

    @DELETE
    @Path("/{fieldId}")
    public Response deleteField(@PathParam("fieldId") String fieldId) {
        try {
            fieldRepository.deleteById(fieldId);
            logger.info("Field deleted: " + fieldId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.severe("Error deleting field: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting field").build();
        }
    }
}