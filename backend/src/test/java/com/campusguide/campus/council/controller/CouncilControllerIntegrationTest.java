package com.campusguide.campus.council.controller;

import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.council.dto.CreateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilStatusRequest;
import com.campusguide.campus.council.entity.Council;
import com.campusguide.campus.council.repository.CouncilRepository;
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
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CouncilControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CommunityRepository communityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Council testCouncil;
    private UserDetails adminDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        communityRepository.deleteAll();
        councilRepository.deleteAll();

        adminDetails = User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        testCouncil = Council.builder()
                .id(UUID.randomUUID())
                .name("Technical Council")
                .slug("technical-council")
                .description("Council for technical clubs")
                .logoUrl("https://example.com/logo.png")
                .email("tech@campus.edu")
                .contactNumber("+1234567890")
                .facultyAdvisor("Dr. Alan Turing")
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
    void createCouncil_ReturnsCreated() throws Exception {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Robotics Council")
                .slug("robotics-council")
                .description("Council for robotics and AI clubs")
                .email("robotics@campus.edu")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/v1/councils")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Robotics Council"))
                .andExpect(jsonPath("$.slug").value("robotics-council"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createCouncil_WithInvalidSlug_ReturnsBadRequest() throws Exception {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Invalid Slug Council")
                .slug("Invalid Slug!")
                .description("Description")
                .build();

        mockMvc.perform(post("/api/v1/councils")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCouncil_WithDuplicateName_ReturnsConflict() throws Exception {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Technical Council")
                .slug("different-slug")
                .description("Description")
                .build();

        mockMvc.perform(post("/api/v1/councils")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllCouncils_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/councils")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Technical Council"))
                .andExpect(jsonPath("$[0].slug").value("technical-council"));
    }

    @Test
    void getCouncilById_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/councils/" + testCouncil.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCouncil.getId().toString()))
                .andExpect(jsonPath("$.name").value("Technical Council"));
    }

    @Test
    void getCouncilById_NonExistent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/councils/" + UUID.randomUUID())
                        .with(user(adminDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCouncilBySlug_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/councils/slug/technical-council")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Technical Council"));
    }

    @Test
    void getCouncilBySlug_NonExistent_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/councils/slug/non-existent-slug")
                        .with(user(adminDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCouncil_ReturnsOk() throws Exception {
        UpdateCouncilRequest request = UpdateCouncilRequest.builder()
                .name("Updated Technical Council")
                .slug("updated-technical-council")
                .description("Updated description")
                .logoUrl("https://example.com/new-logo.png")
                .email("tech-updated@campus.edu")
                .contactNumber("+111222333")
                .facultyAdvisor("Dr. Grace Hopper")
                .isActive(true)
                .build();

        mockMvc.perform(put("/api/v1/councils/" + testCouncil.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Technical Council"))
                .andExpect(jsonPath("$.slug").value("updated-technical-council"));
    }

    @Test
    void updateCouncilStatus_ReturnsOk() throws Exception {
        UpdateCouncilStatusRequest request = new UpdateCouncilStatusRequest(false);

        mockMvc.perform(patch("/api/v1/councils/" + testCouncil.getId() + "/status")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteCouncil_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/councils/" + testCouncil.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/councils/" + testCouncil.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCouncil_WithDependentCommunities_ReturnsConflict() throws Exception {
        // Create a dependent community
        Community community = Community.builder()
                .name("Coding Club")
                .description("Learn programming")
                .councilId(testCouncil.getId().toString())
                .isActive(true)
                .build();
        communityRepository.save(community);

        mockMvc.perform(delete("/api/v1/councils/" + testCouncil.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isConflict());
    }
}
