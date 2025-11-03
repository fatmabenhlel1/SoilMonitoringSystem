package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrantPKTest {

    @Test
    void getTenantId() {
        GrantPK grantPK = new GrantPK();
        grantPK.setTenantId("tenant123");
        assertEquals("tenant123", grantPK.getTenantId());
    }

    @Test
    void setTenantId() {
        GrantPK grantPK = new GrantPK();
        grantPK.setTenantId("tenant456");
        assertEquals("tenant456", grantPK.getTenantId());
    }

    @Test
    void getIdentityId() {
        GrantPK grantPK = new GrantPK();
        grantPK.setIdentityId("identity123");
        assertEquals("identity123", grantPK.getIdentityId());
    }

    @Test
    void setIdentityId() {
        GrantPK grantPK = new GrantPK();
        grantPK.setIdentityId("identity456");
        assertEquals("identity456", grantPK.getIdentityId());
    }

    @Test
    void testEquals() {
        GrantPK grantPK1 = new GrantPK();
        grantPK1.setTenantId("tenant123");
        grantPK1.setIdentityId("identity123");

        GrantPK grantPK2 = new GrantPK();
        grantPK2.setTenantId("tenant123");
        grantPK2.setIdentityId("identity123");

        GrantPK grantPK3 = new GrantPK();
        grantPK3.setTenantId("tenant456");
        grantPK3.setIdentityId("identity456");

        assertEquals(grantPK1, grantPK2);
        assertNotEquals(grantPK1, grantPK3);
    }

    @Test
    void testHashCode() {
        GrantPK grantPK1 = new GrantPK();
        grantPK1.setTenantId("tenant123");
        grantPK1.setIdentityId("identity123");

        GrantPK grantPK2 = new GrantPK();
        grantPK2.setTenantId("tenant123");
        grantPK2.setIdentityId("identity123");

        GrantPK grantPK3 = new GrantPK();
        grantPK3.setTenantId("tenant456");
        grantPK3.setIdentityId("identity456");

        assertEquals(grantPK1.hashCode(), grantPK2.hashCode());
        assertNotEquals(grantPK1.hashCode(), grantPK3.hashCode());
    }
}