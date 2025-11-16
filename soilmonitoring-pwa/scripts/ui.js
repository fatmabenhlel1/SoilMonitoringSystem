// ui.js
let tempChart, humidityChart, npkChart;

function createChart(ctx, label, color) {
    return new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label,
                data: [],
                fill: false,
                borderColor: color,
                tension: 0.2
            }]
        },
        options: {
            responsive: true,
            scales: { x: { display: false } }
        }
    });
}

function initCharts() {
    const tctx = document.getElementById('tempChart').getContext('2d');
    const hctx = document.getElementById('humidityChart').getContext('2d');
    const nctx = document.getElementById('npkChart').getContext('2d');

    tempChart = createChart(tctx, 'Temperature (°C)', '#ff6b6b');
    humidityChart = createChart(hctx, 'Humidity (%)', '#1f78ff');
    npkChart = createChart(nctx, 'NPK', '#2ecc71');
}

function pushData(chart, label, value, maxPoints = 20) {
    chart.data.labels.push(label);
    chart.data.datasets[0].data.push(value);
    if (chart.data.labels.length > maxPoints) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
    }
    chart.update();
}

// Alerts
function addAlert(text) {
    const list = document.getElementById('alertList');
    const li = document.createElement('li');
    li.className = 'alert-item';
    li.textContent = `${new Date().toLocaleTimeString()} — ${text}`;
    list.prepend(li);
}

// Update DOM when new sensor data arrive
function handleSensorData(data, thresholds = { temp: 35, hum: 25 }) {
    const tsLabel = new Date(data.timestamp).toLocaleTimeString();
    pushData(tempChart, tsLabel, data.temperature);
    pushData(humidityChart, tsLabel, data.humidity);
    pushData(npkChart, tsLabel, data.npk);

    document.getElementById('lastUpdate').textContent = tsLabel;

    // Simple règles d'alerte
    if (data.temperature >= thresholds.temp) addAlert(`Temperature élevée: ${data.temperature}°C`);
    if (data.humidity <= thresholds.hum) addAlert(`Humidité faible: ${data.humidity}%`);
}

export { initCharts, handleSensorData, addAlert };
