package com.campusguide.campus.resource.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.campus.resource.dto.CreateResourceRequest;
import com.campusguide.campus.resource.dto.ResourceResponse;
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

    private static final java.util.Map<String, Set<String>> MIME_TO_EXTENSIONS = java.util.Map.of(
            "application/pdf", Set.of(".pdf"),
            "application/msword", Set.of(".doc"),
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", Set.of(".docx"),
            "application/vnd.ms-powerpoint", Set.of(".ppt"),
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", Set.of(".pptx"),
            "application/vnd.ms-excel", Set.of(".xls"),
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Set.of(".xlsx"),
            "image/jpeg", Set.of(".jpg", ".jpeg"),
            "image/png", Set.of(".png")
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
        if (mimeType == null || !MIME_TO_EXTENSIONS.containsKey(mimeType)) {
            throw new BadRequestException("Unsupported MIME type: " + mimeType);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BadRequestException("Original filename is missing");
        }

        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex == -1) {
            throw new BadRequestException("File extension is missing");
        }
        String extension = originalFilename.substring(extensionIndex).toLowerCase();

        Set<String> allowedExtensions = MIME_TO_EXTENSIONS.get(mimeType);
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new BadRequestException("File extension " + extension + " does not match MIME type " + mimeType);
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
