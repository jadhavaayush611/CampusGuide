package com.campusguide.personal.planner.controller;

import com.campusguide.personal.planner.dto.CreateStudyGoalRequest;
import com.campusguide.personal.planner.dto.UpdateStudyGoalRequest;
import com.campusguide.personal.planner.entity.StudyGoal;
import com.campusguide.personal.planner.repository.StudyGoalRepository;
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
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class StudyGoalControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudyGoalRepository studyGoalRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User user1;
    private User user2;
    private UserDetails userDetails1;
    private UserDetails userDetails2;
    private StudyGoal user1Goal;
    private StudyGoal user2Goal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        studyGoalRepository.deleteAll();
        userRepository.deleteAll();

        user1 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("student1@planner.com")
                .username("student1planner")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .id(UUID.randomUUID().toString())
                .email("student2@planner.com")
                .username("student2planner")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2 = userRepository.save(user2);

        userDetails1 = org.springframework.security.core.userdetails.User.withUsername("student1@planner.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        userDetails2 = org.springframework.security.core.userdetails.User.withUsername("student2@planner.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user1Goal = StudyGoal.builder()
                .id(UUID.randomUUID())
                .userId(user1.getId())
                .title("User 1 Goal")
                .description("Goal details for user 1")
                .targetHours(10)
                .completedHours(3)
                .deadline("2026-09-20")
                .category("Exam Prep")
                .isCompleted(false)
                .build();
        user1Goal = studyGoalRepository.save(user1Goal);

        user2Goal = StudyGoal.builder()
                .id(UUID.randomUUID())
                .userId(user2.getId())
                .title("User 2 Private Goal")
                .targetHours(15)
                .completedHours(0)
                .isCompleted(false)
                .build();
        user2Goal = studyGoalRepository.save(user2Goal);
    }

    @AfterEach
    void tearDown() {
        studyGoalRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/planner/goals"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/planner/goals/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        CreateStudyGoalRequest request = CreateStudyGoalRequest.builder()
                .title("Unauthorized Goal")
                .targetHours(5)
                .build();

        mockMvc.perform(post("/api/v1/planner/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createStudyGoal_Success() throws Exception {
        CreateStudyGoalRequest request = CreateStudyGoalRequest.builder()
                .title("New Target Goal")
                .description("New target description")
                .targetHours(12)
                .deadline("2026-10-01")
                .category("Coursework")
                .build();

        mockMvc.perform(post("/api/v1/planner/goals")
                        .with(user(userDetails1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Target Goal")))
                .andExpect(jsonPath("$.targetHours", is(12)))
                .andExpect(jsonPath("$.completedHours", is(0)))
                .andExpect(jsonPath("$.isCompleted", is(false)))
                .andExpect(jsonPath("$.userId", is(user1.getId())));
    }

    @Test
    void getAllGoals_ReturnsOnlyOwnGoals() throws Exception {
        mockMvc.perform(get("/api/v1/planner/goals")
                        .with(user(userDetails1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user1Goal.getId().toString())));
    }

    @Test
    void getGoalById_OwnGoal_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/planner/goals/" + user1Goal.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user1Goal.getId().toString())))
                .andExpect(jsonPath("$.title", is("User 1 Goal")));
    }

    @Test
    void getGoalById_OtherUserGoal_Returns404() throws Exception {
        // Scoped lookup prevents ID enumeration
        mockMvc.perform(get("/api/v1/planner/goals/" + user2Goal.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGoal_Success() throws Exception {
        UpdateStudyGoalRequest updateRequest = UpdateStudyGoalRequest.builder()
                .completedHours(10)
                .build();

        mockMvc.perform(put("/api/v1/planner/goals/" + user1Goal.getId())
                        .with(user(userDetails1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedHours", is(10)))
                .andExpect(jsonPath("$.isCompleted", is(true)));
    }

    @Test
    void deleteGoal_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/planner/goals/" + user1Goal.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isNoContent());

        assertFalse(studyGoalRepository.existsById(user1Goal.getId()));
    }
}
