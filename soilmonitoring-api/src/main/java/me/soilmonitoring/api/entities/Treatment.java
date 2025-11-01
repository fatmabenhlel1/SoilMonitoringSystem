package me.soilmonitoring.api.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.LocalDateTime;

@Entity
public class Treatment implements RootEntity<String> {
    @Id
    private String id;

    @Column
    private long version = 0L;

    @Column
    private String fieldId;

    @Column
    private String treatmentType; // "fertilizer", "pesticide", "irrigation"

    @Column
    private String productName;

    @Column
    private String quantity;

    @Column
    private LocalDateTime appliedAt;

    @Column
    private String appliedBy; // userId

    @Column
    private String notes;

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

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(String treatmentType) {
        this.treatmentType = treatmentType;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getAppliedBy() {
        return appliedBy;
    }

    public void setAppliedBy(String appliedBy) {
        this.appliedBy = appliedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}