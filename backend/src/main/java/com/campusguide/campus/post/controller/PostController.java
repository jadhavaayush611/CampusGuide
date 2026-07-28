package com.campusguide.campus.post.controller;

import com.campusguide.campus.post.dto.CreatePostRequest;
import com.campusguide.campus.post.dto.PostResponse;
import com.campusguide.campus.post.dto.PostSummaryResponse;
import com.campusguide.campus.post.dto.UpdatePostRequest;
import com.campusguide.campus.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.createPost(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId,
            @Valid @RequestBody UpdatePostRequest request) {
        PostResponse response = postService.updatePost(userDetails, postId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String postId) {
        postService.deletePost(userDetails, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PostSummaryResponse>> getAllActivePosts() {
        List<PostSummaryResponse> response = postService.getAllActivePosts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> getPostById(@PathVariable String postId) {
        PostResponse response = postService.getPostById(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/{communityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PostSummaryResponse>> getPostsByCommunity(@PathVariable String communityId) {
        List<PostSummaryResponse> response = postService.getPostsByCommunity(communityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/author/{authorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PostSummaryResponse>> getPostsByAuthor(@PathVariable String authorId) {
        List<PostSummaryResponse> response = postService.getPostsByAuthor(authorId);
        return ResponseEntity.ok(response);
    }
}
