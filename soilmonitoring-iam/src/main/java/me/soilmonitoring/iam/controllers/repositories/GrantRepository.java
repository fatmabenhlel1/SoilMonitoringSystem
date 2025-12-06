// src/main/java/me/soilmonitoring/iam/controllers/repositories/GrantRepository.java
package me.soilmonitoring.iam.controllers.repositories;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.GrantPK;

@Repository
public interface GrantRepository extends CrudRepository<Grant, GrantPK> {
}