package com.campusguide.common.attachment.controller;

import com.campusguide.common.attachment.dto.AttachmentResponse;
import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import com.campusguide.common.attachment.service.AttachmentService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerType") AttachmentOwnerType ownerType,
            @RequestParam("ownerId") UUID ownerId) {

        AttachmentResponse response = attachmentService.uploadAttachment(userDetails, file, ownerType, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsForOwner(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("ownerType") AttachmentOwnerType ownerType,
            @RequestParam("ownerId") UUID ownerId) {

        List<AttachmentResponse> responses = attachmentService.getAttachmentsForOwner(userDetails, ownerType, ownerId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentResponse> getAttachmentById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {

        AttachmentResponse response = attachmentService.getAttachmentById(userDetails, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam(value = "inline", defaultValue = "false") boolean inline) {

        return attachmentService.downloadAttachment(userDetails, id, inline);
    }

    @GetMapping("/{id}/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> viewAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {

        return attachmentService.downloadAttachment(userDetails, id, true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAttachment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {

        attachmentService.deleteAttachment(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}
