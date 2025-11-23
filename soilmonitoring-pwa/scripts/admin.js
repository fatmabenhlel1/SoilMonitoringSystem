// =====================================================
// AGROMONITOR - ADMIN DASHBOARD
// API Integration with Real-Time Updates
// =====================================================

console.log('🌱 AgroMonitor Admin Dashboard v2.0');
console.log('📡 API Integration: Active');

// =====================================================
// GLOBAL VARIABLES
// =====================================================

const CONFIG = {
    userId: 'test-user-001', // TODO: Replace with real userId from IAM
    refreshInterval: 30000,  // 30 seconds auto-refresh
    notificationSound: true
};

let STATE = {
    currentUser: null,
    fields: [],
    alerts: [],
    sensors: [],
    selectedFieldId: null,
    wsConnected: false
};

// =====================================================
// AUTHENTICATION & INITIALIZATION
// =====================================================

window.addEventListener('load', async function() {
    console.log('🚀 Initializing dashboard...');

    // Check authentication
    if (!checkAuthentication()) return;

    // Request notification permission
    requestNotificationPermission();

    // Initialize dashboard
    await initializeDashboard();

    // Setup auto-refresh
    setupAutoRefresh();

    // Setup logout handler
    setupLogoutHandler();
});

function checkAuthentication() {
    const userStr = sessionStorage.getItem('currentUser') || localStorage.getItem('currentUser');

    if (!userStr) {
        console.warn('⚠️ No user session found. Redirecting to home...');
        window.location.href = '../index.html';
        return false;
    }

    STATE.currentUser = JSON.parse(userStr);

    // Check if user is Administrator
    if (STATE.currentUser.role !== 'Administrator') {
        console.warn('⚠️ User is not an Administrator. Redirecting...');
        window.location.href = 'user.html';
        return false;
    }

    console.log('👤 User:', STATE.currentUser.name, '| Role:', STATE.currentUser.role);

    // Update UI with user name
    document.getElementById('user-name').textContent = STATE.currentUser.name;

    return true;
}

function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
            console.log('🔔 Notification permission:', permission);
        });
    }
}

// =====================================================
// DASHBOARD INITIALIZATION
// =====================================================

async function initializeDashboard() {
    try {
        showLoading(true);

        // Step 1: Check API health
        await checkAPIHealth();

        // Step 2: Load all dashboard data
        await loadAllData();

        // Step 3: Initialize WebSocket for real-time updates
        initializeWebSocket();

        showLoading(false);
        showSuccess('Dashboard loaded successfully!');

    } catch (error) {
        console.error('❌ Dashboard initialization failed:', error);
        showError('Failed to load dashboard. Please ensure the API is running on http://localhost:8080');
        showLoading(false);
    }
}

async function checkAPIHealth() {
    console.log('🏥 Checking API health...');
    const health = await ApiService.checkHealth();
    console.log('✅ API Status:', health.status);
}

async function loadAllData() {
    console.log('📥 Loading dashboard data...');

    // Load fields
    console.log('📍 Loading fields...');
    STATE.fields = await ApiService.getFieldsByUser(CONFIG.userId);
    console.log(`✅ Loaded ${STATE.fields.length} fields`);
    displayFields(STATE.fields);
    updateFieldsCount(STATE.fields.length);

    // Load alerts
    console.log('🔔 Loading alerts...');
    STATE.alerts = await ApiService.getAlertsByUser(CONFIG.userId);
    console.log(`✅ Loaded ${STATE.alerts.length} alerts`);
    displayAlerts(STATE.alerts);
    updateNotificationBadge(STATE.alerts);

    // Load sensor data for each field
    if (STATE.fields.length > 0) {
        STATE.selectedFieldId = STATE.fields[0].id;
        await loadFieldsSensorData();
    }
}

async function loadFieldsSensorData() {
    console.log('🔌 Loading sensor data for all fields...');

    for (const field of STATE.fields) {
        try {
            const reading = await ApiService.getLatestReading(field.id);
            updateFieldSensorData(field.id, reading);
        } catch (error) {
            console.warn(`⚠️ No sensor data for field: ${field.name}`);
            updateFieldSensorData(field.id, null);
        }
    }
}

// =====================================================
// DISPLAY FUNCTIONS - FIELDS
// =====================================================

function displayFields(fields) {
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
            
            <!-- Sensor Data Grid -->
            <div class="sensor-grid" id="sensors-grid-${field.id}">
                <div class="text-center py-3">
                    <div class="spinner-border spinner-border-sm text-success" role="status"></div>
                    <span class="ms-2 text-muted small">Loading sensors...</span>
                </div>
            </div>
            
            <!-- Action Buttons -->
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

function updateFieldSensorData(fieldId, reading) {
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

    // Update status badge
    if (statusBadge) {
        statusBadge.innerHTML = isNormal
            ? '<span class="badge bg-success">Normal</span>'
            : '<span class="badge bg-warning">Warning</span>';
    }

    // Display sensor data
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
            <div class="sensor-value">${data.ph.toFixed(1)}</div>
            <div class="sensor-label">pH Level</div>
        </div>
    `;
}

function checkDataHealth(data) {
    return (
        data.temperature >= 15 && data.temperature <= 35 &&
        data.humidity >= 30 && data.humidity <= 85 &&
        data.nitrogen >= 20 && data.nitrogen <= 100 &&
        data.phosphorus >= 10 && data.phosphorus <= 80 &&
        data.potassium >= 50 && data.potassium <= 200 &&
        data.ph >= 5.5 && data.ph <= 7.5
    );
}

function updateFieldsCount(count) {
    const badge = document.getElementById('fieldsCount');
    if (badge) {
        badge.textContent = `${count} Field${count !== 1 ? 's' : ''}`;
    }
}

// =====================================================
// DISPLAY FUNCTIONS - ALERTS
// =====================================================

function displayAlerts(alerts) {
    const container = document.getElementById('alertsContainer');

    if (!alerts || alerts.length === 0) {
        container.innerHTML = `
            <div class="card p-4 text-center">
                <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
                <h6 class="text-success">All Clear!</h6>
                <p class="text-muted small mb-0">No alerts at the moment</p>
            </div>
        `;
        return;
    }

    // Sort alerts by severity and date
    const sortedAlerts = [...alerts].sort((a, b) => {
        const severityOrder = { high: 3, medium: 2, low: 1 };
        const severityDiff = severityOrder[b.severity] - severityOrder[a.severity];
        if (severityDiff !== 0) return severityDiff;
        return new Date(b.createdAt) - new Date(a.createdAt);
    });

    container.innerHTML = sortedAlerts.slice(0, 10).map(alert => {
        const config = getSeverityConfig(alert.severity);
        const timeAgo = formatTimeAgo(alert.createdAt);
        const fieldName = getFieldName(alert.fieldId);

        return `
            <div class="recommendation-card ${alert.isRead ? 'opacity-75' : ''}">
                <div class="recommendation-header">
                    <div class="recommendation-icon ${config.class}">
                        <i class="fas fa-${config.icon}"></i>
                    </div>
                    <div class="flex-grow-1">
                        <div class="recommendation-title">
                            ${escapeHtml(alert.alertType.replace(/_/g, ' ').toUpperCase())}
                        </div>
                        <small class="text-muted">${escapeHtml(fieldName)}</small>
                    </div>
                </div>
                <p class="mb-2 small">${escapeHtml(alert.message)}</p>
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted"><i class="far fa-clock me-1"></i>${timeAgo}</small>
                    ${!alert.isRead ? `
                        <button class="btn btn-sm btn-link p-0 text-decoration-none" 
                                onclick="markAlertAsRead('${alert.id}')">
                            <i class="fas fa-check me-1"></i>Mark as read
                        </button>
                    ` : '<small class="text-muted"><i class="fas fa-check-circle me-1"></i>Read</small>'}
                </div>
            </div>
        `;
    }).join('');
}

function updateNotificationBadge(alerts) {
    const dot = document.getElementById('notificationDot');
    const unreadCount = alerts.filter(a => !a.isRead).length;

    if (dot) {
        dot.style.display = unreadCount > 0 ? 'block' : 'none';
    }

    updateNotificationsDropdown(alerts);
}

function updateNotificationsDropdown(alerts) {
    const container = document.getElementById('notificationsList');
    if (!container) return;

    if (!alerts || alerts.length === 0) {
        container.innerHTML = '<p class="text-center text-muted py-3">No notifications</p>';
        return;
    }

    const recentAlerts = alerts.slice(0, 5);
    container.innerHTML = recentAlerts.map(alert => {
        const config = getSeverityConfig(alert.severity);
        const timeAgo = formatTimeAgo(alert.createdAt);

        return `
            <div class="notification-item d-flex gap-3">
                <div class="notification-icon ${config.class}">
                    <i class="fas fa-${config.icon}"></i>
                </div>
                <div class="flex-grow-1">
                    <div class="fw-semibold">${escapeHtml(alert.alertType.replace(/_/g, ' '))}</div>
                    <div class="small text-muted">${escapeHtml(alert.message)}</div>
                    <div class="small text-muted">${timeAgo}</div>
                </div>
            </div>
        `;
    }).join('');
}

function getSeverityConfig(severity) {
    const configs = {
        high: { class: 'alert', icon: 'exclamation-triangle', color: 'danger' },
        medium: { class: 'info', icon: 'info-circle', color: 'warning' },
        low: { class: 'success', icon: 'check-circle', color: 'info' }
    };
    return configs[severity] || configs.low;
}

// =====================================================
// DISPLAY FUNCTIONS - PREDICTIONS
// =====================================================

function displayPrediction(prediction) {
    const container = document.getElementById('predictionsContainer');

    const details = prediction.result.details || [];
    const confidence = (prediction.confidence * 100).toFixed(1);

    container.innerHTML = `
        <div class="card p-3 border-success">
            <div class="d-flex align-items-center mb-3">
                <i class="fas fa-seedling fa-2x text-success me-3"></i>
                <div>
                    <h6 class="mb-0">Recommended Crop</h6>
                    <h5 class="mb-0 text-success fw-bold">${escapeHtml(prediction.result.recommendation)}</h5>
                </div>
            </div>
            
            <div class="mb-2">
                <small class="text-muted">Confidence Score</small>
                <div class="progress" style="height: 20px;">
                    <div class="progress-bar bg-success" role="progressbar" 
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

// =====================================================
// ACTION HANDLERS
// =====================================================

async function markAlertAsRead(alertId) {
    try {
        await ApiService.markAlertAsRead(alertId);
        console.log('✅ Alert marked as read:', alertId);

        // Reload alerts
        STATE.alerts = await ApiService.getAlertsByUser(CONFIG.userId);
        displayAlerts(STATE.alerts);
        updateNotificationBadge(STATE.alerts);

    } catch (error) {
        console.error('❌ Error marking alert as read:', error);
        showError('Failed to mark alert as read');
    }
}

async function viewFieldDetails(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('fieldDetailsModal'));
        const content = document.getElementById('fieldDetailsContent');

        // Show loading
        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-success"></div></div>';
        modal.show();

        // Load data
        const field = await ApiService.getFieldById(fieldId);
        const readings = await ApiService.getReadingsByField(fieldId);
        const sensors = await ApiService.getSensorsByField(fieldId);

        // Display content
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

async function viewFieldSensors(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('sensorsModal'));
        const content = document.getElementById('sensorsModalContent');

        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-info"></div></div>';
        modal.show();

        const sensors = await ApiService.getSensorsByField(fieldId);
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

async function getPredictionForField(fieldId) {
    try {
        showLoading(true);

        const reading = await ApiService.getLatestReading(fieldId);

        if (!reading || !reading.data) {
            throw new Error('No sensor data available for prediction');
        }

        const prediction = await ApiService.predictCrop(fieldId, {
            nitrogen: reading.data.nitrogen,
            phosphorus: reading.data.phosphorus,
            potassium: reading.data.potassium,
            temperature: reading.data.temperature,
            humidity: reading.data.humidity,
            ph: reading.data.ph,
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

async function requestCropPrediction() {
    if (STATE.fields.length === 0) {
        showError('No fields available. Please add a field first.');
        return;
    }

    const fieldId = STATE.selectedFieldId || STATE.fields[0].id;
    await getPredictionForField(fieldId);
}

async function refreshDashboard() {
    console.log('🔄 Refreshing dashboard...');
    showSuccess('Refreshing dashboard data...');
    await loadAllData();
}

// =====================================================
// WEBSOCKET REAL-TIME UPDATES
// =====================================================

function initializeWebSocket() {
    if (typeof wsManager === 'undefined') {
        console.warn('⚠️ WebSocket manager not available');
        return;
    }

    console.log('🔌 Connecting to WebSocket...');
    wsManager.connect();
    STATE.wsConnected = true;

    // Sensor data updates
    wsManager.on('SENSOR_DATA', async (data) => {
        console.log('🔄 Real-time sensor update received');
        if (data.fieldId) {
            const reading = await ApiService.getLatestReading(data.fieldId);
            updateFieldSensorData(data.fieldId, reading);
        }
    });

    // New alerts
    wsManager.on('ALERT', async (alert) => {
        console.log('🔔 New alert received:', alert.message);

        STATE.alerts = await ApiService.getAlertsByUser(CONFIG.userId);
        displayAlerts(STATE.alerts);
        updateNotificationBadge(STATE.alerts);

        // Show browser notification
        if (Notification.permission === 'granted') {
            new Notification('🚨 Soil Alert', {
                body: alert.message,
                icon: '../images/icons/icon-192x192.png',
                requireInteraction: alert.severity === 'high'
            });
        }
    });

    // New predictions
    wsManager.on('PREDICTION', (prediction) => {
        console.log('🤖 New prediction received');
        displayPrediction(prediction);
        showSuccess('New crop prediction available!');
    });
}

// =====================================================
// UTILITY FUNCTIONS
// =====================================================

function getFieldName(fieldId) {
    const field = STATE.fields.find(f => f.id === fieldId);
    return field ? field.name : 'Unknown Field';
}

function formatTimeAgo(timestamp) {
    const now = new Date();
    const past = new Date(timestamp);
    const diffMs = now - past;
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Just now';
    if (diffMins === 1) return '1 minute ago';
    if (diffMins < 60) return `${diffMins} minutes ago`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours === 1) return '1 hour ago';
    if (diffHours < 24) return `${diffHours} hours ago`;

    const diffDays = Math.floor(diffHours / 24);
    if (diffDays === 1) return '1 day ago';
    return `${diffDays} days ago`;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = show ? 'flex' : 'none';
    }
}

function showError(message) {
    const alert = document.getElementById('errorAlert');
    const messageEl = document.getElementById('errorMessage');

    if (alert && messageEl) {
        messageEl.textContent = message;
        alert.style.display = 'block';
        setTimeout(() => alert.style.display = 'none', 5000);
    }
}

function hideError() {
    const alert = document.getElementById('errorAlert');
    if (alert) alert.style.display = 'none';
}

function showSuccess(message) {
    const alert = document.getElementById('successAlert');
    const messageEl = document.getElementById('successMessage');

    if (alert && messageEl) {
        messageEl.textContent = message;
        alert.style.display = 'block';
        setTimeout(() => alert.style.display = 'none', 3000);
    }
}

function hideSuccess() {
    const alert = document.getElementById('successAlert');
    if (alert) alert.style.display = 'none';
}

// =====================================================
// AUTO-REFRESH & LOGOUT
// =====================================================

function setupAutoRefresh() {
    setInterval(async () => {
        console.log('🔄 Auto-refresh triggered');
        await loadAllData();
    }, CONFIG.refreshInterval);
}

function setupLogoutHandler() {
    document.getElementById('signout')?.addEventListener('click', logout);
}

function logout() {
    console.log('👋 Logging out...');

    sessionStorage.removeItem('currentUser');
    localStorage.removeItem('currentUser');

    if (STATE.wsConnected && typeof wsManager !== 'undefined') {
        wsManager.disconnect();
    }

    window.location.href = '../index.html';
}

// =====================================================
// PLACEHOLDER FUNCTIONS (TO IMPLEMENT)
// =====================================================

function showAddFieldModal() {
    showError('Add Field functionality - Coming soon!');
}

function showAddSensorModal() {
    showError('Add Sensor functionality - Coming soon!');
}

function viewAllNotifications(event) {
    event.preventDefault();
    showError('View All Notifications - Coming soon!');
}

function exportData() {
    showError('Export Data functionality - Coming soon!');
}

// =====================================================
// END OF ADMIN DASHBOARD
// =====================================================

console.log('✅ Admin Dashboard loaded successfully');