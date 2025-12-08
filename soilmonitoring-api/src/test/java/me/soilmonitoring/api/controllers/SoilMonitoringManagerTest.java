package me.soilmonitoring.api.controllers.managers;

import me.soilmonitoring.api.controllers.repositories.*;
import me.soilmonitoring.api.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoilMonitoringManager Tests")
class SoilMonitoringManagerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private SensorReadingRepository sensorReadingRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @InjectMocks
    private SoilMonitoringManager manager;

    private User testUser;
    private Field testField;
    private Sensor testSensor;
    private SensorReading testReading;
    private Prediction testPrediction;
    private Alert testAlert;
    private Treatment testTreatment;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId("user-001");
        testUser.setUsername("john.doe");
        testUser.setEmail("john@example.com");
        testUser.setFullName("John Doe");
        testUser.setRole("farmer");
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup test field
        testField = new Field();
        testField.setId("field-001");
        testField.setUserId("user-001");
        testField.setName("North Field");
        testField.setArea(5.5);
        testField.setCurrentCrop("Wheat");
        testField.setSoilType("Clay");
        testField.setCreatedAt(LocalDateTime.now());

        // Setup test sensor
        testSensor = new Sensor();
        testSensor.setId("sensor-001");
        testSensor.setFieldId("field-001");
        testSensor.setSensorType("DHT22");
        testSensor.setDeviceId("device-123");
        testSensor.setStatus("active");
        testSensor.setInstalledAt(LocalDateTime.now());

        // Setup test sensor reading
        testReading = new SensorReading();
        testReading.setId("reading-001");
        testReading.setSensorId("sensor-001");
        testReading.setFieldId("field-001");
        testReading.setTimestamp(LocalDateTime.now());
        SensorData data = new SensorData();
        data.setTemperature(25.5);
        data.setHumidity(65.0);
        testReading.setData(data);

        // Setup test prediction
        testPrediction = new Prediction();
        testPrediction.setId("pred-001");
        testPrediction.setFieldId("field-001");
        testPrediction.setPredictionType("crop");
        testPrediction.setModelUsed("XGBoost");
        testPrediction.setConfidence(0.92);
        testPrediction.setCreatedAt(LocalDateTime.now());

        // Setup test alert
        testAlert = new Alert();
        testAlert.setId("alert-001");
        testAlert.setUserId("user-001");
        testAlert.setFieldId("field-001");
        testAlert.setAlertType("temperature");
        testAlert.setSeverity("high");
        testAlert.setMessage("Temperature too high");
        testAlert.setIsRead(false);
        testAlert.setCreatedAt(LocalDateTime.now());

        // Setup test treatment
        testTreatment = new Treatment();
        testTreatment.setId("treatment-001");
        testTreatment.setFieldId("field-001");
        testTreatment.setTreatmentType("fertilizer");
        testTreatment.setProductName("NPK 15-15-15");
        testTreatment.setQuantity("100kg");
        testTreatment.setAppliedAt(LocalDateTime.now());
        testTreatment.setAppliedBy("user-001");
    }

    // ===== Tests pour findUserByUsername =====

    @Test
    @DisplayName("Should find user by username successfully")
    void testFindUserByUsernameSuccess() {
        // Given
        String username = "john.doe";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        User result = manager.findUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when user not found by username")
    void testFindUserByUsernameNotFound() {
        // Given
        String username = "non.existent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            manager.findUserByUsername(username);
        });
        verify(userRepository, times(1)).findByUsername(username);
    }

    // ===== Tests pour findFieldById =====

    @Test
    @DisplayName("Should find field by id successfully")
    void testFindFieldByIdSuccess() {
        // Given
        String fieldId = "field-001";
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(testField));

        // When
        Field result = manager.findFieldById(fieldId);

        // Then
        assertNotNull(result);
        assertEquals(testField.getId(), result.getId());
        assertEquals(testField.getName(), result.getName());
        assertEquals(testField.getUserId(), result.getUserId());
        verify(fieldRepository, times(1)).findById(fieldId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when field not found by id")
    void testFindFieldByIdNotFound() {
        // Given
        String fieldId = "non-existent";
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            manager.findFieldById(fieldId);
        });
        verify(fieldRepository, times(1)).findById(fieldId);
    }

    // ===== Tests pour getUserFields =====

    @Test
    @DisplayName("Should get all fields for a user")
    void testGetUserFieldsSuccess() {
        // Given
        String userId = "user-001";
        Field field2 = new Field();
        field2.setId("field-002");
        field2.setUserId(userId);
        field2.setName("South Field");

        List<Field> fields = Arrays.asList(testField, field2);
        when(fieldRepository.findByUserId(userId)).thenReturn(fields);

        // When
        List<Field> result = manager.getUserFields(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testField.getId(), result.get(0).getId());
        assertEquals(field2.getId(), result.get(1).getId());
        verify(fieldRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no fields")
    void testGetUserFieldsEmpty() {
        // Given
        String userId = "user-999";
        when(fieldRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // When
        List<Field> result = manager.getUserFields(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fieldRepository, times(1)).findByUserId(userId);
    }

    // ===== Tests pour findSensorByDeviceId =====

    @Test
    @DisplayName("Should find sensor by device id successfully")
    void testFindSensorByDeviceIdSuccess() {
        // Given
        String deviceId = "device-123";
        when(sensorRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(testSensor));

        // When
        Sensor result = manager.findSensorByDeviceId(deviceId);

        // Then
        assertNotNull(result);
        assertEquals(testSensor.getId(), result.getId());
        assertEquals(testSensor.getDeviceId(), result.getDeviceId());
        assertEquals(testSensor.getSensorType(), result.getSensorType());
        verify(sensorRepository, times(1)).findByDeviceId(deviceId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when sensor not found by device id")
    void testFindSensorByDeviceIdNotFound() {
        // Given
        String deviceId = "non-existent";
        when(sensorRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            manager.findSensorByDeviceId(deviceId);
        });
        verify(sensorRepository, times(1)).findByDeviceId(deviceId);
    }

    // ===== Tests pour getFieldSensors =====

    @Test
    @DisplayName("Should get all sensors for a field")
    void testGetFieldSensorsSuccess() {
        // Given
        String fieldId = "field-001";
        Sensor sensor2 = new Sensor();
        sensor2.setId("sensor-002");
        sensor2.setFieldId(fieldId);
        sensor2.setSensorType("NPK");
        sensor2.setDeviceId("device-456");

        List<Sensor> sensors = Arrays.asList(testSensor, sensor2);
        when(sensorRepository.findByFieldId(fieldId)).thenReturn(sensors);

        // When
        List<Sensor> result = manager.getFieldSensors(fieldId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testSensor.getId(), result.get(0).getId());
        assertEquals(sensor2.getId(), result.get(1).getId());
        verify(sensorRepository, times(1)).findByFieldId(fieldId);
    }

    @Test
    @DisplayName("Should return empty list when field has no sensors")
    void testGetFieldSensorsEmpty() {
        // Given
        String fieldId = "field-999";
        when(sensorRepository.findByFieldId(fieldId)).thenReturn(Arrays.asList());

        // When
        List<Sensor> result = manager.getFieldSensors(fieldId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sensorRepository, times(1)).findByFieldId(fieldId);
    }

    // ===== Tests pour getFieldReadings =====

    @Test
    @DisplayName("Should get all readings for a field")
    void testGetFieldReadingsSuccess() {
        // Given
        String fieldId = "field-001";
        SensorReading reading2 = new SensorReading();
        reading2.setId("reading-002");
        reading2.setFieldId(fieldId);
        reading2.setTimestamp(LocalDateTime.now());

        List<SensorReading> readings = Arrays.asList(testReading, reading2);
        when(sensorReadingRepository.findByFieldId(fieldId)).thenReturn(readings);

        // When
        List<SensorReading> result = manager.getFieldReadings(fieldId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testReading.getId(), result.get(0).getId());
        assertEquals(reading2.getId(), result.get(1).getId());
        verify(sensorReadingRepository, times(1)).findByFieldId(fieldId);
    }

    @Test
    @DisplayName("Should return empty list when field has no readings")
    void testGetFieldReadingsEmpty() {
        // Given
        String fieldId = "field-999";
        when(sensorReadingRepository.findByFieldId(fieldId)).thenReturn(Arrays.asList());

        // When
        List<SensorReading> result = manager.getFieldReadings(fieldId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sensorReadingRepository, times(1)).findByFieldId(fieldId);
    }

    // ===== Tests pour getFieldReadingsByTimeRange =====

    @Test
    @DisplayName("Should get field readings by time range")
    void testGetFieldReadingsByTimeRangeSuccess() {
        // Given
        String fieldId = "field-001";
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        List<SensorReading> readings = Arrays.asList(testReading);
        when(sensorReadingRepository.findByFieldIdAndTimestampBetween(fieldId, from, to))
                .thenReturn(readings);

        // When
        List<SensorReading> result = manager.getFieldReadingsByTimeRange(fieldId, from, to);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReading.getId(), result.get(0).getId());
        verify(sensorReadingRepository, times(1))
                .findByFieldIdAndTimestampBetween(fieldId, from, to);
    }

    @Test
    @DisplayName("Should return empty list when no readings in time range")
    void testGetFieldReadingsByTimeRangeEmpty() {
        // Given
        String fieldId = "field-001";
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now().minusDays(20);

        when(sensorReadingRepository.findByFieldIdAndTimestampBetween(fieldId, from, to))
                .thenReturn(Arrays.asList());

        // When
        List<SensorReading> result = manager.getFieldReadingsByTimeRange(fieldId, from, to);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(sensorReadingRepository, times(1))
                .findByFieldIdAndTimestampBetween(fieldId, from, to);
    }

    @Test
    @DisplayName("Should get multiple readings in time range")
    void testGetFieldReadingsByTimeRangeMultiple() {
        // Given
        String fieldId = "field-001";
        LocalDateTime from = LocalDateTime.now().minusHours(24);
        LocalDateTime to = LocalDateTime.now();

        SensorReading reading1 = new SensorReading();
        reading1.setId("reading-001");
        reading1.setTimestamp(LocalDateTime.now().minusHours(12));

        SensorReading reading2 = new SensorReading();
        reading2.setId("reading-002");
        reading2.setTimestamp(LocalDateTime.now().minusHours(6));

        SensorReading reading3 = new SensorReading();
        reading3.setId("reading-003");
        reading3.setTimestamp(LocalDateTime.now().minusHours(1));

        List<SensorReading> readings = Arrays.asList(reading1, reading2, reading3);
        when(sensorReadingRepository.findByFieldIdAndTimestampBetween(fieldId, from, to))
                .thenReturn(readings);

        // When
        List<SensorReading> result = manager.getFieldReadingsByTimeRange(fieldId, from, to);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(sensorReadingRepository, times(1))
                .findByFieldIdAndTimestampBetween(fieldId, from, to);
    }

    // ===== Tests pour getFieldPredictions =====

    @Test
    @DisplayName("Should get field predictions by type")
    void testGetFieldPredictionsSuccess() {
        // Given
        String fieldId = "field-001";
        String predictionType = "crop";

        List<Prediction> predictions = Arrays.asList(testPrediction);
        when(predictionRepository.findByFieldIdAndPredictionType(fieldId, predictionType))
                .thenReturn(predictions);

        // When
        List<Prediction> result = manager.getFieldPredictions(fieldId, predictionType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPrediction.getId(), result.get(0).getId());
        assertEquals(predictionType, result.get(0).getPredictionType());
        verify(predictionRepository, times(1))
                .findByFieldIdAndPredictionType(fieldId, predictionType);
    }

    @Test
    @DisplayName("Should return empty list when no predictions of specified type")
    void testGetFieldPredictionsEmpty() {
        // Given
        String fieldId = "field-001";
        String predictionType = "fertilizer";

        when(predictionRepository.findByFieldIdAndPredictionType(fieldId, predictionType))
                .thenReturn(Arrays.asList());

        // When
        List<Prediction> result = manager.getFieldPredictions(fieldId, predictionType);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(predictionRepository, times(1))
                .findByFieldIdAndPredictionType(fieldId, predictionType);
    }

    @Test
    @DisplayName("Should get multiple predictions of same type")
    void testGetFieldPredictionsMultiple() {
        // Given
        String fieldId = "field-001";
        String predictionType = "crop";

        Prediction pred1 = new Prediction();
        pred1.setId("pred-001");
        pred1.setPredictionType(predictionType);

        Prediction pred2 = new Prediction();
        pred2.setId("pred-002");
        pred2.setPredictionType(predictionType);

        List<Prediction> predictions = Arrays.asList(pred1, pred2);
        when(predictionRepository.findByFieldIdAndPredictionType(fieldId, predictionType))
                .thenReturn(predictions);

        // When
        List<Prediction> result = manager.getFieldPredictions(fieldId, predictionType);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(predictionRepository, times(1))
                .findByFieldIdAndPredictionType(fieldId, predictionType);
    }

    // ===== Tests pour getUnreadAlerts =====

    @Test
    @DisplayName("Should get unread alerts for user")
    void testGetUnreadAlertsSuccess() {
        // Given
        String userId = "user-001";
        Alert alert2 = new Alert();
        alert2.setId("alert-002");
        alert2.setUserId(userId);
        alert2.setIsRead(false);

        List<Alert> alerts = Arrays.asList(testAlert, alert2);
        when(alertRepository.findByUserIdAndIsRead(userId, false)).thenReturn(alerts);

        // When
        List<Alert> result = manager.getUnreadAlerts(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testAlert.getId(), result.get(0).getId());
        assertFalse(result.get(0).getIsRead());
        assertEquals(alert2.getId(), result.get(1).getId());
        assertFalse(result.get(1).getIsRead());
        verify(alertRepository, times(1)).findByUserIdAndIsRead(userId, false);
    }

    @Test
    @DisplayName("Should return empty list when user has no unread alerts")
    void testGetUnreadAlertsEmpty() {
        // Given
        String userId = "user-999";
        when(alertRepository.findByUserIdAndIsRead(userId, false)).thenReturn(Arrays.asList());

        // When
        List<Alert> result = manager.getUnreadAlerts(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(alertRepository, times(1)).findByUserIdAndIsRead(userId, false);
    }

    @Test
    @DisplayName("Should only return unread alerts")
    void testGetUnreadAlertsOnlyUnread() {
        // Given
        String userId = "user-001";
        Alert unreadAlert = new Alert();
        unreadAlert.setId("alert-unread");
        unreadAlert.setUserId(userId);
        unreadAlert.setIsRead(false);

        List<Alert> alerts = Arrays.asList(unreadAlert);
        when(alertRepository.findByUserIdAndIsRead(userId, false)).thenReturn(alerts);

        // When
        List<Alert> result = manager.getUnreadAlerts(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
        verify(alertRepository, times(1)).findByUserIdAndIsRead(userId, false);
    }

    // ===== Tests pour getFieldTreatments =====

    @Test
    @DisplayName("Should get all treatments for a field")
    void testGetFieldTreatmentsSuccess() {
        // Given
        String fieldId = "field-001";
        Treatment treatment2 = new Treatment();
        treatment2.setId("treatment-002");
        treatment2.setFieldId(fieldId);
        treatment2.setTreatmentType("pesticide");

        List<Treatment> treatments = Arrays.asList(testTreatment, treatment2);
        when(treatmentRepository.findByFieldId(fieldId)).thenReturn(treatments);

        // When
        List<Treatment> result = manager.getFieldTreatments(fieldId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testTreatment.getId(), result.get(0).getId());
        assertEquals(treatment2.getId(), result.get(1).getId());
        verify(treatmentRepository, times(1)).findByFieldId(fieldId);
    }

    @Test
    @DisplayName("Should return empty list when field has no treatments")
    void testGetFieldTreatmentsEmpty() {
        // Given
        String fieldId = "field-999";
        when(treatmentRepository.findByFieldId(fieldId)).thenReturn(Arrays.asList());

        // When
        List<Treatment> result = manager.getFieldTreatments(fieldId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(treatmentRepository, times(1)).findByFieldId(fieldId);
    }

    @Test
    @DisplayName("Should get multiple treatments of different types")
    void testGetFieldTreatmentsMultipleTypes() {
        // Given
        String fieldId = "field-001";

        Treatment fertilizer = new Treatment();
        fertilizer.setId("treatment-fert");
        fertilizer.setTreatmentType("fertilizer");

        Treatment pesticide = new Treatment();
        pesticide.setId("treatment-pest");
        pesticide.setTreatmentType("pesticide");

        Treatment irrigation = new Treatment();
        irrigation.setId("treatment-irr");
        irrigation.setTreatmentType("irrigation");

        List<Treatment> treatments = Arrays.asList(fertilizer, pesticide, irrigation);
        when(treatmentRepository.findByFieldId(fieldId)).thenReturn(treatments);

        // When
        List<Treatment> result = manager.getFieldTreatments(fieldId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(treatmentRepository, times(1)).findByFieldId(fieldId);
    }

    // ===== Tests d'intégration =====

    @Test
    @DisplayName("Should handle multiple operations for same entity")
    void testMultipleOperationsSameEntity() {
        // Given
        String fieldId = "field-001";
        when(fieldRepository.findById(fieldId)).thenReturn(Optional.of(testField));
        when(sensorRepository.findByFieldId(fieldId)).thenReturn(Arrays.asList(testSensor));
        when(sensorReadingRepository.findByFieldId(fieldId)).thenReturn(Arrays.asList(testReading));

        // When
        Field field = manager.findFieldById(fieldId);
        List<Sensor> sensors = manager.getFieldSensors(fieldId);
        List<SensorReading> readings = manager.getFieldReadings(fieldId);

        // Then
        assertNotNull(field);
        assertNotNull(sensors);
        assertNotNull(readings);
        assertEquals(1, sensors.size());
        assertEquals(1, readings.size());

        verify(fieldRepository, times(1)).findById(fieldId);
        verify(sensorRepository, times(1)).findByFieldId(fieldId);
        verify(sensorReadingRepository, times(1)).findByFieldId(fieldId);
    }
}