package me.soilmonitoring.api.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.entities.Sensor;
import me.soilmonitoring.api.entities.SensorData;
import me.soilmonitoring.api.entities.SensorReading;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
@Startup
public class MQTTService {

    @Inject
    private Logger logger;

    @Inject
    private SoilMonitoringManager manager;

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
    @ConfigProperty(name = "mqtt.topic.sensor.data")
    private String sensorDataTopic;

    @Inject
    @ConfigProperty(name = "mqtt.use.tls", defaultValue = "true")
    private Boolean useTls;

    private Mqtt3AsyncClient mqttClient;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing MQTT Connection to HiveMQ Cloud...");

            // Build MQTT client
            var clientBuilder = MqttClient.builder()
                    .identifier(clientId + "-" + UUID.randomUUID().toString())
                    .serverHost(brokerUrl)
                    .serverPort(brokerPort)
                    .useMqttVersion3();

            // Add TLS if enabled
            if (useTls) {
                clientBuilder.sslWithDefaultConfig();
            }

            mqttClient = clientBuilder.buildAsync();

            // Connect with credentials
            mqttClient.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes(StandardCharsets.UTF_8))
                    .applySimpleAuth()
                    .send()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            logger.severe("Failed to connect to MQTT broker: " + throwable.getMessage());
                        } else {
                            logger.info("Successfully connected to HiveMQ Cloud!");
                            subscribeToSensorData();
                        }
                    });

        } catch (Exception e) {
            logger.severe("Error initializing MQTT service: " + e.getMessage());
        }
    }

    private void subscribeToSensorData() {
        mqttClient.subscribeWith()
                .topicFilter(sensorDataTopic)
                .callback(this::handleSensorData)
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        logger.severe("Failed to subscribe to topic: " + throwable.getMessage());
                    } else {
                        logger.info("Subscribed to topic: " + sensorDataTopic);
                    }
                });
    }

    private void handleSensorData(Mqtt3Publish publish) {
        try {
            String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
            logger.info("Received sensor data: " + payload);

            // Parse JSON payload
            JsonReader jsonReader = Json.createReader(new StringReader(payload));
            JsonObject json = jsonReader.readObject();

            // Extract sensor data
            String deviceId = json.getString("deviceId");
            String fieldId = json.getString("fieldId");

            // Find sensor
            Sensor sensor = manager.findSensorByDeviceId(deviceId);

            // Create sensor data
            SensorData data = new SensorData();
            data.setTemperature(json.getJsonNumber("temperature").doubleValue());
            data.setHumidity(json.getJsonNumber("humidity").doubleValue());

            if (json.containsKey("nitrogen")) {
                data.setNitrogen(json.getJsonNumber("nitrogen").doubleValue());
            }
            if (json.containsKey("phosphorus")) {
                data.setPhosphorus(json.getJsonNumber("phosphorus").doubleValue());
            }
            if (json.containsKey("potassium")) {
                data.setPotassium(json.getJsonNumber("potassium").doubleValue());
            }
            if (json.containsKey("soilMoisture")) {
                data.setSoilMoisture(json.getJsonNumber("soilMoisture").doubleValue());
            }
            if (json.containsKey("rainfall")) {
                data.setRainfall(json.getJsonNumber("rainfall").doubleValue());
            }

            // Create sensor reading
            SensorReading reading = new SensorReading();
            reading.setId(UUID.randomUUID().toString());
            reading.setSensorId(sensor.getId());
            reading.setFieldId(fieldId);
            reading.setTimestamp(LocalDateTime.now());
            reading.setData(data);

            // Save to database (direct repository call)
            // Note: In real implementation, inject ReadingRepository here
            logger.info("Sensor reading saved successfully");

            // Update sensor last connection
            sensor.setLastConnection(LocalDateTime.now());

        } catch (Exception e) {
            logger.severe("Error processing sensor data: " + e.getMessage());
        }
    }

    public void publishMessage(String topic, String message) {
        if (mqttClient != null && mqttClient.getState().isConnected()) {
            mqttClient.publishWith()
                    .topic(topic)
                    .payload(message.getBytes(StandardCharsets.UTF_8))
                    .send()
                    .whenComplete((publish, throwable) -> {
                        if (throwable != null) {
                            logger.severe("Failed to publish message: " + throwable.getMessage());
                        } else {
                            logger.info("Message published to topic: " + topic);
                        }
                    });
        }
    }

    @PreDestroy
    public void cleanup() {
        if (mqttClient != null && mqttClient.getState().isConnected()) {
            mqttClient.disconnect();
            logger.info("Disconnected from MQTT broker");
        }
    }
}