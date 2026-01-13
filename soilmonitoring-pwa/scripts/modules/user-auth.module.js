/**
 * User Authentication Module
 * Handles user-specific authentication (NOT admin)
 */

import { CONFIG, STATE } from './config.module.js';

/**
 * Check user authentication
 */
export function checkUserAuthentication() {
    console.log('🔍 USER.HTML: Checking authentication...');

    const accessToken = sessionStorage.getItem('access_token');
    console.log('🔑 Access token found:', accessToken ? 'YES' : 'NO');

    if (!accessToken) {
        console.warn('⚠️ No authentication token found. Redirecting to login...');
        window.location.href = '../pages/login.html';
        return false;
    }

    const expiry = sessionStorage.getItem('token_expiry');
    if (expiry && Date.now() > parseInt(expiry)) {
        console.warn('⚠️ Token expired. Redirecting to login...');
        sessionStorage.clear();
        window.location.href = '../pages/login.html';
        return false;
    }

    try {
        const payload = parseJWT(accessToken);
        console.log('📦 JWT Payload:', payload);

        STATE.currentUser = {
            id: payload.sub || payload.upn,
            name: payload.sub || payload.upn,
            email: payload.email || `${payload.sub}@soilmonitoring.com`,
            groups: payload.groups || []
        };

        const isAdmin = STATE.currentUser.groups.some(g =>
            g.toLowerCase() === 'admin' || g.toLowerCase() === 'administrator'
        );

        console.log('👤 User:', STATE.currentUser.name);
        console.log('🔑 Groups:', STATE.currentUser.groups);
        console.log('🛡️ Is Admin?', isAdmin);

        if (isAdmin) {
            console.warn('⚠️ User is Administrator. Redirecting to admin panel...');
            if (!sessionStorage.getItem('redirect_attempted')) {
                sessionStorage.setItem('redirect_attempted', 'true');
                window.location.replace('admin.html');
            }
            return false;
        }

        STATE.currentUser.role = 'User';
        CONFIG.userId = STATE.currentUser.id;
        console.log('✅ Using userId:', CONFIG.userId);

        document.getElementById('user-name').textContent = STATE.currentUser.name;
        document.getElementById('welcome-name').textContent = STATE.currentUser.name;

        sessionStorage.removeItem('redirect_attempted');

        return true;

    } catch (error) {
        console.error('❌ Failed to parse token:', error);
        sessionStorage.clear();
        window.location.href = '../pages/login.html';
        return false;
    }
}

/**
 * Parse JWT token
 */
export function parseJWT(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Failed to parse JWT:', e);
        throw e;
    }
}

/**
 * Request notification permission
 */
export function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission().then(permission => {
            console.log('🔔 Notification permission:', permission);
        });
    }
}

/**
 * Setup logout handler
 */
export function setupLogoutHandler() {
    document.getElementById('signout')?.addEventListener('click', logout);
}

/**
 * Logout function
 */
export function logout() {
    console.log('👋 Logging out...');

    sessionStorage.clear();
    localStorage.clear();

    if (STATE.wsConnected && typeof window.wsManager !== 'undefined') {
        window.wsManager.disconnect();
    }

    window.location.href = '../pages/login.html';
}