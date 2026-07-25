package com.campusguide.academic.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {

    private String id;
    private String courseCode;
    private String courseName;
    private String description;
    private String department;
    private Integer credits;
    private Integer semester;
    private List<String> prerequisiteCourseIds;
    private Boolean elective;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
