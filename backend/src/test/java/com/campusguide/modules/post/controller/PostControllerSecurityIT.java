package com.campusguide.modules.post.controller;

import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.post.dto.CreatePostRequest;
import com.campusguide.modules.post.dto.UpdatePostRequest;
import com.campusguide.modules.post.entity.Post;
import com.campusguide.modules.post.repository.PostRepository;
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
class PostControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User otherUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails otherDetails;
    private UserDetails adminDetails;

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

        // 3. Save a Test Community
        Community community = Community.builder()
                .name("Coding Community")
                .description("Test Coding Community")
                .councilId("council-123")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCommunity = communityRepository.save(community);
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();
    }

    // 1. Authenticated user can create a Post.
    @Test
    void createPost_AuthenticatedUser_ReturnsCreated() throws Exception {
        CreatePostRequest request = CreatePostRequest.builder()
                .title("A Wonderful Post")
                .content("Content of wonderful post")
                .communityId(testCommunity.getId())
                .imageUrls(List.of("http://example.com/img.png"))
                .build();

        mockMvc.perform(post("/api/posts")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("A Wonderful Post"))
                .andExpect(jsonPath("$.content").value("Content of wonderful post"))
                .andExpect(jsonPath("$.authorId").value(studentUser.getId()))
                .andExpect(jsonPath("$.communityId").value(testCommunity.getId()));
    }

    // 2. Unauthenticated user receives 401.
    @Test
    void createPost_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        CreatePostRequest request = CreatePostRequest.builder()
                .title("A Wonderful Post")
                .content("Content of wonderful post")
                .communityId(testCommunity.getId())
                .build();

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // 3. Author can update own Post.
    @Test
    void updatePost_Author_ReturnsOk() throws Exception {
        Post post = Post.builder()
                .title("Original Title")
                .content("Original content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated content")
                .build();

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    // 4. Non-owner cannot update another user's Post.
    @Test
    void updatePost_NonOwner_ReturnsForbidden() throws Exception {
        Post post = Post.builder()
                .title("Original Title")
                .content("Original content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated content")
                .build();

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to update this post"));
    }

    // 5. SUPER_ADMIN can update any Post.
    @Test
    void updatePost_SuperAdmin_ReturnsOk() throws Exception {
        Post post = Post.builder()
                .title("Original Title")
                .content("Original content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated content")
                .build();

        mockMvc.perform(put("/api/posts/" + post.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    // 6. Author can delete own Post.
    @Test
    void deletePost_Author_ReturnsNoContent() throws Exception {
        Post post = Post.builder()
                .title("Title")
                .content("Content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertTrue(updatedPost.getIsDeleted());
    }

    // 7. Non-owner cannot delete another user's Post.
    @Test
    void deletePost_NonOwner_ReturnsForbidden() throws Exception {
        Post post = Post.builder()
                .title("Title")
                .content("Content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to delete this post"));

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertFalse(updatedPost.getIsDeleted());
    }

    // 8. SUPER_ADMIN can delete any Post.
    @Test
    void deletePost_SuperAdmin_ReturnsNoContent() throws Exception {
        Post post = Post.builder()
                .title("Title")
                .content("Content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/" + post.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
        assertTrue(updatedPost.getIsDeleted());
    }

    // 9. GET endpoints require authentication.
    @Test
    void getEndpoints_Unauthenticated_ReturnUnauthorized() throws Exception {
        // GET /api/posts
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isUnauthorized());

        // GET /api/posts/{postId}
        mockMvc.perform(get("/api/posts/some-id"))
                .andExpect(status().isUnauthorized());

        // GET /api/posts/community/{communityId}
        mockMvc.perform(get("/api/posts/community/some-community-id"))
                .andExpect(status().isUnauthorized());

        // GET /api/posts/author/{authorId}
        mockMvc.perform(get("/api/posts/author/some-author-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEndpoints_Authenticated_ReturnOk() throws Exception {
        Post post = Post.builder()
                .title("Title")
                .content("Content")
                .authorId(studentUser.getId())
                .communityId(testCommunity.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        post = postRepository.save(post);

        // GET /api/posts
        mockMvc.perform(get("/api/posts")
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/posts/{postId}
        mockMvc.perform(get("/api/posts/" + post.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));

        // GET /api/posts/community/{communityId}
        mockMvc.perform(get("/api/posts/community/" + testCommunity.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // GET /api/posts/author/{authorId}
        mockMvc.perform(get("/api/posts/author/" + studentUser.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
