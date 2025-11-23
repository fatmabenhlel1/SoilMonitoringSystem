package me.soilmonitoring.iam.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GrantPKTest {

    // Test to verify that two GrantPK objects with the same tenantId and identityId are considered equal
    @Test
    void testEquals_whenObjectsAreEqual_returnsTrue() {
        GrantPK grantPK1 = new GrantPK();
        grantPK1.setTenantId("tenant123");
        grantPK1.setIdentityId("identity123");

        GrantPK grantPK2 = new GrantPK();
        grantPK2.setTenantId("tenant123");
        grantPK2.setIdentityId("identity123");

        // Assert that the two objects are equal
        assertEquals(grantPK1, grantPK2);
    }

    // Test to verify that two GrantPK objects with different tenantId or identityId are not considered equal
    @Test
    void testEquals_whenObjectsAreNotEqual_returnsFalse() {
        GrantPK grantPK1 = new GrantPK();
        grantPK1.setTenantId("tenant123");
        grantPK1.setIdentityId("identity123");

        GrantPK grantPK2 = new GrantPK();
        grantPK2.setTenantId("tenant456");
        grantPK2.setIdentityId("identity456");

        // Assert that the two objects are not equal
        assertNotEquals(grantPK1, grantPK2);
    }

    // Test to verify that two GrantPK objects with the same tenantId and identityId have the same hash code
    @Test
    void testHashCode_whenObjectsAreEqual_returnsSameHashCode() {
        GrantPK grantPK1 = new GrantPK();
        grantPK1.setTenantId("tenant123");
        grantPK1.setIdentityId("identity123");

        GrantPK grantPK2 = new GrantPK();
        grantPK2.setTenantId("tenant123");
        grantPK2.setIdentityId("identity123");

        // Assert that the hash codes of the two objects are equal
        assertEquals(grantPK1.hashCode(), grantPK2.hashCode());
    }

    // Test to verify that two GrantPK objects with different tenantId or identityId have different hash codes
    @Test
    void testHashCode_whenObjectsAreNotEqual_returnsDifferentHashCodes() {
        GrantPK grantPK1 = new GrantPK();
        grantPK1.setTenantId("tenant123");
        grantPK1.setIdentityId("identity123");

        GrantPK grantPK2 = new GrantPK();
        grantPK2.setTenantId("tenant456");
        grantPK2.setIdentityId("identity456");

        // Assert that the hash codes of the two objects are not equal
        assertNotEquals(grantPK1.hashCode(), grantPK2.hashCode());
    }
}