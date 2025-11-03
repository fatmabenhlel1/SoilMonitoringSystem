package me.soilmonitoring.iam.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdentityUtilityTest {

    @Test
    void iAm() {
        IdentityUtility.iAm("testUser");
        assertEquals("testUser", IdentityUtility.whoAmI(), "The username should be set correctly.");
    }

    @Test
    void whoAmI() {
        IdentityUtility.iAm("anotherUser");
        assertEquals("anotherUser", IdentityUtility.whoAmI(), "The username should be retrieved correctly.");
    }

    @Test
    void setRoles() {
        Set<String> roles = Set.of("admin", "user");
        IdentityUtility.setRoles(roles);
        assertEquals(roles, IdentityUtility.getRoles(), "The roles should be set correctly.");
    }

    @Test
    void getRoles() {
        Set<String> roles = Set.of("viewer");
        IdentityUtility.setRoles(roles);
        assertTrue(IdentityUtility.getRoles().contains("viewer"), "The roles should include 'viewer'.");
    }

    @Test
    void tenantWithName() {
        IdentityUtility.tenantWithName("testTenant");
        assertEquals("testTenant", IdentityUtility.whichTenant(), "The tenant name should be set correctly.");
    }

    @Test
    void whichTenant() {
        IdentityUtility.tenantWithName("anotherTenant");
        assertEquals("anotherTenant", IdentityUtility.whichTenant(), "The tenant name should be retrieved correctly.");
    }
}