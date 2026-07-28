package com.campusguide.campus.academic.course.entity;

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

@Document(collection = "courses")
@CompoundIndexes({
    @CompoundIndex(name = "dept_active_idx", def = "{'department': 1, 'active': 1}"),
    @CompoundIndex(name = "semester_active_idx", def = "{'semester': 1, 'active': 1}"),
    @CompoundIndex(name = "elective_active_idx", def = "{'elective': 1, 'active': 1}"),
    @CompoundIndex(name = "active_code_idx", def = "{'active': 1, 'courseCode': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    private String id;

    @Indexed(unique = true)
    private String courseCode;

    private String courseName;

    private String description;

    @Indexed
    private String department;

    private Integer credits;

    @Indexed
    private Integer semester;

    private List<String> prerequisiteCourseIds;

    private Boolean elective;

    private Boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public static class CourseBuilder {
        public CourseBuilder createdAt(Instant instant) {
            this.createdAt = instant;
            return this;
        }

        public CourseBuilder createdAt(LocalDateTime dateTime) {
            this.createdAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }

        public CourseBuilder updatedAt(Instant instant) {
            this.updatedAt = instant;
            return this;
        }

        public CourseBuilder updatedAt(LocalDateTime dateTime) {
            this.updatedAt = dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
            return this;
        }
    }
}
