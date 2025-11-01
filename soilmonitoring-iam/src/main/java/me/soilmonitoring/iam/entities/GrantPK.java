package me.soilmonitoring.iam.entities;



import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GrantPK implements Serializable {

    @Column
    private String tenantId;
    @Column
    private String identityId;

    public GrantPK(){
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GrantPK grantPK)) return false;
        return Objects.equals(tenantId, grantPK.tenantId) && Objects.equals(identityId, grantPK.identityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, identityId);
    }
}
