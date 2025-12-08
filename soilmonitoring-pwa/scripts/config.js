// ===================== API Configuration =====================
const API_CONFIG = {
    BASE_URL: 'http://api.soilmonitoring.me:8080/api',
    WS_URL: 'ws://api.soilmonitoring.me:8080/ws/sensor-data',
    ENDPOINTS: {
        // Health Check
        TEST: '/test',

        // Fields
        FIELDS_BY_USER: '/fields/user',      // + /{userId}
        FIELD_BY_ID: '/fields',              // + /{fieldId}
        FIELDS: '/fields',                   // POST/PUT/DELETE

        // Readings
        READINGS_BY_FIELD: '/readings/field', // + /{fieldId}
        LATEST_READING: '/readings/field',    // + /{fieldId}/latest
        READINGS_RANGE: '/readings/field',    // + /{fieldId}/range
        READINGS: '/readings',                // POST

        // Sensors
        SENSORS_BY_FIELD: '/sensors/field',   // + /{fieldId}
        SENSOR_BY_ID: '/sensors',             // + /{sensorId}
        SENSOR_BY_DEVICE: '/sensors/device',  // + /{deviceId}
        SENSORS: '/sensors',                  // POST/PUT/DELETE
        SENSOR_STATUS: '/sensors',            // + /{sensorId}/status

        // Alerts
        ALERTS_BY_USER: '/alerts/user',       // + /{userId}
        ALERT_BY_ID: '/alerts',               // + /{alertId}
        ALERTS: '/alerts',                    // POST/DELETE
        ALERT_READ: '/alerts',                // + /{alertId}/read

        // Predictions
        PREDICTIONS_BY_FIELD: '/predictions/field', // + /{fieldId}
        PREDICTION_BY_ID: '/predictions',           // + /{predictionId}
        PREDICT_CROP: '/predictions/crop',          // POST
        PREDICT_FERTILIZER: '/predictions/fertilizer' // POST
    }
};
// ===================== AUTH TOKEN HANDLER =====================
function getAuthHeaders() {
    const token = sessionStorage.getItem("access_token"); // or sessionStorage

    // If no token, return only content-type
    if (!token) {
        return {
            "Content-Type": "application/json"
        };
    }

    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
    };
}
// ===================== API Helper Functions =====================
const API = {
    // Generic GET request
    async get(endpoint, params = {}) {
        const url = new URL(`${API_CONFIG.BASE_URL}${endpoint}`);
        Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API GET Error:', error);
            throw error;
        }
    },

    // Generic POST request
    async post(endpoint, data) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API POST Error:', error);
            throw error;
        }
    },

    // Generic PUT request
    async put(endpoint, data) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API PUT Error:', error);
            throw error;
        }
    },

    // Generic DELETE request
    async delete(endpoint) {
        try {
            const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
                method: 'DELETE',
                headers: getAuthHeaders(),
            });

            if (!response.ok && response.status !== 204) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return response.status === 204 ? null : await response.json();
        } catch (error) {
            console.error('API DELETE Error:', error);
            throw error;
        }
    }
};

// ===================== WebSocket Manager =====================
class WebSocketManager {
    constructor() {
        this.ws = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 5000;
        this.listeners = {
            'SENSOR_DATA': [],
            'ALERT': [],
            'PREDICTION': [],
            'CONNECTION': []
        };
    }

    connect() {
        try {
            this.ws = new WebSocket(API_CONFIG.WS_URL);

            this.ws.onopen = (event) => {
                console.log('✅ WebSocket Connected');
                this.reconnectAttempts = 0;
            };

            this.ws.onmessage = (event) => {
                const data = JSON.parse(event.data);
                console.log('📨 WebSocket Message:', data);

                // Notify all listeners for this message type
                if (this.listeners[data.type]) {
                    this.listeners[data.type].forEach(callback => {
                        callback(data.payload || data);
                    });
                }
            };

            this.ws.onerror = (error) => {
                console.error('❌ WebSocket Error:', error);
            };

            this.ws.onclose = (event) => {
                console.log('❌ WebSocket Disconnected');
                this.reconnect();
            };
        } catch (error) {
            console.error('Failed to create WebSocket:', error);
            this.reconnect();
        }
    }

    reconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Reconnecting... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            setTimeout(() => this.connect(), this.reconnectDelay);
        } else {
            console.error('❌ Max reconnection attempts reached');
        }
    }

    on(messageType, callback) {
        if (this.listeners[messageType]) {
            this.listeners[messageType].push(callback);
        }
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }
}