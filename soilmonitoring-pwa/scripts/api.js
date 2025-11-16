// api.js
// Point d'intégration pour backend / middleware
// Remplace les fonctions mock par des fetch() ou WebSocket vers ton backend.

const Api = (() => {
    // MOCK: génère données aléatoires
    function mockSensorData() {
        const now = new Date().toISOString();
        return {
            timestamp: now,
            temperature: 18 + Math.round(Math.random() * 12),    // 18-30°C
            humidity: 30 + Math.round(Math.random() * 50),       // 30-80%
            npk: 8 + Math.round(Math.random() * 8)               // 8-16 index
        };
    }

    // Appel REST (placeholder)
    async function fetchLatest() {
        // Si tu as une API : return fetch('/api/sensors/latest').then(r => r.json())
        return new Promise(resolve => setTimeout(() => resolve(mockSensorData()), 300));
    }

    // WebSocket stub : retourne un objet "socket-like" avec onmessage callback
    function connectWS(wsUrl, onMessage) {
        // Si tu as un vrai ws : const ws = new WebSocket(wsUrl); ws.onmessage = e => onMessage(JSON.parse(e.data))
        // Ici on simule un flux :
        const interval = setInterval(() => {
            onMessage(mockSensorData());
        }, 3000);

        return {
            close: () => clearInterval(interval)
        };
    }

    return { fetchLatest, connectWS };
})();
