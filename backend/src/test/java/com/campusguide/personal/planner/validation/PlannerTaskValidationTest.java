package com.campusguide.personal.planner.validation;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import com.campusguide.personal.planner.exception.PlannerTaskValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerTaskValidationTest {

    private Validator validator;

    @Mock
    private EventRepository eventRepository;

    private PlannerTaskValidator plannerTaskValidator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        plannerTaskValidator = new PlannerTaskValidator(eventRepository);
    }

    @Test
    void testCreatePlannerTaskRequest_ValidationSuccess() {
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Valid Task")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Set<ConstraintViolation<CreatePlannerTaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCreatePlannerTaskRequest_BlankTitle_ValidationFailure() {
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("   ")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        Set<ConstraintViolation<CreatePlannerTaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals("Title is mandatory", violations.iterator().next().getMessage());
    }

    @Test
    void testCreatePlannerTaskRequest_NullTypeAndPriority_ValidationFailure() {
        CreatePlannerTaskRequest request = CreatePlannerTaskRequest.builder()
                .title("Valid Title")
                .build();

        Set<ConstraintViolation<CreatePlannerTaskRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }

    @Test
    void testValidator_DueAtBeforeCreatedAt() {
        java.time.Instant createdAt = java.time.Instant.now();
        LocalDateTime dueAt = LocalDateTime.ofInstant(createdAt, java.time.ZoneId.systemDefault()).minusMinutes(10);

        assertThrows(PlannerTaskValidationException.class, () ->
                plannerTaskValidator.validateDueAt(dueAt, createdAt));
    }

    @Test
    void testValidator_ReminderAtAfterDueAt() {
        LocalDateTime dueAt = LocalDateTime.now().plusDays(1);
        LocalDateTime reminderAt = dueAt.plusHours(1);

        assertThrows(PlannerTaskValidationException.class, () ->
                plannerTaskValidator.validateReminderAt(reminderAt, dueAt));
    }

    @Test
    void testValidator_ReminderAtWithoutDueAt() {
        LocalDateTime reminderAt = LocalDateTime.now().plusDays(1);

        assertThrows(PlannerTaskValidationException.class, () ->
                plannerTaskValidator.validateReminderAt(reminderAt, null));
    }

    @Test
    void testValidator_LinkedEventNotFound() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                plannerTaskValidator.validateLinkedEvent(eventId));
    }

    @Test
    void testValidator_CompletedTaskModifyingTitle() {
        PlannerTask completedTask = PlannerTask.builder()
                .id(UUID.randomUUID())
                .title("Original Title")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        UpdatePlannerTaskRequest request = UpdatePlannerTaskRequest.builder()
                .title("New Title")
                .type(TaskType.TODO)
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .build();

        assertThrows(PlannerTaskValidationException.class, () ->
                plannerTaskValidator.validateUpdate(completedTask, request));
    }
}
