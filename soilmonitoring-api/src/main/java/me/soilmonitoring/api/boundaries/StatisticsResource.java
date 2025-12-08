package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.entities.SensorReading;
import me.soilmonitoring.api.security.Secured;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Path("/statistics")

@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class StatisticsResource {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

    /**
     * Get hourly averages for the last 24 hours
     */
    @GET
    @Path("/field/{fieldId}/hourly")
    public Response getHourlyStatistics(@PathParam("fieldId") String fieldId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusHours(24);

            List<SensorReading> readings = manager.getFieldReadingsByTimeRange(
                    fieldId, yesterday, now
            );

            if (readings.isEmpty()) {
                return Response.ok("[]").build();
            }

            // Group by hour and calculate averages
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (int i = 0; i < 24; i++) {
                LocalDateTime hourStart = yesterday.plusHours(i);
                LocalDateTime hourEnd = hourStart.plusHours(1);

                List<SensorReading> hourReadings = readings.stream()
                        .filter(r -> r.getTimestamp().isAfter(hourStart) &&
                                r.getTimestamp().isBefore(hourEnd))
                        .toList();

                if (!hourReadings.isEmpty()) {
                    double avgTemp = hourReadings.stream()
                            .mapToDouble(r -> r.getData().getTemperature() != null ?
                                    r.getData().getTemperature() : 0)
                            .average().orElse(0);

                    double avgHumidity = hourReadings.stream()
                            .mapToDouble(r -> r.getData().getHumidity() != null ?
                                    r.getData().getHumidity() : 0)
                            .average().orElse(0);

                    double avgMoisture = hourReadings.stream()
                            .mapToDouble(r -> r.getData().getSoilMoisture() != null ?
                                    r.getData().getSoilMoisture() : 0)
                            .average().orElse(0);

                    arrayBuilder.add(Json.createObjectBuilder()
                            .add("timestamp", hourStart.toString())
                            .add("temperature", avgTemp)
                            .add("humidity", avgHumidity)
                            .add("soilMoisture", avgMoisture)
                            .add("count", hourReadings.size())
                    );
                }
            }

            return Response.ok(arrayBuilder.build().toString()).build();

        } catch (Exception e) {
            logger.severe("Error getting hourly statistics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get daily summary for dashboard
     */
    @GET
    @Path("/field/{fieldId}/today")
    public Response getTodaySummary(@PathParam("fieldId") String fieldId) {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime now = LocalDateTime.now();

            List<SensorReading> readings = manager.getFieldReadingsByTimeRange(
                    fieldId, startOfDay, now
            );

            if (readings.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\":\"No readings for today\"}").build();
            }

            // Calculate min, max, avg
            double minTemp = readings.stream()
                    .mapToDouble(r -> r.getData().getTemperature() != null ?
                            r.getData().getTemperature() : Double.MAX_VALUE)
                    .min().orElse(0);

            double maxTemp = readings.stream()
                    .mapToDouble(r -> r.getData().getTemperature() != null ?
                            r.getData().getTemperature() : Double.MIN_VALUE)
                    .max().orElse(0);

            double avgTemp = readings.stream()
                    .mapToDouble(r -> r.getData().getTemperature() != null ?
                            r.getData().getTemperature() : 0)
                    .average().orElse(0);

            JsonObject summary = Json.createObjectBuilder()
                    .add("date", startOfDay.toLocalDate().toString())
                    .add("readingCount", readings.size())
                    .add("temperature", Json.createObjectBuilder()
                            .add("min", minTemp)
                            .add("max", maxTemp)
                            .add("avg", avgTemp)
                    )
                    .build();

            return Response.ok(summary.toString()).build();

        } catch (Exception e) {
            logger.severe("Error getting today's summary: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}