package me.soilmonitoring.iam.controllers.managers;

import jakarta.inject.Inject;
import me.soilmonitoring.iam.controllers.repositories.GrantRepository;
import me.soilmonitoring.iam.controllers.repositories.IdentityRepository;
import me.soilmonitoring.iam.controllers.repositories.TenantRepository;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.GrantPK;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
class PhoenixIAMManagerTest {

    @Inject
    private PhoenixIAMManager phoenixIAMManager;

    @Inject
    private TenantRepository tenantRepository;

    @Inject
    private IdentityRepository identityRepository;

    @Inject
    private GrantRepository grantRepository;

    /**
     * Creates the Arquillian deployment archive for the test.
     * Includes the necessary classes and resources for the test environment.
     */
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(PhoenixIAMManager.class, TenantRepository.class, IdentityRepository.class, GrantRepository.class,
                        Tenant.class, Identity.class, Grant.class, GrantPK.class)
                .addAsManifestResource("META-INF/beans.xml", "beans.xml");
    }

    /**
     * Tests the `findTenantByName` method to ensure it retrieves the correct tenant by name.
     */
    @Test
    void findTenantByName() {
        Tenant tenant = new Tenant();
        tenant.setId("tenant123");
        tenant.setName("TestTenant");
        tenantRepository.save(tenant);

        Tenant result = phoenixIAMManager.findTenantByName("TestTenant");
        assertNotNull(result);
        assertEquals("tenant123", result.getId());
        assertEquals("TestTenant", result.getName());
    }

    /**
     * Tests the `findIdentityByUsername` method to ensure it retrieves the correct identity by username.
     */
    @Test
    void findIdentityByUsername() {
        Identity identity = new Identity();
        identity.setId("identity123");
        identity.setUsername("TestUser");
        identityRepository.save(identity);

        Identity result = phoenixIAMManager.findIdentityByUsername("TestUser");
        assertNotNull(result);
        assertEquals("identity123", result.getId());
        assertEquals("TestUser", result.getUsername());
    }

    /**
     * Tests the `findGrant` method to ensure it retrieves the correct grant based on tenant name and identity ID.
     */
    @Test
    void findGrant() {
        Tenant tenant = new Tenant();
        tenant.setId("tenant123");
        tenant.setName("TestTenant");
        tenantRepository.save(tenant);

        Grant grant = new Grant();
        GrantPK grantPK = new GrantPK();
        grantPK.setTenantId("tenant123");
        grantPK.setIdentityId("identity123");
        grant.setId(grantPK);
        grantRepository.save(grant);

        Optional<Grant> result = phoenixIAMManager.findGrant("TestTenant", "identity123");
        assertTrue(result.isPresent());
        assertEquals(grantPK, result.get().getId());
    }
}