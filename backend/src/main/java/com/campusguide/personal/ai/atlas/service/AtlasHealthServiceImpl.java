package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.context.optimization.ContextCache;
import com.campusguide.personal.ai.atlas.dto.AtlasHealthResponse;
import com.campusguide.personal.ai.atlas.dto.SubsystemHealthDto;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowRuntime;
import com.campusguide.personal.ai.atlas.knowledge.catalog.KnowledgeCatalog;
import com.campusguide.personal.ai.atlas.knowledge.vector.VectorStore;
import com.campusguide.personal.ai.atlas.orchestration.ConversationOrchestrator;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.atlas.resilience.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtlasHealthServiceImpl implements AtlasHealthService {

    @Autowired(required = false)
    private final AtlasProperties atlasProperties;
    @Autowired(required = false)
    private final WorkflowRuntime workflowRuntime;
    @Autowired(required = false)
    private final ConversationOrchestrator conversationOrchestrator;
    @Autowired(required = false)
    private final KnowledgeCatalog knowledgeCatalog;
    @Autowired(required = false)
    private final VectorStore vectorStore;
    @Autowired(required = false)
    private final AIProvider aiProvider;
    @Autowired(required = false)
    private final CircuitBreaker circuitBreaker;
    @Autowired(required = false)
    private final ConversationRepository conversationRepository;
    @Autowired(required = false)
    private final ContextCache contextCache;

    @Override
    public AtlasHealthResponse getHealth() {
        Map<String, SubsystemHealthDto> components = new HashMap<>();

        // 1. Runtime Subsystem
        boolean runtimeUp = workflowRuntime != null;
        components.put("runtime", SubsystemHealthDto.builder()
                .status(runtimeUp ? "UP" : "DOWN")
                .details(Map.of("available", runtimeUp))
                .build());

        // 2. Orchestrator Subsystem
        boolean orchestratorUp = conversationOrchestrator != null;
        components.put("orchestrator", SubsystemHealthDto.builder()
                .status(orchestratorUp ? "UP" : "DOWN")
                .details(Map.of("available", orchestratorUp))
                .build());

        // 3. Knowledge Subsystem
        boolean knowledgeUp = knowledgeCatalog != null;
        int catalogSize = (knowledgeCatalog != null && knowledgeCatalog.getAllEntries() != null) ? knowledgeCatalog.getAllEntries().size() : 0;
        components.put("knowledge", SubsystemHealthDto.builder()
                .status(knowledgeUp ? "UP" : "DOWN")
                .details(Map.of("available", knowledgeUp, "catalogEntries", catalogSize))
                .build());

        // 4. Memory Subsystem
        boolean memoryUp = contextCache != null;
        components.put("memory", SubsystemHealthDto.builder()
                .status(memoryUp ? "UP" : "DOWN")
                .details(Map.of("available", memoryUp))
                .build());

        // 5. Vector Store Subsystem
        boolean vectorStoreUp = vectorStore != null;
        components.put("vectorStore", SubsystemHealthDto.builder()
                .status(vectorStoreUp ? "UP" : "DOWN")
                .details(Map.of("available", vectorStoreUp))
                .build());

        // 6. LLM Provider Subsystem
        boolean providerUp = aiProvider != null && aiProvider.isAvailable();
        String cbState = circuitBreaker != null ? circuitBreaker.getState().name() : "CLOSED";
        if ("OPEN".equals(cbState)) {
            providerUp = false;
        }
        components.put("llmProvider", SubsystemHealthDto.builder()
                .status(providerUp ? "UP" : ("HALF_OPEN".equals(cbState) ? "DEGRADED" : "DOWN"))
                .details(Map.of(
                        "available", providerUp,
                        "circuitBreakerState", cbState,
                        "providerName", (aiProvider != null && aiProvider.getMetadata() != null) ? aiProvider.getMetadata().getName() : "N/A"
                ))
                .build());

        // 7. Database Subsystem
        boolean dbUp = conversationRepository != null;
        components.put("database", SubsystemHealthDto.builder()
                .status(dbUp ? "UP" : "DOWN")
                .details(Map.of("available", dbUp, "repository", "ConversationRepository"))
                .build());

        // 8. Cache Subsystem
        boolean cacheUp = contextCache != null;
        components.put("cache", SubsystemHealthDto.builder()
                .status(cacheUp ? "UP" : "DOWN")
                .details(Map.of("available", cacheUp))
                .build());

        boolean allUp = runtimeUp && orchestratorUp && providerUp && dbUp;
        String overallStatus = allUp ? "UP" : (providerUp ? "DEGRADED" : "DOWN");
        String readiness = allUp ? "READY" : "NOT_READY";

        return AtlasHealthResponse.builder()
                .status(overallStatus)
                .subsystemReadiness(readiness)
                .timestamp(Instant.now())
                .components(components)
                .build();
    }

    @Override
    public AtlasHealthResponse getReadiness() {
        AtlasHealthResponse health = getHealth();
        return health;
    }

    @Override
    public AtlasHealthResponse getLiveness() {
        return AtlasHealthResponse.builder()
                .status("UP")
                .subsystemReadiness("READY")
                .timestamp(Instant.now())
                .components(Map.of("process", SubsystemHealthDto.builder().status("UP").details(Map.of("alive", true)).build()))
                .build();
    }
}
