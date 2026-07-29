package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.context.AtlasContext;
import com.campusguide.personal.ai.atlas.context.ContextEngine;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException;
import com.campusguide.personal.ai.atlas.mapper.AtlasMapper;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import com.campusguide.personal.ai.atlas.model.AtlasChatMessage;
import com.campusguide.personal.ai.atlas.model.AtlasNormalizedResponse;
import com.campusguide.personal.ai.atlas.model.AtlasPrompt;
import com.campusguide.personal.ai.atlas.model.AtlasRole;
import com.campusguide.personal.ai.atlas.prompt.ContextSectionAssembler;
import com.campusguide.personal.ai.atlas.prompt.PromptBuilder;
import com.campusguide.personal.ai.atlas.prompt.model.ContextSection;
import com.campusguide.personal.ai.atlas.prompt.model.PromptVersion;
import com.campusguide.personal.ai.atlas.provider.AIProvider;
import com.campusguide.personal.ai.atlas.ratelimit.RateLimitPolicy;
import com.campusguide.personal.ai.atlas.resilience.CircuitBreaker;
import com.campusguide.personal.ai.atlas.validation.AtlasPromptValidator;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orchestrates the Atlas AI conversation lifecycle, context aggregation, prompt construction,
 * model invocation, message persistence, and structured logging.
 */
@Service
@Slf4j
public class ConversationOrchestrator {

    private final AIProvider aiProvider;
    private final PromptBuilder promptBuilder;
    private final ContextSectionAssembler contextSectionAssembler;
    private final AtlasPromptValidator atlasPromptValidator;
    private final AtlasMapper atlasMapper;
    private final ContextEngine contextEngine;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final RateLimitPolicy rateLimitPolicy;
    private final AtlasMetrics atlasMetrics;
    private final CircuitBreaker circuitBreaker;

    public ConversationOrchestrator(
            AIProvider aiProvider,
            @Qualifier("atlasPromptBuilder") PromptBuilder promptBuilder,
            ContextSectionAssembler contextSectionAssembler,
            AtlasPromptValidator atlasPromptValidator,
            AtlasMapper atlasMapper,
            ContextEngine contextEngine,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository
    ) {
        this(aiProvider, promptBuilder, contextSectionAssembler, atlasPromptValidator, atlasMapper, contextEngine, conversationRepository, messageRepository, null, null, null);
    }

    @Autowired
    public ConversationOrchestrator(
            AIProvider aiProvider,
            @Qualifier("atlasPromptBuilder") PromptBuilder promptBuilder,
            ContextSectionAssembler contextSectionAssembler,
            AtlasPromptValidator atlasPromptValidator,
            AtlasMapper atlasMapper,
            ContextEngine contextEngine,
            @Autowired(required = false) ConversationRepository conversationRepository,
            @Autowired(required = false) MessageRepository messageRepository,
            @Autowired(required = false) RateLimitPolicy rateLimitPolicy,
            @Autowired(required = false) AtlasMetrics atlasMetrics,
            @Autowired(required = false) CircuitBreaker circuitBreaker
    ) {
        this.aiProvider = aiProvider;
        this.promptBuilder = promptBuilder;
        this.contextSectionAssembler = contextSectionAssembler;
        this.atlasPromptValidator = atlasPromptValidator;
        this.atlasMapper = atlasMapper;
        this.contextEngine = contextEngine;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.rateLimitPolicy = rateLimitPolicy;
        this.atlasMetrics = atlasMetrics;
        this.circuitBreaker = circuitBreaker;
    }

    public AtlasChatResponse orchestrate(AtlasChatRequest request) {
        return orchestrate(request, null);
    }

    public AtlasChatResponse orchestrate(AtlasChatRequest request, String userId) {
        long orchestrateStartTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        // 1. Validation
        atlasPromptValidator.validateRequest(request);

        // 2. Conversation Creation / ID resolution
        String conversationId = resolveOrCreateConversationId(request, userId);
        request.setConversationId(conversationId);

        // Rate limit check per user/conversation
        String rateLimitKey = StringUtils.hasText(userId) ? userId : conversationId;
        if (rateLimitPolicy != null && !rateLimitPolicy.tryAcquire(rateLimitKey)) {
            throw new AtlasRateLimitException("Rate limit exceeded for user or session: " + rateLimitKey);
        }

        setupMdcContext(requestId, conversationId);

        try {
            // 3. Chronological History Loading
            List<AtlasChatMessage> historyModels = loadChronologicalHistory(request, conversationId);

            // 4. Message Persistence (User Prompt)
            persistUserMessage(conversationId, request.getPrompt());

            // 5. Context Engine Execution with Latency Tracking
            long contextStart = System.currentTimeMillis();
            AtlasContext atlasContext = contextEngine.buildContext(request);
            if (StringUtils.hasText(userId)) {
                atlasContext.setUserId(userId);
            }
            long contextLatency = System.currentTimeMillis() - contextStart;
            if (atlasMetrics != null) {
                atlasMetrics.recordContextAssemblyLatency(contextLatency);
            }

            List<ContextSection> contextSections = contextSectionAssembler.assembleSections(atlasContext);
            Map<String, Object> contextPlaceholders = atlasContext != null ? atlasContext.getMergedPlaceholders() : request.getContextPlaceholders();

            // 6. Prompt Assembly using PromptBuilder with Latency Tracking
            long promptStart = System.currentTimeMillis();
            AtlasPrompt prompt = promptBuilder.buildPrompt(
                    request.getPrompt(),
                    request.getSystemPrompt(),
                    historyModels,
                    contextSections,
                    contextPlaceholders,
                    request.getModel(),
                    request.getTemperature(),
                    request.getMaxTokens()
            );
            long promptLatency = System.currentTimeMillis() - promptStart;
            if (atlasMetrics != null) {
                atlasMetrics.recordPromptAssemblyLatency(promptLatency);
            }

            // 7. Provider Call & Latency Measurement
            long providerStart = System.currentTimeMillis();
            AtlasNormalizedResponse normalizedResponse = aiProvider.sendPrompt(prompt);
            long providerLatency = System.currentTimeMillis() - providerStart;

            // 8. Assistant Response Persistence
            persistAssistantMessage(conversationId, normalizedResponse.getContent());

            long totalOrchestrationLatency = System.currentTimeMillis() - orchestrateStartTime;
            if (atlasMetrics != null) {
                atlasMetrics.recordOrchestrationLatency(totalOrchestrationLatency);
            }

            // 9. Structured Logging (NEVER log prompt or response content)
            logStructuredExecution(requestId, conversationId, totalOrchestrationLatency, prompt.getPromptVersion(), normalizedResponse);

            // 10. Response Mapping
            AtlasChatResponse response = atlasMapper.toResponseDto(normalizedResponse);
            response.setConversationId(conversationId);
            return response;
        } finally {
            clearMdcContext();
        }
    }

    private String resolveOrCreateConversationId(AtlasChatRequest request, String authenticatedUserId) {
        String convId = request.getConversationId();
        if (!StringUtils.hasText(convId)) {
            convId = UUID.randomUUID().toString();
        }

        if (conversationRepository != null) {
            final String finalConvId = convId;
            conversationRepository.findById(finalConvId).orElseGet(() -> {
                String effectiveUserId = StringUtils.hasText(authenticatedUserId) ? authenticatedUserId : "anonymous";
                Conversation newConv = Conversation.builder()
                        .id(finalConvId)
                        .userId(effectiveUserId)
                        .title("Atlas Conversation " + finalConvId.substring(0, Math.min(8, finalConvId.length())))
                        .type(ConversationType.GENERAL_CHAT)
                        .status(ConversationStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                return conversationRepository.save(newConv);
            });
        }
        return convId;
    }

    private List<AtlasChatMessage> loadChronologicalHistory(AtlasChatRequest request, String conversationId) {
        List<AtlasChatMessage> historyModels = new ArrayList<>();

        if (messageRepository != null && StringUtils.hasText(conversationId)) {
            List<Message> persistedMessages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
            if (persistedMessages != null && !persistedMessages.isEmpty()) {
                historyModels = persistedMessages.stream()
                        .map(this::toAtlasChatMessage)
                        .collect(Collectors.toList());
            }
        }

        if (historyModels.isEmpty() && request.getConversationHistory() != null && !request.getConversationHistory().isEmpty()) {
            historyModels = atlasMapper.toModelList(request.getConversationHistory());
        }

        return historyModels;
    }

    private AtlasChatMessage toAtlasChatMessage(Message msg) {
        AtlasRole role = AtlasRole.USER;
        if (msg.getRole() != null) {
            switch (msg.getRole()) {
                case ASSISTANT -> role = AtlasRole.ASSISTANT;
                case SYSTEM -> role = AtlasRole.SYSTEM;
                default -> role = AtlasRole.USER;
            }
        }
        return AtlasChatMessage.builder()
                .role(role)
                .content(msg.getContent())
                .build();
    }

    private void persistUserMessage(String conversationId, String userPrompt) {
        if (messageRepository != null && StringUtils.hasText(conversationId) && StringUtils.hasText(userPrompt)) {
            Message userMessage = Message.builder()
                    .conversationId(conversationId)
                    .role(MessageRole.USER)
                    .content(userPrompt)
                    .timestamp(Instant.now())
                    .build();
            messageRepository.save(userMessage);
        }
    }

    private void persistAssistantMessage(String conversationId, String assistantContent) {
        if (messageRepository != null && StringUtils.hasText(conversationId) && StringUtils.hasText(assistantContent)) {
            Message assistantMessage = Message.builder()
                    .conversationId(conversationId)
                    .role(MessageRole.ASSISTANT)
                    .content(assistantContent)
                    .timestamp(Instant.now())
                    .build();
            messageRepository.save(assistantMessage);
        }

        if (conversationRepository != null && StringUtils.hasText(conversationId)) {
            conversationRepository.findById(conversationId).ifPresent(conv -> {
                conv.setUpdatedAt(Instant.now());
                conversationRepository.save(conv);
            });
        }
    }

    private void setupMdcContext(String requestId, String conversationId) {
        MDC.put("requestId", requestId);
        MDC.put("conversationId", conversationId);
        if (circuitBreaker != null) {
            MDC.put("circuitBreakerState", circuitBreaker.getState().name());
        }
    }

    private void clearMdcContext() {
        MDC.remove("requestId");
        MDC.remove("conversationId");
        MDC.remove("circuitBreakerState");
    }

    private void logStructuredExecution(String requestId, String conversationId, long latencyMs, PromptVersion promptVersion, AtlasNormalizedResponse response) {
        int promptTokens = response.getUsage() != null ? response.getUsage().getPromptTokens() : 0;
        int completionTokens = response.getUsage() != null ? response.getUsage().getCompletionTokens() : 0;
        int totalTokens = response.getUsage() != null ? response.getUsage().getTotalTokens() : 0;
        String model = response.getModelUsed() != null ? response.getModelUsed() : "unknown";
        String provider = response.getProviderName() != null ? response.getProviderName() : aiProvider.getMetadata().getName();
        String versionStr = promptVersion != null ? promptVersion.getVersion() : "unknown";
        String cbState = circuitBreaker != null ? circuitBreaker.getState().name() : "N/A";

        log.info("Atlas Orchestration Complete - requestId: {}, conversationId: {}, model: {}, provider: {}, retries: 0, promptVersion: {}, latencyMs: {}, circuitBreakerState: {}, usage: [promptTokens: {}, completionTokens: {}, totalTokens: {}]",
                requestId, conversationId, model, provider, versionStr, latencyMs, cbState, promptTokens, completionTokens, totalTokens);
    }
}
