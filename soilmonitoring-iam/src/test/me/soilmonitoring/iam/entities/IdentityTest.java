package me.soilmonitoring.iam.entities;

import me.soilmonitoring.iam.security.Argon2Utility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityTest {

    @Test
    void getName_returnsUsername() {
        Identity identity = new Identity();
        identity.setUsername("john.doe");

        assertEquals("john.doe", identity.getName());
    }

    @Test
    void hashPassword_correctlyHashesPassword() {
        Identity identity = new Identity();
        Argon2Utility argon2Utility = new Argon2Utility();

        String plainPassword = "password123";
        identity.hashPassword(plainPassword, argon2Utility);

        assertNotEquals(plainPassword, identity.getPassword());
        assertTrue(argon2Utility.verify(plainPassword.toCharArray(), identity.getPassword()));
    }

    @Test
    void defaultConstructor_initializesFieldsCorrectly() {
        Identity identity = new Identity();

        assertNotNull(identity.getId());
        assertFalse(identity.isAccountActivated());
    }

    @Test
    void parameterizedConstructor_initializesFieldsCorrectly() {
        String id = "12345";
        String username = "john.doe";
        String password = "password123";
        String creationDate = "2023-10-01";
        Long roles = 1L;
        boolean isAccountActivated = true;

        Identity identity = new Identity(id, username, password, creationDate, roles, isAccountActivated);

        assertEquals(id, identity.getId());
        assertEquals(username, identity.getUsername());
        assertEquals(password, identity.getPassword());
        assertEquals(creationDate, identity.getCreationDate());
        assertEquals(roles, identity.getRoles());
        assertTrue(identity.isAccountActivated());
    }
}