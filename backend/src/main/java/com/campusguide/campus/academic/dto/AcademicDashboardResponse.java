package com.campusguide.campus.academic.dto;

import com.campusguide.campus.academic.course.dto.CourseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicDashboardResponse {
    private String roadmapTitle;
    private String degreeProgram;
    private String department;
    private Integer currentSemester;
    private Integer totalCreditsRequired;
    private Integer totalCreditsEarned;
    private Integer remainingCredits;
    private Double completionPercentage;
    private Double currentGpa;
    private Boolean graduationEligible;
    private Integer plannedCredits;
    private Boolean finalizedSemesterPlan;
    private List<CourseResponse> completedCourses;
    private List<CourseResponse> remainingCourses;
}
