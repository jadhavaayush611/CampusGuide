package com.campusguide.academic.roadmap.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoadmapRequest {

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private String degreeProgram;

    private String department;

    @Min(value = 1, message = "Total credits must be at least 1")
    private Integer totalCredits;

    @Min(value = 2000, message = "Expected graduation year must be at least 2000")
    private Integer expectedGraduationYear;
}
