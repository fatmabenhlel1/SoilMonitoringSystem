/**
 * Admin Actions Module
 * Handles admin-specific actions (view details, sensors, predictions, etc.)
 */

import { CONFIG, STATE } from './config.module.js';
import { escapeHtml, getFieldName, showError, showSuccess, showLoading } from './utils.module.js';
import { displayPrediction } from './predictions.module.js';

/**
 * Mark alert as read
 */
export async function markAlertAsRead(alertId) {
    try {
        await window.ApiService.markAlertAsRead(alertId);
        console.log('✅ Alert marked as read:', alertId);

        STATE.alerts = await window.ApiService.getAlertsByUser(CONFIG.userId);

        // Trigger update (will be called from main admin.js)
        return true;
    } catch (error) {
        console.error('❌ Error marking alert as read:', error);
        showError('Failed to mark alert as read');
        return false;
    }
}

/**
 * View field details in modal
 */
export async function viewFieldDetails(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('fieldDetailsModal'));
        const content = document.getElementById('fieldDetailsContent');

        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-success"></div></div>';
        modal.show();

        const field = await window.ApiService.getFieldById(fieldId);
        const readings = await window.ApiService.getReadingsByField(fieldId);
        const sensors = await window.ApiService.getSensorsByField(fieldId);

        content.innerHTML = `
            <h5>${escapeHtml(field.name)}</h5>
            <p class="text-muted">${escapeHtml(field.location.address)}</p>
            <hr>
            <div class="row">
                <div class="col-6"><strong>Area:</strong> ${field.area} hectares</div>
                <div class="col-6"><strong>Soil Type:</strong> ${escapeHtml(field.soilType)}</div>
                <div class="col-6"><strong>Current Crop:</strong> ${escapeHtml(field.currentCrop || 'Not set')}</div>
                <div class="col-6"><strong>Sensors:</strong> ${sensors.length}</div>
            </div>
            <hr>
            <h6>Recent Readings</h6>
            <p class="text-muted">${readings.length} total readings</p>
        `;
    } catch (error) {
        console.error('❌ Error loading field details:', error);
        showError('Failed to load field details');
    }
}

/**
 * View field sensors in modal
 */
export async function viewFieldSensors(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('sensorsModal'));
        const content = document.getElementById('sensorsModalContent');

        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-info"></div></div>';
        modal.show();

        const sensors = await window.ApiService.getSensorsByField(fieldId);
        const fieldName = getFieldName(fieldId);

        if (!sensors || sensors.length === 0) {
            content.innerHTML = `
                <div class="text-center py-4">
                    <i class="fas fa-microchip fa-3x text-muted mb-3"></i>
                    <p class="text-muted">No sensors found for ${escapeHtml(fieldName)}</p>
                </div>
            `;
            return;
        }

        content.innerHTML = `
            <h6 class="mb-3">Sensors for ${escapeHtml(fieldName)}</h6>
            ${sensors.map(sensor => {
            const statusColor = { active: 'success', inactive: 'secondary', error: 'danger' }[sensor.status] || 'secondary';
            return `
                    <div class="card mb-2">
                        <div class="card-body d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="mb-1">${escapeHtml(sensor.sensorType)}</h6>
                                <small class="text-muted">${escapeHtml(sensor.deviceId)}</small>
                                <br><small class="text-muted">Installed: ${new Date(sensor.installedAt).toLocaleDateString()}</small>
                            </div>
                            <span class="badge bg-${statusColor}">${escapeHtml(sensor.status)}</span>
                        </div>
                    </div>
                `;
        }).join('')}
        `;
    } catch (error) {
        console.error('❌ Error loading sensors:', error);
        showError('Failed to load sensors');
    }
}

/**
 * Get prediction for a field
 */
export async function getPredictionForField(fieldId) {
    try {
        showLoading(true);

        const reading = await window.ApiService.getLatestReading(fieldId);

        if (!reading || !reading.data) {
            throw new Error('No sensor data available for prediction');
        }

        const prediction = await window.ApiService.predictCrop(fieldId, {
            n: reading.data.nitrogen,
            p: reading.data.phosphorus,
            k: reading.data.potassium,
            temperature: reading.data.temperature,
            humidity: reading.data.humidity,
            ph: reading.data.pH,
            rainfall: reading.data.rainfall || 0
        });

        console.log('✅ Prediction generated:', prediction);
        displayPrediction(prediction);
        showSuccess('Crop prediction generated successfully!');
        showLoading(false);
    } catch (error) {
        console.error('❌ Error getting prediction:', error);
        showError('Failed to generate prediction: ' + error.message);
        showLoading(false);
    }
}

/**
 * Request crop prediction (called from button)
 */
export async function requestCropPrediction() {
    if (STATE.fields.length === 0) {
        showError('No fields available. Please add a field first.');
        return;
    }

    const fieldId = STATE.selectedFieldId || STATE.fields[0].id;
    await getPredictionForField(fieldId);
}

/**
 * Placeholder functions
 */
export function showAddFieldModal() {
    showError('Add Field functionality - Coming soon!');
}

export function showAddSensorModal() {
    showError('Add Sensor functionality - Coming soon!');
}

export function viewAllNotifications(event) {
    event.preventDefault();
    showError('View All Notifications - Coming soon!');
}

export function exportData() {
    showError('Export Data functionality - Coming soon!');
}