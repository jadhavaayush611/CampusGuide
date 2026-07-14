package com.campusguide.modules.course.controller;

import com.campusguide.modules.course.dto.CreateCourseRequest;
import com.campusguide.modules.course.dto.UpdateCourseRequest;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.course.repository.CourseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CourseControllerSecurityIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CourseRepository courseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Course testCourse;
    private UserDetails adminDetails;
    private UserDetails studentDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        courseRepository.deleteAll();

        adminDetails = User.withUsername("admin@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                .build();

        studentDetails = User.withUsername("student@campusguide.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        Course course = Course.builder()
                .courseCode("CS101")
                .courseName("Intro to Computer Science")
                .description("Basics of coding")
                .department("CS")
                .credits(4)
                .semester(1)
                .prerequisiteCourseIds(Collections.emptyList())
                .elective(false)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCourse = courseRepository.save(course);
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
    }

    // --- CREATE COURSE SEC TESTS ---

    @Test
    void createCourse_SuperAdmin_ReturnsCreated() throws Exception {
        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS102")
                .courseName("Data Structures")
                .description("Study of fundamental data structures")
                .department("CS")
                .credits(4)
                .semester(2)
                .elective(false)
                .build();

        mockMvc.perform(post("/api/courses")
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS102"))
                .andExpect(jsonPath("$.courseName").value("Data Structures"));
    }

    @Test
    void createCourse_Student_ReturnsForbidden() throws Exception {
        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS102")
                .courseName("Data Structures")
                .department("CS")
                .credits(4)
                .semester(2)
                .elective(false)
                .build();

        mockMvc.perform(post("/api/courses")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    void createCourse_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CreateCourseRequest request = CreateCourseRequest.builder()
                .courseCode("CS102")
                .courseName("Data Structures")
                .department("CS")
                .credits(4)
                .semester(2)
                .elective(false)
                .build();

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- UPDATE COURSE SEC TESTS ---

    @Test
    void updateCourse_SuperAdmin_ReturnsOk() throws Exception {
        UpdateCourseRequest request = UpdateCourseRequest.builder()
                .courseName("Intro to Computer Science v2")
                .credits(3)
                .build();

        mockMvc.perform(put("/api/courses/" + testCourse.getId())
                        .with(user(adminDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseName").value("Intro to Computer Science v2"))
                .andExpect(jsonPath("$.credits").value(3));
    }

    @Test
    void updateCourse_Student_ReturnsForbidden() throws Exception {
        UpdateCourseRequest request = UpdateCourseRequest.builder()
                .courseName("Intro to Computer Science v2")
                .build();

        mockMvc.perform(put("/api/courses/" + testCourse.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    void updateCourse_Unauthenticated_ReturnsUnauthorized() throws Exception {
        UpdateCourseRequest request = UpdateCourseRequest.builder()
                .courseName("Intro to Computer Science v2")
                .build();

        mockMvc.perform(put("/api/courses/" + testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- DELETE COURSE SEC TESTS ---

    @Test
    void deleteCourse_SuperAdmin_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/courses/" + testCourse.getId())
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCourse_Student_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/courses/" + testCourse.getId())
                        .with(user(studentDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access Denied"));
    }

    @Test
    void deleteCourse_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/courses/" + testCourse.getId()))
                .andExpect(status().isUnauthorized());
    }

    // --- GET COURSES SEC TESTS ---

    @Test
    void getAllCourses_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void getAllCourses_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCourseById_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/courses/" + testCourse.getId())
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseCode").value("CS101"));
    }

    @Test
    void getCourseById_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses/" + testCourse.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByDepartment_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/courses/department/CS")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void getBySemester_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/courses/semester/1")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void getElectives_Student_ReturnsOk() throws Exception {
        // Create an elective course to test
        Course electiveCourse = Course.builder()
                .courseCode("CS200")
                .courseName("Advanced Programming")
                .department("CS")
                .credits(3)
                .semester(2)
                .elective(true)
                .active(true)
                .build();
        courseRepository.save(electiveCourse);

        mockMvc.perform(get("/api/courses/electives")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS200"));
    }

    @Test
    void getMandatoryCourses_Student_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/courses/mandatory")
                        .with(user(studentDetails))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }
}
