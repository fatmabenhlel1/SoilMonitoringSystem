package me.soilmonitoring.iam.entities;


import jakarta.nosql.Column;

import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.LocalDateTime;

@Entity
public class Grant implements RootEntity<GrantPK> {
    @Id
    private GrantPK id;
    @Column
    private long version=0L;
    @Column
    private Tenant tenant;
    @Column
    private Identity identity;

    @Column
    private String approvedScopes;

    @Column
    private LocalDateTime issuanceDateTime;

    @Override
    public GrantPK getId() {
        return id;
    }

    @Override
    public void setId(GrantPK id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        if (this.version !=version ){
            throw new IllegalStateException();

        }

        ++this.version;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
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
