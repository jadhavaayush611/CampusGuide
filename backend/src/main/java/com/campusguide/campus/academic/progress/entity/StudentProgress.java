package com.campusguide.campus.academic.progress.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "student_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgress {

    @Id
    private String id;

    @Indexed(unique = true)
    private String studentId;

    @Indexed
    private String roadmapId;

    private List<String> completedCourseIds;

    private Integer currentSemester;

    private Integer totalCreditsEarned;

    private Double currentGpa;

    private Boolean graduationEligible;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
