package com.campusguide.modules.comment.service;

import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.comment.dto.CommentResponse;
import com.campusguide.modules.comment.dto.CommentSummaryResponse;
import com.campusguide.modules.comment.dto.CreateCommentRequest;
import com.campusguide.modules.comment.dto.UpdateCommentRequest;
import com.campusguide.modules.comment.entity.Comment;
import com.campusguide.modules.comment.repository.CommentRepository;
import com.campusguide.modules.post.entity.Post;
import com.campusguide.modules.post.repository.PostRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
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
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private UserDetails authorUserDetails;
    private UserDetails adminUserDetails;
    private UserDetails otherUserDetails;

    private User authorUser;
    private User adminUser;
    private User otherUser;

    private Post activePost;
    private Post deletedPost;

    private Comment activeComment;
    private Comment deletedComment;

    private CreateCommentRequest createRequest;
    private UpdateCommentRequest updateRequest;

    @BeforeEach
    void setUp() {
        authorUserDetails = org.springframework.security.core.userdetails.User.withUsername("author@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminUserDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        otherUserDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        authorUser = User.builder()
                .id("user-author")
                .email("author@campusguide.com")
                .role(Role.STUDENT)
                .build();

        adminUser = User.builder()
                .id("user-admin")
                .email("admin@campusguide.com")
                .role(Role.SUPER_ADMIN)
                .build();

        otherUser = User.builder()
                .id("user-other")
                .email("other@campusguide.com")
                .role(Role.STUDENT)
                .build();

        activePost = Post.builder()
                .id("post-123")
                .title("Active Post")
                .content("Content of active post")
                .authorId("user-other")
                .commentCount(2)
                .isDeleted(false)
                .build();

        deletedPost = Post.builder()
                .id("post-deleted")
                .title("Deleted Post")
                .isDeleted(true)
                .commentCount(0)
                .build();

        activeComment = Comment.builder()
                .id("comment-1")
                .postId("post-123")
                .authorId("user-author")
                .content("Original Comment Content")
                .isEdited(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();

        deletedComment = Comment.builder()
                .id("comment-deleted")
                .postId("post-123")
                .authorId("user-author")
                .content("Deleted Comment Content")
                .isEdited(false)
                .isDeleted(true)
                .build();

        createRequest = CreateCommentRequest.builder()
                .postId("post-123")
                .content("This is a new comment.")
                .build();

        updateRequest = UpdateCommentRequest.builder()
                .content("This is an updated comment.")
                .build();
    }

    // --- createComment() Tests ---

    @Test
    void createComment_Successful() {
        when(postRepository.findById("post-123")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("author@campusguide.com")).thenReturn(Optional.of(authorUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId("new-comment-id");
            return c;
        });

        CommentResponse response = commentService.createComment(authorUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("new-comment-id", response.getId());
        assertEquals("This is a new comment.", response.getContent());
        assertEquals("user-author", response.getAuthorId());
        assertEquals("post-123", response.getPostId());
        assertFalse(response.getIsEdited());
        assertNotNull(response.getCreatedAt());

        // Comment count increment check
        assertEquals(3, activePost.getCommentCount());

        verify(postRepository).findById("post-123");
        verify(userRepository).findByEmail("author@campusguide.com");
        verify(commentRepository).save(any(Comment.class));
        verify(postRepository).save(activePost);
    }

    @Test
    void createComment_ThrowsResourceNotFoundException_WhenPostDoesNotExist() {
        when(postRepository.findById("post-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(authorUserDetails, createRequest));

        verify(postRepository).findById("post-123");
        verify(userRepository, never()).findByEmail(anyString());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_ThrowsResourceNotFoundException_WhenPostIsSoftDeleted() {
        when(postRepository.findById("post-deleted")).thenReturn(Optional.of(deletedPost));
        CreateCommentRequest request = CreateCommentRequest.builder()
                .postId("post-deleted")
                .content("Comment on deleted post")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(authorUserDetails, request));

        verify(postRepository).findById("post-deleted");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void createComment_ThrowsResourceNotFoundException_WhenUserDoesNotExist() {
        when(postRepository.findById("post-123")).thenReturn(Optional.of(activePost));
        when(userRepository.findByEmail("author@campusguide.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(authorUserDetails, createRequest));

        verify(postRepository).findById("post-123");
        verify(userRepository).findByEmail("author@campusguide.com");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_ThrowsUnauthorisedException_WhenUserDetailsIsNull() {
        assertThrows(UnauthorisedException.class, () -> commentService.createComment(null, createRequest));
    }

    // --- updateComment() Tests ---

    @Test
    void updateComment_Successful_AsOwner() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("author@campusguide.com")).thenReturn(Optional.of(authorUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentResponse response = commentService.updateComment(authorUserDetails, "comment-1", updateRequest);

        assertNotNull(response);
        assertEquals("This is an updated comment.", response.getContent());
        assertTrue(response.getIsEdited());

        verify(commentRepository).findById("comment-1");
        verify(userRepository).findByEmail("author@campusguide.com");
        verify(commentRepository).save(activeComment);
    }

    @Test
    void updateComment_Successful_AsSuperAdmin() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("admin@campusguide.com")).thenReturn(Optional.of(adminUser));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentResponse response = commentService.updateComment(adminUserDetails, "comment-1", updateRequest);

        assertNotNull(response);
        assertEquals("This is an updated comment.", response.getContent());
        assertTrue(response.getIsEdited());

        verify(commentRepository).findById("comment-1");
        verify(userRepository).findByEmail("admin@campusguide.com");
        verify(commentRepository).save(activeComment);
    }

    @Test
    void updateComment_ThrowsAccessDeniedException_WhenNotOwnerAndNotAdmin() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("other@campusguide.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> commentService.updateComment(otherUserDetails, "comment-1", updateRequest));

        verify(commentRepository).findById("comment-1");
        verify(userRepository).findByEmail("other@campusguide.com");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_ThrowsResourceNotFoundException_WhenCommentIsSoftDeleted() {
        when(commentRepository.findById("comment-deleted")).thenReturn(Optional.of(deletedComment));

        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(authorUserDetails, "comment-deleted", updateRequest));

        verify(commentRepository).findById("comment-deleted");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    // --- deleteComment() Tests ---

    @Test
    void deleteComment_Successful_AsOwner() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("author@campusguide.com")).thenReturn(Optional.of(authorUser));
        when(postRepository.findById("post-123")).thenReturn(Optional.of(activePost));

        commentService.deleteComment(authorUserDetails, "comment-1");

        assertTrue(activeComment.getIsDeleted());
        assertEquals(1, activePost.getCommentCount()); // decremented from 2 to 1

        verify(commentRepository).findById("comment-1");
        verify(userRepository).findByEmail("author@campusguide.com");
        verify(commentRepository).save(activeComment);
        verify(postRepository).findById("post-123");
        verify(postRepository).save(activePost);
    }

    @Test
    void deleteComment_Successful_AsSuperAdmin() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("admin@campusguide.com")).thenReturn(Optional.of(adminUser));
        when(postRepository.findById("post-123")).thenReturn(Optional.of(activePost));

        commentService.deleteComment(adminUserDetails, "comment-1");

        assertTrue(activeComment.getIsDeleted());
        assertEquals(1, activePost.getCommentCount());

        verify(commentRepository).findById("comment-1");
        verify(userRepository).findByEmail("admin@campusguide.com");
        verify(commentRepository).save(activeComment);
        verify(postRepository).findById("post-123");
        verify(postRepository).save(activePost);
    }

    @Test
    void deleteComment_ThrowsAccessDeniedException_WhenNotOwnerAndNotAdmin() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("other@campusguide.com")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(otherUserDetails, "comment-1"));

        verify(commentRepository, never()).save(any(Comment.class));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deleteComment_ThrowsResourceNotFoundException_WhenCommentIsSoftDeleted() {
        when(commentRepository.findById("comment-deleted")).thenReturn(Optional.of(deletedComment));

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(authorUserDetails, "comment-deleted"));

        verify(commentRepository).findById("comment-deleted");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteComment_PreventsNegativeCommentCount() {
        activePost.setCommentCount(0); // set to 0 to test negative prevention
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));
        when(userRepository.findByEmail("author@campusguide.com")).thenReturn(Optional.of(authorUser));
        when(postRepository.findById("post-123")).thenReturn(Optional.of(activePost));

        commentService.deleteComment(authorUserDetails, "comment-1");

        assertTrue(activeComment.getIsDeleted());
        assertEquals(0, activePost.getCommentCount()); // should remain 0, not -1

        verify(commentRepository).save(activeComment);
        verify(postRepository).save(activePost);
    }

    // --- getCommentById() Tests ---

    @Test
    void getCommentById_Successful() {
        when(commentRepository.findById("comment-1")).thenReturn(Optional.of(activeComment));

        CommentResponse response = commentService.getCommentById("comment-1");

        assertNotNull(response);
        assertEquals("comment-1", response.getId());
        assertEquals("Original Comment Content", response.getContent());
    }

    @Test
    void getCommentById_ThrowsResourceNotFoundException_WhenDeleted() {
        when(commentRepository.findById("comment-deleted")).thenReturn(Optional.of(deletedComment));

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById("comment-deleted"));
    }

    // --- getCommentsByPost() Tests ---

    @Test
    void getCommentsByPost_Successful() {
        when(postRepository.findById("post-123")).thenReturn(Optional.of(activePost));
        when(commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc("post-123"))
                .thenReturn(List.of(activeComment));

        List<CommentSummaryResponse> responses = commentService.getCommentsByPost("post-123");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("comment-1", responses.get(0).getId());
        assertEquals("Original Comment Content", responses.get(0).getContent());

        verify(postRepository).findById("post-123");
        verify(commentRepository).findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc("post-123");
    }

    @Test
    void getCommentsByPost_ThrowsResourceNotFoundException_WhenPostIsSoftDeleted() {
        when(postRepository.findById("post-deleted")).thenReturn(Optional.of(deletedPost));

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentsByPost("post-deleted"));

        verify(postRepository).findById("post-deleted");
        verify(commentRepository, never()).findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(anyString());
    }

    // --- getCommentsByAuthor() Tests ---

    @Test
    void getCommentsByAuthor_Successful() {
        when(userRepository.existsById("user-author")).thenReturn(true);
        when(commentRepository.findByAuthorIdAndIsDeletedFalse("user-author"))
                .thenReturn(List.of(activeComment));

        List<CommentSummaryResponse> responses = commentService.getCommentsByAuthor("user-author");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("comment-1", responses.get(0).getId());

        verify(userRepository).existsById("user-author");
        verify(commentRepository).findByAuthorIdAndIsDeletedFalse("user-author");
    }

    @Test
    void getCommentsByAuthor_ThrowsResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.existsById("user-author")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentsByAuthor("user-author"));

        verify(userRepository).existsById("user-author");
        verify(commentRepository, never()).findByAuthorIdAndIsDeletedFalse(anyString());
    }
}
