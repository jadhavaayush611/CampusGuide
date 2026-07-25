package com.campusguide.personal.ai.controller;

import com.campusguide.personal.ai.dto.request.ChatRequest;
import com.campusguide.personal.ai.dto.request.CreateConversationRequest;
import com.campusguide.personal.ai.dto.request.SendMessageRequest;
import com.campusguide.personal.ai.dto.request.UpdateConversationRequest;
import com.campusguide.personal.ai.dto.response.ChatResponse;
import com.campusguide.personal.ai.dto.response.ConversationHistoryResponse;
import com.campusguide.personal.ai.dto.response.ConversationResponse;
import com.campusguide.personal.ai.dto.response.ConversationSummaryResponse;
import com.campusguide.personal.ai.dto.response.MessageResponse;
import com.campusguide.personal.ai.service.interfaces.AiService;
import com.campusguide.personal.ai.service.interfaces.ConversationService;
import com.campusguide.personal.ai.service.interfaces.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/conversations")
@RequiredArgsConstructor
@Validated
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final AiService aiService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> createConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateConversationRequest request) {
        ConversationResponse response = conversationService.createConversation(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationSummaryResponse>> listConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ConversationSummaryResponse> response = conversationService.listConversations(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationHistoryResponse> getConversationHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        ConversationHistoryResponse response = messageService.getConversationHistory(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponse> renameConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody UpdateConversationRequest request) {
        ConversationResponse response = conversationService.renameConversation(userDetails, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        conversationService.deleteConversation(userDetails, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") String conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.saveMessage(userDetails, conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") String conversationId,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiService.chat(userDetails, conversationId, request);
        return ResponseEntity.ok(response);
    }
}
