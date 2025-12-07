// Parse URL parameters
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');
const state = urlParams.get('state');
const error = urlParams.get('error');
const errorDescription = urlParams.get('error_description');

// IAM Configuration
const IAM_TOKEN_URL = 'http://iam.soilmonitoring.me:8080/iam/oauth/token';

// Check for errors from IAM
if (error) {
    showError(error, errorDescription);
    // Redirect back to login after 3 seconds
    setTimeout(() => {
        window.location.href = '../pages/login.html';
    }, 3000);
} else if (!code) {
    showError('No authorization code received', 'Please try logging in again');
} else {
    // Validate state and exchange code
    handleCallback(code, state);
}

function showError(errorMsg, description) {
    document.getElementById('status').style.display = 'none';
    document.querySelector('.spinner').style.display = 'none';
    document.getElementById('error').style.display = 'block';
    document.getElementById('error').innerHTML =
        `<strong>Error:</strong> ${errorMsg}<br>${description || ''}`;
    console.error('OAuth error:', errorMsg, description);
}

function handleCallback(authCode, receivedState) {
    // Validate state (CSRF protection)
    const storedState = sessionStorage.getItem('oauth_state');
    const storedVerifier = sessionStorage.getItem('code_verifier');

    if (receivedState !== storedState) {
        showError('Invalid state parameter', 'CSRF protection triggered');
        console.error('State mismatch:', receivedState, 'vs', storedState);
        return;
    }

    if (!storedVerifier) {
        showError('Missing code verifier', 'Session may have expired');
        return;
    }

    // Exchange authorization code for tokens
    exchangeCodeForTokens(authCode, storedVerifier);
}

async function exchangeCodeForTokens(authCode, codeVerifier) {
    try {
        document.getElementById('status').textContent = 'Exchanging code for tokens...';

        const response = await fetch(IAM_TOKEN_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                grant_type: 'authorization_code',
                code: authCode,
                code_verifier: codeVerifier,
                client_id: 'soil-monitoring-pwa',
                redirect_uri: window.location.origin + '/pages/callback.html'
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error_description || 'Token exchange failed');
        }

        const tokens = await response.json();
        console.log('Tokens received:', tokens);

        sessionStorage.setItem('access_token', tokens.access_token);
        sessionStorage.setItem('refresh_token', tokens.refresh_token);
        sessionStorage.setItem('token_expiry', Date.now() + (tokens.expires_in * 1000));

        // Cleanup
        sessionStorage.removeItem('oauth_state');
        sessionStorage.removeItem('code_verifier');
        sessionStorage.removeItem('code_challenge');

        redirectToApp(parseJWT(tokens.access_token));

    } catch (error) {
        console.error('Token exchange error:', error);
        document.getElementById('status').style.display = 'none';
        document.querySelector('.spinner').style.display = 'none';
        document.getElementById('error').style.display = 'block';
        document.getElementById('error').textContent = 'Failed to complete login: ' + error.message;
    }
}


function parseJWT(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Failed to parse JWT:', e);
        return {};
    }
}

function redirectToApp(userPayload) {
    setTimeout(() => {
        // Check if user has admin role
        if (userPayload.groups && userPayload.groups.includes('admin')) {
            window.location.href = '../pages/admin.html';
        } else {
            window.location.href = '../pages/user.html';
        }
    }, 1000);
}