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
        const user = JSON.parse(currentUser);
        const redirectPage = user.role === 'Administrator' ? 'pages/admin.html' : 'pages/user.html';
        window.location.href = redirectPage;
    }
});
