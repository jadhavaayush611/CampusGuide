package com.campusguide.modules.resource.controller;

import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.ResourceResponse;
import com.campusguide.modules.resource.dto.ResourceSummaryResponse;
import com.campusguide.modules.resource.dto.UpdateResourceRequest;
import com.campusguide.modules.resource.service.ResourceService;
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
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResourceResponse> createResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateResourceRequest request) {
        ResourceResponse response = resourceService.createResource(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResourceResponse> updateResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String resourceId,
            @Valid @RequestBody UpdateResourceRequest request) {
        ResourceResponse response = resourceService.updateResource(userDetails, resourceId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String resourceId) {
        resourceService.deleteResource(userDetails, resourceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> getAllResources() {
        List<ResourceSummaryResponse> response = resourceService.getAllResources();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> searchResources(@RequestParam String query) {
        List<ResourceSummaryResponse> response = resourceService.searchResources(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/uploader/{uploaderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> getResourcesByUploader(@PathVariable String uploaderId) {
        List<ResourceSummaryResponse> response = resourceService.getResourcesByUploader(uploaderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/council/{councilId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> getResourcesByCouncil(@PathVariable String councilId) {
        List<ResourceSummaryResponse> response = resourceService.getResourcesByCouncil(councilId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/community/{communityId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> getResourcesByCommunity(@PathVariable String communityId) {
        List<ResourceSummaryResponse> response = resourceService.getResourcesByCommunity(communityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tag/{tag}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> getResourcesByTag(@PathVariable String tag) {
        List<ResourceSummaryResponse> response = resourceService.getResourcesByTag(tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable String resourceId) {
        ResourceResponse response = resourceService.getResourceById(resourceId);
        return ResponseEntity.ok(response);
    }
}
