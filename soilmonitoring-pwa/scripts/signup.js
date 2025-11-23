// ===================== Signup Form Elements =====================
const signupForm = document.getElementById('signupForm');
const alertMessage = document.getElementById('alertMessage');

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

// ===================== Handle Signup Submission =====================
if (signupForm) {
    signupForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const confirmPassword = document.getElementById('confirmPassword').value.trim();
        const accountType = document.getElementById('accountType').value;
        const agreeTerms = document.getElementById('agreeTerms').checked;

        // Validate passwords match
        if (password !== confirmPassword) {
            showAlert('Passwords do not match!', 'danger');
            return;
        }

        // Validate terms agreement
        if (!agreeTerms) {
            showAlert('Please agree to the Terms & Conditions', 'warning');
            return;
        }

        // Check if email already exists (demo check)
        const existingUsers = ['admin@agromonitor.com', 'user@agromonitor.com'];
        if (existingUsers.includes(email)) {
            showAlert('This email is already registered. Please login instead.', 'danger');
            return;
        }

        // Simulate successful registration
        showAlert('Account created successfully! Redirecting to login...', 'success');

        // For demo purposes, log user registration
        console.log('New User Registration:', {
            fullName,
            email,
            accountType,
            password: '***hidden***'
        });

        // Redirect to login page after 2 seconds
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
    });
}

// ===================== Password Strength Indicator (Optional) =====================
const passwordInput = document.getElementById('password');
if (passwordInput) {
    passwordInput.addEventListener('input', function(e) {
        const password = e.target.value;
        const strength = password.length >= 8 ? 'strong' : password.length >= 6 ? 'medium' : 'weak';
        // Optionally, add visual feedback for password strength here
        console.log(`Password strength: ${strength}`);
    });
}
