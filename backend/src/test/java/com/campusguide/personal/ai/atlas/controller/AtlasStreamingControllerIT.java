package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.streaming.AtlasStreamingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AtlasStreamingControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private AtlasStreamingService streamingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testStreamChat_Unauthenticated_ReturnsUnauthorized() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Hello streaming")
                .build();

        mockMvc.perform(post("/api/v1/atlas/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testStreamChat_Authenticated_ReturnsOk() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Hello streaming test")
                .build();

        when(streamingService.streamChat(any(), any(), any())).thenReturn(new SseEmitter());

        mockMvc.perform(post("/api/v1/atlas/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = {"STUDENT"})
    void testStreamChat_WithLastEventIdHeader_ReturnsOk() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Reconnect stream test")
                .build();

        when(streamingService.streamChat(any(), eq("evt-123"), any())).thenReturn(new SseEmitter());

        mockMvc.perform(post("/api/v1/atlas/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Last-Event-ID", "evt-123")
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
