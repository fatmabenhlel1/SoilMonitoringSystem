package me.soilmonitoring.api.events;

import me.soilmonitoring.api.entities.Alert;

public class AlertTriggeredEvent {
    private final Alert alert;

    public AlertTriggeredEvent(Alert alert) {
        this.alert = alert;
    }

    public Alert getAlert() {
        return alert;
    }
}