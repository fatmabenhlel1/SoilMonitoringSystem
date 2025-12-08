package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.FieldRepository;
import me.soilmonitoring.api.entities.Field;
import me.soilmonitoring.api.entities.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FieldResource Tests")
class FieldResourceTest {

    @Mock
    private Logger logger;

    @Mock
    private SoilMonitoringManager manager;

    @Mock
    private FieldRepository fieldRepository;

    @InjectMocks
    private FieldResource fieldResource;

    private Field testField;
    private List<Field> testFields;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new Location(36.8065, 10.1815, "Tunis, Tunisia");

        testField = new Field();
        testField.setId("field-001");
        testField.setUserId("user-001");
        testField.setName("North Field");
        testField.setLocation(testLocation);
        testField.setArea(5.5);
        testField.setCurrentCrop("Wheat");
        testField.setSoilType("Clay");
        testField.setCreatedAt(LocalDateTime.now());

        Field field2 = new Field();
        field2.setId("field-002");
        field2.setUserId("user-001");
        field2.setName("South Field");
        field2.setLocation(new Location(36.7, 10.2, "Ariana, Tunisia"));
        field2.setArea(3.2);
        field2.setCurrentCrop("Corn");
        field2.setSoilType("Sandy");
        field2.setCreatedAt(LocalDateTime.now());

        testFields = Arrays.asList(testField, field2);
    }

    // ===== Tests pour getUserFields =====

    @Test
    @DisplayName("Should get all fields for a user successfully")
    void testGetUserFieldsSuccess() {
        // Given
        String userId = "user-001";
        when(manager.getUserFields(userId)).thenReturn(testFields);

        // When
        Response response = fieldResource.getUserFields(userId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testFields, response.getEntity());
        verify(manager, times(1)).getUserFields(userId);
    }

    @Test
    @DisplayName("Should return empty list when user has no fields")
    void testGetUserFieldsEmptyList() {
        // Given
        String userId = "user-999";
        when(manager.getUserFields(userId)).thenReturn(Arrays.asList());

        // When
        Response response = fieldResource.getUserFields(userId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<?> fields = (List<?>) response.getEntity();
        assertTrue(fields.isEmpty());
        verify(manager, times(1)).getUserFields(userId);
    }

    @Test
    @DisplayName("Should handle exception when getting user fields")
    void testGetUserFieldsException() {
        // Given
        String userId = "user-001";
        when(manager.getUserFields(userId)).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = fieldResource.getUserFields(userId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error retrieving fields", response.getEntity());
        verify(logger, times(1)).severe(contains("Error getting user fields"));
    }

    // ===== Tests pour getFieldById =====

    @Test
    @DisplayName("Should get field by id successfully")
    void testGetFieldByIdSuccess() {
        // Given
        String fieldId = "field-001";
        when(manager.findFieldById(fieldId)).thenReturn(testField);

        // When
        Response response = fieldResource.getFieldById(fieldId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        Field returnedField = (Field) response.getEntity();
        assertEquals(testField.getId(), returnedField.getId());
        assertEquals(testField.getName(), returnedField.getName());
        verify(manager, times(1)).findFieldById(fieldId);
    }

    @Test
    @DisplayName("Should return 404 when field not found")
    void testGetFieldByIdNotFound() {
        // Given
        String fieldId = "non-existent";
        when(manager.findFieldById(fieldId)).thenThrow(new IllegalArgumentException("Field not found"));

        // When
        Response response = fieldResource.getFieldById(fieldId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Field not found", response.getEntity());
        verify(manager, times(1)).findFieldById(fieldId);
    }

    @Test
    @DisplayName("Should handle exception when getting field by id")
    void testGetFieldByIdException() {
        // Given
        String fieldId = "field-001";
        when(manager.findFieldById(fieldId)).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = fieldResource.getFieldById(fieldId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error retrieving field", response.getEntity());
        verify(logger, times(1)).severe(contains("Error getting field"));
    }

    // ===== Tests pour createField =====

    @Test
    @DisplayName("Should create field successfully")
    void testCreateFieldSuccess() {
        // Given
        Field newField = new Field();
        newField.setUserId("user-001");
        newField.setName("New Field");
        newField.setLocation(testLocation);
        newField.setArea(4.0);
        newField.setCurrentCrop("Rice");
        newField.setSoilType("Loamy");

        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = fieldResource.createField(newField);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        Field savedField = (Field) response.getEntity();

        assertNotNull(savedField.getId());
        assertNotNull(savedField.getCreatedAt());
        assertEquals("user-001", savedField.getUserId());
        assertEquals("New Field", savedField.getName());
        assertEquals(4.0, savedField.getArea());

        verify(fieldRepository, times(1)).save(any(Field.class));
        verify(logger, times(1)).info(contains("Field created"));
    }

    @Test
    @DisplayName("Should set default values when creating field")
    void testCreateFieldDefaultValues() {
        // Given
        Field newField = new Field();
        newField.setUserId("user-001");
        newField.setName("Test Field");

        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = fieldResource.createField(newField);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Field savedField = (Field) response.getEntity();

        assertNotNull(savedField.getId(), "ID should be generated");
        assertNotNull(savedField.getCreatedAt(), "CreatedAt should be set");
    }

    @Test
    @DisplayName("Should handle exception when creating field")
    void testCreateFieldException() {
        // Given
        Field newField = new Field();
        newField.setUserId("user-001");
        newField.setName("Test Field");

        when(fieldRepository.save(any(Field.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = fieldResource.createField(newField);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error creating field", response.getEntity());
        verify(logger, times(1)).severe(contains("Error creating field"));
    }

    @Test
    @DisplayName("Should verify UUID format for generated field ID")
    void testFieldIdUUIDFormat() {
        // Given
        Field newField = new Field();
        newField.setUserId("user-001");
        newField.setName("Test");

        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = fieldResource.createField(newField);

        // Then
        Field savedField = (Field) response.getEntity();
        assertNotNull(savedField.getId());
        assertTrue(savedField.getId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "ID should be a valid UUID format");
    }

    // ===== Tests pour updateField =====

    @Test
    @DisplayName("Should update field successfully")
    void testUpdateFieldSuccess() {
        // Given
        String fieldId = "field-001";
        Field updatedField = new Field();
        updatedField.setUserId("user-001");
        updatedField.setName("Updated Field Name");
        updatedField.setLocation(testLocation);
        updatedField.setArea(6.0);
        updatedField.setCurrentCrop("Barley");
        updatedField.setSoilType("Clay");

        when(manager.findFieldById(fieldId)).thenReturn(testField);
        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = fieldResource.updateField(fieldId, updatedField);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Field returnedField = (Field) response.getEntity();

        assertEquals(fieldId, returnedField.getId());
        assertEquals("Updated Field Name", returnedField.getName());
        assertEquals(6.0, returnedField.getArea());
        assertEquals(testField.getCreatedAt(), returnedField.getCreatedAt());

        verify(manager, times(1)).findFieldById(fieldId);
        verify(fieldRepository, times(1)).save(any(Field.class));
        verify(logger, times(1)).info(contains("Field updated"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent field")
    void testUpdateFieldNotFound() {
        // Given
        String fieldId = "non-existent";
        Field updatedField = new Field();
        updatedField.setName("Test");

        when(manager.findFieldById(fieldId)).thenThrow(new IllegalArgumentException("Field not found"));

        // When
        Response response = fieldResource.updateField(fieldId, updatedField);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Field not found", response.getEntity());
        verify(manager, times(1)).findFieldById(fieldId);
        verify(fieldRepository, never()).save(any(Field.class));
    }

    @Test
    @DisplayName("Should handle exception when updating field")
    void testUpdateFieldException() {
        // Given
        String fieldId = "field-001";
        Field updatedField = new Field();
        updatedField.setName("Test");

        when(manager.findFieldById(fieldId)).thenReturn(testField);
        when(fieldRepository.save(any(Field.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = fieldResource.updateField(fieldId, updatedField);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error updating field", response.getEntity());
        verify(logger, times(1)).severe(contains("Error updating field"));
    }

    @Test
    @DisplayName("Should preserve createdAt when updating field")
    void testUpdateFieldPreservesCreatedAt() {
        // Given
        String fieldId = "field-001";
        LocalDateTime originalCreatedAt = testField.getCreatedAt();

        Field updatedField = new Field();
        updatedField.setName("Updated Name");

        when(manager.findFieldById(fieldId)).thenReturn(testField);
        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = fieldResource.updateField(fieldId, updatedField);

        // Then
        Field returnedField = (Field) response.getEntity();
        assertEquals(originalCreatedAt, returnedField.getCreatedAt());
    }

    // ===== Tests pour deleteField =====

    @Test
    @DisplayName("Should delete field successfully")
    void testDeleteFieldSuccess() {
        // Given
        String fieldId = "field-001";
        doNothing().when(fieldRepository).deleteById(fieldId);

        // When
        Response response = fieldResource.deleteField(fieldId);

        // Then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(fieldRepository, times(1)).deleteById(fieldId);
        verify(logger, times(1)).info(contains("Field deleted"));
    }

    @Test
    @DisplayName("Should handle exception when deleting field")
    void testDeleteFieldException() {
        // Given
        String fieldId = "field-001";
        doThrow(new RuntimeException("Database error")).when(fieldRepository).deleteById(fieldId);

        // When
        Response response = fieldResource.deleteField(fieldId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error deleting field", response.getEntity());
        verify(logger, times(1)).severe(contains("Error deleting field"));
    }

    // ===== Tests d'intégration et cas limites =====

    @Test
    @DisplayName("Should handle null field in createField")
    void testCreateFieldWithNull() {
        // When
        Response response = fieldResource.createField(null);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(logger, times(1)).severe(contains("Error creating field"));
    }

    @Test
    @DisplayName("Should verify field properties are set correctly on creation")
    void testCreateFieldPropertiesVerification() {
        // Given
        Field newField = new Field();
        newField.setUserId("user-123");
        newField.setName("Premium Field");
        newField.setLocation(testLocation);
        newField.setArea(10.5);
        newField.setCurrentCrop("Tomatoes");
        newField.setSoilType("Loamy");

        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> {
            Field field = invocation.getArgument(0);
            assertNotNull(field.getId(), "ID should be set");
            assertNotNull(field.getCreatedAt(), "CreatedAt should be set");
            assertEquals("user-123", field.getUserId());
            assertEquals("Premium Field", field.getName());
            return field;
        });

        // When
        Response response = fieldResource.createField(newField);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(fieldRepository, times(1)).save(any(Field.class));
    }

    @Test
    @DisplayName("Should handle multiple fields for same user")
    void testGetMultipleFieldsForUser() {
        // Given
        String userId = "user-001";
        when(manager.getUserFields(userId)).thenReturn(testFields);

        // When
        Response response = fieldResource.getUserFields(userId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Field> returnedFields = (List<Field>) response.getEntity();
        assertEquals(2, returnedFields.size());
    }

    @Test
    @DisplayName("Should update field with complete information")
    void testUpdateFieldComplete() {
        // Given
        String fieldId = "field-001";
        Field updatedField = new Field();
        updatedField.setUserId("user-001");
        updatedField.setName("Complete Update");
        updatedField.setLocation(new Location(36.9, 10.3, "New Location"));
        updatedField.setArea(7.5);
        updatedField.setCurrentCrop("Potatoes");
        updatedField.setSoilType("Sandy Loam");

        when(manager.findFieldById(fieldId)).thenReturn(testField);
        when(fieldRepository.save(any(Field.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = fieldResource.updateField(fieldId, updatedField);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Field returnedField = (Field) response.getEntity();

        assertEquals(fieldId, returnedField.getId());
        assertEquals("Complete Update", returnedField.getName());
        assertEquals(7.5, returnedField.getArea());
        assertEquals("Potatoes", returnedField.getCurrentCrop());
        assertEquals("Sandy Loam", returnedField.getSoilType());
        assertNotNull(returnedField.getLocation());
    }
}