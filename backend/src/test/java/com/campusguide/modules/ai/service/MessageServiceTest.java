package com.campusguide.modules.ai.service;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.modules.ai.dto.request.SendMessageRequest;
import com.campusguide.modules.ai.dto.response.ConversationHistoryResponse;
import com.campusguide.modules.ai.dto.response.MessageResponse;
import com.campusguide.modules.ai.entity.Conversation;
import com.campusguide.modules.ai.entity.Message;
import com.campusguide.modules.ai.enums.ConversationStatus;
import com.campusguide.modules.ai.enums.ConversationType;
import com.campusguide.modules.ai.enums.MessageRole;
import com.campusguide.modules.ai.mapper.AiMapper;
import com.campusguide.modules.ai.repository.ConversationRepository;
import com.campusguide.modules.ai.repository.MessageRepository;
import com.campusguide.modules.ai.service.impl.MessageServiceImpl;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private AiMapper aiMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private UserDetails userDetails;
    private User user;
    private Conversation activeConversation;
    private Conversation inactiveConversation;
    private Message msg1;
    private Message msg2;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user = User.builder()
                .id("user-123")
                .email("student@campusguide.com")
                .build();

        activeConversation = Conversation.builder()
                .id("conv-123")
                .userId("user-123")
                .title("Active Chat")
                .type(ConversationType.GENERAL_CHAT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        inactiveConversation = Conversation.builder()
                .id("conv-inactive")
                .userId("user-123")
                .title("Inactive Chat")
                .type(ConversationType.GENERAL_CHAT)
                .status(ConversationStatus.DELETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        msg1 = Message.builder()
                .id("msg-1")
                .conversationId("conv-123")
                .role(MessageRole.USER)
                .content("Hello")
                .timestamp(Instant.now().minusSeconds(10))
                .build();

        msg2 = Message.builder()
                .id("msg-2")
                .conversationId("conv-123")
                .role(MessageRole.ASSISTANT)
                .content("Hi there")
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void saveMessage_Success() {
        SendMessageRequest request = SendMessageRequest.builder()
                .role(MessageRole.USER)
                .content("New Message")
                .build();

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(activeConversation));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            saved.setId("msg-new");
            return saved;
        });

        MessageResponse response = messageService.saveMessage(userDetails, "conv-123", request);

        assertNotNull(response);
        assertEquals("msg-new", response.getId());
        assertEquals("New Message", response.getContent());
        assertEquals(MessageRole.USER, response.getRole());
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(conversationRepository, times(1)).save(activeConversation);
    }

    @Test
    void saveMessage_ConversationInactive_ThrowsResourceNotFoundException() {
        SendMessageRequest request = SendMessageRequest.builder()
                .role(MessageRole.USER)
                .content("New Message")
                .build();

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-inactive", "user-123")).thenReturn(Optional.of(inactiveConversation));

        assertThrows(ResourceNotFoundException.class, () -> 
                messageService.saveMessage(userDetails, "conv-inactive", request));
    }

    @Test
    void getConversationHistory_Success() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(activeConversation));
        when(messageRepository.findByConversationIdOrderByTimestampAsc("conv-123")).thenReturn(List.of(msg1, msg2));

        ConversationHistoryResponse response = messageService.getConversationHistory(userDetails, "conv-123");

        assertNotNull(response);
        assertEquals("conv-123", response.getConversation().getId());
        assertEquals(2, response.getMessages().size());
        assertEquals("msg-1", response.getMessages().get(0).getId());
        assertEquals("msg-2", response.getMessages().get(1).getId());
        
        assertTrue(response.getMessages().get(0).getTimestamp().isBefore(response.getMessages().get(1).getTimestamp()));
    }

    @Test
    void deleteConversationMessages_Success() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(activeConversation));

        messageService.deleteConversationMessages(userDetails, "conv-123");

        verify(messageRepository, times(1)).deleteByConversationId("conv-123");
        verify(conversationRepository, times(1)).save(activeConversation);
    }
}
