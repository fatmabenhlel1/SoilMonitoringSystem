/**
 * Configuration Module
 * Central configuration for admin and user dashboards
 */

export const CONFIG = {
    userId: null,
    refreshInterval: 30000, // 30 seconds auto-refresh
    notificationSound: true
};

export let STATE = {
    currentUser: null,
    fields: [],
    alerts: [],
    sensors: [],
    selectedFieldId: null,
    wsConnected: false
};

export function updateConfig(key, value) {
    if (CONFIG.hasOwnProperty(key)) {
        CONFIG[key] = value;
        console.log(`✅ Config updated: ${key} = ${value}`);
    }
}

export function updateState(key, value) {
    if (STATE.hasOwnProperty(key)) {
        STATE[key] = value;
        console.log(`✅ State updated: ${key}`);
    }
}