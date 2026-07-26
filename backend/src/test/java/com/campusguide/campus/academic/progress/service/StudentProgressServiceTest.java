package com.campusguide.campus.academic.progress.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.campus.academic.course.dto.CourseResponse;
import com.campusguide.campus.academic.course.service.CourseService;
import com.campusguide.campus.academic.progress.dto.AdminUpdateStudentProgressRequest;
import com.campusguide.campus.academic.progress.dto.CreateStudentProgressRequest;
import com.campusguide.campus.academic.progress.dto.StudentProgressResponse;
import com.campusguide.campus.academic.progress.dto.UpdateStudentProgressRequest;
import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.academic.roadmap.dto.RoadmapResponse;
import com.campusguide.campus.academic.roadmap.service.RoadmapService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProgressServiceTest {

    @Mock
    private StudentProgressRepository studentProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private RoadmapService roadmapService;

    @InjectMocks
    private StudentProgressService studentProgressService;

    private User studentUser;
    private User adminUser;
    private User otherStudentUser;

    private UserDetails studentUserDetails;
    private UserDetails adminUserDetails;
    private UserDetails otherStudentUserDetails;

    private CreateStudentProgressRequest createRequest;
    private UpdateStudentProgressRequest updateRequest;
    private AdminUpdateStudentProgressRequest adminUpdateRequest;
    private StudentProgress studentProgress;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id("student-123")
                .email("student@test.com")
                .role(Role.STUDENT)
                .build();

        adminUser = User.builder()
                .id("admin-456")
                .email("admin@test.com")
                .role(Role.SUPER_ADMIN)
                .build();

        otherStudentUser = User.builder()
                .id("student-789")
                .email("other@test.com")
                .role(Role.STUDENT)
                .build();

        studentUserDetails = org.springframework.security.core.userdetails.User.withUsername("student@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminUserDetails = org.springframework.security.core.userdetails.User.withUsername("admin@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        otherStudentUserDetails = org.springframework.security.core.userdetails.User.withUsername("other@test.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        createRequest = CreateStudentProgressRequest.builder()
                .roadmapId("roadmap-123")
                .build();

        updateRequest = UpdateStudentProgressRequest.builder()
                .currentSemester(2)
                .build();

        adminUpdateRequest = AdminUpdateStudentProgressRequest.builder()
                .studentId("student-123")
                .currentSemester(2)
                .currentGpa(8.5)
                .totalCreditsEarned(30)
                .graduationEligible(false)
                .build();

        studentProgress = StudentProgress.builder()
                .id("progress-123")
                .studentId("student-123")
                .roadmapId("roadmap-123")
                .completedCourseIds(new ArrayList<>())
                .currentSemester(1)
                .totalCreditsEarned(0)
                .currentGpa(0.0)
                .graduationEligible(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- CREATE PROGRESS TESTS ---

    @Test
    void createProgress_Successful() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.empty());
        when(roadmapService.getRoadmapById("roadmap-123")).thenReturn(RoadmapResponse.builder().id("roadmap-123").build());
        when(studentProgressRepository.save(any(StudentProgress.class))).thenReturn(studentProgress);

        StudentProgressResponse response = studentProgressService.createProgress(studentUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("student-123", response.getStudentId());
        assertEquals("roadmap-123", response.getRoadmapId());
        assertEquals(1, response.getCurrentSemester());
        assertEquals(0, response.getTotalCreditsEarned());
        assertEquals(0.0, response.getCurrentGpa());
        assertFalse(response.getGraduationEligible());
        assertTrue(response.getCompletedCourseIds().isEmpty());

        verify(studentProgressRepository).save(any(StudentProgress.class));
    }

    @Test
    void createProgress_DuplicateProgress_ThrowsConflictException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));

        assertThrows(ConflictException.class, () -> studentProgressService.createProgress(studentUserDetails, createRequest));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void createProgress_RoadmapNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.empty());
        when(roadmapService.getRoadmapById("roadmap-123")).thenThrow(new ResourceNotFoundException("Roadmap not found"));

        assertThrows(ResourceNotFoundException.class, () -> studentProgressService.createProgress(studentUserDetails, createRequest));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    // --- UPDATE PROGRESS TESTS ---

    @Test
    void updateProgress_Owner_Successful() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProgressResponse response = studentProgressService.updateProgress(studentUserDetails, updateRequest);

        assertNotNull(response);
        assertEquals(2, response.getCurrentSemester());
        assertEquals(0.0, response.getCurrentGpa());
        assertEquals(0, response.getTotalCreditsEarned());
    }

    @Test
    void adminUpdateProgress_SuperAdmin_Successful() {
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));
        when(studentProgressRepository.findByStudentId("student-123")).thenReturn(Optional.of(studentProgress));
        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProgressResponse response = studentProgressService.adminUpdateProgress(adminUserDetails, adminUpdateRequest);

        assertNotNull(response);
        assertEquals(2, response.getCurrentSemester());
        assertEquals(8.5, response.getCurrentGpa());
        assertEquals(0, response.getTotalCreditsEarned());
    }

    @Test
    void adminUpdateProgress_Student_ThrowsAccessDeniedException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));

        assertThrows(AccessDeniedException.class, () -> studentProgressService.adminUpdateProgress(studentUserDetails, adminUpdateRequest));
        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void updateProgress_Unauthorized_ThrowsAccessDeniedException() {
        when(userRepository.findByEmail(otherStudentUserDetails.getUsername())).thenReturn(Optional.of(otherStudentUser));
        // We're attempting to update progress of student-123, but we are otherStudentUser (student-789)
        when(studentProgressRepository.findByStudentId("student-123")).thenReturn(Optional.of(studentProgress));
        updateRequest.setStudentId("student-123");

        assertThrows(AccessDeniedException.class, () -> studentProgressService.updateProgress(otherStudentUserDetails, updateRequest));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void adminUpdateProgress_InvalidGpa_ThrowsBadRequestException() {
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));
        when(studentProgressRepository.findByStudentId("student-123")).thenReturn(Optional.of(studentProgress));

        // GPA too high
        adminUpdateRequest.setCurrentGpa(11.0);
        assertThrows(BadRequestException.class, () -> studentProgressService.adminUpdateProgress(adminUserDetails, adminUpdateRequest));

        // GPA too low
        adminUpdateRequest.setCurrentGpa(-1.0);
        assertThrows(BadRequestException.class, () -> studentProgressService.adminUpdateProgress(adminUserDetails, adminUpdateRequest));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    @Test
    void updateProgress_InvalidSemester_ThrowsBadRequestException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));

        // Semester <= 0
        updateRequest.setCurrentSemester(0);
        assertThrows(BadRequestException.class, () -> studentProgressService.updateProgress(studentUserDetails, updateRequest));

        updateRequest.setCurrentSemester(-5);
        assertThrows(BadRequestException.class, () -> studentProgressService.updateProgress(studentUserDetails, updateRequest));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    // --- MARK COURSE COMPLETED TESTS ---

    @Test
    void markCourseCompleted_Successful() {
        CourseResponse courseResponse = CourseResponse.builder()
                .id("course-456")
                .credits(4)
                .active(true)
                .build();

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(courseService.getCourseById("course-456")).thenReturn(courseResponse);
        when(courseService.getCourseByIdInternal("course-456")).thenReturn(courseResponse);
        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProgressResponse response = studentProgressService.markCourseCompleted(studentUserDetails, "course-456", null);

        assertNotNull(response);
        assertEquals(1, response.getCompletedCourseIds().size());
        assertTrue(response.getCompletedCourseIds().contains("course-456"));
        assertEquals(4, response.getTotalCreditsEarned()); // Credits calculations
    }

    @Test
    void markCourseCompleted_Duplicate_ThrowsConflictException() {
        studentProgress.getCompletedCourseIds().add("course-456");

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(courseService.getCourseById("course-456")).thenReturn(CourseResponse.builder().id("course-456").credits(4).build());

        assertThrows(ConflictException.class, () -> studentProgressService.markCourseCompleted(studentUserDetails, "course-456", null));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }

    // --- REMOVE COMPLETED COURSE TESTS ---

    @Test
    void removeCompletedCourse_Successful() {
        studentProgress.getCompletedCourseIds().add("course-456");
        studentProgress.getCompletedCourseIds().add("course-789");

        CourseResponse courseResponse456 = CourseResponse.builder()
                .id("course-456")
                .credits(4)
                .build();

        CourseResponse courseResponse789 = CourseResponse.builder()
                .id("course-789")
                .credits(6)
                .build();

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(courseService.getCourseByIdInternal("course-456")).thenReturn(courseResponse456);
        when(courseService.getCourseByIdInternal("course-789")).thenReturn(courseResponse789);
        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProgressResponse response = studentProgressService.removeCompletedCourse(studentUserDetails, "course-456", null);

        assertNotNull(response);
        assertEquals(1, response.getCompletedCourseIds().size());
        assertTrue(response.getCompletedCourseIds().contains("course-789"));
        assertEquals(6, response.getTotalCreditsEarned()); // Derived from remaining course-789
    }

    @Test
    void removeCompletedCourse_NegativeCreditsPrevention_Successful() {
        studentProgress.getCompletedCourseIds().add("course-456");
        studentProgress.setTotalCreditsEarned(2); // Credits earned is lower than course credits

        CourseResponse courseResponse = CourseResponse.builder()
                .id("course-456")
                .credits(4)
                .build();

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(courseService.getCourseByIdInternal("course-456")).thenReturn(courseResponse);
        when(studentProgressRepository.save(any(StudentProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentProgressResponse response = studentProgressService.removeCompletedCourse(studentUserDetails, "course-456", null);

        assertNotNull(response);
        assertEquals(0, response.getTotalCreditsEarned()); // Credits set to 0, never negative
    }

    @Test
    void removeCompletedCourse_NotCompleted_ThrowsBadRequestException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));

        assertThrows(BadRequestException.class, () -> studentProgressService.removeCompletedCourse(studentUserDetails, "course-456", null));

        verify(studentProgressRepository, never()).save(any(StudentProgress.class));
    }
}
