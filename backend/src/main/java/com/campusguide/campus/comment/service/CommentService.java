package com.campusguide.campus.comment.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.campus.comment.dto.CommentResponse;
import com.campusguide.campus.comment.dto.CommentSummaryResponse;
import com.campusguide.campus.comment.dto.CreateCommentRequest;
import com.campusguide.campus.comment.dto.UpdateCommentRequest;
import com.campusguide.campus.comment.entity.Comment;
import com.campusguide.campus.comment.repository.CommentRepository;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.campus.post.repository.PostRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CurrentUserService currentUserService;

    /**
     * Creates a new comment.
     *
     * @param userDetails the authenticated user details
     * @param request the request containing comment details
     * @return the created comment details
     * @throws UnauthorisedException if user is not authenticated
     * @throws ResourceNotFoundException if the post or user does not exist
     */
    public CommentResponse createComment(UserDetails userDetails, CreateCommentRequest request) {
        User user = currentUserService.getCurrentUser(userDetails);

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + request.getPostId()));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + request.getPostId());
        }

        Comment comment = Comment.builder()
                .postId(post.getId())
                .authorId(user.getId())
                .content(request.getContent())
                .isEdited(false)
                .isDeleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        comment = commentRepository.save(comment);

        int currentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        post.setCommentCount(currentCount + 1);
        postRepository.save(post);

        return toCommentResponse(comment);
    }

    /**
     * Updates an existing comment.
     *
     * @param userDetails the authenticated user details
     * @param commentId the ID of the comment to update
     * @param request the request containing updated fields
     * @return the updated comment details
     * @throws UnauthorisedException if user is not authenticated
     * @throws ResourceNotFoundException if the comment does not exist or is soft-deleted
     * @throws AccessDeniedException if the user is not the author and not a SUPER_ADMIN
     */
    public CommentResponse updateComment(UserDetails userDetails, String commentId, UpdateCommentRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        User user = currentUserService.getCurrentUser(userDetails);

        boolean isAuthor = comment.getAuthorId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isAuthor && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to update this comment");
        }

        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }

        comment.setIsEdited(true);
        comment.setUpdatedAt(Instant.now());

        comment = commentRepository.save(comment);
        return toCommentResponse(comment);
    }

    /**
     * Soft deletes a comment and decrements the post's comment count.
     *
     * @param userDetails the authenticated user details
     * @param commentId the ID of the comment to delete
     * @throws UnauthorisedException if user is not authenticated
     * @throws ResourceNotFoundException if the comment or parent post does not exist, or if comment is already soft-deleted
     * @throws AccessDeniedException if the user is not the author and not a SUPER_ADMIN
     */
    public void deleteComment(UserDetails userDetails, String commentId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        User user = currentUserService.getCurrentUser(userDetails);

        boolean isAuthor = comment.getAuthorId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isAuthor && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this comment");
        }

        comment.setIsDeleted(true);
        comment.setUpdatedAt(Instant.now());
        commentRepository.save(comment);

        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + comment.getPostId()));

        int currentCount = post.getCommentCount() != null ? post.getCommentCount() : 0;
        post.setCommentCount(Math.max(0, currentCount - 1));
        postRepository.save(post);
    }

    /**
     * Retrieves a comment by its ID.
     *
     * @param commentId the ID of the comment to retrieve
     * @return the comment details
     * @throws ResourceNotFoundException if the comment does not exist or is soft-deleted
     */
    public CommentResponse getCommentById(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        return toCommentResponse(comment);
    }

    /**
     * Retrieves all active comments for a post, ordered oldest first.
     *
     * @param postId the ID of the post
     * @return a list of summaries of active comments for the post
     * @throws ResourceNotFoundException if the post does not exist or is soft-deleted
     */
    public List<CommentSummaryResponse> getCommentsByPost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        return commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId).stream()
                .map(this::toCommentSummaryResponse)
                .toList();
    }

    /**
     * Retrieves all active comments by a specific author.
     *
     * @param authorId the ID of the author
     * @return a list of summaries of active comments by the author
     * @throws ResourceNotFoundException if the author does not exist
     */
    public List<CommentSummaryResponse> getCommentsByAuthor(String authorId) {
        currentUserService.getUserByIdentifier(authorId);

        return commentRepository.findByAuthorIdAndIsDeletedFalse(authorId).stream()
                .map(this::toCommentSummaryResponse)
                .toList();
    }

    private CommentResponse toCommentResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .postId(comment.getPostId())
                .isEdited(comment.getIsEdited())
                .createdAt(comment.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(comment.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .updatedAt(comment.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(comment.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    private CommentSummaryResponse toCommentSummaryResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentSummaryResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .createdAt(comment.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(comment.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }
}
