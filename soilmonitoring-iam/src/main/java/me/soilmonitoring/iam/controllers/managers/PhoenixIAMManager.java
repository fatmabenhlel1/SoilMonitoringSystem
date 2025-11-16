package me.soilmonitoring.iam.controllers.managers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.soilmonitoring.iam.controllers.repositories.GrantRepository;
import me.soilmonitoring.iam.controllers.repositories.IdentityRepository;
import me.soilmonitoring.iam.controllers.repositories.TenantRepository;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;
import me.soilmonitoring.iam.entities.GrantPK;

import java.util.List;
import java.util.Optional;
@Singleton
public class PhoenixIAMManager {
    @Inject
    IdentityRepository identityRepository;

    @Inject
    GrantRepository grantRepository;

    @Inject
    TenantRepository tenantRepository;

    public void saveTenant(Tenant tenant) {
        tenantRepository.save(tenant);
    }

    public void saveIdentity(Identity identity) {
        identityRepository.save(identity);
    }

    public void saveGrant(Grant grant) {
        grantRepository.save(grant);
    }

    public Tenant findTenantByName(String name) {
        return tenantRepository.findByName(name).orElseThrow(IllegalArgumentException::new);
    }

    public Identity findIdentityByUsername(String username) {
        return identityRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }
    public List<String> getRoles(String username) {
        // Remplacez cette logique par celle qui correspond à votre application
        // Par exemple, interrogez une base de données ou utilisez une liste prédéfinie
        return List.of("role1", "role2"); // Exemple de rôles
    }

    public Optional<Grant> findGrant(String tenantName, String identityId) {
        Tenant tenant = findTenantByName(tenantName);
        if (tenant == null) {
            throw new IllegalArgumentException("Invalid Client Id!");
        }
        var pk = new GrantPK();
        pk.setIdentityId(identityId);
        pk.setTenantId(tenant.getId());
        return grantRepository.findById(pk);
    }
}