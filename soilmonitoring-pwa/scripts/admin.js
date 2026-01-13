// =====================================================
// AGROMONITOR - ADMIN DASHBOARD (ES6 MODULES)
// API Integration with Real-Time Updates
// =====================================================

console.log('🌱 AgroMonitor Admin Dashboard v2.0 (ES6 Modules)');
console.log('📡 API Integration: Active');
console.log('📦 Loading modules...');

// =====================================================
// IMPORTS
// =====================================================

import { CONFIG, STATE } from './modules/config.module.js';
import { checkAdminAuthentication, setupLogoutHandler, logout } from './modules/auth.module.js';
import { initializeAdminDashboard, refreshDashboard, setupAutoRefresh, loadAllData } from './modules/admin-dashboard.module.js';
import {
    markAlertAsRead,
    viewFieldDetails,
    viewFieldSensors,
    getPredictionForField,
    requestCropPrediction,
    showAddFieldModal,
    showAddSensorModal,
    viewAllNotifications,
    exportData
} from './modules/admin-actions.module.js';
import { initializeWebSocket } from './modules/websocket-handlers.module.js';
import { loadDynamicStyles } from './modules/styles.module.js';
import { displayAlerts, updateNotificationBadge } from './modules/alerts.module.js';

// =====================================================
// GLOBAL WINDOW EXPORTS
// =====================================================
// Export functions to window for HTML onclick handlers

window.markAlertAsRead = markAlertAsRead;
window.viewFieldDetails = viewFieldDetails;
window.viewFieldSensors = viewFieldSensors;
window.getPredictionForField = getPredictionForField;
window.requestCropPrediction = requestCropPrediction;
window.showAddFieldModal = showAddFieldModal;
window.showAddSensorModal = showAddSensorModal;
window.viewAllNotifications = viewAllNotifications;
window.exportData = exportData;
window.refreshDashboard = refreshDashboard;
window.logout = logout;

// =====================================================
// INITIALIZATION
// =====================================================

window.addEventListener('load', async function() {
    console.log('🚀 Initializing admin dashboard...');

    // Load dynamic styles
    loadDynamicStyles();

    // Check authentication
    if (!checkAdminAuthentication()) return;

    // Initialize admin dashboard
    await initializeAdminDashboard();

    // Initialize WebSocket
    initializeWebSocket();

    // Setup auto-refresh
    setupAutoRefresh();

    // Setup logout handler
    setupLogoutHandler();

    console.log('✅ Admin Dashboard initialized successfully');
});

// =====================================================
// EXPORT FOR OTHER MODULES
// =====================================================

export { CONFIG, STATE };