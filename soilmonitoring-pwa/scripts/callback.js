// Parse URL parameters
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');
const state = urlParams.get('state');
const error = urlParams.get('error');
const errorDescription = urlParams.get('error_description');

// IAM Configuration
const IAM_TOKEN_URL = 'https://iam.soilmonitoring.me/iam/oauth/token';

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

        console.log('🔄 Exchanging authorization code for tokens...');
        console.log('📝 Auth code:', authCode.substring(0, 30) + '...');
        console.log('🔑 Code verifier:', codeVerifier.substring(0, 20) + '...');

        const requestBody = new URLSearchParams({
            grant_type: 'authorization_code',
            code: authCode,
            code_verifier: codeVerifier
        });

        console.log('📤 Request details:');
        console.log('  URL:', IAM_TOKEN_URL);
        console.log('  Body:', requestBody.toString());

        const response = await fetch(IAM_TOKEN_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            body: requestBody
        });

        console.log('📡 Response status:', response.status);
        console.log('📡 Response headers:', Object.fromEntries(response.headers.entries()));

        // Get response text first to see what we received
        const responseText = await response.text();
        console.log('📄 Response body:', responseText);

        if (!response.ok) {
            let errorMessage = `HTTP ${response.status}`;
            try {
                const errorData = JSON.parse(responseText);
                errorMessage = errorData.error_description || errorData.error || errorMessage;
            } catch (e) {
                errorMessage = responseText || response.statusText;
            }
            throw new Error(errorMessage);
        }

        // Parse JSON response
        let tokens;
        try {
            tokens = JSON.parse(responseText);
        } catch (e) {
            console.error('❌ Failed to parse response as JSON');
            throw new Error('Invalid response format from server');
        }

        console.log('✅ Tokens received successfully');
        console.log('Token type:', tokens.token_type);
        console.log('Expires in:', tokens.expires_in, 'seconds');
        console.log('Scope:', tokens.scope);

        // Validate tokens
        if (!tokens.access_token) {
            throw new Error('No access token in response');
        }

        // Store tokens in sessionStorage
        sessionStorage.setItem('access_token', tokens.access_token);
        if (tokens.refresh_token) {
            sessionStorage.setItem('refresh_token', tokens.refresh_token);
        }
        if (tokens.expires_in) {
            sessionStorage.setItem('token_expiry', Date.now() + (tokens.expires_in * 1000));
        }

        // Cleanup OAuth session data
        sessionStorage.removeItem('oauth_state');
        sessionStorage.removeItem('code_verifier');
        sessionStorage.removeItem('code_challenge');

        console.log('✅ Tokens stored in sessionStorage');

        // Parse JWT and redirect
        const userPayload = parseJWT(tokens.access_token);
        console.log('👤 User payload:', userPayload);
        
        redirectToApp(userPayload);

    } catch (error) {
        console.error('❌ Token exchange error:', error);
        document.getElementById('status').style.display = 'none';
        document.querySelector('.spinner').style.display = 'none';
        document.getElementById('error').style.display = 'block';
        document.getElementById('error').textContent = 'Failed to complete login: ' + error.message;
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
            window.location.href = '../pages/login.html';
        }, 3000);
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
    document.getElementById('status').textContent = 'Login successful! Redirecting...';
    
    setTimeout(() => {
        // ✅ ALWAYS redirect to user.html first
        // user.html will check roles and redirect to admin.html if needed
        window.location.href = '../pages/user.html';
    }, 1000);
}