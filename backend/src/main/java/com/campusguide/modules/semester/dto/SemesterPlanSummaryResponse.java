package com.campusguide.modules.semester.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterPlanSummaryResponse {

    private String id;
    private String studentId;
    private String roadmapId;
    private Integer semesterNumber;
    private Integer totalPlannedCredits;
    private Boolean finalized;
}
