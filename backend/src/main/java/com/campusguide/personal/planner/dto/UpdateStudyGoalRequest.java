package com.campusguide.personal.planner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStudyGoalRequest {

    private String title;

    private String description;

    @Min(value = 1, message = "Target hours must be at least 1")
    private Integer targetHours;

    @Min(value = 0, message = "Completed hours must be at least 0")
    private Integer completedHours;

    private String deadline;

    private String category;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;
}
