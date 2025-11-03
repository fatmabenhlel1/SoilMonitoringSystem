package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GrantTest {

    @Test
    void getId() {
        Grant grant = new Grant();
        GrantPK id = new GrantPK();
        grant.setId(id);
        assertEquals(id, grant.getId());
    }

    @Test
    void setId() {
        Grant grant = new Grant();
        GrantPK id = new GrantPK();
        grant.setId(id);
        assertEquals(id, grant.getId());
    }

    @Test
    void getVersion() {
        Grant grant = new Grant();
        assertEquals(0L, grant.getVersion());
    }

    @Test
    void setVersion() {
        Grant grant = new Grant();
        grant.setVersion(0L); // Initial version matches
        assertEquals(1L, grant.getVersion());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> grant.setVersion(2L));
        assertNotNull(exception);
    }

    @Test
    void getTenant() {
        Grant grant = new Grant();
        Tenant tenant = new Tenant();
        grant.setTenant(tenant);
        assertEquals(tenant, grant.getTenant());
    }

    @Test
    void setTenant() {
        Grant grant = new Grant();
        Tenant tenant = new Tenant();
        grant.setTenant(tenant);
        assertEquals(tenant, grant.getTenant());
    }

    @Test
    void getIdentity() {
        Grant grant = new Grant();
        Identity identity = new Identity();
        grant.setIdentity(identity);
        assertEquals(identity, grant.getIdentity());
    }

    @Test
    void setIdentity() {
        Grant grant = new Grant();
        Identity identity = new Identity();
        grant.setIdentity(identity);
        assertEquals(identity, grant.getIdentity());
    }

    @Test
    void getApprovedScopes() {
        Grant grant = new Grant();
        String scopes = "read write";
        grant.setApprovedScopes(scopes);
        assertEquals(scopes, grant.getApprovedScopes());
    }

    @Test
    void setApprovedScopes() {
        Grant grant = new Grant();
        String scopes = "read write";
        grant.setApprovedScopes(scopes);
        assertEquals(scopes, grant.getApprovedScopes());
    }

    @Test
    void getIssuanceDateTime() {
        Grant grant = new Grant();
        LocalDateTime dateTime = LocalDateTime.now();
        grant.setIssuanceDateTime(dateTime);
        assertEquals(dateTime, grant.getIssuanceDateTime());
    }

    @Test
    void setIssuanceDateTime() {
        Grant grant = new Grant();
        LocalDateTime dateTime = LocalDateTime.now();
        grant.setIssuanceDateTime(dateTime);
        assertEquals(dateTime, grant.getIssuanceDateTime());
    }
}