// =====================================================
// SERVICE WORKER - MODE DÉVELOPPEMENT
// =====================================================

const CACHE_NAME = 'agromonitor-dev-v1';

console.log('[SW] 🔧 Service Worker en MODE DÉVELOPPEMENT');
console.log('[SW] ⚠️ Pas de cache - Toutes les requêtes passent par le réseau');

// =====================================================
// INSTALL - Activation immédiate sans cache
// =====================================================
self.addEventListener('install', event => {
    console.log('[SW] ✅ Installing Service Worker (No Cache)...');
    self.skipWaiting();
});

// =====================================================
// ACTIVATE - Nettoyage de tous les caches existants
// =====================================================
self.addEventListener('activate', event => {
    console.log('[SW] 🧹 Activating and clearing ALL caches...');

    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    console.log('[SW] 🗑️ Deleting cache:', cacheName);
                    return caches.delete(cacheName);
                })
            );
        })
    );

    self.clients.claim();
});

// =====================================================
// FETCH - TOUJOURS LE RÉSEAU (pas de cache)
// =====================================================
self.addEventListener('fetch', event => {
    const { request } = event;

    // Ignorer les requêtes non-HTTP
    if (!request.url.startsWith('http')) {
        return;
    }

    // ⚠️ TOUTES LES REQUÊTES → RÉSEAU DIRECT (pas de cache)
    event.respondWith(
        fetch(request)
            .then(response => {
                console.log('[SW] 🌐 Network:', request.url);
                return response;
            })
            .catch(error => {
                console.error('[SW] ❌ Fetch failed:', request.url, error);

                // Fallback basique pour la navigation
                if (request.mode === 'navigate') {
                    return new Response(
                        '<h1>Offline</h1><p>Impossible de charger la page sans connexion.</p>',
                        { headers: { 'Content-Type': 'text/html' } }
                    );
                }

                return new Response('Network error', { status: 503 });
            })
    );
});

console.log('[SW] 🚀 Service Worker ready (NO CACHE MODE)');