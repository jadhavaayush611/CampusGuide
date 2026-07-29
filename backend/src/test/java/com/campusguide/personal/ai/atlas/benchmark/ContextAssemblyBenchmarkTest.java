package com.campusguide.personal.ai.atlas.benchmark;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextContributor;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextAssemblyBenchmarkTest {

    @Mock
    private ContextContributor mockContributor1;

    @Mock
    private ContextContributor mockContributor2;

    private ContextEngine contextEngine;

    @BeforeEach
    void setUp() {
        when(mockContributor1.getName()).thenReturn("Contributor1");
        when(mockContributor2.getName()).thenReturn("Contributor2");

        contextEngine = new ContextEngine(List.of(mockContributor1, mockContributor2));
    }

    @Test
    void benchmarkContextAssemblyLatency() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("How is my schedule for today?")
                .conversationId("bench-conv-1")
                .build();

        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            AtlasContext context = contextEngine.buildContext(request);
            assertNotNull(context);
        }

        long totalTimeNs = System.nanoTime() - startTime;
        double avgTimeMs = (totalTimeNs / 1_000_000.0) / iterations;

        System.out.println("Average Context Assembly Latency: " + avgTimeMs + " ms over " + iterations + " iterations.");
        assertTrue(avgTimeMs < 100.0, "Context assembly average latency should be under 100ms");
    }
}
