package com.campusguide.personal.ai.atlas.health;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.resilience.CircuitBreaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtlasHealthIndicatorTest {

    @Mock
    private AIProvider aiProvider;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private ContextEngine contextEngine;

    @Mock
    private CircuitBreaker circuitBreaker;

    private AtlasProperties atlasProperties;
    private AtlasHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        atlasProperties = new AtlasProperties();
        healthIndicator = new AtlasHealthIndicator(atlasProperties, aiProvider, promptBuilder, contextEngine, circuitBreaker);
    }

    @Test
    void testHealthUpWhenAllReady() {
        when(aiProvider.isAvailable()).thenReturn(true);
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);

        Health health = healthIndicator.health();
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void testHealthOutOfServiceWhenDisabled() {
        atlasProperties.setEnabled(false);

        Health health = healthIndicator.health();
        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
    }

    @Test
    void testHealthDownWhenCircuitBreakerOpen() {
        when(aiProvider.isAvailable()).thenReturn(true);
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);

        Health health = healthIndicator.health();
        assertEquals(Status.DOWN, health.getStatus());
    }
}
