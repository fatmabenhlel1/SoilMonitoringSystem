package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.controllers.repositories.SensorReadingRepository;
import me.soilmonitoring.api.entities.Alert;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.entities.SensorReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadingResourceTest {

    private ReadingResource resource;
    private SoilMonitoringManager manager;
    private SensorReadingRepository readingRepository;
    private AlertRepository alertRepository;
    private Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        resource = new ReadingResource();
        manager = mock(SoilMonitoringManager.class);
        readingRepository = mock(SensorReadingRepository.class);
        alertRepository = mock(AlertRepository.class);
        logger = mock(Logger.class);

        // Inject mocks via reflection
        inject(resource, "manager", manager);
        inject(resource, "readingRepository", readingRepository);
        inject(resource, "alertRepository", alertRepository);
        inject(resource, "logger", logger);
    }

    private void inject(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testGetFieldReadings_success() {
        SensorReading reading = new SensorReading();
        reading.setId("r1");
        when(manager.getFieldReadings("field1")).thenReturn(List.of(reading));

        Response response = resource.getFieldReadings("field1");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<?> readings = (List<?>) response.getEntity();
        assertEquals(1, readings.size());
        assertEquals(reading, readings.get(0));
    }

    @Test
    void testGetFieldReadingsByTimeRange_success() {
        SensorReading reading = new SensorReading();
        reading.setId("r2");
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(manager.getFieldReadingsByTimeRange(eq("field1"), any(), any())).thenReturn(List.of(reading));

        Response response = resource.getFieldReadingsByTimeRange("field1", from.toString(), to.toString());

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<?> readings = (List<?>) response.getEntity();
        assertEquals(1, readings.size());
        assertEquals(reading, readings.get(0));
    }

    @Test
    void testGetReadingById_found() {
        SensorReading reading = new SensorReading();
        reading.setId("r3");
        when(readingRepository.findById("r3")).thenReturn(Optional.of(reading));

        Response response = resource.getReadingById("r3");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(reading, response.getEntity());
    }

    @Test
    void testGetReadingById_notFound() {
        when(readingRepository.findById("r4")).thenReturn(Optional.empty());

        Response response = resource.getReadingById("r4");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reading not found", response.getEntity());
    }

    @Test
    void testCreateReading_success() {
        SensorReading reading = new SensorReading();
        reading.setData(new SensorData());

        when(readingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Response response = resource.createReading(reading);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        SensorReading saved = (SensorReading) response.getEntity();
        assertNotNull(saved.getId());
        assertNotNull(saved.getTimestamp());
        verify(logger).info(contains("Sensor reading created"));
    }

    @Test
    void testGetLatestReading_found() {
        SensorReading r1 = new SensorReading();
        r1.setTimestamp(LocalDateTime.now().minusHours(1));
        SensorReading r2 = new SensorReading();
        r2.setTimestamp(LocalDateTime.now());
        when(manager.getFieldReadings("field1")).thenReturn(Arrays.asList(r1, r2));

        Response response = resource.getLatestReading("field1");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(r2, response.getEntity());
    }

    @Test
    void testGetLatestReading_notFound() {
        when(manager.getFieldReadings("field1")).thenReturn(Collections.emptyList());

        Response response = resource.getLatestReading("field1");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No readings found", response.getEntity());
    }

    @Test
    void testGetFieldSummary_noReadings() {
        when(manager.getFieldReadings("field1")).thenReturn(Collections.emptyList());

        Response response = resource.getFieldSummary("field1");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("No readings found"));
    }
}
