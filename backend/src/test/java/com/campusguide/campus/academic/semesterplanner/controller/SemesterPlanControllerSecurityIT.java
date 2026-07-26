package com.campusguide.campus.academic.semesterplanner.controller;

import com.campusguide.campus.academic.course.entity.Course;
import com.campusguide.campus.academic.course.repository.CourseRepository;
import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.campus.academic.roadmap.entity.Roadmap;
import com.campusguide.campus.academic.roadmap.repository.RoadmapRepository;
import com.campusguide.campus.academic.semesterplanner.dto.CreateSemesterPlanRequest;
import com.campusguide.campus.academic.semesterplanner.dto.UpdateSemesterPlanRequest;
import com.campusguide.campus.academic.semesterplanner.entity.SemesterPlan;
import com.campusguide.campus.academic.semesterplanner.repository.SemesterPlanRepository;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SemesterPlanControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SemesterPlanRepository semesterPlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentProgressRepository studentProgressRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User studentUser;
    private User otherStudentUser;
    private User adminUser;

    private UserDetails studentDetails;
    private UserDetails otherStudentDetails;
    private UserDetails adminDetails;

    private Roadmap testRoadmap;
    private Course testCourse;
    private Course testPrereqCourse;
    private StudentProgress testProgress;
    private SemesterPlan testPlan;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Clear databases in correct dependency order
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

        otherStudentUser = User.builder()
                .email("otherstudent@campusguide.com")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        otherStudentUser = userRepository.save(otherStudentUser);

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

        otherStudentDetails = org.springframework.security.core.userdetails.User.withUsername("otherstudent@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        adminDetails = org.springframework.security.core.userdetails.User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
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

        // Create Prerequisite Course
        testPrereqCourse = Course.builder()
                .courseCode("CS100")
                .courseName("Intro to Computing")
                .description("Basic computing course")
                .department("CS")
                .credits(3)
                .semester(1)
                .prerequisiteCourseIds(new ArrayList<>())
                .elective(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testPrereqCourse = courseRepository.save(testPrereqCourse);

        // Create Course
        testCourse = Course.builder()
                .courseCode("CS101")
                .courseName("Intro to Programming")
                .description("Introductory course")
                .department("CS")
                .credits(4)
                .semester(1)
                .prerequisiteCourseIds(new ArrayList<>(List.of(testPrereqCourse.getId())))
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
                .completedCourseIds(new ArrayList<>(List.of(testPrereqCourse.getId())))
                .currentSemester(1)
                .totalCreditsEarned(3)
                .currentGpa(0.0)
                .graduationEligible(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testProgress = studentProgressRepository.save(testProgress);

        // Create Semester Plan
        testPlan = SemesterPlan.builder()
                .studentId(studentUser.getId())
                .roadmapId(testRoadmap.getId())
                .semesterNumber(1)
                .plannedCourseIds(new ArrayList<>())
                .totalPlannedCredits(0)
                .finalized(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testPlan = semesterPlanRepository.save(testPlan);
    }

    @AfterEach
    void tearDown() {
        semesterPlanRepository.deleteAll();
        studentProgressRepository.deleteAll();
        userRepository.deleteAll();
        roadmapRepository.deleteAll();
        courseRepository.deleteAll();
    }

    // --- CREATE PLANS SECURITY TESTS ---

    @Test
    void createSemesterPlan_Student_ReturnsCreated() throws Exception {
        StudentProgress otherProgress = StudentProgress.builder()
                .studentId(otherStudentUser.getId())
                .roadmapId(testRoadmap.getId())
                .completedCourseIds(new ArrayList<>())
                .currentSemester(1)
                .totalCreditsEarned(0)
                .currentGpa(0.0)
                .graduationEligible(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentProgressRepository.save(otherProgress);

        CreateSemesterPlanRequest request = CreateSemesterPlanRequest.builder()
                .roadmapId(testRoadmap.getId())
                .semesterNumber(1)
                .build();

        mockMvc.perform(post("/api/semester-plans")
                        .with(user(otherStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentId").value(otherStudentUser.getId()))
                .andExpect(jsonPath("$.roadmapId").value(testRoadmap.getId()))
                .andExpect(jsonPath("$.semesterNumber").value(1));
    }

    @Test
    void createSemesterPlan_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CreateSemesterPlanRequest request = CreateSemesterPlanRequest.builder()
                .roadmapId(testRoadmap.getId())
                .semesterNumber(1)
                .build();

        mockMvc.perform(post("/api/semester-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE PLANS SECURITY TESTS ---

    @Test
    void updateSemesterPlan_Student_OwnPlan_ReturnsOk() throws Exception {
        UpdateSemesterPlanRequest request = UpdateSemesterPlanRequest.builder()
                .semesterNumber(2)
                .build();

        mockMvc.perform(put("/api/semester-plans/" + testPlan.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterNumber").value(2));
    }

    @Test
    void updateSemesterPlan_Student_OtherPlan_ReturnsForbidden() throws Exception {
        UpdateSemesterPlanRequest request = UpdateSemesterPlanRequest.builder()
                .semesterNumber(2)
                .build();

        mockMvc.perform(put("/api/semester-plans/" + testPlan.getId())
                        .with(user(otherStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSemesterPlan_SuperAdmin_AnyPlan_ReturnsOk() throws Exception {
        UpdateSemesterPlanRequest request = UpdateSemesterPlanRequest.builder()
                .semesterNumber(3)
                .build();

        mockMvc.perform(put("/api/semester-plans/" + testPlan.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semesterNumber").value(3));
    }

    @Test
    void updateSemesterPlan_Unauthenticated_ReturnsUnauthorized() throws Exception {
        UpdateSemesterPlanRequest request = UpdateSemesterPlanRequest.builder()
                .semesterNumber(2)
                .build();

        mockMvc.perform(put("/api/semester-plans/" + testPlan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- GET PLANS SECURITY TESTS ---

    @Test
    void getMyPlans_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/semester-plans")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPlan.getId()));
    }

    @Test
    void getMyPlans_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/semester-plans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSemesterPlan_Student_OwnPlan_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/semester-plans/" + testPlan.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPlan.getId()));
    }

    @Test
    void getSemesterPlan_Student_OtherPlan_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/semester-plans/" + testPlan.getId())
                        .with(user(otherStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSemesterPlan_SuperAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/semester-plans/" + testPlan.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPlan.getId()));
    }

    @Test
    void getSemesterPlan_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/semester-plans/" + testPlan.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- ADD COURSE SECURITY TESTS ---

    @Test
    void addCourse_Student_OwnPlan_ReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/add/" + testCourse.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedCourseIds[0]").value(testCourse.getId()))
                .andExpect(jsonPath("$.totalPlannedCredits").value(4));
    }

    @Test
    void addCourse_Student_OtherPlan_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/add/" + testCourse.getId())
                        .with(user(otherStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void addCourse_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/add/" + testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- REMOVE COURSE SECURITY TESTS ---

    @Test
    void removeCourse_Student_OwnPlan_ReturnsOk() throws Exception {
        testPlan.getPlannedCourseIds().add(testCourse.getId());
        testPlan.setTotalPlannedCredits(4);
        semesterPlanRepository.save(testPlan);

        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/remove/" + testCourse.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedCourseIds").isEmpty())
                .andExpect(jsonPath("$.totalPlannedCredits").value(0));
    }

    @Test
    void removeCourse_Student_OtherPlan_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/remove/" + testCourse.getId())
                        .with(user(otherStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeCourse_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/remove/" + testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- FINALIZE PLAN SECURITY TESTS ---

    @Test
    void finalizeSemesterPlan_Student_OwnPlan_ReturnsOk() throws Exception {
        testPlan.getPlannedCourseIds().add(testCourse.getId());
        testPlan.setTotalPlannedCredits(4);
        semesterPlanRepository.save(testPlan);

        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/finalize")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalized").value(true));
    }

    @Test
    void finalizeSemesterPlan_Student_OtherPlan_ReturnsForbidden() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/finalize")
                        .with(user(otherStudentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void finalizeSemesterPlan_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/semester-plans/" + testPlan.getId() + "/finalize")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- GET PLANS BY STUDENT SECURITY TESTS ---

    @Test
    void getPlansByStudent_SuperAdmin_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/semester-plans/student/" + studentUser.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(studentUser.getId()));
    }

    @Test
    void getPlansByStudent_Student_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/semester-plans/student/" + studentUser.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPlansByStudent_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/semester-plans/student/" + studentUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
