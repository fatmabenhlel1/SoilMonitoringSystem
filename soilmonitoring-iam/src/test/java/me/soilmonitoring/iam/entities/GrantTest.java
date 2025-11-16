package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GrantTest {

    // Test to verify that the ID can be set and retrieved correctly
    @Test
    void testSetAndGetId() {
        Grant grant = new Grant();
        GrantPK id = new GrantPK();
        grant.setId(id);
        assertEquals(id, grant.getId());
    }

    // Test to verify the version is incremented correctly when it matches the current version
    @Test
    void testSetVersion_whenVersionMatches_incrementsVersion() {
        Grant grant = new Grant();
        grant.setVersion(0L); // Initial version matches
        assertEquals(1L, grant.getVersion());
    }

    // Test to verify an exception is thrown when the version does not match the current version
    @Test
    void testSetVersion_whenVersionDoesNotMatch_throwsException() {
        Grant grant = new Grant();
        grant.setVersion(0L); // Initial version matches

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> grant.setVersion(2L));
        assertNotNull(exception);
    }

    // Test to verify that the tenant can be set and retrieved correctly
    @Test
    void testSetAndGetTenant() {
        Grant grant = new Grant();
        Tenant tenant = new Tenant();
        grant.setTenant(tenant);
        assertEquals(tenant, grant.getTenant());
    }

    // Test to verify that the identity can be set and retrieved correctly
    @Test
    void testSetAndGetIdentity() {
        Grant grant = new Grant();
        Identity identity = new Identity();
        grant.setIdentity(identity);
        assertEquals(identity, grant.getIdentity());
    }

    // Test to verify that the approved scopes can be set and retrieved correctly
    @Test
    void testSetAndGetApprovedScopes() {
        Grant grant = new Grant();
        String scopes = "read write";
        grant.setApprovedScopes(scopes);
        assertEquals(scopes, grant.getApprovedScopes());
    }

    // Test to verify that the issuance date and time can be set and retrieved correctly
    @Test
    void testSetAndGetIssuanceDateTime() {
        Grant grant = new Grant();
        LocalDateTime dateTime = LocalDateTime.now();
        grant.setIssuanceDateTime(dateTime);
        assertEquals(dateTime, grant.getIssuanceDateTime());
    }
}