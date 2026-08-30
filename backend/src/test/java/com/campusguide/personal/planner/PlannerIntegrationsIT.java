package com.campusguide.personal.planner;

import com.campusguide.campus.event.entity.Event;
import com.campusguide.campus.event.entity.EventStatus;
import com.campusguide.campus.event.entity.EventType;
import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.CreateStudyGoalRequest;
import com.campusguide.personal.planner.dto.PlannerTaskResponse;
import com.campusguide.personal.planner.dto.StudyGoalResponse;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdateTaskStatusRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.StudyGoal;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PlannerIntegrationsIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PlannerTaskRepository plannerTaskRepository;

    @Autowired
    private StudyGoalRepository studyGoalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private User student1;
    private UserDetails student1Details;

    private User student2;
    private UserDetails student2Details;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        cleanUp();

        // Create student 1
        student1 = User.builder()
                .email("student1@ves.ac.in")
                .username("student1")
                .passwordHash("password")
                .role(Role.STUDENT)
                .enabled(true)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        student1 = userRepository.save(student1);
        student1Details = org.springframework.security.core.userdetails.User.withUsername("student1@ves.ac.in")
                .password("password")
                .roles("STUDENT")
                .build();

        // Create student 2
        student2 = User.builder()
                .email("student2@ves.ac.in")
                .username("student2")
                .passwordHash("password")
                .role(Role.STUDENT)
                .enabled(true)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        student2 = userRepository.save(student2);
        student2Details = org.springframework.security.core.userdetails.User.withUsername("student2@ves.ac.in")
                .password("password")
                .roles("STUDENT")
                .build();

        // Create test Event
        testEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Hackathon 2026")
                .slug("hackathon-2026-" + UUID.randomUUID())
                .description("VESIT Annual Hackathon")
                .councilId(UUID.randomUUID())
                .venue("Auditorium")
                .eventType(EventType.HACKATHON)
                .status(EventStatus.PUBLISHED)
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(12))
                .build();
        testEvent = eventRepository.save(testEvent);
    }

    @AfterEach
    void cleanUp() {
        plannerTaskRepository.deleteAll();
        studyGoalRepository.deleteAll();
        userRepository.deleteAll();
        eventRepository.deleteAll();
    }

    // =========================================================================
    // TASK 3: LINKED EVENTS INTEGRATION
    // =========================================================================

    @Test
    void testCreateTaskWithValidLinkedEvent_Success() throws Exception {
        LocalDateTime dueAt = LocalDateTime.now().plusDays(5);
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Prepare Hackathon Slides")
                .type(TaskType.EVENT)
                .priority(TaskPriority.HIGH)
                .linkedEventId(testEvent.getId())
                .dueAt(dueAt)
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.linkedEventId").value(testEvent.getId().toString()))
                .andReturn().getResponse().getContentAsString();

        PlannerTaskResponse created = objectMapper.readValue(responseJson, PlannerTaskResponse.class);

        // Verify retrieval preserves linked event
        mockMvc.perform(get("/api/v1/planner/" + created.getId())
                        .with(user(student1Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkedEventId").value(testEvent.getId().toString()));
    }

    @Test
    void testCreateTaskWithNonexistentLinkedEvent_Returns404() throws Exception {
        UUID nonexistentEventId = UUID.randomUUID();
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Task with Bad Event")
                .type(TaskType.EVENT)
                .priority(TaskPriority.MEDIUM)
                .linkedEventId(nonexistentEventId)
                .build();

        mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // TASK 4: REMINDERS INTEGRATION
    // =========================================================================

    @Test
    void testReminderLifecycle_Creation_Editing_Removal_And_Cancellation() throws Exception {
        LocalDateTime dueAt = LocalDateTime.now().plusDays(5);
        LocalDateTime reminderAt = LocalDateTime.now().plusDays(2);

        // 1. Create with valid reminder
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Submit Assignment")
                .type(TaskType.ASSIGNMENT)
                .priority(TaskPriority.HIGH)
                .dueAt(dueAt)
                .reminderAt(reminderAt)
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reminderAt").exists())
                .andReturn().getResponse().getContentAsString();

        PlannerTaskResponse task = objectMapper.readValue(responseJson, PlannerTaskResponse.class);
        assertNotNull(task.getReminderAt());

        // 2. Edit reminder to a different valid timestamp
        LocalDateTime newReminder = LocalDateTime.now().plusDays(3);
        UpdatePlannerTaskRequest updateRequest = UpdatePlannerTaskRequest.builder()
                .title("Submit Assignment Updated")
                .type(TaskType.ASSIGNMENT)
                .priority(TaskPriority.HIGH)
                .dueAt(dueAt)
                .reminderAt(newReminder)
                .build();

        mockMvc.perform(put("/api/v1/planner/" + task.getId())
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // 3. Remove reminder by setting reminderAt to null
        updateRequest.setReminderAt(null);
        mockMvc.perform(put("/api/v1/planner/" + task.getId())
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reminderAt").doesNotExist());

        // 4. Set reminder again, then CANCEL task -> reminder must be automatically cleared
        updateRequest.setReminderAt(LocalDateTime.now().plusDays(1));
        mockMvc.perform(put("/api/v1/planner/" + task.getId())
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        UpdateTaskStatusRequest cancelStatus = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.CANCELLED)
                .build();

        mockMvc.perform(patch("/api/v1/planner/" + task.getId() + "/status")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.reminderAt").doesNotExist());
    }

    @Test
    void testInvalidReminderAfterDueAt_Returns400() throws Exception {
        LocalDateTime dueAt = LocalDateTime.now().plusDays(2);
        LocalDateTime reminderAt = LocalDateTime.now().plusDays(3); // after due date

        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Invalid Reminder Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.LOW)
                .dueAt(dueAt)
                .reminderAt(reminderAt)
                .build();

        mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testReminderWithoutDueAt_Returns400() throws Exception {
        LocalDateTime reminderAt = LocalDateTime.now().plusDays(1);

        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Reminder Without Due Date")
                .type(TaskType.TODO)
                .priority(TaskPriority.LOW)
                .reminderAt(reminderAt)
                .build();

        mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCompletedTaskImmutability_CannotModifyFieldsExceptNotes() throws Exception {
        LocalDateTime dueAt = LocalDateTime.now().plusDays(2).truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        CreatePlannerTaskRequest createRequest = CreatePlannerTaskRequest.builder()
                .title("Task to Complete")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueAt(dueAt)
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PlannerTaskResponse task = objectMapper.readValue(responseJson, PlannerTaskResponse.class);

        // Transition to COMPLETED
        UpdateTaskStatusRequest completeRequest = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.COMPLETED)
                .build();

        mockMvc.perform(patch("/api/v1/planner/" + task.getId() + "/status")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());

        // Attempt to modify title on completed task -> 400 Bad Request
        UpdatePlannerTaskRequest updateTitle = UpdatePlannerTaskRequest.builder()
                .title("Changing Completed Task Title")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueAt(dueAt)
                .build();

        mockMvc.perform(put("/api/v1/planner/" + task.getId())
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTitle)))
                .andExpect(status().isBadRequest());

        // Modifying notes only on completed task -> 200 OK
        UpdatePlannerTaskRequest updateNotes = UpdatePlannerTaskRequest.builder()
                .title("Task to Complete")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .dueAt(task.getDueAt())
                .notes("Reviewed and finalized with advisor")
                .build();

        mockMvc.perform(put("/api/v1/planner/" + task.getId())
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNotes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Reviewed and finalized with advisor"));
    }

    // =========================================================================
    // TASK 8: USER ISOLATION (TWO STUDENTS)
    // =========================================================================

    @Test
    void testUserIsolation_TasksAndStudyGoalsAreCompletelyIsolated() throws Exception {
        // 1. Student 1 creates a Task and a Study Goal
        CreatePlannerTaskRequest taskReq = CreatePlannerTaskRequest.builder()
                .title("Student 1 Confidential Task")
                .type(TaskType.PERSONAL)
                .priority(TaskPriority.URGENT)
                .build();

        String taskResp = mockMvc.perform(post("/api/v1/planner")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        PlannerTaskResponse s1Task = objectMapper.readValue(taskResp, PlannerTaskResponse.class);

        CreateStudyGoalRequest goalReq = CreateStudyGoalRequest.builder()
                .title("Student 1 Private Study Goal")
                .targetHours(10)
                .category("Competitive Programming")
                .build();

        String goalResp = mockMvc.perform(post("/api/v1/planner/goals")
                        .with(user(student1Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        StudyGoalResponse s1Goal = objectMapper.readValue(goalResp, StudyGoalResponse.class);

        // 2. Student 2 attempts to view Student 1's task -> 404
        mockMvc.perform(get("/api/v1/planner/" + s1Task.getId())
                        .with(user(student2Details)))
                .andExpect(status().isNotFound());

        // 3. Student 2 attempts to modify Student 1's task -> 404
        UpdatePlannerTaskRequest modReq = UpdatePlannerTaskRequest.builder()
                .title("Hacked Title")
                .type(TaskType.TODO)
                .priority(TaskPriority.LOW)
                .build();
        mockMvc.perform(put("/api/v1/planner/" + s1Task.getId())
                        .with(user(student2Details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modReq)))
                .andExpect(status().isNotFound());

        // 4. Student 2 attempts to delete Student 1's task -> 404
        mockMvc.perform(delete("/api/v1/planner/" + s1Task.getId())
                        .with(user(student2Details)))
                .andExpect(status().isNotFound());

        // 5. Student 2 list tasks -> should NOT include Student 1's task
        mockMvc.perform(get("/api/v1/planner")
                        .with(user(student2Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // 6. Student 2 attempts to view / list Student 1's study goals -> should be empty
        mockMvc.perform(get("/api/v1/planner/goals")
                        .with(user(student2Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // 7. Student 2 attempts to delete Student 1's study goal -> 404
        mockMvc.perform(delete("/api/v1/planner/goals/" + s1Goal.getId())
                        .with(user(student2Details)))
                .andExpect(status().isNotFound());

        // 8. Confirm Student 1 still owns both items intact
        mockMvc.perform(get("/api/v1/planner/" + s1Task.getId())
                        .with(user(student1Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Student 1 Confidential Task"));

        mockMvc.perform(get("/api/v1/planner/goals")
                        .with(user(student1Details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Student 1 Private Study Goal"));
    }
}
