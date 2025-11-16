package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.Treatment;

import java.util.List;

//TODO: add test file

@Repository
public interface TreatmentRepository extends CrudRepository<Treatment, String> {

    /**
     * Retrieves all treatments applied to a specific field.
     *
     * This method allows the platform to display the complete treatment history
     * for a given field, which can be useful for monitoring crop care and
     * evaluating productivity over time.
     *
     *
     * @param fieldId the unique identifier of the field
     * @return a list of {@link Treatment} entities associated with the specified field
     */
    @Find
    List<Treatment> findByFieldId(@By("fieldId") String fieldId);

    /**
     * Retrieves treatments of a specific type applied to a particular field.
     *
     * This query is useful when filtering treatment records, for instance,
     * to show only irrigation or fertilization actions within a certain field.
     *
     *
     * @param fieldId        the unique identifier of the field
     * @param treatmentType  the type of treatment (e.g., "irrigation", "fertilization")
     * @return a list of {@link Treatment} entities matching the specified criteria
     */
    @Find
    List<Treatment> findByFieldIdAndTreatmentType(
            @By("fieldId") String fieldId,
            @By("treatmentType") String treatmentType
    );
}
