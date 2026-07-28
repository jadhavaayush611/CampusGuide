package com.campusguide.personal.planner.service;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.PlannerTaskResponse;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdateTaskStatusRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.exception.PlannerTaskAccessDeniedException;
import com.campusguide.personal.planner.exception.PlannerTaskNotFoundException;
import com.campusguide.personal.planner.exception.PlannerTaskValidationException;
import com.campusguide.personal.planner.mapper.PlannerTaskMapper;
import com.campusguide.personal.planner.repository.PlannerTaskRepository;
import com.campusguide.personal.planner.validation.PlannerTaskValidator;
import com.campusguide.platform.user.entity.User;

import com.campusguide.platform.user.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannerTaskServiceTest {

    @Mock
    private PlannerTaskRepository plannerTaskRepository;

    @Spy
    private PlannerTaskMapper plannerTaskMapper = new PlannerTaskMapper();

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CurrentUserService currentUserService;

    private PlannerTaskValidator plannerTaskValidator;
    private PlannerTaskService plannerTaskService;

    private User testUser;
    private UserDetails userDetails;
    private UUID userId;
    private UUID taskId;
    private PlannerTask existingTask;

    @BeforeEach
    void setUp() {
        plannerTaskValidator = new PlannerTaskValidator(eventRepository);
        plannerTaskService = new PlannerTaskService(plannerTaskRepository, plannerTaskMapper, plannerTaskValidator, currentUserService);

        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId.toString())
                .email("user@example.com")
                .username("testuser")
                .build();

        userDetails = org.springframework.security.core.userdetails.User.withUsername("user@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        lenient().when(currentUserService.getCurrentUserId(any())).thenReturn(userId.toString());

        existingTask = PlannerTask.builder()
                .id(taskId)
                .userId(userId.toString())
                .title("Sample Task")
                .description("Sample Description")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Test
    void createTask_Success() {

        when(plannerTaskRepository.save(any(PlannerTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime now = LocalDateTime.now();
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .type(TaskType.ASSIGNMENT)
                .priority(TaskPriority.HIGH)
                .dueAt(now.plusDays(2))
                .reminderAt(now.plusDays(1))
                .build();

        PlannerTaskResponse response = plannerTaskService.createTask(userDetails, request);

        assertNotNull(response);
        assertEquals("New Task", response.getTitle());
        assertEquals(TaskStatus.TODO, response.getStatus());
        assertEquals(userId.toString(), response.getUserId());
        verify(plannerTaskRepository, times(1)).save(any(PlannerTask.class));
    }

    @Test
    void createTask_LinkedEventNotFound_ThrowsException() {

        UUID linkedEventId = UUID.randomUUID();
        when(eventRepository.existsById(linkedEventId)).thenReturn(false);

        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Event Task")
                .type(TaskType.EVENT)
                .priority(TaskPriority.MEDIUM)
                .linkedEventId(linkedEventId)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> plannerTaskService.createTask(userDetails, request));
    }

    @Test
    void createTask_DueAtBeforeCreatedAt_ThrowsException() {


        LocalDateTime dueAtInPast = LocalDateTime.now().minusDays(2);
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Past Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueAt(dueAtInPast)
                .build();

        assertThrows(PlannerTaskValidationException.class, () -> plannerTaskService.createTask(userDetails, request));
    }

    @Test
    void createTask_ReminderAtAfterDueAt_ThrowsException() {


        LocalDateTime now = LocalDateTime.now();
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Invalid Reminder Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueAt(now.plusDays(1))
                .reminderAt(now.plusDays(2))
                .build();

        assertThrows(PlannerTaskValidationException.class, () -> plannerTaskService.createTask(userDetails, request));
    }

    @Test
    void getAllTasks_Success() {

        when(plannerTaskRepository.findByUserIdOrderByDueAtAsc(userId.toString())).thenReturn(List.of(existingTask));

        List<PlannerTaskResponse> responses = plannerTaskService.getAllTasks(userDetails);

        assertEquals(1, responses.size());
        assertEquals(taskId, responses.get(0).getId());
    }

    @Test
    void getTaskById_Success() {

        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        PlannerTaskResponse response = plannerTaskService.getTaskById(userDetails, taskId);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
    }

    @Test
    void getTaskById_NotFound_ThrowsPlannerTaskNotFoundException() {

        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(PlannerTaskNotFoundException.class, () -> plannerTaskService.getTaskById(userDetails, taskId));
    }

    @Test
    void getTaskById_OtherUser_ThrowsPlannerTaskAccessDeniedException() {

        PlannerTask otherUserTask = PlannerTask.builder()
                .id(taskId)
                .userId(UUID.randomUUID().toString())
                .title("Other User Task")
                .build();
        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(otherUserTask));

        assertThrows(PlannerTaskAccessDeniedException.class, () -> plannerTaskService.getTaskById(userDetails, taskId));
    }

    @Test
    void updateTask_TransitionToCompleted_SetsCompletedAt() {

        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(plannerTaskRepository.save(any(PlannerTask.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePlannerTaskRequest request = UpdatePlannerTaskRequest.builder()
                .title("Sample Task")
                .description("Sample Description")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .build();

        PlannerTaskResponse response = plannerTaskService.updateTask(userDetails, taskId, request);

        assertEquals(TaskStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getCompletedAt());
    }

    @Test
    void updateTask_TransitionToCancelled_ClearsReminderAt() {

        existingTask.setReminderAt(LocalDateTime.now().plusHours(2));
        existingTask.setDueAt(LocalDateTime.now().plusDays(1));
        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(plannerTaskRepository.save(any(PlannerTask.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePlannerTaskRequest request = UpdatePlannerTaskRequest.builder()
                .title("Sample Task")
                .description("Sample Description")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.CANCELLED)
                .dueAt(existingTask.getDueAt())
                .reminderAt(existingTask.getReminderAt())
                .build();

        PlannerTaskResponse response = plannerTaskService.updateTask(userDetails, taskId, request);

        assertEquals(TaskStatus.CANCELLED, response.getStatus());
        assertNull(response.getReminderAt());
    }

    @Test
    void updateTask_TaskAlreadyCompleted_ModifyingNonNotes_ThrowsException() {

        existingTask.setStatus(TaskStatus.COMPLETED);
        existingTask.setCompletedAt(LocalDateTime.now().minusHours(1));
        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        UpdatePlannerTaskRequest request = UpdatePlannerTaskRequest.builder()
                .title("Changed Title")
                .description("Sample Description")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .build();

        assertThrows(PlannerTaskValidationException.class, () -> plannerTaskService.updateTask(userDetails, taskId, request));
    }

    @Test
    void updateTask_TaskAlreadyCompleted_ModifyingNotes_Success() {

        existingTask.setStatus(TaskStatus.COMPLETED);
        existingTask.setCompletedAt(LocalDateTime.now().minusHours(1));
        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(plannerTaskRepository.save(any(PlannerTask.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePlannerTaskRequest request = UpdatePlannerTaskRequest.builder()
                .title("Sample Task")
                .description("Sample Description")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .notes("Updated notes after completion")
                .build();

        PlannerTaskResponse response = plannerTaskService.updateTask(userDetails, taskId, request);

        assertEquals("Updated notes after completion", response.getNotes());
    }

    @Test
    void updateTaskStatus_Success() {

        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(plannerTaskRepository.save(any(PlannerTask.class))).thenAnswer(i -> i.getArgument(0));

        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();

        PlannerTaskResponse response = plannerTaskService.updateTaskStatus(userDetails, taskId, request);

        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void deleteTask_Success() {

        when(plannerTaskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        plannerTaskService.deleteTask(userDetails, taskId);

        verify(plannerTaskRepository, times(1)).delete(existingTask);
    }
}
