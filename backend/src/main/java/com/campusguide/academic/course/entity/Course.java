package com.campusguide.academic.course.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "courses")
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
