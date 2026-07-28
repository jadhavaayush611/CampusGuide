package com.campusguide.personal.ai.service.impl;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.ai.client.AiGatewayClient;
import com.campusguide.personal.ai.config.AiGatewayProperties;
import com.campusguide.personal.ai.dto.gateway.AiGatewayRequest;
import com.campusguide.personal.ai.dto.gateway.AiGatewayResponse;
import com.campusguide.personal.ai.dto.gateway.GatewayMessage;
import com.campusguide.personal.ai.dto.request.ChatRequest;
import com.campusguide.personal.ai.dto.response.ChatResponse;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.prompt.PromptBuilder;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import com.campusguide.personal.ai.service.interfaces.AiService;
import com.campusguide.personal.ai.service.interfaces.ConversationContextBuilder;
import com.campusguide.personal.ai.enums.AiProvider;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final CurrentUserService currentUserService;
    private final ConversationContextBuilder conversationContextBuilder;
    private final PromptBuilder promptBuilder;
    private final AiGatewayClient aiGatewayClient;
    private final AiGatewayProperties properties;

    @Override
    public ChatResponse chat(UserDetails userDetails, String conversationId, ChatRequest request) {
        String correlationId = java.util.UUID.randomUUID().toString();
        log.info("AI Service: Processing chat request [correlationId: {}] for conversation ID: {}", correlationId, conversationId);

        // 1. Validate ownership
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new ResourceNotFoundException("Conversation is not active");
        }

        // 2. Save the user's message
        Message userMessage = Message.builder()
                .conversationId(conversationId)
                .role(MessageRole.USER)
                .content(request.getMessage())
                .metadata(new HashMap<>())
                .timestamp(Instant.now())
                .build();
        messageRepository.save(userMessage);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        // 3. Build conversation context
        List<GatewayMessage> historyContext = conversationContextBuilder.buildHistoryContext(conversationId);
        
        // Exclude the user's message we just saved from the history context to prevent duplication
        if (!historyContext.isEmpty() && historyContext.get(historyContext.size() - 1).getContent().equals(request.getMessage())) {
            historyContext = new java.util.ArrayList<>(historyContext);
            historyContext.remove(historyContext.size() - 1);
        }

        String assistantText;
        String model = "fallback";
        AiProvider provider = AiProvider.UNKNOWN;
        Double processingTime = 0.0;

        if (properties.isEnabled()) {
            try {
                // 4. Build the gateway request
                AiGatewayRequest gatewayRequest = promptBuilder.buildPayload(
                        correlationId,
                        conversationId,
                        conversation.getType(),
                        request.getMessage(),
                        historyContext,
                        conversation.getMetadata()
                );

                // 5. Invoke the AI gateway
                AiGatewayResponse gatewayResponse = aiGatewayClient.sendRequest(gatewayRequest);

                assistantText = gatewayResponse.getResponse();
                model = gatewayResponse.getModel();
                provider = gatewayResponse.getProvider();
                processingTime = gatewayResponse.getProcessingTime();

                // 6. Save the assistant response
                Message assistantMessage = Message.builder()
                        .conversationId(conversationId)
                        .role(MessageRole.ASSISTANT)
                        .content(assistantText)
                        .metadata(new HashMap<>())
                        .timestamp(Instant.now())
                        .build();
                messageRepository.save(assistantMessage);

                conversation.setUpdatedAt(Instant.now());
                conversationRepository.save(conversation);

            } catch (Exception e) {
                log.error("AI Gateway call failed [correlationId: {}] for conversation: {}. Falling back to default response.", correlationId, conversationId, e);
                assistantText = "I'm currently unavailable. Please try again in a few moments.";
            }
        } else {
            log.warn("AI Gateway is disabled. Falling back to default response.");
            assistantText = "I'm currently unavailable. Please try again in a few moments.";
        }

        // 7. Return a ChatResponse
        return ChatResponse.builder()
                .assistantMessage(assistantText)
                .conversationId(conversationId)
                .model(model)
                .provider(provider)
                .processingTime(processingTime)
                .build();
    }

    private User getUser(UserDetails userDetails) {
        return currentUserService.getCurrentUser(userDetails);
    }
}
