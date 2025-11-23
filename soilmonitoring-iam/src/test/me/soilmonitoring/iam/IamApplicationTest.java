package me.soilmonitoring.iam;

import jakarta.ws.rs.core.Application;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IamApplicationTest {

    @Test
    void getClasses() {
        IamApplication app = new IamApplication();
        Set<Class<?>> classes = app.getClasses();
        assertNotNull(classes, "Classes set should not be null");
        assertTrue(classes.isEmpty(), "Classes set should be empty by default");
    }

    @Test
    void getSingletons() {
        IamApplication app = new IamApplication();
        Set<Object> singletons = app.getSingletons();
        assertNotNull(singletons, "Singletons set should not be null");
        assertTrue(singletons.isEmpty(), "Singletons set should be empty by default");
    }

    @Test
    void getProperties() {
        IamApplication app = new IamApplication();
        assertNotNull(app.getProperties(), "Properties map should not be null");
        assertTrue(app.getProperties().isEmpty(), "Properties map should be empty by default");
    }
}