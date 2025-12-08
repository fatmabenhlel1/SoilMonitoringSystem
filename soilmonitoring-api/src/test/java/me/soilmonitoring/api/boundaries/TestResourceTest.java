package me.soilmonitoring.api.boundaries;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestResourceTest {

    private TestResource resource;

    @BeforeEach
    void setUp() {
        resource = new TestResource();
    }

    @Test
    void testTestEndpoint() {
        Response response = resource.test();

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String entity = (String) response.getEntity();
        assertNotNull(entity);
        assertTrue(entity.contains("\"status\":\"API is working!\""));
        assertTrue(entity.contains("\"message\":\"Soil Monitoring API is running\""));
    }

    @Test
    void testHealthEndpoint() {
        Response response = resource.health();

        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String entity = (String) response.getEntity();
        assertNotNull(entity);
        assertTrue(entity.contains("\"status\":\"healthy\""));
    }
}
