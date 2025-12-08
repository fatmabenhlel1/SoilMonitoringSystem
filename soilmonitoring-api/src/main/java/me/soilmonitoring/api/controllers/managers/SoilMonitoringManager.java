package me.soilmonitoring.api.controllers.managers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.soilmonitoring.api.controllers.repositories.*;
import me.soilmonitoring.api.entities.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//TODO: add test file


@Singleton
public class SoilMonitoringManager {

    @Inject
    private UserRepository userRepository;

    @Inject
    private FieldRepository fieldRepository;

    @Inject
    private SensorRepository sensorRepository;

    @Inject
    private SensorReadingRepository sensorReadingRepository;

    @Inject
    private PredictionRepository predictionRepository;

    @Inject
    private AlertRepository alertRepository;

    @Inject
    private TreatmentRepository treatmentRepository;

    /* ******* user *********
    */

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user
     * @return the User entity
     * @throws IllegalArgumentException if no user is found
     */

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }


    /* ****** field *********
     */

    /**
     * Finds a field by its ID.
     *
     * @param fieldId the ID of the field
     * @return the Field entity
     * @throws IllegalArgumentException if no field is found
     */
    public Field findFieldById(String fieldId) {
        return fieldRepository.findById(fieldId).orElseThrow(IllegalArgumentException::new);
    }


    /**
     * Gets all fields owned by a specific user.
     *
     * @param userId the ID of the user
     * @return a list of fields owned by the user
     */
    public List<Field> getUserFields(String userId) {
        return fieldRepository.findByUserId(userId);
    }


    /*  ***sensors******
     */

    /**
     * Finds a sensor by its unique device ID.
     *
     * @param deviceId the device ID of the sensor
     * @return the Sensor entity
     * @throws IllegalArgumentException if no sensor is found
     */
    public Sensor findSensorByDeviceId(String deviceId) {
        return sensorRepository.findByDeviceId(deviceId).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Gets all sensors installed in a specific field.
     *
     * @param fieldId the ID of the field
     * @return a list of sensors
     */
    public List<Sensor> getFieldSensors(String fieldId) {
        return sensorRepository.findByFieldId(fieldId);
    }


    /* *****sensor reading ******
    */


    /**
     * Gets all sensor readings for a specific field.
     *
     * @param fieldId the ID of the field
     * @return a list of sensor readings
     */
    public List<SensorReading> getFieldReadings(String fieldId) {
        return sensorReadingRepository.findByFieldId(fieldId);
    }

    /**
     * Gets sensor readings for a specific field within a time range.
     *
     * @param fieldId the ID of the field
     * @param from    the start of the time range
     * @param to      the end of the time range
     * @return a list of sensor readings
     */
    public List<SensorReading> getFieldReadingsByTimeRange(String fieldId, LocalDateTime from, LocalDateTime to) {
        return sensorReadingRepository.findByFieldIdAndTimestampBetween(fieldId, from, to);
    }

    /* ******predictions******
    */

    /**
     * Gets predictions for a specific field filtered by prediction type.
     *
     * @param fieldId        the ID of the field
     * @param predictionType the type of prediction ("crop", "fertilizer")
     * @return a list of predictions
     */
    public List<Prediction> getFieldPredictions(String fieldId, String predictionType) {
        return predictionRepository.findByFieldIdAndPredictionType(fieldId, predictionType);
    }

    /* *******alerts******
    */

    /**
     * Gets all unread alerts for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of unread alerts
     */
    public List<Alert> getUnreadAlerts(String userId) {
        return alertRepository.findByUserIdAndIsRead(userId, false);
    }

    /* *****treatments******
    */

    /**
     * Creates an alert and automatically attaches the field's location.
     *
     * @param userId the ID of the user to notify
     * @param fieldId the ID of the field where the issue occurred
     * @param alertType the type of alert
     * @param severity the severity level
     * @param message the alert message
     * @return the created Alert entity
     */
    public Alert createAlertWithLocation(String userId, String fieldId, String alertType,
                                         String severity, String message) {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setFieldId(fieldId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setIsRead(false);
        alert.setCreatedAt(LocalDateTime.now());

        // Attach field location to alert
        try {
            Field field = fieldRepository.findById(fieldId).orElse(null);
            if (field != null && field.getLocation() != null) {
                alert.setLocation(field.getLocation());
            }
        } catch (Exception e) {
            // Log but don't fail - location is optional
        }

        return alertRepository.save(alert);
    }

    /**
     * Gets all treatments applied to a specific field.
     *
     * @param fieldId the ID of the field
     * @return a list of treatments
     */
    public List<Treatment> getFieldTreatments(String fieldId) {
        return treatmentRepository.findByFieldId(fieldId);
    }




}