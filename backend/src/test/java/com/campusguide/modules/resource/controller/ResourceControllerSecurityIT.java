package com.campusguide.modules.resource.controller;

import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.council.entity.Council;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.UpdateResourceRequest;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.resource.repository.ResourceRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
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
class ResourceControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CommunityRepository communityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User otherUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails otherDetails;
    private UserDetails adminDetails;

    private Council testCouncil;
    private Community testCommunity;

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

        // 3. Save a Test Council
        Council council = Council.builder()
                .name("Student Council")
                .description("Test Student Council")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCouncil = councilRepository.save(council);

        // 4. Save a Test Community
        Community community = Community.builder()
                .name("Coding Community")
                .description("Test Coding Community")
                .councilId(testCouncil.getId())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCommunity = communityRepository.save(community);
    }

    @AfterEach
    void tearDown() {
        resourceRepository.deleteAll();
        communityRepository.deleteAll();
        councilRepository.deleteAll();
        userRepository.deleteAll();
    }

    // --- Create Resource ---

    @Test
    void createResource_Student_ReturnsCreated() throws Exception {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .title("Lecture Notes")
                .description("Math lecture notes")
                .councilId(testCouncil.getId())
                .communityId(testCommunity.getId())
                .tags(List.of("math", "notes"))
                .fileName("math_notes.pdf")
                .originalFileName("math_notes_v1.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();

        mockMvc.perform(post("/api/resources")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Lecture Notes"))
                .andExpect(jsonPath("$.uploaderId").value(studentUser.getId()))
                .andExpect(jsonPath("$.councilId").value(testCouncil.getId()))
                .andExpect(jsonPath("$.communityId").value(testCommunity.getId()));
    }

    @Test
    void createResource_SuperAdmin_ReturnsCreated() throws Exception {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .title("Lecture Notes")
                .description("Math lecture notes")
                .councilId(testCouncil.getId())
                .communityId(testCommunity.getId())
                .tags(List.of("math", "notes"))
                .fileName("math_notes.pdf")
                .originalFileName("math_notes_v1.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();

        mockMvc.perform(post("/api/resources")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Lecture Notes"))
                .andExpect(jsonPath("$.uploaderId").value(adminUser.getId()))
                .andExpect(jsonPath("$.councilId").value(testCouncil.getId()))
                .andExpect(jsonPath("$.communityId").value(testCommunity.getId()));
    }

    @Test
    void createResource_NoJwt_ReturnsUnauthorized() throws Exception {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .title("Lecture Notes")
                .description("Math lecture notes")
                .fileName("math_notes.pdf")
                .originalFileName("math_notes_v1.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- Update Resource ---

    @Test
    void updateResource_Owner_ReturnsOk() throws Exception {
        Resource resource = Resource.builder()
                .title("Old Title")
                .description("Old description")
                .uploaderId(studentUser.getId())
                .councilId(testCouncil.getId())
                .communityId(testCommunity.getId())
                .tags(List.of("old"))
                .fileName("old.pdf")
                .originalFileName("old.pdf")
                .fileType("application/pdf")
                .fileSize(100L)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        UpdateResourceRequest request = UpdateResourceRequest.builder()
                .title("Updated Title")
                .description("Updated description")
                .tags(List.of("updated"))
                .build();

        mockMvc.perform(put("/api/resources/" + resource.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateResource_NonOwner_ReturnsForbidden() throws Exception {
        Resource resource = Resource.builder()
                .title("Old Title")
                .description("Old description")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        UpdateResourceRequest request = UpdateResourceRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/api/resources/" + resource.getId())
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to update this resource"));
    }

    @Test
    void updateResource_SuperAdmin_ReturnsOk() throws Exception {
        Resource resource = Resource.builder()
                .title("Old Title")
                .description("Old description")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        UpdateResourceRequest request = UpdateResourceRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/api/resources/" + resource.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateResource_NoJwt_ReturnsUnauthorized() throws Exception {
        Resource resource = Resource.builder()
                .title("Old Title")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        UpdateResourceRequest request = UpdateResourceRequest.builder()
                .title("Updated Title")
                .build();

        mockMvc.perform(put("/api/resources/" + resource.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- Delete Resource ---

    @Test
    void deleteResource_Owner_ReturnsNoContent() throws Exception {
        Resource resource = Resource.builder()
                .title("Title")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        mockMvc.perform(delete("/api/resources/" + resource.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());

        Resource updatedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertTrue(updatedResource.getIsDeleted());
    }

    @Test
    void deleteResource_NonOwner_ReturnsForbidden() throws Exception {
        Resource resource = Resource.builder()
                .title("Title")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        mockMvc.perform(delete("/api/resources/" + resource.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to delete this resource"));

        Resource updatedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertFalse(updatedResource.getIsDeleted());
    }

    @Test
    void deleteResource_SuperAdmin_ReturnsNoContent() throws Exception {
        Resource resource = Resource.builder()
                .title("Title")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        mockMvc.perform(delete("/api/resources/" + resource.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        Resource updatedResource = resourceRepository.findById(resource.getId()).orElseThrow();
        assertTrue(updatedResource.getIsDeleted());
    }

    @Test
    void deleteResource_NoJwt_ReturnsUnauthorized() throws Exception {
        Resource resource = Resource.builder()
                .title("Title")
                .uploaderId(studentUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        mockMvc.perform(delete("/api/resources/" + resource.getId()))
                .andExpect(status().isUnauthorized());
    }

    // --- Retrieval ---

    @Test
    void retrievalEndpoints_Authenticated_ReturnOk() throws Exception {
        Resource resource = Resource.builder()
                .title("Special Lecture Notes")
                .uploaderId(studentUser.getId())
                .councilId(testCouncil.getId())
                .communityId(testCommunity.getId())
                .tags(List.of("exam"))
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        resource = resourceRepository.save(resource);

        // GET /api/resources (Get all)
        mockMvc.perform(get("/api/resources")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/resources/{resourceId} (Get by ID)
        mockMvc.perform(get("/api/resources/" + resource.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Special Lecture Notes"));

        // GET /api/resources/uploader/{uploaderId} (Get by uploader)
        mockMvc.perform(get("/api/resources/uploader/" + studentUser.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/resources/council/{councilId} (Get by council)
        mockMvc.perform(get("/api/resources/council/" + testCouncil.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/resources/community/{communityId} (Get by community)
        mockMvc.perform(get("/api/resources/community/" + testCommunity.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/resources/search (Search)
        mockMvc.perform(get("/api/resources/search")
                        .param("query", "Special")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/resources/tag/{tag} (Tag search)
        mockMvc.perform(get("/api/resources/tag/exam")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void retrievalEndpoints_Unauthenticated_ReturnUnauthorized() throws Exception {
        // GET /api/resources
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());

        // GET /api/resources/{resourceId}
        mockMvc.perform(get("/api/resources/some-id"))
                .andExpect(status().isUnauthorized());

        // GET /api/resources/uploader/{uploaderId}
        mockMvc.perform(get("/api/resources/uploader/some-uploader"))
                .andExpect(status().isUnauthorized());

        // GET /api/resources/council/{councilId}
        mockMvc.perform(get("/api/resources/council/some-council"))
                .andExpect(status().isUnauthorized());

        // GET /api/resources/community/{communityId}
        mockMvc.perform(get("/api/resources/community/some-community"))
                .andExpect(status().isUnauthorized());

        // GET /api/resources/search
        mockMvc.perform(get("/api/resources/search").param("query", "notes"))
                .andExpect(status().isUnauthorized());

        // GET /api/resources/tag/{tag}
        mockMvc.perform(get("/api/resources/tag/notes"))
                .andExpect(status().isUnauthorized());
    }
}
