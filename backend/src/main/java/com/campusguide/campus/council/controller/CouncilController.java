package com.campusguide.campus.council.controller;

import com.campusguide.campus.council.dto.CouncilResponse;
import com.campusguide.campus.council.dto.CreateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilStatusRequest;
import com.campusguide.campus.council.service.CouncilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/councils")
@RequiredArgsConstructor
public class CouncilController {

    private final CouncilService councilService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CouncilResponse> createCouncil(@Valid @RequestBody CreateCouncilRequest request) {
        CouncilResponse response = councilService.createCouncil(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CouncilResponse>> getAllCouncils() {
        List<CouncilResponse> response = councilService.getAllCouncils();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponse> getCouncilById(@PathVariable UUID id) {
        CouncilResponse response = councilService.getCouncilById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponse> getCouncilBySlug(@PathVariable String slug) {
        CouncilResponse response = councilService.getCouncilBySlug(slug);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CouncilResponse> updateCouncil(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCouncilRequest request) {
        CouncilResponse response = councilService.updateCouncil(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CouncilResponse> updateCouncilStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCouncilStatusRequest request) {
        CouncilResponse response = councilService.updateCouncilStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCouncil(@PathVariable UUID id) {
        councilService.deleteCouncil(id);
        return ResponseEntity.noContent().build();
    }
}
