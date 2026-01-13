/**
 * Alerts Module
 * Handles alerts display and notifications
 */

import { escapeHtml, formatTimeAgo, getSeverityConfig, getFieldName } from './utils.module.js';

/**
 * Display alerts list
 */
export function displayAlerts(alerts) {
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

/**
 * Update notification badge
 */
export function updateNotificationBadge(alerts) {
    const dot = document.getElementById('notificationDot');
    const unreadCount = alerts.filter(a => !a.isRead).length;

    if (dot) {
        dot.style.display = unreadCount > 0 ? 'block' : 'none';
    }

    updateNotificationsDropdown(alerts);
}

/**
 * Update notifications dropdown
 */
export function updateNotificationsDropdown(alerts) {
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