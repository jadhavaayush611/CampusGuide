package com.campusguide.personal.ai.atlas.service;

import com.campusguide.personal.ai.atlas.dto.AtlasChatMessageDto;
import com.campusguide.personal.ai.atlas.dto.ConversationCreateRequest;
import com.campusguide.personal.ai.atlas.dto.ConversationHistoryResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationUpdateRequest;
import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException;
import com.campusguide.personal.ai.atlas.security.AtlasSecurityManager;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import com.campusguide.common.security.UserPrincipal;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AtlasConversationServiceImpl implements AtlasConversationService {

    @Autowired(required = false)
    private final ConversationRepository conversationRepository;
    @Autowired(required = false)
    private final MessageRepository messageRepository;
    @Autowired(required = false)
    private final CurrentUserService currentUserService;
    @Autowired(required = false)
    private final AtlasSecurityManager securityManager;

    @Override
    public ConversationResponse createConversation(ConversationCreateRequest request, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        if (securityManager != null && userId != null) {
            securityManager.enforceRateLimit(userId);
        }

        ConversationType type = parseType(request.getType());
        Conversation conversation = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .title(request.getTitle())
                .type(type)
                .status(ConversationStatus.ACTIVE)
                .metadata(request.getMetadata() != null ? new HashMap<>(request.getMetadata()) : new HashMap<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        if (conversationRepository != null) {
            conversation = conversationRepository.save(conversation);
        }

        if (securityManager != null) {
            securityManager.logAudit("POST /api/v1/atlas/conversations", userId, conversation.getId(), 0, "CREATED");
        }

        return mapToResponse(conversation, 0);
    }

    @Override
    public List<ConversationResponse> getUserConversations(UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        if (securityManager != null && userId != null) {
            securityManager.enforceRateLimit(userId);
        }

        if (conversationRepository == null || userId == null) {
            return Collections.emptyList();
        }

        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        return conversations.stream().map(c -> {
            long count = messageRepository != null ? messageRepository.countByConversationId(c.getId()) : 0;
            return mapToResponse(c, (int) count);
        }).collect(Collectors.toList());
    }

    @Override
    public ConversationResponse getConversation(String id, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);
        long count = messageRepository != null ? messageRepository.countByConversationId(conversation.getId()) : 0;
        return mapToResponse(conversation, (int) count);
    }

    @Override
    public ConversationResponse updateConversation(String id, ConversationUpdateRequest request, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            conversation.setTitle(request.getTitle());
        }
        if (request.getStatus() != null) {
            try {
                conversation.setStatus(ConversationStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid conversation status: {}", request.getStatus());
            }
        }
        if (request.getMetadata() != null) {
            if (conversation.getMetadata() == null) {
                conversation.setMetadata(new HashMap<>());
            }
            conversation.getMetadata().putAll(request.getMetadata());
        }
        conversation.setUpdatedAt(Instant.now());

        if (conversationRepository != null) {
            conversation = conversationRepository.save(conversation);
        }

        long count = messageRepository != null ? messageRepository.countByConversationId(conversation.getId()) : 0;
        return mapToResponse(conversation, (int) count);
    }

    @Override
    public void deleteConversation(String id, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);

        if (messageRepository != null) {
            messageRepository.deleteByConversationId(id);
        }
        if (conversationRepository != null) {
            conversationRepository.delete(conversation);
        }

        if (securityManager != null) {
            securityManager.logAudit("DELETE /api/v1/atlas/conversations", userId, id, 0, "DELETED");
        }
    }

    @Override
    public ConversationHistoryResponse getConversationHistory(String id, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);

        List<AtlasChatMessageDto> chatMessages = Collections.emptyList();
        if (messageRepository != null) {
            List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(id);
            chatMessages = messages.stream().map(m -> AtlasChatMessageDto.builder()
                    .role(m.getRole() != null ? m.getRole().name().toLowerCase() : "user")
                    .content(m.getContent())
                    .build()
            ).collect(Collectors.toList());
        }

        return ConversationHistoryResponse.builder()
                .conversationId(id)
                .userId(conversation.getUserId())
                .messages(chatMessages)
                .totalMessages(chatMessages.size())
                .build();
    }

    private Conversation findAndValidateConversation(String id, String userId, UserDetails userDetails) {
        if (conversationRepository == null) {
            throw new AtlasNotFoundException("Conversation not found with id: " + id);
        }
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new AtlasNotFoundException("Conversation not found with id: " + id));

        boolean isAdmin = isAdminUser(userDetails);
        if (securityManager != null) {
            securityManager.validateOwnership(conversation.getUserId(), userId, isAdmin);
        } else if (!isAdmin && (userId == null || !userId.equals(conversation.getUserId()))) {
            throw new AtlasForbiddenException("Access denied: You do not own this conversation");
        }

        return conversation;
    }

    private boolean isAdminUser(UserDetails userDetails) {
        if (userDetails == null) return false;
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ADMIN"));
    }

    private String resolveUserId(UserDetails userDetails) {
        if (userDetails instanceof UserPrincipal principal) {
            return principal.getId();
        }
        if (userDetails != null && currentUserService != null) {
            try {
                User user = currentUserService.getCurrentUser(userDetails);
                if (user != null) return user.getId();
            } catch (Exception e) {
                log.warn("Could not resolve current user id: {}", e.getMessage());
            }
            return userDetails.getUsername();
        }
        return userDetails != null ? userDetails.getUsername() : null;
    }

    private ConversationType parseType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) {
            return ConversationType.GENERAL_CHAT;
        }
        try {
            return ConversationType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ConversationType.GENERAL_CHAT;
        }
    }

    @Override
    public ConversationResponse archiveConversation(String id, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);
        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversation.setUpdatedAt(Instant.now());
        if (conversationRepository != null) {
            conversation = conversationRepository.save(conversation);
        }
        long count = messageRepository != null ? messageRepository.countByConversationId(id) : 0;
        return mapToResponse(conversation, (int) count);
    }

    @Override
    public ConversationResponse restoreConversation(String id, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setUpdatedAt(Instant.now());
        if (conversationRepository != null) {
            conversation = conversationRepository.save(conversation);
        }
        long count = messageRepository != null ? messageRepository.countByConversationId(id) : 0;
        return mapToResponse(conversation, (int) count);
    }

    @Override
    public ConversationResponse renameConversation(String id, String newTitle, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);
        if (newTitle != null && !newTitle.isBlank()) {
            conversation.setTitle(newTitle);
            conversation.setUpdatedAt(Instant.now());
            if (conversationRepository != null) {
                conversation = conversationRepository.save(conversation);
            }
        }
        long count = messageRepository != null ? messageRepository.countByConversationId(id) : 0;
        return mapToResponse(conversation, (int) count);
    }

    @Override
    public com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse getConversationSummary(String id, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);
        List<Message> messages = messageRepository != null ? messageRepository.findByConversationIdOrderByTimestampAsc(id) : Collections.emptyList();

        String summaryText = messages.isEmpty()
                ? "No messages in conversation."
                : "Conversation on " + conversation.getTitle() + " with " + messages.size() + " messages.";
        List<String> keyTopics = List.of(conversation.getType() != null ? conversation.getType().name() : "GENERAL");
        Instant lastMsgTime = messages.isEmpty() ? conversation.getUpdatedAt() : messages.get(messages.size() - 1).getTimestamp();

        return com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse.builder()
                .conversationId(id)
                .title(conversation.getTitle())
                .summary(summaryText)
                .keyTopics(keyTopics)
                .messageCount(messages.size())
                .lastMessageTimestamp(lastMsgTime)
                .status(conversation.getStatus() != null ? conversation.getStatus().name() : "ACTIVE")
                .build();
    }

    @Override
    public com.campusguide.personal.ai.atlas.dto.AtlasChatResponse continueConversation(String id, com.campusguide.personal.ai.atlas.dto.AtlasChatRequest request, UserDetails userDetails) {
        String userId = resolveUserId(userDetails);
        Conversation conversation = findAndValidateConversation(id, userId, userDetails);

        request.setConversationId(conversation.getId());
        // Save user message
        if (messageRepository != null && request.getPrompt() != null) {
            Message userMsg = Message.builder()
                    .id(UUID.randomUUID().toString())
                    .conversationId(id)
                    .role(com.campusguide.personal.ai.enums.MessageRole.USER)
                    .content(request.getPrompt())
                    .timestamp(Instant.now())
                    .build();
            messageRepository.save(userMsg);
        }

        com.campusguide.personal.ai.atlas.dto.AtlasChatResponse response = com.campusguide.personal.ai.atlas.dto.AtlasChatResponse.builder()
                .id("atlas-" + UUID.randomUUID())
                .conversationId(id)
                .content("Continued conversation response for: " + request.getPrompt())
                .role("assistant")
                .model(request.getModel() != null ? request.getModel() : "gpt-4o-mini")
                .finishReason("stop")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        // Save assistant response
        if (messageRepository != null) {
            Message assistantMsg = Message.builder()
                    .id(UUID.randomUUID().toString())
                    .conversationId(id)
                    .role(com.campusguide.personal.ai.enums.MessageRole.ASSISTANT)
                    .content(response.getContent())
                    .timestamp(Instant.now())
                    .build();
            messageRepository.save(assistantMsg);
        }

        conversation.setUpdatedAt(Instant.now());
        if (conversationRepository != null) {
            conversationRepository.save(conversation);
        }

        return response;
    }

    private ConversationResponse mapToResponse(Conversation conversation, int messageCount) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUserId())
                .title(conversation.getTitle())
                .type(conversation.getType() != null ? conversation.getType().name() : "GENERAL_CHAT")
                .status(conversation.getStatus() != null ? conversation.getStatus().name() : "ACTIVE")
                .messageCount(messageCount)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .metadata(conversation.getMetadata() != null ? conversation.getMetadata() : Map.of())
                .build();
    }
}
