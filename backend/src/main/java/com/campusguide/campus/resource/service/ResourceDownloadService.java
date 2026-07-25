package com.campusguide.campus.resource.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.campus.resource.dto.ResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ResourceDownloadService {

    private final ResourceService resourceService;
    private final StorageService storageService;

    /**
     * Downloads the physical file for the specified resource.
     *
     * @param resourceId the ID of the resource
     * @return a ResponseEntity containing the file resource and response headers
     */
    public ResponseEntity<Resource> downloadResource(String resourceId) {
        // Retrieve resource metadata. This method automatically validates that the
        // resource exists and is not soft deleted, throwing ResourceNotFoundException if invalid.
        ResourceResponse resourceMetadata = resourceService.getResourceById(resourceId);

        String storedFileName = resourceMetadata.getFileName();
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new ResourceNotFoundException("Stored file name is missing in resource metadata.");
        }

        // Verify the stored file exists physically in storage
        if (!storageService.exists(storedFileName)) {
            throw new ResourceNotFoundException("Physical file not found in storage.");
        }

        // Retrieve the file resource from storage
        Resource fileResource;
        try {
            fileResource = storageService.loadAsResource(storedFileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve file from storage: " + e.getMessage(), e);
        }

        if (fileResource == null || !fileResource.exists()) {
            throw new ResourceNotFoundException("Physical file not found in storage.");
        }

        // Determine MIME type
        String contentType = resourceMetadata.getFileType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // Set attachment mode with originalFileName
        String originalFileName = resourceMetadata.getOriginalFileName();
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = storedFileName;
        }

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(originalFileName)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(fileResource);
    }
}
