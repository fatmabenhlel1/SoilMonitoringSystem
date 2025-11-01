package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SensorReadingTest {

    @Test
    void testGettersAndSetters() {
        SensorReading reading = new SensorReading();

        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 1, 14, 0);
        SensorData data = new SensorData();
        data.setTemperature(25.5);
        data.setHumidity(60.0);
        data.setNitrogen(10.0);

        reading.setId("r1");
        reading.setSensorId("s1");
        reading.setFieldId("f1");
        reading.setTimestamp(timestamp);
        reading.setData(data);

        assertEquals("r1", reading.getId());
        assertEquals("s1", reading.getSensorId());
        assertEquals("f1", reading.getFieldId());
        assertEquals(timestamp, reading.getTimestamp());
        assertEquals(data, reading.getData());
        assertEquals(25.5, reading.getData().getTemperature());
        assertEquals(60.0, reading.getData().getHumidity());
        assertEquals(10.0, reading.getData().getNitrogen());
    }

    @Test
    void testVersionIncrementAndValidation() {
        SensorReading reading = new SensorReading();
        assertEquals(0L, reading.getVersion());

        // Valid version increment
        reading.setVersion(0L);
        assertEquals(1L, reading.getVersion());

        // Attempt to set version again with mismatch should throw
        assertThrows(IllegalStateException.class, () -> reading.setVersion(0L));
    }

    @Test
    void testSensorDataEmbedding() {
        SensorData data = new SensorData();
        data.setTemperature(22.0);
        data.setHumidity(55.0);
        data.setSoilMoisture(30.0);

        SensorReading reading = new SensorReading();
        reading.setData(data);

        assertNotNull(reading.getData());
        assertEquals(22.0, reading.getData().getTemperature());
        assertEquals(55.0, reading.getData().getHumidity());
        assertEquals(30.0, reading.getData().getSoilMoisture());
    }
}
