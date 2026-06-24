package com.campusguide.modules.council.controller;

import com.campusguide.modules.council.dto.CreateCouncilRequest;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CouncilControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CouncilRepository councilRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        councilRepository.deleteAll();
    }

    @Test
    void createCouncil_StudentRole_ReturnsForbidden() throws Exception {
        UserDetails studentDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Student Council")
                .description("A test council")
                .category("ACADEMIC")
                .build();

        mockMvc.perform(post("/api/councils")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    void createCouncil_SuperAdminRole_ReturnsCreated() throws Exception {
        UserDetails adminDetails = User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Unique Council Name")
                .description("A test council description")
                .category("ACADEMIC")
                .build();

        mockMvc.perform(post("/api/councils")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Unique Council Name"));
    }

    @Test
    void getCouncils_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/councils")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCouncils_AuthenticatedStudent_ReturnsOk() throws Exception {
        UserDetails studentDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        mockMvc.perform(get("/api/councils")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
