package com.campusguide.personal.planner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Document(collection = "planner_tasks")
@CompoundIndexes({
    @CompoundIndex(name = "user_status_due_idx", def = "{'userId': 1, 'status': 1, 'dueAt': 1}"),
    @CompoundIndex(name = "user_due_idx", def = "{'userId': 1, 'dueAt': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannerTask {

    @Id
    private UUID id;

    @Indexed
    private String userId;

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
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class PlannerTaskBuilder {
        public PlannerTaskBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public PlannerTaskBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public PlannerTaskBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public PlannerTaskBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
