package com.campusguide.academic.service;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.academic.dto.AcademicDashboardResponse;
import com.campusguide.academic.dto.AcademicProgressResponse;
import com.campusguide.academic.dto.RecommendedSemesterResponse;
import com.campusguide.academic.course.entity.Course;
import com.campusguide.academic.course.repository.CourseRepository;
import com.campusguide.academic.progress.entity.StudentProgress;
import com.campusguide.academic.progress.repository.StudentProgressRepository;
import com.campusguide.academic.roadmap.entity.Roadmap;
import com.campusguide.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.academic.semesterplanner.entity.SemesterPlan;
import com.campusguide.academic.semesterplanner.repository.SemesterPlanRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AcademicServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProgressRepository studentProgressRepository;

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private SemesterPlanRepository semesterPlanRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private AcademicService academicService;

    private User studentUser;
    private UserDetails userDetails;
    private StudentProgress progress;
    private Roadmap roadmap;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id("student-1")
                .email("student@campusguide.com")
                .role(Role.STUDENT)
                .build();

        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        progress = StudentProgress.builder()
                .id("progress-1")
                .studentId("student-1")
                .roadmapId("roadmap-1")
                .completedCourseIds(new ArrayList<>())
                .currentSemester(2)
                .totalCreditsEarned(12)
                .currentGpa(8.5)
                .graduationEligible(false)
                .build();

        roadmap = Roadmap.builder()
                .id("roadmap-1")
                .title("BS Computer Science")
                .degreeProgram("Computer Science")
                .department("CS")
                .totalCredits(120)
                .isDeleted(false)
                .build();
    }

    @Test
    void getDashboard_Success() {
        progress.getCompletedCourseIds().add("course-101");
        progress.setTotalCreditsEarned(4);

        SemesterPlan plan = SemesterPlan.builder()
                .id("plan-1")
                .studentId("student-1")
                .roadmapId("roadmap-1")
                .semesterNumber(2)
                .plannedCourseIds(List.of("course-201"))
                .totalPlannedCredits(4)
                .finalized(true)
                .build();

        Course course101 = Course.builder().id("course-101").courseCode("CS101").department("CS").credits(4).active(true).build();
        Course course201 = Course.builder().id("course-201").courseCode("CS201").department("CS").credits(4).active(true).build();
        Course course301 = Course.builder().id("course-301").courseCode("CS301").department("CS").credits(4).active(true).build();

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(studentUser.getId())).thenReturn(List.of(plan));
        when(courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc(roadmap.getDepartment())).thenReturn(List.of(course101, course201, course301));
        when(courseRepository.findAllById(List.of("course-101"))).thenReturn(List.of(course101));

        AcademicDashboardResponse response = academicService.getDashboard(userDetails);

        assertNotNull(response);
        assertEquals("BS Computer Science", response.getRoadmapTitle());
        assertEquals("CS", response.getDepartment());
        assertEquals(4, response.getTotalCreditsEarned());
        assertEquals(116, response.getRemainingCredits());
        assertEquals(3.33, response.getCompletionPercentage()); // (4 / 120) * 100 = 3.333... -> 3.33
        assertEquals(4, response.getPlannedCredits());
        assertTrue(response.getFinalizedSemesterPlan());
        assertEquals(1, response.getCompletedCourses().size());
        assertEquals("CS101", response.getCompletedCourses().get(0).getCourseCode());
        assertEquals(1, response.getRemainingCourses().size());
        assertEquals("CS301", response.getRemainingCourses().get(0).getCourseCode());
    }

    @Test
    void getDashboard_MissingProgress() {
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> academicService.getDashboard(userDetails));
    }

    @Test
    void getDashboard_MissingRoadmap() {
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> academicService.getDashboard(userDetails));
    }

    @Test
    void getDashboard_MissingSemesterPlan() {
        Course course101 = Course.builder().id("course-101").courseCode("CS101").department("CS").credits(4).active(true).build();

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(studentUser.getId())).thenReturn(Collections.emptyList());
        when(courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc(roadmap.getDepartment())).thenReturn(List.of(course101));
        when(courseRepository.findAllById(any())).thenReturn(Collections.emptyList());

        AcademicDashboardResponse response = academicService.getDashboard(userDetails);

        assertNotNull(response);
        assertEquals(0, response.getPlannedCredits());
        assertFalse(response.getFinalizedSemesterPlan());
    }

    @Test
    void getProgress_CompletionCalculations() {
        progress.setTotalCreditsEarned(30);

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(semesterPlanRepository.findByStudentIdOrderBySemesterNumberAsc(studentUser.getId())).thenReturn(Collections.emptyList());
        when(courseRepository.findByDepartmentAndActiveTrueOrderByCourseCodeAsc(roadmap.getDepartment())).thenReturn(Collections.emptyList());

        AcademicProgressResponse response = academicService.getProgress(userDetails);

        assertNotNull(response);
        assertEquals(30, response.getCreditsEarned());
        assertEquals(90, response.getCreditsRemaining());
        assertEquals(25.0, response.getCompletionPercentage()); // (30 / 120) * 100 = 25.0
    }

    @Test
    void getRecommendedSemester_PrerequisitesSatisfied() {
        progress.getCompletedCourseIds().add("CS101");

        Course recommendedCourse = Course.builder()
                .id("CS201")
                .courseCode("CS201")
                .department("CS")
                .semester(2)
                .credits(4)
                .active(true)
                .prerequisiteCourseIds(List.of("CS101"))
                .build();

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(2)).thenReturn(List.of(recommendedCourse));

        RecommendedSemesterResponse response = academicService.getRecommendedSemester(userDetails, 2);

        assertNotNull(response);
        assertEquals(1, response.getRecommendedCourseIds().size());
        assertEquals("CS201", response.getRecommendedCourseIds().get(0));
        assertEquals(4, response.getTotalCredits());
        assertTrue(response.getPrerequisiteWarnings().isEmpty());
    }

    @Test
    void getRecommendedSemester_PrerequisitesMissing() {
        Course recommendedCourse = Course.builder()
                .id("CS201")
                .courseCode("CS201")
                .department("CS")
                .semester(2)
                .credits(4)
                .active(true)
                .prerequisiteCourseIds(List.of("CS101"))
                .build();

        Course prerequisiteCourse = Course.builder()
                .id("CS101")
                .courseCode("CS101")
                .build();

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(2)).thenReturn(List.of(recommendedCourse));
        when(courseRepository.findAllById(List.of("CS101"))).thenReturn(List.of(prerequisiteCourse));

        RecommendedSemesterResponse response = academicService.getRecommendedSemester(userDetails, 2);

        assertNotNull(response);
        assertTrue(response.getRecommendedCourseIds().isEmpty());
        assertEquals(0, response.getTotalCredits());
        assertEquals(1, response.getPrerequisiteWarnings().size());
        assertTrue(response.getPrerequisiteWarnings().get(0).contains("CS201"));
        assertTrue(response.getPrerequisiteWarnings().get(0).contains("CS101"));
    }

    @Test
    void getRecommendedSemester_CompletedCoursesExcluded() {
        progress.getCompletedCourseIds().add("CS201");

        Course recommendedCourse = Course.builder()
                .id("CS201")
                .courseCode("CS201")
                .department("CS")
                .semester(2)
                .credits(4)
                .active(true)
                .prerequisiteCourseIds(Collections.emptyList())
                .build();

        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(2)).thenReturn(List.of(recommendedCourse));

        RecommendedSemesterResponse response = academicService.getRecommendedSemester(userDetails, 2);

        assertNotNull(response);
        assertTrue(response.getRecommendedCourseIds().isEmpty());
        assertEquals(0, response.getTotalCredits());
    }

    @Test
    void getRecommendedSemester_InactiveCoursesExcluded() {
        // Because courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(2) only returns active courses
        // (inactive courses are not returned in candidate courses).
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(studentProgressRepository.findByStudentId(studentUser.getId())).thenReturn(Optional.of(progress));
        when(roadmapRepository.findById(progress.getRoadmapId())).thenReturn(Optional.of(roadmap));
        when(courseRepository.findBySemesterAndActiveTrueOrderByCourseCodeAsc(2)).thenReturn(Collections.emptyList());

        RecommendedSemesterResponse response = academicService.getRecommendedSemester(userDetails, 2);

        assertNotNull(response);
        assertTrue(response.getRecommendedCourseIds().isEmpty());
    }
}
