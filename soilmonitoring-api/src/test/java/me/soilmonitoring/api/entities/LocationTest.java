package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    @Test
    void testDefaultConstructorAndSetters() {
        Location location = new Location();
        location.setLatitude(36.8065);
        location.setLongitude(10.1815);
        location.setAddress("Tunis, Tunisia");

        assertEquals(36.8065, location.getLatitude());
        assertEquals(10.1815, location.getLongitude());
        assertEquals("Tunis, Tunisia", location.getAddress());
    }

    @Test
    void testParameterizedConstructor() {
        Location location = new Location(36.8, 10.2, "Carthage");

        assertEquals(36.8, location.getLatitude());
        assertEquals(10.2, location.getLongitude());
        assertEquals("Carthage", location.getAddress());
    }

    @Test
    void testMutability() {
        Location location = new Location();
        location.setLatitude(35.0);
        assertEquals(35.0, location.getLatitude());

        location.setLatitude(37.0);
        assertEquals(37.0, location.getLatitude());
    }
}
