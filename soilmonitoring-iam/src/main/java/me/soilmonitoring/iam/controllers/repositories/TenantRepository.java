package me.soilmonitoring.iam.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.iam.entities.Tenant;

import java.util.Optional;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {
    @Find
    Optional <Tenant> findByName (@By("name") String name);
}
