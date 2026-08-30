package com.campusguide.personal.planner.controller;

import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PlannerTaskAuthorizationIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User user1;
    private User user2;
    private UserDetails userDetails1;
    private UserDetails userDetails2;
    private PlannerTask user2Task;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        plannerTaskRepository.deleteAll();
        userRepository.deleteAll();

        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        user1 = User.builder()
                .id(user1Id.toString())
                .email("user1@planner.com")
                .username("user1planner")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user1 = userRepository.save(user1);

        user2 = User.builder()
                .id(user2Id.toString())
                .email("user2@planner.com")
                .username("user2planner")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2 = userRepository.save(user2);

        userDetails1 = org.springframework.security.core.userdetails.User.withUsername("user1@planner.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        userDetails2 = org.springframework.security.core.userdetails.User.withUsername("user2@planner.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();

        user2Task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(user2.getId())
                .title("User 2 Private Task")
                .type(TaskType.PERSONAL)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user2Task = plannerTaskRepository.save(user2Task);

        PlannerTask user1Task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(user1.getId())
                .title("User 1 Own Task")
                .type(TaskType.STUDY)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user1Task = plannerTaskRepository.save(user1Task);
    }

    @AfterEach
    void tearDown() {
        plannerTaskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void unauthenticatedRequest_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/planner"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/planner/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        CreatePlannerTaskRequest createRequest = CreatePlannerTaskRequest.builder()
                .title("Unauth Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/v1/planner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessingOwnTask_Returns200() throws Exception {
        PlannerTask ownTask = plannerTaskRepository.findByUserId(user1.getId()).get(0);
        mockMvc.perform(get("/api/v1/planner/" + ownTask.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isOk());
    }

    @Test
    void accessingNonexistentTask_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/planner/" + UUID.randomUUID())
                        .with(user(userDetails1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void accessingOtherUserTask_IndistinguishableFromNotFound_Returns404() throws Exception {
        // User 1 tries to access User 2's task - returns 404 to prevent ID enumeration
        mockMvc.perform(get("/api/v1/planner/" + user2Task.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isNotFound());

        UpdatePlannerTaskRequest updateRequest = UpdatePlannerTaskRequest.builder()
                .title("Hacked Title")
                .type(TaskType.TODO)
                .priority(TaskPriority.LOW)
                .build();

        mockMvc.perform(put("/api/v1/planner/" + user2Task.getId())
                        .with(user(userDetails1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/v1/planner/" + user2Task.getId())
                        .with(user(userDetails1)))
                .andExpect(status().isNotFound());
    }
}
