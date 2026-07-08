package com.campusguide.modules.resource.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.ResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourceUploadService {

    private final StorageService storageService;
    private final ResourceService resourceService;

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024L; // 20 MB

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png"
    );

    /**
     * Handles the file upload process, including validation, physical storage,
     * and database persistence delegation.
     *
     * @param userDetails the authenticated user details
     * @param file the MultipartFile representing the uploaded file
     * @param request the CreateResourceRequest containing metadata
     * @return the persisted ResourceResponse
     */
    public ResourceResponse uploadResource(UserDetails userDetails, MultipartFile file, CreateResourceRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BadRequestException("Unsupported MIME type: " + mimeType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds the maximum limit of 20MB");
        }

        String storedFileName;
        try {
            storedFileName = storageService.store(file);
        } catch (IOException e) {
            throw new RuntimeException("Storage failure: " + e.getMessage(), e);
        }

        request.setFileName(storedFileName);
        request.setOriginalFileName(file.getOriginalFilename());
        request.setFileType(mimeType);
        request.setFileSize(file.getSize());

        try {
            return resourceService.createResource(userDetails, request);
        } catch (Exception e) {
            // Cleanup physical storage on database metadata insertion failure
            storageService.delete(storedFileName);
            throw e;
        }
    }
}
