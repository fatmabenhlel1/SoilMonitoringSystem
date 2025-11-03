package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    void getVersion() {
        Tenant tenant = new Tenant();
        assertEquals(0L, tenant.getVersion());
    }

    @Test
    void setVersion() {
        Tenant tenant = new Tenant();
        tenant.setVersion(0L); // Initial version matches
        assertEquals(1L, tenant.getVersion());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> tenant.setVersion(2L));
        assertNotNull(exception);
    }

    @Test
    void getId() {
        Tenant tenant = new Tenant();
        tenant.setId("tenant123");
        assertEquals("tenant123", tenant.getId());
    }

    @Test
    void setId() {
        Tenant tenant = new Tenant();
        tenant.setId("tenant456");
        assertEquals("tenant456", tenant.getId());
    }

    @Test
    void getName() {
        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant");
        assertEquals("Test Tenant", tenant.getName());
    }

    @Test
    void setName() {
        Tenant tenant = new Tenant();
        tenant.setName("Another Tenant");
        assertEquals("Another Tenant", tenant.getName());
    }

    @Test
    void getSecret() {
        Tenant tenant = new Tenant();
        tenant.setSecret("secret123");
        assertEquals("secret123", tenant.getSecret());
    }

    @Test
    void setSecret() {
        Tenant tenant = new Tenant();
        tenant.setSecret("newSecret");
        assertEquals("newSecret", tenant.getSecret());
    }

    @Test
    void getRedirectUri() {
        Tenant tenant = new Tenant();
        tenant.setRedirectUri("http://example.com");
        assertEquals("http://example.com", tenant.getRedirectUri());
    }

    @Test
    void setRedirectUri() {
        Tenant tenant = new Tenant();
        tenant.setRedirectUri("http://newexample.com");
        assertEquals("http://newexample.com", tenant.getRedirectUri());
    }

    @Test
    void getAllowedRoles() {
        Tenant tenant = new Tenant();
        tenant.setAllowedRoles(5L);
        assertEquals(5L, tenant.getAllowedRoles());
    }

    @Test
    void setAllowedRoles() {
        Tenant tenant = new Tenant();
        tenant.setAllowedRoles(10L);
        assertEquals(10L, tenant.getAllowedRoles());
    }

    @Test
    void getRequiredScopes() {
        Tenant tenant = new Tenant();
        tenant.setRequiredScopes("read write");
        assertEquals("read write", tenant.getRequiredScopes());
    }

    @Test
    void setRequiredScopes() {
        Tenant tenant = new Tenant();
        tenant.setRequiredScopes("read");
        assertEquals("read", tenant.getRequiredScopes());
    }

    @Test
    void getSupportedGrantTypes() {
        Tenant tenant = new Tenant();
        tenant.setSupportedGrantTypes("authorization_code");
        assertEquals("authorization_code", tenant.getSupportedGrantTypes());
    }

    @Test
    void setSupportedGrantTypes() {
        Tenant tenant = new Tenant();
        tenant.setSupportedGrantTypes("implicit");
        assertEquals("implicit", tenant.getSupportedGrantTypes());
    }
}