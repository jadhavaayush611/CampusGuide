package com.campusguide.modules.community.controller;

import com.campusguide.modules.community.dto.CommunityResponse;
import com.campusguide.modules.community.dto.CommunitySummaryResponse;
import com.campusguide.modules.community.dto.CreateCommunityRequest;
import com.campusguide.modules.community.dto.UpdateCommunityRequest;
import com.campusguide.modules.community.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommunityResponse> createCommunity(@Valid @RequestBody CreateCommunityRequest request) {
        CommunityResponse response = communityService.createCommunity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{communityId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable String communityId,
            @Valid @RequestBody UpdateCommunityRequest request) {
        CommunityResponse response = communityService.updateCommunity(communityId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommunitySummaryResponse>> getAllCommunities() {
        List<CommunitySummaryResponse> response = communityService.getAllCommunities();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{communityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommunityResponse> getCommunityById(@PathVariable String communityId) {
        CommunityResponse response = communityService.getCommunityById(communityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/councils/{councilId}/communities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommunitySummaryResponse>> getCommunitiesByCouncil(@PathVariable String councilId) {
        List<CommunitySummaryResponse> response = communityService.getCommunitiesByCouncil(councilId);
        return ResponseEntity.ok(response);
    }
}
