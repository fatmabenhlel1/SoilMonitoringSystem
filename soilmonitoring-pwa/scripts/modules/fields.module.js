/**
 * Fields Module
 * Handles field display and sensor data
 */

import { escapeHtml, checkDataHealth } from './utils.module.js';

/**
 * Display fields list
 */
export function displayFields(fields) {
    const container = document.getElementById('fieldsContainer');

    if (!fields || fields.length === 0) {
        container.innerHTML = `
            <div class="card zone-card text-center py-5">
                <i class="fas fa-map-marked-alt fa-4x text-muted mb-3"></i>
                <h5 class="text-muted">No Fields Found</h5>
                <p class="text-muted">Click "Add Field" to create your first agricultural zone</p>
                <button class="btn btn-success mt-3" onclick="showAddFieldModal()">
                    <i class="fas fa-plus me-2"></i>Add Your First Field
                </button>
            </div>
        `;
        return;
    }

    container.innerHTML = fields.map(field => `
        <div class="card zone-card mb-3" id="field-${field.id}">
            <div class="zone-header">
                <div class="zone-title">
                    <i class="fas fa-map-marker-alt me-2"></i>${escapeHtml(field.name)}
                </div>
                <span class="zone-status" id="status-${field.id}">
                    <span class="spinner-border spinner-border-sm" role="status"></span>
                </span>
            </div>
            
            <div class="small text-muted mb-3">
                <div><i class="fas fa-map-pin me-2"></i>${escapeHtml(field.location.address)}</div>
                <div class="mt-1">
                    <i class="fas fa-seedling me-2"></i>Current Crop: <strong>${escapeHtml(field.currentCrop || 'Not set')}</strong>
                </div>
                <div class="mt-1">
                    <i class="fas fa-ruler me-2"></i>Area: <strong>${field.area} hectares</strong> | 
                    Soil: <strong>${escapeHtml(field.soilType)}</strong>
                </div>
            </div>
            
            <div class="sensor-grid" id="sensors-grid-${field.id}">
                <div class="text-center py-3">
                    <div class="spinner-border spinner-border-sm text-success" role="status"></div>
                    <span class="ms-2 text-muted small">Loading sensors...</span>
                </div>
            </div>
            
            <div class="mt-3 d-flex gap-2 flex-wrap">
                <button class="btn btn-sm btn-primary" onclick="viewFieldDetails('${field.id}')">
                    <i class="fas fa-chart-line me-1"></i>View Details
                </button>
                <button class="btn btn-sm btn-success" onclick="getPredictionForField('${field.id}')">
                    <i class="fas fa-brain me-1"></i>Crop Prediction
                </button>
                <button class="btn btn-sm btn-info" onclick="viewFieldSensors('${field.id}')">
                    <i class="fas fa-microchip me-1"></i>View Sensors
                </button>
            </div>
        </div>
    `).join('');
}

/**
 * Update field sensor data display
 */
export function updateFieldSensorData(fieldId, reading) {
    const gridContainer = document.getElementById(`sensors-grid-${fieldId}`);
    const statusBadge = document.getElementById(`status-${fieldId}`);

    if (!reading || !reading.data) {
        gridContainer.innerHTML = `
            <div class="text-center text-muted py-3">
                <i class="fas fa-exclamation-triangle me-2"></i>
                <span class="small">No sensor data available</span>
            </div>
        `;
        if (statusBadge) {
            statusBadge.innerHTML = '<span class="badge bg-secondary">No Data</span>';
        }
        return;
    }

    const data = reading.data;
    const isNormal = checkDataHealth(data);

    if (statusBadge) {
        statusBadge.innerHTML = isNormal
            ? '<span class="badge bg-success">Normal</span>'
            : '<span class="badge bg-warning">Warning</span>';
    }

    gridContainer.innerHTML = `
        <div class="sensor-box">
            <div class="sensor-icon temp">
                <i class="fas fa-thermometer-half"></i>
            </div>
            <div class="sensor-value">${data.temperature}°C</div>
            <div class="sensor-label">Temperature</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon humidity">
                <i class="fas fa-tint"></i>
            </div>
            <div class="sensor-value">${data.humidity}%</div>
            <div class="sensor-label">Humidity</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon moisture">
                <i class="fas fa-water"></i>
            </div>
            <div class="sensor-value">${data.soilMoisture}%</div>
            <div class="sensor-label">Soil Moisture</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon rainfall">
                <i class="fas fa-cloud-rain"></i>
            </div>
            <div class="sensor-value">${data.rainfall} mm</div>
            <div class="sensor-label">Rainfall</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon npk">
                <i class="fas fa-flask"></i>
            </div>
            <div class="sensor-value">N: ${data.nitrogen}</div>
            <div class="sensor-label">Nitrogen</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon npk">
                <i class="fas fa-vial"></i>
            </div>
            <div class="sensor-value">P: ${data.phosphorus}</div>
            <div class="sensor-label">Phosphorus</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon npk">
                <i class="fas fa-leaf"></i>
            </div>
            <div class="sensor-value">K: ${data.potassium}</div>
            <div class="sensor-label">Potassium</div>
        </div>
        <div class="sensor-box">
            <div class="sensor-icon">
                <i class="fas fa-balance-scale"></i>
            </div>
            <div class="sensor-value">${data.pH}</div>
            <div class="sensor-label">pH Level</div>
        </div>
    `;
}

/**
 * Update fields count badge
 */
export function updateFieldsCount(count) {
    const badge = document.getElementById('fieldsCount');
    if (badge) {
        badge.textContent = `${count} Field${count !== 1 ? 's' : ''}`;
    }
}