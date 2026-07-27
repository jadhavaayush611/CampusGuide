package com.campusguide.campus.council.service;

import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.campus.council.dto.CouncilResponse;
import com.campusguide.campus.council.dto.CreateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilStatusRequest;
import com.campusguide.campus.council.entity.Council;
import com.campusguide.campus.council.exception.CouncilHasDependenciesException;
import com.campusguide.campus.council.exception.CouncilNotFoundException;
import com.campusguide.campus.council.exception.DuplicateCouncilException;
import com.campusguide.campus.council.mapper.CouncilMapper;
import com.campusguide.campus.council.repository.CouncilRepository;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.campus.resource.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouncilServiceTest {

    @Mock
    private CouncilRepository councilRepository;

    @Spy
    private CouncilMapper councilMapper;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private CouncilService councilService;

    private UUID councilId;
    private Council council;
    private CreateCouncilRequest createRequest;
    private UpdateCouncilRequest updateRequest;

    @BeforeEach
    void setUp() {
        councilId = UUID.randomUUID();
        council = Council.builder()
                .id(councilId)
                .name("Sports Council")
                .slug("sports-council")
                .description("All athletics and sports activities")
                .logoUrl("https://example.com/sports.png")
                .email("sports@campus.edu")
                .contactNumber("12345678")
                .facultyAdvisor("Prof. Davis")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateCouncilRequest.builder()
                .name("Sports Council")
                .slug("sports-council")
                .description("All athletics and sports activities")
                .logoUrl("https://example.com/sports.png")
                .email("sports@campus.edu")
                .contactNumber("12345678")
                .facultyAdvisor("Prof. Davis")
                .isActive(true)
                .build();

        updateRequest = UpdateCouncilRequest.builder()
                .name("Updated Sports Council")
                .slug("updated-sports-council")
                .description("Updated description")
                .logoUrl("https://example.com/sports-new.png")
                .email("sports-new@campus.edu")
                .contactNumber("87654321")
                .facultyAdvisor("Prof. Adams")
                .isActive(false)
                .build();
    }

    @Test
    void createCouncil_Success() {
        when(councilRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(councilRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);
        when(councilRepository.save(any(Council.class))).thenReturn(council);

        CouncilResponse response = councilService.createCouncil(createRequest);

        assertNotNull(response);
        assertEquals("Sports Council", response.getName());
        assertEquals("sports-council", response.getSlug());
        verify(councilRepository).save(any(Council.class));
    }

    @Test
    void createCouncil_ThrowsDuplicateCouncilException_WhenNameExists() {
        when(councilRepository.existsByName(createRequest.getName())).thenReturn(true);

        assertThrows(DuplicateCouncilException.class, () -> councilService.createCouncil(createRequest));
        verify(councilRepository, never()).save(any(Council.class));
    }

    @Test
    void createCouncil_ThrowsDuplicateCouncilException_WhenSlugExists() {
        when(councilRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(councilRepository.existsBySlug(createRequest.getSlug())).thenReturn(true);

        assertThrows(DuplicateCouncilException.class, () -> councilService.createCouncil(createRequest));
        verify(councilRepository, never()).save(any(Council.class));
    }

    @Test
    void getAllCouncils_Success() {
        when(councilRepository.findAll()).thenReturn(List.of(council));

        List<CouncilResponse> result = councilService.getAllCouncils();

        assertEquals(1, result.size());
        assertEquals("Sports Council", result.get(0).getName());
    }

    @Test
    void getCouncilById_Success() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));

        CouncilResponse response = councilService.getCouncilById(councilId);

        assertNotNull(response);
        assertEquals(councilId, response.getId());
    }

    @Test
    void getCouncilById_ThrowsCouncilNotFoundException_WhenNotFound() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.empty());

        assertThrows(CouncilNotFoundException.class, () -> councilService.getCouncilById(councilId));
    }

    @Test
    void getCouncilBySlug_Success() {
        when(councilRepository.findBySlug("sports-council")).thenReturn(Optional.of(council));

        CouncilResponse response = councilService.getCouncilBySlug("sports-council");

        assertNotNull(response);
        assertEquals("sports-council", response.getSlug());
    }

    @Test
    void getCouncilBySlug_ThrowsCouncilNotFoundException_WhenNotFound() {
        when(councilRepository.findBySlug("unknown-slug")).thenReturn(Optional.empty());

        assertThrows(CouncilNotFoundException.class, () -> councilService.getCouncilBySlug("unknown-slug"));
    }

    @Test
    void updateCouncil_Success() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));
        when(councilRepository.existsByNameAndIdNot(updateRequest.getName(), councilId)).thenReturn(false);
        when(councilRepository.existsBySlugAndIdNot(updateRequest.getSlug(), councilId)).thenReturn(false);
        when(councilRepository.save(any(Council.class))).thenReturn(council);

        CouncilResponse response = councilService.updateCouncil(councilId, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Sports Council", response.getName());
        assertEquals("updated-sports-council", response.getSlug());
        assertFalse(response.getIsActive());
    }

    @Test
    void updateCouncil_ThrowsDuplicateCouncilException_WhenNameExistsOnOtherCouncil() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));
        when(councilRepository.existsByNameAndIdNot(updateRequest.getName(), councilId)).thenReturn(true);

        assertThrows(DuplicateCouncilException.class, () -> councilService.updateCouncil(councilId, updateRequest));
        verify(councilRepository, never()).save(any(Council.class));
    }

    @Test
    void updateCouncil_ThrowsDuplicateCouncilException_WhenSlugExistsOnOtherCouncil() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));
        when(councilRepository.existsByNameAndIdNot(updateRequest.getName(), councilId)).thenReturn(false);
        when(councilRepository.existsBySlugAndIdNot(updateRequest.getSlug(), councilId)).thenReturn(true);

        assertThrows(DuplicateCouncilException.class, () -> councilService.updateCouncil(councilId, updateRequest));
        verify(councilRepository, never()).save(any(Council.class));
    }

    @Test
    void updateCouncilStatus_Success() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));
        when(councilRepository.save(any(Council.class))).thenReturn(council);

        UpdateCouncilStatusRequest statusRequest = new UpdateCouncilStatusRequest(false);
        CouncilResponse response = councilService.updateCouncilStatus(councilId, statusRequest);

        assertNotNull(response);
        assertFalse(response.getIsActive());
    }

    @Test
    void deleteCouncil_Success_WhenNoDependencies() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));
        when(communityRepository.findByCouncilId(councilId.toString())).thenReturn(Collections.emptyList());
        when(eventRepository.findByCouncilIdAndIsDeletedFalse(councilId.toString())).thenReturn(Collections.emptyList());
        when(resourceRepository.findByCouncilIdAndIsDeletedFalseOrderByCreatedAtDesc(councilId.toString())).thenReturn(Collections.emptyList());

        councilService.deleteCouncil(councilId);

        verify(councilRepository).delete(council);
    }

    @Test
    void deleteCouncil_ThrowsCouncilHasDependenciesException_WhenCommunitiesExist() {
        when(councilRepository.findById(councilId)).thenReturn(Optional.of(council));
        Community community = Community.builder().id("comm-1").councilId(councilId.toString()).build();
        when(communityRepository.findByCouncilId(councilId.toString())).thenReturn(List.of(community));

        assertThrows(CouncilHasDependenciesException.class, () -> councilService.deleteCouncil(councilId));
        verify(councilRepository, never()).delete(any(Council.class));
    }
}
