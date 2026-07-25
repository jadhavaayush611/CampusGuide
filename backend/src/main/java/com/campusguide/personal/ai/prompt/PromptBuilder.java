package com.campusguide.personal.ai.prompt;

import com.campusguide.personal.ai.dto.gateway.AiGatewayRequest;
import com.campusguide.personal.ai.dto.gateway.GatewayMessage;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.exception.PromptBuildException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PromptBuilder {

    @Value("classpath:prompts/general_chat.txt")
    private Resource generalChatPromptResource;

    @Value("classpath:prompts/academic_advisor.txt")
    private Resource academicAdvisorPromptResource;

    @Value("classpath:prompts/career_guidance.txt")
    private Resource careerGuidancePromptResource;

    @Value("classpath:prompts/campus_assistant.txt")
    private Resource campusAssistantPromptResource;

    private final Map<ConversationType, String> systemPrompts = new HashMap<>();

    @PostConstruct
    public void init() {
        if (generalChatPromptResource != null) {
            systemPrompts.put(ConversationType.GENERAL_CHAT, loadPromptResource(generalChatPromptResource));
        }
        if (academicAdvisorPromptResource != null) {
            systemPrompts.put(ConversationType.ACADEMIC_ADVISOR, loadPromptResource(academicAdvisorPromptResource));
        }
        if (careerGuidancePromptResource != null) {
            systemPrompts.put(ConversationType.CAREER_GUIDANCE, loadPromptResource(careerGuidancePromptResource));
        }
        if (campusAssistantPromptResource != null) {
            systemPrompts.put(ConversationType.CAMPUS_ASSISTANT, loadPromptResource(campusAssistantPromptResource));
        }
    }

    private String loadPromptResource(Resource resource) {
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException | NullPointerException e) {
            log.error("Failed to load prompt resource: {}", resource != null ? resource.getFilename() : "null", e);
            throw new PromptBuildException("Could not load system prompt resource", e);
        }
    }

    public void setSystemPrompt(ConversationType type, String prompt) {
        systemPrompts.put(type, prompt);
    }

    public String getSystemPrompt(ConversationType type) {
        return systemPrompts.get(type);
    }

    public AiGatewayRequest buildPayload(
            String correlationId,
            String conversationId,
            ConversationType conversationType,
            String userMessage,
            List<GatewayMessage> conversationHistory,
            Map<String, Object> metadata) {
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            throw new PromptBuildException("Correlation ID cannot be null or empty");
        }
        if (conversationId == null) {
            throw new PromptBuildException("Conversation ID cannot be null");
        }
        if (conversationType == null) {
            throw new PromptBuildException("Conversation type cannot be null");
        }
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new PromptBuildException("User message cannot be null or empty");
        }

        String systemPrompt = systemPrompts.get(conversationType);
        if (systemPrompt == null) {
            throw new PromptBuildException("No system prompt configured for type: " + conversationType);
        }

        Map<String, Object> finalMetadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        finalMetadata.put("systemPrompt", systemPrompt);

        return AiGatewayRequest.builder()
                .correlationId(correlationId)
                .conversationId(conversationId)
                .conversationType(conversationType.name())
                .userMessage(userMessage)
                .conversationHistory(conversationHistory)
                .metadata(finalMetadata)
                .build();
    }
}
