/**
 * WebSocket Handlers Module
 * Handles real-time WebSocket events for admin dashboard
 */

import { STATE, CONFIG } from './config.module.js';
import { updateFieldSensorData } from './fields.module.js';
import { displayAlerts, updateNotificationBadge } from './alerts.module.js';
import { displayPrediction } from './predictions.module.js';
import { showSuccess, showError, showToast, showWarning, playNotificationSound, getFieldName, checkDataHealth, getSeverityConfig } from './utils.module.js';
import { wsManager } from './websocket.module.js';

/**
 * Initialize WebSocket with event handlers
 */
export function initializeWebSocket() {
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

        updateFieldSensorData(wsData.fieldId, reading);
        showSensorUpdateNotification(wsData.fieldId, reading.data);
    });

    // Handle ALERT notifications
    wsManager.on('ALERT', (alert) => {
        console.log('🔔 New alert received:', alert);

        STATE.alerts.unshift(alert);
        displayAlerts(STATE.alerts);
        updateNotificationBadge(STATE.alerts);
        showAlertNotification(alert);

        if (CONFIG.notificationSound) {
            playNotificationSound();
        }
    });

    // Handle PREDICTION updates
    wsManager.on('PREDICTION', (prediction) => {
        console.log('🤖 New prediction received:', prediction);

        displayPrediction(prediction);
        showSuccess(`New crop prediction for ${getFieldName(prediction.fieldId)}: ${prediction.result.recommendation}`);

        if (Notification.permission === 'granted') {
            new Notification('🌱 Crop Prediction Ready', {
                body: `Recommended: ${prediction.result.recommendation}`,
                icon: '../images/icons/icon-192x192.png'
            });
        }
    });

    // Handle connection status
    wsManager.on('connected', () => {
        console.log('✅ WebSocket connected');
        STATE.wsConnected = true;
        updateConnectionStatus(true);
    });

    wsManager.on('disconnected', () => {
        console.log('❌ WebSocket disconnected');
        STATE.wsConnected = false;
        updateConnectionStatus(false);
    });

    wsManager.on('error', (error) => {
        console.error('❌ WebSocket error:', error);
        showError('Real-time connection error. Retrying...');
    });
}

/**
 * Show sensor update notification
 */
function showSensorUpdateNotification(fieldId, data) {
    const fieldName = getFieldName(fieldId);
    const isWarning = !checkDataHealth(data);

    if (isWarning) {
        showWarning(`⚠️ Unusual readings detected in ${fieldName}`);
    } else {
        const statusBadge = document.getElementById(`status-${fieldId}`);
        if (statusBadge) {
            statusBadge.classList.add('pulse-animation');
            setTimeout(() => statusBadge.classList.remove('pulse-animation'), 1000);
        }
    }
}

/**
 * Show alert notification
 */
function showAlertNotification(alert) {
    const fieldName = getFieldName(alert.fieldId);

    if (Notification.permission === 'granted') {
        const title = alert.severity === 'high' ? '🚨 Urgent Alert' :
            alert.severity === 'medium' ? '⚠️ Warning' : 'ℹ️ Notice';

        new Notification(title, {
            body: `${fieldName}: ${alert.message}`,
            icon: '../images/icons/icon-192x192.png',
            requireInteraction: alert.severity === 'high',
            tag: alert.id
        });
    }

    showToast(alert.message, alert.severity);
}

/**
 * Update connection status indicator
 */
function updateConnectionStatus(connected) {
    let indicator = document.getElementById('ws-connection-indicator');

    if (!indicator) {
        indicator = document.createElement('div');
        indicator.id = 'ws-connection-indicator';
        indicator.className = 'ws-indicator';
        document.querySelector('.header-actions')?.prepend(indicator);
    }

    if (connected) {
        indicator.innerHTML = '<i class="fas fa-circle text-success"></i> <span class="small">Live</span>';
        indicator.className = 'ws-indicator connected';
    } else {
        indicator.innerHTML = '<i class="fas fa-circle text-danger"></i> <span class="small">Offline</span>';
        indicator.className = 'ws-indicator disconnected';
    }
}