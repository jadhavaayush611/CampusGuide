package com.campusguide.campus.post.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.post.dto.CreatePostRequest;
import com.campusguide.campus.post.dto.PostResponse;
import com.campusguide.campus.post.dto.PostSummaryResponse;
import com.campusguide.campus.post.dto.UpdatePostRequest;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.campus.post.repository.PostRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private UserDetails studentUserDetails;
    private UserDetails superAdminUserDetails;
    private UserDetails otherUserDetails;
    private User studentUser;
    private User superAdminUser;
    private User otherUser;
    private CreatePostRequest createRequest;
    private UpdatePostRequest updateRequest;
    private Post activePost;
    private Post deletedPost;

    @BeforeEach
    void setUp() {
        studentUserDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        superAdminUserDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        otherUserDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        studentUser = User.builder()
                .id("user-student")
                .email("student@campusguide.com")
                .role(Role.STUDENT)
                .build();

        superAdminUser = User.builder()
                .id("user-admin")
                .email("admin@campusguide.com")
                .role(Role.SUPER_ADMIN)
                .build();

        otherUser = User.builder()
                .id("user-other")
                .email("other@campusguide.com")
                .role(Role.STUDENT)
                .build();

        createRequest = CreatePostRequest.builder()
                .title("Test Post Title")
                .content("This is test post content.")
                .communityId("comm-123")
                .imageUrls(List.of("http://example.com/image.png"))
                .build();

        updateRequest = UpdatePostRequest.builder()
                .title("Updated Title")
                .content("Updated content.")
                .imageUrls(List.of("http://example.com/image-updated.png"))
                .build();

        activePost = Post.builder()
                .id("post-789")
                .title("Test Post Title")
                .content("This is test post content.")
                .authorId("user-student")
                .communityId("comm-123")
                .imageUrls(List.of("http://example.com/image.png"))
                .likeCount(0)
                .commentCount(0)
                .isPinned(false)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        deletedPost = Post.builder()
                .id("post-999")
                .title("Deleted Post")
                .content("Deleted content.")
                .authorId("user-student")
                .communityId("comm-123")
                .isDeleted(true)
                .build();
    }

    // --- createPost() Tests ---

    @Test
    void createPost_Successful() {
        when(communityRepository.existsById("comm-123")).thenReturn(true);
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponse response = postService.createPost(studentUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("Test Post Title", response.getTitle());
        assertEquals("This is test post content.", response.getContent());
        assertEquals("user-student", response.getAuthorId());
        assertEquals("comm-123", response.getCommunityId());
        assertFalse(response.getIsPinned());
        assertFalse(response.getIsEdited());
        assertEquals(0, response.getLikeCount());
        assertEquals(0, response.getCommentCount());

        verify(communityRepository).existsById("comm-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_ThrowsResourceNotFoundException_WhenCommunityDoesNotExist() {
        when(communityRepository.existsById("comm-123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.createPost(studentUserDetails, createRequest));

        verify(communityRepository).existsById("comm-123");
        verify(userRepository, never()).findByEmail(anyString());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPost_ThrowsResourceNotFoundException_WhenUserDoesNotExist() {
        when(communityRepository.existsById("comm-123")).thenReturn(true);
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.createPost(studentUserDetails, createRequest));

        verify(communityRepository).existsById("comm-123");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(postRepository, never()).save(any(Post.class));
    }

    // --- updatePost() Tests ---

    @Test
    void updatePost_Successful_AsAuthor() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponse response = postService.updatePost(studentUserDetails, "post-789", updateRequest);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated content.", response.getContent());
        assertTrue(response.getIsEdited());

        verify(postRepository).findById("post-789");
        verify(userRepository).findByEmail("student@campusguide.com");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_Successful_AsSuperAdmin() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("admin@campusguide.com")).thenReturn(Optional.of(superAdminUser));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponse response = postService.updatePost(superAdminUserDetails, "post-789", updateRequest);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertTrue(response.getIsEdited());

        verify(postRepository).findById("post-789");
        verify(userRepository).findByEmail("admin@campusguide.com");
    }

    @Test
    void updatePost_ThrowsAccessDeniedException_WhenNotAuthorAndNotAdmin() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("other@campusguide.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> postService.updatePost(otherUserDetails, "post-789", updateRequest));

        verify(postRepository).findById("post-789");
        verify(userRepository).findByEmail("other@campusguide.com");
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_ThrowsResourceNotFoundException_WhenPostIsDeleted() {
        when(postRepository.findById("post-999")).thenReturn(Optional.of(deletedPost));

        assertThrows(ResourceNotFoundException.class, () -> postService.updatePost(studentUserDetails, "post-999", updateRequest));

        verify(postRepository).findById("post-999");
        verify(postRepository, never()).save(any(Post.class));
    }

    // --- deletePost() Tests ---

    @Test
    void deletePost_Successful_AsAuthor() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("student@campusguide.com")).thenReturn(Optional.of(studentUser));

        postService.deletePost(studentUserDetails, "post-789");

        assertTrue(activePost.getIsDeleted());
        verify(postRepository).findById("post-789");
        verify(postRepository).save(activePost);
    }

    @Test
    void deletePost_Successful_AsSuperAdmin() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("admin@campusguide.com")).thenReturn(Optional.of(superAdminUser));

        postService.deletePost(superAdminUserDetails, "post-789");

        assertTrue(activePost.getIsDeleted());
        verify(postRepository).findById("post-789");
        verify(postRepository).save(activePost);
    }

    @Test
    void deletePost_ThrowsAccessDeniedException_WhenNotAuthorAndNotAdmin() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("other@campusguide.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> postService.deletePost(otherUserDetails, "post-789"));

        verify(postRepository, never()).save(any(Post.class));
    }

    // --- getPostById() Tests ---

    @Test
    void getPostById_Successful() {
        when(postRepository.findById("post-789")).thenReturn(Optional.of(activePost));

        PostResponse response = postService.getPostById("post-789");

        assertNotNull(response);
        assertEquals("post-789", response.getId());
        assertEquals("Test Post Title", response.getTitle());
    }

    @Test
    void getPostById_ThrowsResourceNotFoundException_WhenDeleted() {
        when(postRepository.findById("post-999")).thenReturn(Optional.of(deletedPost));

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById("post-999"));
    }

    // --- getPostsByCommunity() Tests ---

    @Test
    void getPostsByCommunity_Successful() {
        when(communityRepository.existsById("comm-123")).thenReturn(true);
        when(postRepository.findByCommunityIdAndIsDeletedFalse("comm-123")).thenReturn(List.of(activePost));

        List<PostSummaryResponse> responses = postService.getPostsByCommunity("comm-123");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("post-789", responses.get(0).getId());
        assertEquals("Test Post Title", responses.get(0).getTitle());
    }

    @Test
    void getPostsByCommunity_ThrowsResourceNotFoundException_WhenCommunityDoesNotExist() {
        when(communityRepository.existsById("comm-123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostsByCommunity("comm-123"));
    }

    // --- getPostsByAuthor() Tests ---

    @Test
    void getPostsByAuthor_Successful() {
        when(userRepository.existsById("user-student")).thenReturn(true);
        when(postRepository.findByAuthorIdAndIsDeletedFalse("user-student")).thenReturn(List.of(activePost));

        List<PostSummaryResponse> responses = postService.getPostsByAuthor("user-student");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("post-789", responses.get(0).getId());
    }

    @Test
    void getPostsByAuthor_ThrowsResourceNotFoundException_WhenAuthorDoesNotExist() {
        when(userRepository.existsById("user-student")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostsByAuthor("user-student"));
    }

    // --- getAllActivePosts() Tests ---

    @Test
    void getAllActivePosts_Successful() {
        when(postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activePost));

        List<PostSummaryResponse> responses = postService.getAllActivePosts();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("post-789", responses.get(0).getId());
    }
}
