package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.entities.Alert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class AlertResourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(AlertResource.class.getPackage())
                .addPackage(SoilMonitoringManager.class.getPackage())
                .addPackage(AlertRepository.class.getPackage())
                .addPackage(Alert.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private AlertResource alertResource;

    @Inject
    private AlertRepository alertRepository;

    @Test
    public void testCreateAlert() {
        Alert alert = new Alert();
        alert.setUserId("user123");
        alert.setMessage("Low moisture detected");
        alert.setType("MOISTURE");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsRead(false);

        Response response = alertResource.createAlert(alert);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Alert created = (Alert) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("user123", created.getUserId());
    }

    @Test
    public void testGetAlertById() {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId("user456");
        alert.setMessage("High temperature");
        alert.setType("TEMPERATURE");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsRead(false);
        alertRepository.save(alert);

        Response response = alertResource.getAlertById(alert.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Alert found = (Alert) response.getEntity();
        assertEquals(alert.getId(), found.getId());
    }

    @Test
    public void testMarkAlertAsRead() {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId("user789");
        alert.setMessage("NPK imbalance");
        alert.setType("NPK");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsRead(false);
        alertRepository.save(alert);

        Response response = alertResource.markAlertAsRead(alert.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Alert updated = (Alert) response.getEntity();
        assertTrue(updated.getIsRead());
    }

    @Test
    public void testDeleteAlert() {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId("user999");
        alert.setMessage("Test delete");
        alert.setType("TEST");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsRead(false);
        alertRepository.save(alert);

        Response response = alertResource.deleteAlert(alert.getId());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        assertFalse(alertRepository.findById(alert.getId()).isPresent());
    }

    @Test
    public void testGetUserAlerts() {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setUserId("user321");
        alert.setMessage("Humidity warning");
        alert.setType("MOISTURE");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setIsRead(false);
        alertRepository.save(alert);

        Response response = alertResource.getUserAlerts("user321", null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        List<Alert> alerts = (List<Alert>) response.getEntity();
        assertFalse(alerts.isEmpty());
    }
}
