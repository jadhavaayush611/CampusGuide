package com.campusguide.personal.ai.atlas.controller;

import com.campusguide.personal.ai.atlas.dto.AtlasHealthResponse;
import com.campusguide.personal.ai.atlas.service.AtlasHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/atlas")
@RequiredArgsConstructor
@Slf4j
public class AtlasHealthController {

    private final AtlasHealthService healthService;

    @GetMapping("/health")
    public ResponseEntity<AtlasHealthResponse> getHealth() {
        log.debug("Received GET /api/v1/atlas/health request");
        AtlasHealthResponse response = healthService.getHealth();
        HttpStatus status = "DOWN".equalsIgnoreCase(response.getStatus()) ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<AtlasHealthResponse> getReadiness() {
        log.debug("Received GET /api/v1/atlas/ready request");
        AtlasHealthResponse response = healthService.getReadiness();
        HttpStatus status = "NOT_READY".equalsIgnoreCase(response.getSubsystemReadiness()) ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/live")
    public ResponseEntity<AtlasHealthResponse> getLiveness() {
        log.debug("Received GET /api/v1/atlas/live request");
        AtlasHealthResponse response = healthService.getLiveness();
        return ResponseEntity.ok(response);
    }
}
