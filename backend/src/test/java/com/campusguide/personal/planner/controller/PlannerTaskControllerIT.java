package com.campusguide.personal.planner.controller;

import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdateTaskStatusRequest;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PlannerTaskControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User userEntity;
    private UserDetails userDetails;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        plannerTaskRepository.deleteAll();
        userRepository.deleteAll();

        userId = UUID.randomUUID();

        userEntity = User.builder()
                .id(userId.toString())
                .email("student@planner.com")
                .username("plannerstudent")
                .password("password")
                .role(Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userEntity = userRepository.save(userEntity);

        userDetails = org.springframework.security.core.userdetails.User.withUsername("student@planner.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }

    @AfterEach
    void tearDown() {
        plannerTaskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreatePlannerTask_Success() throws Exception {
        LocalDateTime dueAt = LocalDateTime.now().plusDays(2);
        LocalDateTime reminderAt = LocalDateTime.now().plusDays(1);

        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Prepare Presentation")
                .description("Prepare slides for software engineering")
                .type(TaskType.PROJECT)
                .priority(TaskPriority.HIGH)
                .dueAt(dueAt)
                .reminderAt(reminderAt)
                .notes("Focus on Clean Architecture")
                .build();

        mockMvc.perform(post("/api/v1/planner")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Prepare Presentation"))
                .andExpect(jsonPath("$.type").value("PROJECT"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.notes").value("Focus on Clean Architecture"));
    }

    @Test
    void testGetAllPlannerTasks_Success() throws Exception {
        PlannerTask task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .title("Read Book")
                .type(TaskType.STUDY)
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        plannerTaskRepository.save(task);

        mockMvc.perform(get("/api/v1/planner")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Read Book"));
    }

    @Test
    void testGetPlannerTaskById_Success() throws Exception {
        PlannerTask task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .title("Single Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        task = plannerTaskRepository.save(task);

        mockMvc.perform(get("/api/v1/planner/" + task.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId().toString()))
                .andExpect(jsonPath("$.title").value("Single Task"));
    }

    @Test
    void testUpdatePlannerTask_Success() throws Exception {
        PlannerTask task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .title("Old Title")
                .type(TaskType.TODO)
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        task = plannerTaskRepository.save(task);

        UpdatePlannerTaskRequest request = UpdatePlannerTaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .type(TaskType.EXAM)
                .priority(TaskPriority.URGENT)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(put("/api/v1/planner/" + task.getId())
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.type").value("EXAM"))
                .andExpect(jsonPath("$.priority").value("URGENT"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testUpdateTaskStatus_Success() throws Exception {
        PlannerTask task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .title("Status Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        task = plannerTaskRepository.save(task);

        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .build();

        mockMvc.perform(patch("/api/v1/planner/" + task.getId() + "/status")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void testDeletePlannerTask_Success() throws Exception {
        PlannerTask task = PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(UUID.fromString(userEntity.getId()))
                .title("Delete Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        task = plannerTaskRepository.save(task);

        mockMvc.perform(delete("/api/v1/planner/" + task.getId())
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());

        assertFalse(plannerTaskRepository.findById(task.getId()).isPresent());
    }
}
