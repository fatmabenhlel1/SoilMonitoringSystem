package me.soilmonitoring.api.observers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.events.AlertTriggeredEvent;
import me.soilmonitoring.api.events.SensorReadingEvent;
import me.soilmonitoring.api.websocket.SensorDataWebSocket;

import java.util.logging.Logger;

@ApplicationScoped
public class WebSocketObserver {

    @Inject
    private Logger logger;

    /**
     * Broadcast sensor readings to connected WebSocket clients
     */
    public void onSensorReading(@ObservesAsync SensorReadingEvent event) {
        try {
            SensorData data = event.getReading().getData();

            JsonObject message = Json.createObjectBuilder()
                    .add("type", "SENSOR_DATA")
                    .add("payload", Json.createObjectBuilder()
                            .add("id", event.getReading().getId())
                            .add("fieldId", event.getReading().getFieldId())
                            .add("sensorId", event.getReading().getSensorId())
                            .add("temperature", data.getTemperature() != null ? data.getTemperature() : 0.0)
                            .add("humidity", data.getHumidity() != null ? data.getHumidity() : 0.0)
                            .add("nitrogen", data.getNitrogen() != null ? data.getNitrogen() : 0.0)
                            .add("phosphorus", data.getPhosphorus() != null ? data.getPhosphorus() : 0.0)
                            .add("potassium", data.getPotassium() != null ? data.getPotassium() : 0.0)
                            .add("soilMoisture", data.getSoilMoisture() != null ? data.getSoilMoisture() : 0.0)
                            .add("pH", data.getPh() != null ? data.getPh() : 0.0)
                            .add("rainfall", data.getRainfall() != null ? data.getRainfall() : 0.0)
                            .add("timestamp", event.getReading().getTimestamp().toString())
                    )
                    .build();

            SensorDataWebSocket.broadcast(message.toString());
            logger.info("📡 WebSocketObserver: Broadcast sensor data to " +
                    SensorDataWebSocket.getConnectedClientsCount() + " clients");

        } catch (Exception e) {
            logger.severe("Failed to broadcast sensor data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcast alerts to connected WebSocket clients
     */
    public void onAlert(@ObservesAsync AlertTriggeredEvent event) {
        try {
            JsonObject message = Json.createObjectBuilder()
                    .add("type", "ALERT")
                    .add("payload", Json.createObjectBuilder()
                            .add("id", event.getAlert().getId())
                            .add("fieldId", event.getAlert().getFieldId())
                            .add("message", event.getAlert().getMessage())
                            .add("severity", event.getAlert().getSeverity())
                            .add("alertType", event.getAlert().getAlertType())
                            .add("timestamp", event.getAlert().getCreatedAt().toString())
                    )
                    .build();

            SensorDataWebSocket.broadcast(message.toString());
            logger.info("🚨 WebSocketObserver: Broadcast alert to " +
                    SensorDataWebSocket.getConnectedClientsCount() + " clients");

        } catch (Exception e) {
            logger.severe("Failed to broadcast alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
}