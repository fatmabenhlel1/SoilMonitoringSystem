// ===================== Demo Users Database =====================
const users = {
    'admin@agromonitor.com': {
        password: 'admin',
        role: 'Administrator',
        name: 'Administrator',
        redirect: 'admin.html'
    },
    'user@agromonitor.com': {
        password: 'user',
        role: 'User',
        name: 'John Farmer',
        redirect: 'user.html'
    }
};

// ===================== Login Form Elements =====================
const loginForm = document.getElementById('loginForm');
const alertMessage = document.getElementById('alertMessage');

console.log('🚀 Script loaded');
console.log('📋 Form element:', loginForm);

// ===================== Show Alert Function =====================
function showAlert(message, type = 'info') {
    if (!alertMessage) return;
    alertMessage.textContent = message;
    alertMessage.className = `alert alert-${type}`;
    alertMessage.classList.remove('d-none');

    setTimeout(() => {
        alertMessage.classList.add('d-none');
    }, 5000);
}

// ===================== Handle Login Submission =====================
if (loginForm) {
    console.log('✅ Form found - attaching listener');

    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();
        e.stopPropagation();

        console.log('🔥 Form submitted!');

        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const rememberMeInput = document.getElementById('rememberMe');

        console.log('Input elements:', {emailInput, passwordInput, rememberMeInput});

        const email = emailInput?.value.trim() || '';
        const password = passwordInput?.value.trim() || '';
        const rememberMe = rememberMeInput?.checked || false;

        console.log('📧 Email:', email);
        console.log('🔑 Password:', password ? '****' : 'EMPTY');
        console.log('💾 Remember me:', rememberMe);

        if (users[email]) {
            console.log('✅ User found:', users[email].name);

            if (users[email].password === password) {
                console.log('✅ Password correct!');

                const storage = rememberMe ? localStorage : sessionStorage;

                storage.setItem('currentUser', JSON.stringify({
                    email: email,
                    name: users[email].name,
                    role: users[email].role
                }));

                console.log('✅ User data saved to storage');
                console.log('🔄 Redirecting to:', users[email].redirect);

                showAlert('Login successful! Redirecting...', 'success');

                setTimeout(() => {
                    window.location.href = users[email].redirect;
                }, 1000);

                return false;
            } else {
                console.log('❌ Password incorrect');
                showAlert('Incorrect password. Please try again.', 'danger');
            }
        } else {
            console.log('❌ User not found');
            showAlert('Email not found. Please check your email or sign up.', 'danger');
        }

        return false;
    }, true);
} else {
    console.error('❌ LoginForm element NOT FOUND!');
}

// ===================== Auto-Redirect if Already Logged In =====================
window.addEventListener('load', function() {
    const currentUser = sessionStorage.getItem('currentUser') || localStorage.getItem('currentUser');

    if (currentUser) {
        const user = JSON.parse(currentUser);
        const redirectPage = user.role === 'Administrator' ? 'pages/admin.html' : 'pages/user.html';

        showAlert('You are already logged in. Redirecting...', 'info');
        setTimeout(() => {
            window.location.href = redirectPage;
        }, 1500);
    }
});