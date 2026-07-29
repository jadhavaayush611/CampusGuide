package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.dto.AtlasChatResponse;
import com.campusguide.personal.ai.atlas.service.AtlasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/v1/atlas")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AtlasController {

    private final AtlasService atlasService;

    @PostMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AtlasChatResponse> chat(
            @Valid @RequestBody AtlasChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Received POST /api/v1/atlas/chat request");
        AtlasChatResponse response = atlasService.chat(request, userDetails);
        return ResponseEntity.ok(response);
    }
}
