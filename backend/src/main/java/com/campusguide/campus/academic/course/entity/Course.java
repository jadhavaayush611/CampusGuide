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

    @jakarta.validation.constraints.NotBlank(message = "Course code must not be blank")
    @Indexed(unique = true)
    private String courseCode;

    @jakarta.validation.constraints.NotBlank(message = "Course name must not be blank")
    private String courseName;

    private String description;

    @jakarta.validation.constraints.NotBlank(message = "Department must not be blank")
    @Indexed
    private String department;

    @jakarta.validation.constraints.NotNull(message = "Credits must not be null")
    @jakarta.validation.constraints.Min(value = 0, message = "Credits cannot be negative")
    private Integer credits;

    @jakarta.validation.constraints.NotNull(message = "Semester must not be null")
    @jakarta.validation.constraints.Min(value = 1, message = "Semester must be at least 1")
    @Indexed
    private Integer semester;

    private List<String> prerequisiteCourseIds;

    private Boolean elective;

    private Boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @org.springframework.data.annotation.Version
    private Long version;

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
