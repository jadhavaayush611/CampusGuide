package com.campusguide.campus.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProgressResponse {
    private List<String> completedCourseIds;
    private List<String> plannedCourseIds;
    private List<String> remainingCourseIds;
    private Integer creditsEarned;
    private Integer creditsRemaining;
    private Double completionPercentage;
}
