package com.campusguide.personal.planner.validation;

import com.campusguide.campus.event.repository.EventRepository;
import com.campusguide.common.exception.ResourceNotFoundException;
import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.UpdatePlannerTaskRequest;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.exception.PlannerTaskValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlannerTaskValidator {

    private final EventRepository eventRepository;

    public void validateCreate(CreatePlannerTaskRequest request, LocalDateTime createdAt) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new PlannerTaskValidationException("Title is mandatory");
        }

        validateLinkedEvent(request.getLinkedEventId());
        validateDueAt(request.getDueAt(), createdAt);
        validateReminderAt(request.getReminderAt(), request.getDueAt());
    }

    public void validateUpdate(PlannerTask existingTask, UpdatePlannerTaskRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new PlannerTaskValidationException("Title is mandatory");
        }

        // Business Rule: Once COMPLETED, only notes may be modified.
        if (existingTask.getStatus() == TaskStatus.COMPLETED) {
            boolean nonNotesChanged = !Objects.equals(existingTask.getTitle(), request.getTitle())
                    || !Objects.equals(existingTask.getDescription(), request.getDescription())
                    || existingTask.getType() != request.getType()
                    || existingTask.getPriority() != request.getPriority()
                    || (request.getStatus() != null && request.getStatus() != TaskStatus.COMPLETED)
                    || !Objects.equals(existingTask.getLinkedEventId(), request.getLinkedEventId())
                    || !Objects.equals(existingTask.getDueAt(), request.getDueAt())
                    || !Objects.equals(existingTask.getReminderAt(), request.getReminderAt());

            if (nonNotesChanged) {
                throw new PlannerTaskValidationException("Task is COMPLETED; only notes may be modified");
            }
        }

        validateLinkedEvent(request.getLinkedEventId());
        validateDueAt(request.getDueAt(), existingTask.getCreatedAt());
        validateReminderAt(request.getReminderAt(), request.getDueAt());
    }

    public void validateStatusChange(PlannerTask existingTask, TaskStatus newStatus) {
        if (existingTask.getStatus() == TaskStatus.COMPLETED && newStatus != TaskStatus.COMPLETED) {
            throw new PlannerTaskValidationException("Task is COMPLETED; only notes may be modified");
        }
    }

    public void validateLinkedEvent(UUID linkedEventId) {
        if (linkedEventId != null) {
            if (!eventRepository.existsById(linkedEventId)) {
                throw new ResourceNotFoundException("Event not found with id: " + linkedEventId);
            }
        }
    }

    public void validateDueAt(LocalDateTime dueAt, LocalDateTime createdAt) {
        if (dueAt != null && createdAt != null && dueAt.isBefore(createdAt)) {
            throw new PlannerTaskValidationException("dueAt cannot precede createdAt");
        }
    }

    public void validateReminderAt(LocalDateTime reminderAt, LocalDateTime dueAt) {
        if (reminderAt != null) {
            if (dueAt == null) {
                throw new PlannerTaskValidationException("reminderAt must be before dueAt");
            }
            if (!reminderAt.isBefore(dueAt)) {
                throw new PlannerTaskValidationException("reminderAt must be before dueAt");
            }
        }
    }
}
