package com.campusguide.modules.resource.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.ResourceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceUploadServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceUploadService resourceUploadService;

    private UserDetails userDetails;
    private CreateResourceRequest createRequest;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.withUsername("test@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        createRequest = CreateResourceRequest.builder()
                .title("Lecture Notes")
                .description("Week 5 slides")
                .councilId("council-abc")
                .communityId("community-xyz")
                .tags(List.of("java", "spring"))
                .build();
    }

    // ==========================================
    // SUCCESSFUL UPLOADS
    // ==========================================

    @Test
    void uploadResource_Success_Pdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lecture_notes.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        String generatedFilename = UUID.randomUUID().toString() + ".pdf";
        when(storageService.store(file)).thenReturn(generatedFilename);

        ResourceResponse expectedResponse = ResourceResponse.builder()
                .id("resource-123")
                .title("Lecture Notes")
                .fileName(generatedFilename)
                .originalFileName("lecture_notes.pdf")
                .fileType("application/pdf")
                .fileSize(file.getSize())
                .downloadUrl("/api/resources/download/resource-123")
                .build();

        when(resourceService.createResource(eq(userDetails), any(CreateResourceRequest.class)))
                .thenReturn(expectedResponse);

        ResourceResponse response = resourceUploadService.uploadResource(userDetails, file, createRequest);

        assertNotNull(response);
        assertEquals(generatedFilename, response.getFileName());
        assertEquals("lecture_notes.pdf", response.getOriginalFileName());
        assertEquals("application/pdf", response.getFileType());
        assertEquals("/api/resources/download/resource-123", response.getDownloadUrl());

        verify(storageService, times(1)).store(file);
        verify(resourceService, times(1)).createResource(eq(userDetails), any(CreateResourceRequest.class));
    }

    @Test
    void uploadResource_Success_Docx() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "assignment.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx content".getBytes()
        );

        String generatedFilename = UUID.randomUUID().toString() + ".docx";
        when(storageService.store(file)).thenReturn(generatedFilename);

        ResourceResponse expectedResponse = ResourceResponse.builder()
                .id("resource-124")
                .title("Lecture Notes")
                .fileName(generatedFilename)
                .originalFileName("assignment.docx")
                .fileType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .fileSize(file.getSize())
                .downloadUrl("/api/resources/download/resource-124")
                .build();

        when(resourceService.createResource(eq(userDetails), any(CreateResourceRequest.class)))
                .thenReturn(expectedResponse);

        ResourceResponse response = resourceUploadService.uploadResource(userDetails, file, createRequest);

        assertNotNull(response);
        assertEquals(generatedFilename, response.getFileName());
        assertEquals("assignment.docx", response.getOriginalFileName());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", response.getFileType());

        verify(storageService, times(1)).store(file);
        verify(resourceService, times(1)).createResource(eq(userDetails), any(CreateResourceRequest.class));
    }

    @Test
    void uploadResource_Success_Png() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "diagram.png",
                "image/png",
                "png content".getBytes()
        );

        String generatedFilename = UUID.randomUUID().toString() + ".png";
        when(storageService.store(file)).thenReturn(generatedFilename);

        ResourceResponse expectedResponse = ResourceResponse.builder()
                .id("resource-125")
                .title("Lecture Notes")
                .fileName(generatedFilename)
                .originalFileName("diagram.png")
                .fileType("image/png")
                .fileSize(file.getSize())
                .downloadUrl("/api/resources/download/resource-125")
                .build();

        when(resourceService.createResource(eq(userDetails), any(CreateResourceRequest.class)))
                .thenReturn(expectedResponse);

        ResourceResponse response = resourceUploadService.uploadResource(userDetails, file, createRequest);

        assertNotNull(response);
        assertEquals(generatedFilename, response.getFileName());
        assertEquals("diagram.png", response.getOriginalFileName());
        assertEquals("image/png", response.getFileType());

        verify(storageService, times(1)).store(file);
        verify(resourceService, times(1)).createResource(eq(userDetails), any(CreateResourceRequest.class));
    }

    // ==========================================
    // VALIDATIONS
    // ==========================================

    @Test
    void uploadResource_Validation_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(BadRequestException.class, () ->
                resourceUploadService.uploadResource(userDetails, file, createRequest));

        verifyNoInteractions(storageService);
        verifyNoInteractions(resourceService);
    }

    @Test
    void uploadResource_Validation_NullFile() {
        assertThrows(BadRequestException.class, () ->
                resourceUploadService.uploadResource(userDetails, null, createRequest));

        verifyNoInteractions(storageService);
        verifyNoInteractions(resourceService);
    }

    @Test
    void uploadResource_Validation_UnsupportedMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.sh",
                "text/x-shellscript",
                "echo hello".getBytes()
        );

        assertThrows(BadRequestException.class, () ->
                resourceUploadService.uploadResource(userDetails, file, createRequest));

        verifyNoInteractions(storageService);
        verifyNoInteractions(resourceService);
    }

    @Test
    void uploadResource_Validation_FileTooLarge() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                "some content".getBytes()
        ) {
            @Override
            public long getSize() {
                return 20 * 1024 * 1024L + 1; // 20 MB + 1 byte
            }
        };

        assertThrows(BadRequestException.class, () ->
                resourceUploadService.uploadResource(userDetails, file, createRequest));

        verifyNoInteractions(storageService);
        verifyNoInteractions(resourceService);
    }

    // ==========================================
    // STORAGE FAILURES
    // ==========================================

    @Test
    void uploadResource_StorageFailure_IOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        when(storageService.store(file)).thenThrow(new IOException("Disk full"));

        assertThrows(RuntimeException.class, () ->
                resourceUploadService.uploadResource(userDetails, file, createRequest));

        verify(storageService, times(1)).store(file);
        verifyNoInteractions(resourceService);
    }

    @Test
    void uploadResource_StorageFailure_GenericException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        when(storageService.store(file)).thenThrow(new RuntimeException("Storage service down"));

        assertThrows(RuntimeException.class, () ->
                resourceUploadService.uploadResource(userDetails, file, createRequest));

        verify(storageService, times(1)).store(file);
        verifyNoInteractions(resourceService);
    }

    @Test
    void uploadResource_MetadataFailure_DeletesPhysicalFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        String generatedFilename = "stored-uuid.pdf";
        when(storageService.store(file)).thenReturn(generatedFilename);
        when(resourceService.createResource(eq(userDetails), any(CreateResourceRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () ->
                resourceUploadService.uploadResource(userDetails, file, createRequest));

        verify(storageService, times(1)).store(file);
        verify(resourceService, times(1)).createResource(eq(userDetails), any(CreateResourceRequest.class));
        verify(storageService, times(1)).delete(generatedFilename);
    }

    // ==========================================
    // LOCAL STORAGE SERVICE TESTS
    // ==========================================

    @Test
    void localStorageService_InitializationAndStore() throws Exception {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_file.png",
                "image/png",
                "png content".getBytes()
        );

        String storedName = localStorageService.store(file);
        assertNotNull(storedName);
        assertTrue(storedName.endsWith(".png"));
        assertNotEquals("test_file.png", storedName);

        // Verify file exists
        assertTrue(localStorageService.exists(storedName));

        // Verify file can be deleted
        localStorageService.delete(storedName);
        assertFalse(localStorageService.exists(storedName));
    }

    @Test
    void localStorageService_GenerateStoredFileName() {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());

        String storedName = localStorageService.generateStoredFileName("doc.pdf");
        assertNotNull(storedName);
        assertTrue(storedName.endsWith(".pdf"));
        assertEquals(40, storedName.length()); // 36 characters for UUID + 4 characters for ".pdf"

        String noExtName = localStorageService.generateStoredFileName("document");
        assertEquals(36, noExtName.length());

        String nullName = localStorageService.generateStoredFileName(null);
        assertEquals(36, nullName.length());
    }

    @Test
    void localStorageService_PathTraversalPrevention_Store() {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../traversal.png",
                "image/png",
                "content".getBytes()
        );

        assertThrows(SecurityException.class, () ->
                localStorageService.store(file));
    }

    @Test
    void localStorageService_PathTraversalPrevention_Delete() {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        assertThrows(SecurityException.class, () ->
                localStorageService.delete("../traversal.png"));
    }

    @Test
    void localStorageService_PathTraversalPrevention_Exists() {
        LocalStorageService localStorageService = new LocalStorageService(tempDir.toString());
        localStorageService.init();

        assertFalse(localStorageService.exists("../traversal.png"));
    }
}
