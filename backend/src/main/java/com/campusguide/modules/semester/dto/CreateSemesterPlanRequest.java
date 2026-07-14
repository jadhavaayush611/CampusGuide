package com.campusguide.modules.semester.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSemesterPlanRequest {

    @NotBlank(message = "Roadmap ID is required")
    private String roadmapId;

    @NotNull(message = "Semester number is required")
    @Min(value = 1, message = "Semester number must be at least 1")
    private Integer semesterNumber;

    private List<String> plannedCourseIds;

    private Boolean finalized;
}
