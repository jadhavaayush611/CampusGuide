package com.campusguide.academic.semesterplanner.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSemesterPlanRequest {

    private String roadmapId;

    @Min(value = 1, message = "Semester number must be at least 1")
    private Integer semesterNumber;

    private List<String> plannedCourseIds;

    private Boolean finalized;
}
