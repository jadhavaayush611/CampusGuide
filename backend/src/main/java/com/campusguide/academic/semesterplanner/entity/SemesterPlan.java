package com.campusguide.academic.semesterplanner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "semester_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterPlan {

    @Id
    private String id;

    @Indexed
    private String studentId;

    @Indexed
    private String roadmapId;

    @Indexed
    private Integer semesterNumber;

    private List<String> plannedCourseIds;

    private Integer totalPlannedCredits;

    private Boolean finalized;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
