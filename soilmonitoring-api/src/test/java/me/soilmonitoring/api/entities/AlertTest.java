package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;


class AlertTest {

    private Alert alert;

    @BeforeEach
    void setUp() {
        alert = new Alert();
    }



    @Test
    void testGetAndSetId() {
        alert.setId("alert-001");
        assertEquals("alert-001", alert.getId());
    }

    @Test
    void testInitialVersionIsZero() {
        assertEquals(0L, alert.getVersion());
    }

    @Test
    void testSetVersionThrowsIfDifferent() {
        // Version is initially 0, so setting to any other value should throw
        assertThrows(IllegalStateException.class, () -> alert.setVersion(1L));
    }

    @Test
    void testSetVersionIncrementsWhenEqual() {
        long initialVersion = alert.getVersion();
        alert.setVersion(initialVersion);
        assertEquals(initialVersion + 1, alert.getVersion());
    }

    @Test
    void testGetAndSetUserId() {
        alert.setUserId("user123");
        assertEquals("user123", alert.getUserId());
    }


    @Test
    void testGetAndSetFieldId() {
        alert.setFieldId("field-A");
        assertEquals("field-A", alert.getFieldId());
    }


    @Test
    void testGetAndSetAlertType() {
        alert.setAlertType("temperature");
        assertEquals("temperature", alert.getAlertType());
    }

    @Test
    void testGetAndSetSeverity() {
        alert.setSeverity("high");
        assertEquals("high", alert.getSeverity());
    }

    @Test
    void testGetAndSetMessage() {
        String message = "Temperature exceeded threshold";
        alert.setMessage(message);
        assertEquals(message, alert.getMessage());
    }

    @Test
    void testGetAndSetIsRead() {
        alert.setIsRead(true);
        assertTrue(alert.getIsRead());
        alert.setIsRead(false);
        assertFalse(alert.getIsRead());
    }

    @Test
    void testGetAndSetCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        alert.setCreatedAt(now);
        assertEquals(now, alert.getCreatedAt());
    }

    @Test
    void testAlertObjectState() {
        LocalDateTime now = LocalDateTime.now();
        alert.setId("alert-101");
        alert.setUserId("user-A");
        alert.setFieldId("field-12");
        alert.setAlertType("moisture");
        alert.setSeverity("medium");
        alert.setMessage("Moisture level low");
        alert.setIsRead(false);
        alert.setCreatedAt(now);

        assertAll(
                () -> assertEquals("alert-101", alert.getId()),
                () -> assertEquals("user-A", alert.getUserId()),
                () -> assertEquals("field-12", alert.getFieldId()),
                () -> assertEquals("moisture", alert.getAlertType()),
                () -> assertEquals("medium", alert.getSeverity()),
                () -> assertEquals("Moisture level low", alert.getMessage()),
                () -> assertFalse(alert.getIsRead()),
                () -> assertEquals(now, alert.getCreatedAt())
        );
    }




}