/**
 * User Actions Module
 * Handles user-specific actions (views, predictions, etc.)
 */

import { escapeHtml, formatTimeAgo, showError, showSuccess, getFieldName } from './utils.module.js';

/**
 * Display fertilizer prediction
 */
export function displayFertilizerPrediction(prediction) {
    const container = document.getElementById('predictionsContainer');

    const details = prediction.result.details || [];
    const confidence = (prediction.confidence * 100).toFixed(1);

    container.innerHTML = `
        <div class="card p-3 border-warning">
            <div class="d-flex align-items-center mb-3">
                <i class="fas fa-flask fa-2x text-warning me-3"></i>
                <div>
                    <h6 class="mb-0">Recommended Fertilizer</h6>
                    <h5 class="mb-0 text-warning fw-bold">${escapeHtml(prediction.result.recommendation)}</h5>
                </div>
            </div>
            
            ${prediction.result.dosage ? `
                <div class="alert alert-info mb-2">
                    <i class="fas fa-weight me-2"></i>
                    <strong>Dosage:</strong> ${escapeHtml(prediction.result.dosage)}
                </div>
            ` : ''}
            
            <div class="mb-2">
                <small class="text-muted">Confidence Score</small>
                <div class="progress" style="height: 20px;">
                    <div class="progress-bar bg-warning" role="progressbar" 
                         style="width: ${confidence}%" 
                         aria-valuenow="${confidence}" aria-valuemin="0" aria-valuemax="100">
                        ${confidence}%
                    </div>
                </div>
            </div>
            
            <hr>
            
            <div class="mb-2">
                <small class="text-muted"><i class="fas fa-robot me-1"></i>Model: ${escapeHtml(prediction.modelUsed)}</small>
            </div>
            
            ${details.length > 0 ? `
                <div class="mt-2">
                    <strong class="small">Analysis Details:</strong>
                    <ul class="small mb-0 mt-1">
                        ${details.map(detail => `<li>${escapeHtml(detail)}</li>`).join('')}
                    </ul>
                </div>
            ` : ''}
            
            <div class="mt-3 text-end">
                <small class="text-muted">
                    <i class="far fa-clock me-1"></i>
                    ${formatTimeAgo(prediction.createdAt)}
                </small>
            </div>
        </div>
    `;
}

/**
 * Get fertilizer prediction for field
 */
export async function getFertilizerPredictionForField(fieldId) {
    try {
        const reading = await window.ApiService.getLatestReading(fieldId);

        if (!reading || !reading.data) {
            throw new Error('No sensor data available for prediction');
        }

        const field = await window.ApiService.getFieldById(fieldId);

        const prediction = await window.ApiService.predictFertilizer(fieldId, {
            temperature: reading.data.temperature,
            humidity: reading.data.humidity,
            soilMoisture: reading.data.soilMoisture,
            soilType: field.soilType,
            cropType: field.currentCrop || 'rice',
            nitrogen: reading.data.nitrogen,
            phosphorus: reading.data.phosphorus,
            potassium: reading.data.potassium
        });

        console.log('✅ Fertilizer prediction generated:', prediction);
        displayFertilizerPrediction(prediction);
        showSuccess('Fertilizer recommendation generated successfully!');

    } catch (error) {
        console.error('❌ Error getting fertilizer prediction:', error);
        showError('Failed to generate fertilizer recommendation: ' + error.message);
    }
}

/**
 * View historical data
 */
export async function viewHistoricalData(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('historyModal'));
        const content = document.getElementById('historyModalContent');

        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-warning"></div></div>';
        modal.show();

        const readings = await window.ApiService.getReadingsByField(fieldId);
        const fieldName = getFieldName(fieldId);

        if (!readings || readings.length === 0) {
            content.innerHTML = `
                <div class="text-center py-4">
                    <i class="fas fa-history fa-3x text-muted mb-3"></i>
                    <p class="text-muted">No historical data for ${escapeHtml(fieldName)}</p>
                </div>
            `;
            return;
        }

        const recentReadings = readings.slice(-10).reverse();
        content.innerHTML = `
            <h6 class="mb-3"><i class="fas fa-history me-2"></i>Recent Readings for ${escapeHtml(fieldName)}</h6>
            <div class="table-responsive">
                <table class="table table-sm">
                    <thead>
                        <tr>
                            <th>Time</th>
                            <th>Temp</th>
                            <th>Humidity</th>
                            <th>Moisture</th>
                            <th>pH</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${recentReadings.map(reading => `
                            <tr>
                                <td><small>${formatTimeAgo(reading.timestamp)}</small></td>
                                <td>${reading.data.temperature}°C</td>
                                <td>${reading.data.humidity}%</td>
                                <td>${reading.data.soilMoisture}%</td>
                                <td>${reading.data.pH}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch (error) {
        console.error('❌ Error loading historical data:', error);
        showError('Failed to load historical data');
    }
}

/**
 * Placeholder functions
 */
export function viewAllNotifications(event) {
    event.preventDefault();
    showError('View All Notifications - Coming soon!');
}