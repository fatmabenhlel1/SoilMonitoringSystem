package me.soilmonitoring.iam.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.GrantPK;


import java.util.Optional;

@Repository
public interface GrantRepository extends CrudRepository<Grant, GrantPK> {

}
