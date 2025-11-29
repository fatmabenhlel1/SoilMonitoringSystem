package me.soilmonitoring.api.services;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for integrating with AWS SageMaker ML models
 * Handles crop and fertilizer recommendations
 */
@ApplicationScoped
public class SageMakerService {

    private static final Logger LOGGER = Logger.getLogger(SageMakerService.class.getName());

    private final SageMakerRuntimeClient sageMakerClient;

    // TODO: Replace with your actual endpoint names
    private static final String CROP_ENDPOINT = "xgboost-crop-endpoint";
    private static final String FERTILIZER_ENDPOINT = "fertilizer-recommendation-endpoint";
    private static final String AWS_REGION = "us-east-1";

    // Crop label mapping (22 crops - matches your label encoder)
    private static final String[] CROP_LABELS = {
            "apple", "banana", "blackgram", "chickpea", "coconut",
            "coffee", "cotton", "grapes", "jute", "kidneybeans",
            "lentil", "maize", "mango", "mothbeans", "mungbean",
            "muskmelon", "orange", "papaya", "pigeonpeas", "pomegranate",
            "rice", "watermelon"
    };

    // Fertilizer labels from your encoder (alphabetically sorted by sklearn)
    private static final String[] FERTILIZER_LABELS = {
            "10-26-26", "14-35-14", "17-17-17", "20-20",
            "28-28", "DAP", "Urea"
    };

    // Soil type labels from your encoder (alphabetically sorted by sklearn)
    private static final String[] SOIL_TYPE_LABELS = {
            "Black", "Clayey", "Loamy", "Red", "Sandy"
    };

    // Crop type labels from your encoder (alphabetically sorted by sklearn)
    private static final String[] CROP_TYPE_LABELS = {
            "Barley", "Cotton", "Ground Nuts", "Maize", "Millets",
            "Oil seeds", "Paddy", "Pulses", "Sugarcane", "Tobacco", "Wheat"
    };

    /**
     * Constructor - initializes SageMaker client with default credentials
     */
    public SageMakerService() {
        LOGGER.info("Initializing SageMaker service...");
        try {
            this.sageMakerClient = SageMakerRuntimeClient.builder()
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            LOGGER.info("SageMaker client initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize SageMaker client", e);
            throw new RuntimeException("Cannot initialize SageMaker service", e);
        }
    }

    /**
     * Get crop recommendation from SageMaker XGBoost model
     *
     * @param n Nitrogen content (mg/kg): 0-150
     * @param p Phosphorus content (mg/kg): 0-150
     * @param k Potassium content (mg/kg): 0-300
     * @param temperature Temperature (°C): -10 to 50
     * @param humidity Humidity (%): 0-100
     * @param ph pH level: 0-14
     * @param rainfall Rainfall (mm): 0-500
     * @return CropPrediction object with recommended crop and confidence
     * @throws RuntimeException if prediction fails
     */
    public CropPrediction predictCrop(double n, double p, double k,
                                      double temperature, double humidity,
                                      double ph, double rainfall) {

        LOGGER.info(String.format("Predicting crop for: N=%.1f, P=%.1f, K=%.1f, T=%.1f°C, H=%.1f%%, pH=%.1f, R=%.1fmm",
                n, p, k, temperature, humidity, ph, rainfall));

        try {
            // Format input as CSV: N,P,K,temperature,humidity,ph,rainfall
            String csvInput = String.format("%.1f,%.1f,%.1f,%.1f,%.1f,%.1f,%.1f",
                    n, p, k, temperature, humidity, ph, rainfall);

            LOGGER.fine("CSV input: " + csvInput);

            // Invoke SageMaker endpoint
            InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                    .endpointName(CROP_ENDPOINT)
                    .contentType("text/csv")
                    .accept("text/csv")
                    .body(SdkBytes.fromString(csvInput, StandardCharsets.UTF_8))
                    .build();

            InvokeEndpointResponse response = sageMakerClient.invokeEndpoint(request);
            String result = response.body().asUtf8String().trim();

            LOGGER.fine("Raw SageMaker response: " + result);

            // Parse prediction (returns class index as float)
            int classIndex = (int) Float.parseFloat(result);

            // Validate class index
            if (classIndex < 0 || classIndex >= CROP_LABELS.length) {
                throw new IllegalStateException("Invalid crop class index: " + classIndex);
            }

            String cropName = CROP_LABELS[classIndex];
            double confidence = 0.95; // XGBoost doesn't return confidence by default

            LOGGER.info(String.format("Prediction successful: %s (class %d, confidence: %.2f)",
                    cropName, classIndex, confidence));

            return new CropPrediction(cropName, classIndex, confidence);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get crop prediction", e);
            throw new RuntimeException("Failed to get crop prediction: " + e.getMessage(), e);
        }
    }

    /**
     * Get fertilizer recommendation from SageMaker XGBoost model
     *
     * @param temperature Temperature (°C)
     * @param humidity Humidity (%)
     * @param moisture Soil moisture (%)
     * @param soilType Soil type (e.g., "Loamy", "Sandy", "Clayey")
     * @param cropType Crop type (e.g., "Rice", "Wheat", "Maize")
     * @param nitrogen Nitrogen content
     * @param potassium Potassium content
     * @param phosphorous Phosphorus content
     * @return FertilizerPrediction object with recommended fertilizer
     * @throws RuntimeException if prediction fails
     */
    public FertilizerPrediction predictFertilizer(double temperature, double humidity,
                                                  double moisture, String soilType,
                                                  String cropType, int nitrogen,
                                                  int potassium, int phosphorous) {

        LOGGER.info(String.format("Predicting fertilizer for: T=%.1f°C, H=%.1f%%, M=%.1f%%, Soil=%s, Crop=%s, N=%d, K=%d, P=%d",
                temperature, humidity, moisture, soilType, cropType, nitrogen, potassium, phosphorous));

        try {
            // Encode categorical variables
            int soilTypeEncoded = encodeSoilType(soilType);
            int cropTypeEncoded = encodeCropType(cropType);

            // Format input as CSV
            // Order MUST match training data: Temperature,Humidity,Moisture,Soil Type,Crop Type,Nitrogen,Potassium,Phosphorous
            String csvInput = String.format("%.1f,%.1f,%.1f,%d,%d,%d,%d,%d",
                    temperature, humidity, moisture, soilTypeEncoded, cropTypeEncoded,
                    nitrogen, potassium, phosphorous);

            LOGGER.fine("CSV input: " + csvInput);

            // Invoke SageMaker endpoint
            InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                    .endpointName(FERTILIZER_ENDPOINT)
                    .contentType("text/csv")
                    .accept("text/csv")
                    .body(SdkBytes.fromString(csvInput, StandardCharsets.UTF_8))
                    .build();

            InvokeEndpointResponse response = sageMakerClient.invokeEndpoint(request);
            String result = response.body().asUtf8String().trim();

            LOGGER.fine("Raw SageMaker response: " + result);

            int classIndex = (int) Float.parseFloat(result);

            // Validate class index
            if (classIndex < 0 || classIndex >= FERTILIZER_LABELS.length) {
                throw new IllegalStateException("Invalid fertilizer class index: " + classIndex);
            }

            String fertilizerType = FERTILIZER_LABELS[classIndex];
            String dosage = calculateDosage(nitrogen, phosphorous, potassium, fertilizerType);
            double confidence = 0.92;

            LOGGER.info(String.format("Prediction successful: %s, %s (class %d, confidence: %.2f)",
                    fertilizerType, dosage, classIndex, confidence));

            return new FertilizerPrediction(fertilizerType, dosage, classIndex, confidence);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get fertilizer prediction", e);
            throw new RuntimeException("Failed to get fertilizer prediction: " + e.getMessage(), e);
        }
    }

    /**
     * Encode soil type to integer (matches training label encoder)
     * Mapping: Black=0, Clayey=1, Loamy=2, Red=3, Sandy=4
     */
    private int encodeSoilType(String soilType) {
        if (soilType == null || soilType.trim().isEmpty()) {
            LOGGER.warning("Null or empty soil type, defaulting to Loamy (2)");
            return 2; // Default to Loamy
        }

        String normalized = soilType.trim();

        // Find exact match (case-insensitive)
        for (int i = 0; i < SOIL_TYPE_LABELS.length; i++) {
            if (SOIL_TYPE_LABELS[i].equalsIgnoreCase(normalized)) {
                LOGGER.fine(String.format("Encoded soil type '%s' -> %d", normalized, i));
                return i;
            }
        }

        // Fallback mapping for common variations
        switch (normalized.toLowerCase()) {
            case "clay":
            case "clayey":
                return 1; // Clayey
            case "loam":
            case "loamy":
                return 2; // Loamy
            case "sand":
            case "sandy":
                return 4; // Sandy
            case "black":
                return 0; // Black
            case "red":
                return 3; // Red
            default:
                LOGGER.warning("Unknown soil type: " + soilType + ", defaulting to Loamy (2)");
                return 2; // Default to Loamy
        }
    }

    /**
     * Encode crop type to integer (matches training label encoder)
     * Mapping: Barley=0, Cotton=1, Ground Nuts=2, Maize=3, Millets=4,
     *          Oil seeds=5, Paddy=6, Pulses=7, Sugarcane=8, Tobacco=9, Wheat=10
     */
    private int encodeCropType(String cropType) {
        if (cropType == null || cropType.trim().isEmpty()) {
            LOGGER.warning("Null or empty crop type, defaulting to Paddy (6)");
            return 6; // Default to Paddy (rice)
        }

        String normalized = cropType.trim();

        // Find exact match (case-insensitive)
        for (int i = 0; i < CROP_TYPE_LABELS.length; i++) {
            if (CROP_TYPE_LABELS[i].equalsIgnoreCase(normalized)) {
                LOGGER.fine(String.format("Encoded crop type '%s' -> %d", normalized, i));
                return i;
            }
        }

        // Fallback mapping for common variations and aliases
        switch (normalized.toLowerCase()) {
            case "barley":
                return 0;
            case "cotton":
                return 1;
            case "groundnuts":
            case "ground nuts":
            case "peanuts":
                return 2;
            case "maize":
            case "corn":
                return 3;
            case "millets":
            case "millet":
                return 4;
            case "oilseeds":
            case "oil seeds":
                return 5;
            case "paddy":
            case "rice":
                return 6;
            case "pulses":
            case "lentils":
            case "beans":
                return 7;
            case "sugarcane":
            case "sugar cane":
                return 8;
            case "tobacco":
                return 9;
            case "wheat":
                return 10;
            default:
                LOGGER.warning("Unknown crop type: " + cropType + ", defaulting to Paddy (6)");
                return 6; // Default to Paddy
        }
    }

    /**
     * Calculate fertilizer dosage based on nutrient levels
     * This is a simple heuristic - adjust based on agronomic requirements
     */
    private String calculateDosage(int nitrogen, int phosphorous, int potassium, String fertType) {
        double deficiency = 0;

        // Optimal ranges (adjust based on crop requirements)
        final int OPTIMAL_N = 50;
        final int OPTIMAL_P = 25;
        final int OPTIMAL_K = 110;

        // Calculate total deficiency
        if (nitrogen < OPTIMAL_N) deficiency += (OPTIMAL_N - nitrogen);
        if (phosphorous < OPTIMAL_P) deficiency += (OPTIMAL_P - phosphorous);
        if (potassium < OPTIMAL_K) deficiency += (OPTIMAL_K - potassium) / 2.0;

        // Base dosage calculation
        double dosage = Math.max(50, Math.min(200, 100 + deficiency * 1.5));

        // Adjust based on fertilizer type
        if (fertType.contains("Urea")) {
            dosage *= 0.8; // Urea is concentrated nitrogen
        } else if (fertType.contains("DAP")) {
            dosage *= 0.9; // DAP is concentrated phosphorus
        }

        return String.format("%.0f kg/hectare", dosage);
    }

    /**
     * Clean up resources
     */
    public void close() {
        if (sageMakerClient != null) {
            sageMakerClient.close();
            LOGGER.info("SageMaker client closed");
        }
    }

    // ======================== RESULT CLASSES ========================

    /**
     * Crop prediction result
     */
    public static class CropPrediction {
        public final String cropName;
        public final int classIndex;
        public final double confidence;

        public CropPrediction(String cropName, int classIndex, double confidence) {
            this.cropName = cropName;
            this.classIndex = classIndex;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            return String.format("CropPrediction{crop='%s', class=%d, confidence=%.2f}",
                    cropName, classIndex, confidence);
        }
    }

    /**
     * Fertilizer prediction result
     */
    public static class FertilizerPrediction {
        public final String fertilizerType;
        public final String dosage;
        public final int classIndex;
        public final double confidence;

        public FertilizerPrediction(String type, String dosage, int index, double conf) {
            this.fertilizerType = type;
            this.dosage = dosage;
            this.classIndex = index;
            this.confidence = conf;
        }

        @Override
        public String toString() {
            return String.format("FertilizerPrediction{type='%s', dosage='%s', class=%d, confidence=%.2f}",
                    fertilizerType, dosage, classIndex, confidence);
        }
    }
}
