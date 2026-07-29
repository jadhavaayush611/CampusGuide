package com.campusguide.personal.ai.atlas.config;

import com.campusguide.personal.ai.atlas.exception.AtlasConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtlasPropertiesTest {

    @Test
    void testValidConfigurationDoesNotThrow() {
        AtlasProperties properties = new AtlasProperties();
        assertDoesNotThrow(properties::validate);
    }

    @Test
    void testInvalidTimeoutThrowsConfigurationException() {
        AtlasProperties properties = new AtlasProperties();
        properties.setTimeoutMs(-1);
        assertThrows(AtlasConfigurationException.class, properties::validate);
    }

    @Test
    void testInvalidRetryAttemptsThrowsConfigurationException() {
        AtlasProperties properties = new AtlasProperties();
        properties.getRetry().setMaxAttempts(0);
        assertThrows(AtlasConfigurationException.class, properties::validate);
    }

    @Test
    void testInvalidCircuitBreakerThresholdThrowsConfigurationException() {
        AtlasProperties properties = new AtlasProperties();
        properties.getCircuitBreaker().setFailureThreshold(0);
        assertThrows(AtlasConfigurationException.class, properties::validate);
    }
}
