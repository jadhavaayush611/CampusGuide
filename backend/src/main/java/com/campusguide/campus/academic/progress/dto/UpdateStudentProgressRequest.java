package com.campusguide.campus.academic.progress.dto;

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

}
