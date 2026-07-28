package com.campusguide.campus.comment.controller;

import com.campusguide.campus.comment.dto.CommentResponse;
import com.campusguide.campus.comment.dto.CommentSummaryResponse;
import com.campusguide.campus.comment.dto.CreateCommentRequest;
import com.campusguide.campus.comment.dto.UpdateCommentRequest;
import com.campusguide.campus.comment.service.CommentService;
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
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentResponse response = commentService.updateComment(userDetails, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String commentId) {
        commentService.deleteComment(userDetails, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable String commentId) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentSummaryResponse>> getCommentsByPost(@PathVariable String postId) {
        List<CommentSummaryResponse> response = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/author/{authorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentSummaryResponse>> getCommentsByAuthor(@PathVariable String authorId) {
        List<CommentSummaryResponse> response = commentService.getCommentsByAuthor(authorId);
        return ResponseEntity.ok(response);
    }
}
