package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class SensorTest {

    @Test
    void testGettersAndSetters() {
        Sensor sensor = new Sensor();

        LocalDateTime now = LocalDateTime.of(2025, 11, 1, 12, 30);

        sensor.setId("s1");
        sensor.setFieldId("f1");
        sensor.setSensorType("DHT22");
        sensor.setDeviceId("raspi-001");
        sensor.setStatus("active");
        sensor.setLastConnection(now);
        sensor.setInstalledAt(now.minusDays(10));

        assertEquals("s1", sensor.getId());
        assertEquals("f1", sensor.getFieldId());
        assertEquals("DHT22", sensor.getSensorType());
        assertEquals("raspi-001", sensor.getDeviceId());
        assertEquals("active", sensor.getStatus());
        assertEquals(now, sensor.getLastConnection());
        assertEquals(now.minusDays(10), sensor.getInstalledAt());
    }

    @Test
    void testVersionIncrementAndValidation() {
        Sensor sensor = new Sensor();
        assertEquals(0L, sensor.getVersion());

        // Valid version increment
        sensor.setVersion(0L);
        assertEquals(1L, sensor.getVersion());

        // Attempt to set version again with mismatch should throw
        assertThrows(IllegalStateException.class, () -> sensor.setVersion(0L));
    }

    @Test
    void testStatusChangeAndDeviceAssociation() {
        Sensor sensor = new Sensor();

        sensor.setDeviceId("raspi-002");
        sensor.setStatus("inactive");
        sensor.setSensorType("NPK");

        assertEquals("raspi-002", sensor.getDeviceId());
        assertEquals("inactive", sensor.getStatus());
        assertEquals("NPK", sensor.getSensorType());

        // Change status and verify
        sensor.setStatus("error");
        assertEquals("error", sensor.getStatus());
    }

    @Test
    void testTimestampsConsistency() {
        Sensor sensor = new Sensor();

        LocalDateTime installTime = LocalDateTime.of(2025, 10, 20, 8, 0);
        LocalDateTime lastConnect = LocalDateTime.of(2025, 10, 31, 18, 45);

        sensor.setInstalledAt(installTime);
        sensor.setLastConnection(lastConnect);

        assertTrue(sensor.getLastConnection().isAfter(sensor.getInstalledAt()));
        assertEquals(installTime, sensor.getInstalledAt());
        assertEquals(lastConnect, sensor.getLastConnection());
    }
}
