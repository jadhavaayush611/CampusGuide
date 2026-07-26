package com.campusguide.campus.academic.progress.controller;

import com.campusguide.campus.academic.course.entity.Course;
import com.campusguide.campus.academic.course.repository.CourseRepository;
import com.campusguide.campus.academic.progress.dto.AdminUpdateStudentProgressRequest;
import com.campusguide.campus.academic.progress.dto.CreateStudentProgressRequest;
import com.campusguide.campus.academic.progress.dto.UpdateStudentProgressRequest;
import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.academic.roadmap.entity.Roadmap;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.platform.user.entity.Role;
import com.campusguide.platform.user.entity.User;
import com.campusguide.platform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class StudentProgressControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudentProgressRepository studentProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private CourseRepository courseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User adminUser;
    private User newStudentUser; // Student without progress record yet

    private UserDetails studentDetails;
    private UserDetails adminDetails;
    private UserDetails newStudentDetails;

    private Roadmap testRoadmap;
    private Course testCourse;
    private StudentProgress testProgress;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Clear databases in correct dependency order
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

        newStudentUser = User.builder()
                .email("newstudent@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        newStudentUser = userRepository.save(newStudentUser);

        // Create UserDetails
        studentDetails = org.springframework.security.core.userdetails.User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        newStudentDetails = org.springframework.security.core.userdetails.User.withUsername("newstudent@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        // Create Roadmap
        testRoadmap = Roadmap.builder()
                .title("CS Degree Roadmap")
                .description("Roadmap for Computer Science")
                .degreeProgram("CS")
                .department("CS")
                .totalCredits(120)
                .expectedGraduationYear(2028)
                .createdBy(adminUser.getId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testRoadmap = roadmapRepository.save(testRoadmap);

        // Create Course
        testCourse = Course.builder()
                .courseCode("CS101")
                .courseName("Intro to Programming")
                .description("Introductory course")
                .department("CS")
                .credits(4)
                .semester(1)
                .prerequisiteCourseIds(new ArrayList<>())
                .elective(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCourse = courseRepository.save(testCourse);

        // Create Student Progress
        testProgress = StudentProgress.builder()
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
        testProgress = studentProgressRepository.save(testProgress);
    }

    @AfterEach
    void tearDown() {
        studentProgressRepository.deleteAll();
        userRepository.deleteAll();
        roadmapRepository.deleteAll();
        courseRepository.deleteAll();
    }

    // --- CREATE PROGRESS SECURITY TESTS ---

    @Test
    void createProgress_Student_ReturnsCreated() throws Exception {
        CreateStudentProgressRequest request = CreateStudentProgressRequest.builder()
                .roadmapId(testRoadmap.getId())
                .build();

        // Use newStudentDetails who does not have progress yet
        mockMvc.perform(post("/api/progress")
                        .with(user(newStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(newStudentUser.getId()))
                .andExpect(jsonPath("$.roadmapId").value(testRoadmap.getId()))
                .andExpect(jsonPath("$.currentSemester").value(1));
    }

    @Test
    void createProgress_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CreateStudentProgressRequest request = CreateStudentProgressRequest.builder()
                .roadmapId(testRoadmap.getId())
                .build();

        mockMvc.perform(post("/api/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE PROGRESS SECURITY TESTS ---

    @Test
    void updateProgress_Student_OwnProgress_ReturnsOk() throws Exception {
        UpdateStudentProgressRequest request = UpdateStudentProgressRequest.builder()
                .currentSemester(3)
                .build();

        mockMvc.perform(put("/api/progress")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSemester").value(3));
    }

    @Test
    void updateProgress_Student_UpdatingGpa_ReturnsForbidden() throws Exception {
        AdminUpdateStudentProgressRequest request = AdminUpdateStudentProgressRequest.builder()
                .studentId(studentUser.getId())
                .currentGpa(9.2)
                .build();

        mockMvc.perform(put("/api/progress/admin")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProgress_Student_UpdatingEarnedCredits_ReturnsForbidden() throws Exception {
        AdminUpdateStudentProgressRequest request = AdminUpdateStudentProgressRequest.builder()
                .studentId(studentUser.getId())
                .totalCreditsEarned(30)
                .build();

        mockMvc.perform(put("/api/progress/admin")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProgress_Student_UpdatingGraduationEligibility_ReturnsForbidden() throws Exception {
        AdminUpdateStudentProgressRequest request = AdminUpdateStudentProgressRequest.builder()
                .studentId(studentUser.getId())
                .graduationEligible(true)
                .build();

        mockMvc.perform(put("/api/progress/admin")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProgress_SuperAdmin_UpdatingAcademicRecord_ReturnsOk() throws Exception {
        AdminUpdateStudentProgressRequest request = AdminUpdateStudentProgressRequest.builder()
                .studentId(studentUser.getId())
                .currentGpa(8.8)
                .currentSemester(4)
                .build();

        mockMvc.perform(put("/api/progress/admin")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentUser.getId()))
                .andExpect(jsonPath("$.currentGpa").value(8.8))
                .andExpect(jsonPath("$.currentSemester").value(4));
    }

    @Test
    void updateProgress_Unauthenticated_ReturnsUnauthorized() throws Exception {
        UpdateStudentProgressRequest request = UpdateStudentProgressRequest.builder()
                .currentSemester(3)
                .build();

        mockMvc.perform(put("/api/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    // --- GET PROGRESS SECURITY TESTS ---

    @Test
    void getProgress_Student_OwnProgress_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/progress")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentUser.getId()))
                .andExpect(jsonPath("$.roadmapId").value(testRoadmap.getId()));
    }

    @Test
    void getProgress_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/progress")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- COMPLETE COURSE SECURITY TESTS ---

    @Test
    void markCourseCompleted_Student_OwnProgress_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/progress/complete/" + testCourse.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedCourseIds[0]").value(testCourse.getId()))
                .andExpect(jsonPath("$.totalCreditsEarned").value(4));
    }

    @Test
    void markCourseCompleted_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/progress/complete/" + testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- REMOVE COMPLETED COURSE SECURITY TESTS ---

    @Test
    void removeCompletedCourse_Student_OwnProgress_ReturnsOk() throws Exception {
        // First add the course completion
        testProgress.getCompletedCourseIds().add(testCourse.getId());
        testProgress.setTotalCreditsEarned(4);
        studentProgressRepository.save(testProgress);

        mockMvc.perform(patch("/api/progress/remove/" + testCourse.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedCourseIds").isEmpty())
                .andExpect(jsonPath("$.totalCreditsEarned").value(0));
    }

    @Test
    void removeCompletedCourse_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/progress/remove/" + testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- GET PROGRESS BY STUDENT (ADMIN ENDPOINT) SECURITY TESTS ---

    @Test
    void getProgressByStudent_SuperAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/progress/student/" + studentUser.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(studentUser.getId()));
    }

    @Test
    void getProgressByStudent_Student_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/progress/student/" + studentUser.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProgressByStudent_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/progress/student/" + studentUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
