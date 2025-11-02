package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.Sensor;

import java.util.List;
import java.util.Optional;

//TODO: add test file

@Repository
public interface SensorRepository extends CrudRepository<Sensor, String> {

    /**
     * Retrieves all sensors associated with a specific field.
     *
     * This is typically used to fetch all sensors deployed in a particular
     * agricultural field to display their readings or statuses in the PWA dashboard.
     *
     *
     * @param fieldId the unique identifier of the field
     * @return a list of {@link Sensor} objects linked to the specified field
     */
    @Find
    List<Sensor> findByFieldId(@By("fieldId") String fieldId);



    /**
     * Retrieves a sensor based on its unique device ID.
     *
     * This method is useful for checking whether a sensor device is already registered
     * in the system or when processing incoming data from IoT devices.
     *
     *
     * @param deviceId the unique hardware or network identifier of the sensor device
     * @return an {@link Optional} containing the {@link Sensor} if found, or empty if not found
     */
    @Find
    Optional<Sensor> findByDeviceId(@By("deviceId") String deviceId);



    /**
     * Retrieves all sensors that match a given operational status.
     *
     * The status may represent conditions such as "ACTIVE", "INACTIVE",
     * "FAULTY", or "DISCONNECTED", allowing system administrators or automated
     * services to monitor sensor health.
     *
     *
     * @param status the operational status of the sensors
     * @return a list of {@link Sensor} objects with the specified status
     */
    @Find
    List<Sensor> findByStatus(@By("status") String status);
}
