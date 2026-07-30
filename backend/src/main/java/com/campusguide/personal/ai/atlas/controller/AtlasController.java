package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.dto.CapabilityResponse;
import com.campusguide.personal.ai.atlas.service.AtlasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/atlas")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AtlasController {

    private final AtlasService atlasService;
    private final com.campusguide.personal.ai.atlas.streaming.AtlasStreamingService streamingService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AtlasChatResponse> chat(
            @Valid @RequestBody AtlasChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/chat request");
        AtlasChatResponse response = atlasService.chat(request, userDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/chat/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamChat(
            @Valid @RequestBody AtlasChatRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/chat/stream request");
        return streamingService.streamChat(request, lastEventId, userDetails);
    }

    @GetMapping("/capabilities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CapabilityResponse> getCapabilities() {
        log.info("Received GET /api/v1/atlas/capabilities request");
        CapabilityResponse response = atlasService.getCapabilities();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CapabilityResponse> getOperationalInfo() {
        log.info("Received GET /api/v1/atlas/info request");
        CapabilityResponse response = atlasService.getOperationalInfo();
        return ResponseEntity.ok(response);
    }
}
