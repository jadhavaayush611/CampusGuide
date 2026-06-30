package com.campusguide.modules.community.service;

import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.modules.community.dto.CommunityResponse;
import com.campusguide.modules.community.dto.CommunitySummaryResponse;
import com.campusguide.modules.community.dto.CreateCommunityRequest;
import com.campusguide.modules.community.dto.UpdateCommunityRequest;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.community.repository.CommunityRepository;
import com.campusguide.modules.council.repository.CouncilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private CouncilRepository councilRepository;

    @InjectMocks
    private CommunityService communityService;

    private CreateCommunityRequest createRequest;
    private UpdateCommunityRequest updateRequest;
    private Community community;

    @BeforeEach
    void setUp() {
        createRequest = CreateCommunityRequest.builder()
                .name("Coding Club")
                .description("A community for coding enthusiasts.")
                .bannerUrl("http://example.com/banner.png")
                .councilId("council-123")
                .build();

        updateRequest = UpdateCommunityRequest.builder()
                .description("Updated description.")
                .bannerUrl("http://example.com/new-banner.png")
                .isActive(false)
                .build();

        community = Community.builder()
                .id("comm-456")
                .name("Coding Club")
                .description("A community for coding enthusiasts.")
                .bannerUrl("http://example.com/banner.png")
                .councilId("council-123")
                .memberCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createCommunity_Successful() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(communityRepository.existsByName("Coding Club")).thenReturn(false);
        when(communityRepository.save(any(Community.class))).thenReturn(community);

        CommunityResponse response = communityService.createCommunity(createRequest);

        assertNotNull(response);
        assertEquals("comm-456", response.getId());
        assertEquals("Coding Club", response.getName());
        assertEquals("A community for coding enthusiasts.", response.getDescription());
        assertEquals("http://example.com/banner.png", response.getBannerUrl());
        assertEquals("council-123", response.getCouncilId());
        assertEquals(0, response.getMemberCount());
        assertTrue(response.getIsActive());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());

        verify(councilRepository).existsById("council-123");
        verify(communityRepository).existsByName("Coding Club");
        verify(communityRepository).save(any(Community.class));
    }

    @Test
    void createCommunity_ThrowsResourceNotFoundException_WhenCouncilDoesNotExist() {
        when(councilRepository.existsById("council-123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> communityService.createCommunity(createRequest));

        verify(councilRepository).existsById("council-123");
        verify(communityRepository, never()).existsByName(anyString());
        verify(communityRepository, never()).save(any(Community.class));
    }

    @Test
    void createCommunity_ThrowsConflictException_WhenCommunityNameExists() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(communityRepository.existsByName("Coding Club")).thenReturn(true);

        assertThrows(ConflictException.class, () -> communityService.createCommunity(createRequest));

        verify(councilRepository).existsById("council-123");
        verify(communityRepository).existsByName("Coding Club");
        verify(communityRepository, never()).save(any(Community.class));
    }

    @Test
    void updateCommunity_Successful() {
        when(communityRepository.findById("comm-456")).thenReturn(Optional.of(community));
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityResponse response = communityService.updateCommunity("comm-456", updateRequest);

        assertNotNull(response);
        assertEquals("comm-456", response.getId());
        assertEquals("Updated description.", response.getDescription());
        assertEquals("http://example.com/new-banner.png", response.getBannerUrl());
        assertFalse(response.getIsActive());

        verify(communityRepository).findById("comm-456");
        verify(communityRepository).save(any(Community.class));
    }

    @Test
    void updateCommunity_ThrowsResourceNotFoundException_WhenCommunityDoesNotExist() {
        when(communityRepository.findById("comm-456")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> communityService.updateCommunity("comm-456", updateRequest));

        verify(communityRepository).findById("comm-456");
        verify(communityRepository, never()).save(any(Community.class));
    }

    @Test
    void getCommunityById_Successful() {
        when(communityRepository.findById("comm-456")).thenReturn(Optional.of(community));

        CommunityResponse response = communityService.getCommunityById("comm-456");

        assertNotNull(response);
        assertEquals("comm-456", response.getId());
        assertEquals("Coding Club", response.getName());

        verify(communityRepository).findById("comm-456");
    }

    @Test
    void getCommunityById_ThrowsResourceNotFoundException_WhenCommunityDoesNotExist() {
        when(communityRepository.findById("comm-456")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> communityService.getCommunityById("comm-456"));

        verify(communityRepository).findById("comm-456");
    }

    @Test
    void getAllCommunities_Successful() {
        when(communityRepository.findByIsActiveTrue()).thenReturn(List.of(community));

        List<CommunitySummaryResponse> responses = communityService.getAllCommunities();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("comm-456", responses.get(0).getId());
        assertEquals("Coding Club", responses.get(0).getName());
        assertEquals("http://example.com/banner.png", responses.get(0).getBannerUrl());
        assertEquals(0, responses.get(0).getMemberCount());

        verify(communityRepository).findByIsActiveTrue();
    }

    @Test
    void getCommunitiesByCouncil_Successful() {
        when(councilRepository.existsById("council-123")).thenReturn(true);
        when(communityRepository.findByCouncilId("council-123")).thenReturn(List.of(community));

        List<CommunitySummaryResponse> responses = communityService.getCommunitiesByCouncil("council-123");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("comm-456", responses.get(0).getId());

        verify(councilRepository).existsById("council-123");
        verify(communityRepository).findByCouncilId("council-123");
    }

    @Test
    void getCommunitiesByCouncil_ThrowsResourceNotFoundException_WhenCouncilDoesNotExist() {
        when(councilRepository.existsById("council-123")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> communityService.getCommunitiesByCouncil("council-123"));

        verify(councilRepository).existsById("council-123");
        verify(communityRepository, never()).findByCouncilId(anyString());
    }
}
