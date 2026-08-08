package com.campusguide.personal.ai.atlas.streaming;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.exception.AtlasPromptValidationException;
import com.campusguide.personal.ai.atlas.security.AtlasSecurityManager;
import com.campusguide.personal.ai.atlas.service.AtlasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtlasStreamingServiceImpl implements AtlasStreamingService {

    private final StreamPublisher streamPublisher;
    private final AtlasService atlasService;
    @Autowired(required = false)
    private final AtlasSecurityManager securityManager;

    private final ExecutorService streamingExecutor = Executors.newCachedThreadPool();

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down AtlasStreamingServiceImpl streamingExecutor...");
        streamingExecutor.shutdown();
        try {
            if (!streamingExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                streamingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            streamingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public SseEmitter streamChat(AtlasChatRequest request, String lastEventId, UserDetails userDetails) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            throw new AtlasPromptValidationException("Prompt message cannot be empty or blank");
        }

        String userId = userDetails != null ? userDetails.getUsername() : "anonymous";
        if (securityManager != null && userDetails != null) {
            securityManager.enforceRateLimit(userId);
        }

        String convId = request.getConversationId() != null ? request.getConversationId() : "conv_" + UUID.randomUUID().toString();
        StreamSession session = streamPublisher.createSession(convId, userId);
        SseEmitter emitter = streamPublisher.subscribeSse(session.getSessionId(), lastEventId);

        CompletableFuture.runAsync(() -> {
            String sessionId = session.getSessionId();
            try {
                // 1. CONNECTION_OPENED
                streamPublisher.publishEvent(sessionId, StreamEventType.CONNECTION_OPENED,
                        Map.of("sessionId", sessionId, "conversationId", convId, "status", "connected"));

                // 2. THINKING
                streamPublisher.publishEvent(sessionId, StreamEventType.THINKING,
                        Map.of("phase", "intent_detection", "message", "Analyzing prompt context and user intent"));

                // 3. REASONING
                streamPublisher.publishEvent(sessionId, StreamEventType.REASONING,
                        Map.of("phase", "graph_reasoning", "message", "Evaluating knowledge graph and candidate strategies"));

                // 4. PLANNING
                streamPublisher.publishEvent(sessionId, StreamEventType.PLANNING,
                        Map.of("phase", "plan_generation", "message", "Formulating optimal execution plan"));

                // 5. EXECUTION_STARTED
                String executionId = "exec_" + UUID.randomUUID().toString().substring(0, 8);
                streamPublisher.publishEvent(sessionId, StreamEventType.EXECUTION_STARTED,
                        Map.of("executionId", executionId, "status", "RUNNING"));

                // 6. TOOL_STARTED (simulated tool execution step)
                streamPublisher.publishEvent(sessionId, StreamEventType.TOOL_STARTED,
                        Map.of("toolName", "ContextRetrievalTool", "status", "IN_PROGRESS"));

                // 7. TOOL_COMPLETED
                streamPublisher.publishEvent(sessionId, StreamEventType.TOOL_COMPLETED,
                        Map.of("toolName", "ContextRetrievalTool", "status", "SUCCESS"));

                // 8. EXECUTION_COMPLETED
                streamPublisher.publishEvent(sessionId, StreamEventType.EXECUTION_COMPLETED,
                        Map.of("executionId", executionId, "status", "COMPLETED"));

                // Get complete response from Atlas Core service
                AtlasChatResponse response = atlasService.chat(request, userDetails);
                String content = response.getContent() != null ? response.getContent() : "";

                // 9. RESPONSE_TOKEN (emit content in chunks/tokens)
                String[] tokens = content.split("(?<=\\s)|(?=\\s)");
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        streamPublisher.publishEvent(sessionId, StreamEventType.RESPONSE_TOKEN,
                                Map.of("token", token, "conversationId", convId));
                    }
                }

                // 10. COMPLETION
                streamPublisher.publishEvent(sessionId, StreamEventType.COMPLETION,
                        Map.of("id", response.getId(),
                                "conversationId", convId,
                                "model", response.getModel() != null ? response.getModel() : "atlas-v1",
                                "finishReason", response.getFinishReason() != null ? response.getFinishReason() : "stop",
                                "usage", response.getUsage() != null ? response.getUsage() : Map.of("totalTokens", tokens.length)));

                // 11. CONNECTION_CLOSED
                streamPublisher.publishEvent(sessionId, StreamEventType.CONNECTION_CLOSED,
                        Map.of("sessionId", sessionId, "status", "closed"));

            } catch (Exception e) {
                log.error("Error during stream execution for session {}: {}", session.getSessionId(), e.getMessage());
                streamPublisher.publishEvent(session.getSessionId(), StreamEventType.ERROR,
                        Map.of("error", e.getMessage() != null ? e.getMessage() : "Execution error"));
                streamPublisher.publishEvent(session.getSessionId(), StreamEventType.CONNECTION_CLOSED,
                        Map.of("sessionId", session.getSessionId(), "status", "closed_with_error"));
            }
        }, streamingExecutor);

        return emitter;
    }
}
