package com.campusguide.modules.progress.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressSummaryResponse {

    private String id;
    private String studentId;
    private String roadmapId;
    private Integer currentSemester;
    private Integer totalCreditsEarned;
    private Double currentGpa;
    private Boolean graduationEligible;
}
