/**
 * Utilities Module
 * Common utility functions used across the application
 */

import { STATE } from './config.module.js';

/**
 * Get field name by ID
 */
export function getFieldName(fieldId) {
    const field = STATE.fields.find(f => f.id === fieldId);
    return field ? field.name : 'Unknown Field';
}

/**
 * Format timestamp to relative time
 */
export function formatTimeAgo(timestamp) {
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

/**
 * Escape HTML to prevent XSS
 */
export function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Check if sensor data is within healthy ranges
 */
export function checkDataHealth(data) {
    return (
        data.temperature >= 15 && data.temperature <= 35 &&
        data.humidity >= 30 && data.humidity <= 85 &&
        data.nitrogen >= 20 && data.nitrogen <= 100 &&
        data.phosphorus >= 10 && data.phosphorus <= 80 &&
        data.potassium >= 50 && data.potassium <= 200 &&
        data.pH >= 5.5 && data.pH <= 7.5
    );
}

/**
 * Show loading overlay
 */
export function showLoading(show) {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = show ? 'flex' : 'none';
    }
}

/**
 * Show error message
 */
export function showError(message) {
    const alert = document.getElementById('errorAlert');
    const messageEl = document.getElementById('errorMessage');

    if (alert && messageEl) {
        messageEl.textContent = message;
        alert.style.display = 'block';
        setTimeout(() => alert.style.display = 'none', 5000);
    }
}

/**
 * Hide error message
 */
export function hideError() {
    const alert = document.getElementById('errorAlert');
    if (alert) alert.style.display = 'none';
}

/**
 * Show success message
 */
export function showSuccess(message) {
    const alert = document.getElementById('successAlert');
    const messageEl = document.getElementById('successMessage');

    if (alert && messageEl) {
        messageEl.textContent = message;
        alert.style.display = 'block';
        setTimeout(() => alert.style.display = 'none', 3000);
    }
}

/**
 * Hide success message
 */
export function hideSuccess() {
    const alert = document.getElementById('successAlert');
    if (alert) alert.style.display = 'none';
}

/**
 * Show toast notification
 */
export function showToast(message, severity = 'info') {
    const colors = {
        high: 'danger',
        medium: 'warning',
        low: 'info',
        info: 'info',
        success: 'success'
    };

    const color = colors[severity] || 'info';

    const toast = document.createElement('div');
    toast.className = `toast-notification bg-${color}`;
    toast.innerHTML = `
        <div class="d-flex align-items-center">
            <i class="fas fa-bell me-2"></i>
            <span>${escapeHtml(message)}</span>
        </div>
    `;

    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add('show'), 100);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}

/**
 * Show warning message
 */
export function showWarning(message) {
    showToast(message, 'medium');
}

/**
 * Play notification sound
 */
export function playNotificationSound() {
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

/**
 * Get severity configuration for alerts
 */
export function getSeverityConfig(severity) {
    const configs = {
        high: { class: 'alert', icon: 'exclamation-triangle', color: 'danger' },
        medium: { class: 'info', icon: 'info-circle', color: 'warning' },
        low: { class: 'success', icon: 'check-circle', color: 'info' }
    };
    return configs[severity] || configs.low;
}