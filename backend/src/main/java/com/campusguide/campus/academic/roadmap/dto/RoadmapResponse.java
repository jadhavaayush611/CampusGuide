package com.campusguide.campus.academic.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapResponse {

    private String id;

    private String title;

    private String description;

    private String degreeProgram;

    private String department;

    private Integer totalCredits;

    private Integer expectedGraduationYear;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
