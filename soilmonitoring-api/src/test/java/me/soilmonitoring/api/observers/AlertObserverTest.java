package me.soilmonitoring.api.observers;

import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.controllers.repositories.FieldRepository;
import me.soilmonitoring.api.entities.Alert;
import me.soilmonitoring.api.entities.Field;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.entities.SensorReading;
import me.soilmonitoring.api.events.AlertTriggeredEvent;
import me.soilmonitoring.api.events.SensorReadingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import jakarta.enterprise.event.Event;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AlertObserver Tests")
class AlertObserverTest {

    @Mock
    private Logger logger;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private Event<AlertTriggeredEvent> alertEvent;

    @InjectMocks
    private AlertObserver alertObserver;

    private SensorReading reading;
    private SensorData data;
    private Field field;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        data = new SensorData();
        data.setTemperature(10.0);
        data.setNitrogen(20.0);
        data.setPhosphorus(10.0);
        data.setPotassium(50.0);
        data.setHumidity(30.0);
        data.setSoilMoisture(10.0);
        data.setPh(5.5);

        reading = new SensorReading();
        reading.setId("reading-123");
        reading.setFieldId("field-001");
        reading.setTimestamp(LocalDateTime.now());
        reading.setData(data);

        field = new Field();
        field.setId("field-001");
        field.setUserId("user-123");
        field.setName("Test Field");
    }

    @Test
    @DisplayName("Should create alerts based on sensor thresholds")
    void testThresholdAlertsTriggered() {

        when(fieldRepository.findById("field-001")).thenReturn(Optional.of(field));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SensorReadingEvent event = new SensorReadingEvent(reading, "test-source");

        alertObserver.onSensorReading(event);

        // Verify that multiple alerts are triggered
        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
        verify(alertEvent, atLeastOnce()).fireAsync(any(AlertTriggeredEvent.class));
        verify(logger, atLeastOnce()).info(contains("Alert created"));
    }

    @Test
    @DisplayName("Should handle missing field gracefully")
    void testMissingField() {

        when(fieldRepository.findById("field-001")).thenReturn(Optional.empty());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SensorReadingEvent event = new SensorReadingEvent(reading, "test-source");

        alertObserver.onSensorReading(event);

        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
        verify(alertEvent, atLeastOnce()).fireAsync(any(AlertTriggeredEvent.class));
    }

    @Test
    @DisplayName("Should log error when alert persistence fails")
    void testAlertSaveFailure() {

        when(fieldRepository.findById("field-001")).thenReturn(Optional.of(field));
        doThrow(new RuntimeException("DB ERROR")).when(alertRepository).save(any(Alert.class));

        SensorReadingEvent event = new SensorReadingEvent(reading, "test-source");

        alertObserver.onSensorReading(event);

        verify(logger, atLeastOnce()).severe(contains("Failed to create alert"));
    }

    @Test
    @DisplayName("Should not create alert if values are normal")
    void testNoAlertsWhenValuesNormal() {

        data.setTemperature(25.0);
        data.setNitrogen(50.0);
        data.setPhosphorus(20.0);
        data.setPotassium(150.0);
        data.setHumidity(60.0);
        data.setSoilMoisture(40.0);
        data.setPh(7.0);

        when(fieldRepository.findById("field-001")).thenReturn(Optional.of(field));

        SensorReadingEvent event = new SensorReadingEvent(reading, "test-source");

        alertObserver.onSensorReading(event);

        verify(alertRepository, never()).save(any(Alert.class));
        verify(alertEvent, never()).fireAsync(any(AlertTriggeredEvent.class));
    }
}
