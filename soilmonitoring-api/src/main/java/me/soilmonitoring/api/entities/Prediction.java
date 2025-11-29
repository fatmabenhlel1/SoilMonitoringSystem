package me.soilmonitoring.api.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.LocalDateTime;
import java.util.List;


// TODO: add documentation


@Entity
public class Prediction implements RootEntity<String> {
    @Id
    private String id;

    @Column
    private long version = 0L;

    @Column
    private String fieldId;

    @Column
    private String predictionType; // "crop", "fertilizer"

    @Column
    private String modelUsed; // "CatBoost", "RandomForest"

    @Column
    private PredictionInput inputData;

    @Column
    private PredictionResult result;

    @Column
    private Double confidence;

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

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public PredictionInput getInputData() {
        return inputData;
    }

    public void setInputData(PredictionInput inputData) {
        this.inputData = inputData;
    }

    public PredictionResult getResult() {
        return result;
    }

    public void setResult(PredictionResult result) {
        this.result = result;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Embeddable
    public static class PredictionInput {
        // Common inputs for both models
        @Column
        private Double nitrogen;

        @Column
        private Double phosphorus;

        @Column
        private Double potassium;

        @Column
        private Double temperature;

        @Column
        private Double humidity;

        @Column
        private Double soilMoisture;

        // Additional inputs for fertilizer recommendation
        @Column
        private String soilType; // For fertilizer model

        @Column
        private String cropType; // For fertilizer model

        // Additional inputs for fertilizer recommendation
        @Column
        private Double pH; // For fertilizer model

        @Column
        private Double rainfall; // For fertilizer model



        // Getters and Setters
        public Double getNitrogen() { return nitrogen; }
        public void setNitrogen(Double nitrogen) { this.nitrogen = nitrogen; }

        public Double getPhosphorus() { return phosphorus; }
        public void setPhosphorus(Double phosphorus) { this.phosphorus = phosphorus; }

        public Double getPotassium() { return potassium; }
        public void setPotassium(Double potassium) { this.potassium = potassium; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Double getHumidity() { return humidity; }
        public void setHumidity(Double humidity) { this.humidity = humidity; }

        public Double getSoilMoisture() { return soilMoisture; }
        public void setSoilMoisture(Double soilMoisture) { this.soilMoisture = soilMoisture; }

        public String getSoilType() { return soilType; }
        public void setSoilType(String soilType) { this.soilType = soilType; }

        public String getCropType() { return cropType; }
        public void setCropType(String cropType) { this.cropType = cropType; }

        public Double getpH() { return pH; }
        public void setpH(Double pH) { this.pH = pH; }


        public Double getRainfall() { return rainfall; }
        public void setRainfall(Double rainfall) { this.rainfall = rainfall; }
    }

    @Embeddable
    public static class PredictionResult {
        @Column
        private String recommendation; // Crop name or fertilizer type

        @Column
        private String dosage; // For fertilizer

        @Column
        private List<String> details;

        // Getters and Setters
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public List<String> getDetails() { return details; }
        public void setDetails(List<String> details) { this.details = details; }
    }
}