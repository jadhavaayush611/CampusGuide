package com.campusguide.personal.ai.atlas;

import com.campusguide.common.security.UserPrincipal;
import com.campusguide.personal.ai.atlas.dto.ConversationCreateRequest;
import com.campusguide.personal.ai.atlas.dto.ConversationHistoryResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationUpdateRequest;
import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException;
import com.campusguide.personal.ai.atlas.service.AtlasConversationService;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConversationSessionHandlingTest {

    @Autowired
    private AtlasConversationService conversationService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    private UserPrincipal userA;
    private UserPrincipal userB;

    @BeforeEach
    void setUp() {
        userA = new UserPrincipal(
                "65b9876543210987654321aa",
                "student.a@ves.ac.in",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        userB = new UserPrincipal(
                "65b9876543210987654321bb",
                "student.b@ves.ac.in",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @Test
    @DisplayName("User should be able to create, retrieve, and switch conversations")
    void testConversationManagementLifecycle() {
        // 1. Create Conversation
        ConversationCreateRequest createReq = ConversationCreateRequest.builder()
                .title("Advising Session A")
                .type("ACADEMIC_ADVISOR")
                .build();
        
        ConversationResponse created = conversationService.createConversation(createReq, userA);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Advising Session A", created.getTitle());
        assertEquals("ACADEMIC_ADVISOR", created.getType());

        // 2. Fetch list for User A
        List<ConversationResponse> listA = conversationService.getUserConversations(userA);
        assertTrue(listA.stream().anyMatch(c -> c.getId().equals(created.getId())));

        // 3. Switch/Retrieve specific conversation
        ConversationResponse retrieved = conversationService.getConversation(created.getId(), userA);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals("Advising Session A", retrieved.getTitle());

        // 4. Update title
        ConversationUpdateRequest updateReq = ConversationUpdateRequest.builder()
                .title("Updated Session A")
                .build();
        ConversationResponse updated = conversationService.updateConversation(created.getId(), updateReq, userA);
        assertEquals("Updated Session A", updated.getTitle());
    }

    @Test
    @DisplayName("Conversation and messages should be isolated between users")
    void testUserIsolationAndForbiddenAccess() {
        // User A creates conversation
        ConversationCreateRequest createReq = ConversationCreateRequest.builder()
                .title("User A Private Chat")
                .type("GENERAL")
                .build();
        ConversationResponse userAConv = conversationService.createConversation(createReq, userA);

        // Verify User B cannot list User A's conversation
        List<ConversationResponse> listB = conversationService.getUserConversations(userB);
        assertFalse(listB.stream().anyMatch(c -> c.getId().equals(userAConv.getId())), 
                "User A's conversation must not leak into User B's list");

        // Verify User B accessing User A's conversation ID directly fails
        assertThrows(AtlasForbiddenException.class, () -> {
            conversationService.getConversation(userAConv.getId(), userB);
        }, "Access to another user's conversation ID directly must trigger Forbidden");
    }

    @Test
    @DisplayName("Non-existent conversation ID queries should trigger Not Found")
    void testNonExistentConversationHandling() {
        assertThrows(AtlasNotFoundException.class, () -> {
            conversationService.getConversation("non-existent-conv-id", userA);
        }, "Should throw Not Found for invalid ID lookup");
    }
}
