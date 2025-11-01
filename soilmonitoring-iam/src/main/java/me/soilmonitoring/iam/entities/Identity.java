package me.soilmonitoring.iam.entities;



import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.security.Principal;

@Entity
public class Identity implements RootEntity<String>,Principal {
    @Id
    private String id;

    @Column
    private long version=0L;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private Long roles;

    @Column
    private String providedScopes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        if (this.version !=version ){
            throw new IllegalStateException();

        }

        ++this.version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getRoles() {
        return roles;
    }

    public void setRoles(Long roles) {
        this.roles = roles;
    }

    public String getProvidedScopes() {
        return providedScopes;
    }

    public void setProvidedScopes(String providedScopes) {
        this.providedScopes = providedScopes;
    }
}
