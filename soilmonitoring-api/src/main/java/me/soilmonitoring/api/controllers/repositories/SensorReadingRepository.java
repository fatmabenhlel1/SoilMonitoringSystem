package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.SensorReading;

import java.time.LocalDateTime;
import java.util.List;

//TODO: add test file


@Repository
public interface SensorReadingRepository extends CrudRepository<SensorReading, String> {

    /**
     * Retrieves all sensor readings associated with a specific field.
     *
     * This is typically used to display aggregated or historical data for
     * all sensors in a given agricultural field.
     *
     *
     * @param fieldId the unique identifier of the field
     * @return a list of {@link SensorReading} objects linked to the specified field
     */
    @Find
    List<SensorReading> findByFieldId(@By("fieldId") String fieldId);

    /**
     * Retrieves all readings collected from a specific sensor.
     *
     * This allows the system to analyze or visualize time-series data
     * for a single device, such as temperature, moisture, or pH readings.
     *
     *
     * @param sensorId the unique identifier of the sensor
     * @return a list of {@link SensorReading} objects recorded by the specified sensor
     */
    @Find
    List<SensorReading> findBySensorId(@By("sensorId") String sensorId);

    /**
     * Retrieves all readings for a specific field within a given time range.
     *
     * This query is useful for time-based analysis — for example, generating
     * charts or predictions based on readings collected between two dates.
     *
     *
     * @param fieldId the unique identifier of the field
     * @param from    the start of the time interval
     * @param to      the end of the time interval
     * @return a list of {@link SensorReading} objects collected during the specified period
     */
    @Find
    List<SensorReading> findByFieldIdAndTimestampBetween(
            @By("fieldId") String fieldId,
            @By("timestamp") LocalDateTime from,
            @By("timestamp") LocalDateTime to
    );
}
