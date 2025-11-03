package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityTest {

    @Test
    void getId() {
        Identity identity = new Identity();
        identity.setId("identity123");
        assertEquals("identity123", identity.getId());
    }

    @Test
    void setId() {
        Identity identity = new Identity();
        identity.setId("identity456");
        assertEquals("identity456", identity.getId());
    }

    @Test
    void getVersion() {
        Identity identity = new Identity();
        assertEquals(0L, identity.getVersion());
    }

    @Test
    void setVersion() {
        Identity identity = new Identity();
        identity.setVersion(0L); // Initial version matches
        assertEquals(1L, identity.getVersion());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> identity.setVersion(2L));
        assertNotNull(exception);
    }

    @Test
    void getUsername() {
        Identity identity = new Identity();
        identity.setUsername("testUser");
        assertEquals("testUser", identity.getUsername());
    }

    @Test
    void setUsername() {
        Identity identity = new Identity();
        identity.setUsername("newUser");
        assertEquals("newUser", identity.getUsername());
    }

    @Test
    void getName() {
        Identity identity = new Identity();
        identity.setUsername("principalUser");
        assertEquals("principalUser", identity.getName());
    }

    @Test
    void getPassword() {
        Identity identity = new Identity();
        identity.setPassword("password123");
        assertEquals("password123", identity.getPassword());
    }

    @Test
    void setPassword() {
        Identity identity = new Identity();
        identity.setPassword("newPassword");
        assertEquals("newPassword", identity.getPassword());
    }

    @Test
    void getRoles() {
        Identity identity = new Identity();
        identity.setRoles(5L);
        assertEquals(5L, identity.getRoles());
    }

    @Test
    void setRoles() {
        Identity identity = new Identity();
        identity.setRoles(10L);
        assertEquals(10L, identity.getRoles());
    }

    @Test
    void getProvidedScopes() {
        Identity identity = new Identity();
        identity.setProvidedScopes("read write");
        assertEquals("read write", identity.getProvidedScopes());
    }

    @Test
    void setProvidedScopes() {
        Identity identity = new Identity();
        identity.setProvidedScopes("read");
        assertEquals("read", identity.getProvidedScopes());
    }
}