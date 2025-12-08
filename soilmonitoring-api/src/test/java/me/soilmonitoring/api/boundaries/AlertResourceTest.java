package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.entities.Alert;
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
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertResource Tests")
class AlertResourceTest {

    @Mock
    private Logger logger;

    @Mock
    private SoilMonitoringManager manager;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private me.soilmonitoring.api.boundaries.AlertResource alertResource;

    private Alert testAlert;
    private List<Alert> testAlerts;

    @BeforeEach
    void setUp() {
        testAlert = new Alert();
        testAlert.setId("alert-001");
        testAlert.setUserId("user-001");
        testAlert.setFieldId("field-001");
        testAlert.setAlertType("temperature");
        testAlert.setSeverity("high");
        testAlert.setMessage("Temperature too high");
        testAlert.setIsRead(false);
        testAlert.setCreatedAt(LocalDateTime.now());

        Alert alert2 = new Alert();
        alert2.setId("alert-002");
        alert2.setUserId("user-001");
        alert2.setFieldId("field-002");
        alert2.setAlertType("humidity");
        alert2.setSeverity("medium");
        alert2.setMessage("Humidity level warning");
        alert2.setIsRead(true);
        alert2.setCreatedAt(LocalDateTime.now());

        testAlerts = Arrays.asList(testAlert, alert2);
    }

    // ===== Tests pour getUserAlerts =====

    @Test
    @DisplayName("Should get all user alerts when unreadOnly is null")
    void testGetUserAlertsAll() {
        // Given
        String userId = "user-001";
        when(alertRepository.findByUserId(userId)).thenReturn(testAlerts);

        // When
        Response response = alertResource.getUserAlerts(userId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testAlerts, response.getEntity());
        verify(alertRepository, times(1)).findByUserId(userId);
        verify(manager, never()).getUnreadAlerts(anyString());
    }

    @Test
    @DisplayName("Should get all user alerts when unreadOnly is false")
    void testGetUserAlertsAllWhenUnreadOnlyFalse() {
        // Given
        String userId = "user-001";
        when(alertRepository.findByUserId(userId)).thenReturn(testAlerts);

        // When
        Response response = alertResource.getUserAlerts(userId, false);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(testAlerts, response.getEntity());
        verify(alertRepository, times(1)).findByUserId(userId);
        verify(manager, never()).getUnreadAlerts(anyString());
    }

    @Test
    @DisplayName("Should get only unread alerts when unreadOnly is true")
    void testGetUserAlertsUnreadOnly() {
        // Given
        String userId = "user-001";
        List<Alert> unreadAlerts = Arrays.asList(testAlert);
        when(manager.getUnreadAlerts(userId)).thenReturn(unreadAlerts);

        // When
        Response response = alertResource.getUserAlerts(userId, true);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(unreadAlerts, response.getEntity());
        verify(manager, times(1)).getUnreadAlerts(userId);
        verify(alertRepository, never()).findByUserId(anyString());
    }

    @Test
    @DisplayName("Should return empty list when user has no alerts")
    void testGetUserAlertsEmptyList() {
        // Given
        String userId = "user-999";
        when(alertRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // When
        Response response = alertResource.getUserAlerts(userId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<?> alerts = (List<?>) response.getEntity();
        assertTrue(alerts.isEmpty());
        verify(alertRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should handle exception when getting user alerts")
    void testGetUserAlertsException() {
        // Given
        String userId = "user-001";
        when(alertRepository.findByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = alertResource.getUserAlerts(userId, null);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error retrieving alerts", response.getEntity());
        verify(logger, times(1)).severe(contains("Error getting user alerts"));
    }

    // ===== Tests pour getAlertById =====

    @Test
    @DisplayName("Should get alert by id successfully")
    void testGetAlertByIdSuccess() {
        // Given
        String alertId = "alert-001";
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(testAlert));

        // When
        Response response = alertResource.getAlertById(alertId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        Alert returnedAlert = (Alert) response.getEntity();
        assertEquals(testAlert.getId(), returnedAlert.getId());
        assertEquals(testAlert.getUserId(), returnedAlert.getUserId());
        verify(alertRepository, times(1)).findById(alertId);
    }

    @Test
    @DisplayName("Should return 404 when alert not found")
    void testGetAlertByIdNotFound() {
        // Given
        String alertId = "non-existent";
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        // When
        Response response = alertResource.getAlertById(alertId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Alert not found", response.getEntity());
        verify(alertRepository, times(1)).findById(alertId);
    }

    @Test
    @DisplayName("Should handle exception when getting alert by id")
    void testGetAlertByIdException() {
        // Given
        String alertId = "alert-001";
        when(alertRepository.findById(alertId)).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = alertResource.getAlertById(alertId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error retrieving alert", response.getEntity());
        verify(logger, times(1)).severe(contains("Error getting alert"));
    }

    // ===== Tests pour createAlert =====

    @Test
    @DisplayName("Should create alert successfully")
    void testCreateAlertSuccess() {
        // Given
        Alert newAlert = new Alert();
        newAlert.setUserId("user-001");
        newAlert.setFieldId("field-001");
        newAlert.setAlertType("temperature");
        newAlert.setSeverity("high");
        newAlert.setMessage("Temperature warning");

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert alert = invocation.getArgument(0);
            return alert;
        });

        // When
        Response response = alertResource.createAlert(newAlert);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        Alert savedAlert = (Alert) response.getEntity();

        assertNotNull(savedAlert.getId());
        assertNotNull(savedAlert.getCreatedAt());
        assertFalse(savedAlert.getIsRead());
        assertEquals("user-001", savedAlert.getUserId());
        assertEquals("field-001", savedAlert.getFieldId());
        assertEquals("temperature", savedAlert.getAlertType());

        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(logger, times(1)).info(contains("Alert created"));
    }

    @Test
    @DisplayName("Should set default values when creating alert")
    void testCreateAlertDefaultValues() {
        // Given
        Alert newAlert = new Alert();
        newAlert.setUserId("user-001");
        newAlert.setMessage("Test alert");

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = alertResource.createAlert(newAlert);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Alert savedAlert = (Alert) response.getEntity();

        assertNotNull(savedAlert.getId(), "ID should be generated");
        assertNotNull(savedAlert.getCreatedAt(), "CreatedAt should be set");
        assertFalse(savedAlert.getIsRead(), "IsRead should be false by default");
    }

    @Test
    @DisplayName("Should handle exception when creating alert")
    void testCreateAlertException() {
        // Given
        Alert newAlert = new Alert();
        newAlert.setUserId("user-001");
        newAlert.setMessage("Test alert");

        when(alertRepository.save(any(Alert.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = alertResource.createAlert(newAlert);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error creating alert", response.getEntity());
        verify(logger, times(1)).severe(contains("Error creating alert"));
    }

    // ===== Tests pour markAlertAsRead =====

    @Test
    @DisplayName("Should mark alert as read successfully")
    void testMarkAlertAsReadSuccess() {
        // Given
        String alertId = "alert-001";
        testAlert.setIsRead(false);

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = alertResource.markAlertAsRead(alertId);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Alert updatedAlert = (Alert) response.getEntity();
        assertTrue(updatedAlert.getIsRead());

        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, times(1)).save(testAlert);
        verify(logger, times(1)).info(contains("Alert marked as read"));
    }

    @Test
    @DisplayName("Should return 404 when marking non-existent alert as read")
    void testMarkAlertAsReadNotFound() {
        // Given
        String alertId = "non-existent";
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        // When
        Response response = alertResource.markAlertAsRead(alertId);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Alert not found", response.getEntity());
        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should handle exception when marking alert as read")
    void testMarkAlertAsReadException() {
        // Given
        String alertId = "alert-001";
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenThrow(new RuntimeException("Database error"));

        // When
        Response response = alertResource.markAlertAsRead(alertId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error updating alert", response.getEntity());
        verify(logger, times(1)).severe(contains("Error marking alert as read"));
    }

    // ===== Tests pour deleteAlert =====

    @Test
    @DisplayName("Should delete alert successfully")
    void testDeleteAlertSuccess() {
        // Given
        String alertId = "alert-001";
        doNothing().when(alertRepository).deleteById(alertId);

        // When
        Response response = alertResource.deleteAlert(alertId);

        // Then
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(alertRepository, times(1)).deleteById(alertId);
        verify(logger, times(1)).info(contains("Alert deleted"));
    }

    @Test
    @DisplayName("Should handle exception when deleting alert")
    void testDeleteAlertException() {
        // Given
        String alertId = "alert-001";
        doThrow(new RuntimeException("Database error")).when(alertRepository).deleteById(alertId);

        // When
        Response response = alertResource.deleteAlert(alertId);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error deleting alert", response.getEntity());
        verify(logger, times(1)).severe(contains("Error deleting alert"));
    }

    // ===== Tests d'intégration et cas limites =====

    @Test
    @DisplayName("Should handle null alert in createAlert")
    void testCreateAlertWithNull() {
        // When
        Response response = alertResource.createAlert(null);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(logger, times(1)).severe(contains("Error creating alert"));
    }

    @Test
    @DisplayName("Should verify alert properties are set correctly on creation")
    void testCreateAlertPropertiesVerification() {
        // Given
        Alert newAlert = new Alert();
        newAlert.setUserId("user-123");
        newAlert.setFieldId("field-456");
        newAlert.setAlertType("moisture");
        newAlert.setSeverity("low");
        newAlert.setMessage("Low soil moisture detected");

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert alert = invocation.getArgument(0);
            assertNotNull(alert.getId(), "ID should be set");
            assertNotNull(alert.getCreatedAt(), "CreatedAt should be set");
            assertFalse(alert.getIsRead(), "IsRead should be false");
            assertEquals("user-123", alert.getUserId());
            assertEquals("field-456", alert.getFieldId());
            return alert;
        });

        // When
        Response response = alertResource.createAlert(newAlert);

        // Then
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should handle multiple alerts for same user")
    void testGetMultipleAlertsForUser() {
        // Given
        String userId = "user-001";
        List<Alert> multipleAlerts = Arrays.asList(testAlert, testAlerts.get(1));
        when(alertRepository.findByUserId(userId)).thenReturn(multipleAlerts);

        // When
        Response response = alertResource.getUserAlerts(userId, null);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Alert> returnedAlerts = (List<Alert>) response.getEntity();
        assertEquals(2, returnedAlerts.size());
    }

    @Test
    @DisplayName("Should verify UUID format for generated alert ID")
    void testAlertIdUUIDFormat() {
        // Given
        Alert newAlert = new Alert();
        newAlert.setUserId("user-001");
        newAlert.setMessage("Test");

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Response response = alertResource.createAlert(newAlert);

        // Then
        Alert savedAlert = (Alert) response.getEntity();
        assertNotNull(savedAlert.getId());
        assertTrue(savedAlert.getId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                "ID should be a valid UUID format");
    }
}