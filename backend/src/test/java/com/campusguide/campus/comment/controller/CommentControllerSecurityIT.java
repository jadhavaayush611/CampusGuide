package com.campusguide.campus.comment.controller;

import com.campusguide.campus.comment.dto.CreateCommentRequest;
import com.campusguide.campus.comment.dto.UpdateCommentRequest;
import com.campusguide.campus.comment.entity.Comment;
import com.campusguide.campus.comment.repository.CommentRepository;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.campus.post.repository.PostRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CommentControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User otherUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails otherDetails;
    private UserDetails adminDetails;

    private Post testPost;

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

        // 3. Save a Test Post
        Post post = Post.builder()
                .title("A Fantastic Post")
                .content("Content of fantastic post")
                .authorId(studentUser.getId())
                .communityId("community-123")
                .commentCount(0)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testPost = postRepository.save(post);
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    // 1. Authenticated user can create a Comment.
    @Test
    void createComment_AuthenticatedUser_ReturnsCreated() throws Exception {
        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("Nice post!")
                .postId(testPost.getId())
                .build();

        mockMvc.perform(post("/api/comments")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("Nice post!"))
                .andExpect(jsonPath("$.authorId").value(studentUser.getId()))
                .andExpect(jsonPath("$.postId").value(testPost.getId()));
    }

    // 2. Unauthenticated user receives 401.
    @Test
    void createComment_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("Nice post!")
                .postId(testPost.getId())
                .build();

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // 3. Author can update own Comment.
    @Test
    void updateComment_Author_ReturnsOk() throws Exception {
        Comment comment = Comment.builder()
                .content("Original content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Updated content")
                .build();

        mockMvc.perform(put("/api/comments/" + comment.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    // 4. Non-owner receives 403.
    @Test
    void updateComment_NonOwner_ReturnsForbidden() throws Exception {
        Comment comment = Comment.builder()
                .content("Original content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Updated content")
                .build();

        mockMvc.perform(put("/api/comments/" + comment.getId())
                        .with(user(otherDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to update this comment"));
    }

    // 5. SUPER_ADMIN can update any Comment.
    @Test
    void updateComment_SuperAdmin_ReturnsOk() throws Exception {
        Comment comment = Comment.builder()
                .content("Original content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Updated content")
                .build();

        mockMvc.perform(put("/api/comments/" + comment.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    // 6. Author can delete own Comment.
    @Test
    void deleteComment_Author_ReturnsNoContent() throws Exception {
        Comment comment = Comment.builder()
                .content("Content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/comments/" + comment.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNoContent());

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertTrue(updatedComment.getIsDeleted());
    }

    // 7. Non-owner receives 403.
    @Test
    void deleteComment_NonOwner_ReturnsForbidden() throws Exception {
        Comment comment = Comment.builder()
                .content("Content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/comments/" + comment.getId())
                        .with(user(otherDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to delete this comment"));

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertFalse(updatedComment.getIsDeleted());
    }

    // 8. SUPER_ADMIN can delete any Comment.
    @Test
    void deleteComment_SuperAdmin_ReturnsNoContent() throws Exception {
        Comment comment = Comment.builder()
                .content("Content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(delete("/api/comments/" + comment.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertTrue(updatedComment.getIsDeleted());
    }

    // 9. GET endpoints require authentication.
    @Test
    void getEndpoints_Unauthenticated_ReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/comments/some-id"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/comments/post/some-post-id"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/comments/author/some-author-id"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getEndpoints_Authenticated_ReturnOk() throws Exception {
        Comment comment = Comment.builder()
                .content("Content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(get("/api/comments/" + comment.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Content"));

        mockMvc.perform(get("/api/comments/post/" + testPost.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/comments/author/" + studentUser.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // 10. Soft-deleted comments behave as not found.
    @Test
    void getComment_SoftDeleted_ReturnsNotFound() throws Exception {
        Comment comment = Comment.builder()
                .content("Deleted content")
                .authorId(studentUser.getId())
                .postId(testPost.getId())
                .isDeleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        mockMvc.perform(get("/api/comments/" + comment.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isNotFound());
    }
}
