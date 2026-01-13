/**
 * Statistics Module (User Dashboard)
 * Manages dashboard statistics display
 */

/**
 * Update dashboard statistics
 */
export async function updateStatistics(fields, alerts, predictions) {
    // Update field count
    const fieldsEl = document.getElementById('totalFields');
    if (fieldsEl) fieldsEl.textContent = fields.length;

    // Count total sensors
    let totalSensors = 0;
    for (const field of fields) {
        try {
            const sensors = await window.ApiService.getSensorsByField(field.id);
            totalSensors += sensors.length;
        } catch (error) {
            // Ignore errors
        }
    }

    const sensorsEl = document.getElementById('totalSensors');
    if (sensorsEl) sensorsEl.textContent = totalSensors;

    // Update alerts count
    const unreadAlerts = alerts.filter(a => !a.isRead).length;
    const alertsEl = document.getElementById('totalAlerts');
    if (alertsEl) alertsEl.textContent = unreadAlerts;

    // Update predictions count
    const predictionsEl = document.getElementById('totalPredictions');
    if (predictionsEl) predictionsEl.textContent = predictions.length;
}