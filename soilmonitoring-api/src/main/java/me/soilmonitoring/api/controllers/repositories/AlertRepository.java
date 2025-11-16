package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.Alert;


import java.util.List;

//TODO: add test file

@Repository
public interface AlertRepository extends CrudRepository<Alert, String> {

    /**
     * Finds all alerts belonging to a specific user.
     *
     * @param userId the ID of the user whose alerts are being retrieved
     * @return a list of {@link Alert} objects associated with the specified user
     */
    @Find
    List<Alert> findByUserId(@By("userId") String userId);



    /**
     * Finds all alerts for a user filtered by their read status.
     *
     * @param userId the ID of the user whose alerts are being retrieved
     * @param isRead a boolean value indicating whether to retrieve read or unread alerts
     * @return a list of {@link Alert} objects matching the given criteria
     */
    @Find
    List<Alert> findByUserIdAndIsRead(
            @By("userId") String userId,
            @By("isRead") Boolean isRead
    );



    /**
     * Finds all alerts related to a specific field.
     *
     * This is useful when displaying alerts related to a given agricultural field
     * in the PWA’s prediction or monitoring dashboard.
     *
     *
     * @param fieldId the ID of the field for which alerts are being retrieved
     * @return a list of {@link Alert} objects associated with the specified field
     */
    @Find
    List<Alert> findByFieldId(@By("fieldId") String fieldId);
}
