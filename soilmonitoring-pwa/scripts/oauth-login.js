// OAuth2 Configuration
const OAUTH_CONFIG = {
    clientId: 'soil-monitoring-pwa',
    authorizationEndpoint: 'http://iam.soilmonitoring.me:8080/iam/authorize',
    redirectUri: 'http://localhost:58928/sw.js/pages/callback.html',
    scope: 'openid profile email fields',
    responseType: 'code'
};

/**
 * Generate a cryptographically secure random string
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
        alert('Failed to initiate login. Please try again.');
    }
}

// Export for use in login page
window.startOAuthLogin = startOAuthLogin;