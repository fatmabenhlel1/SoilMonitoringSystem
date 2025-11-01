package me.soilmonitoring.iam.controllers.managers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import me.soilmonitoring.iam.controllers.Role;
import me.soilmonitoring.iam.controllers.repositories.GrantRepository;
import me.soilmonitoring.iam.controllers.repositories.IdentityRepository;
import me.soilmonitoring.iam.controllers.repositories.TenantRepository;
import me.soilmonitoring.iam.entities.Grant;
import me.soilmonitoring.iam.entities.GrantPK;
import me.soilmonitoring.iam.entities.Identity;
import me.soilmonitoring.iam.entities.Tenant;

import java.util.HashSet;
import java.util.Optional;

@Singleton
public class PhoenixIAMManager {
    @Inject
    private IdentityRepository identityRepository;
    @Inject
    private GrantRepository grantRepository;
    @Inject
    private TenantRepository tenantRepository;
    public Tenant findTenantByName(String name){
        return tenantRepository.findByName(name).orElseThrow(IllegalArgumentException::new);
    }

    public Identity findIdentityByUsername(String username){
        return identityRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
    }

    public Optional<Grant> findGrant(String tenantName,String identityId){
        Tenant tenant = findTenantByName(tenantName);
        if(tenant==null){
            throw new IllegalArgumentException("Invalid Client Id!");
        }
        var pk = new GrantPK();
        pk.setIdentityId(identityId);
        pk.setTenantId(tenant.getId());
        return grantRepository.findById(pk);

    }
    public String[] getRoles(String username){
        var identity = identityRepository.findByUsername(username).orElseThrow(IllegalArgumentException::new);
        var roles = identity.getRoles();
        var ret = new HashSet<String>();
        for(var role: Role.values()){
            if((roles&role.getValue())!=0L){
                String value = Role.byValue(role.getValue());
                if (value==null){
                    continue;
                }
                ret.add(value);
            }
        }
        return ret.toArray(new String[0]);
    }
}
