/**
 * Charts Module (User Dashboard)
 * Manages Chart.js instances for 6 sensor data visualizations
 */

let CHARTS = {
    temperature: null,
    humidity: null,
    npk: null,
    moisture: null,
    ph: null,
    rainfall: null
};

/**
 * Initialize all charts
 */
export function initializeCharts() {
    console.log('📊 Initializing charts...');

    // Temperature Chart
    const tempCtx = document.getElementById('temperatureChart');
    if (tempCtx) {
        CHARTS.temperature = new Chart(tempCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Temperature (°C)',
                    data: [],
                    borderColor: 'rgb(255, 99, 132)',
                    backgroundColor: 'rgba(255, 99, 132, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        title: { display: true, text: 'Temperature (°C)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // Humidity Chart
    const humidityCtx = document.getElementById('humidityChart');
    if (humidityCtx) {
        CHARTS.humidity = new Chart(humidityCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Humidity (%)',
                    data: [],
                    borderColor: 'rgb(54, 162, 235)',
                    backgroundColor: 'rgba(54, 162, 235, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: { display: true, text: 'Humidity (%)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // NPK Bar Chart
    const npkCtx = document.getElementById('npkChart');
    if (npkCtx) {
        CHARTS.npk = new Chart(npkCtx, {
            type: 'bar',
            data: {
                labels: ['Nitrogen (N)', 'Phosphorus (P)', 'Potassium (K)'],
                datasets: [{
                    label: 'NPK Levels (mg/kg)',
                    data: [0, 0, 0],
                    backgroundColor: [
                        'rgba(75, 192, 192, 0.7)',
                        'rgba(255, 206, 86, 0.7)',
                        'rgba(153, 102, 255, 0.7)'
                    ],
                    borderColor: [
                        'rgb(75, 192, 192)',
                        'rgb(255, 206, 86)',
                        'rgb(153, 102, 255)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'mg/kg', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // Soil Moisture Chart
    const moistureCtx = document.getElementById('moistureChart');
    if (moistureCtx) {
        CHARTS.moisture = new Chart(moistureCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'Soil Moisture (%)',
                    data: [],
                    borderColor: 'rgb(139, 195, 74)',
                    backgroundColor: 'rgba(139, 195, 74, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: { display: true, text: 'Moisture (%)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // pH Chart
    const phCtx = document.getElementById('phChart');
    if (phCtx) {
        CHARTS.ph = new Chart(phCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: 'pH Level',
                    data: [],
                    borderColor: 'rgb(255, 159, 64)',
                    backgroundColor: 'rgba(255, 159, 64, 0.1)',
                    tension: 0.4,
                    fill: true,
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true, mode: 'index', intersect: false }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        min: 0,
                        max: 14,
                        title: { display: true, text: 'pH Level', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    // Rainfall Chart
    const rainfallCtx = document.getElementById('rainfallChart');
    if (rainfallCtx) {
        CHARTS.rainfall = new Chart(rainfallCtx, {
            type: 'bar',
            data: {
                labels: [],
                datasets: [{
                    label: 'Rainfall (mm)',
                    data: [],
                    backgroundColor: 'rgba(33, 150, 243, 0.7)',
                    borderColor: 'rgb(33, 150, 243)',
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: true, position: 'top' },
                    tooltip: { enabled: true }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'Rainfall (mm)', font: { size: 12, weight: 'bold' } },
                        grid: { color: 'rgba(0,0,0,0.05)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    console.log('✅ Charts initialized');
}

/**
 * Update charts with historical data
 */
export async function updateChartsWithHistoricalData(fieldId) {
    try {
        console.log('📊 Loading chart data for field:', fieldId);

        const readings = await window.ApiService.getReadingsByField(fieldId);

        if (!readings || readings.length === 0) {
            console.warn('⚠️ No historical data available for charts');
            const noDataEl = document.getElementById('noChartsData');
            if (noDataEl) noDataEl.style.display = 'block';
            return;
        }

        const noDataEl = document.getElementById('noChartsData');
        if (noDataEl) noDataEl.style.display = 'none';

        readings.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
        const recentReadings = readings.slice(-20);

        const labels = recentReadings.map(r => {
            const date = new Date(r.timestamp);
            return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        });

        const temperatures = recentReadings.map(r => r.data.temperature);
        const humidity = recentReadings.map(r => r.data.humidity);
        const moisture = recentReadings.map(r => r.data.soilMoisture);
        const ph = recentReadings.map(r => r.data.pH);
        const rainfall = recentReadings.map(r => r.data.rainfall);

        if (CHARTS.temperature) {
            CHARTS.temperature.data.labels = labels;
            CHARTS.temperature.data.datasets[0].data = temperatures;
            CHARTS.temperature.update();
        }

        if (CHARTS.humidity) {
            CHARTS.humidity.data.labels = labels;
            CHARTS.humidity.data.datasets[0].data = humidity;
            CHARTS.humidity.update();
        }

        if (CHARTS.moisture) {
            CHARTS.moisture.data.labels = labels;
            CHARTS.moisture.data.datasets[0].data = moisture;
            CHARTS.moisture.update();
        }

        if (CHARTS.ph) {
            CHARTS.ph.data.labels = labels;
            CHARTS.ph.data.datasets[0].data = ph;
            CHARTS.ph.update();
        }

        if (CHARTS.rainfall) {
            CHARTS.rainfall.data.labels = labels;
            CHARTS.rainfall.data.datasets[0].data = rainfall;
            CHARTS.rainfall.update();
        }

        const latestReading = recentReadings[recentReadings.length - 1];
        if (CHARTS.npk && latestReading) {
            CHARTS.npk.data.datasets[0].data = [
                latestReading.data.nitrogen,
                latestReading.data.phosphorus,
                latestReading.data.potassium
            ];
            CHARTS.npk.update();
        }

        console.log('✅ Charts updated with', recentReadings.length, 'data points');

    } catch (error) {
        console.error('❌ Error updating charts:', error);
        const noDataEl = document.getElementById('noChartsData');
        if (noDataEl) noDataEl.style.display = 'block';
    }
}

/**
 * Update charts in real-time
 */
export function updateChartsRealtime(reading) {
    const timestamp = new Date(reading.timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });

    // Temperature
    if (CHARTS.temperature) {
        CHARTS.temperature.data.labels.push(timestamp);
        CHARTS.temperature.data.datasets[0].data.push(reading.data.temperature);

        if (CHARTS.temperature.data.labels.length > 20) {
            CHARTS.temperature.data.labels.shift();
            CHARTS.temperature.data.datasets[0].data.shift();
        }

        CHARTS.temperature.update('none');
    }

    // Humidity
    if (CHARTS.humidity) {
        CHARTS.humidity.data.labels.push(timestamp);
        CHARTS.humidity.data.datasets[0].data.push(reading.data.humidity);

        if (CHARTS.humidity.data.labels.length > 20) {
            CHARTS.humidity.data.labels.shift();
            CHARTS.humidity.data.datasets[0].data.shift();
        }

        CHARTS.humidity.update('none');
    }

    // Soil Moisture
    if (CHARTS.moisture) {
        CHARTS.moisture.data.labels.push(timestamp);
        CHARTS.moisture.data.datasets[0].data.push(reading.data.soilMoisture);

        if (CHARTS.moisture.data.labels.length > 20) {
            CHARTS.moisture.data.labels.shift();
            CHARTS.moisture.data.datasets[0].data.shift();
        }

        CHARTS.moisture.update('none');
    }

    // pH
    if (CHARTS.ph) {
        CHARTS.ph.data.labels.push(timestamp);
        CHARTS.ph.data.datasets[0].data.push(reading.data.pH);

        if (CHARTS.ph.data.labels.length > 20) {
            CHARTS.ph.data.labels.shift();
            CHARTS.ph.data.datasets[0].data.shift();
        }

        CHARTS.ph.update('none');
    }

    // NPK
    if (CHARTS.npk) {
        CHARTS.npk.data.datasets[0].data = [
            reading.data.nitrogen,
            reading.data.phosphorus,
            reading.data.potassium
        ];
        CHARTS.npk.update('none');
    }

    // Rainfall
    if (CHARTS.rainfall) {
        CHARTS.rainfall.data.labels.push(timestamp);
        CHARTS.rainfall.data.datasets[0].data.push(reading.data.rainfall);

        if (CHARTS.rainfall.data.labels.length > 20) {
            CHARTS.rainfall.data.labels.shift();
            CHARTS.rainfall.data.datasets[0].data.shift();
        }

        CHARTS.rainfall.update('none');
    }
}

/**
 * Refresh charts manually
 */
export async function refreshChartsData(fields) {
    if (fields && fields.length > 0) {
        await updateChartsWithHistoricalData(fields[0].id);
    }
}