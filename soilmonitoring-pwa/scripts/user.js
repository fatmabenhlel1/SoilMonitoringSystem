// =====================================================
// AGROMONITOR - USER DASHBOARD (ES6 MODULES)
// Real-Time Monitoring for Regular Users
// =====================================================

console.log('🌱 AgroMonitor User Dashboard v2.0 (ES6 Modules)');
console.log('📡 Real-Time Updates: Active');
console.log('📦 Loading modules...');

// =====================================================
// IMPORTS
// =====================================================

// Core modules (shared with admin)
import { CONFIG, STATE } from './modules/config.module.js';
import { wsManager } from './modules/websocket.module.js';
import { escapeHtml, formatTimeAgo, showError, showSuccess } from './modules/utils.module.js';
import { displayFields } from './modules/fields.module.js';
import { displayAlerts, updateNotificationBadge } from './modules/alerts.module.js';
import { displayPrediction } from './modules/predictions.module.js';
import { loadDynamicStyles } from './modules/styles.module.js';

// WebSocket handlers (reuse from admin or create user-specific)
import { wsManager as WS } from './modules/websocket.module.js';

// User-specific modules
import { checkUserAuthentication, requestNotificationPermission, setupLogoutHandler, logout } from './modules/user-auth.module.js';
import { initializeDashboard, setupAutoRefresh, refreshDashboard, loadAllData } from './modules/user-dashboard.module.js';
import { initializeCharts, updateChartsWithHistoricalData, updateChartsRealtime, refreshChartsData } from './modules/charts.module.js';
import { updateStatistics } from './modules/statistics.module.js';
import {
    displayFertilizerPrediction,
    getFertilizerPredictionForField,
    viewHistoricalData,
    viewAllNotifications
} from './modules/user-actions.module.js';

// Actions (reuse from admin with modifications)
import {
    markAlertAsRead,
    viewFieldDetails,
    viewFieldSensors,
    getPredictionForField
} from './modules/admin-actions.module.js';

// =====================================================
// WEBSOCKET INITIALIZATION
// =====================================================

function initializeWebSocket() {
    console.log('🔌 Connecting to WebSocket...');
    wsManager.connect();
    STATE.wsConnected = true;

    // Handle SENSOR_DATA updates
    wsManager.on('SENSOR_DATA', (wsData) => {
        console.log('🔄 Real-time sensor update received:', wsData);

        if (!wsData || !wsData.fieldId) {
            console.warn('⚠️ Invalid sensor data received:', wsData);
            return;
        }

        const userField = STATE.fields.find(f => f.id === wsData.fieldId);
        if (!userField) return;

        const reading = {
            fieldId: wsData.fieldId,
            data: {
                temperature: wsData.temperature,
                humidity: wsData.humidity,
                soilMoisture: wsData.soilMoisture,
                rainfall: wsData.rainfall || 0,
                nitrogen: wsData.nitrogen,
                phosphorus: wsData.phosphorus,
                potassium: wsData.potassium,
                pH: wsData.pH
            },
            timestamp: wsData.timestamp || new Date().toISOString()
        };

        // Import updateFieldSensorData
        import('./modules/fields.module.js').then(({ updateFieldSensorData }) => {
            updateFieldSensorData(wsData.fieldId, reading);
        });

        // Update charts in real-time
        updateChartsRealtime(reading);
    });

    // Handle ALERT notifications
    wsManager.on('ALERT', (alert) => {
        console.log('🔔 New alert received:', alert);

        const userField = STATE.fields.find(f => f.id === alert.fieldId);
        if (!userField) return;

        STATE.alerts.unshift(alert);
        displayAlerts(STATE.alerts);
        updateNotificationBadge(STATE.alerts);
        updateStatistics(STATE.fields, STATE.alerts, STATE.predictions);

        if (Notification.permission === 'granted') {
            const title = alert.severity === 'high' ? '🚨 Urgent Alert' :
                alert.severity === 'medium' ? '⚠️ Warning' : 'ℹ️ Notice';
            new Notification(title, {
                body: `${alert.message}`,
                icon: '../images/icons/icon-192x192.png'
            });
        }

        if (CONFIG.notificationSound) {
            import('./modules/utils.module.js').then(({ playNotificationSound }) => {
                playNotificationSound();
            });
        }
    });

    // Handle PREDICTION updates
    wsManager.on('PREDICTION', (prediction) => {
        console.log('🤖 New prediction received:', prediction);

        const userField = STATE.fields.find(f => f.id === prediction.fieldId);
        if (!userField) return;

        if (prediction.predictionType === 'fertilizer') {
            displayFertilizerPrediction(prediction);
            showSuccess(`New fertilizer recommendation: ${prediction.result.recommendation}`);
        } else {
            displayPrediction(prediction);
            showSuccess(`New crop prediction: ${prediction.result.recommendation}`);
        }

        if (Notification.permission === 'granted') {
            const title = prediction.predictionType === 'fertilizer' ? '🧪 Fertilizer' : '🌱 Crop Prediction';
            new Notification(title, {
                body: `Recommended: ${prediction.result.recommendation}`,
                icon: '../images/icons/icon-192x192.png'
            });
        }
    });

    // Handle connection status
    wsManager.on('connected', () => {
        console.log('✅ WebSocket connected');
        STATE.wsConnected = true;
    });

    wsManager.on('disconnected', () => {
        console.log('❌ WebSocket disconnected');
        STATE.wsConnected = false;
    });

    wsManager.on('error', (error) => {
        console.error('❌ WebSocket error:', error);
        showError('Real-time connection error. Retrying...');
    });
}

// =====================================================
// GLOBAL WINDOW EXPORTS
// =====================================================

window.markAlertAsRead = markAlertAsRead;
window.viewFieldDetails = viewFieldDetails;
window.viewFieldSensors = viewFieldSensors;
window.getPredictionForField = getPredictionForField;
window.getFertilizerPredictionForField = getFertilizerPredictionForField;
window.viewHistoricalData = viewHistoricalData;
window.viewAllNotifications = viewAllNotifications;
window.refreshDashboard = refreshDashboard;
window.refreshChartsData = () => refreshChartsData(STATE.fields);
window.logout = logout;

// =====================================================
// INITIALIZATION
// =====================================================

window.addEventListener('load', async function() {
    console.log('🚀 Initializing user dashboard...');

    // Load dynamic styles
    loadDynamicStyles();

    // Check authentication
    if (!checkUserAuthentication()) return;

    // Request notification permission
    requestNotificationPermission();

    // Initialize dashboard
    await initializeDashboard();

    // Initialize charts
    initializeCharts();

    // Load chart data for first field
    if (STATE.fields.length > 0) {
        await updateChartsWithHistoricalData(STATE.fields[0].id);
    }

    // Initialize WebSocket
    initializeWebSocket();

    // Setup auto-refresh
    setupAutoRefresh();

    // Setup logout handler
    setupLogoutHandler();

    console.log('✅ User Dashboard initialized successfully');
});

// =====================================================
// EXPORT FOR OTHER MODULES
// =====================================================

export { CONFIG, STATE };