package me.soilmonitoring.api.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import me.soilmonitoring.api.controllers.repositories.SensorReadingRepository;
import me.soilmonitoring.api.controllers.repositories.SensorRepository;
import me.soilmonitoring.api.entities.Sensor;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.entities.SensorReading;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
@Startup
public class MQTTService {

    private static final Logger logger = Logger.getLogger(MQTTService.class.getName());

    @Inject
    private SensorRepository sensorRepository;

    @Inject
    private SensorReadingRepository readingRepository;

    @Inject
    @ConfigProperty(name = "mqtt.broker.url")
    private String brokerUrl;

    @Inject
    @ConfigProperty(name = "mqtt.broker.port")
    private Integer brokerPort;

    @Inject
    @ConfigProperty(name = "mqtt.username")
    private String username;

    @Inject
    @ConfigProperty(name = "mqtt.password")
    private String password;

    @Inject
    @ConfigProperty(name = "mqtt.client.id")
    private String clientId;

    @Inject
    @ConfigProperty(name = "mqtt.use.tls", defaultValue = "true")
    private Boolean useTls;

    private Mqtt5AsyncClient mqttClient;

    @PostConstruct
    public void init() {
        try {
            logger.info("🚀 Initializing MQTT 5 connection to HiveMQ Cloud...");

            // Build MQTT 5 client
            var clientBuilder = MqttClient.builder()
                    .useMqttVersion5()
                    .identifier(clientId + "-" + UUID.randomUUID())
                    .serverHost(brokerUrl)
                    .serverPort(brokerPort);

            if (useTls) {
                clientBuilder.sslWithDefaultConfig();
            }

            mqttClient = clientBuilder.buildAsync();

            // Connect with credentials
            mqttClient.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(UTF_8.encode(password))
                    .applySimpleAuth()
                    .send()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            logger.severe("❌ Failed to connect to MQTT broker: " + throwable.getMessage());
                        } else {
                            logger.info("✅ Connected to HiveMQ Cloud successfully!");
                            subscribeToSensorData();
                        }
                    });

        } catch (Exception e) {
            logger.severe("Error initializing MQTT service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to the combined topic from Node-RED
     */
    private void subscribeToSensorData() {
        String topic = "sensor/data";

        mqttClient.subscribeWith()
                .topicFilter(topic)
                .callback(this::handleSensorData)
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        logger.severe("❌ Failed to subscribe to topic '" + topic + "': " + throwable.getMessage());
                    } else {
                        logger.info("📡 Subscribed to topic: " + topic);
                    }
                });
    }

    /**
     * Handle incoming full sensor data payload
     */
    private void handleSensorData(Mqtt5Publish publish) {
        try {
            String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
            logger.info("📥 Received payload: " + payload);

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonObject json = reader.readObject();

            // Parse all fields
            String deviceId = json.getString("deviceId", "unknown-device");
            String fieldId = json.getString("fieldId", "default-field");

            SensorData data = new SensorData();
            if (json.containsKey("temperature"))
                data.setTemperature(json.getJsonNumber("temperature").doubleValue());
            if (json.containsKey("humidity"))
                data.setHumidity(json.getJsonNumber("humidity").doubleValue());
            if (json.containsKey("soil_moisture"))
                data.setSoilMoisture(json.getJsonNumber("soil_moisture").doubleValue());
            if (json.containsKey("nitrogen"))
                data.setNitrogen(json.getJsonNumber("nitrogen").doubleValue());
            if (json.containsKey("phosphorus"))
                data.setPhosphorus(json.getJsonNumber("phosphorus").doubleValue());
            if (json.containsKey("potassium"))
                data.setPotassium(json.getJsonNumber("potassium").doubleValue());
            if (json.containsKey("pH"))
                data.setPh(json.getJsonNumber("pH").doubleValue());
            if (json.containsKey("rainfall"))
                data.setRainfall(json.getJsonNumber("rainfall").doubleValue());


            saveReading(deviceId, fieldId, data);

        } catch (Exception e) {
            logger.severe("❌ Error processing sensor/data message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Save reading in DB
     */
    private void saveReading(String deviceId, String fieldId, SensorData data) {
        try {
            Sensor sensor = null;
            try {
                sensor = sensorRepository.findByDeviceId(deviceId).orElse(null);
            } catch (Exception e) {
                logger.warning("⚠️ Sensor not found for deviceId: " + deviceId);
            }

            SensorReading reading = new SensorReading();
            reading.setId(UUID.randomUUID().toString());
            reading.setSensorId(sensor != null ? sensor.getId() : deviceId);
            reading.setFieldId(fieldId);
            reading.setTimestamp(LocalDateTime.now());
            reading.setData(data);

            readingRepository.save(reading);
            logger.info("✅ Saved reading for device " + deviceId +
                    " | Temp: " + data.getTemperature() +
                    "°C | Hum: " + data.getHumidity() +
                    "% | Moisture: " + data.getSoilMoisture() +
                    "% | NPK: " + data.getNitrogen() + "/" +
                    data.getPhosphorus() + "/" + data.getPotassium());

            if (sensor != null) {
                sensor.setLastConnection(LocalDateTime.now());
                sensor.setStatus("active");
                sensorRepository.save(sensor);
            }

        } catch (Exception e) {
            logger.severe("❌ Failed to save reading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String message) {
        if (mqttClient != null && mqttClient.getState().isConnected()) {
            mqttClient.publishWith()
                    .topic(topic)
                    .payload(message.getBytes(StandardCharsets.UTF_8))
                    .send()
                    .whenComplete((ack, throwable) -> {
                        if (throwable != null) {
                            logger.severe("❌ Failed to publish message: " + throwable.getMessage());
                        } else {
                            logger.info("📤 Published message to topic: " + topic);
                        }
                    });
        } else {
            logger.warning("⚠️ MQTT client not connected; message not sent");
        }
    }

    @PreDestroy
    public void cleanup() {
        if (mqttClient != null && mqttClient.getState().isConnected()) {
            mqttClient.disconnect();
            logger.info("🛑 Disconnected from MQTT broker");
        }
    }
}
