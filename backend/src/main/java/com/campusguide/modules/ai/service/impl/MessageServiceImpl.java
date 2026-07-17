package com.campusguide.modules.ai.service.impl;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.ai.dto.request.SendMessageRequest;
import com.campusguide.modules.ai.dto.response.ConversationHistoryResponse;
import com.campusguide.modules.ai.dto.response.ConversationResponse;
import com.campusguide.modules.ai.dto.response.MessageResponse;
import com.campusguide.modules.ai.entity.Conversation;
import com.campusguide.modules.ai.entity.Message;
import com.campusguide.modules.ai.enums.ConversationStatus;
import com.campusguide.modules.ai.mapper.AiMapper;
import com.campusguide.modules.ai.repository.ConversationRepository;
import com.campusguide.modules.ai.repository.MessageRepository;
import com.campusguide.modules.ai.service.interfaces.MessageService;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AiMapper aiMapper;

    @Override
    public MessageResponse saveMessage(UserDetails userDetails, String conversationId, SendMessageRequest request) {
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new ResourceNotFoundException("Conversation is not active");
        }

        Message message = Message.builder()
                .conversationId(conversationId)
                .role(request.getRole())
                .content(request.getContent())
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .timestamp(Instant.now())
                .build();

        message = messageRepository.save(message);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return aiMapper.toMessageResponse(message);
    }

    @Override
    public ConversationHistoryResponse getConversationHistory(UserDetails userDetails, String conversationId) {
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new ResourceNotFoundException("Conversation is not active");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        List<MessageResponse> messageResponses = messages.stream()
                .map(aiMapper::toMessageResponse)
                .collect(Collectors.toList());

        ConversationResponse conversationResponse = aiMapper.toConversationResponse(conversation);

        return ConversationHistoryResponse.builder()
                .conversation(conversationResponse)
                .messages(messageResponses)
                .build();
    }

    @Override
    public void deleteConversationMessages(UserDetails userDetails, String conversationId) {
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));

        messageRepository.deleteByConversationId(conversationId);

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}
