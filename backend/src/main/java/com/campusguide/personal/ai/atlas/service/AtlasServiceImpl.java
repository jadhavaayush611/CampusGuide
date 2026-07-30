package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.CapabilityResponse;
import com.campusguide.personal.ai.atlas.dto.ChatRequest;
import com.campusguide.personal.ai.atlas.dto.ChatResponse;
import com.campusguide.personal.ai.atlas.execution.workflow.WorkflowRegistry;
import com.campusguide.personal.ai.atlas.orchestration.ConversationOrchestrator;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.security.AtlasSecurityManager;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AtlasServiceImpl implements AtlasService {

    private final ConversationOrchestrator conversationOrchestrator;
    private final CurrentUserService currentUserService;
    private final WorkflowRegistry workflowRegistry;
    private final AIProvider aiProvider;
    private final AtlasSecurityManager securityManager;

    @Autowired
    public AtlasServiceImpl(
            ConversationOrchestrator conversationOrchestrator,
            @Autowired(required = false) CurrentUserService currentUserService,
            @Autowired(required = false) WorkflowRegistry workflowRegistry,
            @Autowired(required = false) AIProvider aiProvider,
            @Autowired(required = false) AtlasSecurityManager securityManager
    ) {
        this.conversationOrchestrator = conversationOrchestrator;
        this.currentUserService = currentUserService;
        this.workflowRegistry = workflowRegistry;
        this.aiProvider = aiProvider;
        this.securityManager = securityManager;
    }

    public AtlasServiceImpl(ConversationOrchestrator conversationOrchestrator, CurrentUserService currentUserService) {
        this(conversationOrchestrator, currentUserService, null, null, null);
    }

    @Override
    public AtlasChatResponse chat(AtlasChatRequest request) {
        log.info("Delegating unauthenticated/implicit chat request to ConversationOrchestrator");
        return conversationOrchestrator.orchestrate(request, (String) null);
    }

    @Override
    public AtlasChatResponse chat(AtlasChatRequest request, UserDetails userDetails) {
        log.info("Delegating authenticated chat request to ConversationOrchestrator");
        String userId = resolveUserId(userDetails);
        if (securityManager != null && userId != null) {
            securityManager.enforceRateLimit(userId);
        }
        long startTime = System.currentTimeMillis();
        AtlasChatResponse response = conversationOrchestrator.orchestrate(request, userId);
        long latencyMs = System.currentTimeMillis() - startTime;
        if (securityManager != null) {
            securityManager.logAudit("POST /api/v1/atlas/chat", userId, response.getConversationId(), latencyMs, "SUCCESS");
        }
        return response;
    }

    @Override
    public ChatResponse chat(ChatRequest request, UserDetails userDetails) {
        AtlasChatResponse response = chat((AtlasChatRequest) request, userDetails);
        return ChatResponse.chatResponseBuilder()
                .id(response.getId())
                .conversationId(response.getConversationId())
                .content(response.getContent())
                .role(response.getRole())
                .model(response.getModel())
                .finishReason(response.getFinishReason())
                .usage(response.getUsage())
                .timestamp(response.getTimestamp())
                .metadata(response.getMetadata())
                .build();
    }

    @Override
    public CapabilityResponse getCapabilities() {
        List<String> workflows = new ArrayList<>();
        if (workflowRegistry != null) {
            workflows.addAll(workflowRegistry.getAllTemplates().keySet());
        }
        if (workflows.isEmpty()) {
            workflows.addAll(List.of("academic_advising_workflow", "course_recommendation_workflow", "campus_navigation_workflow", "default_workflow"));
        }

        List<String> models = List.of("gpt-4o-mini", "gpt-4o", "mock-model");
        if (aiProvider != null && aiProvider.getMetadata() != null && aiProvider.getMetadata().getSupportedModels() != null) {
            models = aiProvider.getMetadata().getSupportedModels();
        }

        String providerName = (aiProvider != null && aiProvider.getMetadata() != null) ? aiProvider.getMetadata().getName() : "OpenAI Resilient Provider";

        return CapabilityResponse.builder()
                .atlasVersion("1.0.0")
                .apiVersion("v1")
                .status("OPERATIONAL")
                .registeredCapabilities(List.of(
                        "PROVIDER_AGNOSTIC_CHAT",
                        "CONTEXT_INTELLIGENCE",
                        "HYBRID_RAG",
                        "KNOWLEDGE_GRAPH_REASONING",
                        "DECISION_INTELLIGENCE",
                        "WORKFLOW_ORCHESTRATION"
                ))
                .availableWorkflows(workflows)
                .supportedFeatures(List.of(
                        "multi_turn_conversations",
                        "selective_strategy_retrieval",
                        "evidence_fusion",
                        "token_budgeting",
                        "circuit_breaker",
                        "rate_limiting"
                ))
                .supportedModels(models)
                .provider(providerName)
                .limits(Map.of(
                        "maxPromptLength", 4096,
                        "maxTokens", 32000,
                        "rateLimitPerMinute", 60
                ))
                .build();
    }

    @Override
    public CapabilityResponse getOperationalInfo() {
        return getCapabilities();
    }

    private String resolveUserId(UserDetails userDetails) {
        if (userDetails != null && currentUserService != null) {
            try {
                User user = currentUserService.getCurrentUser(userDetails);
                if (user != null) {
                    return user.getId();
                }
            } catch (Exception e) {
                log.warn("Could not resolve authenticated user from UserDetails: {}", e.getMessage());
            }
        }
        return null;
    }
}
