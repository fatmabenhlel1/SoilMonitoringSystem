/**
 * Styles Module
 * Loads external CSS file for dynamic styles
 */

/**
 * Load dynamic styles from external CSS file
 */
export function loadDynamicStyles() {
// Check if already loaded
if (document.getElementById('admin-dynamic-styles')) {
console.log('⚠️ Dynamic styles already loaded');
    return;
}

// Create link element for CSS
const link = document.createElement('link');
link.id = 'admin-dynamic-styles';
link.rel = 'stylesheet';
link.href = '../styles/admin-dynamic.css';

// Add load event listener
link.onload = () => {
console.log('✅ Dynamic styles loaded from admin-dynamic.css');
};

// Add error handler
link.onerror = () => {
console.error('❌ Failed to load admin-dynamic.css');
};

document.head.appendChild(link);
}