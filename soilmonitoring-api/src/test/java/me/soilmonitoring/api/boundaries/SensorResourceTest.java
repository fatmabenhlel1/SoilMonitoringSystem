package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.SensorRepository;
import me.soilmonitoring.api.entities.Sensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensorResourceTest {

    private SensorResource resource;
    private SoilMonitoringManager manager;
    private SensorRepository sensorRepository;
    private Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        resource = new SensorResource();
        manager = mock(SoilMonitoringManager.class);
        sensorRepository = mock(SensorRepository.class);
        logger = mock(Logger.class);

        inject(resource, "manager", manager);
        inject(resource, "sensorRepository", sensorRepository);
        inject(resource, "logger", logger);
    }

    private void inject(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    void testGetFieldSensors_success() {
        Sensor sensor = new Sensor();
        sensor.setId("s1");
        when(manager.getFieldSensors("field1")).thenReturn(Arrays.asList(sensor));

        Response response = resource.getFieldSensors("field1");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(1, ((java.util.List<?>) response.getEntity()).size());
    }

    @Test
    void testGetSensorById_found() {
        Sensor sensor = new Sensor();
        sensor.setId("s2");
        when(sensorRepository.findById("s2")).thenReturn(Optional.of(sensor));

        Response response = resource.getSensorById("s2");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(sensor, response.getEntity());
    }

    @Test
    void testGetSensorById_notFound() {
        when(sensorRepository.findById("s3")).thenReturn(Optional.empty());

        Response response = resource.getSensorById("s3");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Sensor not found", response.getEntity());
    }

    @Test
    void testGetSensorByDeviceId_found() {
        Sensor sensor = new Sensor();
        sensor.setId("s4");
        when(manager.findSensorByDeviceId("device1")).thenReturn(sensor);

        Response response = resource.getSensorByDeviceId("device1");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(sensor, response.getEntity());
    }

    @Test
    void testGetSensorByDeviceId_notFound() {
        when(manager.findSensorByDeviceId("device2")).thenThrow(new IllegalArgumentException());

        Response response = resource.getSensorByDeviceId("device2");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Sensor not found", response.getEntity());
    }

    @Test
    void testCreateSensor_success() {
        Sensor sensor = new Sensor();

        when(sensorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Response response = resource.createSensor(sensor);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Sensor saved = (Sensor) response.getEntity();
        assertNotNull(saved.getId());
        assertNotNull(saved.getInstalledAt());
        assertEquals("active", saved.getStatus());
        verify(logger).info(contains("Sensor created"));
    }

    @Test
    void testUpdateSensorStatus_success() {
        Sensor sensor = new Sensor();
        sensor.setId("s5");
        sensor.setStatus("inactive");
        when(sensorRepository.findById("s5")).thenReturn(Optional.of(sensor));
        when(sensorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Response response = resource.updateSensorStatus("s5", "active");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Sensor updated = (Sensor) response.getEntity();
        assertEquals("active", updated.getStatus());
        assertNotNull(updated.getLastConnection());
        verify(logger).info(contains("Sensor status updated"));
    }

    @Test
    void testUpdateSensorStatus_notFound() {
        when(sensorRepository.findById("s6")).thenReturn(Optional.empty());

        Response response = resource.updateSensorStatus("s6", "active");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Sensor not found", response.getEntity());
    }

    @Test
    void testDeleteSensor_success() {
        doNothing().when(sensorRepository).deleteById("s7");

        Response response = resource.deleteSensor("s7");

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(logger).info(contains("Sensor deleted"));
    }

    @Test
    void testDeleteSensor_error() {
        doThrow(new RuntimeException()).when(sensorRepository).deleteById("s8");

        Response response = resource.deleteSensor("s8");

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error deleting sensor", response.getEntity());
    }
}
