package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.SensorReadingRepository;
import me.soilmonitoring.api.entities.SensorReading;
import me.soilmonitoring.api.entities.SensorData;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadingResourceTest {

    @Inject
    private ReadingResource readingResource;

    @Inject
    private SensorReadingRepository readingRepository;

    @Inject
    private SoilMonitoringManager manager;

    private static String testReadingId;
    private static String testFieldId;
    private static String testSensorId;

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
        testSensorId = "sensor-" + UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup après chaque test
        if (testReadingId != null) {
            try {
                readingRepository.deleteById(testReadingId);
            } catch (Exception e) {
                // Ignore si déjà supprimé
            }
            testReadingId = null;
        }
    }

    // ============= Tests CREATE =============

    @Test
    @Order(1)
    @DisplayName("Should create sensor reading successfully")
    public void testCreateReading() {
        // Given
        SensorReading reading = createTestReading();

        // When
        Response response = readingResource.createReading(reading);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        SensorReading created = (SensorReading) response.getEntity();
        assertNotNull(created.getId());
        assertEquals(testFieldId, created.getFieldId());
        assertEquals(testSensorId, created.getSensorId());
        assertNotNull(created.getTimestamp());
        assertNotNull(created.getData());

        testReadingId = created.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should create reading with complete sensor data")
    public void testCreateReadingWithCompleteData() {
        // Given
        SensorReading reading = createTestReading();
        SensorData data = new SensorData();
        data.setTemperature(25.5);
        data.setHumidity(65.0);
        data.setNitrogen(45.0);
        data.setPhosphorus(38.0);
        data.setPotassium(42.0);
        data.setSoilMoisture(55.0);
        data.setRainfall(10.5);
        data.setPh(6.8);
        reading.setData(data);

        // When
        Response response = readingResource.createReading(reading);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        SensorReading created = (SensorReading) response.getEntity();
        testReadingId = created.getId();

        assertNotNull(created.getData());
        assertEquals(25.5, created.getData().getTemperature());
        assertEquals(65.0, created.getData().getHumidity());
        assertEquals(45.0, created.getData().getNitrogen());
        assertEquals(38.0, created.getData().getPhosphorus());
        assertEquals(42.0, created.getData().getPotassium());
        assertEquals(55.0, created.getData().getSoilMoisture());
        assertEquals(10.5, created.getData().getRainfall());
        assertEquals(6.8, created.getData().getPh());
    }

    @Test
    @Order(3)
    @DisplayName("Should auto-generate ID and timestamp on creation")
    public void testCreateReadingAutoGeneratesFields() {
        // Given
        SensorReading reading = createTestReading();
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        Response response = readingResource.createReading(reading);

        // Then
        SensorReading created = (SensorReading) response.getEntity();
        testReadingId = created.getId();

        assertNotNull(created.getId());
        assertNotNull(created.getTimestamp());
        assertTrue(created.getTimestamp().isAfter(beforeCreation));
        assertTrue(created.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @Order(4)
    @DisplayName("Should handle reading with partial sensor data")
    public void testCreateReadingWithPartialData() {
        // Given
        SensorReading reading = createTestReading();
        SensorData data = new SensorData();
        data.setTemperature(22.0);
        data.setHumidity(60.0);
        // Autres champs null
        reading.setData(data);

        // When
        Response response = readingResource.createReading(reading);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        SensorReading created = (SensorReading) response.getEntity();
        testReadingId = created.getId();

        assertEquals(22.0, created.getData().getTemperature());
        assertEquals(60.0, created.getData().getHumidity());
    }

    // ============= Tests READ by ID =============

    @Test
    @Order(5)
    @DisplayName("Should retrieve reading by ID successfully")
    public void testGetReadingById() {
        // Given - Create a reading first
        SensorReading reading = createTestReading();
        Response createResponse = readingResource.createReading(reading);
        SensorReading created = (SensorReading) createResponse.getEntity();
        testReadingId = created.getId();

        // When
        Response response = readingResource.getReadingById(testReadingId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        SensorReading found = (SensorReading) response.getEntity();
        assertEquals(testReadingId, found.getId());
        assertEquals(testFieldId, found.getFieldId());
        assertEquals(testSensorId, found.getSensorId());
    }

    @Test
    @Order(6)
    @DisplayName("Should return 404 when reading not found")
    public void testGetReadingByIdNotFound() {
        // Given
        String nonExistentId = "non-existent-" + UUID.randomUUID();

        // When
        Response response = readingResource.getReadingById(nonExistentId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ============= Tests READ by Field =============

    @Test
    @Order(7)
    @DisplayName("Should retrieve all readings for a field")
    public void testGetFieldReadings() {
        // Given - Create multiple readings for the same field
        for (int i = 0; i < 3; i++) {
            SensorReading reading = createTestReading();
            Response response = readingResource.createReading(reading);
            if (i == 2) {
                testReadingId = ((SensorReading) response.getEntity()).getId();
            }
        }

        // When
        Response response = readingResource.getFieldReadings(testFieldId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();

        assertTrue(readings.size() >= 3);
        assertTrue(readings.stream().allMatch(r -> r.getFieldId().equals(testFieldId)));
    }

    @Test
    @Order(8)
    @DisplayName("Should return empty list for field without readings")
    public void testGetFieldReadingsEmpty() {
        // Given
        String fieldWithNoReadings = "field-empty-" + UUID.randomUUID();

        // When
        Response response = readingResource.getFieldReadings(fieldWithNoReadings);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();
        assertTrue(readings.isEmpty());
    }

    // ============= Tests READ by Time Range =============

    @Test
    @Order(9)
    @DisplayName("Should retrieve readings within time range")
    public void testGetFieldReadingsByTimeRange() {
        // Given - Create readings with different timestamps
        LocalDateTime now = LocalDateTime.now();

        SensorReading reading1 = createTestReading();
        readingResource.createReading(reading1);

        // Petit délai pour assurer des timestamps différents
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SensorReading reading2 = createTestReading();
        Response response2 = readingResource.createReading(reading2);
        testReadingId = ((SensorReading) response2.getEntity()).getId();

        LocalDateTime from = now.minusMinutes(5);
        LocalDateTime to = now.plusMinutes(5);

        // When
        Response response = readingResource.getFieldReadingsByTimeRange(
                testFieldId,
                from.toString(),
                to.toString()
        );

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();

        assertTrue(readings.size() >= 2);
        assertTrue(readings.stream().allMatch(r ->
                r.getTimestamp().isAfter(from) && r.getTimestamp().isBefore(to)
        ));
    }

    @Test
    @Order(10)
    @DisplayName("Should return empty list for time range without readings")
    public void testGetFieldReadingsByTimeRangeEmpty() {
        // Given
        LocalDateTime pastFrom = LocalDateTime.now().minusDays(10);
        LocalDateTime pastTo = LocalDateTime.now().minusDays(9);

        // When
        Response response = readingResource.getFieldReadingsByTimeRange(
                testFieldId,
                pastFrom.toString(),
                pastTo.toString()
        );

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();
        assertTrue(readings.isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("Should handle invalid date format in time range")
    public void testGetFieldReadingsByTimeRangeInvalidFormat() {
        // Given
        String invalidFrom = "invalid-date";
        String invalidTo = "2024-01-01T00:00:00";

        // When
        Response response = readingResource.getFieldReadingsByTimeRange(
                testFieldId,
                invalidFrom,
                invalidTo
        );

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("Should retrieve readings for specific date range")
    public void testGetFieldReadingsBySpecificDateRange() {
        // Given - Create reading
        SensorReading reading = createTestReading();
        Response createResponse = readingResource.createReading(reading);
        testReadingId = ((SensorReading) createResponse.getEntity()).getId();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(1);
        LocalDateTime to = now.plusHours(1);

        // When
        Response response = readingResource.getFieldReadingsByTimeRange(
                testFieldId,
                from.toString(),
                to.toString()
        );

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();
        assertTrue(readings.size() >= 1);
    }

    // ============= Tests GET Latest Reading =============

    @Test
    @Order(13)
    @DisplayName("Should retrieve latest reading for a field")
    public void testGetLatestReading() {
        // Given - Create multiple readings
        for (int i = 0; i < 3; i++) {
            SensorReading reading = createTestReading();
            SensorData data = new SensorData();
            data.setTemperature(20.0 + i);
            reading.setData(data);

            Response response = readingResource.createReading(reading);
            if (i == 2) {
                testReadingId = ((SensorReading) response.getEntity()).getId();
            }

            // Petit délai pour assurer ordre chronologique
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // When
        Response response = readingResource.getLatestReading(testFieldId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        SensorReading latest = (SensorReading) response.getEntity();
        assertNotNull(latest);
        assertEquals(testFieldId, latest.getFieldId());
    }

    @Test
    @Order(14)
    @DisplayName("Should return 404 when no readings found for field")
    public void testGetLatestReadingNotFound() {
        // Given
        String fieldWithNoReadings = "field-no-readings-" + UUID.randomUUID();

        // When
        Response response = readingResource.getLatestReading(fieldWithNoReadings);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ============= Tests d'intégration complexes =============

    @Test
    @Order(15)
    @DisplayName("Should handle multiple readings from different sensors")
    public void testMultipleReadingsFromDifferentSensors() {
        // Given - Create readings from different sensors for same field
        String sensor1 = "sensor-1-" + UUID.randomUUID();
        String sensor2 = "sensor-2-" + UUID.randomUUID();

        SensorReading reading1 = createTestReading();
        reading1.setSensorId(sensor1);
        readingResource.createReading(reading1);

        SensorReading reading2 = createTestReading();
        reading2.setSensorId(sensor2);
        Response response2 = readingResource.createReading(reading2);
        testReadingId = ((SensorReading) response2.getEntity()).getId();

        // When
        Response response = readingResource.getFieldReadings(testFieldId);

        // Then
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();

        assertTrue(readings.size() >= 2);
        assertTrue(readings.stream().anyMatch(r -> r.getSensorId().equals(sensor1)));
        assertTrue(readings.stream().anyMatch(r -> r.getSensorId().equals(sensor2)));
    }

    @Test
    @Order(16)
    @DisplayName("Should persist all sensor data fields correctly")
    public void testDataPersistence() {
        // Given
        SensorReading reading = createTestReading();
        SensorData data = new SensorData();
        data.setTemperature(26.7);
        data.setHumidity(68.3);
        data.setNitrogen(42.5);
        data.setPhosphorus(35.8);
        data.setPotassium(40.2);
        data.setSoilMoisture(52.6);
        data.setRainfall(8.3);
        data.setPh(6.9);
        reading.setData(data);

        // When - Create and retrieve
        Response createResponse = readingResource.createReading(reading);
        testReadingId = ((SensorReading) createResponse.getEntity()).getId();

        Response getResponse = readingResource.getReadingById(testReadingId);
        SensorReading retrieved = (SensorReading) getResponse.getEntity();

        // Then - Verify all data persisted
        assertNotNull(retrieved.getData());
        assertEquals(26.7, retrieved.getData().getTemperature());
        assertEquals(68.3, retrieved.getData().getHumidity());
        assertEquals(42.5, retrieved.getData().getNitrogen());
        assertEquals(35.8, retrieved.getData().getPhosphorus());
        assertEquals(40.2, retrieved.getData().getPotassium());
        assertEquals(52.6, retrieved.getData().getSoilMoisture());
        assertEquals(8.3, retrieved.getData().getRainfall());
        assertEquals(6.9, retrieved.getData().getPh());
    }

    @Test
    @Order(17)
    @DisplayName("Should handle readings with extreme sensor values")
    public void testExtremeSensorValues() {
        // Given
        SensorReading reading = createTestReading();
        SensorData data = new SensorData();
        data.setTemperature(50.0);  // Extrême chaud
        data.setHumidity(95.0);     // Très humide
        data.setNitrogen(100.0);    // Très élevé
        data.setPhosphorus(0.0);    // Très bas
        data.setPotassium(80.0);
        data.setSoilMoisture(90.0); // Très humide
        data.setRainfall(100.0);    // Forte pluie
        data.setPh(9.0);            // Très alcalin
        reading.setData(data);

        // When
        Response response = readingResource.createReading(reading);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        SensorReading created = (SensorReading) response.getEntity();
        testReadingId = created.getId();

        assertEquals(50.0, created.getData().getTemperature());
        assertEquals(95.0, created.getData().getHumidity());
    }

    @Test
    @Order(18)
    @DisplayName("Should handle chronological order of readings")
    public void testChronologicalOrder() {
        // Given - Create readings in sequence
        LocalDateTime startTime = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            SensorReading reading = createTestReading();
            Response response = readingResource.createReading(reading);
            if (i == 4) {
                testReadingId = ((SensorReading) response.getEntity()).getId();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // When
        Response response = readingResource.getFieldReadings(testFieldId);

        // Then
        @SuppressWarnings("unchecked")
        List<SensorReading> readings = (List<SensorReading>) response.getEntity();

        assertTrue(readings.size() >= 5);

        // Verify all readings are after start time
        assertTrue(readings.stream().allMatch(r ->
                r.getTimestamp().isAfter(startTime.minusSeconds(1))
        ));
    }

    @Test
    @Order(19)
    @DisplayName("Should handle readings with null optional fields")
    public void testReadingWithNullOptionalFields() {
        // Given
        SensorReading reading = new SensorReading();
        reading.setFieldId(testFieldId);
        reading.setSensorId(testSensorId);

        SensorData data = new SensorData();
        data.setTemperature(25.0);
        data.setHumidity(60.0);
        // Autres champs restent null
        reading.setData(data);

        // When
        Response response = readingResource.createReading(reading);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        SensorReading created = (SensorReading) response.getEntity();
        testReadingId = created.getId();

        assertNotNull(created.getData());
        assertEquals(25.0, created.getData().getTemperature());
        assertNull(created.getData().getNitrogen());
        assertNull(created.getData().getPhosphorus());
    }

    @Test
    @Order(20)
    @DisplayName("Should retrieve latest reading correctly with multiple readings")
    public void testLatestReadingWithMultipleReadings() {
        // Given - Create several readings with delays
        SensorReading oldest = createTestReading();
        oldest.getData().setTemperature(20.0);
        readingResource.createReading(oldest);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SensorReading middle = createTestReading();
        middle.getData().setTemperature(22.0);
        readingResource.createReading(middle);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SensorReading newest = createTestReading();
        newest.getData().setTemperature(25.0);
        Response createResponse = readingResource.createReading(newest);
        testReadingId = ((SensorReading) createResponse.getEntity()).getId();

        // When
        Response response = readingResource.getLatestReading(testFieldId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        SensorReading latest = (SensorReading) response.getEntity();

        // Should be the last created reading
        assertEquals(25.0, latest.getData().getTemperature());
    }

    // ============= Helper Methods =============

    private SensorReading createTestReading() {
        SensorReading reading = new SensorReading();
        reading.setFieldId(testFieldId);
        reading.setSensorId(testSensorId);

        SensorData data = new SensorData();
        data.setTemperature(22.0);
        data.setHumidity(60.0);
        data.setNitrogen(40.0);
        data.setPhosphorus(35.0);
        data.setPotassium(38.0);
        data.setSoilMoisture(50.0);
        data.setRainfall(5.0);
        data.setPh(6.5);

        reading.setData(data);

        return reading;
    }
}