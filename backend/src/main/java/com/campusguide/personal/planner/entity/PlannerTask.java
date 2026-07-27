package com.campusguide.personal.planner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "planner_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannerTask {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

    private String title;

    private String description;

    private TaskType type;

    private TaskPriority priority;

    private TaskStatus status;

    @Indexed
    private UUID linkedEventId;

    private LocalDateTime dueAt;

    private LocalDateTime completedAt;

    private LocalDateTime reminderAt;

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
