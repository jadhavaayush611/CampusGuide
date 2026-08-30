package com.campusguide.personal.planner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGoalResponse {

    private UUID id;
    private String userId;
    private String title;
    private String description;
    private Integer targetHours;
    private Integer completedHours;
    private String deadline;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    private String category;
    private Instant createdAt;
    private Instant updatedAt;
}
