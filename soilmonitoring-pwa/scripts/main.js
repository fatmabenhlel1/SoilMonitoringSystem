// =====================================================
// AGROMONITOR - HOME PAGE
// =====================================================

console.log('🌱 AgroMonitor - Home Page');

// =====================================================
// CONFIGURATION
// =====================================================

const DEV_MODE = true; // Set to true to disable auto-redirect during development

// ===================== Demo Users Database =====================
const users = {
    'admin@agromonitor.com': {
        password: 'admin',
        role: 'Administrator',
        name: 'Administrator',
        redirect: 'pages/admin.html'
    },
    'user@agromonitor.com': {
        password: 'user',
        role: 'User',
        name: 'John Farmer',
        redirect: 'pages/user.html'
    }
};

// ===================== Index Page Buttons =====================
const signinBtn = document.getElementById('signin');
const signupBtn = document.getElementById('signup');

if (signinBtn) {
    signinBtn.addEventListener('click', () => {
        window.location.href = 'pages/login.html';
    });
}

if (signupBtn) {
    signupBtn.addEventListener('click', () => {
        window.location.href = 'pages/signup.html';
    });
}

// ===================== Auto Redirect if Already Logged In =====================
window.addEventListener('load', () => {
    const currentUser = sessionStorage.getItem('currentUser') || localStorage.getItem('currentUser');

    if (currentUser) {
        try {
            const user = JSON.parse(currentUser);
            const redirectPage = user.role === 'Administrator' ? 'pages/admin.html' : 'pages/user.html';

            if (DEV_MODE) {
                console.log('👤 Already logged in as:', user.name);
                console.log('🚧 DEV_MODE enabled - Auto-redirect disabled');
                console.log('💡 Set DEV_MODE = false to enable auto-redirect');
            } else {
                console.log('👤 User logged in, redirecting to', redirectPage);
                window.location.href = redirectPage;
            }
        } catch (error) {
            console.error('❌ Error parsing user data:', error);
            sessionStorage.removeItem('currentUser');
            localStorage.removeItem('currentUser');
        }
    } else {
        console.log('👋 Welcome! Please login or sign up.');
    }
});