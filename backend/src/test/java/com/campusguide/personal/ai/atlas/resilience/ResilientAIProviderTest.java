package com.campusguide.personal.ai.atlas.resilience;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.exception.AtlasErrorCategory;
import com.campusguide.personal.ai.atlas.exception.AtlasPromptValidationException;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderException;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderUnavailableException;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.model.ProviderMetadata;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientAIProviderTest {

    @Mock
    private AIProvider delegate;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private AtlasMetrics atlasMetrics;

    private AtlasProperties properties;
    private ResilientAIProvider resilientAIProvider;

    @BeforeEach
    void setUp() {
        properties = new AtlasProperties();
        properties.getRetry().setMaxAttempts(2);
        properties.getRetry().setInitialIntervalMs(10);
        resilientAIProvider = new ResilientAIProvider(delegate, properties, circuitBreaker, atlasMetrics);

        lenient().when(delegate.getMetadata()).thenReturn(ProviderMetadata.builder()
                .name("OpenAI")
                .supportedModels(List.of("gpt-4o-mini"))
                .active(true)
                .build());
    }

    @Test
    void testSuccessfulExecution() {
        when(circuitBreaker.allowRequest()).thenReturn(true);
        AtlasPrompt prompt = AtlasPrompt.builder().userMessage("Hi").build();
        AtlasNormalizedResponse response = AtlasNormalizedResponse.builder()
                .id("resp-1")
                .content("Hello")
                .role(AtlasRole.ASSISTANT)
                .providerName("OpenAI")
                .modelUsed("gpt-4o-mini")
                .timestamp(LocalDateTime.now())
                .build();

        when(delegate.sendPrompt(any())).thenReturn(response);

        AtlasNormalizedResponse actual = resilientAIProvider.sendPrompt(prompt);
        assertNotNull(actual);
        assertEquals("Hello", actual.getContent());
        verify(circuitBreaker).recordSuccess();
    }

    @Test
    void testCircuitBreakerOpenFastFails() {
        when(circuitBreaker.allowRequest()).thenReturn(false);
        AtlasPrompt prompt = AtlasPrompt.builder().userMessage("Hi").build();

        AtlasProviderUnavailableException ex = assertThrows(
                AtlasProviderUnavailableException.class,
                () -> resilientAIProvider.sendPrompt(prompt)
        );
        assertEquals(AtlasErrorCategory.CIRCUIT_BREAKER_OPEN, ex.getCategory());
        verify(delegate, never()).sendPrompt(any());
    }

    @Test
    void testNonRetriableValidationExceptionDoesNotRetry() {
        when(circuitBreaker.allowRequest()).thenReturn(true);
        when(delegate.sendPrompt(any())).thenThrow(new AtlasPromptValidationException("Invalid prompt"));

        AtlasPrompt prompt = AtlasPrompt.builder().userMessage("Hi").build();

        assertThrows(AtlasPromptValidationException.class, () -> resilientAIProvider.sendPrompt(prompt));
        verify(delegate, times(1)).sendPrompt(any());
        verify(circuitBreaker).recordFailure();
    }

    @Test
    void testTransientFailureRetriesAndSucceeds() {
        when(circuitBreaker.allowRequest()).thenReturn(true);
        AtlasNormalizedResponse response = AtlasNormalizedResponse.builder()
                .id("resp-2")
                .content("Success on retry")
                .role(AtlasRole.ASSISTANT)
                .providerName("OpenAI")
                .modelUsed("gpt-4o-mini")
                .timestamp(LocalDateTime.now())
                .build();

        when(delegate.sendPrompt(any()))
                .thenThrow(new AtlasProviderUnavailableException("Temporary 503"))
                .thenReturn(response);

        AtlasPrompt prompt = AtlasPrompt.builder().userMessage("Hi").build();
        AtlasNormalizedResponse actual = resilientAIProvider.sendPrompt(prompt);

        assertNotNull(actual);
        assertEquals("Success on retry", actual.getContent());
        verify(delegate, times(2)).sendPrompt(any());
        verify(circuitBreaker).recordSuccess();
    }
}
