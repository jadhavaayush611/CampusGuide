package com.campusguide.campus.academic.roadmap.controller;

import com.campusguide.campus.academic.roadmap.dto.CreateRoadmapRequest;
import com.campusguide.campus.academic.roadmap.dto.UpdateRoadmapRequest;
import com.campusguide.campus.academic.roadmap.entity.Roadmap;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RoadmapControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User otherUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails otherDetails;
    private UserDetails adminDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // 1. Save Users in Repository
        studentUser = User.builder()
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Student")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentUser = userRepository.save(studentUser);

        otherUser = User.builder()
                .email("other@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .firstName("Other")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        otherUser = userRepository.save(otherUser);

        adminUser = User.builder()
                .email("admin@campusguide.com")
                .password("password")
                .role(Role.SUPER_ADMIN)
                .firstName("Admin")
                .lastName("User")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);

        // 2. Build UserDetails for MockMvc authentication helper
        studentDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        otherDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();
    }

    @AfterEach
    void tearDown() {
        roadmapRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- CREATE ROADMAP TESTS ---

    @Test
    void createRoadmap_Student_ReturnsCreated() throws Exception {
        CreateRoadmapRequest request = CreateRoadmapRequest.builder()
                .title("Computer Science Roadmap")
                .description("B.Tech CSE roadmap")
                .degreeProgram("B.Tech")
                .department("Computer Science")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .build();

        mockMvc.perform(post("/api/roadmaps")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Computer Science Roadmap"))
                .andExpect(jsonPath("$.createdBy").value(studentUser.getId()));
    }

    @Test
    void createRoadmap_SuperAdmin_ReturnsCreated() throws Exception {
        CreateRoadmapRequest request = CreateRoadmapRequest.builder()
                .title("Mechanical Engineering Roadmap")
                .description("B.Tech ME roadmap")
                .degreeProgram("B.Tech")
                .department("Mechanical Engineering")
                .totalCredits(170)
                .expectedGraduationYear(2027)
                .build();

        mockMvc.perform(post("/api/roadmaps")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Mechanical Engineering Roadmap"))
                .andExpect(jsonPath("$.createdBy").value(adminUser.getId()));
    }

    @Test
    void createRoadmap_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CreateRoadmapRequest request = CreateRoadmapRequest.builder()
                .title("Unauthenticated Roadmap")
                .description("B.Tech CSE roadmap")
                .degreeProgram("B.Tech")
                .department("Computer Science")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .build();

        mockMvc.perform(post("/api/roadmaps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE ROADMAP TESTS ---

    @Test
    void updateRoadmap_OwnRoadmap_ReturnsOk() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Original Title")
                .description("Original description")
                .degreeProgram("B.Tech")
                .department("CSE")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        UpdateRoadmapRequest request = UpdateRoadmapRequest.builder()
                .title("Updated Title")
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/roadmaps/" + roadmap.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateRoadmap_OtherStudentRoadmap_ReturnsForbidden() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Original Title")
                .description("Original description")
                .degreeProgram("B.Tech")
                .department("CSE")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        UpdateRoadmapRequest request = UpdateRoadmapRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/api/roadmaps/" + roadmap.getId())
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to update this roadmap"));
    }

    @Test
    void updateRoadmap_SuperAdmin_ReturnsOk() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Original Title")
                .description("Original description")
                .degreeProgram("B.Tech")
                .department("CSE")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        UpdateRoadmapRequest request = UpdateRoadmapRequest.builder()
                .title("Updated Title By Admin")
                .build();

        mockMvc.perform(put("/api/roadmaps/" + roadmap.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title By Admin"));
    }

    @Test
    void updateRoadmap_Unauthenticated_ReturnsUnauthorized() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Original Title")
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        UpdateRoadmapRequest request = UpdateRoadmapRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/api/roadmaps/" + roadmap.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE ROADMAP TESTS ---

    @Test
    void deleteRoadmap_OwnRoadmap_ReturnsNoContent() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Title to Delete")
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        mockMvc.perform(delete("/api/roadmaps/" + roadmap.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());

        Roadmap deletedRoadmap = roadmapRepository.findById(roadmap.getId()).orElseThrow();
        assertTrue(deletedRoadmap.getIsDeleted());
    }

    @Test
    void deleteRoadmap_OtherStudentRoadmap_ReturnsForbidden() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Title to Delete")
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        mockMvc.perform(delete("/api/roadmaps/" + roadmap.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to delete this roadmap"));

        Roadmap notDeletedRoadmap = roadmapRepository.findById(roadmap.getId()).orElseThrow();
        assertFalse(notDeletedRoadmap.getIsDeleted());
    }

    @Test
    void deleteRoadmap_SuperAdmin_ReturnsNoContent() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Title to Delete")
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        mockMvc.perform(delete("/api/roadmaps/" + roadmap.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        Roadmap deletedRoadmap = roadmapRepository.findById(roadmap.getId()).orElseThrow();
        assertTrue(deletedRoadmap.getIsDeleted());
    }

    @Test
    void deleteRoadmap_Unauthenticated_ReturnsUnauthorized() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Title to Delete")
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        mockMvc.perform(delete("/api/roadmaps/" + roadmap.getId()))
                .andExpect(status().isUnauthorized());

        Roadmap notDeletedRoadmap = roadmapRepository.findById(roadmap.getId()).orElseThrow();
        assertFalse(notDeletedRoadmap.getIsDeleted());
    }

    // --- GET / RETRIEVAL TESTS ---

    @Test
    void retrievalEndpoints_Authenticated_ReturnOk() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Special CSE Roadmap")
                .description("CSE description")
                .degreeProgram("B.Tech")
                .department("Computer Science")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        // GET /api/roadmaps (Get all)
        mockMvc.perform(get("/api/roadmaps")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/roadmaps/{roadmapId} (Get by ID)
        mockMvc.perform(get("/api/roadmaps/" + roadmap.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Special CSE Roadmap"));

        // GET /api/roadmaps/creator/{userId} (Get by creator)
        mockMvc.perform(get("/api/roadmaps/creator/" + studentUser.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/roadmaps/degree/{degreeProgram} (Get by degree program)
        mockMvc.perform(get("/api/roadmaps/degree/B.Tech")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/roadmaps/department/{department} (Get by department)
        mockMvc.perform(get("/api/roadmaps/department/Computer Science")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void retrievalEndpoints_Unauthenticated_Permitted() throws Exception {
        Roadmap roadmap = Roadmap.builder()
                .title("Special CSE Roadmap")
                .description("CSE description")
                .degreeProgram("B.Tech")
                .department("Computer Science")
                .totalCredits(180)
                .expectedGraduationYear(2028)
                .createdBy(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        roadmap = roadmapRepository.save(roadmap);

        // GET /api/roadmaps (Get all)
        mockMvc.perform(get("/api/roadmaps"))
                .andExpect(status().isOk());

        // GET /api/roadmaps/{roadmapId} (Get by ID)
        mockMvc.perform(get("/api/roadmaps/" + roadmap.getId()))
                .andExpect(status().isOk());

        // GET /api/roadmaps/creator/{userId} (Get by creator)
        mockMvc.perform(get("/api/roadmaps/creator/" + studentUser.getId()))
                .andExpect(status().isOk());

        // GET /api/roadmaps/degree/{degreeProgram} (Get by degree program)
        mockMvc.perform(get("/api/roadmaps/degree/B.Tech"))
                .andExpect(status().isOk());

        // GET /api/roadmaps/department/{department} (Get by department)
        mockMvc.perform(get("/api/roadmaps/department/Computer Science"))
                .andExpect(status().isOk());
    }
}
