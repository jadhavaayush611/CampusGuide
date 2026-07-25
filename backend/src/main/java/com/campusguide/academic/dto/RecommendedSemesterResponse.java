package com.campusguide.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedSemesterResponse {
    private Integer semesterNumber;
    private List<String> recommendedCourseIds;
    private Integer totalCredits;
    private List<String> prerequisiteWarnings;
}
