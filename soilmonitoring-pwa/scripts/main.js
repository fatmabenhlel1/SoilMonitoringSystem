// Demo users database
const users = {
    'admin@agromonitor.com': {
        password: 'admin123',
        role: 'Administrator',
        name: 'Administrator',
        redirect: 'pages/admin.html'
    },
    'user@agromonitor.com': {
        password: 'user123',
        role: 'User',
        name: 'John Farmer',
        redirect: 'pages/user.html'
    }
};

// Login button
document.getElementById('signin')?.addEventListener('click', function() {
    const email = prompt('Enter email:\n\nDemo accounts:\n• admin@agromonitor.com / admin123\n• user@agromonitor.com / user123');

    if (!email) return;

    const password = prompt('Enter password:');

    if (email && password && users[email]) {
        if (users[email].password === password) {
            sessionStorage.setItem('currentUser', JSON.stringify({
                email: email,
                name: users[email].name,
                role: users[email].role
            }));
            window.location.href = users[email].redirect;
        } else {
            alert('Incorrect password!');
        }
    } else {
        alert('User not found!');
    }
});

// Signup button
document.getElementById('signup')?.addEventListener('click', function() {
    alert('Signup feature coming soon!\n\nPlease use demo accounts:\n• Admin: admin@agromonitor.com / admin123\n• User: user@agromonitor.com / user123');
});

// Check if already logged in
window.addEventListener('load', function() {
    const currentUser = sessionStorage.getItem('currentUser') || localStorage.getItem('currentUser');

    if (currentUser) {
        const user = JSON.parse(currentUser);
        const redirectPage = user.role === 'Administrator' ? 'pages/admin.html' : 'pages/user.html';
        window.location.href = redirectPage;
    }
});
