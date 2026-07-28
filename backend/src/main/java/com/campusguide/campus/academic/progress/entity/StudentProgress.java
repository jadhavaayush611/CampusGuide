package com.campusguide.campus.academic.progress.entity;

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

@Document(collection = "student_progress")
@CompoundIndexes({
    @CompoundIndex(name = "roadmap_created_idx", def = "{'roadmapId': 1, 'createdAt': -1}")
})
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

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class StudentProgressBuilder {
        public StudentProgressBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public StudentProgressBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public StudentProgressBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public StudentProgressBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
