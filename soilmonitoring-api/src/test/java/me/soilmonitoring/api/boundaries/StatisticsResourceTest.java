package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.entities.SensorReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsResourceTest {

    private StatisticsResource resource;
    private SoilMonitoringManager manager;
    private Logger logger;

    @BeforeEach
    void setUp() throws Exception {
        resource = new StatisticsResource();
        manager = mock(SoilMonitoringManager.class);
        logger = mock(Logger.class);

        inject(resource, "manager", manager);
        inject(resource, "logger", logger);
    }

    private void inject(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }




    @Test
    void testGetHourlyStatistics_noReadings() {
        String fieldId = "field2";
        when(manager.getFieldReadingsByTimeRange(eq(fieldId), any(), any())).thenReturn(new ArrayList<>());

        Response response = resource.getHourlyStatistics(fieldId);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("[]", response.getEntity());
    }

    @Test
    void testGetTodaySummary_noReadings() {
        String fieldId = "field2";
        when(manager.getFieldReadingsByTimeRange(eq(fieldId), any(), any())).thenReturn(new ArrayList<>());

        Response response = resource.getTodaySummary(fieldId);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        String json = (String) response.getEntity();
        assertTrue(json.contains("No readings for today"));
    }
}
