package com.campusguide.personal.ai.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.ai.dto.request.CreateConversationRequest;
import com.campusguide.personal.ai.dto.request.UpdateConversationRequest;
import com.campusguide.personal.ai.dto.response.ConversationResponse;
import com.campusguide.personal.ai.dto.response.ConversationSummaryResponse;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.mapper.AiMapper;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.service.impl.ConversationServiceImpl;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
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

import com.campusguide.platform.user.service.CurrentUserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private AiMapper aiMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private UserDetails userDetails;
    private UserDetails otherUserDetails;
    private User user;
    private User otherUser;
    private Conversation conversation;

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
                .title("Test Conversation")
                .type(ConversationType.GENERAL_CHAT)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        lenient().when(currentUserService.getCurrentUser(userDetails)).thenReturn(user);
        lenient().when(currentUserService.getCurrentUser(otherUserDetails)).thenReturn(otherUser);
    }

    @Test
    void createConversation_Success() {
        CreateConversationRequest request = CreateConversationRequest.builder()
                .title("New Chat")
                .type(ConversationType.GENERAL_CHAT)
                .build();


        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation saved = invocation.getArgument(0);
            saved.setId("conv-new");
            return saved;
        });

        ConversationResponse response = conversationService.createConversation(userDetails, request);

        assertNotNull(response);
        assertEquals("conv-new", response.getId());
        assertEquals("New Chat", response.getTitle());
        assertEquals("user-123", response.getUserId());
        assertEquals(ConversationType.GENERAL_CHAT, response.getType());
        assertTrue(response.isActive());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void createConversation_UserNotAuthenticated_ThrowsUnauthorisedException() {
        CreateConversationRequest request = CreateConversationRequest.builder()
                .title("New Chat")
                .type(ConversationType.GENERAL_CHAT)
                .build();

        when(currentUserService.getCurrentUser(null)).thenThrow(new UnauthorisedException("User is not authenticated"));

        assertThrows(UnauthorisedException.class, () -> conversationService.createConversation(null, request));
    }

    @Test
    void renameConversation_Success() {
        UpdateConversationRequest request = UpdateConversationRequest.builder()
                .title("Updated Title")
                .build();


        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        ConversationResponse response = conversationService.renameConversation(userDetails, "conv-123", request);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    void renameConversation_NotFound_ThrowsResourceNotFoundException() {
        UpdateConversationRequest request = UpdateConversationRequest.builder()
                .title("Updated Title")
                .build();


        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                conversationService.renameConversation(userDetails, "conv-123", request));
    }

    @Test
    void deleteConversation_Success() {

        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        conversationService.deleteConversation(userDetails, "conv-123");

        assertEquals(ConversationStatus.DELETED, conversation.getStatus());
        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    void getConversation_Success() {

        when(conversationRepository.findByIdAndUserId("conv-123", "user-123")).thenReturn(Optional.of(conversation));

        ConversationResponse response = conversationService.getConversation(userDetails, "conv-123");

        assertNotNull(response);
        assertEquals("conv-123", response.getId());
        assertEquals("Test Conversation", response.getTitle());
    }

    @Test
    void getConversation_UnauthorizedAccess_ThrowsResourceNotFoundException() {
        when(conversationRepository.findByIdAndUserId("conv-123", "user-456")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                conversationService.getConversation(otherUserDetails, "conv-123"));
    }

    @Test
    void listConversations_Success() {

        when(conversationRepository.findByUserIdAndStatus("user-123", ConversationStatus.ACTIVE)).thenReturn(List.of(conversation));

        List<ConversationSummaryResponse> list = conversationService.listConversations(userDetails);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("conv-123", list.get(0).getId());
        assertEquals("Test Conversation", list.get(0).getTitle());
    }
}
