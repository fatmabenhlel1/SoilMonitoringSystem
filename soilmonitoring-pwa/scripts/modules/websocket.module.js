/**
 * WebSocket Module
 * Manages WebSocket connection with pub/sub pattern
 */

export const wsManager = {
    ws: null,
    listeners: {},
    reconnectTimeout: null,

    connect() {
        console.log('🔌 Connecting to WebSocket...');
        // Note: Using WSS (secure WebSocket)
        this.ws = new WebSocket('wss://api.soilmonitoring.me/ws/sensor-data');

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
            // Auto-reconnect after 5 seconds
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
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
        }
        if (this.ws) {
            this.ws.close();
        }
    }
};