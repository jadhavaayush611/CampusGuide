package com.campusguide.personal.planner.dto;

import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskStatus;
import com.campusguide.personal.planner.entity.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannerTaskResponse {

    private UUID id;
    private String userId;
    private String title;
    private String description;
    private TaskType type;
    private TaskPriority priority;
    private TaskStatus status;
    private UUID linkedEventId;
    private LocalDateTime dueAt;
    private LocalDateTime completedAt;
    private LocalDateTime reminderAt;
    private String notes;
    @Builder.Default
    private java.util.List<com.campusguide.common.attachment.dto.AttachmentResponse> attachments = new java.util.ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
