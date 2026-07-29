package com.campusguide.personal.ai.atlas.benchmark;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.model.ProviderMetadata;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.resilience.CircuitBreaker;
import com.campusguide.personal.ai.atlas.resilience.ResilientAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderLayerBenchmarkTest {

    @Mock
    private AIProvider delegateProvider;

    @Mock
    private AtlasMetrics atlasMetrics;

    private ResilientAIProvider resilientAIProvider;

    @BeforeEach
    void setUp() {
        AtlasProperties properties = new AtlasProperties();
        CircuitBreaker circuitBreaker = new CircuitBreaker(properties, atlasMetrics);
        resilientAIProvider = new ResilientAIProvider(delegateProvider, properties, circuitBreaker, atlasMetrics);

        when(delegateProvider.getMetadata()).thenReturn(ProviderMetadata.builder()
                .name("OpenAI")
                .supportedModels(List.of("gpt-4o-mini"))
                .active(true)
                .build());

        when(delegateProvider.sendPrompt(any())).thenReturn(AtlasNormalizedResponse.builder()
                .id("norm-1")
                .content("Normal response")
                .role(AtlasRole.ASSISTANT)
                .providerName("OpenAI")
                .modelUsed("gpt-4o-mini")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Test
    void benchmarkProviderResilienceOverhead() {
        AtlasPrompt prompt = AtlasPrompt.builder()
                .userMessage("Test message")
                .model("gpt-4o-mini")
                .build();

        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            AtlasNormalizedResponse response = resilientAIProvider.sendPrompt(prompt);
            assertNotNull(response);
        }

        long totalTimeNs = System.nanoTime() - startTime;
        double avgTimeMs = (totalTimeNs / 1_000_000.0) / iterations;

        System.out.println("Average Provider Resilience Overhead: " + avgTimeMs + " ms over " + iterations + " iterations.");
        assertTrue(avgTimeMs < 50.0, "Provider resilience layer average overhead should be under 50ms");
    }
}
