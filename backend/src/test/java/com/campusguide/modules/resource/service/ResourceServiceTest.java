package com.campusguide.modules.resource.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.council.repository.CouncilRepository;
import com.campusguide.modules.resource.dto.CreateResourceRequest;
import com.campusguide.modules.resource.dto.ResourceResponse;
import com.campusguide.modules.resource.dto.ResourceSummaryResponse;
import com.campusguide.modules.resource.dto.UpdateResourceRequest;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.resource.repository.ResourceRepository;
import com.campusguide.modules.user.entity.Role;
import com.campusguide.modules.user.entity.User;
import com.campusguide.modules.user.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private CouncilRepository councilRepository;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserRepository userRepository;

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
    void createResource_Success_WithCouncilAndCommunity() {
        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.of(uploaderUser));
        when(councilRepository.existsById("council-abc")).thenReturn(true);
        when(communityRepository.existsById("community-xyz")).thenReturn(true);
        when(resourceRepository.save(any(Resource.class))).thenReturn(activeResource);

        ResourceResponse response = resourceService.createResource(uploaderUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("resource-123", response.getId());
        assertEquals("Lecture Notes", response.getTitle());
        assertEquals("user-uploader", response.getUploaderId());
        assertEquals("/api/resources/download/resource-123", response.getDownloadUrl());

        verify(resourceRepository).save(any(Resource.class));
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

        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.of(uploaderUser));
        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResourceWithNulls);

        ResourceResponse response = resourceService.createResource(uploaderUserDetails, requestWithNulls);

        assertNotNull(response);
        assertNull(response.getCouncilId());
        assertNull(response.getCommunityId());
        verify(resourceRepository).save(any(Resource.class));
        verify(councilRepository, never()).existsById(anyString());
        verify(communityRepository, never()).existsById(anyString());
    }

    @Test
    void createResource_UserNotFound() {
        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.createResource(uploaderUserDetails, createRequest));

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void createResource_InvalidCouncil() {
        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.of(uploaderUser));
        when(councilRepository.existsById("council-abc")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.createResource(uploaderUserDetails, createRequest));

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void createResource_InvalidCommunity() {
        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.of(uploaderUser));
        when(councilRepository.existsById("council-abc")).thenReturn(true);
        when(communityRepository.existsById("community-xyz")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.createResource(uploaderUserDetails, createRequest));

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void createResource_Unauthenticated() {
        assertThrows(UnauthorisedException.class, () -> 
                resourceService.createResource(null, createRequest));
    }

    // ==========================================
    // UPDATE RESOURCE TESTS
    // ==========================================

    @Test
    void updateResource_OwnerSuccess() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));
        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.of(uploaderUser));

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
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));

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
        when(userRepository.findByEmail(otherUserDetails.getUsername())).thenReturn(Optional.of(otherUser));

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
        when(userRepository.findByEmail(uploaderUserDetails.getUsername())).thenReturn(Optional.of(uploaderUser));

        resourceService.deleteResource(uploaderUserDetails, "resource-123");

        verify(resourceRepository).save(argThat(resource -> resource.getIsDeleted() == true));
    }

    @Test
    void deleteResource_AdminSuccess() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));

        resourceService.deleteResource(adminUserDetails, "resource-123");

        verify(resourceRepository).save(argThat(resource -> resource.getIsDeleted() == true));
    }

    @Test
    void deleteResource_NonOwnerForbidden() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));
        when(userRepository.findByEmail(otherUserDetails.getUsername())).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> 
                resourceService.deleteResource(otherUserDetails, "resource-123"));

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void deleteResource_AlreadyDeleted() {
        when(resourceRepository.findById("resource-deleted")).thenReturn(Optional.of(deletedResource));

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.deleteResource(uploaderUserDetails, "resource-deleted"));

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    // ==========================================
    // RETRIEVAL TESTS
    // ==========================================

    @Test
    void getResourceById_Success() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.of(activeResource));

        ResourceResponse response = resourceService.getResourceById("resource-123");

        assertNotNull(response);
        assertEquals("resource-123", response.getId());
        assertFalse(response.getFileName().isEmpty());
    }

    @Test
    void getResourceById_NotFound() {
        when(resourceRepository.findById("resource-123")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.getResourceById("resource-123"));
    }

    @Test
    void getResourceById_SoftDeleted() {
        when(resourceRepository.findById("resource-deleted")).thenReturn(Optional.of(deletedResource));

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.getResourceById("resource-deleted"));
    }

    @Test
    void getAllResources_Success() {
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getAllResources();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("resource-123", responses.get(0).getId());
        assertEquals("Lecture Notes", responses.get(0).getTitle());
    }

    @Test
    void getAllResources_Empty() {
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        List<ResourceSummaryResponse> responses = resourceService.getAllResources();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getResourcesByUploader_Success() {
        when(userRepository.existsById("user-uploader")).thenReturn(true);
        when(resourceRepository.findByUploaderIdAndIsDeletedFalseOrderByCreatedAtDesc("user-uploader")).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByUploader("user-uploader");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("user-uploader", responses.get(0).getUploaderId());
    }

    @Test
    void getResourcesByUploader_UserNotFound() {
        when(userRepository.existsById("user-uploader")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.getResourcesByUploader("user-uploader"));
    }

    @Test
    void getResourcesByUploader_Empty() {
        when(userRepository.existsById("user-uploader")).thenReturn(true);
        when(resourceRepository.findByUploaderIdAndIsDeletedFalseOrderByCreatedAtDesc("user-uploader")).thenReturn(Collections.emptyList());

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByUploader("user-uploader");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getResourcesByCouncil_Success() {
        when(councilRepository.existsById("council-abc")).thenReturn(true);
        when(resourceRepository.findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc("council-abc")).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByCouncil("council-abc");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getResourcesByCouncil_NotFound() {
        when(councilRepository.existsById("council-abc")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> 
                resourceService.getResourcesByCouncil("council-abc"));
    }

    @Test
    void getResourcesByCouncil_Empty() {
        when(councilRepository.existsById("council-abc")).thenReturn(true);
        when(resourceRepository.findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc("council-abc")).thenReturn(Collections.emptyList());

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByCouncil("council-abc");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getResourcesByCommunity_Success() {
        when(communityRepository.existsById("community-xyz")).thenReturn(true);
        when(resourceRepository.findByCommunityIdAndIsDeletedFalseOrderByCreatedAtDesc("community-xyz")).thenReturn(List.of(activeResource));

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
    void getResourcesByCommunity_Empty() {
        when(communityRepository.existsById("community-xyz")).thenReturn(true);
        when(resourceRepository.findByCommunityIdAndIsDeletedFalseOrderByCreatedAtDesc("community-xyz")).thenReturn(Collections.emptyList());

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByCommunity("community-xyz");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ==========================================
    // SEARCH TESTS
    // ==========================================

    @Test
    void searchResources_TitleMatch() {
        when(resourceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("lecture", "lecture"))
                .thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.searchResources("lecture");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Lecture Notes", responses.get(0).getTitle());
    }

    @Test
    void searchResources_DescriptionMatch() {
        when(resourceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("slides", "slides"))
                .thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.searchResources("slides");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchResources_CaseInsensitive() {
        when(resourceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("LECTURE", "LECTURE"))
                .thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.searchResources("LECTURE");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchResources_PartialSearch() {
        when(resourceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("Notes", "Notes"))
                .thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.searchResources("Notes");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchResources_BlankSearchReturnsAll() {
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.searchResources("   ");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchResources_NullSearchReturnsAll() {
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.searchResources(null);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void searchResources_EmptyResult() {
        when(resourceRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("none", "none"))
                .thenReturn(Collections.emptyList());

        List<ResourceSummaryResponse> responses = resourceService.searchResources("none");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ==========================================
    // TAG TESTS
    // ==========================================

    @Test
    void getResourcesByTag_Success_ValidTag() {
        when(resourceRepository.findByTagsIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("java")).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByTag("java");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Lecture Notes", responses.get(0).getTitle());
    }

    @Test
    void getResourcesByTag_Success_WhitespaceTrimming() {
        when(resourceRepository.findByTagsIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("java")).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByTag("  java  ");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getResourcesByTag_BlankTag_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> resourceService.getResourcesByTag(""));
    }

    @Test
    void getResourcesByTag_WhitespaceTag_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> resourceService.getResourcesByTag("    "));
    }

    @Test
    void getResourcesByTag_NullTag_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> resourceService.getResourcesByTag(null));
    }

    @Test
    void getResourcesByTag_UnknownTag_ReturnsEmpty() {
        when(resourceRepository.findByTagsIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("python")).thenReturn(Collections.emptyList());

        List<ResourceSummaryResponse> responses = resourceService.getResourcesByTag("python");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ==========================================
    // RECENT RESOURCES TESTS
    // ==========================================

    @Test
    void getRecentResources_Success_NewestFirst() {
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activeResource));

        List<ResourceSummaryResponse> responses = resourceService.getRecentResources();

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }
}
