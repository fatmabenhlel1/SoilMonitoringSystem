// ===================== GEOLOCATION SERVICE =====================
// Handle user location tracking and navigation

const GeolocationService = {
    // Store current position
    currentPosition: null,
    watchId: null,
    isTracking: false,

    // Callbacks for position updates
    onPositionUpdate: null,
    onError: null,

    /**
     * Check if geolocation is supported
     */
    isSupported() {
        return 'geolocation' in navigator;
    },

    /**
     * Get user's current position (one-time)
     */
    async getCurrentPosition() {
        return new Promise((resolve, reject) => {
            if (!this.isSupported()) {
                reject(new Error('Geolocation is not supported by your browser'));
                return;
            }

            navigator.geolocation.getCurrentPosition(
                (position) => {
                    this.currentPosition = {
                        latitude: position.coords.latitude,
                        longitude: position.coords.longitude,
                        accuracy: position.coords.accuracy,
                        timestamp: position.timestamp
                    };
                    resolve(this.currentPosition);
                },
                (error) => {
                    reject(this.handleGeolocationError(error));
                },
                {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 0
                }
            );
        });
    },

    /**
     * Start watching user's position (continuous tracking)
     */
    startWatchingPosition(onUpdate, onError) {
        if (!this.isSupported()) {
            console.error('Geolocation not supported');
            return false;
        }

        if (this.isTracking) {
            console.warn('Already tracking position');
            return false;
        }

        this.onPositionUpdate = onUpdate;
        this.onError = onError;

        this.watchId = navigator.geolocation.watchPosition(
            (position) => {
                this.currentPosition = {
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude,
                    accuracy: position.coords.accuracy,
                    altitude: position.coords.altitude,
                    heading: position.coords.heading,
                    speed: position.coords.speed,
                    timestamp: position.timestamp
                };

                console.log('Position updated:', this.currentPosition);

                if (this.onPositionUpdate) {
                    this.onPositionUpdate(this.currentPosition);
                }
            },
            (error) => {
                console.error('Geolocation error:', error);
                const errorMessage = this.handleGeolocationError(error);

                if (this.onError) {
                    this.onError(errorMessage);
                }
            },
            {
                enableHighAccuracy: true,
                timeout: 5000,
                maximumAge: 0
            }
        );

        this.isTracking = true;
        console.log('Started watching position. Watch ID:', this.watchId);
        return true;
    },

    /**
     * Stop watching user's position
     */
    stopWatchingPosition() {
        if (this.watchId !== null) {
            navigator.geolocation.clearWatch(this.watchId);
            this.watchId = null;
            this.isTracking = false;
            console.log('Stopped watching position');
        }
    },

    /**
     * Calculate distance between two coordinates (Haversine formula)
     * Returns distance in meters
     */
    calculateDistance(lat1, lon1, lat2, lon2) {
        const R = 6371e3; // Earth's radius in meters
        const φ1 = lat1 * Math.PI / 180;
        const φ2 = lat2 * Math.PI / 180;
        const Δφ = (lat2 - lat1) * Math.PI / 180;
        const Δλ = (lon2 - lon1) * Math.PI / 180;

        const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in meters
    },

    /**
     * Calculate bearing (direction) from current position to target
     * Returns angle in degrees (0-360)
     */
    calculateBearing(lat1, lon1, lat2, lon2) {
        const φ1 = lat1 * Math.PI / 180;
        const φ2 = lat2 * Math.PI / 180;
        const Δλ = (lon2 - lon1) * Math.PI / 180;

        const y = Math.sin(Δλ) * Math.cos(φ2);
        const x = Math.cos(φ1) * Math.sin(φ2) -
            Math.sin(φ1) * Math.cos(φ2) * Math.cos(Δλ);
        const θ = Math.atan2(y, x);

        return ((θ * 180 / Math.PI) + 360) % 360; // Normalize to 0-360
    },

    /**
     * Format distance for display
     */
    formatDistance(meters) {
        if (meters < 1000) {
            return `${Math.round(meters)} m`;
        } else {
            return `${(meters / 1000).toFixed(2)} km`;
        }
    },

    /**
     * Get direction label based on bearing
     */
    getDirectionLabel(bearing) {
        const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
        const index = Math.round(bearing / 45) % 8;
        return directions[index];
    },

    /**
     * Handle geolocation errors
     */
    handleGeolocationError(error) {
        switch(error.code) {
            case error.PERMISSION_DENIED:
                return "Location permission denied. Please enable location access in your browser settings.";
            case error.POSITION_UNAVAILABLE:
                return "Location information unavailable. Please check your device settings.";
            case error.TIMEOUT:
                return "Location request timed out. Please try again.";
            default:
                return "An unknown error occurred while getting your location.";
        }
    },

    /**
     * Request location permission
     */
    async requestPermission() {
        try {
            const position = await this.getCurrentPosition();
            console.log('Location permission granted:', position);
            return true;
        } catch (error) {
            console.error('Location permission denied:', error);
            return false;
        }
    }
};

// Export for use in other files
window.GeolocationService = GeolocationService;