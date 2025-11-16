package me.soilmonitoring.api.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        LocalDateTime createdAt = LocalDateTime.of(2025, 11, 1, 8, 0);

        user.setId("u1");
        user.setUsername("farmer01");
        user.setEmail("farmer01@example.com");
        user.setFullName("John Doe");
        user.setRole("farmer");
        user.setCreatedAt(createdAt);

        assertEquals("u1", user.getId());
        assertEquals("farmer01", user.getUsername());
        assertEquals("farmer01@example.com", user.getEmail());
        assertEquals("John Doe", user.getFullName());
        assertEquals("farmer", user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testVersionIncrementAndValidation() {
        User user = new User();
        assertEquals(0L, user.getVersion());

        // Valid version increment
        user.setVersion(0L);
        assertEquals(1L, user.getVersion());

        // Attempt to set version again with mismatch should throw
        assertThrows(IllegalStateException.class, () -> user.setVersion(0L));
    }

    @Test
    void testMutability() {
        User user = new User();
        user.setUsername("user123");
        assertEquals("user123", user.getUsername());

        user.setUsername("newUser");
        assertEquals("newUser", user.getUsername());
    }
}
