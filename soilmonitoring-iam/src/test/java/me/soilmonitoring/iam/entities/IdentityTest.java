package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentityTest {

    @Test
    void setVersion_whenVersionMatches_incrementsVersion() {
        Identity identity = new Identity();
        identity.setVersion(0L); // Initial version matches
        assertEquals(1L, identity.getVersion());
    }

    @Test
    void setVersion_whenVersionDoesNotMatch_throwsException() {
        Identity identity = new Identity();
        identity.setVersion(0L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> identity.setVersion(2L));
        assertNotNull(exception);
    }

    @Test
    void getName_returnsUsername() {
        Identity identity = new Identity();
        identity.setUsername("john.doe");

        assertEquals("john.doe", identity.getName());
    }
}