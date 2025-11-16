package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TreatmentTest {

    @Test
    void testGettersAndSetters() {
        Treatment treatment = new Treatment();
        LocalDateTime appliedAt = LocalDateTime.of(2025, 11, 1, 9, 0);

        treatment.setId("t1");
        treatment.setFieldId("f1");
        treatment.setTreatmentType("fertilizer");
        treatment.setProductName("Urea");
        treatment.setQuantity("50kg");
        treatment.setAppliedAt(appliedAt);
        treatment.setAppliedBy("user123");
        treatment.setNotes("Applied evenly across the field");

        assertEquals("t1", treatment.getId());
        assertEquals("f1", treatment.getFieldId());
        assertEquals("fertilizer", treatment.getTreatmentType());
        assertEquals("Urea", treatment.getProductName());
        assertEquals("50kg", treatment.getQuantity());
        assertEquals(appliedAt, treatment.getAppliedAt());
        assertEquals("user123", treatment.getAppliedBy());
        assertEquals("Applied evenly across the field", treatment.getNotes());
    }

    @Test
    void testVersionIncrementAndValidation() {
        Treatment treatment = new Treatment();
        assertEquals(0L, treatment.getVersion());

        // Valid version increment
        treatment.setVersion(0L);
        assertEquals(1L, treatment.getVersion());

        // Attempt to set version again with mismatch should throw
        assertThrows(IllegalStateException.class, () -> treatment.setVersion(0L));
    }

    @Test
    void testMutability() {
        Treatment treatment = new Treatment();
        treatment.setProductName("NPK");
        assertEquals("NPK", treatment.getProductName());

        treatment.setProductName("Potash");
        assertEquals("Potash", treatment.getProductName());
    }
}
