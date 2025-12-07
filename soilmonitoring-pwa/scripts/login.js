// OAuth2 Configuration
const OAUTH_CONFIG = {
    clientId: 'soil-monitoring-pwa',
    authorizationEndpoint: 'http://iam.soilmonitoring.me:8080/iam/authorize',
    redirectUri: window.location.origin + '/pages/callback.html',
    scope: 'openid profile email fields',
    responseType: 'code'
};

// Check if user is already logged in
window.addEventListener('DOMContentLoaded', () => {
    const accessToken = sessionStorage.getItem('access_token');
    if (accessToken && !isTokenExpired()) {
        // Already logged in, redirect to dashboard
        redirectToDashboard();
    }
});

// Handle login form submission
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    // Show loading state
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i> Signing in...';
    submitBtn.disabled = true;

    try {
        // Start OAuth2 flow
        await startOAuthLogin();
    } catch (error) {
        console.error('Login error:', error);
        showAlert('Failed to initiate login. Please try again.', 'danger');
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
});

/**
 * Generate cryptographically secure random string
 */
function generateRandomString(length) {
    const array = new Uint8Array(length);
    crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
}

/**
 * Generate SHA-256 hash
 */
async function sha256(plain) {
    const encoder = new TextEncoder();
    const data = encoder.encode(plain);
    const hash = await crypto.subtle.digest('SHA-256', data);
    return hash;
}

/**
 * Base64 URL encode
 */
function base64URLEncode(buffer) {
    const bytes = new Uint8Array(buffer);
    let str = '';
    bytes.forEach(byte => str += String.fromCharCode(byte));
    return btoa(str)
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}

/**
 * Generate PKCE code challenge from verifier
 */
async function generateCodeChallenge(codeVerifier) {
    const hashed = await sha256(codeVerifier);
    return base64URLEncode(hashed);
}

/**
 * Initiate OAuth2 Authorization Code + PKCE flow
 */
async function startOAuthLogin() {
    try {
        // Generate PKCE parameters
        const state = generateRandomString(32);
        const codeVerifier = generateRandomString(64);
        const codeChallenge = await generateCodeChallenge(codeVerifier);

        // Store state and verifier for validation in callback
        sessionStorage.setItem('oauth_state', state);
        sessionStorage.setItem('code_verifier', codeVerifier);
        sessionStorage.setItem('code_challenge', codeChallenge);

        // Build authorization URL
        const authUrl = new URL(OAUTH_CONFIG.authorizationEndpoint);
        authUrl.searchParams.append('client_id', OAUTH_CONFIG.clientId);
        authUrl.searchParams.append('redirect_uri', OAUTH_CONFIG.redirectUri);
        authUrl.searchParams.append('response_type', OAUTH_CONFIG.responseType);
        authUrl.searchParams.append('scope', OAUTH_CONFIG.scope);
        authUrl.searchParams.append('state', state);
        authUrl.searchParams.append('code_challenge', codeChallenge);
        authUrl.searchParams.append('code_challenge_method', 'S256');

        console.log('🚀 Starting OAuth flow:', authUrl.toString());

        // Redirect to IAM login page
        window.location.href = authUrl.toString();

    } catch (error) {
        console.error('❌ Failed to start OAuth login:', error);
        throw error;
    }
}

/**
 * Check if token is expired
 */
function isTokenExpired() {
    const expiry = sessionStorage.getItem('token_expiry');
    if (!expiry) return true;
    return Date.now() > parseInt(expiry);
}

/**
 * Redirect to appropriate dashboard based on user role
 */
function redirectToDashboard() {
    const accessToken = sessionStorage.getItem('access_token');

    try {
        // Decode JWT to get user info
        const payload = parseJWT(accessToken);

        // Check user role
        if (payload.groups && payload.groups.includes('admin')) {
            window.location.href = '../pages/admin.html';
        } else {
            window.location.href = '../pages/user.html';
        }
    } catch (error) {
        console.error('Failed to parse token:', error);
        window.location.href = '../pages/user.html';
    }
}

/**
 * Parse JWT token
 */
function parseJWT(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
}

/**
 * Show alert message
 */
function showAlert(message, type = 'info') {
    const alertDiv = document.getElementById('alertMessage');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.classList.remove('d-none');

    // Auto-hide after 5 seconds
    setTimeout(() => {
        alertDiv.classList.add('d-none');
    }, 5000);
}

// Handle "Remember me" functionality (optional)
const rememberMeCheckbox = document.getElementById('rememberMe');
if (rememberMeCheckbox && localStorage.getItem('rememberMe') === 'true') {
    rememberMeCheckbox.checked = true;
}

rememberMeCheckbox?.addEventListener('change', (e) => {
    localStorage.setItem('rememberMe', e.target.checked);
});