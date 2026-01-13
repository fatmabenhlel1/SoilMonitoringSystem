/**
 * User Dashboard Module
 * Handles user dashboard initialization and data loading
 */

import { CONFIG, STATE } from './config.module.js';
import { showSuccess, showError } from './utils.module.js';
import { displayFields, updateFieldSensorData } from './fields.module.js';
import { displayAlerts, updateNotificationBadge } from './alerts.module.js';
import { displayPrediction } from './predictions.module.js';
import { updateStatistics } from './statistics.module.js';

/**
 * Initialize user dashboard
 */
export async function initializeDashboard() {
    try {
        await checkAPIHealth();
        await loadAllData();

        showSuccess('Dashboard loaded - Real-time monitoring active! 🔴');

    } catch (error) {
        console.error('❌ Dashboard initialization failed:', error);
        showError('Failed to load dashboard. Please ensure the API is running on https://api.soilmonitoring.me');
    }
}

/**
 * Check API health
 */
export async function checkAPIHealth() {
    console.log('🏥 Checking API health...');
    const health = await window.ApiService.checkHealth();
    console.log('✅ API Status:', health.status);
}

/**
 * Load all dashboard data
 */
export async function loadAllData() {
    console.log('📥 Loading dashboard data...');

    console.log('📍 Loading fields...');
    STATE.fields = await window.ApiService.getFieldsByUser(CONFIG.userId);
    console.log(`✅ Loaded ${STATE.fields.length} fields`);
    displayFields(STATE.fields);

    console.log('🔔 Loading alerts...');
    STATE.alerts = await window.ApiService.getAlertsByUser(CONFIG.userId);
    console.log(`✅ Loaded ${STATE.alerts.length} alerts`);
    displayAlerts(STATE.alerts);
    updateNotificationBadge(STATE.alerts);

    if (STATE.fields.length > 0) {
        console.log('🤖 Loading predictions...');
        await loadPredictions();
    }

    if (STATE.fields.length > 0 && !STATE.wsConnected) {
        STATE.selectedFieldId = STATE.fields[0].id;
        await loadFieldsSensorData();
    }

    await updateStatistics(STATE.fields, STATE.alerts, STATE.predictions);
}

/**
 * Load sensor data for all fields
 */
export async function loadFieldsSensorData() {
    console.log('🔌 Loading sensor data for all fields...');

    for (const field of STATE.fields) {
        try {
            const reading = await window.ApiService.getLatestReading(field.id);
            updateFieldSensorData(field.id, reading);
        } catch (error) {
            console.warn(`⚠️ No sensor data for field: ${field.name}`);
            updateFieldSensorData(field.id, null);
        }
    }
}

/**
 * Load predictions
 */
export async function loadPredictions() {
    try {
        const fieldId = STATE.fields[0].id;
        const predictions = await window.ApiService.getPredictionsByField(fieldId);

        if (predictions && predictions.length > 0) {
            STATE.predictions = predictions;
            displayPrediction(predictions[0]);
        }
    } catch (error) {
        console.warn('⚠️ No predictions available yet');
    }
}

/**
 * Setup auto-refresh
 */
export function setupAutoRefresh() {
    setInterval(async () => {
        if (STATE.wsConnected) {
            console.log('🔄 Auto-refresh (WS active): alerts only');

            STATE.alerts = await window.ApiService.getAlertsByUser(CONFIG.userId);
            displayAlerts(STATE.alerts);
            updateNotificationBadge(STATE.alerts);
            await updateStatistics(STATE.fields, STATE.alerts, STATE.predictions);
        } else {
            console.log('🔄 Auto-refresh (WS inactive): full reload');
            await loadAllData();
        }
    }, CONFIG.refreshInterval);
}

/**
 * Refresh dashboard
 */
export async function refreshDashboard() {
    console.log('🔄 Refreshing dashboard...');
    showSuccess('Refreshing dashboard data...');
    await loadAllData();
}