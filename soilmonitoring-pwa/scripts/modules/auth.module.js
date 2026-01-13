/**
 * Authentication Module
 * Handles JWT authentication and role-based access control
 */

import { CONFIG, STATE } from './config.module.js';

/**
 * Check admin authentication
 */
export function checkAdminAuthentication() {
    console.log('🔍 ADMIN.HTML: Checking authentication...');

    const accessToken = sessionStorage.getItem('access_token');
    console.log('🔑 Access token found:', accessToken ? 'YES' : 'NO');

    if (!accessToken) {
        console.warn('⚠️ No authentication token found. Redirecting to login...');
        window.location.href = '../pages/login.html';
        return false;
    }

    // Check token expiry
    const expiry = sessionStorage.getItem('token_expiry');
    if (expiry && Date.now() > parseInt(expiry)) {
        console.warn('⚠️ Token expired. Redirecting to login...');
        sessionStorage.clear();
        window.location.href = '../pages/login.html';
        return false;
    }

    // Parse JWT
    try {
        const payload = parseJWT(accessToken);
        console.log('📦 JWT Payload:', payload);

        const currentUser = {
            id: payload.sub || payload.upn,
            name: payload.sub || payload.upn,
            email: payload.email || `${payload.sub}@soilmonitoring.com`,
            groups: payload.groups || []
        };

        const isAdmin = currentUser.groups.some(g =>
            g.toLowerCase() === 'admin' || g.toLowerCase() === 'administrator'
        );

        console.log('👤 User:', currentUser.name);
        console.log('🔑 Groups:', currentUser.groups);
        console.log('🛡️ Is Admin?', isAdmin);

        if (!isAdmin) {
            console.warn('⚠️ User is not Administrator. Redirecting to user dashboard...');
            if (!sessionStorage.getItem('redirect_attempted')) {
                sessionStorage.setItem('redirect_attempted', 'true');
                window.location.replace('user.html');
            }
            return false;
        }

        currentUser.role = 'Administrator';
        STATE.currentUser = currentUser;
        CONFIG.userId = currentUser.id;

        console.log('✅ Admin access granted');

        const userNameEl = document.getElementById('user-name') || document.getElementById('admin-name');
        if (userNameEl) userNameEl.textContent = currentUser.name;

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
 * Setup logout handler
 */
export function setupLogoutHandler() {
    const logoutBtn = document.getElementById('signout') || document.getElementById('logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', logout);
    }
}

/**
 * Logout function
 */
export function logout() {
    console.log('👋 Logging out...');

    sessionStorage.clear();
    localStorage.clear();

    window.location.href = '../pages/login.html';
}