package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    void setVersion_whenVersionMatches_incrementsVersion() {
        Tenant tenant = new Tenant();
        tenant.setVersion(0L); // Initial version matches
        assertEquals(1L, tenant.getVersion());
    }

    @Test
    void setVersion_whenVersionDoesNotMatch_throwsException() {
        Tenant tenant = new Tenant();
        tenant.setVersion(0L); // Initial version matches

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> tenant.setVersion(2L));
        assertNotNull(exception);
    }
}