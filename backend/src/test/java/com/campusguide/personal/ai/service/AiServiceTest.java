package com.campusguide.personal.ai.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.ai.client.AiGatewayClient;
import com.campusguide.personal.ai.config.AiGatewayProperties;
import com.campusguide.personal.ai.dto.gateway.AiGatewayRequest;
import com.campusguide.personal.ai.dto.gateway.AiGatewayResponse;
import com.campusguide.personal.ai.dto.request.ChatRequest;
import com.campusguide.personal.ai.dto.response.ChatResponse;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.AiProvider;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.exception.AiGatewayException;
import com.campusguide.personal.ai.prompt.PromptBuilder;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import com.campusguide.personal.ai.service.impl.AiServiceImpl;
import com.campusguide.personal.ai.service.interfaces.ConversationContextBuilder;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConversationContextBuilder conversationContextBuilder;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private AiGatewayClient aiGatewayClient;

    @Mock
    private AiGatewayProperties properties;

    @InjectMocks
    private AiServiceImpl aiService;

    private UserDetails userDetails;
    private UserDetails otherUserDetails;
    private User user;
    private User otherUser;
    private Conversation conversation;
    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        otherUserDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user = User.builder()
                .id("user-123")
                .email("student@campusguide.com")
                .build();

        otherUser = User.builder()
                .id("user-456")
                .email("other@campusguide.com")
                .build();

        conversation = Conversation.builder()
                .id("conv-123")
                .userId("user-123")
                .title("General Chat")
                .type(ConversationType.GENERAL_CHAT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        chatRequest = ChatRequest.builder()
                .message("Hello Gateway")
                .build();
    }

    @Test
    void chat_Success() {
        AiGatewayRequest gatewayRequest = AiGatewayRequest.builder()
                .correlationId("corr-123")
                .conversationId("conv-123")
                .conversationType("GENERAL_CHAT")
                .userMessage("Hello Gateway")
                .conversationHistory(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        AiGatewayResponse gatewayResponse = AiGatewayResponse.builder()
                .response("Response from Gateway")
                .model("gpt-4")
                .provider(AiProvider.OPENAI)
                .processingTime(0.3)
                .build();

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(conversation));
        when(conversationContextBuilder.buildHistoryContext("conv-123")).thenReturn(new ArrayList<>());
        when(properties.isEnabled()).thenReturn(true);
        when(promptBuilder.buildPayload(any(), eq("conv-123"), eq(ConversationType.GENERAL_CHAT), eq("Hello Gateway"), any(), any()))
                .thenReturn(gatewayRequest);
        when(aiGatewayClient.sendRequest(gatewayRequest)).thenReturn(gatewayResponse);

        ChatResponse response = aiService.chat(userDetails, "conv-123", chatRequest);

        assertNotNull(response);
        assertEquals("Response from Gateway", response.getAssistantMessage());
        assertEquals("conv-123", response.getConversationId());
        assertEquals("gpt-4", response.getModel());
        assertEquals(AiProvider.OPENAI, response.getProvider());
        assertEquals(0.3, response.getProcessingTime());

        // Verify message persistence (both user message and assistant response)
        verify(messageRepository, times(2)).save(any(Message.class));
        verify(conversationRepository, times(2)).save(conversation);
    }

    @Test
    void chat_GatewayUnavailable_ReturnsGracefulFallback() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(conversation));
        when(conversationContextBuilder.buildHistoryContext("conv-123")).thenReturn(new ArrayList<>());
        when(properties.isEnabled()).thenReturn(true);
        when(promptBuilder.buildPayload(any(), eq("conv-123"), eq(ConversationType.GENERAL_CHAT), eq("Hello Gateway"), any(), any()))
                .thenThrow(new AiGatewayException("Gateway unavailable"));

        ChatResponse response = aiService.chat(userDetails, "conv-123", chatRequest);

        assertNotNull(response);
        assertEquals("I'm currently unavailable. Please try again in a few moments.", response.getAssistantMessage());
        assertEquals("fallback", response.getModel());
        assertEquals(AiProvider.UNKNOWN, response.getProvider());
        assertEquals(0.0, response.getProcessingTime());

        // Verify only user message is saved, assistant response is NOT saved upon error
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void chat_ConversationNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-not-found", "user-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                aiService.chat(userDetails, "conv-not-found", chatRequest));
    }

    @Test
    void chat_UnauthorizedConversationAccess_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(otherUserDetails.getUsername())).thenReturn(Optional.of(otherUser));
        when(conversationRepository.findByIdAndUserId("conv-123", "user-456")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                aiService.chat(otherUserDetails, "conv-123", chatRequest));
    }
}
