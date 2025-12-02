// =====================================================
// AGROMONITOR - PWA INSTALLER
// =====================================================

let deferredPrompt;
let installButton;

// Register Service Worker
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        // ✅ Chemin correct depuis pages/
        const swPath = window.location.pathname.includes('/pages/')
            ? '../sw.js'
            : './sw.js';

        navigator.serviceWorker.register(swPath)
            .then(reg => {
                console.log('✅ SW registered:', reg.scope);

                // Check for updates
                reg.addEventListener('updatefound', () => {
                    const newWorker = reg.installing;
                    newWorker.addEventListener('statechange', () => {
                        if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                            console.log('🔄 New version available!');
                            if (confirm('New version available! Reload to update?')) {
                                window.location.reload();
                            }
                        }
                    });
                });
            })
            .catch(err => console.error('❌ SW registration failed:', err));
    });
}

// Capture install prompt
window.addEventListener('beforeinstallprompt', (e) => {
    console.log('💾 Install prompt available');
    e.preventDefault();
    deferredPrompt = e;
    showInstallButton();
});

// Show Install Button with App Icon
function showInstallButton() {
    // Check if button already exists
    if (document.getElementById('pwa-install-btn')) return;

    // Check if already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
        console.log('✅ Already installed');
        return;
    }

    // ✅ Chemin correct pour l'icône
    const iconPath = window.location.pathname.includes('/pages/')
        ? '../images/icons/icon-192x192.png'
        : './images/icons/icon-192x192.png';

    // Create install button
    installButton = document.createElement('button');
    installButton.id = 'pwa-install-btn';
    installButton.className = 'pwa-install-button';
    installButton.innerHTML = `
        <img src="${iconPath}" alt="AgroMonitor" class="install-icon">
        <span>Install App</span>
    `;
    installButton.onclick = installPWA;

    document.body.appendChild(installButton);
}

// Install PWA
async function installPWA() {
    if (!deferredPrompt) {
        console.warn('⚠️ Install prompt not available');
        return;
    }

    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;

    console.log(`Install: ${outcome}`);

    if (outcome === 'accepted') {
        console.log('✅ User accepted installation');
        hideInstallButton();
    } else {
        console.log('❌ User dismissed installation');
    }

    deferredPrompt = null;
}

// Hide Install Button
function hideInstallButton() {
    if (installButton) {
        installButton.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => installButton.remove(), 300);
    }
}

// App installed event
window.addEventListener('appinstalled', () => {
    console.log('🎉 AgroMonitor installed!');
    hideInstallButton();
});

// =====================================================
// STYLES FOR INSTALL BUTTON
// =====================================================

const style = document.createElement('style');
style.textContent = `
    .pwa-install-button {
        position: fixed;
        bottom: 24px;
        right: 24px;
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 14px 24px;
        background: linear-gradient(135deg, #2d6a4f 0%, #40916c 100%);
        color: white;
        border: none;
        border-radius: 16px;
        font-weight: 600;
        font-size: 15px;
        box-shadow: 0 8px 24px rgba(45, 106, 79, 0.35);
        cursor: pointer;
        z-index: 10000;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        animation: slideIn 0.5s ease;
    }
    
    .install-icon {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        background: white;
        padding: 2px;
    }
    
    .pwa-install-button:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 32px rgba(45, 106, 79, 0.45);
        background: linear-gradient(135deg, #40916c 0%, #52b788 100%);
    }
    
    .pwa-install-button:active {
        transform: translateY(-2px);
    }
    
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
    
    /* Mobile styles */
    @media (max-width: 768px) {
        .pwa-install-button {
            bottom: 16px;
            right: 16px;
            left: 16px;
            justify-content: center;
            padding: 16px 20px;
            border-radius: 12px;
        }
    }
`;
document.head.appendChild(style);

console.log('✅ PWA Installer ready');