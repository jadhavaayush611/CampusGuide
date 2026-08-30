package com.campusguide.common.attachment.service;

import com.campusguide.common.attachment.dto.AttachmentResponse;
import com.campusguide.common.attachment.entity.Attachment;
import com.campusguide.common.attachment.entity.AttachmentOwnerType;
import com.campusguide.common.attachment.repository.AttachmentRepository;
import com.campusguide.common.attachment.validation.AttachmentValidator;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.storage.StorageService;
import com.campusguide.platform.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final StorageService storageService;
    private final AttachmentValidator validator;
    private final AttachmentAuthorizer authorizer;
    private final CurrentUserService currentUserService;

    public AttachmentResponse uploadAttachment(UserDetails userDetails, MultipartFile file, AttachmentOwnerType ownerType, UUID ownerId) {
        validator.validateUpload(file);
        authorizer.authorizeUpload(userDetails, ownerType, ownerId);

        String currentUserId = currentUserService.getCurrentUserId(userDetails);
        String storedFileName = null;
        try {
            storedFileName = storageService.store(file);
            Attachment attachment = Attachment.builder()
                    .id(UUID.randomUUID())
                    .ownerType(ownerType)
                    .ownerId(ownerId)
                    .uploaderId(currentUserId)
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .contentType(file.getContentType().split(";")[0].trim())
                    .fileSize(file.getSize())
                    .storageReference("storage://" + storedFileName)
                    .createdAt(Instant.now())
                    .build();

            Attachment saved = attachmentRepository.save(attachment);
            return toResponse(saved);
        } catch (Exception e) {
            if (storedFileName != null) {
                try {
                    storageService.delete(storedFileName);
                } catch (Exception delEx) {
                    log.warn("Failed to clean up stored file after failed attachment save: {}", storedFileName, delEx);
                }
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Failed to process attachment upload: " + e.getMessage(), e);
        }
    }

    public List<AttachmentResponse> getAttachmentsForOwner(UserDetails userDetails, AttachmentOwnerType ownerType, UUID ownerId) {
        authorizer.authorizeRead(userDetails, ownerType, ownerId);
        return attachmentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AttachmentResponse getAttachmentById(UserDetails userDetails, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        authorizer.authorizeRead(userDetails, attachment.getOwnerType(), attachment.getOwnerId());
        return toResponse(attachment);
    }

    public ResponseEntity<Resource> downloadAttachment(UserDetails userDetails, UUID attachmentId, boolean inline) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        authorizer.authorizeRead(userDetails, attachment.getOwnerType(), attachment.getOwnerId());

        if (!storageService.exists(attachment.getStoredFileName())) {
            throw new ResourceNotFoundException("Physical file not found in storage.");
        }

        Resource fileResource;
        try {
            fileResource = storageService.loadAsResource(attachment.getStoredFileName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from storage: " + e.getMessage(), e);
        }

        if (fileResource == null || !fileResource.exists()) {
            throw new ResourceNotFoundException("Physical file not found in storage.");
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(attachment.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(attachment.getOriginalFileName())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(attachment.getFileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(fileResource);
    }

    public void deleteAttachment(UserDetails userDetails, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));

        authorizer.authorizeDelete(userDetails, attachment.getOwnerType(), attachment.getOwnerId());

        storageService.delete(attachment.getStoredFileName());
        attachmentRepository.delete(attachment);
    }

    public void deleteByOwner(AttachmentOwnerType ownerType, UUID ownerId) {
        List<Attachment> list = attachmentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId);
        for (Attachment att : list) {
            try {
                storageService.delete(att.getStoredFileName());
            } catch (Exception e) {
                log.warn("Failed to delete physical file during cascading cleanup: {}", att.getStoredFileName(), e);
            }
        }
        attachmentRepository.deleteByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    public AttachmentResponse toResponse(Attachment attachment) {
        LocalDateTime createdLdt = null;
        if (attachment.getCreatedAt() != null) {
            createdLdt = LocalDateTime.ofInstant(attachment.getCreatedAt(), ZoneId.systemDefault());
        }
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .ownerType(attachment.getOwnerType())
                .ownerId(attachment.getOwnerId())
                .uploaderId(attachment.getUploaderId())
                .originalFileName(attachment.getOriginalFileName())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .downloadUrl("/api/v1/attachments/" + attachment.getId() + "/download")
                .createdAt(createdLdt)
                .build();
    }
}
