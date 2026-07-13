package com.campusguide.modules.roadmap.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.exception.UnauthorisedException;
import com.campusguide.modules.roadmap.dto.CreateRoadmapRequest;
import com.campusguide.modules.roadmap.dto.RoadmapResponse;
import com.campusguide.modules.roadmap.dto.RoadmapSummaryResponse;
import com.campusguide.modules.roadmap.dto.UpdateRoadmapRequest;
import com.campusguide.modules.roadmap.entity.Roadmap;
import com.campusguide.modules.roadmap.repository.RoadmapRepository;
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
class RoadmapServiceTest {

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoadmapService roadmapService;

    private UserDetails creatorUserDetails;
    private UserDetails adminUserDetails;
    private UserDetails otherUserDetails;

    private User creatorUser;
    private User adminUser;
    private User otherUser;

    private CreateRoadmapRequest createRequest;
    private UpdateRoadmapRequest updateRequest;
    private Roadmap activeRoadmap;
    private Roadmap deletedRoadmap;

    @BeforeEach
    void setUp() {
        creatorUserDetails = org.springframework.security.core.userdetails.User.withUsername("creator@campusguide.com")
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

        creatorUser = User.builder()
                .id("user-creator")
                .email("creator@campusguide.com")
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

        createRequest = CreateRoadmapRequest.builder()
                .title("B.Tech Computer Science 2026")
                .description("Four year academic curriculum roadmap")
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .build();

        updateRequest = UpdateRoadmapRequest.builder()
                .title("B.Tech CSE Revised")
                .description("Updated semester breakdown")
                .degreeProgram("B.Tech CSE Honours")
                .department("CS and Engineering")
                .totalCredits(165)
                .expectedGraduationYear(2027)
                .build();

        activeRoadmap = Roadmap.builder()
                .id("roadmap-123")
                .title("B.Tech Computer Science 2026")
                .description("Four year academic curriculum roadmap")
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .createdBy("user-creator")
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        deletedRoadmap = Roadmap.builder()
                .id("roadmap-deleted")
                .title("Old curriculum roadmap")
                .description("Obsolete catalog")
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .totalCredits(150)
                .expectedGraduationYear(2025)
                .createdBy("user-creator")
                .isDeleted(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==========================================
    // CREATE ROADMAP TESTS
    // ==========================================

    @Test
    void createRoadmap_Success() {
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(activeRoadmap);

        RoadmapResponse response = roadmapService.createRoadmap(creatorUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("roadmap-123", response.getId());
        assertEquals("B.Tech Computer Science 2026", response.getTitle());
        assertEquals("user-creator", response.getCreatedBy());
        assertFalse(activeRoadmap.getIsDeleted());

        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    void createRoadmap_Success_NullOptionalDescription() {
        CreateRoadmapRequest requestWithNullDesc = CreateRoadmapRequest.builder()
                .title("B.Tech CSE")
                .description(null)
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .build();

        Roadmap activeRoadmapWithNullDesc = Roadmap.builder()
                .id("roadmap-123")
                .title("B.Tech CSE")
                .description(null)
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .createdBy("user-creator")
                .isDeleted(false)
                .build();

        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(activeRoadmapWithNullDesc);

        RoadmapResponse response = roadmapService.createRoadmap(creatorUserDetails, requestWithNullDesc);

        assertNotNull(response);
        assertNull(response.getDescription());
        assertEquals("roadmap-123", response.getId());
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    void createRoadmap_Unauthenticated() {
        assertThrows(UnauthorisedException.class, () ->
                roadmapService.createRoadmap(null, createRequest));
        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    @Test
    void createRoadmap_UserNotFound() {
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                roadmapService.createRoadmap(creatorUserDetails, createRequest));
        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    // ==========================================
    // UPDATE ROADMAP TESTS
    // ==========================================

    @Test
    void updateRoadmap_CreatorSuccess() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));
        
        Roadmap updatedRoadmap = Roadmap.builder()
                .id("roadmap-123")
                .title("B.Tech CSE Revised")
                .description("Updated semester breakdown")
                .degreeProgram("B.Tech CSE Honours")
                .department("CS and Engineering")
                .totalCredits(165)
                .expectedGraduationYear(2027)
                .createdBy("user-creator")
                .isDeleted(false)
                .build();
        
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(updatedRoadmap);

        RoadmapResponse response = roadmapService.updateRoadmap(creatorUserDetails, "roadmap-123", updateRequest);

        assertNotNull(response);
        assertEquals("B.Tech CSE Revised", response.getTitle());
        assertEquals("B.Tech CSE Honours", response.getDegreeProgram());
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    void updateRoadmap_AdminSuccess() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));
        
        Roadmap updatedRoadmap = Roadmap.builder()
                .id("roadmap-123")
                .title("B.Tech CSE Revised")
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .createdBy("user-creator")
                .isDeleted(false)
                .build();
        
        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(updatedRoadmap);

        RoadmapResponse response = roadmapService.updateRoadmap(adminUserDetails, "roadmap-123", updateRequest);

        assertNotNull(response);
        assertEquals("B.Tech CSE Revised", response.getTitle());
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    @Test
    void updateRoadmap_NonOwnerDenied() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(otherUserDetails.getUsername())).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                roadmapService.updateRoadmap(otherUserDetails, "roadmap-123", updateRequest));
        verify(roadmapRepository, never()).save(any(Roadmap.class));
    }

    @Test
    void updateRoadmap_PartialUpdates() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));

        UpdateRoadmapRequest partialRequest = UpdateRoadmapRequest.builder()
                .title("New Title Only")
                .build();

        Roadmap partiallyUpdatedRoadmap = Roadmap.builder()
                .id("roadmap-123")
                .title("New Title Only")
                .description("Four year academic curriculum roadmap")
                .degreeProgram("B.Tech CSE")
                .department("Computer Science")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .createdBy("user-creator")
                .isDeleted(false)
                .build();

        when(roadmapRepository.save(any(Roadmap.class))).thenReturn(partiallyUpdatedRoadmap);

        RoadmapResponse response = roadmapService.updateRoadmap(creatorUserDetails, "roadmap-123", partialRequest);

        assertNotNull(response);
        assertEquals("New Title Only", response.getTitle());
        assertEquals("Four year academic curriculum roadmap", response.getDescription()); // unchanged
        assertEquals("B.Tech CSE", response.getDegreeProgram()); // unchanged
        verify(roadmapRepository, times(1)).save(any(Roadmap.class));
    }

    // ==========================================
    // DELETE ROADMAP TESTS
    // ==========================================

    @Test
    void deleteRoadmap_Creator() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));

        roadmapService.deleteRoadmap(creatorUserDetails, "roadmap-123");

        assertTrue(activeRoadmap.getIsDeleted());
        verify(roadmapRepository, times(1)).save(activeRoadmap);
    }

    @Test
    void deleteRoadmap_Admin() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));

        roadmapService.deleteRoadmap(adminUserDetails, "roadmap-123");

        assertTrue(activeRoadmap.getIsDeleted());
        verify(roadmapRepository, times(1)).save(activeRoadmap);
    }

    @Test
    void deleteRoadmap_AlreadyDeleted() {
        when(roadmapRepository.findById("roadmap-deleted")).thenReturn(Optional.of(deletedRoadmap));

        assertThrows(ResourceNotFoundException.class, () ->
                roadmapService.deleteRoadmap(creatorUserDetails, "roadmap-deleted"));
        verify(roadmapRepository, never()).save(deletedRoadmap);
    }

    @Test
    void deleteRoadmap_NotFound() {
        when(roadmapRepository.findById("roadmap-none")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                roadmapService.deleteRoadmap(creatorUserDetails, "roadmap-none"));
    }

    // ==========================================
    // RETRIEVAL TESTS
    // ==========================================

    @Test
    void getRoadmapById_Success() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));

        RoadmapResponse response = roadmapService.getRoadmapById("roadmap-123");

        assertNotNull(response);
        assertEquals("roadmap-123", response.getId());
        assertEquals("B.Tech Computer Science 2026", response.getTitle());
    }

    @Test
    void getRoadmapById_Deleted() {
        when(roadmapRepository.findById("roadmap-deleted")).thenReturn(Optional.of(deletedRoadmap));

        assertThrows(ResourceNotFoundException.class, () ->
                roadmapService.getRoadmapById("roadmap-deleted"));
    }

    @Test
    void getRoadmapById_NotFound() {
        when(roadmapRepository.findById("roadmap-none")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                roadmapService.getRoadmapById("roadmap-none"));
    }

    @Test
    void getAllRoadmaps_Success() {
        when(roadmapRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(activeRoadmap));

        List<RoadmapSummaryResponse> response = roadmapService.getAllRoadmaps();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("roadmap-123", response.get(0).getId());
    }

    @Test
    void getRoadmapsByCreator_Success() {
        when(userRepository.existsById("user-creator")).thenReturn(true);
        when(roadmapRepository.findByCreatedByAndIsDeletedFalseOrderByCreatedAtDesc("user-creator")).thenReturn(List.of(activeRoadmap));

        List<RoadmapSummaryResponse> response = roadmapService.getRoadmapsByCreator("user-creator");

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("roadmap-123", response.get(0).getId());
    }

    @Test
    void getRoadmapsByCreator_CreatorNotFound() {
        when(userRepository.existsById("user-none")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                roadmapService.getRoadmapsByCreator("user-none"));
    }

    @Test
    void getRoadmapsByDegree_Success() {
        when(roadmapRepository.findByDegreeProgramIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("B.Tech CSE"))
                .thenReturn(List.of(activeRoadmap));

        List<RoadmapSummaryResponse> response = roadmapService.getRoadmapsByDegree("  B.Tech CSE  "); // checks trimming

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("roadmap-123", response.get(0).getId());
    }

    @Test
    void getRoadmapsByDepartment_Success() {
        when(roadmapRepository.findByDepartmentIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc("Computer Science"))
                .thenReturn(List.of(activeRoadmap));

        List<RoadmapSummaryResponse> response = roadmapService.getRoadmapsByDepartment("  Computer Science  "); // checks trimming

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("roadmap-123", response.get(0).getId());
    }

    // ==========================================
    // VALIDATION TESTS
    // ==========================================

    @Test
    void createRoadmap_BlankDegree() {
        CreateRoadmapRequest request = CreateRoadmapRequest.builder()
                .title("B.Tech CSE")
                .degreeProgram("   ")
                .department("Computer Science")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .build();

        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));

        assertThrows(BadRequestException.class, () ->
                roadmapService.createRoadmap(creatorUserDetails, request));
    }

    @Test
    void createRoadmap_BlankDepartment() {
        CreateRoadmapRequest request = CreateRoadmapRequest.builder()
                .title("B.Tech CSE")
                .degreeProgram("B.Tech CSE")
                .department("")
                .totalCredits(160)
                .expectedGraduationYear(2026)
                .build();

        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));

        assertThrows(BadRequestException.class, () ->
                roadmapService.createRoadmap(creatorUserDetails, request));
    }

    @Test
    void updateRoadmap_BlankDegree() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));

        UpdateRoadmapRequest blankRequest = UpdateRoadmapRequest.builder()
                .degreeProgram("   ")
                .build();

        assertThrows(BadRequestException.class, () ->
                roadmapService.updateRoadmap(creatorUserDetails, "roadmap-123", blankRequest));
    }

    @Test
    void updateRoadmap_BlankDepartment() {
        when(roadmapRepository.findById("roadmap-123")).thenReturn(Optional.of(activeRoadmap));
        when(userRepository.findByEmail(creatorUserDetails.getUsername())).thenReturn(Optional.of(creatorUser));

        UpdateRoadmapRequest blankRequest = UpdateRoadmapRequest.builder()
                .department("")
                .build();

        assertThrows(BadRequestException.class, () ->
                roadmapService.updateRoadmap(creatorUserDetails, "roadmap-123", blankRequest));
    }

    @Test
    void getRoadmapsByDegree_BlankDegree() {
        assertThrows(BadRequestException.class, () ->
                roadmapService.getRoadmapsByDegree("   "));
    }

    @Test
    void getRoadmapsByDepartment_BlankDepartment() {
        assertThrows(BadRequestException.class, () ->
                roadmapService.getRoadmapsByDepartment(""));
    }
}
