package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.PredictionRepository;
import me.soilmonitoring.api.entities.Prediction;
import me.soilmonitoring.api.services.SageMakerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PredictionResource Tests")
class PredictionResourceTest {

    @Mock
    private Logger logger;

    @Mock
    private SoilMonitoringManager manager;

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private SageMakerService sageMakerService;

    @InjectMocks
    private PredictionResource predictionResource;

    private Prediction testPrediction;
    private List<Prediction> testPredictions;
    private Prediction.PredictionInput testInput;
    private Prediction.PredictionResult testResult;

    @BeforeEach
    void setUp() {
        // Setup test input
        testInput = new Prediction.PredictionInput();
        testInput.setNitrogen(50.0);
        testInput.setPhosphorus(40.0);
        testInput.setPotassium(35.0);
        testInput.setTemperature(25.5);
        testInput.setHumidity(65.0);
        testInput.setSoilMoisture(55.0);
        testInput.setpH(6.5);
        testInput.setRainfall(800.0);

        // Setup test result
        testResult = new Prediction.PredictionResult();
        testResult.setRecommendation("Wheat");
        testResult.setDosage("100kg/ha");
        testResult.setDetails(Arrays.asList("Detail 1", "Detail 2"));

        // Setup test prediction
        testPrediction = new Prediction();
        testPrediction.setId("pred-001");
        testPrediction.setFieldId("field-001");
        testPrediction.setPredictionType("crop");
        testPrediction.setModelUsed("XGBoost");
        testPrediction.setInputData(testInput);
        testPrediction.setResult(testResult);
        testPrediction.setConfidence(0.92);
        testPrediction.setCreatedAt(LocalDateTime.now());

        Prediction prediction2 = new Prediction();
        prediction2.setId("pred-002");
        prediction2.setFieldId("field-001");
        prediction2.setPredictionType("fertilizer");
        prediction2.setModelUsed("XGBoost");
        prediction2.setConfidence(0.88);
        prediction2.setCreatedAt(LocalDateTime.now());

        testPredictions = Arrays.asList(testPrediction, prediction2);
    }

    // ===== Tests pour getFieldPredictions =====

    @Test
    @DisplayName("Should get all predictions for a field without type filter")
    void testGetFieldPredictionsWithoutTypeFilter() {
        // Given
        String fieldId = "field-001";
        when(predictionRepository.findByFieldId(fieldId)).thenReturn(testPredictions);

        // When
        Response response = predictionResource.getFieldPredictions(fieldId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testPredictions, response.getEntity());
        verify(predictionRepository, times(1)).findByFieldId(fieldId);
        verify(manager, never()).getFieldPredictions(anyString(), anyString());
    }

    @Test
    @DisplayName("Should get filtered predictions by type")
    void testGetFieldPredictionsWithTypeFilter() {
        // Given
        String fieldId = "field-001";
        String predictionType = "crop";
        List<Prediction> cropPredictions = Arrays.asList(testPrediction);
        when(manager.getFieldPredictions(fieldId, predictionType)).thenReturn(cropPredictions);

        // When
        Response response = predictionResource.getFieldPredictions(fieldId, predictionType);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(cropPredictions, response.getEntity());
        verify(manager, times(1)).getFieldPredictions(fieldId, predictionType);
        verify(predictionRepository, never()).findByFieldId(anyString());
    }

    @Test
    @DisplayName("Should treat empty type as no filter")
    void testGetFieldPredictionsWithEmptyType() {
        // Given
        String fieldId = "field-001";
        when(predictionRepository.findByFieldId(fieldId)).thenReturn(testPredictions);

        // When
        Response response = predictionResource.getFieldPredictions(fieldId, "");

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        verify(predictionRepository, times(1)).findByFieldId(fieldId);
        verify(manager, never()).getFieldPredictions(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return empty list when field has no predictions")
    void testGetFieldPredictionsEmptyList() {
        // Given
        String fieldId = "field-999";
        when(predictionRepository.findByFieldId(fieldId)).thenReturn(Arrays.asList());

        // When
        Response response = predictionResource.getFieldPredictions(fieldId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<?> predictions = (List<?>) response.getEntity();
        assertTrue(predictions.isEmpty());
    }

    @Test
    @DisplayName("Should handle exception when getting field predictions")
    void testGetFieldPredictionsException() {
        // Given
        String fieldId = "field-001";
        when(predictionRepository.findByFieldId(fieldId)).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = predictionResource.getFieldPredictions(fieldId, null);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error retrieving predictions", response.getEntity());
        verify(logger, times(1)).severe(contains("Error getting field predictions"));
    }

    // ===== Tests pour getPredictionById =====

    @Test
    @DisplayName("Should get prediction by id successfully")
    void testGetPredictionByIdSuccess() {
        // Given
        String predictionId = "pred-001";
        when(predictionRepository.findById(predictionId)).thenReturn(Optional.of(testPrediction));

        // When
        Response response = predictionResource.getPredictionById(predictionId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        Prediction returnedPrediction = (Prediction) response.getEntity();
        assertEquals(testPrediction.getId(), returnedPrediction.getId());
        assertEquals(testPrediction.getPredictionType(), returnedPrediction.getPredictionType());
        verify(predictionRepository, times(1)).findById(predictionId);
    }

    @Test
    @DisplayName("Should return 404 when prediction not found")
    void testGetPredictionByIdNotFound() {
        // Given
        String predictionId = "non-existent";
        when(predictionRepository.findById(predictionId)).thenReturn(Optional.empty());

        // When
        Response response = predictionResource.getPredictionById(predictionId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Prediction not found", response.getEntity());
        verify(predictionRepository, times(1)).findById(predictionId);
    }

    @Test
    @DisplayName("Should handle exception when getting prediction by id")
    void testGetPredictionByIdException() {
        // Given
        String predictionId = "pred-001";
        when(predictionRepository.findById(predictionId)).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = predictionResource.getPredictionById(predictionId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error retrieving prediction", response.getEntity());
        verify(logger, times(1)).severe(contains("Error getting prediction"));
    }

    // ===== Tests pour createCropPrediction =====
    @Test
    @DisplayName("Should handle exception when creating crop prediction")
    void testCreateCropPredictionException() {
        // Given
        Prediction newPrediction = new Prediction();
        newPrediction.setFieldId("field-001");
        newPrediction.setInputData(testInput);

        when(sageMakerService.predictCrop(
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble()
        )).thenThrow(new RuntimeException("SageMaker error"));

        // When
        Response response = predictionResource.createCropPrediction(newPrediction);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error creating prediction", response.getEntity());
        verify(logger, times(1)).severe(contains("Error creating crop prediction"));
    }

    @Test
    @DisplayName("Should handle exception when creating fertilizer prediction")
    void testCreateFertilizerPredictionException() {
        // Given
        Prediction newPrediction = new Prediction();
        newPrediction.setFieldId("field-001");
        newPrediction.setInputData(testInput);

        when(sageMakerService.predictFertilizer(
                anyDouble(), anyDouble(), anyDouble(), anyString(),
                anyString(), anyInt(), anyInt(), anyInt()
        )).thenThrow(new RuntimeException("SageMaker error"));

        // When
        Response response = predictionResource.createFertilizerPrediction(newPrediction);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error creating prediction", response.getEntity());
        verify(logger, times(1)).severe(contains("Error creating fertilizer prediction"));
    }

    // ===== Tests d'intégration et cas limites =====

    @Test
    @DisplayName("Should handle multiple prediction types for same field")
    void testGetMultiplePredictionTypesForField() {
        // Given
        String fieldId = "field-001";
        when(predictionRepository.findByFieldId(fieldId)).thenReturn(testPredictions);

        // When
        Response response = predictionResource.getFieldPredictions(fieldId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Prediction> returnedPredictions = (List<Prediction>) response.getEntity();
        assertEquals(2, returnedPredictions.size());
    }
}