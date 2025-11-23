// Authentication check
window.addEventListener('load', function() {
    const currentUser = sessionStorage.getItem('currentUser') || localStorage.getItem('currentUser');

    if (!currentUser) {
        window.location.href = '../index.html';
        return;
    }

    const user = JSON.parse(currentUser);

    if (user.role !== 'Administrator') {
        window.location.href = 'user.html';
        return;
    }

    document.getElementById('user-name').textContent = user.name;
});

// Logout
document.getElementById('signout')?.addEventListener('click', function() {
    sessionStorage.removeItem('currentUser');
    localStorage.removeItem('currentUser');
    window.location.href = '../index.html';
});

// Simulate real-time sensor updates
setInterval(() => {
    const tempElements = document.querySelectorAll('.sensor-value');
    tempElements.forEach(el => {
        if (el.textContent.includes('°C')) {
            const currentTemp = parseFloat(el.textContent);
            const newTemp = (currentTemp + (Math.random() - 0.5) * 0.5).toFixed(1);
            el.textContent = newTemp + '°C';
        }
        if (el.textContent.includes('%')) {
            const currentHum = parseFloat(el.textContent);
            const newHum = Math.max(20, Math.min(95, currentHum + (Math.random() - 0.5) * 2)).toFixed(0);
            el.textContent = newHum + '%';
        }
    });
}, 5000);

console.log('🌱 AgroMonitor Admin Dashboard');
console.log('👤 Role: Administrator');
