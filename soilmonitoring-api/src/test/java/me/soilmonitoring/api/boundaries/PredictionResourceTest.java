package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.PredictionRepository;
import me.soilmonitoring.api.entities.Prediction;
import me.soilmonitoring.api.entities.Prediction.PredictionInput;
import me.soilmonitoring.api.entities.Prediction.PredictionResult;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PredictionResourceTest {

    @Inject
    private PredictionResource predictionResource;

    @Inject
    private PredictionRepository predictionRepository;

    @Inject
    private SoilMonitoringManager manager;

    private static String testPredictionId;
    private static String testFieldId;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                // Entities
                .addPackage("me.soilmonitoring.api.entities")
                // Boundaries
                .addPackage("me.soilmonitoring.api.boundaries")
                // Controllers
                .addPackage("me.soilmonitoring.api.controllers.managers")
                .addPackage("me.soilmonitoring.api.controllers.repositories")
                // Resources nécessaires
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence.xml")
                .addAsResource("META-INF/microprofile-config.properties");
    }

    @BeforeEach
    public void setUp() {
        testFieldId = "field-" + UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup après chaque test
        if (testPredictionId != null) {
            try {
                predictionRepository.deleteById(testPredictionId);
            } catch (Exception e) {
                // Ignore si déjà supprimé
            }
            testPredictionId = null;
        }
    }

    // ============= Tests CREATE - Crop Predictions =============

    @Test
    @Order(1)
    @DisplayName("Should create crop prediction successfully")
    public void testCreateCropPrediction() {
        // Given
        Prediction prediction = createTestCropPrediction();

        // When
        Response response = predictionResource.createCropPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Prediction created = (Prediction) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("crop", created.getPredictionType());
        assertEquals("CatBoost", created.getModelUsed());
        assertEquals(testFieldId, created.getFieldId());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getInputData());

        testPredictionId = created.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should create crop prediction with complete input data")
    public void testCreateCropPredictionWithCompleteData() {
        // Given
        Prediction prediction = createTestCropPrediction();
        PredictionInput input = new PredictionInput();
        input.setNitrogen(45.0);
        input.setPhosphorus(38.0);
        input.setPotassium(42.0);
        input.setTemperature(25.5);
        input.setHumidity(65.0);
        input.setSoilMoisture(55.0);
        prediction.setInputData(input);

        prediction.setConfidence(0.92);

        // When
        Response response = predictionResource.createCropPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        assertEquals(45.0, created.getInputData().getNitrogen());
        assertEquals(38.0, created.getInputData().getPhosphorus());
        assertEquals(42.0, created.getInputData().getPotassium());
        assertEquals(0.92, created.getConfidence());
    }

    @Test
    @Order(3)
    @DisplayName("Should set correct model and type for crop prediction")
    public void testCropPredictionModelAndType() {
        // Given
        Prediction prediction = createTestCropPrediction();

        // When
        Response response = predictionResource.createCropPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        assertEquals("crop", created.getPredictionType());
        assertEquals("CatBoost", created.getModelUsed());
        assertNotNull(created.getCreatedAt());
        assertTrue(created.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @Order(4)
    @DisplayName("Should handle crop prediction with null result initially")
    public void testCropPredictionNullResult() {
        // Given
        Prediction prediction = createTestCropPrediction();
        prediction.setResult(null); // ML model pas encore appelé

        // When
        Response response = predictionResource.createCropPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        // Result peut être null car ML model sera appelé plus tard
        assertNotNull(created.getId());
    }

    // ============= Tests CREATE - Fertilizer Predictions =============

    @Test
    @Order(5)
    @DisplayName("Should create fertilizer prediction successfully")
    public void testCreateFertilizerPrediction() {
        // Given
        Prediction prediction = createTestFertilizerPrediction();

        // When
        Response response = predictionResource.createFertilizerPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Prediction created = (Prediction) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("fertilizer", created.getPredictionType());
        assertEquals("RandomForest", created.getModelUsed());
        assertEquals(testFieldId, created.getFieldId());
        assertNotNull(created.getCreatedAt());

        testPredictionId = created.getId();
    }

    @Test
    @Order(6)
    @DisplayName("Should create fertilizer prediction with soil and crop type")
    public void testCreateFertilizerPredictionWithTypes() {
        // Given
        Prediction prediction = createTestFertilizerPrediction();
        PredictionInput input = new PredictionInput();
        input.setNitrogen(30.0);
        input.setPhosphorus(25.0);
        input.setPotassium(35.0);
        input.setTemperature(28.0);
        input.setHumidity(70.0);
        input.setSoilMoisture(45.0);
        input.setSoilType("Clay");
        input.setCropType("Wheat");
        prediction.setInputData(input);

        prediction.setConfidence(0.88);

        // When
        Response response = predictionResource.createFertilizerPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        assertEquals("Clay", created.getInputData().getSoilType());
        assertEquals("Wheat", created.getInputData().getCropType());
        assertEquals(0.88, created.getConfidence());
    }

    @Test
    @Order(7)
    @DisplayName("Should set correct model and type for fertilizer prediction")
    public void testFertilizerPredictionModelAndType() {
        // Given
        Prediction prediction = createTestFertilizerPrediction();

        // When
        Response response = predictionResource.createFertilizerPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        assertEquals("fertilizer", created.getPredictionType());
        assertEquals("RandomForest", created.getModelUsed());
        assertNotNull(created.getCreatedAt());
    }

    @Test
    @Order(8)
    @DisplayName("Should create fertilizer prediction with result")
    public void testFertilizerPredictionWithResult() {
        // Given
        Prediction prediction = createTestFertilizerPrediction();

        PredictionResult result = new PredictionResult();
        result.setRecommendation("NPK 20-20-20");
        result.setDosage("50 kg/ha");
        result.setDetails(Arrays.asList("Apply in early morning", "Split application recommended"));
        prediction.setResult(result);

        // When
        Response response = predictionResource.createFertilizerPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        assertNotNull(created.getResult());
        assertEquals("NPK 20-20-20", created.getResult().getRecommendation());
        assertEquals("50 kg/ha", created.getResult().getDosage());
    }

    // ============= Tests READ by ID =============

    @Test
    @Order(9)
    @DisplayName("Should retrieve prediction by ID successfully")
    public void testGetPredictionById() {
        // Given - Create a prediction first
        Prediction prediction = createTestCropPrediction();
        Response createResponse = predictionResource.createCropPrediction(prediction);
        Prediction created = (Prediction) createResponse.getEntity();
        testPredictionId = created.getId();

        // When
        Response response = predictionResource.getPredictionById(testPredictionId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Prediction found = (Prediction) response.getEntity();
        assertEquals(testPredictionId, found.getId());
        assertEquals("crop", found.getPredictionType());
        assertEquals(testFieldId, found.getFieldId());
    }

    @Test
    @Order(10)
    @DisplayName("Should return 404 when prediction not found")
    public void testGetPredictionByIdNotFound() {
        // Given
        String nonExistentId = "non-existent-" + UUID.randomUUID();

        // When
        Response response = predictionResource.getPredictionById(nonExistentId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @Order(11)
    @DisplayName("Should retrieve fertilizer prediction by ID")
    public void testGetFertilizerPredictionById() {
        // Given
        Prediction prediction = createTestFertilizerPrediction();
        Response createResponse = predictionResource.createFertilizerPrediction(prediction);
        Prediction created = (Prediction) createResponse.getEntity();
        testPredictionId = created.getId();

        // When
        Response response = predictionResource.getPredictionById(testPredictionId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Prediction found = (Prediction) response.getEntity();

        assertEquals(testPredictionId, found.getId());
        assertEquals("fertilizer", found.getPredictionType());
        assertEquals("RandomForest", found.getModelUsed());
    }

    // ============= Tests READ by Field =============

    @Test
    @Order(12)
    @DisplayName("Should retrieve all predictions for a field without type filter")
    public void testGetFieldPredictionsWithoutType() {
        // Given - Create multiple predictions for the same field
        Prediction crop = createTestCropPrediction();
        Prediction fertilizer = createTestFertilizerPrediction();

        predictionResource.createCropPrediction(crop);
        Response fertResponse = predictionResource.createFertilizerPrediction(fertilizer);
        testPredictionId = ((Prediction) fertResponse.getEntity()).getId();

        // When
        Response response = predictionResource.getFieldPredictions(testFieldId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Prediction> predictions = (List<Prediction>) response.getEntity();

        assertTrue(predictions.size() >= 2);
        assertTrue(predictions.stream().anyMatch(p -> p.getPredictionType().equals("crop")));
        assertTrue(predictions.stream().anyMatch(p -> p.getPredictionType().equals("fertilizer")));
    }

    @Test
    @Order(13)
    @DisplayName("Should retrieve crop predictions only for a field")
    public void testGetFieldCropPredictions() {
        // Given
        Prediction crop1 = createTestCropPrediction();
        Prediction crop2 = createTestCropPrediction();
        Prediction fertilizer = createTestFertilizerPrediction();

        predictionResource.createCropPrediction(crop1);
        predictionResource.createCropPrediction(crop2);
        Response fertResponse = predictionResource.createFertilizerPrediction(fertilizer);
        testPredictionId = ((Prediction) fertResponse.getEntity()).getId();

        // When
        Response response = predictionResource.getFieldPredictions(testFieldId, "crop");

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Prediction> predictions = (List<Prediction>) response.getEntity();

        assertTrue(predictions.size() >= 2);
        assertTrue(predictions.stream().allMatch(p -> p.getPredictionType().equals("crop")));
    }

    @Test
    @Order(14)
    @DisplayName("Should retrieve fertilizer predictions only for a field")
    public void testGetFieldFertilizerPredictions() {
        // Given
        Prediction crop = createTestCropPrediction();
        Prediction fert1 = createTestFertilizerPrediction();
        Prediction fert2 = createTestFertilizerPrediction();

        predictionResource.createCropPrediction(crop);
        predictionResource.createFertilizerPrediction(fert1);
        Response fert2Response = predictionResource.createFertilizerPrediction(fert2);
        testPredictionId = ((Prediction) fert2Response.getEntity()).getId();

        // When
        Response response = predictionResource.getFieldPredictions(testFieldId, "fertilizer");

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Prediction> predictions = (List<Prediction>) response.getEntity();

        assertTrue(predictions.size() >= 2);
        assertTrue(predictions.stream().allMatch(p -> p.getPredictionType().equals("fertilizer")));
    }

    @Test
    @Order(15)
    @DisplayName("Should return empty list for field without predictions")
    public void testGetFieldPredictionsEmpty() {
        // Given
        String fieldWithNoPredictions = "field-empty-" + UUID.randomUUID();

        // When
        Response response = predictionResource.getFieldPredictions(fieldWithNoPredictions, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Prediction> predictions = (List<Prediction>) response.getEntity();
        assertTrue(predictions.isEmpty());
    }

    @Test
    @Order(16)
    @DisplayName("Should handle empty type parameter as null")
    public void testGetFieldPredictionsWithEmptyType() {
        // Given
        Prediction crop = createTestCropPrediction();
        Response createResponse = predictionResource.createCropPrediction(crop);
        testPredictionId = ((Prediction) createResponse.getEntity()).getId();

        // When - empty string should be treated like null
        Response response = predictionResource.getFieldPredictions(testFieldId, "");

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Prediction> predictions = (List<Prediction>) response.getEntity();
        assertTrue(predictions.size() >= 1);
    }

    // ============= Tests d'intégration complexes =============

    @Test
    @Order(17)
    @DisplayName("Should handle multiple predictions of different types for same field")
    public void testMultiplePredictionsForField() {
        // Given - Create various predictions
        for (int i = 0; i < 3; i++) {
            predictionResource.createCropPrediction(createTestCropPrediction());
        }
        for (int i = 0; i < 2; i++) {
            Response response = predictionResource.createFertilizerPrediction(createTestFertilizerPrediction());
            if (i == 1) {
                testPredictionId = ((Prediction) response.getEntity()).getId();
            }
        }

        // When
        Response allResponse = predictionResource.getFieldPredictions(testFieldId, null);
        Response cropResponse = predictionResource.getFieldPredictions(testFieldId, "crop");
        Response fertResponse = predictionResource.getFieldPredictions(testFieldId, "fertilizer");

        // Then
        @SuppressWarnings("unchecked")
        List<Prediction> allPredictions = (List<Prediction>) allResponse.getEntity();
        @SuppressWarnings("unchecked")
        List<Prediction> cropPredictions = (List<Prediction>) cropResponse.getEntity();
        @SuppressWarnings("unchecked")
        List<Prediction> fertPredictions = (List<Prediction>) fertResponse.getEntity();

        assertTrue(allPredictions.size() >= 5);
        assertTrue(cropPredictions.size() >= 3);
        assertTrue(fertPredictions.size() >= 2);
    }

    @Test
    @Order(18)
    @DisplayName("Should preserve all prediction data through save and retrieve")
    public void testDataPersistence() {
        // Given
        Prediction prediction = createTestFertilizerPrediction();

        PredictionInput input = new PredictionInput();
        input.setNitrogen(40.5);
        input.setPhosphorus(35.2);
        input.setPotassium(38.7);
        input.setTemperature(26.3);
        input.setHumidity(68.5);
        input.setSoilMoisture(52.1);
        input.setSoilType("Loamy");
        input.setCropType("Rice");
        prediction.setInputData(input);

        PredictionResult result = new PredictionResult();
        result.setRecommendation("Urea");
        result.setDosage("100 kg/ha");
        result.setDetails(Arrays.asList("Apply in 2 splits", "Morning application preferred"));
        prediction.setResult(result);

        prediction.setConfidence(0.91);

        // When - Create and retrieve
        Response createResponse = predictionResource.createFertilizerPrediction(prediction);
        testPredictionId = ((Prediction) createResponse.getEntity()).getId();

        Response getResponse = predictionResource.getPredictionById(testPredictionId);
        Prediction retrieved = (Prediction) getResponse.getEntity();

        // Then - Verify all data persisted
        assertEquals("fertilizer", retrieved.getPredictionType());
        assertEquals("RandomForest", retrieved.getModelUsed());
        assertEquals(testFieldId, retrieved.getFieldId());
        assertEquals(0.91, retrieved.getConfidence());

        assertNotNull(retrieved.getInputData());
        assertEquals(40.5, retrieved.getInputData().getNitrogen());
        assertEquals("Loamy", retrieved.getInputData().getSoilType());
        assertEquals("Rice", retrieved.getInputData().getCropType());

        assertNotNull(retrieved.getResult());
        assertEquals("Urea", retrieved.getResult().getRecommendation());
        assertEquals("100 kg/ha", retrieved.getResult().getDosage());
        assertEquals(2, retrieved.getResult().getDetails().size());
    }

    @Test
    @Order(19)
    @DisplayName("Should handle prediction with all optional fields null")
    public void testPredictionWithMinimalData() {
        // Given
        Prediction prediction = new Prediction();
        prediction.setFieldId(testFieldId);

        PredictionInput input = new PredictionInput();
        input.setNitrogen(30.0);
        input.setPhosphorus(25.0);
        input.setPotassium(35.0);
        prediction.setInputData(input);

        // Pas de result, pas de confidence, pas de soilType/cropType

        // When
        Response response = predictionResource.createCropPrediction(prediction);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Prediction created = (Prediction) response.getEntity();
        testPredictionId = created.getId();

        assertNotNull(created.getId());
        assertEquals("crop", created.getPredictionType());
        assertEquals("CatBoost", created.getModelUsed());
    }

    @Test
    @Order(20)
    @DisplayName("Should handle error gracefully when repository fails")
    public void testErrorHandling() {
        // Given - try with invalid data that might cause repository error
        Prediction prediction = new Prediction();
        // fieldId null - might cause error depending on repository implementation

        // When
        Response response = predictionResource.createCropPrediction(prediction);

        // Then - Should return error status (400 or 500)
        assertTrue(response.getStatus() >= 400);
    }

    // ============= Helper Methods =============

    private Prediction createTestCropPrediction() {
        Prediction prediction = new Prediction();
        prediction.setFieldId(testFieldId);

        PredictionInput input = new PredictionInput();
        input.setNitrogen(40.0);
        input.setPhosphorus(35.0);
        input.setPotassium(38.0);
        input.setTemperature(22.0);
        input.setHumidity(60.0);
        input.setSoilMoisture(50.0);
        prediction.setInputData(input);

        prediction.setConfidence(0.85);

        return prediction;
    }

    private Prediction createTestFertilizerPrediction() {
        Prediction prediction = new Prediction();
        prediction.setFieldId(testFieldId);

        PredictionInput input = new PredictionInput();
        input.setNitrogen(25.0);
        input.setPhosphorus(30.0);
        input.setPotassium(40.0);
        input.setTemperature(26.0);
        input.setHumidity(65.0);
        input.setSoilMoisture(48.0);
        input.setSoilType("Loamy");
        input.setCropType("Rice");
        prediction.setInputData(input);

        prediction.setConfidence(0.80);

        return prediction;
    }
}