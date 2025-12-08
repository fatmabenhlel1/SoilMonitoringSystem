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
import me.soilmonitoring.iam.enums.Role;

import java.util.ArrayList;
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

    /**
     * Get roles for a user based on their role bitmask
     * Converts the Long roles bitmask to a list of role names
     */
    public List<String> getRoles(String username) {
        try {
            Identity identity = findIdentityByUsername(username);
            Long rolesBitmask = identity.getRoles();

            if (rolesBitmask == null) {
                return List.of("Farmer"); // Default role
            }

            List<String> userRoles = new ArrayList<>();

            // Check each role bit
            for (Role role : Role.values()) {
                if (role == Role.GUEST) continue; // Skip guest role

                // Check if this role bit is set
                if ((rolesBitmask & role.getValue()) != 0) {
                    String roleName = role.id();
                    if (roleName != null) {
                        userRoles.add(roleName);
                    }
                }
            }

            // If no roles found, return default
            if (userRoles.isEmpty()) {
                userRoles.add("Farmer");
            }

            return userRoles;

        } catch (Exception e) {
            // If user not found or error, return default role
            return List.of("Farmer");
        }
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