package com.campusguide.personal.planner.service;

import com.campusguide.personal.planner.dto.CreateStudyGoalRequest;
import com.campusguide.personal.planner.dto.StudyGoalResponse;
import com.campusguide.personal.planner.dto.UpdateStudyGoalRequest;
import com.campusguide.personal.planner.entity.StudyGoal;
import com.campusguide.personal.planner.exception.PlannerTaskNotFoundException;
import com.campusguide.personal.planner.mapper.StudyGoalMapper;
import com.campusguide.personal.planner.repository.StudyGoalRepository;
import com.campusguide.platform.user.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyGoalServiceTest {

    @Mock
    private StudyGoalRepository studyGoalRepository;

    @Spy
    private StudyGoalMapper studyGoalMapper = new StudyGoalMapper();

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private StudyGoalService studyGoalService;

    private UserDetails userDetails;
    private UUID userId;
    private UUID goalId;
    private StudyGoal existingGoal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        goalId = UUID.randomUUID();

        userDetails = User.withUsername("test.student@ves.ac.in")
                .password("Password123")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        lenient().when(currentUserService.getCurrentUserId(any())).thenReturn(userId.toString());

        existingGoal = StudyGoal.builder()
                .id(goalId)
                .userId(userId.toString())
                .title("Complete Python Data Science Course")
                .description("NumPy, Pandas, Matplotlib")
                .targetHours(20)
                .completedHours(5)
                .deadline("2026-09-15")
                .category("Coursework")
                .isCompleted(false)
                .createdAt(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();
    }

    @Test
    void createGoal_Success() {
        when(studyGoalRepository.save(any(StudyGoal.class))).thenAnswer(i -> i.getArgument(0));

        CreateStudyGoalRequest request = CreateStudyGoalRequest.builder()
                .title("Algorithms Prep")
                .description("Dynamic programming problems")
                .targetHours(15)
                .deadline("2026-09-30")
                .category("Exam Prep")
                .build();

        StudyGoalResponse response = studyGoalService.createGoal(userDetails, request);

        assertNotNull(response);
        assertEquals("Algorithms Prep", response.getTitle());
        assertEquals(15, response.getTargetHours());
        assertEquals(0, response.getCompletedHours());
        assertFalse(response.getIsCompleted());
        assertEquals(userId.toString(), response.getUserId());
        verify(studyGoalRepository, times(1)).save(any(StudyGoal.class));
    }

    @Test
    void getGoals_Success() {
        when(studyGoalRepository.findByUserIdOrderByCreatedAtDesc(userId.toString()))
                .thenReturn(List.of(existingGoal));

        List<StudyGoalResponse> goals = studyGoalService.getGoals(userDetails);

        assertEquals(1, goals.size());
        assertEquals(goalId, goals.get(0).getId());
    }

    @Test
    void getGoalById_Success() {
        when(studyGoalRepository.findByIdAndUserId(goalId, userId.toString()))
                .thenReturn(Optional.of(existingGoal));

        StudyGoalResponse response = studyGoalService.getGoalById(userDetails, goalId);

        assertNotNull(response);
        assertEquals(goalId, response.getId());
    }

    @Test
    void getGoalById_NotFound_ThrowsException() {
        when(studyGoalRepository.findByIdAndUserId(goalId, userId.toString()))
                .thenReturn(Optional.empty());

        assertThrows(PlannerTaskNotFoundException.class,
                () -> studyGoalService.getGoalById(userDetails, goalId));
    }

    @Test
    void getGoalById_OtherUser_IndistinguishableFromNotFound_ThrowsException() {
        when(studyGoalRepository.findByIdAndUserId(goalId, userId.toString()))
                .thenReturn(Optional.empty());

        assertThrows(PlannerTaskNotFoundException.class,
                () -> studyGoalService.getGoalById(userDetails, goalId));
    }

    @Test
    void updateGoal_Success() {
        when(studyGoalRepository.findByIdAndUserId(goalId, userId.toString()))
                .thenReturn(Optional.of(existingGoal));
        when(studyGoalRepository.save(any(StudyGoal.class))).thenAnswer(i -> i.getArgument(0));

        UpdateStudyGoalRequest request = UpdateStudyGoalRequest.builder()
                .title("Advanced Python Data Science")
                .completedHours(10)
                .build();

        StudyGoalResponse response = studyGoalService.updateGoal(userDetails, goalId, request);

        assertEquals("Advanced Python Data Science", response.getTitle());
        assertEquals(10, response.getCompletedHours());
        assertFalse(response.getIsCompleted());
    }

    @Test
    void updateGoal_AutoSetsCompleted_WhenTargetHoursReached() {
        when(studyGoalRepository.findByIdAndUserId(goalId, userId.toString()))
                .thenReturn(Optional.of(existingGoal));
        when(studyGoalRepository.save(any(StudyGoal.class))).thenAnswer(i -> i.getArgument(0));

        UpdateStudyGoalRequest request = UpdateStudyGoalRequest.builder()
                .completedHours(20)
                .build();

        StudyGoalResponse response = studyGoalService.updateGoal(userDetails, goalId, request);

        assertEquals(20, response.getCompletedHours());
        assertTrue(response.getIsCompleted());
    }

    @Test
    void deleteGoal_Success() {
        when(studyGoalRepository.findByIdAndUserId(goalId, userId.toString()))
                .thenReturn(Optional.of(existingGoal));

        studyGoalService.deleteGoal(userDetails, goalId);

        verify(studyGoalRepository, times(1)).delete(existingGoal);
    }
}
