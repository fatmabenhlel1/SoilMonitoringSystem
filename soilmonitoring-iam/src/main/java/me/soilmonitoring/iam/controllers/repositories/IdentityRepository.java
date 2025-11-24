package me.soilmonitoring.iam.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.iam.entities.Identity;


import java.util.Optional;


@Repository
public interface IdentityRepository extends CrudRepository<Identity, String> {
    @Find
    Optional<Identity> findByUsername (@By("username") String username);
    Optional<Identity> findByEmail(String email);
}


