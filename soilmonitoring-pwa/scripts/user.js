// =====================================================
// AGROMONITOR - USER DASHBOARD (PART 1)
// Real-Time Monitoring for Regular Users
// =====================================================

console.log('🌱 AgroMonitor User Dashboard v1.0');
console.log('📡 Real-Time Updates: Active');

// =====================================================
// GLOBAL VARIABLES
// =====================================================

const CONFIG = {
    userId: '6912504d2900a86edfa65db5',
    refreshInterval: 300000,  // 5 minutes (reduced because WebSocket handles real-time)
    notificationSound: true,
    chartUpdateInterval: 60000 // Update charts every minute
};

let STATE = {
    currentUser: null,
    fields: [],
    alerts: [],
    sensors: [],
    predictions: [],
    selectedFieldId: null,
    wsConnected: false,
    charts: {} // Store chart instances
};
// Add wsManager
const wsManager = {
    ws: null,
    listeners: {},
    reconnectTimeout: null,

    connect() {
        console.log('🔌 Connecting to WebSocket...');
        this.ws = new WebSocket('ws://api.soilmonitoring.me:8080/ws/sensor-data');

        this.ws.onopen = () => {
            console.log('✅ WebSocket Connected');
            this.trigger('connected');
        };

        this.ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            if (data.type) {
                this.trigger(data.type, data.payload);
            }
        };

        this.ws.onerror = (error) => {
            console.error('❌ WebSocket Error:', error);
            this.trigger('error', error);
        };

        this.ws.onclose = () => {
            console.log('❌ WebSocket Disconnected');
            this.trigger('disconnected');
            this.reconnectTimeout = setTimeout(() => this.connect(), 5000);
        };
    },

    on(eventType, callback) {
        if (!this.listeners[eventType]) {
            this.listeners[eventType] = [];
        }
        this.listeners[eventType].push(callback);
    },

    trigger(eventType, data) {
        if (this.listeners[eventType]) {
            this.listeners[eventType].forEach(callback => callback(data));
        }
    },

    disconnect() {
        if (this.reconnectTimeout) clearTimeout(this.reconnectTimeout);
        if (this.ws) this.ws.close();
    }
};
// =====================================================
// CHARTS CONFIGURATION
// =====================================================

let CHARTS = {
    temperature: null,
    humidity: null,
    npk: null,
    moisture: null,
    ph: null,
    rainfall: null
};

// Initialize all charts
function initializeCharts() {
    console.log('📊 Initializing charts...');

    // Temperature Chart
    const tempCtx = document.getElementById('temperatureChart');
    if (tempCtx) {
        CHARTS.temperature = new Chart(tempCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Temperature (°C)',
                    data: [],
                    borderColor: 'rgb(255, 99, 132)',
                    backgroundColor: 'rgba(255, 99, 132, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        enabled: true,
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        title: {
                            display: true,
                            text: 'Temperature (°C)',
                            font: { size: 12, weight: 'bold' }
                        },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: {
                        grid: { display: false }
                    }
                }
            }
        });
    }

    // Humidity Chart
    const humidityCtx = document.getElementById('humidityChart');
    if (humidityCtx) {
        CHARTS.humidity = new Chart(humidityCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Humidity (%)',
                    data: [],
                    borderColor: 'rgb(54, 162, 235)',
                    backgroundColor: 'rgba(54, 162, 235, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: { display: true, text: 'Humidity (%)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // NPK Bar Chart
    const npkCtx = document.getElementById('npkChart');
    if (npkCtx) {
        CHARTS.npk = new Chart(npkCtx, {
            type: 'bar',
            data: {
                labels: ['Nitrogen (N)', 'Phosphorus (P)', 'Potassium (K)'],
                datasets: [{
                    label: 'NPK Levels (mg/kg)',
                    data: [0, 0, 0],
                    backgroundColor: [
                        'rgba(75, 192, 192, 0.7)',
                        'rgba(255, 206, 86, 0.7)',
                        'rgba(153, 102, 255, 0.7)'
                    ],
                    borderColor: [
                        'rgb(75, 192, 192)',
                        'rgb(255, 206, 86)',
                        'rgb(153, 102, 255)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'mg/kg', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // Soil Moisture Chart
    const moistureCtx = document.getElementById('moistureChart');
    if (moistureCtx) {
        CHARTS.moisture = new Chart(moistureCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Soil Moisture (%)',
                    data: [],
                    borderColor: 'rgb(139, 195, 74)',
                    backgroundColor: 'rgba(139, 195, 74, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: { display: true, text: 'Moisture (%)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // pH Chart
    const phCtx = document.getElementById('phChart');
    if (phCtx) {
        CHARTS.ph = new Chart(phCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'pH Level',
                    data: [],
                    borderColor: 'rgb(255, 159, 64)',
                    backgroundColor: 'rgba(255, 159, 64, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        min: 0,
                        max: 14,
                        title: { display: true, text: 'pH Level', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // Rainfall Chart
    const rainfallCtx = document.getElementById('rainfallChart');
    if (rainfallCtx) {
        CHARTS.rainfall = new Chart(rainfallCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Rainfall (mm)',
                    data: [],
                    backgroundColor: 'rgba(33, 150, 243, 0.7)',
                    borderColor: 'rgb(33, 150, 243)',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'Rainfall (mm)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    console.log('✅ Charts initialized');
}

// Update charts with historical data
async function updateChartsWithHistoricalData(fieldId) {
    try {
        console.log('📊 Loading chart data for field:', fieldId);

        // Get readings from the API
        const readings = await ApiService.getReadingsByField(fieldId);

        if (!readings || readings.length === 0) {
            console.warn('⚠️ No historical data available for charts');
            document.getElementById('noChartsData').style.display = 'block';
            return;
        }

        document.getElementById('noChartsData').style.display = 'none';

        // Sort by timestamp and get last 20 readings
        readings.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
        const recentReadings = readings.slice(-20);

        // Extract data
        const labels = recentReadings.map(r => {
            const date = new Date(r.timestamp);
            return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        });

        const temperatures = recentReadings.map(r => r.data.temperature);
        const humidity = recentReadings.map(r => r.data.humidity);
        const moisture = recentReadings.map(r => r.data.soilMoisture);
        const ph = recentReadings.map(r => r.data.pH);
        const rainfall = recentReadings.map(r => r.data.rainfall);

        // Update Temperature Chart
        if (CHARTS.temperature) {
            CHARTS.temperature.data.labels = labels;
            CHARTS.temperature.data.datasets[0].data = temperatures;
            CHARTS.temperature.update();
        }

        // Update Humidity Chart
        if (CHARTS.humidity) {
            CHARTS.humidity.data.labels = labels;
            CHARTS.humidity.data.datasets[0].data = humidity;
            CHARTS.humidity.update();
        }

        // Update Soil Moisture Chart
        if (CHARTS.moisture) {
            CHARTS.moisture.data.labels = labels;
            CHARTS.moisture.data.datasets[0].data = moisture;
            CHARTS.moisture.update();
        }

        // Update pH Chart
        if (CHARTS.ph) {
            CHARTS.ph.data.labels = labels;
            CHARTS.ph.data.datasets[0].data = ph;
            CHARTS.ph.update();
        }

        // Update Rainfall Chart
        if (CHARTS.rainfall) {
            CHARTS.rainfall.data.labels = labels;
            CHARTS.rainfall.data.datasets[0].data = rainfall;
            CHARTS.rainfall.update();
        }

        // Update NPK with latest reading
        const latestReading = recentReadings[recentReadings.length - 1];
        if (CHARTS.npk && latestReading) {
            CHARTS.npk.data.datasets[0].data = [
                latestReading.data.nitrogen,
                latestReading.data.phosphorus,
                latestReading.data.potassium
            ];
            CHARTS.npk.update();
        }

        console.log('✅ Charts updated with', recentReadings.length, 'data points');

    } catch (error) {
        console.error('❌ Error updating charts:', error);
        document.getElementById('noChartsData').style.display = 'block';
    }
}

// Update charts in real-time (called from WebSocket)
function updateChartsRealtime(reading) {
    const timestamp = new Date(reading.timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });

    // Temperature
    if (CHARTS.temperature) {
        CHARTS.temperature.data.labels.push(timestamp);
        CHARTS.temperature.data.datasets[0].data.push(reading.data.temperature);

        // Keep only last 20 points
        if (CHARTS.temperature.data.labels.length > 20) {
            CHARTS.temperature.data.labels.shift();
            CHARTS.temperature.data.datasets[0].data.shift();
        }

        CHARTS.temperature.update('none');
    }

    // Humidity
    if (CHARTS.humidity) {
        CHARTS.humidity.data.labels.push(timestamp);
        CHARTS.humidity.data.datasets[0].data.push(reading.data.humidity);

        if (CHARTS.humidity.data.labels.length > 20) {
            CHARTS.humidity.data.labels.shift();
            CHARTS.humidity.data.datasets[0].data.shift();
        }

        CHARTS.humidity.update('none');
    }

    // Soil Moisture
    if (CHARTS.moisture) {
        CHARTS.moisture.data.labels.push(timestamp);
        CHARTS.moisture.data.datasets[0].data.push(reading.data.soilMoisture);

        if (CHARTS.moisture.data.labels.length > 20) {
            CHARTS.moisture.data.labels.shift();
            CHARTS.moisture.data.datasets[0].data.shift();
        }

        CHARTS.moisture.update('none');
    }

    // pH
    if (CHARTS.ph) {
        CHARTS.ph.data.labels.push(timestamp);
        CHARTS.ph.data.datasets[0].data.push(reading.data.pH);

        if (CHARTS.ph.data.labels.length > 20) {
            CHARTS.ph.data.labels.shift();
            CHARTS.ph.data.datasets[0].data.shift();
        }

        CHARTS.ph.update('none');
    }

    // NPK (just update latest values)
    if (CHARTS.npk) {
        CHARTS.npk.data.datasets[0].data = [
            reading.data.nitrogen,
            reading.data.phosphorus,
            reading.data.potassium
        ];
        CHARTS.npk.update('none');
    }

    // Rainfall
    if (CHARTS.rainfall) {
        CHARTS.rainfall.data.labels.push(timestamp);
        CHARTS.rainfall.data.datasets[0].data.push(reading.data.rainfall);

        if (CHARTS.rainfall.data.labels.length > 20) {
            CHARTS.rainfall.data.labels.shift();
            CHARTS.rainfall.data.datasets[0].data.shift();
        }

        CHARTS.rainfall.update('none');
    }
}

// Refresh charts manually
async function refreshChartsData() {
    if (STATE.fields.length > 0) {
        showSuccess('Refreshing charts data...');
        await updateChartsWithHistoricalData(STATE.fields[0].id);
    } else {
        showError('No fields available');
    }
}

// =====================================================
// AUTHENTICATION & INITIALIZATION
// =====================================================

window.addEventListener('load', async function() {
    console.log('🚀 Initializing user dashboard...');

    // Check authentication
    if (!checkAuthentication()) return;

    // Request notification permission
    requestNotificationPermission();

    // Initialize dashboard
    await initializeDashboard();

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

    // Check if user is NOT Administrator (regular user)
    if (STATE.currentUser.role === 'Administrator') {
        console.warn('⚠️ User is Administrator. Redirecting to admin panel...');
        window.location.href = 'admin.html';
        return false;
    }

    console.log('👤 User:', STATE.currentUser.name, '| Role:', STATE.currentUser.role);

    // Update UI with user name
    document.getElementById('user-name').textContent = STATE.currentUser.name;
    document.getElementById('welcome-name').textContent = STATE.currentUser.name;

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


        // Step 1: Check API health
        await checkAPIHealth();

        // Step 2: Load all dashboard data
        await loadAllData();

        // ✅ NOUVEAU: Initialize charts
        initializeCharts();

        // ✅ NOUVEAU: Load chart data for first field
        if (STATE.fields.length > 0) {
            await updateChartsWithHistoricalData(STATE.fields[0].id);
        }

        // Step 3: Initialize WebSocket for real-time updates
        initializeWebSocket();

        // Step 4: Setup auto-refresh for static data
        setupAutoRefresh();


        showSuccess('Dashboard loaded - Real-time monitoring active! 🔴');

    } catch (error) {
        console.error('❌ Dashboard initialization failed:', error);
        showError('Failed to load dashboard. Please ensure the API is running on http://api.soilmonitoring.me:8080');

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
    updateStatistics();

    // Load alerts
    console.log('🔔 Loading alerts...');
    STATE.alerts = await ApiService.getAlertsByUser(CONFIG.userId);
    console.log(`✅ Loaded ${STATE.alerts.length} alerts`);
    displayAlerts(STATE.alerts);
    updateNotificationBadge(STATE.alerts);

    // Load predictions
    if (STATE.fields.length > 0) {
        console.log('🤖 Loading predictions...');
        await loadPredictions();
    }

    // Load sensor data for each field (only if WebSocket not connected yet)
    if (STATE.fields.length > 0 && !STATE.wsConnected) {
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

async function loadPredictions() {
    try {
        // Load latest prediction for first field as example
        const fieldId = STATE.fields[0].id;
        const predictions = await ApiService.getPredictionsByField(fieldId);

        if (predictions && predictions.length > 0) {
            STATE.predictions = predictions;
            displayPrediction(predictions[0]); // Show most recent
        }
    } catch (error) {
        console.warn('⚠️ No predictions available yet');
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
                <p class="text-muted">Contact your administrator to get access to fields</p>
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
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-success" onclick="getPredictionForField('${field.id}')" title="Crop Recommendation">
                        <i class="fas fa-seedling me-1"></i>Crop
                    </button>
                    <button class="btn btn-warning" onclick="getFertilizerPredictionForField('${field.id}')" title="Fertilizer Recommendation">
                        <i class="fas fa-flask me-1"></i>Fertilizer
                    </button>
                </div>
                <button class="btn btn-sm btn-info" onclick="viewFieldSensors('${field.id}')">
                    <i class="fas fa-microchip me-1"></i>Sensors
                </button>
                <button class="btn btn-sm btn-warning" onclick="viewHistoricalData('${field.id}')">
                    <i class="fas fa-history me-1"></i>History
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

    // Display sensor data with real-time indicator
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
            <div class="sensor-value">${data.pH ? data.pH.toFixed(1) : 'N/A'}</div>
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
        data.pH >= 5.5 && data.pH <= 7.5
    );
}

// =====================================================
// DISPLAY FUNCTIONS - STATISTICS
// =====================================================

function updateStatistics() {
    // Update field count
    document.getElementById('totalFields').textContent = STATE.fields.length;

    // Count total sensors
    let totalSensors = 0;
    STATE.fields.forEach(field => {
        // We'll count sensors when we load them
        ApiService.getSensorsByField(field.id).then(sensors => {
            totalSensors += sensors.length;
            document.getElementById('totalSensors').textContent = totalSensors;
        }).catch(() => {});
    });

    // Update alerts count
    const unreadAlerts = STATE.alerts.filter(a => !a.isRead).length;
    document.getElementById('totalAlerts').textContent = unreadAlerts;

    // Update predictions count
    document.getElementById('totalPredictions').textContent = STATE.predictions.length;
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
// Display Fertilizer Prediction
function displayFertilizerPrediction(prediction) {
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
// ACTION HANDLERS
// =====================================================

async function markAlertAsRead(alertId) {
    try {
        await ApiService.markAlertAsRead(alertId);
        console.log('✅ Alert marked as read:', alertId);

        STATE.alerts = await ApiService.getAlertsByUser(CONFIG.userId);
        displayAlerts(STATE.alerts);
        updateNotificationBadge(STATE.alerts);
        updateStatistics();
    } catch (error) {
        console.error('❌ Error marking alert as read:', error);
        showError('Failed to mark alert as read');
    }
}

async function viewFieldDetails(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('fieldDetailsModal'));
        const content = document.getElementById('fieldDetailsContent');

        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-success"></div></div>';
        modal.show();

        const field = await ApiService.getFieldById(fieldId);
        const readings = await ApiService.getReadingsByField(fieldId);
        const sensors = await ApiService.getSensorsByField(fieldId);

        content.innerHTML = `
            <h5><i class="fas fa-map-marker-alt me-2"></i>${escapeHtml(field.name)}</h5>
            <p class="text-muted">${escapeHtml(field.location.address)}</p>
            <hr>
            <div class="row mb-3">
                <div class="col-6">
                    <strong><i class="fas fa-ruler me-2"></i>Area:</strong> 
                    <span class="text-muted">${field.area} hectares</span>
                </div>
                <div class="col-6">
                    <strong><i class="fas fa-layer-group me-2"></i>Soil Type:</strong> 
                    <span class="text-muted">${escapeHtml(field.soilType)}</span>
                </div>
                <div class="col-6 mt-2">
                    <strong><i class="fas fa-seedling me-2"></i>Current Crop:</strong> 
                    <span class="text-muted">${escapeHtml(field.currentCrop || 'Not set')}</span>
                </div>
                <div class="col-6 mt-2">
                    <strong><i class="fas fa-microchip me-2"></i>Sensors:</strong> 
                    <span class="text-muted">${sensors.length} active</span>
                </div>
            </div>
            <hr>
            <h6><i class="fas fa-chart-line me-2"></i>Recent Activity</h6>
            <p class="text-muted mb-2">${readings.length} total readings recorded</p>
            ${readings.length > 0 ? `
                <small class="text-muted">
                    Last reading: ${formatTimeAgo(readings[readings.length - 1].timestamp)}
                </small>
            ` : '<small class="text-muted">No readings yet</small>'}
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
            <h6 class="mb-3"><i class="fas fa-microchip me-2"></i>Sensors for ${escapeHtml(fieldName)}</h6>
            ${sensors.map(sensor => {
            const statusColor = { active: 'success', inactive: 'secondary', error: 'danger' }[sensor.status] || 'secondary';
            return `
                    <div class="card mb-2">
                        <div class="card-body d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="mb-1"><i class="fas fa-microchip me-2"></i>${escapeHtml(sensor.sensorType)}</h6>
                                <small class="text-muted">Device: ${escapeHtml(sensor.deviceId)}</small>
                                <br><small class="text-muted">Installed: ${new Date(sensor.installedAt).toLocaleDateString()}</small>
                                ${sensor.lastConnection ? `<br><small class="text-muted">Last seen: ${formatTimeAgo(sensor.lastConnection)}</small>` : ''}
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

async function viewHistoricalData(fieldId) {
    try {
        const modal = new bootstrap.Modal(document.getElementById('historyModal'));
        const content = document.getElementById('historyModalContent');

        content.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-warning"></div></div>';
        modal.show();

        const readings = await ApiService.getReadingsByField(fieldId);
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

async function getPredictionForField(fieldId) {
    try {


        const reading = await ApiService.getLatestReading(fieldId);

        if (!reading || !reading.data) {
            throw new Error('No sensor data available for prediction');
        }
        console.log("Sending crop prediction payload:", {
            nitrogen: reading.data.nitrogen,
            phosphorus: reading.data.phosphorus,
            potassium: reading.data.potassium,
            temperature: reading.data.temperature,
            humidity: reading.data.humidity,
            pH: reading.data.pH || 0,
            rainfall: reading.data.rainfall || 0
        });
        const prediction = await ApiService.predictCrop(fieldId, {
            nitrogen: reading.data.nitrogen,
            phosphorus: reading.data.phosphorus,
            potassium: reading.data.potassium,
            temperature: reading.data.temperature,
            humidity: reading.data.humidity,
            pH: reading.data.pH || 0,
            rainfall: reading.data.rainfall || 0
        });


        console.log('✅ Prediction generated:', prediction);
        displayPrediction(prediction);
        showSuccess('Crop prediction generated successfully!');

    } catch (error) {
        console.error('❌ Error getting prediction:', error);
        showError('Failed to generate prediction: ' + error.message);

    }
}
// Get Fertilizer Prediction for Field
async function getFertilizerPredictionForField(fieldId) {
    try {
        const reading = await ApiService.getLatestReading(fieldId);

        if (!reading || !reading.data) {
            throw new Error('No sensor data available for prediction');
        }

        const field = await ApiService.getFieldById(fieldId);

        console.log("Sending fertilizer prediction payload:", {
            temperature: reading.data.temperature,
            humidity: reading.data.humidity,
            soilMoisture: reading.data.soilMoisture,
            soilType: field.soilType,
            cropType: field.currentCrop || 'rice',
            nitrogen: reading.data.nitrogen,
            phosphorus: reading.data.phosphorus,
            potassium: reading.data.potassium
        });

        const prediction = await ApiService.predictFertilizer(fieldId, {
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

// Request Fertilizer Prediction (triggered by button)
async function requestFertilizerPrediction() {
    if (STATE.fields.length === 0) {
        showError('No fields available. Contact your administrator to add fields.');
        return;
    }

    const fieldId = STATE.selectedFieldId || STATE.fields[0].id;
    await getFertilizerPredictionForField(fieldId);
}
// =====================================================
// WEBSOCKET REAL-TIME UPDATES
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
        if (!userField) {
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

        // ✅ NOUVEAU: Update charts in real-time
        updateChartsRealtime(reading);

        showSensorUpdateNotification(wsData.fieldId, reading.data);
    });
    // Handle ALERT notifications
    wsManager.on('ALERT', (alert) => {
        console.log('🔔 New alert received:', alert);

        // Check if this alert is for current user's fields
        const userField = STATE.fields.find(f => f.id === alert.fieldId);
        if (!userField) {
            return; // Not our field, ignore
        }

        // Add the new alert to the beginning of the array
        STATE.alerts.unshift(alert);

        // Re-display alerts (newest first)
        displayAlerts(STATE.alerts);
        updateNotificationBadge(STATE.alerts);
        updateStatistics();

        // Show browser notification
        showAlertNotification(alert);

        // Play sound if enabled
        if (CONFIG.notificationSound) {
            playNotificationSound();
        }
    });

    // Handle PREDICTION updates
    wsManager.on('PREDICTION', (prediction) => {
        console.log('🤖 New prediction received:', prediction);

        const userField = STATE.fields.find(f => f.id === prediction.fieldId);
        if (!userField) {
            return;
        }

        // Display selon le type de prédiction
        if (prediction.predictionType === 'fertilizer') {
            displayFertilizerPrediction(prediction);
            showSuccess(`New fertilizer recommendation for ${getFieldName(prediction.fieldId)}: ${prediction.result.recommendation}`);
        } else {
            displayPrediction(prediction);
            showSuccess(`New crop prediction for ${getFieldName(prediction.fieldId)}: ${prediction.result.recommendation}`);
        }

        // Show browser notification
        if (Notification.permission === 'granted') {
            const title = prediction.predictionType === 'fertilizer' ? '🧪 Fertilizer Recommendation' : '🌱 Crop Prediction Ready';
            new Notification(title, {
                body: `Recommended: ${prediction.result.recommendation}`,
                icon: '../images/icons/icon-192x192.png'
            });
        }
    });
    // Handle WebSocket connection status
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

// =====================================================
// WEBSOCKET HELPER FUNCTIONS
// =====================================================

function showSensorUpdateNotification(fieldId, data) {
    const fieldName = getFieldName(fieldId);
    const isWarning = !checkDataHealth(data);

    if (isWarning) {
        showWarning(`⚠️ Unusual readings detected in ${fieldName}`);
    } else {
        // Show a subtle success indicator
        const statusBadge = document.getElementById(`status-${fieldId}`);
        if (statusBadge) {
            statusBadge.classList.add('pulse-animation');
            setTimeout(() => statusBadge.classList.remove('pulse-animation'), 1000);
        }
    }
}

function showAlertNotification(alert) {
    const fieldName = getFieldName(alert.fieldId);

    // Show browser notification
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

    // Show in-app toast notification
    showToast(alert.message, alert.severity);
}

function showToast(message, severity = 'info') {
    const colors = {
        high: 'danger',
        medium: 'warning',
        low: 'info',
        info: 'info',
        success: 'success'
    };

    const color = colors[severity] || 'info';

    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast-notification bg-${color}`;
    toast.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="fas fa-bell me-2"></i>
            <span>${escapeHtml(message)}</span>
        </div>
    `;

    // Add to page
    document.body.appendChild(toast);

    // Animate in
    setTimeout(() => toast.classList.add('show'), 100);

    // Remove after 5 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}

function showWarning(message) {
    showToast(message, 'medium');
}

function playNotificationSound() {
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.value = 800;
        oscillator.type = 'sine';

        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.5);
    } catch (error) {
        console.warn('Could not play notification sound:', error);
    }
}

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

// =====================================================
// AUTO-REFRESH
// =====================================================

function setupAutoRefresh() {
    setInterval(async () => {
        // If WebSocket is connected, only refresh alerts (not sensor data)
        // Sensor data comes in real-time via WebSocket
        if (STATE.wsConnected) {
            console.log('🔄 Auto-refresh (WS active): alerts only');

            // Refresh alerts (in case some were missed)
            STATE.alerts = await ApiService.getAlertsByUser(CONFIG.userId);
            displayAlerts(STATE.alerts);
            updateNotificationBadge(STATE.alerts);
            updateStatistics();
        } else {
            // If WebSocket is not connected, do full refresh
            console.log('🔄 Auto-refresh (WS inactive): full reload');
            await loadAllData();
        }
    }, CONFIG.refreshInterval);
}
// =====================================================
// LOGOUT HANDLER
// =====================================================

function setupLogoutHandler() {
    document.getElementById('signout')?.addEventListener('click', logout);
}

function logout() {
    console.log('👋 Logging out...');

    sessionStorage.removeItem('currentUser');
    localStorage.removeItem('currentUser');

    if (STATE.wsConnected) {
        wsManager.disconnect();
    }

    window.location.href = '../index.html';
}

// =====================================================
// PLACEHOLDER FUNCTIONS
// =====================================================

function viewAllNotifications(event) {
    event.preventDefault();
    showError('View All Notifications - Coming soon!');
}

// =====================================================
// CSS FOR TOAST NOTIFICATIONS & ANIMATIONS
// =====================================================

const style = document.createElement('style');
style.textContent = `
    .toast-notification {
        position: fixed;
        top: 80px;
        right: 20px;
        padding: 12px 20px;
        border-radius: 8px;
        color: white;
        font-size: 14px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 9999;
        opacity: 0;
        transform: translateX(400px);
        transition: all 0.3s ease;
    }
    
    .toast-notification.show {
        opacity: 1;
        transform: translateX(0);
    }
    
    .ws-indicator {
        display: flex;
        align-items: center;
        gap: 5px;
        padding: 5px 12px;
        border-radius: 20px;
        background: rgba(0,0,0,0.05);
        font-size: 12px;
        transition: all 0.3s;
    }
    
    .ws-indicator.connected {
        background: rgba(40, 167, 69, 0.1);
        color: #28a745;
    }
    
    .ws-indicator.disconnected {
        background: rgba(220, 53, 69, 0.1);
        color: #dc3545;
    }
    
    .pulse-animation {
        animation: pulse 0.5s ease-in-out;
    }
    
    @keyframes pulse {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.1); }
    }
`;
document.head.appendChild(style);

// =====================================================
// END OF USER DASHBOARD
// =====================================================

console.log('✅ User Dashboard fully loaded and ready!');