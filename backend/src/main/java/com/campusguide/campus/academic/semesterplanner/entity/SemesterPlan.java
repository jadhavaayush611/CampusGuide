package com.campusguide.campus.academic.semesterplanner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Document(collection = "semester_plans")
@CompoundIndexes({
    @CompoundIndex(name = "student_semester_idx", def = "{'studentId': 1, 'semesterNumber': 1}", unique = true),
    @CompoundIndex(name = "roadmap_semester_idx", def = "{'roadmapId': 1, 'semesterNumber': 1}"),
    @CompoundIndex(name = "finalized_semester_idx", def = "{'finalized': 1, 'semesterNumber': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterPlan {

    @Id
    private String id;

    @jakarta.validation.constraints.NotBlank(message = "Student ID must not be blank")
    @Indexed
    private String studentId;

    @jakarta.validation.constraints.NotBlank(message = "Roadmap ID must not be blank")
    @Indexed
    private String roadmapId;

    @jakarta.validation.constraints.NotNull(message = "Semester number must not be null")
    @jakarta.validation.constraints.Min(value = 1, message = "Semester number must be at least 1")
    @Indexed
    private Integer semesterNumber;

    private List<String> plannedCourseIds;

    private Integer totalPlannedCredits;

    private Boolean finalized;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @org.springframework.data.annotation.Version
    private Long version;

    public static class SemesterPlanBuilder {
        public SemesterPlanBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public SemesterPlanBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public SemesterPlanBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public SemesterPlanBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
