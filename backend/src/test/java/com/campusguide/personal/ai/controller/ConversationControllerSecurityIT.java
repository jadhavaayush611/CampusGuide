package com.campusguide.personal.ai.controller;

import com.campusguide.personal.ai.dto.request.CreateConversationRequest;
import com.campusguide.personal.ai.dto.request.SendMessageRequest;
import com.campusguide.personal.ai.dto.request.UpdateConversationRequest;
import com.campusguide.personal.ai.entity.Conversation;
import com.campusguide.personal.ai.entity.Message;
import com.campusguide.personal.ai.enums.ConversationStatus;
import com.campusguide.personal.ai.enums.ConversationType;
import com.campusguide.personal.ai.enums.MessageRole;
import com.campusguide.personal.ai.repository.ConversationRepository;
import com.campusguide.personal.ai.repository.MessageRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ConversationControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User otherUser;

    private UserDetails studentDetails;
    private UserDetails otherDetails;

    private Conversation studentConversation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        studentUser = User.builder()
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("User")
                .build();
        studentUser = userRepository.save(studentUser);

        otherUser = User.builder()
                .email("other@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Other")
                .lastName("User")
                .build();
        otherUser = userRepository.save(otherUser);

        studentDetails = org.springframework.security.core.userdetails.User.withUsername(studentUser.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        otherDetails = org.springframework.security.core.userdetails.User.withUsername(otherUser.getEmail())
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        studentConversation = Conversation.builder()
                .userId(studentUser.getId())
                .title("My Study Chat")
                .type(ConversationType.ACADEMIC_ADVISOR)
                .status(ConversationStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        studentConversation = conversationRepository.save(studentConversation);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    // 1. Create Conversation tests
    @Test
    void createConversation_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CreateConversationRequest request = CreateConversationRequest.builder()
                .title("New Chat")
                .type(ConversationType.GENERAL_CHAT)
                .build();

        mockMvc.perform(post("/api/v1/ai/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createConversation_Authenticated_ReturnsCreated() throws Exception {
        CreateConversationRequest request = CreateConversationRequest.builder()
                .title("New Chat")
                .type(ConversationType.GENERAL_CHAT)
                .build();

        mockMvc.perform(post("/api/v1/ai/conversations")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Chat"))
                .andExpect(jsonPath("$.userId").value(studentUser.getId()))
                .andExpect(jsonPath("$.type").value("GENERAL_CHAT"))
                .andExpect(jsonPath("$.active").value(true));
    }

    // 2. List Conversations tests
    @Test
    void listConversations_Authenticated_ReturnsActiveConversationsOnly() throws Exception {
        // Create an inactive conversation for student
        Conversation inactiveConv = Conversation.builder()
                .userId(studentUser.getId())
                .title("Archived Chat")
                .type(ConversationType.GENERAL_CHAT)
                .status(ConversationStatus.DELETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        conversationRepository.save(inactiveConv);

        mockMvc.perform(get("/api/v1/ai/conversations")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(studentConversation.getId()))
                .andExpect(jsonPath("$[0].title").value("My Study Chat"));
    }

    // 3. Rename Conversation tests
    @Test
    void renameConversation_Owner_ReturnsUpdated() throws Exception {
        UpdateConversationRequest request = UpdateConversationRequest.builder()
                .title("Renamed Academic Chat")
                .build();

        mockMvc.perform(put("/api/v1/ai/conversations/" + studentConversation.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renamed Academic Chat"));

        Conversation updated = conversationRepository.findById(studentConversation.getId()).orElseThrow();
        assertEquals("Renamed Academic Chat", updated.getTitle());
    }

    @Test
    void renameConversation_NonOwner_ReturnsNotFound() throws Exception {
        UpdateConversationRequest request = UpdateConversationRequest.builder()
                .title("Malicious Rename")
                .build();

        mockMvc.perform(put("/api/v1/ai/conversations/" + studentConversation.getId())
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // 4. Soft Delete tests
    @Test
    void deleteConversation_Owner_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/ai/conversations/" + studentConversation.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());

        Conversation deleted = conversationRepository.findById(studentConversation.getId()).orElseThrow();
        assertEquals(ConversationStatus.DELETED, deleted.getStatus());
    }

    @Test
    void deleteConversation_NonOwner_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/ai/conversations/" + studentConversation.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isNotFound());

        Conversation deleted = conversationRepository.findById(studentConversation.getId()).orElseThrow();
        assertEquals(ConversationStatus.ACTIVE, deleted.getStatus());
    }

    // 5. Send Message tests
    @Test
    void sendMessage_Owner_ReturnsCreated() throws Exception {
        SendMessageRequest request = SendMessageRequest.builder()
                .role(MessageRole.USER)
                .content("What is my GPA?")
                .build();

        mockMvc.perform(post("/api/v1/ai/conversations/" + studentConversation.getId() + "/messages")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("What is my GPA?"))
                .andExpect(jsonPath("$.role").value("USER"));

        long count = messageRepository.countByConversationId(studentConversation.getId());
        assertEquals(1, count);
    }

    @Test
    void sendMessage_NonOwner_ReturnsNotFound() throws Exception {
        SendMessageRequest request = SendMessageRequest.builder()
                .role(MessageRole.USER)
                .content("Attempt access")
                .build();

        mockMvc.perform(post("/api/v1/ai/conversations/" + studentConversation.getId() + "/messages")
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // 6. Get History tests
    @Test
    void getConversationHistory_Owner_ReturnsHistory() throws Exception {
        Message msg1 = Message.builder()
                .conversationId(studentConversation.getId())
                .role(MessageRole.USER)
                .content("Question 1")
                .timestamp(Instant.now().minusSeconds(5))
                .build();
        Message msg2 = Message.builder()
                .conversationId(studentConversation.getId())
                .role(MessageRole.ASSISTANT)
                .content("Answer 1")
                .timestamp(Instant.now())
                .build();
        messageRepository.saveAll(List.of(msg1, msg2));

        mockMvc.perform(get("/api/v1/ai/conversations/" + studentConversation.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversation.id").value(studentConversation.getId()))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].content").value("Question 1"))
                .andExpect(jsonPath("$.messages[1].content").value("Answer 1"));
    }

    @Test
    void getConversationHistory_NonOwner_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/ai/conversations/" + studentConversation.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isNotFound());
    }
}
