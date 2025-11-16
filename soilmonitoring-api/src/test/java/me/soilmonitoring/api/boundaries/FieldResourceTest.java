package me.soilmonitoring.api.boundaries;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import me.soilmonitoring.api.controllers.managers.SoilMonitoringManager;
import me.soilmonitoring.api.controllers.repositories.FieldRepository;
import me.soilmonitoring.api.entities.Field;
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
public class FieldResourceTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackage(FieldResource.class.getPackage())
                .addPackage(SoilMonitoringManager.class.getPackage())
                .addPackage(FieldRepository.class.getPackage())
                .addPackage(Field.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private FieldResource fieldResource;

    @Inject
    private FieldRepository fieldRepository;

    @Test
    public void testCreateField() {
        Field field = new Field();
        field.setUserId("user123");
        field.setName("North Plot");
        field.setLocation("Zone A");
        field.setCreatedAt(LocalDateTime.now());

        Response response = fieldResource.createField(field);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Field created = (Field) response.getEntity();
        assertNotNull(created.getId());
        assertEquals("user123", created.getUserId());
    }

    @Test
    public void testGetFieldById() {
        Field field = new Field();
        field.setId(UUID.randomUUID().toString());
        field.setUserId("user456");
        field.setName("South Plot");
        field.setLocation("Zone B");
        field.setCreatedAt(LocalDateTime.now());
        fieldRepository.save(field);

        Response response = fieldResource.getFieldById(field.getId());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Field found = (Field) response.getEntity();
        assertEquals(field.getId(), found.getId());
    }

    @Test
    public void testUpdateField() {
        Field field = new Field();
        field.setId(UUID.randomUUID().toString());
        field.setUserId("user789");
        field.setName("East Plot");
        field.setLocation("Zone C");
        field.setCreatedAt(LocalDateTime.now());
        fieldRepository.save(field);

        field.setName("Updated East Plot");
        field.setLocation("Zone D");

        Response response = fieldResource.updateField(field.getId(), field);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Field updated = (Field) response.getEntity();
        assertEquals("Updated East Plot", updated.getName());
        assertEquals("Zone D", updated.getLocation());
    }

    @Test
    public void testDeleteField() {
        Field field = new Field();
        field.setId(UUID.randomUUID().toString());
        field.setUserId("user999");
        field.setName("West Plot");
        field.setLocation("Zone E");
        field.setCreatedAt(LocalDateTime.now());
        fieldRepository.save(field);

        Response response = fieldResource.deleteField(field.getId());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

        assertFalse(fieldRepository.findById(field.getId()).isPresent());
    }

    @Test
    public void testGetUserFields() {
        Field field = new Field();
        field.setId(UUID.randomUUID().toString());
        field.setUserId("user321");
        field.setName("Central Plot");
        field.setLocation("Zone F");
        field.setCreatedAt(LocalDateTime.now());
        fieldRepository.save(field);

        Response response = fieldResource.getUserFields("user321");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        List<Field> fields = (List<Field>) response.getEntity();
        assertFalse(fields.isEmpty());
    }
}
