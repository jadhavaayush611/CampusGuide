package com.campusguide.personal.ai.atlas.benchmark;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.mapper.AtlasMapper;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.orchestration.ConversationOrchestrator;
import com.campusguide.personal.ai.atlas.prompt.ContextSectionAssembler;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.validation.AtlasPromptValidator;
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
class OrchestrationBenchmarkTest {

    @Mock
    private AIProvider aiProvider;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private ContextSectionAssembler contextSectionAssembler;

    @Mock
    private AtlasPromptValidator promptValidator;

    @Mock
    private AtlasMapper atlasMapper;

    @Mock
    private ContextEngine contextEngine;

    private ConversationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new ConversationOrchestrator(
                aiProvider,
                promptBuilder,
                contextSectionAssembler,
                promptValidator,
                atlasMapper,
                contextEngine,
                null,
                null
        );

        when(contextEngine.buildContext(any(), any())).thenReturn(AtlasContext.builder().build());
        when(contextSectionAssembler.assembleSections(any())).thenReturn(List.of());
        when(promptBuilder.buildPrompt(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(AtlasPrompt.builder().userMessage("test").build());
        when(aiProvider.sendPrompt(any())).thenReturn(AtlasNormalizedResponse.builder()
                .id("test-id")
                .content("Mock response")
                .role(AtlasRole.ASSISTANT)
                .providerName("MockProvider")
                .modelUsed("gpt-4o-mini")
                .timestamp(LocalDateTime.now())
                .build());
        when(atlasMapper.toResponseDto(any())).thenReturn(AtlasChatResponse.builder()
                .content("Mock response")
                .build());
    }

    @Test
    void benchmarkOrchestrationLatency() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Hello Atlas")
                .conversationId("conv-bench")
                .build();

        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            AtlasChatResponse response = orchestrator.orchestrate(request, "user123");
            assertNotNull(response);
        }

        long totalTimeNs = System.nanoTime() - startTime;
        double avgTimeMs = (totalTimeNs / 1_000_000.0) / iterations;

        System.out.println("Average Orchestration Latency: " + avgTimeMs + " ms over " + iterations + " iterations.");
        assertTrue(avgTimeMs < 100.0, "Orchestration average latency should be under 100ms");
    }
}
