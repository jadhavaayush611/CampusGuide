package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.ConversationCreateRequest;
import com.campusguide.personal.ai.atlas.dto.ConversationHistoryResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationUpdateRequest;
import com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException;
import com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException;
import com.campusguide.personal.ai.atlas.service.AtlasConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AtlasConversationControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AtlasConversationService conversationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        reset(conversationService);
    }

    @Test
    void testCreateConversation_Unauthenticated_ReturnsUnauthorized() throws Exception {
        ConversationCreateRequest request = ConversationCreateRequest.builder()
                .title("Academic Planning")
                .build();

        mockMvc.perform(post("/api/v1/atlas/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testCreateConversation_Authenticated_ReturnsCreated() throws Exception {
        ConversationCreateRequest request = ConversationCreateRequest.builder()
                .title("Academic Planning")
                .type("ACADEMIC_ADVISING")
                .build();

        ConversationResponse response = ConversationResponse.builder()
                .id("conv-100")
                .userId("user1")
                .title("Academic Planning")
                .type("ACADEMIC_ADVISING")
                .status("ACTIVE")
                .messageCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .metadata(Map.of())
                .build();

        when(conversationService.createConversation(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/atlas/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("conv-100"))
                .andExpect(jsonPath("$.title").value("Academic Planning"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetConversation_Success() throws Exception {
        ConversationResponse response = ConversationResponse.builder()
                .id("conv-100")
                .userId("user1")
                .title("Academic Planning")
                .type("GENERAL")
                .status("ACTIVE")
                .messageCount(2)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(conversationService.getConversation(eq("conv-100"), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/atlas/conversations/conv-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("conv-100"))
                .andExpect(jsonPath("$.title").value("Academic Planning"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetConversation_ForbiddenForNonOwner() throws Exception {
        when(conversationService.getConversation(eq("conv-999"), any()))
                .thenThrow(new AtlasForbiddenException("Access denied: You do not own this conversation"));

        mockMvc.perform(get("/api/v1/atlas/conversations/conv-999"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied: You do not own this conversation"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetConversation_NotFound() throws Exception {
        when(conversationService.getConversation(eq("conv-000"), any()))
                .thenThrow(new AtlasNotFoundException("Conversation not found with id: conv-000"));

        mockMvc.perform(get("/api/v1/atlas/conversations/conv-000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Conversation not found with id: conv-000"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testUpdateConversation_Success() throws Exception {
        ConversationUpdateRequest request = ConversationUpdateRequest.builder()
                .title("Updated Title")
                .status("ACTIVE")
                .build();

        ConversationResponse response = ConversationResponse.builder()
                .id("conv-100")
                .title("Updated Title")
                .status("ACTIVE")
                .build();

        when(conversationService.updateConversation(eq("conv-100"), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/atlas/conversations/conv-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testDeleteConversation_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/atlas/conversations/conv-100"))
                .andExpect(status().isNoContent());

        verify(conversationService).deleteConversation(eq("conv-100"), any());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetHistory_Success() throws Exception {
        ConversationHistoryResponse history = ConversationHistoryResponse.builder()
                .conversationId("conv-100")
                .userId("user1")
                .messages(Collections.emptyList())
                .totalMessages(0)
                .build();

        when(conversationService.getConversationHistory(eq("conv-100"), any())).thenReturn(history);

        mockMvc.perform(get("/api/v1/atlas/conversations/conv-100/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-100"))
                .andExpect(jsonPath("$.totalMessages").value(0));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testArchiveConversation_Success() throws Exception {
        ConversationResponse response = ConversationResponse.builder()
                .id("conv-100")
                .status("ARCHIVED")
                .build();

        when(conversationService.archiveConversation(eq("conv-100"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/atlas/conversations/conv-100/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testRestoreConversation_Success() throws Exception {
        ConversationResponse response = ConversationResponse.builder()
                .id("conv-100")
                .status("ACTIVE")
                .build();

        when(conversationService.restoreConversation(eq("conv-100"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/atlas/conversations/conv-100/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testRenameConversation_Success() throws Exception {
        ConversationResponse response = ConversationResponse.builder()
                .id("conv-100")
                .title("New Title")
                .build();

        when(conversationService.renameConversation(eq("conv-100"), eq("New Title"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/atlas/conversations/conv-100/rename?title=New Title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testGetSummary_Success() throws Exception {
        com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse summary = com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse.builder()
                .conversationId("conv-100")
                .title("Academic Plan")
                .summary("Summary of academic plan")
                .messageCount(3)
                .build();

        when(conversationService.getConversationSummary(eq("conv-100"), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/atlas/conversations/conv-100/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-100"))
                .andExpect(jsonPath("$.summary").value("Summary of academic plan"));
    }
}
