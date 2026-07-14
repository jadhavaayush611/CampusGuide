package com.campusguide.modules.semester.dto;

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
public class SemesterPlanResponse {

    private String id;
    private String studentId;
    private String roadmapId;
    private Integer semesterNumber;
    private List<String> plannedCourseIds;
    private Integer totalPlannedCredits;
    private Boolean finalized;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
