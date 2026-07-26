package com.campusguide.campus.academic.roadmap.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoadmapRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Degree program is required")
    private String degreeProgram;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Total credits is required")
    @Min(value = 1, message = "Total credits must be at least 1")
    private Integer totalCredits;

    @NotNull(message = "Expected graduation year is required")
    @Min(value = 2000, message = "Expected graduation year must be at least 2000")
    private Integer expectedGraduationYear;
}
