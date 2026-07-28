package com.campusguide.campus.academic.roadmap.entity;

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

@Document(collection = "roadmaps")
@CompoundIndexes({
    @CompoundIndex(name = "createdby_deleted_created_idx", def = "{'createdBy': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "degree_deleted_created_idx", def = "{'degreeProgram': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "dept_deleted_created_idx", def = "{'department': 1, 'isDeleted': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "deleted_created_idx", def = "{'isDeleted': 1, 'createdAt': -1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Roadmap {

    @Id
    private String id;

    private String title;

    private String description;

    @Indexed
    private String degreeProgram;

    @Indexed
    private String department;

    private Integer totalCredits;

    private Integer expectedGraduationYear;

    @Indexed
    private String createdBy;

    @Builder.Default
    private Boolean isDeleted = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class RoadmapBuilder {
        public RoadmapBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public RoadmapBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public RoadmapBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public RoadmapBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
