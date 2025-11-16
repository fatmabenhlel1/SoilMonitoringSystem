package me.soilmonitoring.iam.entities;


import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
public class Tenant implements RootEntity<String> {
    @Id
    private String id;
    @Column
    private long version=0L;

    @Column
    private String name;
    @Column
    private String secret;
    @Column
    private String redirectUri;

    @Column
    private Long allowedRoles;

    @Column
    private String requiredScopes;

    @Column
    private String supportedGrantTypes;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        if (this.version !=version ){
            throw new IllegalStateException();

        }

        ++this.version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public Long getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Long allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public String getRequiredScopes() {
        return requiredScopes;
    }

    public void setRequiredScopes(String requiredScopes) {
        this.requiredScopes = requiredScopes;
    }

    public String getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public void setSupportedGrantTypes(String supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
    }
}
