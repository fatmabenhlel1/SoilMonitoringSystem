package me.soilmonitoring.api.observers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.controllers.repositories.FieldRepository;
import me.soilmonitoring.api.entities.Alert;
import me.soilmonitoring.api.entities.Field;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.events.AlertTriggeredEvent;
import me.soilmonitoring.api.events.SensorReadingEvent;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class AlertObserver {

    @Inject
    private Logger logger;

    @Inject
    private AlertRepository alertRepository;

    @Inject
    private Event<AlertTriggeredEvent> alertEvent;

    @Inject
    private FieldRepository fieldRepository;

    /**
     * Listen for sensor readings and check thresholds asynchronously
     */
    public void onSensorReading(@ObservesAsync SensorReadingEvent event) {
        logger.info("🔍 AlertObserver: Checking thresholds for reading " + event.getReading().getId());

        SensorData data = event.getReading().getData();
        String fieldId = event.getReading().getFieldId();

        // Temperature checks
        if (data.getTemperature() != null) {
            if (data.getTemperature() < 15) {
                createAndFireAlert(fieldId, "temperature", "high",
                        "Temperature critically low: " + String.format("%.1f", data.getTemperature()) + "°C");
            } else if (data.getTemperature() > 35) {
                createAndFireAlert(fieldId, "temperature", "high",
                        "Temperature critically high: " + String.format("%.1f", data.getTemperature()) + "°C");
            } else if (data.getTemperature() < 18 || data.getTemperature() > 32) {
                createAndFireAlert(fieldId, "temperature", "medium",
                        "Temperature approaching limits: " + String.format("%.1f", data.getTemperature()) + "°C");
            }
        }

        // Nitrogen check
        if (data.getNitrogen() != null && data.getNitrogen() < 30) {
            createAndFireAlert(fieldId, "npk_deficiency", "high",
                    "Low nitrogen levels detected: " + String.format("%.1f", data.getNitrogen()) + " mg/kg");
        }

        // Phosphorus check
        if (data.getPhosphorus() != null && data.getPhosphorus() < 15) {
            createAndFireAlert(fieldId, "npk_deficiency", "medium",
                    "Low phosphorus levels detected: " + String.format("%.1f", data.getPhosphorus()) + " mg/kg");
        }

        // Potassium check
        if (data.getPotassium() != null && data.getPotassium() < 100) {
            createAndFireAlert(fieldId, "npk_deficiency", "medium",
                    "Low potassium levels detected: " + String.format("%.1f", data.getPotassium()) + " mg/kg");
        }

        // Humidity check
        if (data.getHumidity() != null) {
            if (data.getHumidity() < 40) {
                createAndFireAlert(fieldId, "humidity", "medium",
                        "Low humidity: " + String.format("%.1f", data.getHumidity()) + "%");
            } else if (data.getHumidity() > 80) {
                createAndFireAlert(fieldId, "humidity", "medium",
                        "High humidity: " + String.format("%.1f", data.getHumidity()) + "%");
            }
        }

        // Soil moisture check
        if (data.getSoilMoisture() != null) {
            if (data.getSoilMoisture() < 20) {
                createAndFireAlert(fieldId, "moisture", "high",
                        "Low soil moisture: " + String.format("%.1f", data.getSoilMoisture()) + "%");
            } else if (data.getSoilMoisture() > 60) {
                createAndFireAlert(fieldId, "moisture", "medium",
                        "High soil moisture: " + String.format("%.1f", data.getSoilMoisture()) + "%");
            }
        }

        // pH check
        if (data.getPh() != null) {
            if (data.getPh() < 6.0 || data.getPh() > 7.5) {
                createAndFireAlert(fieldId, "ph", "medium",
                        "pH out of optimal range: " + String.format("%.2f", data.getPh()));
            }
        }
    }

    private void createAndFireAlert(String fieldId, String type, String severity, String message) {
        try {
            Alert alert = new Alert();
            alert.setId(UUID.randomUUID().toString());
            alert.setFieldId(fieldId);
            alert.setAlertType(type);
            // Get userId from field
            Field field = fieldRepository.findById(fieldId).orElse(null);
            if (field != null) {
                alert.setUserId(field.getUserId());
            }
            alert.setSeverity(severity);
            alert.setMessage(message);
            alert.setIsRead(false);
            alert.setCreatedAt(LocalDateTime.now());

            alertRepository.save(alert);
            logger.info("⚠️ Alert created: " + message);

            // Fire alert event for WebSocket broadcasting
            alertEvent.fireAsync(new AlertTriggeredEvent(alert));

        } catch (Exception e) {
            logger.severe("Failed to create alert: " + e.getMessage());
        }
    }
}