package com.campusguide.modules.semester.service;

import com.campusguide.exception.BadRequestException;
import com.campusguide.exception.ConflictException;
import com.campusguide.exception.ResourceNotFoundException;
import com.campusguide.modules.course.dto.CourseResponse;
import com.campusguide.modules.course.service.CourseService;
import com.campusguide.modules.progress.entity.StudentProgress;
import com.campusguide.modules.progress.repository.StudentProgressRepository;
import com.campusguide.modules.roadmap.dto.RoadmapResponse;
import com.campusguide.modules.roadmap.service.RoadmapService;
import com.campusguide.modules.semester.dto.*;
import com.campusguide.modules.semester.entity.SemesterPlan;
import com.campusguide.modules.semester.repository.SemesterPlanRepository;
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
class SemesterPlanServiceTest {

    @Mock
    private SemesterPlanRepository semesterPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private RoadmapService roadmapService;

    @Mock
    private StudentProgressRepository studentProgressRepository;

    @InjectMocks
    private SemesterPlanService semesterPlanService;

    private User studentUser;
    private User adminUser;
    private User otherStudentUser;

    private UserDetails studentUserDetails;
    private UserDetails adminUserDetails;
    private UserDetails otherStudentUserDetails;

    private CreateSemesterPlanRequest createRequest;
    private SemesterPlan semesterPlan;
    private StudentProgress studentProgress;
    private CourseResponse testCourse;
    private CourseResponse prereqCourse;

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

        createRequest = CreateSemesterPlanRequest.builder()
                .roadmapId("roadmap-123")
                .semesterNumber(1)
                .build();

        semesterPlan = SemesterPlan.builder()
                .id("plan-123")
                .studentId("student-123")
                .roadmapId("roadmap-123")
                .semesterNumber(1)
                .plannedCourseIds(new ArrayList<>())
                .totalPlannedCredits(0)
                .finalized(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .build();

        prereqCourse = CourseResponse.builder()
                .id("course-prereq")
                .courseCode("CS100")
                .credits(3)
                .active(true)
                .prerequisiteCourseIds(new ArrayList<>())
                .build();

        testCourse = CourseResponse.builder()
                .id("course-123")
                .courseCode("CS101")
                .credits(4)
                .active(true)
                .prerequisiteCourseIds(new ArrayList<>(List.of("course-prereq")))
                .build();
    }

    // --- CREATE PLANS TESTS ---

    @Test
    void createSemesterPlan_Success() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(roadmapService.getRoadmapById("roadmap-123")).thenReturn(RoadmapResponse.builder().id("roadmap-123").build());
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(semesterPlanRepository.findByStudentIdAndSemesterNumber(studentUser.getId(), 1)).thenReturn(Optional.empty());
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenReturn(semesterPlan);

        SemesterPlanResponse response = semesterPlanService.createSemesterPlan(studentUserDetails, createRequest);

        assertNotNull(response);
        assertEquals("plan-123", response.getId());
        assertEquals("student-123", response.getStudentId());
        assertEquals("roadmap-123", response.getRoadmapId());
        assertEquals(1, response.getSemesterNumber());
        assertFalse(response.getFinalized());
        assertEquals(0, response.getTotalPlannedCredits());
    }

    @Test
    void createSemesterPlan_Duplicate_ThrowsConflictException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(roadmapService.getRoadmapById("roadmap-123")).thenReturn(RoadmapResponse.builder().id("roadmap-123").build());
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(studentProgress));
        when(semesterPlanRepository.findByStudentIdAndSemesterNumber(studentUser.getId(), 1)).thenReturn(Optional.of(semesterPlan));

        assertThrows(ConflictException.class, () -> semesterPlanService.createSemesterPlan(studentUserDetails, createRequest));
    }

    @Test
    void createSemesterPlan_RoadmapNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(roadmapService.getRoadmapById("roadmap-123")).thenThrow(new ResourceNotFoundException("Roadmap not found"));

        assertThrows(ResourceNotFoundException.class, () -> semesterPlanService.createSemesterPlan(studentUserDetails, createRequest));
    }

    @Test
    void createSemesterPlan_ProgressNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(roadmapService.getRoadmapById("roadmap-123")).thenReturn(RoadmapResponse.builder().id("roadmap-123").build());
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> semesterPlanService.createSemesterPlan(studentUserDetails, createRequest));
    }

    // --- ADD COURSE TESTS ---

    @Test
    void addCourse_PrerequisiteNotCompleted_ThrowsBadRequestException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(courseService.getCourseById("course-123")).thenReturn(testCourse);
        when(studentProgressRepository.findByStudentId("student-123")).thenReturn(Optional.of(studentProgress));

        assertThrows(BadRequestException.class, () -> semesterPlanService.addCourse(studentUserDetails, "plan-123", "course-123"));
    }

    @Test
    void addCourse_PrerequisiteCompleted_Success() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(courseService.getCourseById("course-123")).thenReturn(testCourse);
        
        studentProgress.getCompletedCourseIds().add("course-prereq");
        when(studentProgressRepository.findByStudentId("student-123")).thenReturn(Optional.of(studentProgress));
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SemesterPlanResponse response = semesterPlanService.addCourse(studentUserDetails, "plan-123", "course-123");

        assertNotNull(response);
        assertTrue(response.getPlannedCourseIds().contains("course-123"));
        assertEquals(4, response.getTotalPlannedCredits());
    }

    @Test
    void addCourse_DuplicateCourse_ThrowsConflictException() {
        semesterPlan.getPlannedCourseIds().add("course-123");

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(courseService.getCourseById("course-123")).thenReturn(testCourse);

        assertThrows(ConflictException.class, () -> semesterPlanService.addCourse(studentUserDetails, "plan-123", "course-123"));
    }

    @Test
    void addCourse_WithoutPrerequisites_Success() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(courseService.getCourseById("course-prereq")).thenReturn(prereqCourse);
        when(studentProgressRepository.findByStudentId("student-123")).thenReturn(Optional.of(studentProgress));
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SemesterPlanResponse response = semesterPlanService.addCourse(studentUserDetails, "plan-123", "course-prereq");

        assertNotNull(response);
        assertTrue(response.getPlannedCourseIds().contains("course-prereq"));
        assertEquals(3, response.getTotalPlannedCredits());
    }

    // --- REMOVE COURSE TESTS ---

    @Test
    void removeCourse_Success() {
        semesterPlan.getPlannedCourseIds().add("course-prereq");
        semesterPlan.setTotalPlannedCredits(3);

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(courseService.getCourseById("course-prereq")).thenReturn(prereqCourse);
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SemesterPlanResponse response = semesterPlanService.removeCourse(studentUserDetails, "plan-123", "course-prereq");

        assertNotNull(response);
        assertFalse(response.getPlannedCourseIds().contains("course-prereq"));
        assertEquals(0, response.getTotalPlannedCredits());
    }

    @Test
    void removeCourse_MissingCourse_ThrowsBadRequestException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));

        assertThrows(BadRequestException.class, () -> semesterPlanService.removeCourse(studentUserDetails, "plan-123", "course-prereq"));
    }

    // --- FINALIZE PLANS TESTS ---

    @Test
    void finalizeSemesterPlan_Success() {
        semesterPlan.getPlannedCourseIds().add("course-prereq");
        semesterPlan.setTotalPlannedCredits(3);

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SemesterPlanResponse response = semesterPlanService.finalizeSemesterPlan(studentUserDetails, "plan-123");

        assertNotNull(response);
        assertTrue(response.getFinalized());
    }

    @Test
    void finalizeSemesterPlan_EmptyPlan_ThrowsBadRequestException() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));

        assertThrows(BadRequestException.class, () -> semesterPlanService.finalizeSemesterPlan(studentUserDetails, "plan-123"));
    }

    // --- UPDATE GENERAL AND FINALIZED PLANS TESTS ---

    @Test
    void updateSemesterPlan_Finalized_ThrowsBadRequestException() {
        semesterPlan.setFinalized(true);

        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));

        UpdateSemesterPlanRequest updateReq = UpdateSemesterPlanRequest.builder().semesterNumber(2).build();

        assertThrows(BadRequestException.class, () -> semesterPlanService.updateSemesterPlan(studentUserDetails, "plan-123", updateReq));
    }

    @Test
    void updateSemesterPlan_CreditCalculations_Success() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(courseService.getCourseById("course-prereq")).thenReturn(prereqCourse);
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSemesterPlanRequest updateReq = UpdateSemesterPlanRequest.builder()
                .plannedCourseIds(List.of("course-prereq"))
                .build();

        SemesterPlanResponse response = semesterPlanService.updateSemesterPlan(studentUserDetails, "plan-123", updateReq);

        assertNotNull(response);
        assertEquals(3, response.getTotalPlannedCredits());
    }

    // --- AUTHORIZATION TESTS ---

    @Test
    void updateSemesterPlan_Owner_Success() {
        when(userRepository.findByEmail(studentUserDetails.getUsername())).thenReturn(Optional.of(studentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSemesterPlanRequest updateReq = UpdateSemesterPlanRequest.builder().semesterNumber(2).build();

        SemesterPlanResponse response = semesterPlanService.updateSemesterPlan(studentUserDetails, "plan-123", updateReq);

        assertNotNull(response);
        assertEquals(2, response.getSemesterNumber());
    }

    @Test
    void updateSemesterPlan_Admin_Success() {
        when(userRepository.findByEmail(adminUserDetails.getUsername())).thenReturn(Optional.of(adminUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));
        when(semesterPlanRepository.save(any(SemesterPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSemesterPlanRequest updateReq = UpdateSemesterPlanRequest.builder().semesterNumber(3).build();

        SemesterPlanResponse response = semesterPlanService.updateSemesterPlan(adminUserDetails, "plan-123", updateReq);

        assertNotNull(response);
        assertEquals(3, response.getSemesterNumber());
    }

    @Test
    void updateSemesterPlan_OtherStudent_ThrowsAccessDeniedException() {
        when(userRepository.findByEmail(otherStudentUserDetails.getUsername())).thenReturn(Optional.of(otherStudentUser));
        when(semesterPlanRepository.findById("plan-123")).thenReturn(Optional.of(semesterPlan));

        UpdateSemesterPlanRequest updateReq = UpdateSemesterPlanRequest.builder().semesterNumber(2).build();

        assertThrows(AccessDeniedException.class, () -> semesterPlanService.updateSemesterPlan(otherStudentUserDetails, "plan-123", updateReq));
    }
}
