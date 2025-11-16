package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PredictionTest {

    @Test
    void testDefaultValuesAndSetters() {
        Prediction prediction = new Prediction();

        prediction.setId("p1");
        prediction.setFieldId("f1");
        prediction.setPredictionType("fertilizer");
        prediction.setModelUsed("RandomForest");
        prediction.setConfidence(0.92);
        prediction.setCreatedAt(LocalDateTime.of(2025, 11, 1, 10, 0));

        assertEquals("p1", prediction.getId());
        assertEquals("f1", prediction.getFieldId());
        assertEquals("fertilizer", prediction.getPredictionType());
        assertEquals("RandomForest", prediction.getModelUsed());
        assertEquals(0.92, prediction.getConfidence());
        assertEquals(LocalDateTime.of(2025, 11, 1, 10, 0), prediction.getCreatedAt());
    }

    @Test
    void testVersionIncrementAndValidation() {
        Prediction prediction = new Prediction();
        assertEquals(0L, prediction.getVersion());

        // first increment with correct version
        prediction.setVersion(0L);
        assertEquals(1L, prediction.getVersion());

        // attempt with mismatched version should throw
        assertThrows(IllegalStateException.class, () -> prediction.setVersion(0L));
    }

    @Test
    void testPredictionInputEmbeddable() {
        Prediction.PredictionInput input = new Prediction.PredictionInput();
        input.setNitrogen(10.5);
        input.setPhosphorus(4.2);
        input.setPotassium(6.7);
        input.setTemperature(25.0);
        input.setHumidity(70.0);
        input.setSoilMoisture(30.0);
        input.setSoilType("clay");
        input.setCropType("wheat");

        assertEquals(10.5, input.getNitrogen());
        assertEquals(4.2, input.getPhosphorus());
        assertEquals(6.7, input.getPotassium());
        assertEquals(25.0, input.getTemperature());
        assertEquals(70.0, input.getHumidity());
        assertEquals(30.0, input.getSoilMoisture());
        assertEquals("clay", input.getSoilType());
        assertEquals("wheat", input.getCropType());
    }

    @Test
    void testPredictionResultEmbeddable() {
        Prediction.PredictionResult result = new Prediction.PredictionResult();
        result.setRecommendation("Urea");
        result.setDosage("50kg/ha");
        result.setDetails(List.of("High nitrogen deficiency detected", "Optimal weather conditions"));

        assertEquals("Urea", result.getRecommendation());
        assertEquals("50kg/ha", result.getDosage());
        assertEquals(2, result.getDetails().size());
        assertTrue(result.getDetails().contains("High nitrogen deficiency detected"));
    }

    @Test
    void testCompletePredictionWithEmbeddables() {
        Prediction.PredictionInput input = new Prediction.PredictionInput();
        input.setNitrogen(5.5);
        input.setPhosphorus(2.3);

        Prediction.PredictionResult result = new Prediction.PredictionResult();
        result.setRecommendation("Corn");
        result.setDetails(List.of("Suitable soil type"));

        Prediction prediction = new Prediction();
        prediction.setId("p2");
        prediction.setFieldId("f2");
        prediction.setInputData(input);
        prediction.setResult(result);

        assertEquals("Corn", prediction.getResult().getRecommendation());
        assertEquals(5.5, prediction.getInputData().getNitrogen());
        assertEquals("f2", prediction.getFieldId());
    }
}
