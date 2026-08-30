package com.campusguide.personal.planner.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "study_goals")
@CompoundIndexes({
    @CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGoal {

    @Id
    @jakarta.validation.constraints.NotNull(message = "ID must not be null")
    private UUID id;

    @jakarta.validation.constraints.NotBlank(message = "User ID must not be blank")
    @Indexed
    private String userId;

    @jakarta.validation.constraints.NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @jakarta.validation.constraints.NotNull(message = "Target hours must not be null")
    @jakarta.validation.constraints.Min(value = 1, message = "Target hours must be at least 1")
    private Integer targetHours;

    @jakarta.validation.constraints.NotNull(message = "Completed hours must not be null")
    @jakarta.validation.constraints.Min(value = 0, message = "Completed hours must be at least 0")
    @Builder.Default
    private Integer completedHours = 0;

    private String deadline;

    @JsonProperty("isCompleted")
    @Builder.Default
    private Boolean isCompleted = false;

    @Builder.Default
    private String category = "General";

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;
}
