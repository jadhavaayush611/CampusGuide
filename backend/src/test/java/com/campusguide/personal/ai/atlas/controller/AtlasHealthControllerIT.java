package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.AtlasHealthResponse;
import com.campusguide.personal.ai.atlas.dto.SubsystemHealthDto;
import com.campusguide.personal.ai.atlas.service.AtlasHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AtlasHealthControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AtlasHealthService healthService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AtlasHealthService mockHealthService() {
            return mock(AtlasHealthService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        reset(healthService);
    }

    @Test
    void testGetHealth_ReturnsOkWhenUp() throws Exception {
        AtlasHealthResponse response = AtlasHealthResponse.builder()
                .status("UP")
                .subsystemReadiness("READY")
                .timestamp(Instant.now())
                .components(Map.of(
                        "runtime", SubsystemHealthDto.builder().status("UP").details(Map.of()).build(),
                        "orchestrator", SubsystemHealthDto.builder().status("UP").details(Map.of()).build(),
                        "llmProvider", SubsystemHealthDto.builder().status("UP").details(Map.of()).build()
                ))
                .build();

        when(healthService.getHealth()).thenReturn(response);

        mockMvc.perform(get("/api/v1/atlas/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.subsystemReadiness").value("READY"))
                .andExpect(jsonPath("$.components.runtime.status").value("UP"));
    }

    @Test
    void testGetReadiness_ReturnsOkWhenReady() throws Exception {
        AtlasHealthResponse response = AtlasHealthResponse.builder()
                .status("UP")
                .subsystemReadiness("READY")
                .timestamp(Instant.now())
                .build();

        when(healthService.getReadiness()).thenReturn(response);

        mockMvc.perform(get("/api/v1/atlas/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subsystemReadiness").value("READY"));
    }

    @Test
    void testGetLiveness_ReturnsOk() throws Exception {
        AtlasHealthResponse response = AtlasHealthResponse.builder()
                .status("UP")
                .subsystemReadiness("READY")
                .timestamp(Instant.now())
                .build();

        when(healthService.getLiveness()).thenReturn(response);

        mockMvc.perform(get("/api/v1/atlas/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
