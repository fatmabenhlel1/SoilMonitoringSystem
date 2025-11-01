package me.soilmonitoring.api.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.LocalDateTime;



@Entity
public class Alert implements RootEntity<String> {
    @Id
    private String id;

    @Column
    private long version = 0L;

    @Column
    private String userId;

    @Column
    private String fieldId;

    @Column
    private String alertType; // "temperature", "humidity", "npk_deficiency", "moisture"

    @Column
    private String severity; // "low", "medium", "high"

    @Column
    private String message;

    @Column
    private Boolean isRead;

    @Column
    private LocalDateTime createdAt;

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
            throw new IllegalStateException();
        }
        ++this.version;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}