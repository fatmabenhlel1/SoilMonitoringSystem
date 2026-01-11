package me.soilmonitoring.api.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.PredictionRepository;
import me.soilmonitoring.api.entities.Prediction;
import me.soilmonitoring.api.security.Secured;
import me.soilmonitoring.api.services.SageMakerService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Path("/predictions")
@Secured
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PredictionResource {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

    @Inject
    private PredictionRepository predictionRepository;

    @Inject
    private SageMakerService sageMakerService;

    @GET
    @Path("/field/{fieldId}")
    public Response getFieldPredictions(@PathParam("fieldId") String fieldId,
                                        @QueryParam("type") String predictionType) {
        try {
            List<Prediction> predictions;
            if (predictionType != null && !predictionType.isEmpty()) {
                predictions = manager.getFieldPredictions(fieldId, predictionType);
            } else {
                predictions = predictionRepository.findByFieldId(fieldId);
            }
            return Response.ok(predictions).build();
        } catch (Exception e) {
            logger.severe("Error getting field predictions: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving predictions").build();
        }
    }

    @GET
    @Path("/{predictionId}")
    public Response getPredictionById(@PathParam("predictionId") String predictionId) {
        try {
            Prediction prediction = predictionRepository.findById(predictionId)
                    .orElseThrow(IllegalArgumentException::new);
            return Response.ok(prediction).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Prediction not found").build();
        } catch (Exception e) {
            logger.severe("Error getting prediction: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving prediction").build();
        }
    }

    @POST
    @Path("/crop")
    public Response createCropPrediction(Prediction prediction) {
        try {
            prediction.setId(UUID.randomUUID().toString());
            prediction.setPredictionType("crop");
            prediction.setModelUsed("XGBoost");
            prediction.setCreatedAt(LocalDateTime.now());

            // Call SageMaker ML model
            Prediction.PredictionInput input = prediction.getInputData();
            SageMakerService.CropPrediction sageMakerResult = sageMakerService.predictCrop(
                    input.getNitrogen(),
                    input.getPhosphorus(),
                    input.getPotassium(),
                    input.getTemperature(),
                    input.getHumidity(),
                    input.getpH(), // This should be pH
                    input.getRainfall() != null ? input.getRainfall() : 0.0
            );

            // Set result
            Prediction.PredictionResult result = new Prediction.PredictionResult();
            result.setRecommendation(sageMakerResult.cropName);
            result.setDetails(Arrays.asList(
                    "Optimal conditions detected for " + sageMakerResult.cropName,
                    String.format("Confidence: %.1f%%", sageMakerResult.confidence * 100)
            ));


            prediction.setResult(result);
            prediction.setConfidence(sageMakerResult.confidence);

            Prediction savedPrediction = predictionRepository.save(prediction);
            logger.info("Crop prediction created: " + savedPrediction.getId());
            return Response.status(Response.Status.CREATED).entity(savedPrediction).build();
        } catch (Exception e) {
            logger.severe("Error creating crop prediction: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating prediction").build();
        }
    }

    @POST
    @Path("/fertilizer")
    public Response createFertilizerPrediction(Prediction prediction) {
        try {
            prediction.setId(UUID.randomUUID().toString());
            prediction.setPredictionType("fertilizer");
            prediction.setModelUsed("XGBoost");
            prediction.setCreatedAt(LocalDateTime.now());

            // Call SageMaker ML model
            Prediction.PredictionInput input = prediction.getInputData();
            SageMakerService.FertilizerPrediction sageMakerResult = sageMakerService.predictFertilizer(
                    input.getTemperature(),
                    input.getHumidity(),
                    input.getSoilMoisture(),
                    input.getSoilType(),
                    input.getCropType(),
                    input.getNitrogen().intValue(),
                    input.getPotassium().intValue(),
                    input.getPhosphorus().intValue()
            );

            // Set result
            Prediction.PredictionResult result = new Prediction.PredictionResult();
            result.setRecommendation(sageMakerResult.fertilizerType);
            result.setDosage(sageMakerResult.dosage);
            result.setDetails(Arrays.asList(
                    "Recommended: " + sageMakerResult.fertilizerType,
                    "Application rate: " + sageMakerResult.dosage,
                    String.format("Confidence: %.1f%%", sageMakerResult.confidence * 100)
            ));

            prediction.setResult(result);
            prediction.setConfidence(sageMakerResult.confidence);

            Prediction savedPrediction = predictionRepository.save(prediction);
            logger.info("Fertilizer prediction created: " + savedPrediction.getId());
            return Response.status(Response.Status.CREATED).entity(savedPrediction).build();
        } catch (Exception e) {
            logger.severe("Error creating fertilizer prediction: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating prediction").build();
        }
    }
}