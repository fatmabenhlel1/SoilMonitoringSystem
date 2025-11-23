// ===================== API Service Functions =====================
// Import config.js avant ce fichier dans le HTML

const ApiService = {
    // ===================== FIELDS =====================

    // Get all fields for a user
    async getFieldsByUser(userId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.FIELDS_BY_USER}/${userId}`);
    },

    // Get specific field
    async getFieldById(fieldId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.FIELD_BY_ID}/${fieldId}`);
    },

    // Create new field
    async createField(fieldData) {
        return await API.post(API_CONFIG.ENDPOINTS.FIELDS, fieldData);
    },

    // Update field
    async updateField(fieldId, fieldData) {
        return await API.put(`${API_CONFIG.ENDPOINTS.FIELDS}/${fieldId}`, fieldData);
    },

    // Delete field
    async deleteField(fieldId) {
        return await API.delete(`${API_CONFIG.ENDPOINTS.FIELDS}/${fieldId}`);
    },

    // ===================== READINGS =====================

    // Get all readings for a field
    async getReadingsByField(fieldId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.READINGS_BY_FIELD}/${fieldId}`);
    },

    // Get latest reading for a field
    async getLatestReading(fieldId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.LATEST_READING}/${fieldId}/latest`);
    },

    // Get readings in time range
    async getReadingsRange(fieldId, fromDate, toDate) {
        return await API.get(
            `${API_CONFIG.ENDPOINTS.READINGS_RANGE}/${fieldId}/range`,
            { from: fromDate, to: toDate }
        );
    },

    // Create new reading (for testing)
    async createReading(readingData) {
        return await API.post(API_CONFIG.ENDPOINTS.READINGS, readingData);
    },

    // ===================== SENSORS =====================

    // Get sensors by field
    async getSensorsByField(fieldId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.SENSORS_BY_FIELD}/${fieldId}`);
    },

    // Get sensor by ID
    async getSensorById(sensorId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.SENSOR_BY_ID}/${sensorId}`);
    },

    // Get sensor by device ID
    async getSensorByDevice(deviceId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.SENSOR_BY_DEVICE}/${deviceId}`);
    },

    // Create sensor
    async createSensor(sensorData) {
        return await API.post(API_CONFIG.ENDPOINTS.SENSORS, sensorData);
    },

    // Update sensor status
    async updateSensorStatus(sensorId, status) {
        return await API.put(
            `${API_CONFIG.ENDPOINTS.SENSOR_STATUS}/${sensorId}/status?status=${status}`,
            {}
        );
    },

    // Delete sensor
    async deleteSensor(sensorId) {
        return await API.delete(`${API_CONFIG.ENDPOINTS.SENSORS}/${sensorId}`);
    },

    // ===================== ALERTS =====================

    // Get all alerts for user
    async getAlertsByUser(userId, unreadOnly = false) {
        const params = unreadOnly ? { unread: true } : {};
        return await API.get(`${API_CONFIG.ENDPOINTS.ALERTS_BY_USER}/${userId}`, params);
    },

    // Get alert by ID
    async getAlertById(alertId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.ALERT_BY_ID}/${alertId}`);
    },

    // Create alert
    async createAlert(alertData) {
        return await API.post(API_CONFIG.ENDPOINTS.ALERTS, alertData);
    },

    // Mark alert as read
    async markAlertAsRead(alertId) {
        return await API.put(`${API_CONFIG.ENDPOINTS.ALERT_READ}/${alertId}/read`, {});
    },

    // Delete alert
    async deleteAlert(alertId) {
        return await API.delete(`${API_CONFIG.ENDPOINTS.ALERTS}/${alertId}`);
    },

    // ===================== PREDICTIONS =====================

    // Get predictions by field
    async getPredictionsByField(fieldId, type = null) {
        const params = type ? { type } : {};
        return await API.get(
            `${API_CONFIG.ENDPOINTS.PREDICTIONS_BY_FIELD}/${fieldId}`,
            params
        );
    },

    // Get prediction by ID
    async getPredictionById(predictionId) {
        return await API.get(`${API_CONFIG.ENDPOINTS.PREDICTION_BY_ID}/${predictionId}`);
    },

    // Request crop prediction
    async predictCrop(fieldId, sensorData) {
        return await API.post(API_CONFIG.ENDPOINTS.PREDICT_CROP, {
            fieldId,
            inputData: sensorData
        });
    },

    // Request fertilizer prediction
    async predictFertilizer(fieldId, sensorData) {
        return await API.post(API_CONFIG.ENDPOINTS.PREDICT_FERTILIZER, {
            fieldId,
            inputData: sensorData
        });
    },

    // ===================== HEALTH CHECK =====================

    async checkHealth() {
        return await API.get(API_CONFIG.ENDPOINTS.TEST);
    }
};

// ===================== Example Usage =====================
/*

// Check if API is running
ApiService.checkHealth()
    .then(data => console.log('API Health:', data))
    .catch(err => console.error('API Down:', err));

// Get user's fields
ApiService.getFieldsByUser('test-user-001')
    .then(fields => console.log('Fields:', fields))
    .catch(err => console.error('Error:', err));

// Get latest reading
ApiService.getLatestReading('35124a0b-3430-4764-9868-009d74821f6e')
    .then(reading => console.log('Latest Reading:', reading))
    .catch(err => console.error('Error:', err));

// Get unread alerts
ApiService.getAlertsByUser('test-user-001', true)
    .then(alerts => console.log('Unread Alerts:', alerts))
    .catch(err => console.error('Error:', err));

// Request crop prediction
ApiService.predictCrop('field-id', {
    nitrogen: 45.2,
    phosphorus: 23.1,
    potassium: 105.8,
    temperature: 22.5,
    humidity: 65.3,
    ph: 6.8,
    rainfall: 12.3
})
    .then(prediction => console.log('Prediction:', prediction))
    .catch(err => console.error('Error:', err));

// Connect WebSocket for real-time updates
wsManager.connect();

// Listen for sensor data
wsManager.on('SENSOR_DATA', (data) => {
    console.log('New Sensor Data:', data);
    updateDashboard(data);
});

// Listen for alerts
wsManager.on('ALERT', (alert) => {
    console.log('New Alert:', alert);
    showNotification(alert);
});

// Listen for predictions
wsManager.on('PREDICTION', (prediction) => {
    console.log('New Prediction:', prediction);
    displayPrediction(prediction);
});

*/