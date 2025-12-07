package me.soilmonitoring.iam.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.LocalDateTime;

@Entity
public class Grant implements RootEntity<GrantPK> {

    @Id
    @Column("_id")
    private String id;  // Composite key as string: "tenantId:identityId"

    @Column
    private long version = 0L;

    @Column
    private String tenantId;

    @Column
    private String identityId;

    @Column
    private String approvedScopes;

    @Column
    private LocalDateTime issuanceDateTime;

    // Implement RootEntity interface
    @Override
    public GrantPK getId() {
        if (tenantId == null || identityId == null) {
            return null;
        }
        GrantPK pk = new GrantPK();
        pk.setTenantId(tenantId);
        pk.setIdentityId(identityId);
        return pk;
    }

    @Override
    public void setId(GrantPK pk) {
        if (pk != null) {
            this.id = pk.getTenantId() + ":" + pk.getIdentityId();
            this.tenantId = pk.getTenantId();
            this.identityId = pk.getIdentityId();
        }
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        if (this.version != version) {
            throw new IllegalStateException();
        }
        ++this.version;
    }

    // Additional getters/setters

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

    public String getApprovedScopes() {
        return approvedScopes;
    }

    public void setApprovedScopes(String approvedScopes) {
        this.approvedScopes = approvedScopes;
    }

    public LocalDateTime getIssuanceDateTime() {
        return issuanceDateTime;
    }

    public void setIssuanceDateTime(LocalDateTime issuanceDateTime) {
        this.issuanceDateTime = issuanceDateTime;
    }
}