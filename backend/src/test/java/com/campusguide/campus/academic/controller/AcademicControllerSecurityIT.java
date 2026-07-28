package com.campusguide.campus.academic.controller;

import com.campusguide.campus.academic.course.entity.Course;
import com.campusguide.campus.academic.course.repository.CourseRepository;
import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.academic.roadmap.entity.Roadmap;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.campus.academic.semesterplanner.repository.SemesterPlanRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AcademicControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProgressRepository studentProgressRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterPlanRepository semesterPlanRepository;

    private User studentUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails adminDetails;

    private Roadmap testRoadmap;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Clean databases in dependency order
        semesterPlanRepository.deleteAll();
        studentProgressRepository.deleteAll();
        userRepository.deleteAll();
        roadmapRepository.deleteAll();
        courseRepository.deleteAll();

        // Create Users
        studentUser = User.builder()
                .email("student@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentUser = userRepository.save(studentUser);

        adminUser = User.builder()
                .email("admin@campusguide.com")
                .password("password")
                .role(Role.SUPER_ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);

        // Create UserDetails
        studentDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        // Create Roadmap
        testRoadmap = Roadmap.builder()
                .title("CS Roadmap")
                .degreeProgram("CS")
                .department("CS")
                .totalCredits(120)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testRoadmap = roadmapRepository.save(testRoadmap);

        // Create Student Progress records so API calls succeed (200 OK)
        StudentProgress studentProgress = StudentProgress.builder()
                .studentId(studentUser.getId())
                .roadmapId(testRoadmap.getId())
                .completedCourseIds(new ArrayList<>())
                .currentSemester(1)
                .totalCreditsEarned(0)
                .currentGpa(0.0)
                .graduationEligible(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentProgressRepository.save(studentProgress);

        StudentProgress adminProgress = StudentProgress.builder()
                .studentId(adminUser.getId())
                .roadmapId(testRoadmap.getId())
                .completedCourseIds(new ArrayList<>())
                .currentSemester(1)
                .totalCreditsEarned(0)
                .currentGpa(0.0)
                .graduationEligible(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentProgressRepository.save(adminProgress);
    }

    @AfterEach
    void tearDown() {
        semesterPlanRepository.deleteAll();
        studentProgressRepository.deleteAll();
        userRepository.deleteAll();
        roadmapRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void getDashboard_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/academic/dashboard")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDashboard_SuperAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/academic/dashboard")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getDashboard_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/academic/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProgress_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/academic/progress")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getProgress_SuperAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/academic/progress")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getProgress_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/academic/progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecommendedSemester_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/academic/recommended-semester")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRecommendedSemester_SuperAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/academic/recommended-semester")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRecommendedSemester_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/academic/recommended-semester")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
