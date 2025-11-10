package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.PredictionRepository;
import me.soilmonitoring.api.entities.Prediction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class PredictionResourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(PredictionResource.class.getPackage())
                .addPackage(SoilMonitoringManager.class.getPackage())
                .addPackage(PredictionRepository.class.getPackage())
                .addPackage(Prediction.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private PredictionResource predictionResource;

    @Inject
    private PredictionRepository predictionRepository;

    @Test
    public void testCreateCropPrediction() {
        Prediction prediction = new Prediction();
        prediction.setFieldId("field123");
        prediction.setInputData("humidity=30,temp=22,NPK=low");

        Response response = predictionResource.createCropPrediction(prediction);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Prediction created = (Prediction) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("crop", created.getPredictionType());
        assertEquals("CatBoost", created.getModelUsed());
    }

    @Test
    public void testCreateFertilizerPrediction() {
        Prediction prediction = new Prediction();
        prediction.setFieldId("field456");
        prediction.setInputData("humidity=15,temp=35,NPK=medium");

        Response response = predictionResource.createFertilizerPrediction(prediction);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Prediction created = (Prediction) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("fertilizer", created.getPredictionType());
        assertEquals("RandomForest", created.getModelUsed());
    }

    @Test
    public void testGetPredictionById() {
        Prediction prediction = new Prediction();
        prediction.setId(UUID.randomUUID().toString());
        prediction.setFieldId("field789");
        prediction.setPredictionType("crop");
        prediction.setModelUsed("CatBoost");
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setInputData("humidity=40,temp=20,NPK=high");
        predictionRepository.save(prediction);

        Response response = predictionResource.getPredictionById(prediction.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Prediction found = (Prediction) response.getEntity();
        assertEquals(prediction.getId(), found.getId());
    }

    @Test
    public void testGetFieldPredictionsWithoutType() {
        Prediction prediction = new Prediction();
        prediction.setId(UUID.randomUUID().toString());
        prediction.setFieldId("fieldABC");
        prediction.setPredictionType("fertilizer");
        prediction.setModelUsed("RandomForest");
        prediction.setCreatedAt(LocalDateTime.now());
        prediction.setInputData("humidity=10,temp=30,NPK=low");
        predictionRepository.save(prediction);

        Response response = predictionResource.getFieldPredictions("fieldABC", null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        List<Prediction> predictions = (List<Prediction>) response.getEntity();
        assertFalse(predictions.isEmpty());
    }
}
