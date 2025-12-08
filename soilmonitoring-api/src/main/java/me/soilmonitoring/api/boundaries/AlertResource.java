package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.entities.Alert;
import me.soilmonitoring.api.security.Secured;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/alerts")

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertResource {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

    @Inject
    private AlertRepository alertRepository;

    @GET
    @Path("/user/{userId}")
    public Response getUserAlerts(@PathParam("userId") String userId,
                                  @QueryParam("unread") Boolean unreadOnly) {
        try {
            List<Alert> alerts;
            if (unreadOnly != null && unreadOnly) {
                alerts = manager.getUnreadAlerts(userId);
            } else {
                alerts = alertRepository.findByUserId(userId);
            }
            return Response.ok(alerts).build();
        } catch (Exception e) {
            logger.severe("Error getting user alerts: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving alerts").build();
        }
    }

    @GET
    @Path("/{alertId}")
    public Response getAlertById(@PathParam("alertId") String alertId) {
        try {
            Alert alert = alertRepository.findById(alertId)
                    .orElseThrow(IllegalArgumentException::new);
            return Response.ok(alert).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Alert not found").build();
        } catch (Exception e) {
            logger.severe("Error getting alert: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving alert").build();
        }
    }

    @POST
    public Response createAlert(Alert alert) {
        try {
            alert.setId(UUID.randomUUID().toString());
            alert.setCreatedAt(LocalDateTime.now());
            alert.setIsRead(false);
            Alert savedAlert = alertRepository.save(alert);
            logger.info("Alert created: " + savedAlert.getId());
            return Response.status(Response.Status.CREATED).entity(savedAlert).build();
        } catch (Exception e) {
            logger.severe("Error creating alert: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating alert").build();
        }
    }

    @PUT
    @Path("/{alertId}/read")
    public Response markAlertAsRead(@PathParam("alertId") String alertId) {
        try {
            Alert alert = alertRepository.findById(alertId)
                    .orElseThrow(IllegalArgumentException::new);
            alert.setIsRead(true);
            alertRepository.save(alert);
            logger.info("Alert marked as read: " + alertId);
            return Response.ok(alert).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Alert not found").build();
        } catch (Exception e) {
            logger.severe("Error marking alert as read: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating alert").build();
        }
    }

    @DELETE
    @Path("/{alertId}")
    public Response deleteAlert(@PathParam("alertId") String alertId) {
        try {
            alertRepository.deleteById(alertId);
            logger.info("Alert deleted: " + alertId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.severe("Error deleting alert: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting alert").build();
        }
    }
}
