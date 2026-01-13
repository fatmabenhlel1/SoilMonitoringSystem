/**
 * Admin Dashboard Module
 * Handles dashboard initialization and data loading
 */

import { CONFIG, STATE } from './config.module.js';
import { showLoading, showSuccess, showError } from './utils.module.js';
import { displayFields, updateFieldSensorData, updateFieldsCount } from './fields.module.js';
import { displayAlerts, updateNotificationBadge } from './alerts.module.js';

/**
 * Initialize admin dashboard
 */
export async function initializeAdminDashboard() {
    try {
        showLoading(true);

        await checkAPIHealth();
        await loadAllData();

        showLoading(false);
        showSuccess('Dashboard loaded successfully!');
    } catch (error) {
        console.error('❌ Dashboard initialization failed:', error);
        showError('Failed to load dashboard. Please ensure the API is running on https://api.soilmonitoring.me');
        showLoading(false);
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
    updateFieldsCount(STATE.fields.length);

    console.log('🔔 Loading alerts...');
    STATE.alerts = await window.ApiService.getAlertsByUser(CONFIG.userId);
    console.log(`✅ Loaded ${STATE.alerts.length} alerts`);
    displayAlerts(STATE.alerts);
    updateNotificationBadge(STATE.alerts);

    if (STATE.fields.length > 0 && !STATE.wsConnected) {
        STATE.selectedFieldId = STATE.fields[0].id;
        await loadFieldsSensorData();
    }
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
 * Refresh dashboard
 */
export async function refreshDashboard() {
    console.log('🔄 Refreshing dashboard...');
    showSuccess('Refreshing dashboard data...');
    await loadAllData();
}

/**
 * Setup auto-refresh
 */
export function setupAutoRefresh() {
    setInterval(async () => {
        if (STATE.wsConnected) {
            console.log('🔄 Auto-refresh (WS active): alerts & fields only');

            STATE.alerts = await window.ApiService.getAlertsByUser(CONFIG.userId);
            displayAlerts(STATE.alerts);
            updateNotificationBadge(STATE.alerts);

            const newFields = await window.ApiService.getFieldsByUser(CONFIG.userId);
            if (newFields.length !== STATE.fields.length) {
                STATE.fields = newFields;
                displayFields(STATE.fields);
                updateFieldsCount(STATE.fields.length);
            }
        } else {
            console.log('🔄 Auto-refresh (WS inactive): full reload');
            await loadAllData();
        }
    }, CONFIG.refreshInterval);
}