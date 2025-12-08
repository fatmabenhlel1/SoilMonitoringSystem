package me.soilmonitoring.api;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiApplicationTest {



    @Test
    void testLoggerDisposer() {
        ApiApplication.CDIConfigurator configurator = new ApiApplication.CDIConfigurator();
        Logger logger = mock(Logger.class);

        configurator.disposeLogger(logger);

        verify(logger).info("logger disposed!");
    }
}
