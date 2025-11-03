package me.soilmonitoring.iam.controllers.managers;

import me.soilmonitoring.iam.controllers.repositories.GrantRepository;
import me.soilmonitoring.iam.controllers.repositories.IdentityRepository;
import me.soilmonitoring.iam.controllers.repositories.TenantRepository;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.GrantPK;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoenixIAMManagerTest {

    private PhoenixIAMManager phoenixIAMManager;
    private TenantRepository tenantRepository;
    private IdentityRepository identityRepository;
    private GrantRepository grantRepository;

    @BeforeEach
    void setUp() {
        tenantRepository = mock(TenantRepository.class);
        identityRepository = mock(IdentityRepository.class);
        grantRepository = mock(GrantRepository.class);
        phoenixIAMManager = new PhoenixIAMManager();
        phoenixIAMManager.identityRepository = identityRepository;
        phoenixIAMManager.tenantRepository = tenantRepository;
        phoenixIAMManager.grantRepository = grantRepository;
    }

    @Test
    void findTenantByName() {
        Tenant tenant = new Tenant();
        tenant.setId("tenant123");
        tenant.setName("TestTenant");

        when(tenantRepository.findByName("TestTenant")).thenReturn(Optional.of(tenant));

        Tenant result = phoenixIAMManager.findTenantByName("TestTenant");
        assertNotNull(result);
        assertEquals("tenant123", result.getId());
        assertEquals("TestTenant", result.getName());

        verify(tenantRepository, times(1)).findByName("TestTenant");
    }

    @Test
    void findIdentityByUsername() {
        Identity identity = new Identity();
        identity.setId("identity123");
        identity.setUsername("TestUser");

        when(identityRepository.findByUsername("TestUser")).thenReturn(Optional.of(identity));

        Identity result = phoenixIAMManager.findIdentityByUsername("TestUser");
        assertNotNull(result);
        assertEquals("identity123", result.getId());
        assertEquals("TestUser", result.getUsername());

        verify(identityRepository, times(1)).findByUsername("TestUser");
    }

    @Test
    void findGrant() {
        Tenant tenant = new Tenant();
        tenant.setId("tenant123");
        tenant.setName("TestTenant");

        Grant grant = new Grant();
        GrantPK grantPK = new GrantPK();
        grantPK.setTenantId("tenant123");
        grantPK.setIdentityId("identity123");
        grant.setId(grantPK);

        when(tenantRepository.findByName("TestTenant")).thenReturn(Optional.of(tenant));
        when(grantRepository.findById(grantPK)).thenReturn(Optional.of(grant));

        Optional<Grant> result = phoenixIAMManager.findGrant("TestTenant", "identity123");
        assertTrue(result.isPresent());
        assertEquals(grantPK, result.get().getId());

        verify(tenantRepository, times(1)).findByName("TestTenant");
        verify(grantRepository, times(1)).findById(grantPK);
    }

    @Test
    void getRoles() {
        Identity identity = new Identity();
        identity.setUsername("TestUser");
        identity.setRoles(3L); // Assuming roles 1 and 2 are set

        when(identityRepository.findByUsername("TestUser")).thenReturn(Optional.of(identity));

        String[] roles = phoenixIAMManager.getRoles("TestUser");
        assertNotNull(roles);
        assertTrue(roles.length > 0);

        verify(identityRepository, times(1)).findByUsername("TestUser");
    }
}