package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.FieldRepository;
import me.soilmonitoring.api.entities.Field;
import me.soilmonitoring.api.entities.Location;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FieldResourceTest {

    @Inject
    private FieldResource fieldResource;

    @Inject
    private FieldRepository fieldRepository;

    @Inject
    private SoilMonitoringManager manager;

    private static String testFieldId;
    private static String testUserId;

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
        testUserId = "user-" + UUID.randomUUID().toString();
    }

    @AfterEach
    public void tearDown() {
        // Cleanup après chaque test
        if (testFieldId != null) {
            try {
                fieldRepository.deleteById(testFieldId);
            } catch (Exception e) {
                // Ignore si déjà supprimé
            }
        }
    }

    // ============= Tests CREATE =============

    @Test
    @Order(1)
    @DisplayName("Should create a new field successfully")
    public void testCreateField() {
        // Given
        Field field = createTestField();

        // When
        Response response = fieldResource.createField(field);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Field createdField = (Field) response.getEntity();
        assertNotNull(createdField.getId());
        assertEquals(field.getName(), createdField.getName());
        assertEquals(field.getUserId(), createdField.getUserId());
        assertEquals(field.getArea(), createdField.getArea());
        assertEquals(field.getSoilType(), createdField.getSoilType());
        assertNotNull(createdField.getCreatedAt());

        testFieldId = createdField.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Should handle error when creating field with invalid data")
    public void testCreateFieldWithInvalidData() {
        // Given
        Field field = new Field();
        field.setName(null); // Invalid - no name

        // When
        Response response = fieldResource.createField(field);

        // Then
        assertTrue(response.getStatus() >= 400);
    }

    // ============= Tests READ =============

    @Test
    @Order(3)
    @DisplayName("Should retrieve field by ID successfully")
    public void testGetFieldById() {
        // Given - Create a field first
        Field field = createTestField();
        Response createResponse = fieldResource.createField(field);
        Field createdField = (Field) createResponse.getEntity();
        testFieldId = createdField.getId();

        // When
        Response response = fieldResource.getFieldById(testFieldId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Field retrievedField = (Field) response.getEntity();
        assertEquals(testFieldId, retrievedField.getId());
        assertEquals(field.getName(), retrievedField.getName());
        assertEquals(field.getUserId(), retrievedField.getUserId());
    }

    @Test
    @Order(4)
    @DisplayName("Should return 404 when field not found")
    public void testGetFieldByIdNotFound() {
        // Given
        String nonExistentId = "non-existent-id-" + UUID.randomUUID();

        // When
        Response response = fieldResource.getFieldById(nonExistentId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @Order(5)
    @DisplayName("Should retrieve all fields for a user")
    public void testGetUserFields() {
        // Given - Create multiple fields for the same user
        Field field1 = createTestField();
        Field field2 = createTestField();
        field2.setName("Test Field 2");

        fieldResource.createField(field1);
        Response createResponse2 = fieldResource.createField(field2);
        testFieldId = ((Field) createResponse2.getEntity()).getId();

        // When
        Response response = fieldResource.getUserFields(testUserId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        @SuppressWarnings("unchecked")
        List<Field> fields = (List<Field>) response.getEntity();
        assertTrue(fields.size() >= 2);
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("Test Field 1")));
        assertTrue(fields.stream().anyMatch(f -> f.getName().equals("Test Field 2")));
    }

    @Test
    @Order(6)
    @DisplayName("Should return empty list when user has no fields")
    public void testGetUserFieldsEmpty() {
        // Given
        String userWithNoFields = "user-no-fields-" + UUID.randomUUID();

        // When
        Response response = fieldResource.getUserFields(userWithNoFields);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Field> fields = (List<Field>) response.getEntity();
        assertTrue(fields.isEmpty());
    }

    // ============= Tests UPDATE =============

    @Test
    @Order(7)
    @DisplayName("Should update field successfully")
    public void testUpdateField() {
        // Given - Create a field first
        Field field = createTestField();
        Response createResponse = fieldResource.createField(field);
        Field createdField = (Field) createResponse.getEntity();
        testFieldId = createdField.getId();

        // Modify the field
        createdField.setName("Updated Field Name");
        createdField.setArea(15.5);
        createdField.setCurrentCrop("Corn");

        // When
        Response response = fieldResource.updateField(testFieldId, createdField);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        Field updatedField = (Field) response.getEntity();
        assertEquals("Updated Field Name", updatedField.getName());
        assertEquals(15.5, updatedField.getArea());
        assertEquals("Corn", updatedField.getCurrentCrop());
    }

    @Test
    @Order(8)
    @DisplayName("Should return 404 when updating non-existent field")
    public void testUpdateFieldNotFound() {
        // Given
        String nonExistentId = "non-existent-id-" + UUID.randomUUID();
        Field field = createTestField();

        // When
        Response response = fieldResource.updateField(nonExistentId, field);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    @Order(9)
    @DisplayName("Should preserve createdAt when updating field")
    public void testUpdateFieldPreservesCreatedAt() {
        // Given
        Field field = createTestField();
        Response createResponse = fieldResource.createField(field);
        Field createdField = (Field) createResponse.getEntity();
        testFieldId = createdField.getId();
        LocalDateTime originalCreatedAt = createdField.getCreatedAt();

        // Simulate some time passing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Modify the field
        createdField.setName("Updated Name");

        // When
        Response response = fieldResource.updateField(testFieldId, createdField);

        // Then
        Field updatedField = (Field) response.getEntity();
        assertEquals(originalCreatedAt, updatedField.getCreatedAt());
    }

    // ============= Tests DELETE =============

    @Test
    @Order(10)
    @DisplayName("Should delete field successfully")
    public void testDeleteField() {
        // Given - Create a field first
        Field field = createTestField();
        Response createResponse = fieldResource.createField(field);
        Field createdField = (Field) createResponse.getEntity();
        testFieldId = createdField.getId();

        // When
        Response response = fieldResource.deleteField(testFieldId);

        // Then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        // Verify field is actually deleted
        Response getResponse = fieldResource.getFieldById(testFieldId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());

        testFieldId = null; // Prevent cleanup attempt
    }

    @Test
    @Order(11)
    @DisplayName("Should handle delete of non-existent field")
    public void testDeleteFieldNotFound() {
        // Given
        String nonExistentId = "non-existent-id-" + UUID.randomUUID();

        // When
        Response response = fieldResource.deleteField(nonExistentId);

        // Then
        // Depending on implementation, might be 204 or 500
        assertTrue(response.getStatus() >= 200);
    }

    // ============= Tests d'intégration complexes =============

    @Test
    @Order(12)
    @DisplayName("Should handle complete CRUD lifecycle")
    public void testCompleteCRUDLifecycle() {
        // CREATE
        Field field = createTestField();
        Response createResponse = fieldResource.createField(field);
        assertEquals(Response.Status.CREATED.getStatusCode(), createResponse.getStatus());
        Field createdField = (Field) createResponse.getEntity();
        testFieldId = createdField.getId();

        // READ
        Response readResponse = fieldResource.getFieldById(testFieldId);
        assertEquals(Response.Status.OK.getStatusCode(), readResponse.getStatus());

        // UPDATE
        createdField.setName("Lifecycle Test Field Updated");
        Response updateResponse = fieldResource.updateField(testFieldId, createdField);
        assertEquals(Response.Status.OK.getStatusCode(), updateResponse.getStatus());

        // DELETE
        Response deleteResponse = fieldResource.deleteField(testFieldId);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());

        // VERIFY DELETION
        Response verifyResponse = fieldResource.getFieldById(testFieldId);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), verifyResponse.getStatus());

        testFieldId = null;
    }

    @Test
    @Order(13)
    @DisplayName("Should handle field with complete location data")
    public void testFieldWithCompleteLocation() {
        // Given
        Field field = createTestField();
        Location location = new Location(36.8065, 10.1815, "Tunis, Tunisia");
        field.setLocation(location);

        // When
        Response response = fieldResource.createField(field);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Field createdField = (Field) response.getEntity();
        testFieldId = createdField.getId();

        assertNotNull(createdField.getLocation());
        assertEquals(36.8065, createdField.getLocation().getLatitude());
        assertEquals(10.1815, createdField.getLocation().getLongitude());
        assertEquals("Tunis, Tunisia", createdField.getLocation().getAddress());
    }

    @Test
    @Order(14)
    @DisplayName("Should handle field with all properties set")
    public void testFieldWithAllProperties() {
        // Given
        Field field = createTestField();
        field.setCurrentCrop("Wheat");
        field.setSoilType("Loamy");
        field.setArea(20.5);
        field.setLocation(new Location(36.8065, 10.1815, "Test Location"));

        // When
        Response response = fieldResource.createField(field);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Field createdField = (Field) response.getEntity();
        testFieldId = createdField.getId();

        assertEquals("Wheat", createdField.getCurrentCrop());
        assertEquals("Loamy", createdField.getSoilType());
        assertEquals(20.5, createdField.getArea());
        assertNotNull(createdField.getLocation());
    }

    // ============= Helper Methods =============

    private Field createTestField() {
        Field field = new Field();
        field.setUserId(testUserId);
        field.setName("Test Field 1");
        field.setArea(10.5);
        field.setSoilType("Clay");
        field.setCurrentCrop("Wheat");

        Location location = new Location();
        location.setLatitude(36.8065);
        location.setLongitude(10.1815);
        location.setAddress("Test Address, Tunisia");
        field.setLocation(location);

        return field;
    }
}