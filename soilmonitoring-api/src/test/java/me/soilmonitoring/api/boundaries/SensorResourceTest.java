package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.AlertRepository;
import me.soilmonitoring.api.entities.Alert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jboss.arquillian.junit5.ArquillianExtension;



import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;


@ExtendWith(ArquillianExtension.class)
public class SensorResourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(SensorResource.class.getPackage())
                .addPackage(SoilMonitoringManager.class.getPackage())
                .addPackage(SensorRepository.class.getPackage())
                .addPackage(Sensor.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private SensorResource sensorResource;

    @Inject
    private SensorRepository sensorRepository;

    @Test
    public void testCreateSensor() {
        Sensor sensor = new Sensor();
        sensor.setFieldId("field001");
        sensor.setDeviceId("dev-001");
        sensor.setType("NPK");

        Response response = sensorResource.createSensor(sensor);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Sensor created = (Sensor) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("active", created.getStatus());
        assertEquals("field001", created.getFieldId());
    }

    @Test
    public void testGetSensorById() {
        Sensor sensor = new Sensor();
        sensor.setId(UUID.randomUUID().toString());
        sensor.setFieldId("field002");
        sensor.setDeviceId("dev-002");
        sensor.setType("Temperature");
        sensor.setInstalledAt(LocalDateTime.now());
        sensor.setStatus("active");
        sensorRepository.save(sensor);

        Response response = sensorResource.getSensorById(sensor.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Sensor found = (Sensor) response.getEntity();
        assertEquals(sensor.getId(), found.getId());
    }

    @Test
    public void testGetSensorByDeviceId() {
        Sensor sensor = new Sensor();
        sensor.setId(UUID.randomUUID().toString());
        sensor.setFieldId("field003");
        sensor.setDeviceId("dev-003");
        sensor.setType("Humidity");
        sensor.setInstalledAt(LocalDateTime.now());
        sensor.setStatus("active");
        sensorRepository.save(sensor);

        Response response = sensorResource.getSensorByDeviceId("dev-003");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Sensor found = (Sensor) response.getEntity();
        assertEquals("dev-003", found.getDeviceId());
    }

    @Test
    public void testUpdateSensorStatus() {
        Sensor sensor = new Sensor();
        sensor.setId(UUID.randomUUID().toString());
        sensor.setFieldId("field004");
        sensor.setDeviceId("dev-004");
        sensor.setType("Temperature");
        sensor.setInstalledAt(LocalDateTime.now());
        sensor.setStatus("inactive");
        sensorRepository.save(sensor);

        Response response = sensorResource.updateSensorStatus(sensor.getId(), "active");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Sensor updated = (Sensor) response.getEntity();
        assertEquals("active", updated.getStatus());
        assertNotNull(updated.getLastConnection());
    }

    @Test
    public void testDeleteSensor() {
        Sensor sensor = new Sensor();
        sensor.setId(UUID.randomUUID().toString());
        sensor.setFieldId("field005");
        sensor.setDeviceId("dev-005");
        sensor.setType("NPK");
        sensor.setInstalledAt(LocalDateTime.now());
        sensor.setStatus("active");
        sensorRepository.save(sensor);

        Response response = sensorResource.deleteSensor(sensor.getId());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        assertFalse(sensorRepository.findById(sensor.getId()).isPresent());
    }

    @Test
    public void testGetFieldSensors() {
        Sensor sensor = new Sensor();
        sensor.setId(UUID.randomUUID().toString());
        sensor.setFieldId("field006");
        sensor.setDeviceId("dev-006");
        sensor.setType("Humidity");
        sensor.setInstalledAt(LocalDateTime.now());
        sensor.setStatus("active");
        sensorRepository.save(sensor);

        Response response = sensorResource.getFieldSensors("field006");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        List<Sensor> sensors = (List<Sensor>) response.getEntity();
        assertFalse(sensors.isEmpty());
    }
}
