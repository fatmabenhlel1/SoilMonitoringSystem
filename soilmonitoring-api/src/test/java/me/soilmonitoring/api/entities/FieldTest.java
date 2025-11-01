package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    private Field field;

    @BeforeEach
    void setUp() {
        field = new Field();
    }

    // --- ID ---
    @Test
    void testGetAndSetId() {
        field.setId("field-001");
        assertEquals("field-001", field.getId());
    }

    // --- Version ---
    @Test
    void testInitialVersionIsZero() {
        assertEquals(0L, field.getVersion());
    }

    @Test
    void testSetVersionThrowsIfDifferent() {
        // Version starts at 0, so setting any different value should throw
        assertThrows(IllegalStateException.class, () -> field.setVersion(1L));
    }

    @Test
    void testSetVersionIncrementsWhenEqual() {
        long initialVersion = field.getVersion();
        field.setVersion(initialVersion);
        assertEquals(initialVersion + 1, field.getVersion());
    }

    // --- User ID ---
    @Test
    void testGetAndSetUserId() {
        field.setUserId("user123");
        assertEquals("user123", field.getUserId());
    }

    // --- Name ---
    @Test
    void testGetAndSetName() {
        field.setName("North Farm");
        assertEquals("North Farm", field.getName());
    }

    // --- Location ---
    @Test
    void testGetAndSetLocation() {
        Location loc = new Location(36.8, 10.2,"address");
        field.setLocation(loc);
        assertEquals(loc, field.getLocation());
        assertEquals(36.8, field.getLocation().getLatitude());
        assertEquals(10.2, field.getLocation().getLongitude());
        assertEquals("address", field.getLocation().getAddress());
    }

    // --- Area ---
    @Test
    void testGetAndSetArea() {
        field.setArea(12.5);
        assertEquals(12.5, field.getArea());
    }

    // --- Current Crop ---
    @Test
    void testGetAndSetCurrentCrop() {
        field.setCurrentCrop("Wheat");
        assertEquals("Wheat", field.getCurrentCrop());
    }

    // --- Soil Type ---
    @Test
    void testGetAndSetSoilType() {
        field.setSoilType("Loamy");
        assertEquals("Loamy", field.getSoilType());
    }

    // --- Created At ---
    @Test
    void testGetAndSetCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        field.setCreatedAt(now);
        assertEquals(now, field.getCreatedAt());
    }

    // --- Full Object Consistency ---
    @Test
    void testFieldObjectState() {
        Location loc = new Location(36.8, 10.,"address");
        LocalDateTime now = LocalDateTime.now();

        field.setId("f-01");
        field.setUserId("u-01");
        field.setName("Olive Grove");
        field.setLocation(loc);
        field.setArea(4.5);
        field.setCurrentCrop("Olive Trees");
        field.setSoilType("Clay");
        field.setCreatedAt(now);

        assertAll(
                () -> assertEquals("f-01", field.getId()),
                () -> assertEquals("u-01", field.getUserId()),
                () -> assertEquals("Olive Grove", field.getName()),
                () -> assertEquals(loc, field.getLocation()),
                () -> assertEquals(4.5, field.getArea()),
                () -> assertEquals("Olive Trees", field.getCurrentCrop()),
                () -> assertEquals("Clay", field.getSoilType()),
                () -> assertEquals(now, field.getCreatedAt())
        );
    }
}
