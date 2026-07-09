package com.campusguide.modules.resource.controller;

import com.campusguide.exception.BadRequestException;
import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.ResourceResponse;
import com.campusguide.modules.resource.dto.ResourceSummaryResponse;
import com.campusguide.modules.resource.dto.UpdateResourceRequest;
import com.campusguide.modules.resource.service.ResourceDownloadService;
import com.campusguide.modules.resource.service.ResourceService;
import com.campusguide.modules.resource.service.ResourceUploadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceDownloadService resourceDownloadService;
    private final ResourceUploadService resourceUploadService;
    private final Validator validator;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResourceResponse> createResource(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @ModelAttribute CreateResourceRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }

        Set<ConstraintViolation<CreateResourceRequest>> violations = validator.validate(request);
        for (ConstraintViolation<CreateResourceRequest> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            if (!Set.of("fileName", "originalFileName", "fileType", "fileSize").contains(propertyPath)) {
                throw new BadRequestException(violation.getMessage());
            }
        }

        ResourceResponse response = resourceUploadService.uploadResource(userDetails, file, request);
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

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResourceSummaryResponse>> getRecentResources() {
        List<ResourceSummaryResponse> response = resourceService.getRecentResources();
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

    @GetMapping("/download/{resourceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadResource(@PathVariable String resourceId) {
        return resourceDownloadService.downloadResource(resourceId);
    }
}
