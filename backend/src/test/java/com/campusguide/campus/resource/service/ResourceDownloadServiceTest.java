package com.campusguide.campus.resource.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.campus.resource.dto.ResourceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceDownloadServiceTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ResourceDownloadService resourceDownloadService;

    private ResourceResponse pdfMetadata;
    private ResourceResponse pngMetadata;

    @BeforeEach
    void setUp() {
        pdfMetadata = ResourceResponse.builder()
                .id("pdf-123")
                .title("Lecture Slides")
                .fileName("stored-pdf-uuid.pdf")
                .originalFileName("lecture_notes.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();

        pngMetadata = ResourceResponse.builder()
                .id("png-123")
                .title("Architecture Diagram")
                .fileName("stored-png-uuid.png")
                .originalFileName("diagram.png")
                .fileType("image/png")
                .fileSize(2048L)
                .build();
    }

    // ==========================================
    // SUCCESS CASES
    // ==========================================

    @Test
    void downloadResource_Success_Pdf() throws Exception {
        String resourceId = "pdf-123";
        Resource expectedFileResource = new ByteArrayResource("pdf content".getBytes());

        when(resourceService.getResourceById(resourceId)).thenReturn(pdfMetadata);
        when(storageService.exists(pdfMetadata.getFileName())).thenReturn(true);
        when(storageService.loadAsResource(pdfMetadata.getFileName())).thenReturn(expectedFileResource);

        ResponseEntity<Resource> response = resourceDownloadService.downloadResource(resourceId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"lecture_notes.pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(expectedFileResource, response.getBody());

        verify(resourceService, times(1)).getResourceById(resourceId);
        verify(storageService, times(1)).exists(pdfMetadata.getFileName());
        verify(storageService, times(1)).loadAsResource(pdfMetadata.getFileName());
    }

    @Test
    void downloadResource_Success_Png() throws Exception {
        String resourceId = "png-123";
        Resource expectedFileResource = new ByteArrayResource("png content".getBytes());

        when(resourceService.getResourceById(resourceId)).thenReturn(pngMetadata);
        when(storageService.exists(pngMetadata.getFileName())).thenReturn(true);
        when(storageService.loadAsResource(pngMetadata.getFileName())).thenReturn(expectedFileResource);

        ResponseEntity<Resource> response = resourceDownloadService.downloadResource(resourceId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"diagram.png\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(expectedFileResource, response.getBody());

        verify(resourceService, times(1)).getResourceById(resourceId);
        verify(storageService, times(1)).exists(pngMetadata.getFileName());
        verify(storageService, times(1)).loadAsResource(pngMetadata.getFileName());
    }

    // ==========================================
    // FAILURE CASES
    // ==========================================

    @Test
    void downloadResource_Failure_ResourceNotFound() {
        String resourceId = "non-existent-id";
        when(resourceService.getResourceById(resourceId)).thenThrow(new ResourceNotFoundException("Resource not found with id: " + resourceId));

        assertThrows(ResourceNotFoundException.class, () ->
                resourceDownloadService.downloadResource(resourceId));

        verify(resourceService, times(1)).getResourceById(resourceId);
        verifyNoInteractions(storageService);
    }

    @Test
    void downloadResource_Failure_SoftDeleted() {
        String resourceId = "deleted-123";
        when(resourceService.getResourceById(resourceId)).thenThrow(new ResourceNotFoundException("Resource not found with id: " + resourceId));

        assertThrows(ResourceNotFoundException.class, () ->
                resourceDownloadService.downloadResource(resourceId));

        verify(resourceService, times(1)).getResourceById(resourceId);
        verifyNoInteractions(storageService);
    }

    @Test
    void downloadResource_Failure_MissingPhysicalFile() throws Exception {
        String resourceId = "pdf-123";
        when(resourceService.getResourceById(resourceId)).thenReturn(pdfMetadata);
        when(storageService.exists(pdfMetadata.getFileName())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                resourceDownloadService.downloadResource(resourceId));

        verify(resourceService, times(1)).getResourceById(resourceId);
        verify(storageService, times(1)).exists(pdfMetadata.getFileName());
        verify(storageService, never()).loadAsResource(anyString());
    }

    @Test
    void downloadResource_Failure_StorageServiceFailure() throws Exception {
        String resourceId = "pdf-123";
        when(resourceService.getResourceById(resourceId)).thenReturn(pdfMetadata);
        when(storageService.exists(pdfMetadata.getFileName())).thenReturn(true);
        when(storageService.loadAsResource(pdfMetadata.getFileName())).thenThrow(new IOException("Disk read error"));

        assertThrows(RuntimeException.class, () ->
                resourceDownloadService.downloadResource(resourceId));

        verify(resourceService, times(1)).getResourceById(resourceId);
        verify(storageService, times(1)).exists(pdfMetadata.getFileName());
        verify(storageService, times(1)).loadAsResource(pdfMetadata.getFileName());
    }

    // ==========================================
    // LOCAL STORAGE SERVICE PATH/RESOURCE TESTS
    // ==========================================

    @Test
    void localStorageService_LoadAsResource_Success(@TempDir Path tempDir) throws Exception {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        String storedName = localStorageService.store(file);
        assertTrue(localStorageService.exists(storedName));

        Resource resource = localStorageService.loadAsResource(storedName);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        try (var is = resource.getInputStream()) {
            assertEquals("pdf content", new String(is.readAllBytes()));
        }

        // Cleanup
        localStorageService.delete(storedName);
    }

    @Test
    void localStorageService_LoadAsResource_Failure_FileDoesNotExist(@TempDir Path tempDir) {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        assertThrows(IOException.class, () ->
                localStorageService.loadAsResource("non-existent.pdf"));
    }

    @Test
    void localStorageService_LoadAsResource_PathTraversalPrevention(@TempDir Path tempDir) {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        assertThrows(SecurityException.class, () ->
                localStorageService.loadAsResource("../traversal.png"));
    }
}
