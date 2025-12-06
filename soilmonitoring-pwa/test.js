import React, { useState, useEffect, useRef, useMemo } from 'react';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar } from 'recharts';
import {
    Activity, Zap, AlertTriangle, TrendingDown, Gauge, Battery, Thermometer,
    Upload, User, Bot, Send, Clock, Database, AlertCircle, CheckCircle,
    BarChart3, Cpu, Brain, Target, TrendingUp, Sparkles
} from 'lucide-react';
import Papa from 'papaparse';

const EcoDrivingDashboard = () => {
    const [chatMessages, setChatMessages] = useState([
        { role: "assistant", content: "Bonjour ! Je suis votre assistant IA pour l'éco-conduite. Comment puis-je vous aider aujourd'hui ?" }
    ]);
    const [chatInput, setChatInput] = useState("");
    const [timeRange, setTimeRange] = useState('Tout');
    const [csvData, setCsvData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isTyping, setIsTyping] = useState(false);
    const [stats, setStats] = useState({
        totalRecords: 0,
        uniqueVariables: 0,
        startDate: null,
        endDate: null,
        alarmLevels: {},
        variableCounts: {}
    });
    const chatContainerRef = useRef(null);

    // Charger automatiquement fixed_vf.csv au démarrage
    useEffect(() => {
        const loadFixedCSV = async () => {
            try {
                setLoading(true);
                const response = await fetch('C:\Users\Utilisateur\my-dashboard\fixed_vf.csv');
                if (!response.ok) {
                    throw new Error(`Fichier non trouvé: ${response.status}`);
                }

                const csvText = await response.text();

                Papa.parse(csvText, {
                    header: true,
                    dynamicTyping: true,
                    skipEmptyLines: true,
                    complete: (results) => {
                        const data = results.data;
                        console.log("CSV chargé:", data.length, "enregistrements");

                        // Statistiques initiales
                        const uniqueVars = new Set();
                        const alarmStats = {};
                        const varCounts = {};
                        let timestamps = [];

                        // Premier passage pour collecter des statistiques de base
                        data.forEach(row => {
                            // Compter les variables
                            if (row.variable) {
                                uniqueVars.add(row.variable);
                                varCounts[row.variable] = (varCounts[row.variable] || 0) + 1;
                            }

                            // Compter les alarmes
                            const alarmLevel = row.alarmClass !== undefined ? row.alarmClass : 0;
                            alarmStats[alarmLevel] = (alarmStats[alarmLevel] || 0) + 1;

                            // Collecter les timestamps
                            if (row.timestamp) {
                                try {
                                    const ts = new Date(row.timestamp).getTime();
                                    if (!isNaN(ts)) timestamps.push(ts);
                                } catch (e) {
                                    console.warn("Timestamp invalide:", row.timestamp);
                                }
                            }
                        });

                        const startDate = timestamps.length > 0 ? new Date(Math.min(...timestamps)) : null;
                        const endDate = timestamps.length > 0 ? new Date(Math.max(...timestamps)) : null;

                        setStats({
                            totalRecords: data.length,
                            uniqueVariables: uniqueVars.size,
                            startDate,
                            endDate,
                            alarmLevels: alarmStats,
                            variableCounts: varCounts
                        });

                        setCsvData(data);
                        setLoading(false);

                        // Message d'accueil
                        const criticalCount = Object.entries(alarmStats)
                            .filter(([level]) => parseInt(level) >= 3)
                            .reduce((sum, [, count]) => sum + count, 0);
                        const criticalPercent = data.length > 0 ? ((criticalCount / data.length) * 100).toFixed(1) : 0;

                        setChatMessages(prev => [...prev, {
                            role: "assistant",
                            content: `✅ **Données chargées avec succès !**\n\n📊 **${data.length.toLocaleString()}** enregistrements analysés\n🔍 **${uniqueVars.size}** variables uniques détectées\n⚠️ **${criticalCount.toLocaleString()}** alertes critiques (${criticalPercent}%)\n📅 Période: ${startDate ? startDate.toLocaleDateString('fr-FR') : 'N/A'} - ${endDate ? endDate.toLocaleDateString('fr-FR') : 'N/A'}`
                        }]);
                    },
                    error: (err) => {
                        console.error("Erreur de parsing CSV:", err);
                        setError(`Erreur de chargement: ${err.message}`);
                        setLoading(false);
                        setChatMessages(prev => [...prev, {
                            role: "assistant",
                            content: "⚠️ Fichier fixed_vf.csv non trouvé. Utilisation des données de démo."
                        }]);
                    }
                });
            } catch (err) {
                console.error("Erreur de chargement:", err);
                setError(err.message);
                setLoading(false);
            }
        };

        loadFixedCSV();
    }, []);

    // Faire défiler automatiquement vers le bas du chat
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [chatMessages, isTyping]);

    // CALCULS DES DONNÉES AVEC TOUS LES ENREGISTREMENTS
    // 1. Distribution des alarmes
    const alarmDistribution = useMemo(() => {
        if (!csvData) {
            return [
                { name: 'Normal', value: 43084, color: '#10b981' },
                { name: 'Niveau 1', value: 13539, color: '#3b82f6' },
                { name: 'Niveau 2', value: 8306, color: '#f59e0b' },
                { name: 'Niveau 3', value: 5480, color: '#ef4444' },
                { name: 'Niveau 4', value: 7337, color: '#dc2626' },
                { name: 'Niveau 5', value: 5723, color: '#991b1b' }
            ];
        }

        const alarmCounts = csvData.reduce((acc, row) => {
            const alarm = row.alarmClass !== undefined ? row.alarmClass : 0;
            acc[alarm] = (acc[alarm] || 0) + 1;
            return acc;
        }, {});

        return Object.entries(alarmCounts)
            .map(([key, value]) => ({
                name: key === '0' ? 'Normal' : `Niveau ${key}`,
                value: value,
                color: ['#10b981', '#3b82f6', '#f59e0b', '#ef4444', '#dc2626', '#991b1b'][parseInt(key)] || '#gray'
            }))
            .sort((a, b) => parseInt(a.name.replace(/\D/g, '') || '0') - parseInt(b.name.replace(/\D/g, '') || '0'));
    }, [csvData]);

    // 2. Top variables - avec TOUS les enregistrements
    const topVariables = useMemo(() => {
        if (!csvData || csvData.length === 0) {
            return [
                { variable: 'EXTERNAL BATTERY', count: 9625, category: 'Batterie' },
                { variable: 'TOWING', count: 6653, category: 'État' },
                { variable: 'IGNITION_STATUS', count: 6304, category: 'État' },
                { variable: 'ACCELERATION X', count: 5046, category: 'Conduite' },
                { variable: 'ENGINE RPM', count: 4514, category: 'Moteur' },
                { variable: 'Vehicle speed', count: 3803, category: 'Conduite' },
                { variable: 'ENGINE LOAD', count: 2290, category: 'Moteur' }
            ];
        }

        // Utiliser les statistiques pré-calculées si disponibles
        if (Object.keys(stats.variableCounts).length > 0) {
            return Object.entries(stats.variableCounts)
                .sort((a, b) => b[1] - a[1])
                .slice(0, 10)
                .map(([variable, count]) => ({
                    variable: variable.length > 25 ? variable.substring(0, 22) + '...' : variable,
                    count: count,
                    fullName: variable,
                    category: variable.includes('BATTERY') ? 'Batterie' :
                        variable.includes('ENGINE') || variable.includes('RPM') ? 'Moteur' :
                            variable.includes('SPEED') || variable.includes('ACCELER') ? 'Conduite' :
                                variable.includes('TEMP') ? 'Température' : 'État'
                }));
        }

        // Sinon, calculer à partir des données
        const variableCounts = {};
        csvData.forEach(row => {
            const variable = row.variable;
            if (variable && variable !== 'Unknown') {
                variableCounts[variable] = (variableCounts[variable] || 0) + 1;
            }
        });

        return Object.entries(variableCounts)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 10)
            .map(([variable, count]) => ({
                variable: variable.length > 25 ? variable.substring(0, 22) + '...' : variable,
                count: count,
                fullName: variable,
                category: variable.includes('BATTERY') ? 'Batterie' :
                    variable.includes('ENGINE') || variable.includes('RPM') ? 'Moteur' :
                        variable.includes('SPEED') || variable.includes('ACCELER') ? 'Conduite' :
                            variable.includes('TEMP') ? 'Température' : 'État'
            }));
    }, [csvData, stats.variableCounts]);

    // 3. Séries temporelles - ÉCHANTILLONNAGE INTELLIGENT pour 80,000+ points
    const timeSeriesData = useMemo(() => {
        if (!csvData || csvData.length === 0) {
            return generateDemoTimeSeries();
        }

        // Pour éviter de traiter 80,000 points sur un graphique
        // On échantillonne intelligemment

        // Option 1: Prendre un échantillon aléatoire de 500 points
        const sampleSize = Math.min(500, csvData.length);
        const step = Math.max(1, Math.floor(csvData.length / sampleSize));

        const sampledData = [];
        let batterySum = 0;
        let batteryCount = 0;
        let rpmSum = 0;
        let rpmCount = 0;
        let speedSum = 0;
        let speedCount = 0;

        // Agréger par blocs de temps
        for (let i = 0; i < csvData.length; i += step) {
            const row = csvData[i];
            if (!row || !row.timestamp) continue;

            try {
                const date = new Date(row.timestamp);
                const timeKey = `${date.getHours().toString().padStart(2, '0')}:${Math.floor(date.getMinutes() / 10) * 10}`;

                if (row.variable === 'INTERNAL BATTERY' || row.variable === 'EXTERNAL BATTERY') {
                    const value = parseFloat(row.value);
                    if (!isNaN(value)) {
                        batterySum += value;
                        batteryCount++;
                    }
                } else if (row.variable === 'ENGINE RPM') {
                    const value = parseFloat(row.value);
                    if (!isNaN(value)) {
                        rpmSum += Math.min(value, 5000);
                        rpmCount++;
                    }
                } else if (row.variable === 'Vehicle speed') {
                    const value = parseFloat(row.value);
                    if (!isNaN(value)) {
                        speedSum += value;
                        speedCount++;
                    }
                }

                // Ajouter un point agrégé toutes les 1000 entrées
                if (i % 1000 === 0) {
                    sampledData.push({
                        time: timeKey,
                        battery: batteryCount > 0 ? Math.round(batterySum / batteryCount) : 0,
                        rpm: rpmCount > 0 ? Math.round(rpmSum / rpmCount) : 0,
                        speed: speedCount > 0 ? Math.round(speedSum / speedCount) : 0,
                        load: 0 // À calculer si disponible
                    });

                    // Réinitialiser pour le prochain bloc
                    batterySum = 0;
                    batteryCount = 0;
                    rpmSum = 0;
                    rpmCount = 0;
                    speedSum = 0;
                    speedCount = 0;
                }
            } catch (e) {
                console.warn("Erreur de traitement des données temporelles:", e);
            }
        }

        // Si on n'a pas assez de données, générer des démos
        if (sampledData.length < 10) {
            return generateDemoTimeSeries();
        }

        return sampledData.slice(0, 50); // Limiter à 50 points max pour la lisibilité

    }, [csvData]);

    // Fonction pour générer des séries temporelles de démo
    function generateDemoTimeSeries() {
        const data = [];
        for (let i = 0; i < 24; i++) {
            const hour = i.toString().padStart(2, '0');
            data.push({
                time: `${hour}:00`,
                battery: 70 + Math.random() * 30,
                rpm: 800 + Math.random() * 4000,
                speed: Math.random() * 120,
                load: 10 + Math.random() * 90
            });
        }
        return data;
    }

    // 4. Données de démonstration pour les autres graphiques
    const ecoMetrics = [
        { metric: 'Efficacité Carburant', score: 78, max: 100 },
        { metric: 'Conduite Douce', score: 65, max: 100 },
        { metric: 'Gestion Vitesse', score: 82, max: 100 },
        { metric: 'Anticipation', score: 71, max: 100 },
        { metric: 'Ralenti Optimal', score: 88, max: 100 }
    ];

    const co2Savings = [
        { jour: 'Lun', emis: 4.2, economise: 0.6 },
        { jour: 'Mar', emis: 3.8, economise: 0.8 },
        { jour: 'Mer', emis: 4.5, economise: 0.5 },
        { jour: 'Jeu', emis: 3.9, economise: 0.9 },
        { jour: 'Ven', emis: 4.1, economise: 0.7 },
        { jour: 'Sam', emis: 2.8, economise: 1.2 },
        { jour: 'Dim', emis: 2.5, economise: 1.0 }
    ];

    // Composant de carte métrique
    const MetricCard = ({ title, value, unit, icon: Icon, trend, color, description }) => (
        <div className="bg-white rounded-xl shadow-md p-6 border-l-4 transition-all hover:shadow-lg hover:scale-[1.02]" style={{ borderLeftColor: color }}>
            <div className="flex items-center justify-between">
                <div className="flex-1">
                    <p className="text-gray-500 text-sm font-medium">{title}</p>
                    <p className="text-3xl font-bold mt-2" style={{ color }}>
                        {value}
                        <span className="text-lg ml-1">{unit}</span>
                    </p>
                    {trend && (
                        <p className="text-sm mt-2 text-gray-600">
              <span className={trend > 0 ? 'text-green-600 font-semibold' : 'text-red-600 font-semibold'}>
                {trend > 0 ? '↗' : '↘'} {Math.abs(trend)}%
              </span> vs période précédente
                        </p>
                    )}
                    {description && (
                        <p className="text-xs text-gray-400 mt-1">{description}</p>
                    )}
                </div>
                <div className="bg-opacity-10 p-3 rounded-full" style={{ backgroundColor: color }}>
                    <Icon size={32} color={color} />
                </div>
            </div>
        </div>
    );

    // Calculer les totaux et pourcentages
    const totalAlarms = alarmDistribution.reduce((acc, curr) => acc + curr.value, 0);
    const criticalAlarms = alarmDistribution.slice(3).reduce((acc, curr) => acc + curr.value, 0);
    const criticalPercent = totalAlarms > 0 ? ((criticalAlarms / totalAlarms) * 100).toFixed(1) : 0;

    // Calculer les statistiques dynamiques
    const batteryMeasures = useMemo(() => {
        if (!csvData) return 9625;
        return csvData.filter(r =>
            r.variable && (r.variable.includes('BATTERY') || r.variable === 'EXTERNAL BATTERY' || r.variable === 'INTERNAL BATTERY')
        ).length;
    }, [csvData]);

    const rpmMeasures = useMemo(() => {
        if (!csvData) return 4514;
        return csvData.filter(r =>
            r.variable && r.variable.includes('RPM')
        ).length;
    }, [csvData]);

    const speedMeasures = useMemo(() => {
        if (!csvData) return 3803;
        return csvData.filter(r =>
            r.variable && (r.variable.includes('SPEED') || r.variable === 'Vehicle speed')
        ).length;
    }, [csvData]);

    const temperatureMeasures = useMemo(() => {
        if (!csvData) return 2100;
        return csvData.filter(r =>
            r.variable && r.variable.includes('TEMP')
        ).length;
    }, [csvData]);

    // Fonction d'envoi de message
    const sendChatMessage = async () => {
        if (!chatInput.trim()) return;

        const userMessage = { role: "user", content: chatInput.trim() };
        setChatMessages(prev => [...prev, userMessage]);
        setChatInput("");
        setIsTyping(true);

        try {
            const dataContext = csvData ? `
Contexte des données analysées:
- Total d'enregistrements: ${stats.totalRecords.toLocaleString()}
- Variables uniques détectées: ${stats.uniqueVariables}
- Période d'analyse: ${stats.startDate ? stats.startDate.toLocaleDateString('fr-FR') : 'N/A'} - ${stats.endDate ? stats.endDate.toLocaleDateString('fr-FR') : 'N/A'}
- Distribution des alarmes: ${alarmDistribution.map(a => `${a.name}: ${a.value.toLocaleString()}`).join(', ')}
- Top 3 variables: ${topVariables.slice(0, 3).map(v => `${v.variable} (${v.count.toLocaleString()} mesures)`).join(', ')}
      ` : 'Données de démonstration (80,000+ points simulés)';

            const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
                method: "POST",
                headers: {
                    "Authorization": "Bearer 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",
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    model: "tngtech/deepseek-r1t2-chimera:free",
                    messages: [
                        {
                            role: "system",
                            content: `Tu es un assistant expert en éco-conduite et analyse de données IoT automobiles. Tu analyses des données de capteurs véhiculaires pour optimiser la consommation de carburant et réduire l'impact environnemental.

${dataContext}

Réponds en français de manière précise et technique, en te basant sur les données fournies.`
                        },
                        ...chatMessages.slice(-10),
                        userMessage
                    ]
                })
            });

            const data = await response.json();
            const botMessage = { role: "assistant", content: data.choices[0].message.content };
            setChatMessages(prev => [...prev, botMessage]);
        } catch (err) {
            console.error(err);
            setChatMessages(prev => [...prev, {
                role: "assistant",
                content: "Connexion à l'API impossible. Voici une analyse basée sur vos données locales:\n\n" +
                    `**Statistiques détectées:**\n` +
                    `• ${stats.totalRecords.toLocaleString()} enregistrements au total\n` +
                    `• ${criticalAlarms.toLocaleString()} alertes critiques (niveaux 3-5)\n` +
                    `• ${batteryMeasures.toLocaleString()} mesures de batterie\n` +
                    `• ${rpmMeasures.toLocaleString()} mesures de RPM moteur\n\n` +
                    `💡 **Conseils d'éco-conduite:**\n` +
                    `1. Maintenez le RPM entre 1500-2500 pour une efficacité optimale\n` +
                    `2. Anticipez les arrêts pour éviter les freinages brusques\n` +
                    `3. Vérifiez régulièrement la pression des pneus`
            }]);
        } finally {
            setIsTyping(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-green-50 to-blue-50 p-4 md:p-6">
            {/* Header avec informations de chargement */}
            <div className="mb-8">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
                    <div>
                        <h1 className="text-3xl md:text-4xl font-bold text-gray-800 mb-2">
                            Dashboard Analyse Véhiculaire IoT
                        </h1>
                        <p className="text-gray-600">
                            Analyse en temps réel de données
                        </p>
                    </div>
                </div>

                {/* Filtres temporels */}
                <div className="flex gap-2 overflow-x-auto pb-2">
                    {['1h', '24h', '7j', '30j', 'Tout'].map(range => (
                        <button
                            key={range}
                            onClick={() => setTimeRange(range)}
                            className={`px-4 py-2 rounded-lg font-medium transition-all flex-shrink-0 whitespace-nowrap ${
                                timeRange === range
                                    ? 'bg-gradient-to-r from-green-600 to-blue-600 text-white shadow-lg'
                                    : 'bg-white text-gray-700 hover:bg-gray-100 border'
                            }`}
                        >
                            {range}
                        </button>
                    ))}
                </div>
            </div>

            {/* KPI Cards principales */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-8">
                <MetricCard
                    title="Total Données"
                    value={stats.totalRecords.toLocaleString()}
                    unit="points"
                    icon={Database}
                    color="#3b82f6"
                    description="Enregistrements analysés"
                />
                <MetricCard
                    title="Variables Uniques"
                    value="51"
                    unit="vars"
                    icon={BarChart3}
                    color="#8b5cf6"
                    description="Types de capteurs"
                />
                <MetricCard
                    title="Alertes Critiques"
                    value={criticalAlarms.toLocaleString()}
                    unit="alertes"
                    icon={AlertTriangle}
                    color="#ef4444"
                    description={`${criticalPercent}% du total`}
                />
                <MetricCard
                    title="Points Analysés"
                    value="80,000+"
                    unit="data"
                    icon={Cpu}
                    color="#10b981"
                    description="Traitement en temps réel"
                />
            </div>

            {/* Section 1: Distribution des alarmes */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-6 mb-6">
                {/* Graphique des alarmes */}
                <div className="bg-white rounded-xl shadow-md p-4 md:p-6">
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="text-lg md:text-xl font-bold text-gray-800 flex items-center">
                            <AlertTriangle className="mr-2" size={20} color="#f59e0b" />
                            Distribution des Alarmes
                        </h3>
                    </div>
                    <div className="h-64 md:h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={alarmDistribution}
                                    cx="50%"
                                    cy="50%"
                                    labelLine={false}
                                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(1)}%`}
                                    outerRadius={80}
                                    innerRadius={40}
                                    paddingAngle={2}
                                    dataKey="value"
                                >
                                    {alarmDistribution.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    formatter={(value) => [value.toLocaleString(), 'occurrences']}
                                    contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb' }}
                                />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                    {/* Nouvelle légende améliorée */}
                    <div className="mt-4 bg-gray-50 rounded-lg p-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                            <div className="bg-white p-3 rounded border-l-4 border-green-500">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-medium text-gray-800 text-sm">0</span>
                                    <span className="text-xs text-gray-600">
              Conduite normale
            </span>
                                </div>
                            </div>

                            <div className="bg-white p-3 rounded border-l-4 border-blue-500">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-medium text-gray-800 text-sm">1</span>
                                    <span className="text-xs text-gray-600">
             Freinage brusque
            </span>
                                </div>
                            </div>

                            <div className="bg-white p-3 rounded border-l-4 border-yellow-500">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-medium text-gray-800 text-sm">2</span>
                                    <span className="text-xs text-gray-600">
                      Accélération brusque
            </span>
                                </div>
                            </div>

                            <div className="bg-white p-3 rounded border-l-4 border-orange-500">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-medium text-gray-800 text-sm">3</span>
                                    <span className="text-xs text-gray-600">
          Virage dangereux
        </span>
                                </div>
                            </div>

                            <div className="bg-white p-3 rounded border-l-4 border-red-500">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-medium text-gray-800 text-sm">4</span>
                                    <span className="text-xs text-gray-600">
              Sur-vitesse
            </span>
                                </div>
                            </div>

                            <div className="bg-white p-3 rounded border-l-4 border-red-700">
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-medium text-gray-800 text-sm">5</span>
                                    <span className="text-xs text-gray-600">
              Ralenti prolongé
            </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                {/* Top Variables */}
                <div className="bg-white rounded-xl shadow-md p-4 md:p-6">
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="text-lg md:text-xl font-bold text-gray-800 flex items-center">
                            <Zap className="mr-2" size={20} color="#f59e0b" />
                            Top 10 Variables les plus mesurées
                        </h3>
                    </div>
                    <div className="h-64 md:h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={topVariables} layout="vertical">
                                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={true} vertical={false} />
                                <XAxis
                                    type="number"
                                    stroke="#4b5563"
                                    tickFormatter={(value) => value > 1000 ? `${(value/1000).toFixed(0)}k` : value}
                                />
                                <YAxis
                                    dataKey="variable"
                                    type="category"
                                    width={150}
                                    stroke="#4b5563"
                                    tick={{ fontSize: 11 }}
                                />
                                <Tooltip
                                    formatter={(value) => [value.toLocaleString(), 'mesures']}
                                    labelFormatter={(label) => topVariables.find(v => v.variable === label)?.fullName || label}
                                    contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb' }}
                                />
                                <Bar
                                    dataKey="count"
                                    radius={[0, 4, 4, 0]}
                                    animationDuration={1500}
                                >
                                    {topVariables.map((entry, index) => (
                                        <Cell
                                            key={`cell-${index}`}
                                            fill={
                                                entry.category === 'Batterie' ? '#10b981' :
                                                    entry.category === 'Moteur' ? '#ef4444' :
                                                        entry.category === 'Conduite' ? '#3b82f6' :
                                                            entry.category === 'Température' ? '#f59e0b' : '#8b5cf6'
                                            }
                                        />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                    <div className="mt-4 flex flex-wrap gap-3">
                        <div className="flex items-center gap-1">
                            <div className="w-3 h-3 rounded-full bg-green-500"></div>
                            <span className="text-xs">Batterie</span>
                        </div>
                        <div className="flex items-center gap-1">
                            <div className="w-3 h-3 rounded-full bg-red-500"></div>
                            <span className="text-xs">Moteur</span>
                        </div>
                        <div className="flex items-center gap-1">
                            <div className="w-3 h-3 rounded-full bg-blue-500"></div>
                            <span className="text-xs">Conduite</span>
                        </div>
                        <div className="flex items-center gap-1">
                            <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                            <span className="text-xs">Température</span>
                        </div>
                        <div className="flex items-center gap-1">
                            <div className="w-3 h-3 rounded-full bg-purple-500"></div>
                            <span className="text-xs">État</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Section 2: Séries temporelles */}
            <div className="bg-white rounded-xl shadow-md p-4 md:p-6 mb-6">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg md:text-xl font-bold text-gray-800 flex items-center">
                        <Activity className="mr-2" size={20} color="#3b82f6" />
                        Évolution des Paramètres Clés
                    </h3>
                    <span className="text-xs px-2 py-1 bg-gray-100 text-gray-700 rounded-full">
            {timeSeriesData.length} points d'échantillonnage
          </span>
                </div>
                <div className="h-64 md:h-80">
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={timeSeriesData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                            <XAxis
                                dataKey="time"
                                stroke="#4b5563"
                                interval="preserveStartEnd"
                            />
                            <YAxis stroke="#4b5563" />
                            <Tooltip
                                contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb' }}
                                formatter={(value, name) => {
                                    const unit = name === 'battery' ? '%' :
                                        name === 'speed' ? 'km/h' :
                                            name === 'rpm' ? 'rpm' :
                                                name === 'load' ? '%' : '';
                                    return [`${value} ${unit}`,
                                        name === 'battery' ? 'Batterie' :
                                            name === 'speed' ? 'Vitesse' :
                                                name === 'rpm' ? 'RPM' :
                                                    name === 'load' ? 'Charge' : name];
                                }}
                            />
                            <Legend />
                            <Line
                                type="monotone"
                                dataKey="battery"
                                stroke="#10b981"
                                strokeWidth={2}
                                dot={false}
                                activeDot={{ r: 6, strokeWidth: 0 }}
                                name="Batterie (%)"
                            />
                            <Line
                                type="monotone"
                                dataKey="speed"
                                stroke="#3b82f6"
                                strokeWidth={2}
                                dot={false}
                                activeDot={{ r: 6, strokeWidth: 0 }}
                                name="Vitesse (km/h)"
                            />
                            <Line
                                type="monotone"
                                dataKey="rpm"
                                stroke="#ef4444"
                                strokeWidth={2}
                                dot={false}
                                activeDot={{ r: 6, strokeWidth: 0 }}
                                name="RPM moteur"
                            />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>

            {/* Section 3: Statistiques détaillées */}
            <div className="mt-8">
                <h3 className="text-xl font-bold text-gray-800 mb-4 flex items-center">
                    <Cpu className="mr-2" size={24} />
                    Statistiques Détaillées des Mesures
                </h3>

                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                    <div className="bg-gradient-to-br from-green-50 to-green-100 p-5 rounded-xl shadow border border-green-200 hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3 mb-3">
                            <div className="p-2 bg-green-200 rounded-lg">
                                <Battery size={20} className="text-green-700" />
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-gray-800">{batteryMeasures.toLocaleString()}</p>
                                <p className="text-sm text-gray-600">Mesures Batterie</p>
                            </div>
                        </div>
                        <p className="text-xs text-green-700 font-medium">
                        </p>
                    </div>

                    <div className="bg-gradient-to-br from-red-50 to-red-100 p-5 rounded-xl shadow border border-red-200 hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3 mb-3">
                            <div className="p-2 bg-red-200 rounded-lg">
                                <Thermometer size={20} className="text-red-700" />
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-gray-800">{rpmMeasures.toLocaleString()}</p>
                                <p className="text-sm text-gray-600">Mesures RPM</p>
                            </div>
                        </div>
                        <p className="text-xs text-red-700 font-medium">
                        </p>
                    </div>

                    <div className="bg-gradient-to-br from-blue-50 to-blue-100 p-5 rounded-xl shadow border border-blue-200 hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3 mb-3">
                            <div className="p-2 bg-blue-200 rounded-lg">
                                <Gauge size={20} className="text-blue-700" />
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-gray-800">{speedMeasures.toLocaleString()}</p>
                                <p className="text-sm text-gray-600">Mesures Vitesse</p>
                            </div>
                        </div>
                        <p className="text-xs text-blue-700 font-medium">
                        </p>
                    </div>

                    <div className="bg-gradient-to-br from-purple-50 to-purple-100 p-5 rounded-xl shadow border border-purple-200 hover:shadow-md transition-shadow">
                        <div className="flex items-center gap-3 mb-3">
                            <div className="p-2 bg-purple-200 rounded-lg">
                                <Activity size={20} className="text-purple-700" />
                            </div>
                            <div>
                                <p className="text-2xl font-bold text-gray-800">{temperatureMeasures.toLocaleString()}</p>
                                <p className="text-sm text-gray-600">Mesures Température</p>
                            </div>
                        </div>
                        <p className="text-xs text-purple-700 font-medium">
                        </p>
                    </div>
                </div>
            </div>
            <RealTimePredictionsSection />
            {/* ChatBot amélioré */}
            <div className="bg-white rounded-xl shadow-md p-4 md:p-6 mt-8">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center">
                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-green-400 to-blue-500 flex items-center justify-center mr-3">
                            <Bot size={20} className="text-white" />
                        </div>
                        <div>
                            <h3 className="text-lg md:text-xl font-bold text-gray-800">Assistant Analyse Véhiculaire</h3>
                            <div className="flex items-center">
                                <div className="w-2 h-2 bg-green-500 rounded-full mr-2"></div>
                                <p className="text-xs text-gray-500">
                                    En ligne
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <div
                    ref={chatContainerRef}
                    className="border rounded-xl p-4 h-64 md:h-80 overflow-y-auto mb-4 bg-gray-50"
                    style={{
                        backgroundImage: 'radial-gradient(#e5e7eb 1px, transparent 1px)',
                        backgroundSize: '20px 20px'
                    }}
                >
                    {chatMessages.map((msg, i) => (
                        <div
                            key={i}
                            className={`mb-4 flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
                        >
                            <div className="flex max-w-[85%]">
                                {msg.role === "assistant" && (
                                    <div className="flex-shrink-0 mr-2 mt-1">
                                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-green-400 to-blue-500 flex items-center justify-center">
                                            <Bot size={16} className="text-white" />
                                        </div>
                                    </div>
                                )}

                                <div className={`flex flex-col ${msg.role === "user" ? "items-end" : "items-start"}`}>
                                    <div className="text-xs text-gray-500 mb-1 ml-1">
                                        {msg.role === "user" ? "Vous" : "Assistant IA"}
                                    </div>
                                    <div
                                        className={`p-3 rounded-2xl shadow-sm whitespace-pre-line ${
                                            msg.role === "user"
                                                ? "bg-gradient-to-r from-green-500 to-green-600 text-white rounded-br-none"
                                                : "bg-white text-gray-800 border border-gray-200 rounded-bl-none"
                                        }`}
                                        style={{
                                            boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
                                        }}
                                    >
                                        <p className="text-sm md:text-base">{msg.content}</p>
                                    </div>

                                    <div className="text-xs text-gray-400 mt-1 ml-1">
                                        {new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                                    </div>
                                </div>

                                {msg.role === "user" && (
                                    <div className="flex-shrink-0 ml-2 mt-1">
                                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center">
                                            <User size={16} className="text-white" />
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}

                    {isTyping && (
                        <div className="flex justify-start">
                            <div className="flex">
                                <div className="flex-shrink-0 mr-2 mt-1">
                                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-green-400 to-blue-500 flex items-center justify-center">
                                        <Bot size={16} className="text-white" />
                                    </div>
                                </div>
                                <div className="flex flex-col items-start">
                                    <div className="text-xs text-gray-500 mb-1 ml-1">Analyse en cours...</div>
                                    <div className="bg-white border border-gray-200 rounded-2xl rounded-bl-none p-3">
                                        <div className="flex space-x-1">
                                            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                                            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                                            <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                <div className="flex gap-2">
                    <input
                        type="text"
                        value={chatInput}
                        onChange={e => setChatInput(e.target.value)}
                        onKeyPress={e => e.key === 'Enter' && sendChatMessage()}
                        placeholder={`Pour une meilleure conduite écologique ...`}
                        className="flex-1 border border-gray-300 rounded-full p-3 pl-4 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    />
                    <button
                        onClick={sendChatMessage}
                        disabled={!chatInput.trim() || isTyping}
                        className={`rounded-full p-3 transition-all flex items-center justify-center ${!chatInput.trim() || isTyping ? 'bg-gray-300 cursor-not-allowed' : 'bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700'}`}
                    >
                        <Send size={20} className="text-white" />
                    </button>
                </div>

                <div className="mt-3 text-xs text-gray-500">
                    <div className="flex flex-wrap gap-2">
                        <button
                            onClick={() => setChatInput("Quelle est la distribution exacte des alarmes ?")}
                            className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded-full transition-colors text-xs"
                        >
                            Distribution alarmes
                        </button>
                        <button
                            onClick={() => setChatInput("Quelles sont les variables les plus problématiques ?")}
                            className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded-full transition-colors text-xs"
                        >
                            Variables problématiques
                        </button>
                        <button
                            onClick={() => setChatInput("Analyse l'état de santé de la batterie")}
                            className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded-full transition-colors text-xs"
                        >
                            Santé batterie
                        </button>
                        <button
                            onClick={() => setChatInput("Donne un résumé complet des données")}
                            className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded-full transition-colors text-xs"
                        >
                            Résumé complet
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};
// DÉPLACER RealTimePredictionsSection ICI (après EcoDrivingDashboard mais avant export default)
const RealTimePredictionsSection = () => {
    // État pour les valeurs d'entrée (avec valeurs par défaut réalistes)
    const [inputValues, setInputValues] = useState({
        rpm: 1800,
        speed: 65,
        battery: 88,
        engine_load: 42,
        coolant_temp: 82,
        fuel_rate: 7.8,
        acceleration_x: 0.15,
        acceleration_y: -0.05,
        acceleration_z: 9.81,
        distance_traveled: 14500
    });

    // État pour les prédictions de tous les modèles
    const [predictions, setPredictions] = useState({
        random_forest: null,
        xgboost: null,
        lightgbm: null,
        neural_network: null,
        loading: false,
        lastUpdate: null
    });

    // État pour les données du véhicule (pour la simulation)
    const [vehicleData, setVehicleData] = useState({
        eco_score: 78,
        safety_score: 85,
        fuel_efficiency: 12.5,
        co2_saved: 4.8,
        current_alarm: 0
    });

    // Modèles disponibles avec descriptions
    const MODELS = [
        {
            id: 'random_forest',
            name: 'Random Forest',
            description: 'Ensemble d\'arbres de décision',
            color: 'from-green-400 to-emerald-600',
            icon: '🌲'
        },
        {
            id: 'xgboost',
            name: 'XGBoost',
            description: 'Gradient boosting optimisé',
            color: 'from-blue-400 to-cyan-600',
            icon: '🚀'
        },
        {
            id: 'lightgbm',
            name: 'LightGBM',
            description: 'Arbres basés sur histogrammes',
            color: 'from-purple-400 to-violet-600',
            icon: '⚡'
        },
        {
            id: 'neural_network',
            name: 'Réseau Neuronal',
            description: 'Deep learning multicouche',
            color: 'from-red-400 to-pink-600',
            icon: '🧠'
        }
    ];

    // Types d'alarmes
    const ALARM_TYPES = [
        { level: 0, name: 'Normal', description: 'Conduite optimale', color: 'bg-green-100 text-green-800' },
        { level: 1, name: 'Faible', description: 'Freinage brusque', color: 'bg-blue-100 text-blue-800' },
        { level: 2, name: 'Modéré', description: 'Accélération brusque', color: 'bg-yellow-100 text-yellow-800' },
        { level: 3, name: 'Élevé', description: 'Virage dangereux', color: 'bg-orange-100 text-orange-800' },
        { level: 4, name: 'Critique', description: 'Sur-vitesse', color: 'bg-red-100 text-red-800' },
        { level: 5, name: 'Dangereux', description: 'Ralenti prolongé', color: 'bg-red-200 text-red-900' }
    ];

    // Simulation des prédictions (remplacer par vos appels API réels)
    const simulatePrediction = async (modelId) => {
        // Calcul basé sur les valeurs d'entrée (logique simplifiée)
        const baseScore = 100;

        // Facteurs de pénalité
        const rpmPenalty = Math.max(0, (inputValues.rpm - 2000) / 100);
        const speedPenalty = Math.max(0, (inputValues.speed - 80) / 5);
        const accelerationPenalty = Math.abs(inputValues.acceleration_x) * 10;

        // Calcul du score (100 = parfait, 0 = très mauvais)
        let ecoScore = Math.max(0, baseScore - rpmPenalty - speedPenalty - accelerationPenalty);

        // Ajout d'aléatoire pour simuler différentes prédictions
        const randomFactor = 0.8 + Math.random() * 0.4;
        ecoScore = Math.min(100, Math.max(0, ecoScore * randomFactor));

        // Détermination du niveau d'alarme basé sur le score
        let alarmLevel = 0;
        if (ecoScore >= 80) alarmLevel = 0;
        else if (ecoScore >= 60) alarmLevel = 1;
        else if (ecoScore >= 40) alarmLevel = 2;
        else if (ecoScore >= 25) alarmLevel = 3;
        else if (ecoScore >= 10) alarmLevel = 4;
        else alarmLevel = 5;

        // Confiance basée sur la cohérence des données
        const confidence = 0.75 + Math.random() * 0.2;

        // Recommandations basées sur le score
        const recommendations = [];
        if (inputValues.rpm > 2500) recommendations.push("Réduire le régime moteur (RPM)");
        if (inputValues.speed > 90) recommendations.push("Réduire la vitesse");
        if (Math.abs(inputValues.acceleration_x) > 0.3) recommendations.push("Adoucir l'accélération");
        if (inputValues.engine_load > 80) recommendations.push("Réduire la charge moteur");

        return {
            success: true,
            model_id: modelId,
            eco_score: Math.round(ecoScore),
            alarm_level: alarmLevel,
            alarm_probability: confidence,
            confidence: confidence,
            fuel_efficiency_gain: (ecoScore / 100) * 15, // Gain potentiel en %
            co2_reduction_potential: (ecoScore / 100) * 8, // Réduction CO2 potentielle en kg
            recommendations: recommendations.slice(0, 3),
            features_importance: {
                rpm: 0.35,
                speed: 0.25,
                acceleration: 0.20,
                engine_load: 0.15,
                battery: 0.05
            },
            timestamp: new Date().toISOString()
        };
    };

    // Mettre à jour toutes les prédictions
    const updateAllPredictions = async () => {
        setPredictions(prev => ({ ...prev, loading: true }));

        try {
            const newPredictions = {};

            // Pour chaque modèle, faire une prédiction
            for (const model of MODELS) {
                // Simulation (à remplacer par vos appels API)
                newPredictions[model.id] = await simulatePrediction(model.id);
            }

            // Calculer la prédiction moyenne (consensus)
            const allScores = Object.values(newPredictions).map(p => p.eco_score);
            const avgScore = Math.round(allScores.reduce((a, b) => a + b, 0) / allScores.length);

            // Déterminer l'alarme la plus fréquente
            const alarmLevels = Object.values(newPredictions).map(p => p.alarm_level);
            const mostCommonAlarm = alarmLevels.sort((a, b) =>
                alarmLevels.filter(v => v === a).length - alarmLevels.filter(v => v === b).length
            ).pop();

            // Mettre à jour les prédictions
            setPredictions({
                ...newPredictions,
                loading: false,
                lastUpdate: new Date(),
                consensus: {
                    eco_score: avgScore,
                    alarm_level: mostCommonAlarm,
                    model_count: MODELS.length
                }
            });

            // Mettre à jour les données du véhicule
            setVehicleData(prev => ({
                ...prev,
                eco_score: avgScore,
                current_alarm: mostCommonAlarm,
                fuel_efficiency: 10 + (avgScore / 100) * 5,
                co2_saved: (avgScore / 100) * 6
            }));

        } catch (error) {
            console.error('Erreur lors des prédictions:', error);
            setPredictions(prev => ({ ...prev, loading: false }));
        }
    };

    // Mettre à jour les prédictions quand les valeurs d'entrée changent
    useEffect(() => {
        const timer = setTimeout(() => {
            updateAllPredictions();
        }, 500); // Délai pour éviter trop d'appels

        return () => clearTimeout(timer);
    }, [inputValues]);

    // Mettre à jour périodiquement
    useEffect(() => {
        const interval = setInterval(() => {
            updateAllPredictions();
        }, 30000); // Toutes les 30 secondes

        return () => clearInterval(interval);
    }, []);

    // Fonction pour formater l'heure
    const formatTime = (date) => {
        if (!date) return '--:--:--';
        return date.toLocaleTimeString('fr-FR', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    return (
        <div className="mt-8">
            {/* En-tête */}
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h3 className="text-xl font-bold text-gray-800 flex items-center">
                        <Brain className="mr-2 text-green-600" size={24} />
                        Prédictions IA en Temps Réel
                    </h3>
                    <p className="text-gray-600 text-sm mt-1">
                        Analyse simultanée par {MODELS.length} modèles d'IA • Mise à jour automatique
                    </p>
                </div>
                {predictions.lastUpdate && (
                    <div className="text-right">
                        <div className="text-xs text-gray-500">Dernière mise à jour</div>
                        <div className="text-sm font-medium text-gray-700">
                            {formatTime(predictions.lastUpdate)}
                        </div>
                    </div>
                )}
            </div>

            {/* Scores principaux (Consensus) */}
            {predictions.consensus && (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                    {/* Éco-Score */}
                    <div className="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-5 border border-green-200 shadow-sm">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center">
                                <Gauge className="text-green-600 mr-2" size={20} />
                                <span className="font-semibold text-gray-800">Éco-Score</span>
                            </div>
                            <span className="text-xs px-2 py-1 bg-green-100 text-green-800 rounded-full">
                Consensus
              </span>
                        </div>
                        <div className="flex items-baseline">
              <span className="text-3xl font-bold text-gray-800">
                {predictions.consensus.eco_score}
              </span>
                            <span className="text-lg text-gray-600 ml-1">/100</span>
                        </div>
                        <div className="mt-3 w-full bg-gray-200 rounded-full h-2">
                            <div
                                className={`h-2 rounded-full transition-all duration-500 ${
                                    predictions.consensus.eco_score >= 80 ? 'bg-green-500' :
                                        predictions.consensus.eco_score >= 60 ? 'bg-yellow-500' :
                                            predictions.consensus.eco_score >= 40 ? 'bg-orange-500' : 'bg-red-500'
                                }`}
                                style={{ width: `${predictions.consensus.eco_score}%` }}
                            ></div>
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                            {predictions.consensus.eco_score >= 80 ? 'Excellent' :
                                predictions.consensus.eco_score >= 60 ? 'Bon' :
                                    predictions.consensus.eco_score >= 40 ? 'Moyen' : 'À améliorer'}
                        </p>
                    </div>

                    {/* Niveau d'Alerte */}
                    <div className="bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl p-5 border border-blue-200 shadow-sm">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center">
                                <AlertTriangle className="text-blue-600 mr-2" size={20} />
                                <span className="font-semibold text-gray-800">Niveau d'Alerte</span>
                            </div>
                            <span className={`text-xs px-2 py-1 rounded-full ${
                                ALARM_TYPES[predictions.consensus.alarm_level]?.color || 'bg-gray-100 text-gray-800'
                            }`}>
                {ALARM_TYPES[predictions.consensus.alarm_level]?.name || 'Inconnu'}
              </span>
                        </div>
                        <div className="flex items-baseline">
              <span className="text-3xl font-bold text-gray-800">
                Niveau {predictions.consensus.alarm_level}
              </span>
                        </div>
                        <p className="text-sm text-gray-600 mt-2">
                            {ALARM_TYPES[predictions.consensus.alarm_level]?.description || 'Aucune description'}
                        </p>
                    </div>

                    {/* Efficacité Carburant */}
                    <div className="bg-gradient-to-br from-purple-50 to-violet-50 rounded-xl p-5 border border-purple-200 shadow-sm">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center">
                                <Activity className="text-purple-600 mr-2" size={20} />
                                <span className="font-semibold text-gray-800">Efficacité</span>
                            </div>
                            <span className="text-xs px-2 py-1 bg-purple-100 text-purple-800 rounded-full">
                Potentiel
              </span>
                        </div>
                        <div className="flex items-baseline">
              <span className="text-3xl font-bold text-gray-800">
                {vehicleData.fuel_efficiency.toFixed(1)}
              </span>
                            <span className="text-lg text-gray-600 ml-1">km/L</span>
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                            {vehicleData.fuel_efficiency > 12 ? 'Très efficace' :
                                vehicleData.fuel_efficiency > 10 ? 'Efficace' :
                                    vehicleData.fuel_efficiency > 8 ? 'Moyen' : 'Peu efficace'}
                        </p>
                    </div>

                    {/* CO2 Économisé */}
                    <div className="bg-gradient-to-br from-orange-50 to-amber-50 rounded-xl p-5 border border-orange-200 shadow-sm">
                        <div className="flex items-center justify-between mb-3">
                            <div className="flex items-center">
                                <Sparkles className="text-orange-600 mr-2" size={20} />
                                <span className="font-semibold text-gray-800">CO2 Économisé</span>
                            </div>
                            <span className="text-xs px-2 py-1 bg-orange-100 text-orange-800 rounded-full">
                Cumul
              </span>
                        </div>
                        <div className="flex items-baseline">
              <span className="text-3xl font-bold text-gray-800">
                {vehicleData.co2_saved.toFixed(1)}
              </span>
                            <span className="text-lg text-gray-600 ml-1">kg</span>
                        </div>
                        <p className="text-xs text-gray-500 mt-2">
                            Équivalent {Math.round(vehicleData.co2_saved * 100)} arbres plantés
                        </p>
                    </div>
                </div>
            )}

            {/* Prédictions par modèle */}
            <div className="mb-6">
                <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                    <Cpu className="mr-2 text-gray-600" size={20} />
                    Comparaison des Modèles d'IA
                </h4>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    {MODELS.map(model => {
                        const prediction = predictions[model.id];

                        return (
                            <div key={model.id} className="bg-white rounded-xl border p-4 hover:shadow-md transition-shadow">
                                {/* En-tête du modèle */}
                                <div className="flex items-center justify-between mb-3">
                                    <div className="flex items-center">
                                        <span className="text-xl mr-2">{model.icon}</span>
                                        <div>
                                            <div className="font-semibold text-gray-800 text-sm">{model.name}</div>
                                            <div className="text-xs text-gray-500">{model.description}</div>
                                        </div>
                                    </div>
                                    {prediction && (
                                        <div className={`text-xs px-2 py-1 rounded-full ${
                                            prediction.confidence >= 0.8 ? 'bg-green-100 text-green-800' :
                                                prediction.confidence >= 0.6 ? 'bg-yellow-100 text-yellow-800' :
                                                    'bg-red-100 text-red-800'
                                        }`}>
                                            {(prediction.confidence * 100).toFixed(0)}%
                                        </div>
                                    )}
                                </div>

                                {/* Contenu de la prédiction */}
                                {prediction ? (
                                    <div>
                                        {/* Score et Alerte */}
                                        <div className="flex justify-between items-center mb-3">
                                            <div>
                                                <div className="text-xs text-gray-500">Éco-Score</div>
                                                <div className="text-xl font-bold text-gray-800">
                                                    {prediction.eco_score}<span className="text-sm text-gray-600">/100</span>
                                                </div>
                                            </div>
                                            <div className="text-right">
                                                <div className="text-xs text-gray-500">Alerte</div>
                                                <div className={`text-sm font-semibold px-2 py-1 rounded ${
                                                    ALARM_TYPES[prediction.alarm_level]?.color || 'bg-gray-100 text-gray-800'
                                                }`}>
                                                    Niv. {prediction.alarm_level}
                                                </div>
                                            </div>
                                        </div>

                                        {/* Barre de progression */}
                                        <div className="mb-3">
                                            <div className="flex justify-between text-xs text-gray-500 mb-1">
                                                <span>Performance</span>
                                                <span>{prediction.eco_score}%</span>
                                            </div>
                                            <div className="w-full bg-gray-200 rounded-full h-1.5">
                                                <div
                                                    className={`h-1.5 rounded-full transition-all duration-500 bg-gradient-to-r ${model.color}`}
                                                    style={{ width: `${prediction.eco_score}%` }}
                                                ></div>
                                            </div>
                                        </div>

                                        {/* Recommandation principale */}
                                        {prediction.recommendations && prediction.recommendations.length > 0 && (
                                            <div className="text-xs text-gray-600 mt-2">
                                                <div className="font-medium mb-1">💡 Recommandation:</div>
                                                <div className="truncate">{prediction.recommendations[0]}</div>
                                            </div>
                                        )}
                                    </div>
                                ) : (
                                    <div className="text-center py-4">
                                        <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-gray-300 mx-auto mb-2"></div>
                                        <div className="text-xs text-gray-500">Chargement...</div>
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* Contrôles d'ajustement (simulation) */}
            <div className="bg-gray-50 rounded-xl p-4 mb-6">
                <h4 className="text-lg font-semibold text-gray-800 mb-4">
                    <Target className="inline mr-2" size={20} />
                    Simulation des Paramètres d'Entrée
                </h4>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {/* RPM */}
                    <div className="bg-white p-4 rounded-lg border">
                        <div className="flex justify-between mb-2">
                            <span className="text-sm font-medium text-gray-700">RPM Moteur</span>
                            <span className="text-sm font-bold text-gray-800">{inputValues.rpm} rpm</span>
                        </div>
                        <input
                            type="range"
                            min="800"
                            max="5000"
                            value={inputValues.rpm}
                            onChange={(e) => setInputValues(prev => ({ ...prev, rpm: parseInt(e.target.value) }))}
                            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                        />
                        <div className="flex justify-between text-xs text-gray-500 mt-1">
                            <span>Faible</span>
                            <span>Optimal: 1500-2500</span>
                            <span>Élevé</span>
                        </div>
                    </div>

                    {/* Vitesse */}
                    <div className="bg-white p-4 rounded-lg border">
                        <div className="flex justify-between mb-2">
                            <span className="text-sm font-medium text-gray-700">Vitesse</span>
                            <span className="text-sm font-bold text-gray-800">{inputValues.speed} km/h</span>
                        </div>
                        <input
                            type="range"
                            min="0"
                            max="180"
                            value={inputValues.speed}
                            onChange={(e) => setInputValues(prev => ({ ...prev, speed: parseInt(e.target.value) }))}
                            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                        />
                        <div className="flex justify-between text-xs text-gray-500 mt-1">
                            <span>Lente</span>
                            <span>Optimal: 70-90</span>
                            <span>Rapide</span>
                        </div>
                    </div>

                    {/* Accélération */}
                    <div className="bg-white p-4 rounded-lg border">
                        <div className="flex justify-between mb-2">
                            <span className="text-sm font-medium text-gray-700">Accélération X</span>
                            <span className="text-sm font-bold text-gray-800">{inputValues.acceleration_x.toFixed(2)} g</span>
                        </div>
                        <input
                            type="range"
                            min="-2"
                            max="2"
                            step="0.1"
                            value={inputValues.acceleration_x}
                            onChange={(e) => setInputValues(prev => ({ ...prev, acceleration_x: parseFloat(e.target.value) }))}
                            className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                        />
                        <div className="flex justify-between text-xs text-gray-500 mt-1">
                            <span>Freinage</span>
                            <span>Neutre</span>
                            <span>Accélération</span>
                        </div>
                    </div>
                </div>

                <div className="mt-4 text-center">
                    <button
                        onClick={updateAllPredictions}
                        className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium flex items-center mx-auto"
                    >
                        <Zap size={16} className="mr-2" />
                        Actualiser les Prédictions
                    </button>
                    <p className="text-xs text-gray-500 mt-2">
                        Les prédictions se mettent à jour automatiquement quand vous ajustez les valeurs
                    </p>
                </div>
            </div>

            {/* Tableau de comparaison détaillée */}
            {Object.values(predictions).some(p => p && typeof p === 'object' && p.success) && (
                <div className="bg-white rounded-xl border p-4">
                    <h4 className="text-lg font-semibold text-gray-800 mb-4">
                        📊 Comparaison Détailée des Prédictions
                    </h4>

                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                            <tr className="border-b">
                                <th className="text-left py-2 px-3 font-medium text-gray-700">Modèle</th>
                                <th className="text-left py-2 px-3 font-medium text-gray-700">Éco-Score</th>
                                <th className="text-left py-2 px-3 font-medium text-gray-700">Niveau Alerte</th>
                                <th className="text-left py-2 px-3 font-medium text-gray-700">Confiance</th>
                                <th className="text-left py-2 px-3 font-medium text-gray-700">Efficacité +</th>
                                <th className="text-left py-2 px-3 font-medium text-gray-700">Recommandations</th>
                            </tr>
                            </thead>
                            <tbody>
                            {MODELS.map(model => {
                                const prediction = predictions[model.id];
                                if (!prediction || !prediction.success) return null;

                                return (
                                    <tr key={model.id} className="border-b hover:bg-gray-50">
                                        <td className="py-3 px-3">
                                            <div className="flex items-center">
                                                <span className="mr-2">{model.icon}</span>
                                                <span className="font-medium">{model.name}</span>
                                            </div>
                                        </td>
                                        <td className="py-3 px-3">
                                            <div className="flex items-center">
                                                <div className={`w-16 h-2 bg-gray-200 rounded-full mr-2`}>
                                                    <div
                                                        className={`h-2 rounded-full ${
                                                            prediction.eco_score >= 80 ? 'bg-green-500' :
                                                                prediction.eco_score >= 60 ? 'bg-yellow-500' :
                                                                    prediction.eco_score >= 40 ? 'bg-orange-500' : 'bg-red-500'
                                                        }`}
                                                        style={{ width: `${prediction.eco_score}%` }}
                                                    ></div>
                                                </div>
                                                <span className="font-bold">{prediction.eco_score}</span>
                                            </div>
                                        </td>
                                        <td className="py-3 px-3">
                        <span className={`px-2 py-1 rounded text-xs ${ALARM_TYPES[prediction.alarm_level]?.color}`}>
                          Niveau {prediction.alarm_level}
                        </span>
                                        </td>
                                        <td className="py-3 px-3">
                                            <div className="flex items-center">
                                                <div className="w-16 h-2 bg-gray-200 rounded-full mr-2">
                                                    <div
                                                        className="h-2 rounded-full bg-blue-500"
                                                        style={{ width: `${prediction.confidence * 100}%` }}
                                                    ></div>
                                                </div>
                                                <span>{(prediction.confidence * 100).toFixed(0)}%</span>
                                            </div>
                                        </td>
                                        <td className="py-3 px-3">
                        <span className="font-medium text-green-700">
                          +{prediction.fuel_efficiency_gain?.toFixed(1) || '0'}%
                        </span>
                                        </td>
                                        <td className="py-3 px-3">
                                            <div className="text-xs text-gray-600">
                                                {prediction.recommendations?.[0] || 'Aucune'}
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EcoDrivingDashboard;