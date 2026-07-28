package com.campusguide.platform.auth.controller;

import com.campusguide.platform.auth.dto.AuthResponse;
import com.campusguide.platform.auth.dto.request.LoginRequest;
import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.auth.service.AuthenticationService;
import com.campusguide.platform.user.dto.UserResponse;
import com.campusguide.platform.user.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@campusguide.com")
                .username("testuser")
                .password("Password123!")
                .build();

        loginRequest = LoginRequest.builder()
                .emailOrUsername("test@campusguide.com")
                .password("Password123!")
                .build();

        authResponse = AuthResponse.builder()
                .token("mocked-jwt-token")
                .email("test@campusguide.com")
                .role(Role.STUDENT)
                .build();

        userResponse = UserResponse.builder()
                .id("user123")
                .email("test@campusguide.com")
                .username("test")
                .role(Role.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .build();
    }

    @Test
    void register_Success() throws Exception {
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@campusguide.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void login_Success() throws Exception {
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@campusguide.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void me_Authenticated_Success() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername("test@campusguide.com")
                .password("password123")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(authenticationService.getCurrentUser("test@campusguide.com")).thenReturn(userResponse);

        try {
            mockMvc.perform(get("/api/v1/auth/me")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("test@campusguide.com"))
                    .andExpect(jsonPath("$.role").value("STUDENT"))
                    .andExpect(jsonPath("$.username").value("test"))
                    .andExpect(jsonPath("$.id").value("user123"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void me_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
