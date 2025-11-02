package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.Prediction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PredictionRepository extends CrudRepository<Prediction, String> {

    /**
     * Finds all predictions associated with a specific field.
     *
     * @param fieldId the unique identifier of the field
     * @return a list of all predictions related to that field
     */
    @Find
    List<Prediction> findByFieldId(@By("fieldId") String fieldId);


    /**
     * Finds all predictions for a given field and a specific prediction type
     * (e.g., "crop" or "fertilizer").
     *
     * @param fieldId the field's unique identifier
     * @param predictionType the type of prediction
     * @return a list of predictions matching both field and type
     */
    @Find
    List<Prediction> findByFieldIdAndPredictionType(
            @By("fieldId") String fieldId,
            @By("predictionType") String predictionType
    );


    /**
     * Finds the latest prediction for a given field, based on creation timestamp.
     *
     * This is especially useful for real-time dashboards where the PWA needs
     * to show the most recent prediction result (e.g., next irrigation time or best crop).
     *
     * @param fieldId the field's unique identifier
     * @return the latest Prediction object if found
     */
    @Find
    Optional<Prediction> findTopByFieldIdOrderByTimestampDesc(@By("fieldId") String fieldId);


    /**
     * Finds all predictions for a specific field that were created after a certain timestamp.
     *
     * Useful for showing trends or prediction history (for example,
     * all predictions generated in the last week or month).
     *
     * @param fieldId the field's unique identifier
     * @param since the timestamp after which predictions should be retrieved
     * @return a list of recent predictions for that field
     */
    @Find
    List<Prediction> findByFieldIdAndTimestampAfter(
            @By("fieldId") String fieldId,
            @By("timestamp") LocalDateTime since
    );


    /**
     * Counts how many predictions exist for a specific field.
     *
     * Useful for analytics or usage statistics in your PWA (e.g.,
     * "X predictions generated for Field A this week").
     *
     * @param fieldId the field's unique identifier
     * @return the number of predictions for that field
     */
    long countByFieldId(@By("fieldId") String fieldId);
}
