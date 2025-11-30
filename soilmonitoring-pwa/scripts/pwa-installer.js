// =====================================================
// AGROMONITOR - PWA INSTALLER
// =====================================================

let deferredPrompt;
let installButton;

// Register Service Worker
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js')
            .then(reg => console.log('✅ SW registered:', reg.scope))
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

    // Create install button
    installButton = document.createElement('button');
    installButton.id = 'pwa-install-btn';
    installButton.className = 'pwa-install-button';
    installButton.innerHTML = `
        <img src="./images/icons/icon-192x192.png" alt="AgroMonitor" class="install-icon">
        <span>Install App</span>
    `;
    installButton.onclick = installPWA;

    document.body.appendChild(installButton);
}

// Install PWA
async function installPWA() {
    if (!deferredPrompt) return;

    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;

    console.log(`Install: ${outcome}`);

    if (outcome === 'accepted') {
        hideInstallButton();
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

// Hide if already installed
if (window.matchMedia('(display-mode: standalone)').matches) {
    console.log('✅ Already installed');
}

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