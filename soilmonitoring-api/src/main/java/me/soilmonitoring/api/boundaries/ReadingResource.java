package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.SensorReadingRepository;
import me.soilmonitoring.api.controllers.repositories.SensorReadingRepository;
import me.soilmonitoring.api.entities.SensorReading;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/readings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingResource {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

    @Inject
    private SensorReadingRepository readingRepository;

    @GET
    @Path("/field/{fieldId}")
    public Response getFieldReadings(@PathParam("fieldId") String fieldId) {
        try {
            List<SensorReading> readings = manager.getFieldReadings(fieldId);
            return Response.ok(readings).build();
        } catch (Exception e) {
            logger.severe("Error getting field readings: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving readings").build();
        }
    }

    @GET
    @Path("/field/{fieldId}/range")
    public Response getFieldReadingsByTimeRange(
            @PathParam("fieldId") String fieldId,
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        try {
            LocalDateTime fromDate = LocalDateTime.parse(from);
            LocalDateTime toDate = LocalDateTime.parse(to);
            List<SensorReading> readings = manager.getFieldReadingsByTimeRange(fieldId, fromDate, toDate);
            return Response.ok(readings).build();
        } catch (Exception e) {
            logger.severe("Error getting field readings by time range: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving readings").build();
        }
    }

    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        try {
            SensorReading reading = readingRepository.findById(readingId)
                    .orElseThrow(IllegalArgumentException::new);
            return Response.ok(reading).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Reading not found").build();
        } catch (Exception e) {
            logger.severe("Error getting reading: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving reading").build();
        }
    }

    @POST
    public Response createReading(SensorReading reading) {
        try {
            reading.setId(UUID.randomUUID().toString());
            reading.setTimestamp(LocalDateTime.now());
            SensorReading savedReading = readingRepository.save(reading);
            logger.info("Sensor reading created: " + savedReading.getId());
            return Response.status(Response.Status.CREATED).entity(savedReading).build();
        } catch (Exception e) {
            logger.severe("Error creating reading: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating reading").build();
        }
    }

    @GET
    @Path("/field/{fieldId}/latest")
    public Response getLatestReading(@PathParam("fieldId") String fieldId) {
        try {
            List<SensorReading> readings = manager.getFieldReadings(fieldId);
            if (readings.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No readings found").build();
            }
            SensorReading latest = readings.get(readings.size() - 1);
            return Response.ok(latest).build();
        } catch (Exception e) {
            logger.severe("Error getting latest reading: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving latest reading").build();
        }
    }
}