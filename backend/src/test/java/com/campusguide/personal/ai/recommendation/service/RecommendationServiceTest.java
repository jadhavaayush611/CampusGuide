package com.campusguide.personal.ai.recommendation.service;

import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.common.exception.UnauthorisedException;
import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.personal.ai.recommendation.engine.RecommendationEngine;
import com.campusguide.campus.community.repository.CommunityRepository;
import com.campusguide.academic.course.repository.CourseRepository;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.campus.post.repository.PostRepository;
import com.campusguide.academic.progress.entity.StudentProgress;
import com.campusguide.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.resource.repository.ResourceRepository;
import com.campusguide.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.academic.semesterplanner.repository.SemesterPlanRepository;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentProgressRepository studentProgressRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private RoadmapRepository roadmapRepository;
    @Mock private SemesterPlanRepository semesterPlanRepository;
    @Mock private PostRepository postRepository;
    @Mock private EventRepository eventRepository;
    @Mock private CommunityRepository communityRepository;
    @Mock private ResourceRepository resourceRepository;
    @Mock private RecommendationEngine recommendationEngine;
    @Mock private com.campusguide.personal.notification.service.interfaces.NotificationService notificationService;


    @InjectMocks
    private RecommendationService recommendationService;

    private UserDetails userDetails;
    private User user;

    @BeforeEach
    void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        user = User.builder()
                .id("student-123")
                .email("student@campusguide.com")
                .firstName("John")
                .lastName("Doe")
                .department("Computer Science")
                .build();
    }

    @Test
    void getRecommendations_SuccessfulGeneration() {
        StudentProgress progress = StudentProgress.builder()
                .studentId("student-123")
                .currentSemester(1)
                .completedCourseIds(List.of("course-1"))
                .build();

        RecommendationResponse rec = RecommendationResponse.builder()
                .id("rec-1")
                .title("Recom 1")
                .recommendationType(RecommendationType.ACADEMIC)
                .score(0.9)
                .explanation("Ex")
                .build();

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(studentProgressRepository.findByStudentId(user.getId())).thenReturn(Optional.of(progress));
        when(courseRepository.findByActiveTrueOrderByCourseCodeAsc()).thenReturn(Collections.emptyList());
        when(roadmapRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(user.getId())).thenReturn(Collections.emptyList());
        when(postRepository.findByAuthorIdAndIsDeletedFalse(user.getId())).thenReturn(Collections.emptyList());
        when(eventRepository.findByIsDeletedFalseAndIsCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAsc(any())).thenReturn(Collections.emptyList());
        when(communityRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(resourceRepository.findByIsDeletedFalseOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

        when(recommendationEngine.generateAllRecommendations(any(RecommendationUserContext.class)))
                .thenReturn(List.of(rec));

        List<RecommendationResponse> results = recommendationService.getRecommendations(userDetails, null, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("rec-1", results.get(0).getId());

        verify(recommendationEngine, times(1)).generateAllRecommendations(any(RecommendationUserContext.class));
    }

    @Test
    void getRecommendations_EmptyContextAndNoRecommendations() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(studentProgressRepository.findByStudentId(user.getId())).thenReturn(Optional.empty());
        when(recommendationEngine.generateAllRecommendations(any(RecommendationUserContext.class)))
                .thenReturn(Collections.emptyList());

        List<RecommendationResponse> results = recommendationService.getRecommendations(userDetails, null, null, null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void getRecommendations_WithCategoryFilter() {
        RecommendationResponse recEvent = RecommendationResponse.builder()
                .id("event-rec")
                .title("Event Suggestion")
                .recommendationType(RecommendationType.EVENT)
                .score(0.85)
                .explanation("Matches event")
                .build();

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(user));
        when(studentProgressRepository.findByStudentId(user.getId())).thenReturn(Optional.empty());
        when(recommendationEngine.generateRecommendationsByType(any(RecommendationUserContext.class), eq(RecommendationType.EVENT)))
                .thenReturn(List.of(recEvent));

        List<RecommendationResponse> results = recommendationService.getRecommendations(userDetails, RecommendationType.EVENT, null, null);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("event-rec", results.get(0).getId());
        assertEquals(RecommendationType.EVENT, results.get(0).getRecommendationType());

        verify(recommendationEngine, never()).generateAllRecommendations(any());
        verify(recommendationEngine, times(1)).generateRecommendationsByType(any(), eq(RecommendationType.EVENT));
    }

    @Test
    void getRecommendations_Unauthenticated_ThrowsUnauthorisedException() {
        assertThrows(UnauthorisedException.class, () ->
                recommendationService.getRecommendations(null, null, null, null));
    }

    @Test
    void getRecommendations_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recommendationService.getRecommendations(userDetails, null, null, null));
    }
}
