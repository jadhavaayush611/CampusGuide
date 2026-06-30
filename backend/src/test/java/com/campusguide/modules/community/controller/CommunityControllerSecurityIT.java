package com.campusguide.modules.community.controller;

import com.campusguide.modules.community.dto.CreateCommunityRequest;
import com.campusguide.modules.community.dto.UpdateCommunityRequest;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.council.entity.Council;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CommunityControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CommunityRepository communityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Council testCouncil;
    private UserDetails adminDetails;
    private UserDetails studentDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Setup test users
        adminDetails = User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        studentDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        // Setup a test council in the repository
        testCouncil = Council.builder()
                .name("Academic Council")
                .description("Test Academic Council")
                .category("ACADEMIC")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCouncil = councilRepository.save(testCouncil);
    }

    @AfterEach
    void tearDown() {
        communityRepository.deleteAll();
        councilRepository.deleteAll();
    }

    @Test
    void createCommunity_SuperAdminRole_ReturnsCreated() throws Exception {
        CreateCommunityRequest request = CreateCommunityRequest.builder()
                .name("Coding Community")
                .description("Test Coding Community")
                .councilId(testCouncil.getId())
                .bannerUrl("http://example.com/banner.png")
                .build();

        mockMvc.perform(post("/api/communities")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Coding Community"))
                .andExpect(jsonPath("$.councilId").value(testCouncil.getId()));
    }

    @Test
    void createCommunity_StudentRole_ReturnsForbidden() throws Exception {
        CreateCommunityRequest request = CreateCommunityRequest.builder()
                .name("Coding Community")
                .description("Test Coding Community")
                .councilId(testCouncil.getId())
                .build();

        mockMvc.perform(post("/api/communities")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    void createCommunity_InvalidCouncilId_ReturnsNotFound() throws Exception {
        CreateCommunityRequest request = CreateCommunityRequest.builder()
                .name("Coding Community")
                .description("Test Coding Community")
                .councilId("non-existent-council-id")
                .build();

        mockMvc.perform(post("/api/communities")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Council not found with id: non-existent-council-id"));
    }

    @Test
    void createCommunity_DuplicateName_ReturnsConflict() throws Exception {
        // First create one community
        Community existing = Community.builder()
                .name("Duplicate Name")
                .description("First Community")
                .councilId(testCouncil.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        communityRepository.save(existing);

        CreateCommunityRequest request = CreateCommunityRequest.builder()
                .name("Duplicate Name")
                .description("Second Community")
                .councilId(testCouncil.getId())
                .build();

        mockMvc.perform(post("/api/communities")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Community with name 'Duplicate Name' already exists"));
    }

    @Test
    void updateCommunity_SuperAdminRole_ReturnsOk() throws Exception {
        Community existing = Community.builder()
                .name("Original Community")
                .description("Original Description")
                .councilId(testCouncil.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        existing = communityRepository.save(existing);

        UpdateCommunityRequest request = UpdateCommunityRequest.builder()
                .description("Updated Description")
                .isActive(false)
                .build();

        mockMvc.perform(put("/api/communities/" + existing.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void updateCommunity_StudentRole_ReturnsForbidden() throws Exception {
        Community existing = Community.builder()
                .name("Original Community")
                .description("Original Description")
                .councilId(testCouncil.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        existing = communityRepository.save(existing);

        UpdateCommunityRequest request = UpdateCommunityRequest.builder()
                .description("Updated Description")
                .build();

        mockMvc.perform(put("/api/communities/" + existing.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    void getEndpoints_AuthenticatedStudent_ReturnOk() throws Exception {
        Community comm = Community.builder()
                .name("Public Community")
                .description("For everyone")
                .councilId(testCouncil.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comm = communityRepository.save(comm);

        // GET /api/communities
        mockMvc.perform(get("/api/communities")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/communities/{communityId}
        mockMvc.perform(get("/api/communities/" + comm.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Public Community"));

        // GET /api/communities/councils/{councilId}/communities
        mockMvc.perform(get("/api/communities/councils/" + testCouncil.getId() + "/communities")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getEndpoints_Unauthenticated_ReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/communities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/communities/some-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/communities/councils/some-council/communities")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
