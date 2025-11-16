// app.js - orchestrateur
document.addEventListener('DOMContentLoaded', () => {
    // Simple router : charger pages/dashboard or pages/settings
    const main = document.getElementById('app');

    const loadPage = async (page) => {
        const res = await fetch(`pages/${page}.html`);
        const html = await res.text();
        main.innerHTML = html;

        if (page === 'dashboard') {
            // init charts & connect to data stream
            // Si ui.js export ne marche pas, appelle fonctions globales
            if (typeof initCharts === 'function') initCharts();

            // load initial
            Api.fetchLatest().then(d => {
                if (typeof handleSensorData === 'function') handleSensorData(d);
            });

            // connect WS (mock)
            window._ws = Api.connectWS('wss://your-backend-ws', (data) => {
                if (typeof handleSensorData === 'function') handleSensorData(data);
            });

            // bouton refresh
            const btn = document.getElementById('btn-refresh');
            if (btn) btn.addEventListener('click', () => {
                Api.fetchLatest().then(d => { if (typeof handleSensorData === 'function') handleSensorData(d); });
            });
        }

        if (page === 'settings') {
            const form = document.getElementById('settingsForm');
            if (form) {
                form.addEventListener('submit', e => {
                    e.preventDefault();
                    alert('Settings saved (local demo)');
                });
            }
        }
    };

    // Navigation
    document.getElementById('nav-home').addEventListener('click', () => {
        document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
        document.getElementById('nav-home').classList.add('active');
        loadPage('dashboard');
    });
    document.getElementById('nav-settings').addEventListener('click', () => {
        document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
        document.getElementById('nav-settings').classList.add('active');
        loadPage('settings');
    });

    // register SW
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('./sw.js').then(() => console.log('SW registered')).catch(e => console.warn('SW failed', e));
    }

    // initial page
    loadPage('dashboard');
});
