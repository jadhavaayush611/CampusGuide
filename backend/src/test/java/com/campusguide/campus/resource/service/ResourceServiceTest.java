package com.campusguide.campus.resource.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.resource.dto.CreateResourceRequest;
import com.campusguide.campus.resource.dto.ResourceResponse;
import com.campusguide.campus.resource.dto.ResourceSummaryResponse;
import com.campusguide.campus.resource.dto.UpdateResourceRequest;
import com.campusguide.campus.resource.entity.Resource;
import com.campusguide.campus.resource.repository.ResourceRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.campusguide.platform.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ResourceService resourceService;

    private UserDetails uploaderUserDetails;
    private UserDetails adminUserDetails;
    private UserDetails otherUserDetails;

    private User uploaderUser;
    private User adminUser;
    private User otherUser;

    private CreateResourceRequest createRequest;
    private UpdateResourceRequest updateRequest;
    private Resource activeResource;
    private Resource deletedResource;

    @BeforeEach
    void setUp() {
        uploaderUserDetails = org.springframework.security.core.userdetails.User.withUsername("uploader@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminUserDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        otherUserDetails = org.springframework.security.core.userdetails.User.withUsername("other@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        uploaderUser = User.builder()
                .id("user-uploader")
                .email("uploader@campusguide.com")
                .role(Role.STUDENT)
                .build();

        adminUser = User.builder()
                .id("user-admin")
                .email("admin@campusguide.com")
                .role(Role.SUPER_ADMIN)
                .build();

        otherUser = User.builder()
                .id("user-other")
                .email("other@campusguide.com")
                .role(Role.STUDENT)
                .build();

        lenient().when(currentUserService.getCurrentUser(uploaderUserDetails)).thenReturn(uploaderUser);
        lenient().when(currentUserService.getCurrentUser(adminUserDetails)).thenReturn(adminUser);
        lenient().when(currentUserService.getCurrentUser(otherUserDetails)).thenReturn(otherUser);

        createRequest = CreateResourceRequest.builder()
                .title("Lecture Notes")
                .description("Week 5 slides")
                .councilId("council-abc")
                .communityId("community-xyz")
                .tags(List.of("java", "spring"))
                .fileName("notes.pdf")
                .originalFileName("lecture_notes.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();

        updateRequest = UpdateResourceRequest.builder()
                .title("New Lecture Notes")
                .description("New Week 5 slides")
                .tags(List.of("java", "spring-boot"))
                .build();

        activeResource = Resource.builder()
                .id("resource-123")
                .title("Lecture Notes")
                .description("Week 5 slides")
                .uploaderId("user-uploader")
                .councilId("council-abc")
                .communityId("community-xyz")
                .tags(List.of("java", "spring"))
                .fileName("notes.pdf")
                .originalFileName("lecture_notes.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .downloadUrl("/api/resources/download/resource-123")
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        deletedResource = Resource.builder()
                .id("resource-deleted")
                .title("Old Lecture Notes")
                .description("Week 1 slides")
                .uploaderId("user-uploader")
                .councilId("council-abc")
                .communityId("community-xyz")
                .tags(List.of("java"))
                .fileName("old_notes.pdf")
                .originalFileName("old_lecture_notes.pdf")
                .fileType("application/pdf")
                .fileSize(512L)
                .downloadUrl("/api/resources/download/resource-deleted")
                .isDeleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==========================================
    // CREATE RESOURCE TESTS
    // ==========================================

    @Test
    void createResource_Success_WithCommunity() {

        when(communityRepository.existsById("community-xyz")).thenReturn(true);
        when(resourceRepository.save(any(Resource.class))).thenReturn(activeResource);

        ResourceResponse response = resourceService.createResource(uploaderUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("resource-123", response.getId());
        assertEquals("Lecture Notes", response.getTitle());
        assertEquals("user-uploader", response.getUploaderId());
        assertEquals("/api/resources/download/resource-123", response.getDownloadUrl());
    }

    @Test
    void createResource_Success_WithNullCouncilAndCommunity() {
        CreateResourceRequest requestWithNulls = CreateResourceRequest.builder()
                .title("Lecture Notes")
                .description("Week 5 slides")
                .councilId(null)
                .communityId("")
                .tags(List.of("java"))
                .fileName("notes.pdf")
                .originalFileName("lecture_notes.pdf")
                .fileType("application/pdf")
                .fileSize(1024L)
                .build();

        Resource savedResourceWithNulls = Resource.builder()
                .id("resource-123")
                .title("Lecture Notes")
                .uploaderId("user-uploader")
                .councilId(null)
                .communityId(null)
                .fileName("notes.pdf")
                .isDeleted(false)
                .build();


        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResourceWithNulls);

        ResourceResponse response = resourceService.createResource(uploaderUserDetails, requestWithNulls);

        assertNotNull(response);
        assertNull(response.getCouncilId());
        assertNull(response.getCommunityId());
        assertEquals("/api/resources/download/resource-123", response.getDownloadUrl());
    }

    @Test
    void createResource_Unauthenticated() {
        when(currentUserService.getCurrentUser(null)).thenThrow(new UnauthorisedException("User is not authenticated"));
        assertThrows(UnauthorisedException.class, () -> 
                resourceService.createResource(null, createRequest));
    }

    @Test
    void createResource_UserNotFound() {
        when(currentUserService.getCurrentUser(uploaderUserDetails)).thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.createResource(uploaderUserDetails, createRequest));
    }

    @Test
    void createResource_InvalidCommunity() {

        when(communityRepository.existsById("community-xyz")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.createResource(uploaderUserDetails, createRequest));
    }

    // ==========================================
    // UPDATE RESOURCE TESTS
    // ==========================================

    @Test
    void updateResource_OwnerSuccess() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));


        Resource updatedResource = Resource.builder()
                .id("resource-123")
                .title("New Lecture Notes")
                .description("New Week 5 slides")
                .uploaderId("user-uploader")
                .tags(List.of("java", "spring-boot"))
                .isDeleted(false)
                .build();
        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);

        ResourceResponse response = resourceService.updateResource(uploaderUserDetails, "resource-123", updateRequest);

        assertNotNull(response);
        assertEquals("New Lecture Notes", response.getTitle());
        assertEquals("New Week 5 slides", response.getDescription());
        assertEquals(List.of("java", "spring-boot"), response.getTags());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateResource_AdminSuccess() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));

        Resource updatedResource = Resource.builder()
                .id("resource-123")
                .title("New Lecture Notes")
                .description("New Week 5 slides")
                .uploaderId("user-uploader")
                .tags(List.of("java", "spring-boot"))
                .isDeleted(false)
                .build();
        when(resourceRepository.save(any(Resource.class))).thenReturn(updatedResource);

        ResourceResponse response = resourceService.updateResource(adminUserDetails, "resource-123", updateRequest);

        assertNotNull(response);
        assertEquals("New Lecture Notes", response.getTitle());
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateResource_NonOwnerForbidden() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));

        assertThrows(AccessDeniedException.class, () -> 
                resourceService.updateResource(otherUserDetails, "resource-123", updateRequest));

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void updateResource_ResourceNotFound() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.updateResource(uploaderUserDetails, "resource-123", updateRequest));
    }

    @Test
    void updateResource_SoftDeletedResource() {
        when(resourceRepository.findById("resource-deleted")).thenReturn(Optional.of(deletedResource));

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.updateResource(uploaderUserDetails, "resource-deleted", updateRequest));
    }

    @Test
    void updateResource_Unauthenticated() {
        assertThrows(UnauthorisedException.class, () -> 
                resourceService.updateResource(null, "resource-123", updateRequest));
    }

    // ==========================================
    // DELETE RESOURCE TESTS
    // ==========================================

    @Test
    void deleteResource_OwnerSuccess() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));


        resourceService.deleteResource(uploaderUserDetails, "resource-123");

        assertTrue(activeResource.getIsDeleted());
        verify(resourceRepository).save(activeResource);
    }

    @Test
    void deleteResource_AdminSuccess() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));

        resourceService.deleteResource(adminUserDetails, "resource-123");

        assertTrue(activeResource.getIsDeleted());
        verify(resourceRepository).save(activeResource);
    }

    @Test
    void deleteResource_NonOwnerForbidden() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));

        assertThrows(AccessDeniedException.class, () -> 
                resourceService.deleteResource(otherUserDetails, "resource-123"));

        assertFalse(activeResource.getIsDeleted());
        verify(resourceRepository, never()).save(any(Resource.class));
    }

    // ==========================================
    // GET RESOURCE TESTS
    // ==========================================

    @Test
    void getResourceById_Success() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));

        ResourceResponse response = resourceService.getResourceById("resource-123");

        assertNotNull(response);
        assertEquals("resource-123", response.getId());
        assertEquals("Lecture Notes", response.getTitle());
    }

    @Test
    void getResourceById_NotFound() {
        when(resourceRepository.findById("resource-non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.getResourceById("resource-non-existent"));
    }

    @Test
    void getAllResources_Success() {
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getAllResources();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("resource-123", responses.get(0).getId());
    }

    @Test
    void getResourcesByCommunity_Success() {
        when(communityRepository.existsById("community-xyz")).thenReturn(true);
        when(resourceRepository.findByCommunityIdAndIsDeletedFalseOrderByCreatedAtDesc("community-xyz"))
                .thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByCommunity("community-xyz");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getResourcesByCommunity_NotFound() {
        when(communityRepository.existsById("community-xyz")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.getResourcesByCommunity("community-xyz"));
    }

    @Test
    void getResourcesByCouncil_Success() {
        when(resourceRepository.findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc("council-abc"))
                .thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByCouncil("council-abc");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }
}
