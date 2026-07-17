package com.campusguide.modules.ai.service.impl;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.ai.dto.request.CreateConversationRequest;
import com.campusguide.modules.ai.dto.request.UpdateConversationRequest;
import com.campusguide.modules.ai.dto.response.ConversationResponse;
import com.campusguide.modules.ai.dto.response.ConversationSummaryResponse;
import com.campusguide.modules.ai.entity.Conversation;
import com.campusguide.modules.ai.enums.ConversationStatus;
import com.campusguide.modules.ai.mapper.AiMapper;
import com.campusguide.modules.ai.repository.ConversationRepository;
import com.campusguide.modules.ai.service.interfaces.ConversationService;
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
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AiMapper aiMapper;

    @Override
    public ConversationResponse createConversation(UserDetails userDetails, CreateConversationRequest request) {
        User user = getUser(userDetails);

        Conversation conversation = Conversation.builder()
                .userId(user.getId())
                .title(request.getTitle())
                .type(request.getType())
                .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        conversation = conversationRepository.save(conversation);
        return aiMapper.toConversationResponse(conversation);
    }

    @Override
    public ConversationResponse renameConversation(UserDetails userDetails, String id, UpdateConversationRequest request) {
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + id));

        conversation.setTitle(request.getTitle());
        conversation.setUpdatedAt(Instant.now());

        conversation = conversationRepository.save(conversation);
        return aiMapper.toConversationResponse(conversation);
    }

    @Override
    public void deleteConversation(UserDetails userDetails, String id) {
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + id));

        conversation.setStatus(ConversationStatus.DELETED);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
    }

    @Override
    public ConversationResponse getConversation(UserDetails userDetails, String id) {
        User user = getUser(userDetails);

        Conversation conversation = conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + id));

        return aiMapper.toConversationResponse(conversation);
    }

    @Override
    public List<ConversationSummaryResponse> listConversations(UserDetails userDetails) {
        User user = getUser(userDetails);

        List<Conversation> conversations = conversationRepository.findByUserIdAndStatus(user.getId(), ConversationStatus.ACTIVE);
        return conversations.stream()
                .map(aiMapper::toConversationSummaryResponse)
                .collect(Collectors.toList());
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userDetails.getUsername()));
    }
}
