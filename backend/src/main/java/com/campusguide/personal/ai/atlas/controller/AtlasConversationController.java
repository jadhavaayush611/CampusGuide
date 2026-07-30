package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.ConversationCreateRequest;
import com.campusguide.personal.ai.atlas.dto.ConversationHistoryResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationResponse;
import com.campusguide.personal.ai.atlas.dto.ConversationUpdateRequest;
import com.campusguide.personal.ai.atlas.service.AtlasConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atlas/conversations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AtlasConversationController {

    private final AtlasConversationService conversationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody ConversationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/conversations request");
        ConversationResponse response = conversationService.createConversation(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResponse>> listConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET /api/v1/atlas/conversations request");
        List<ConversationResponse> response = conversationService.getUserConversations(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET /api/v1/atlas/conversations/{} request", id);
        ConversationResponse response = conversationService.getConversation(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> updateConversation(
            @PathVariable("id") String id,
            @Valid @RequestBody ConversationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received PUT /api/v1/atlas/conversations/{} request", id);
        ConversationResponse response = conversationService.updateConversation(id, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received DELETE /api/v1/atlas/conversations/{} request", id);
        conversationService.deleteConversation(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> archiveConversation(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/conversations/{}/archive request", id);
        ConversationResponse response = conversationService.archiveConversation(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> restoreConversation(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/conversations/{}/restore request", id);
        ConversationResponse response = conversationService.restoreConversation(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rename")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> renameConversation(
            @PathVariable("id") String id,
            @org.springframework.web.bind.annotation.RequestParam("title") String title,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/conversations/{}/rename request", id);
        ConversationResponse response = conversationService.renameConversation(id, title, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse> getSummary(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET /api/v1/atlas/conversations/{}/summary request", id);
        com.campusguide.personal.ai.atlas.dto.ConversationSummaryResponse response = conversationService.getConversationSummary(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/continue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<com.campusguide.personal.ai.atlas.dto.AtlasChatResponse> continueConversation(
            @PathVariable("id") String id,
            @Valid @RequestBody com.campusguide.personal.ai.atlas.dto.AtlasChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/conversations/{}/continue request", id);
        com.campusguide.personal.ai.atlas.dto.AtlasChatResponse response = conversationService.continueConversation(id, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationHistoryResponse> getHistory(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET /api/v1/atlas/conversations/{}/history request", id);
        ConversationHistoryResponse response = conversationService.getConversationHistory(id, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationHistoryResponse> getMessages(
            @PathVariable("id") String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received GET /api/v1/atlas/conversations/{}/messages request", id);
        ConversationHistoryResponse response = conversationService.getConversationHistory(id, userDetails);
        return ResponseEntity.ok(response);
    }
}
