const CACHE_NAME = 'soil-pwa-v2'; // ← Version changée pour forcer la mise à jour
const ASSETS = [
    './',
    './index.html',
    './pwa.webmanifest',

    // Pages
    './pages/login.html',
    './pages/admin.html',
    './pages/user.html',
    './pages/signup.html',

    // Scripts
    './scripts/main.js',
    './scripts/login.js',
    './scripts/admin.js',
    './scripts/user.js',
    './scripts/signup.js',

    // Styles
    './styles/main.css',

    // CDN Libraries (optionnel, mais recommandé)
    'https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.2/css/bootstrap.min.css',
    'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css'
];

// Install : mettre en cache les assets essentiels
self.addEventListener('install', event => {
    console.log('[SW] Installing Service Worker v2...');
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('[SW] Caching app shell');
                return cache.addAll(ASSETS);
            })
            .catch(err => console.error('[SW] Cache failed:', err))
    );
    self.skipWaiting();
});

// Activate : nettoyage des anciens caches
self.addEventListener('activate', event => {
    console.log('[SW] Activating new Service Worker...');
    event.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(
                keys.filter(k => k !== CACHE_NAME)
                    .map(k => {
                        console.log('[SW] Deleting old cache:', k);
                        return caches.delete(k);
                    })
            );
        })
    );
    self.clients.claim();
});

// Fetch : stratégie cache-first pour assets, network-first pour API
self.addEventListener('fetch', event => {
    const req = event.request;
    const url = new URL(req.url);

    // Ignorer les requêtes non-HTTP (chrome-extension://, etc.)
    if (!req.url.startsWith('http')) {
        return;
    }

    // Network-first pour les API et WebSocket
    if (url.pathname.includes('/api/') || url.pathname.includes('/ws/')) {
        event.respondWith(
            fetch(req)
                .catch(() => caches.match('./pages/login.html'))
        );
        return;
    }

    // Cache-first pour les assets statiques
    event.respondWith(
        caches.match(req)
            .then(cached => {
                if (cached) {
                    console.log('[SW] Cache hit:', req.url);
                    return cached;
                }

                console.log('[SW] Fetching:', req.url);
                return fetch(req).then(response => {
                    // Mettre en cache les réponses GET valides
                    if (req.method === 'GET' && response && response.status === 200) {
                        const clone = response.clone();
                        caches.open(CACHE_NAME).then(cache => {
                            cache.put(req, clone);
                        });
                    }
                    return response;
                });
            })
            .catch(err => {
                console.error('[SW] Fetch failed:', err);
                // Fallback vers index.html pour navigation
                if (req.mode === 'navigate') {
                    return caches.match('./index.html');
                }
            })
    );
});