
package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.Field;

import java.util.List;

//TODO: add test file

@Repository
public interface FieldRepository extends CrudRepository<Field, String> {
    /**
     * Find all fields for a user
     * @param userId
     * @return all fields for a user
     */
    @Find
    List<Field> findByUserId(@By("userId") String userId);
}