package com.campusguide.modules.ai.service.impl;

import com.campusguide.modules.ai.config.AiGatewayProperties;
import com.campusguide.modules.ai.dto.gateway.GatewayMessage;
import com.campusguide.modules.ai.entity.Message;
import com.campusguide.modules.ai.exception.ConversationContextException;
import com.campusguide.modules.ai.repository.MessageRepository;
import com.campusguide.modules.ai.service.interfaces.ConversationContextBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationContextBuilderImpl implements ConversationContextBuilder {

    private final MessageRepository messageRepository;
    private final AiGatewayProperties properties;

    @Override
    public List<GatewayMessage> buildHistoryContext(String conversationId) {
        if (conversationId == null) {
            throw new ConversationContextException("Conversation ID cannot be null");
        }

        try {
            log.debug("Building conversation context for conversation: {}", conversationId);
            List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
            
            int limit = properties.getHistoryLimit();
            int totalMessages = messages.size();
            
            List<Message> limitedMessages = messages;
            if (totalMessages > limit) {
                limitedMessages = messages.subList(totalMessages - limit, totalMessages);
                log.debug("Conversation context truncated to recent {} messages from total {} messages", limit, totalMessages);
            }

            return limitedMessages.stream()
                    .map(msg -> GatewayMessage.builder()
                            .role(msg.getRole().name().toLowerCase())
                            .content(msg.getContent())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to build conversation context for ID: {}", conversationId, e);
            throw new ConversationContextException("Error building conversation context: " + e.getMessage(), e);
        }
    }
}
