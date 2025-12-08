package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.SensorRepository;
import me.soilmonitoring.api.entities.Sensor;
import me.soilmonitoring.api.security.Secured;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/sensors")

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

    @Inject
    private SensorRepository sensorRepository;

    @GET
    @Path("/field/{fieldId}")
    public Response getFieldSensors(@PathParam("fieldId") String fieldId) {
        try {
            List<Sensor> sensors = manager.getFieldSensors(fieldId);
            return Response.ok(sensors).build();
        } catch (Exception e) {
            logger.severe("Error getting field sensors: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving sensors").build();
        }
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        try {
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(IllegalArgumentException::new);
            return Response.ok(sensor).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found").build();
        } catch (Exception e) {
            logger.severe("Error getting sensor: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving sensor").build();
        }
    }

    @GET
    @Path("/device/{deviceId}")
    public Response getSensorByDeviceId(@PathParam("deviceId") String deviceId) {
        try {
            Sensor sensor = manager.findSensorByDeviceId(deviceId);
            return Response.ok(sensor).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found").build();
        } catch (Exception e) {
            logger.severe("Error getting sensor by device ID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving sensor").build();
        }
    }

    @POST
    public Response createSensor(Sensor sensor) {
        try {
            sensor.setId(UUID.randomUUID().toString());
            sensor.setInstalledAt(LocalDateTime.now());
            sensor.setStatus("active");
            Sensor savedSensor = sensorRepository.save(sensor);
            logger.info("Sensor created: " + savedSensor.getId());
            return Response.status(Response.Status.CREATED).entity(savedSensor).build();
        } catch (Exception e) {
            logger.severe("Error creating sensor: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating sensor").build();
        }
    }

    @PUT
    @Path("/{sensorId}/status")
    public Response updateSensorStatus(@PathParam("sensorId") String sensorId,
                                       @QueryParam("status") String status) {
        try {
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(IllegalArgumentException::new);
            sensor.setStatus(status);
            sensor.setLastConnection(LocalDateTime.now());
            sensorRepository.save(sensor);
            logger.info("Sensor status updated: " + sensorId);
            return Response.ok(sensor).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found").build();
        } catch (Exception e) {
            logger.severe("Error updating sensor status: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating sensor status").build();
        }
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        try {
            sensorRepository.deleteById(sensorId);
            logger.info("Sensor deleted: " + sensorId);
            return Response.noContent().build();
        } catch (Exception e) {
            logger.severe("Error deleting sensor: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting sensor").build();
        }
    }
}