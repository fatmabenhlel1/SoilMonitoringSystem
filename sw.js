const CACHE_NAME = 'soil-pwa-v1';
const ASSETS = [
    './index.html',
    './styles/main.css',
    './scripts/app.js',
    './scripts/ui.js',
    './scripts/api.js',
    './pwa.webmanifest'
];

// Install : mettre en cache les assets essentiels
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => cache.addAll(ASSETS))
    );
    self.skipWaiting();
});

// Activate : nettoyage des anciens caches si besoin
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys => Promise.all(
            keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k))
        ))
    );
    self.clients.claim();
});

// Fetch : stratégie cache-first pour assets, fallback réseau
self.addEventListener('fetch', event => {
    const req = event.request;
    // Pour les API on peut préférer network-first si on veut données fraîches
    if (req.url.includes('/api/') || req.url.includes('/ws/')) {
        event.respondWith(
            fetch(req).catch(() => caches.match('/offline.html'))
        );
        return;
    }

    event.respondWith(
        caches.match(req).then(cached => cached || fetch(req).then(res => {
            // Mettre en cache les réponses GET
            if (req.method === 'GET' && res && res.status === 200) {
                const clone = res.clone();
                caches.open(CACHE_NAME).then(c => c.put(req, clone));
            }
            return res;
        })).catch(() => caches.match('/index.html'))
    );
});
