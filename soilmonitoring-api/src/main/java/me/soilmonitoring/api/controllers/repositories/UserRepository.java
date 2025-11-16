package me.soilmonitoring.api.controllers.repositories;

import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import me.soilmonitoring.api.entities.User;

import java.util.Optional;

//TODO: add test file

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    /**
     * Retrieves a user by their unique username.
     *
     * This method is commonly used during authentication or when checking
     * for duplicate usernames during registration.
     *
     *
     * @param username the username of the user
     * @return an {@link Optional} containing the {@link User} if found, or empty if not found
     */
    @Find
    Optional<User> findByUsername(@By("username") String username);

    /**
     * Retrieves a user by their unique email address.
     *
     * This method is useful for authentication, password recovery,
     * or checking if an email is already registered in the system.
     *
     *
     * @param email the email of the user
     * @return an {@link Optional} containing the {@link User} if found, or empty if not found
     */
    @Find
    Optional<User> findByEmail(@By("email") String email);
}
