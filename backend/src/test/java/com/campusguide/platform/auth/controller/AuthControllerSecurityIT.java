package com.campusguide.platform.auth.controller;

import com.campusguide.platform.auth.dto.AuthResponse;
import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.user.repository.UserRepository;
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
        mockMvc.perform(get("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMeWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-jwt-token-value")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testMeWithValidToken() throws Exception {
        userRepository.deleteAll();

        // 1. Register a user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("flow-test@campusguide.com")
                .username("flowtest")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername("flow-test@campusguide.com")
                        .password("password123")
                        .authorities(java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                        .build();

        // 2. Access /api/v1/auth/me with user principal
        mockMvc.perform(get("/api/v1/auth/me")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow-test@campusguide.com"));
    }
}
