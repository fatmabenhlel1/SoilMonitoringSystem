package me.soilmonitoring.api.events;

import me.soilmonitoring.api.entities.SensorReading;

public class SensorReadingEvent {
    private final SensorReading reading;
    private final String source;

    public SensorReadingEvent(SensorReading reading, String source) {
        this.reading = reading;
        this.source = source;
    }

    public SensorReading getReading() {
        return reading;
    }

    public String getSource() {
        return source;
    }
}