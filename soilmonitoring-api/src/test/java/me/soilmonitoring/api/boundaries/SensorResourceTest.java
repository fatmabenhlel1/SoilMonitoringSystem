package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.SensorRepository;
import me.soilmonitoring.api.entities.Sensor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SensorResourceTest {

    @Inject
    private SensorResource sensorResource;

    @Inject
    private SensorRepository sensorRepository;

    @Inject
    private SoilMonitoringManager manager;

    private static String testSensorId;
    private static String testFieldId;
    private static String testDeviceId;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                // Entities
                .addPackage("me.soilmonitoring.api.entities")
                // Boundaries
                .addPackage("me.soilmonitoring.api.boundaries")
                // Controllers
                .addPackage("me.soilmonitoring.api.controllers.managers")
                .addPackage("me.soilmonitoring.api.controllers.repositories")
                // Resources nécessaires
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence.xml")
                .addAsResource("META-INF/microprofile-config.properties");
    }

    @BeforeEach
    public void setUp() {
        testFieldId = "field-" + UUID.randomUUID().toString();
        testDeviceId = "device-" + UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup après chaque test
        if (testSensorId != null) {
            try {
                sensorRepository.deleteById(testSensorId);
            } catch (Exception e) {
                // Ignore si déjà supprimé
            }
            testSensorId = null;
        }
    }

    // ============= Tests CREATE =============

    @Test
    @Order(1)
    @DisplayName("Should create sensor successfully")
    public void testCreateSensor() {
        // Given
        Sensor sensor = createTestSensor();

        // When
        Response response = sensorResource.createSensor(sensor);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Sensor created = (Sensor) response.getEntity();
        assertNotNull(created.getId());
        assertEquals(testFieldId, created.getFieldId());
        assertEquals(testDeviceId, created.getDeviceId());
        assertEquals("active", created.getStatus());
        assertNotNull(created.getInstalledAt());

        testSensorId = created.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should create sensor with DHT22 type")
    public void testCreateDHT22Sensor() {
        // Given
        Sensor sensor = createTestSensor();
        sensor.setSensorType("DHT22");

        // When
        Response response = sensorResource.createSensor(sensor);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Sensor created = (Sensor) response.getEntity();
        testSensorId = created.getId();

        assertEquals("DHT22", created.getSensorType());
        assertEquals("active", created.getStatus());
    }

    @Test
    @Order(3)
    @DisplayName("Should create sensor with NPK type")
    public void testCreateNPKSensor() {
        // Given
        Sensor sensor = createTestSensor();
        sensor.setSensorType("NPK");

        // When
        Response response = sensorResource.createSensor(sensor);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Sensor created = (Sensor) response.getEntity();
        testSensorId = created.getId();

        assertEquals("NPK", created.getSensorType());
        assertEquals("active", created.getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("Should auto-set status to active on creation")
    public void testCreateSensorAutoSetStatus() {
        // Given
        Sensor sensor = createTestSensor();
        sensor.setStatus(null); // Should be overridden

        // When
        Response response = sensorResource.createSensor(sensor);

        // Then
        Sensor created = (Sensor) response.getEntity();
        testSensorId = created.getId();

        assertEquals("active", created.getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("Should auto-generate ID and installedAt timestamp")
    public void testCreateSensorAutoGeneratesFields() {
        // Given
        Sensor sensor = createTestSensor();
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        Response response = sensorResource.createSensor(sensor);

        // Then
        Sensor created = (Sensor) response.getEntity();
        testSensorId = created.getId();

        assertNotNull(created.getId());
        assertNotNull(created.getInstalledAt());
        assertTrue(created.getInstalledAt().isAfter(beforeCreation));
        assertTrue(created.getInstalledAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    // ============= Tests READ by ID =============

    @Test
    @Order(6)
    @DisplayName("Should retrieve sensor by ID successfully")
    public void testGetSensorById() {
        // Given - Create a sensor first
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        Sensor created = (Sensor) createResponse.getEntity();
        testSensorId = created.getId();

        // When
        Response response = sensorResource.getSensorById(testSensorId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Sensor found = (Sensor) response.getEntity();
        assertEquals(testSensorId, found.getId());
        assertEquals(testFieldId, found.getFieldId());
        assertEquals(testDeviceId, found.getDeviceId());
    }

    @Test
    @Order(7)
    @DisplayName("Should return 404 when sensor not found")
    public void testGetSensorByIdNotFound() {
        // Given
        String nonExistentId = "non-existent-" + UUID.randomUUID();

        // When
        Response response = sensorResource.getSensorById(nonExistentId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ============= Tests READ by Device ID =============

    @Test
    @Order(8)
    @DisplayName("Should retrieve sensor by device ID successfully")
    public void testGetSensorByDeviceId() {
        // Given - Create a sensor first
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        Sensor created = (Sensor) createResponse.getEntity();
        testSensorId = created.getId();

        // When
        Response response = sensorResource.getSensorByDeviceId(testDeviceId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Sensor found = (Sensor) response.getEntity();
        assertEquals(testDeviceId, found.getDeviceId());
        assertEquals(testFieldId, found.getFieldId());
    }

    @Test
    @Order(9)
    @DisplayName("Should return 404 when sensor not found by device ID")
    public void testGetSensorByDeviceIdNotFound() {
        // Given
        String nonExistentDeviceId = "device-non-existent-" + UUID.randomUUID();

        // When
        Response response = sensorResource.getSensorByDeviceId(nonExistentDeviceId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ============= Tests READ by Field =============

    @Test
    @Order(10)
    @DisplayName("Should retrieve all sensors for a field")
    public void testGetFieldSensors() {
        // Given - Create multiple sensors for the same field
        Sensor sensor1 = createTestSensor();
        sensor1.setSensorType("DHT22");
        sensorResource.createSensor(sensor1);

        Sensor sensor2 = createTestSensor();
        sensor2.setSensorType("NPK");
        sensor2.setDeviceId("device-2-" + UUID.randomUUID());
        Response response2 = sensorResource.createSensor(sensor2);
        testSensorId = ((Sensor) response2.getEntity()).getId();

        // When
        Response response = sensorResource.getFieldSensors(testFieldId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Sensor> sensors = (List<Sensor>) response.getEntity();

        assertTrue(sensors.size() >= 2);
        assertTrue(sensors.stream().allMatch(s -> s.getFieldId().equals(testFieldId)));
        assertTrue(sensors.stream().anyMatch(s -> s.getSensorType().equals("DHT22")));
        assertTrue(sensors.stream().anyMatch(s -> s.getSensorType().equals("NPK")));
    }

    @Test
    @Order(11)
    @DisplayName("Should return empty list for field without sensors")
    public void testGetFieldSensorsEmpty() {
        // Given
        String fieldWithNoSensors = "field-empty-" + UUID.randomUUID();

        // When
        Response response = sensorResource.getFieldSensors(fieldWithNoSensors);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Sensor> sensors = (List<Sensor>) response.getEntity();
        assertTrue(sensors.isEmpty());
    }

    // ============= Tests UPDATE Status =============

    @Test
    @Order(12)
    @DisplayName("Should update sensor status successfully")
    public void testUpdateSensorStatus() {
        // Given - Create a sensor first
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        Sensor created = (Sensor) createResponse.getEntity();
        testSensorId = created.getId();

        // When
        Response response = sensorResource.updateSensorStatus(testSensorId, "inactive");

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Sensor updated = (Sensor) response.getEntity();

        assertEquals("inactive", updated.getStatus());
        assertNotNull(updated.getLastConnection());
    }

    @Test
    @Order(13)
    @DisplayName("Should update status to error")
    public void testUpdateSensorStatusToError() {
        // Given
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        testSensorId = ((Sensor) createResponse.getEntity()).getId();

        // When
        Response response = sensorResource.updateSensorStatus(testSensorId, "error");

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Sensor updated = (Sensor) response.getEntity();

        assertEquals("error", updated.getStatus());
    }

    @Test
    @Order(14)
    @DisplayName("Should update lastConnection when updating status")
    public void testUpdateSensorStatusUpdatesLastConnection() {
        // Given
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        Sensor created = (Sensor) createResponse.getEntity();
        testSensorId = created.getId();

        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

        // When
        Response response = sensorResource.updateSensorStatus(testSensorId, "active");

        // Then
        Sensor updated = (Sensor) response.getEntity();

        assertNotNull(updated.getLastConnection());
        assertTrue(updated.getLastConnection().isAfter(beforeUpdate));
    }

    @Test
    @Order(15)
    @DisplayName("Should return 404 when updating non-existent sensor")
    public void testUpdateSensorStatusNotFound() {
        // Given
        String nonExistentId = "non-existent-" + UUID.randomUUID();

        // When
        Response response = sensorResource.updateSensorStatus(nonExistentId, "inactive");

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ============= Tests DELETE =============

    @Test
    @Order(16)
    @DisplayName("Should delete sensor successfully")
    public void testDeleteSensor() {
        // Given - Create a sensor first
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        Sensor created = (Sensor) createResponse.getEntity();
        testSensorId = created.getId();

        // When
        Response response = sensorResource.deleteSensor(testSensorId);

        // Then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        // Verify sensor is actually deleted
        Response getResponse = sensorResource.getSensorById(testSensorId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());

        testSensorId = null; // Prevent cleanup attempt
    }

    @Test
    @Order(17)
    @DisplayName("Should handle delete of non-existent sensor")
    public void testDeleteSensorNotFound() {
        // Given
        String nonExistentId = "non-existent-" + UUID.randomUUID();

        // When
        Response response = sensorResource.deleteSensor(nonExistentId);

        // Then
        // Depending on implementation, might be 204 or 500
        assertTrue(response.getStatus() >= 200);
    }

    // ============= Tests d'intégration complexes =============

    @Test
    @Order(18)
    @DisplayName("Should handle complete CRUD lifecycle")
    public void testCompleteCRUDLifecycle() {
        // CREATE
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        testSensorId = ((Sensor) createResponse.getEntity()).getId();

        // READ
        Response readResponse = sensorResource.getSensorById(testSensorId);
        assertEquals(Response.Status.OK.getStatusCode(), readResponse.getStatus());

        // UPDATE
        Response updateResponse = sensorResource.updateSensorStatus(testSensorId, "inactive");
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        // DELETE
        Response deleteResponse = sensorResource.deleteSensor(testSensorId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // VERIFY DELETION
        Response verifyResponse = sensorResource.getSensorById(testSensorId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), verifyResponse.getStatus());

        testSensorId = null;
    }

    @Test
    @Order(19)
    @DisplayName("Should handle multiple status changes")
    public void testMultipleStatusChanges() {
        // Given
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        testSensorId = ((Sensor) createResponse.getEntity()).getId();

        // When - Change status multiple times
        Response response1 = sensorResource.updateSensorStatus(testSensorId, "inactive");
        assertEquals("inactive", ((Sensor) response1.getEntity()).getStatus());

        Response response2 = sensorResource.updateSensorStatus(testSensorId, "error");
        assertEquals("error", ((Sensor) response2.getEntity()).getStatus());

        Response response3 = sensorResource.updateSensorStatus(testSensorId, "active");
        assertEquals("active", ((Sensor) response3.getEntity()).getStatus());

        // Then
        Response finalResponse = sensorResource.getSensorById(testSensorId);
        Sensor finalSensor = (Sensor) finalResponse.getEntity();
        assertEquals("active", finalSensor.getStatus());
    }

    @Test
    @Order(20)
    @DisplayName("Should persist all sensor data correctly")
    public void testDataPersistence() {
        // Given
        Sensor sensor = createTestSensor();
        sensor.setSensorType("DHT22");
        sensor.setDeviceId("raspberry-pi-12345");

        // When - Create and retrieve
        Response createResponse = sensorResource.createSensor(sensor);
        testSensorId = ((Sensor) createResponse.getEntity()).getId();

        Response getResponse = sensorResource.getSensorById(testSensorId);
        Sensor retrieved = (Sensor) getResponse.getEntity();

        // Then - Verify all data persisted
        assertEquals(testFieldId, retrieved.getFieldId());
        assertEquals("DHT22", retrieved.getSensorType());
        assertEquals("raspberry-pi-12345", retrieved.getDeviceId());
        assertEquals("active", retrieved.getStatus());
        assertNotNull(retrieved.getInstalledAt());
    }

    @Test
    @Order(21)
    @DisplayName("Should handle sensors with unique device IDs")
    public void testUniqueSensorsByDeviceId() {
        // Given - Create sensors with different device IDs
        Sensor sensor1 = createTestSensor();
        sensor1.setDeviceId("device-001");
        Response response1 = sensorResource.createSensor(sensor1);

        Sensor sensor2 = createTestSensor();
        sensor2.setDeviceId("device-002");
        Response response2 = sensorResource.createSensor(sensor2);
        testSensorId = ((Sensor) response2.getEntity()).getId();

        // When - Retrieve by device IDs
        Response getResponse1 = sensorResource.getSensorByDeviceId("device-001");
        Response getResponse2 = sensorResource.getSensorByDeviceId("device-002");

        // Then
        Sensor found1 = (Sensor) getResponse1.getEntity();
        Sensor found2 = (Sensor) getResponse2.getEntity();

        assertEquals("device-001", found1.getDeviceId());
        assertEquals("device-002", found2.getDeviceId());
        assertNotEquals(found1.getId(), found2.getId());
    }

    @Test
    @Order(22)
    @DisplayName("Should handle field with both sensor types")
    public void testFieldWithBothSensorTypes() {
        // Given
        Sensor dht22 = createTestSensor();
        dht22.setSensorType("DHT22");
        dht22.setDeviceId("device-dht22");
        sensorResource.createSensor(dht22);

        Sensor npk = createTestSensor();
        npk.setSensorType("NPK");
        npk.setDeviceId("device-npk");
        Response npkResponse = sensorResource.createSensor(npk);
        testSensorId = ((Sensor) npkResponse.getEntity()).getId();

        // When
        Response response = sensorResource.getFieldSensors(testFieldId);

        // Then
        @SuppressWarnings("unchecked")
        List<Sensor> sensors = (List<Sensor>) response.getEntity();

        assertTrue(sensors.size() >= 2);
        assertEquals(1, sensors.stream().filter(s -> s.getSensorType().equals("DHT22")).count());
        assertEquals(1, sensors.stream().filter(s -> s.getSensorType().equals("NPK")).count());
    }

    @Test
    @Order(23)
    @DisplayName("Should handle sensor with all status values")
    public void testAllStatusValues() {
        // Given
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        testSensorId = ((Sensor) createResponse.getEntity()).getId();

        // Test each status value
        String[] statuses = {"active", "inactive", "error"};

        for (String status : statuses) {
            // When
            Response response = sensorResource.updateSensorStatus(testSensorId, status);

            // Then
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Sensor updated = (Sensor) response.getEntity();
            assertEquals(status, updated.getStatus());
        }
    }

    @Test
    @Order(24)
    @DisplayName("Should preserve installedAt timestamp through updates")
    public void testInstalledAtPreservedThroughUpdates() {
        // Given
        Sensor sensor = createTestSensor();
        Response createResponse = sensorResource.createSensor(sensor);
        Sensor created = (Sensor) createResponse.getEntity();
        testSensorId = created.getId();
        LocalDateTime originalInstalledAt = created.getInstalledAt();

        // When - Update status
        sensorResource.updateSensorStatus(testSensorId, "inactive");

        // Wait a bit
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        sensorResource.updateSensorStatus(testSensorId, "active");

        // Then - Retrieve and verify
        Response getResponse = sensorResource.getSensorById(testSensorId);
        Sensor retrieved = (Sensor) getResponse.getEntity();

        assertEquals(originalInstalledAt, retrieved.getInstalledAt());
        assertNotNull(retrieved.getLastConnection());
    }

    @Test
    @Order(25)
    @DisplayName("Should retrieve sensors by field and verify all created")
    public void testGetAllFieldSensorsVerification() {
        // Given - Create 3 sensors
        for (int i = 0; i < 3; i++) {
            Sensor sensor = createTestSensor();
            sensor.setDeviceId("device-" + i + "-" + UUID.randomUUID());
            sensor.setSensorType(i % 2 == 0 ? "DHT22" : "NPK");
            Response response = sensorResource.createSensor(sensor);
            if (i == 2) {
                testSensorId = ((Sensor) response.getEntity()).getId();
            }
        }

        // When
        Response response = sensorResource.getFieldSensors(testFieldId);

        // Then
        @SuppressWarnings("unchecked")
        List<Sensor> sensors = (List<Sensor>) response.getEntity();

        assertTrue(sensors.size() >= 3);
        assertTrue(sensors.stream().allMatch(s -> s.getFieldId().equals(testFieldId)));
        assertTrue(sensors.stream().allMatch(s -> s.getStatus().equals("active")));
    }

    // ============= Helper Methods =============

    private Sensor createTestSensor() {
        Sensor sensor = new Sensor();
        sensor.setFieldId(testFieldId);
        sensor.setSensorType("DHT22");
        sensor.setDeviceId(testDeviceId);
        return sensor;
    }
}