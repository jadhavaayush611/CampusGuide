package com.campusguide.modules.progress.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentProgressRequest {

    private String studentId;

    private String roadmapId;

    @Min(value = 1, message = "Semester must be greater than 0")
    private Integer currentSemester;

    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "10.0", message = "GPA cannot exceed 10.0")
    private Double currentGpa;

    @Min(value = 0, message = "Total credits earned must be at least 0")
    private Integer totalCreditsEarned;

    private Boolean graduationEligible;
}
