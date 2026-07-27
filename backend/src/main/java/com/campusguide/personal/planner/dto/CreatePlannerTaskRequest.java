package com.campusguide.personal.planner.dto;

import com.campusguide.personal.planner.entity.TaskPriority;
import com.campusguide.personal.planner.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreatePlannerTaskRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    @NotNull(message = "Task type is mandatory")
    private TaskType type;

    @NotNull(message = "Task priority is mandatory")
    private TaskPriority priority;

    private UUID linkedEventId;

    private LocalDateTime dueAt;

    private LocalDateTime reminderAt;

    private String notes;
}
