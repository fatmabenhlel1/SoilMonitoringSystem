package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SensorDataTest {

    @Test
    void testGettersAndSetters() {
        SensorData data = new SensorData();

        data.setTemperature(25.5);
        data.setHumidity(60.0);
        data.setNitrogen(10.0);
        data.setPhosphorus(5.5);
        data.setPotassium(7.0);
        data.setSoilMoisture(30.0);
        data.setRainfall(12.0);

        assertEquals(25.5, data.getTemperature());
        assertEquals(60.0, data.getHumidity());
        assertEquals(10.0, data.getNitrogen());
        assertEquals(5.5, data.getPhosphorus());
        assertEquals(7.0, data.getPotassium());
        assertEquals(30.0, data.getSoilMoisture());
        assertEquals(12.0, data.getRainfall());
    }

    @Test
    void testMutability() {
        SensorData data = new SensorData();

        data.setTemperature(20.0);
        assertEquals(20.0, data.getTemperature());

        data.setTemperature(22.5);
        assertEquals(22.5, data.getTemperature());
    }
}
