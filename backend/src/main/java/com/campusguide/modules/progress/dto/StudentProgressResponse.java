package com.campusguide.modules.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressResponse {

    private String id;
    private String studentId;
    private String roadmapId;
    private List<String> completedCourseIds;
    private Integer currentSemester;
    private Integer totalCreditsEarned;
    private Double currentGpa;
    private Boolean graduationEligible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
