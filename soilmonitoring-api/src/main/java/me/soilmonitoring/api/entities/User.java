package me.soilmonitoring.api.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a system user (farmer/admin).
 * This entity is used for authentication and access control.
 */
@Entity
public class User implements RootEntity<String> {

    @Id
    private String id;

    @Column
    private long version = 0L;

    @Column
    private String username;

    @Column
    private String email;

    @Column
    private String fullName;

    @Column
    private String password; // hashed password

    @Column
    private String role; // "farmer", "admin"

    @Column
    private boolean active = true; // account enabled/disabled

    @Column
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
        // Required by Jakarta NoSQL
    }

    public User(String id, String username, String email, String fullName,
                String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // ---- RootEntity methods ----
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        if (this.version != version) {
            throw new IllegalStateException("Version conflict detected");
        }
        ++this.version;
    }

    // ---- Getters / Setters ----
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ---- Utility methods ----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * DO NOT include password in toString() for security reasons.
     */
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
