package me.soilmonitoring.api.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

@Embeddable
public class SensorData {
    @Column
    private Double temperature;

    @Column
    private Double humidity;

    @Column
    private Double nitrogen;

    @Column
    private Double phosphorus;

    @Column
    private Double potassium;

    @Column
    private Double soilMoisture;

    @Column
    private Double rainfall;

    public SensorData() {
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getNitrogen() {
        return nitrogen;
    }

    public void setNitrogen(Double nitrogen) {
        this.nitrogen = nitrogen;
    }

    public Double getPhosphorus() {
        return phosphorus;
    }

    public void setPhosphorus(Double phosphorus) {
        this.phosphorus = phosphorus;
    }

    public Double getPotassium() {
        return potassium;
    }

    public void setPotassium(Double potassium) {
        this.potassium = potassium;
    }

    public Double getSoilMoisture() {
        return soilMoisture;
    }

    public void setSoilMoisture(Double soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    public Double getRainfall() {
        return rainfall;
    }

    public void setRainfall(Double rainfall) {
        this.rainfall = rainfall;
    }
}