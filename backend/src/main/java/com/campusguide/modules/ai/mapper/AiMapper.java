package com.campusguide.modules.ai.mapper;

import com.campusguide.modules.ai.entity.Conversation;
import com.campusguide.modules.ai.entity.Message;
import com.campusguide.modules.ai.enums.ConversationStatus;
import com.campusguide.modules.ai.dto.response.ConversationResponse;
import com.campusguide.modules.ai.dto.response.ConversationSummaryResponse;
import com.campusguide.modules.ai.dto.response.MessageResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class AiMapper {

    public ConversationResponse toConversationResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUserId())
                .title(conversation.getTitle())
                .type(conversation.getType())
                .metadata(conversation.getMetadata() != null ? new HashMap<>(conversation.getMetadata()) : new HashMap<>())
                .active(conversation.getStatus() == ConversationStatus.ACTIVE)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public ConversationSummaryResponse toConversationSummaryResponse(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        return ConversationSummaryResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .type(conversation.getType())
                .active(conversation.getStatus() == ConversationStatus.ACTIVE)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public MessageResponse toMessageResponse(Message message) {
        if (message == null) {
            return null;
        }
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .role(message.getRole())
                .content(message.getContent())
                .metadata(message.getMetadata() != null ? new HashMap<>(message.getMetadata()) : new HashMap<>())
                .timestamp(message.getTimestamp())
                .build();
    }
}
