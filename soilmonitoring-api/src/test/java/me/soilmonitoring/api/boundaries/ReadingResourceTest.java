package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.SensorReadingRepository;
import me.soilmonitoring.api.entities.SensorReading;
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
public class ReadingResourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(ReadingResource.class.getPackage())
                .addPackage(SoilMonitoringManager.class.getPackage())
                .addPackage(SensorReadingRepository.class.getPackage())
                .addPackage(SensorReading.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private ReadingResource readingResource;

    @Inject
    private SensorReadingRepository readingRepository;

    @Test
    public void testCreateReading() {
        SensorReading reading = new SensorReading();
        reading.setFieldId("field001");
        reading.setHumidity(35.0);
        reading.setTemperature(22.5);
        reading.setNpk("medium");

        Response response = readingResource.createReading(reading);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        SensorReading created = (SensorReading) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("field001", created.getFieldId());
    }

    @Test
    public void testGetReadingById() {
        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setFieldId("field002");
        reading.setHumidity(40.0);
        reading.setTemperature(25.0);
        reading.setNpk("high");
        reading.setTimestamp(LocalDateTime.now());
        readingRepository.save(reading);

        Response response = readingResource.getReadingById(reading.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        SensorReading found = (SensorReading) response.getEntity();
        assertEquals(reading.getId(), found.getId());
    }

    @Test
    public void testGetFieldReadings() {
        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setFieldId("field003");
        reading.setHumidity(28.0);
        reading.setTemperature(20.0);
        reading.setNpk("low");
        reading.setTimestamp(LocalDateTime.now());
        readingRepository.save(reading);

        Response response = readingResource.getFieldReadings("field003");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        List<SensorReading> readings = (List<SensorReading>) response.getEntity();
        assertFalse(readings.isEmpty());
    }

    @Test
    public void testGetFieldReadingsByTimeRange() {
        SensorReading reading = new SensorReading();
        reading.setId(UUID.randomUUID().toString());
        reading.setFieldId("field004");
        reading.setHumidity(50.0);
        reading.setTemperature(18.0);
        reading.setNpk("low");
        reading.setTimestamp(LocalDateTime.now().minusHours(1));
        readingRepository.save(reading);

        String from = LocalDateTime.now().minusHours(2).toString();
        String to = LocalDateTime.now().toString();

        Response response = readingResource.getFieldReadingsByTimeRange("field004", from, to);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        List<SensorReading> readings = (List<SensorReading>) response.getEntity();
        assertFalse(readings.isEmpty());
    }

    @Test
    public void testGetLatestReading() {
        SensorReading reading1 = new SensorReading();
        reading1.setId(UUID.randomUUID().toString());
        reading1.setFieldId("field005");
        reading1.setHumidity(30.0);
        reading1.setTemperature(21.0);
        reading1.setNpk("medium");
        reading1.setTimestamp(LocalDateTime.now().minusMinutes(10));
        readingRepository.save(reading1);

        SensorReading reading2 = new SensorReading();
        reading2.setId(UUID.randomUUID().toString());
        reading2.setFieldId("field005");
        reading2.setHumidity(32.0);
        reading2.setTemperature(22.0);
        reading2.setNpk("medium");
        reading2.setTimestamp(LocalDateTime.now());
        readingRepository.save(reading2);

        Response response = readingResource.getLatestReading("field005");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        SensorReading latest = (SensorReading) response.getEntity();
        assertEquals(reading2.getId(), latest.getId());
    }
}
