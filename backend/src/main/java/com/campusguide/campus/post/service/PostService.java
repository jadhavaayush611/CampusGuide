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
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final CurrentUserService currentUserService;

    /**
     * Creates a new post.
     *
     * @param userDetails the authenticated user details
     * @param request the request containing post details
     * @return the created post details
     * @throws ResourceNotFoundException if the community or user does not exist
     */
    public PostResponse createPost(UserDetails userDetails, CreatePostRequest request) {
        User user = currentUserService.getCurrentUser(userDetails);

        if (!communityRepository.existsById(request.getCommunityId())) {
            throw new ResourceNotFoundException("Community not found with id: " + request.getCommunityId());
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .communityId(request.getCommunityId())
                .imageUrls(request.getImageUrls())
                .authorId(user.getId())
                .likeCount(0)
                .commentCount(0)
                .isPinned(false)
                .isEdited(false)
                .isDeleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        post = postRepository.save(post);
        return toPostResponse(post);
    }

    /**
     * Updates an existing post.
     *
     * @param userDetails the authenticated user details
     * @param postId the ID of the post to update
     * @param request the request containing updated fields
     * @return the updated post details
     * @throws ResourceNotFoundException if the post does not exist or is soft-deleted
     * @throws AccessDeniedException if the user is not the author and not a SUPER_ADMIN
     */
    public PostResponse updatePost(UserDetails userDetails, String postId, UpdatePostRequest request) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        User user = currentUserService.getCurrentUser(userDetails);

        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isAuthor && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to update this post");
        }

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getImageUrls() != null) {
            post.setImageUrls(request.getImageUrls());
        }

        post.setIsEdited(true);
        post.setUpdatedAt(Instant.now());

        post = postRepository.save(post);
        return toPostResponse(post);
    }

    /**
     * Soft deletes a post.
     *
     * @param userDetails the authenticated user details
     * @param postId the ID of the post to delete
     * @throws ResourceNotFoundException if the post does not exist or is already soft-deleted
     * @throws AccessDeniedException if the user is not the author and not a SUPER_ADMIN
     */
    public void deletePost(UserDetails userDetails, String postId) {
        if (userDetails == null) {
            throw new UnauthorisedException("User is not authenticated");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        User user = currentUserService.getCurrentUser(userDetails);

        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isSuperAdmin = user.getRole() == Role.SUPER_ADMIN;

        if (!isAuthor && !isSuperAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this post");
        }

        post.setIsDeleted(true);
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);
    }

    /**
     * Retrieves a post by its ID.
     *
     * @param postId the ID of the post to retrieve
     * @return the post details
     * @throws ResourceNotFoundException if the post does not exist or is soft-deleted
     */
    public PostResponse getPostById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        return toPostResponse(post);
    }

    /**
     * Retrieves all active posts in a community.
     *
     * @param communityId the ID of the community
     * @return a list of summaries of active posts in the community
     * @throws ResourceNotFoundException if the community does not exist
     */
    public List<PostSummaryResponse> getPostsByCommunity(String communityId) {
        if (!communityRepository.existsById(communityId)) {
            throw new ResourceNotFoundException("Community not found with id: " + communityId);
        }

        return postRepository.findByCommunityIdAndIsDeletedFalse(communityId).stream()
                .map(this::toPostSummaryResponse)
                .toList();
    }

    /**
     * Retrieves all active posts by an author.
     *
     * @param authorId the ID of the author
     * @return a list of summaries of active posts by the author
     * @throws ResourceNotFoundException if the author does not exist
     */
    public List<PostSummaryResponse> getPostsByAuthor(String authorId) {
        currentUserService.getUserByIdentifier(authorId);

        return postRepository.findByAuthorIdAndIsDeletedFalse(authorId).stream()
                .map(this::toPostSummaryResponse)
                .toList();
    }

    /**
     * Retrieves all active posts in the system, sorted newest first.
     *
     * @return a list of summaries of all active posts sorted newest first
     */
    public List<PostSummaryResponse> getAllActivePosts() {
        return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc().stream()
                .map(this::toPostSummaryResponse)
                .toList();
    }

    private PostResponse toPostResponse(Post post) {
        if (post == null) {
            return null;
        }
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorId(post.getAuthorId())
                .communityId(post.getCommunityId())
                .imageUrls(post.getImageUrls())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isPinned(post.getIsPinned())
                .isEdited(post.getIsEdited())
                .createdAt(post.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(post.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .updatedAt(post.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(post.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }

    private PostSummaryResponse toPostSummaryResponse(Post post) {
        if (post == null) {
            return null;
        }
        return PostSummaryResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorId(post.getAuthorId())
                .communityId(post.getCommunityId())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(post.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }
}
