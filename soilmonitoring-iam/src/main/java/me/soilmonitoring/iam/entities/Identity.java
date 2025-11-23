package me.soilmonitoring.iam.entities;



import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;
import me.soilmonitoring.iam.security.Argon2Utility;


@Entity
public class Identity implements Serializable, Principal {    @Id
    @Column("_id")
    private String id;

    @Column("username")
    private String username;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("creationDate")
    private String creationDate;

    @Column("role")
    private Long roles;

    @Column("scopes")
    private String scopes;

    @Column("isAccountActivated")
    private boolean isAccountActivated;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public Long getRoles() {
        return roles;
    }

    public void setRoles(Long roles) {
        this.roles = roles;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public boolean isAccountActivated() {
        return isAccountActivated;
    }

    public void setAccountActivated(boolean accountActivated) {
        this.isAccountActivated = accountActivated;
    }

    // Constructor
    public Identity() {
        this.id = UUID.randomUUID().toString();
        this.isAccountActivated = false;
    }

    public Identity(String id, String username, String password, String creationDate, Long roles, boolean isAccountActivated) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.creationDate = creationDate;
        this.roles = roles;
        this.isAccountActivated = isAccountActivated;
    }

    @Override
    public String toString() {
        return "Identity{" +
                "_id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", creationDate=" + creationDate +
                ", roles=" + roles +
                ", scopes=" + scopes +
                ", accountActivated=" + isAccountActivated +
                '}';
    }

    @Override
    public String getName() {
        return username;
    }

    // Password hashing utility method
    public void hashPassword(String password, Argon2Utility argonUtility) {
        this.password = argonUtility.hash(password.toCharArray());
    }
}