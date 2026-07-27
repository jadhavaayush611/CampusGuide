package com.campusguide.personal.planner.mapper;

import com.campusguide.personal.planner.dto.CreatePlannerTaskRequest;
import com.campusguide.personal.planner.dto.PlannerTaskResponse;
import com.campusguide.personal.planner.entity.PlannerTask;
import com.campusguide.personal.planner.entity.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PlannerTaskMapper {

    public PlannerTask toEntity(CreatePlannerTaskRequest request, UUID userId) {
        if (request == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return PlannerTask.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .status(TaskStatus.TODO)
                .linkedEventId(request.getLinkedEventId())
                .dueAt(request.getDueAt())
                .reminderAt(request.getReminderAt())
                .notes(request.getNotes())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public PlannerTaskResponse toResponse(PlannerTask task) {
        if (task == null) {
            return null;
        }
        return PlannerTaskResponse.builder()
                .id(task.getId())
                .userId(task.getUserId())
                .title(task.getTitle())
                .description(task.getDescription())
                .type(task.getType())
                .priority(task.getPriority())
                .status(task.getStatus())
                .linkedEventId(task.getLinkedEventId())
                .dueAt(task.getDueAt())
                .completedAt(task.getCompletedAt())
                .reminderAt(task.getReminderAt())
                .notes(task.getNotes())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
