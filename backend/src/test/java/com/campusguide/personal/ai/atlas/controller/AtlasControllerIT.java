package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.AtlasUsageDto;
import com.campusguide.personal.ai.atlas.exception.AtlasProviderUnavailableException;
import com.campusguide.personal.ai.atlas.exception.AtlasTimeoutException;
import com.campusguide.personal.ai.atlas.service.AtlasService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AtlasControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AtlasService atlasService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AtlasService mockAtlasService() {
            return mock(AtlasService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        reset(atlasService);
    }

    @Test
    void testChat_Unauthenticated_ReturnsUnauthorized() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Hello Atlas")
                .build();

        mockMvc.perform(post("/api/v1/atlas/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testChat_Authenticated_ReturnsOk() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("What courses should I take next semester?")
                .build();

        AtlasChatResponse mockResponse = AtlasChatResponse.builder()
                .id("atlas-123")
                .content("I recommend Data Structures and Algorithms.")
                .role("assistant")
                .model("gpt-4o-mini")
                .finishReason("stop")
                .usage(AtlasUsageDto.builder().promptTokens(10).completionTokens(15).totalTokens(25).build())
                .timestamp(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(atlasService.chat(any(AtlasChatRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/atlas/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("atlas-123"))
                .andExpect(jsonPath("$.content").value("I recommend Data Structures and Algorithms."))
                .andExpect(jsonPath("$.model").value("gpt-4o-mini"))
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.usage.totalTokens").value(25));
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testChat_EmptyPrompt_ReturnsBadRequest() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("   ")
                .build();

        mockMvc.perform(post("/api/v1/atlas/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testChat_ProviderUnavailable_ReturnsServiceUnavailable() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Hello Atlas")
                .build();

        when(atlasService.chat(any(AtlasChatRequest.class), any()))
                .thenThrow(new AtlasProviderUnavailableException("OpenAI provider disabled"));

        mockMvc.perform(post("/api/v1/atlas/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("OpenAI provider disabled"));
    }

    @Test
    @WithMockUser(username = "student", roles = {"STUDENT"})
    void testChat_ProviderTimeout_ReturnsGatewayTimeout() throws Exception {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Hello Atlas")
                .build();

        when(atlasService.chat(any(AtlasChatRequest.class), any()))
                .thenThrow(new AtlasTimeoutException("OpenAI request timed out"));

        mockMvc.perform(post("/api/v1/atlas/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.error").value("OpenAI request timed out"));
    }
}
