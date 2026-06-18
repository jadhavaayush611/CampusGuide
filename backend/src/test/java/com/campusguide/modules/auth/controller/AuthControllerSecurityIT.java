package com.campusguide.modules.auth.controller;

import com.campusguide.modules.auth.dto.AuthResponse;
import com.campusguide.modules.auth.dto.RegisterRequest;
import com.campusguide.modules.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMeWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-jwt-token-value")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMeWithValidToken() throws Exception {
        userRepository.deleteAll();

        // 1. Register a user to get a token
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("flow-test@campusguide.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .department("Science")
                .year(2)
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponseStr = registerResult.getResponse().getContentAsString();
        AuthResponse registerResponse = objectMapper.readValue(registerResponseStr, AuthResponse.class);
        String token = registerResponse.getToken();
        assertNotNull(token);

        // 2. Access /api/auth/me with the token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow-test@campusguide.com"));
    }
}
