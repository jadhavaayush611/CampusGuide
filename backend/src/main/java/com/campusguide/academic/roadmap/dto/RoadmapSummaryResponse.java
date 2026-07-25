package com.campusguide.academic.roadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapSummaryResponse {

    private String id;

    private String title;

    private String degreeProgram;

    private String department;

    private Integer expectedGraduationYear;
}
