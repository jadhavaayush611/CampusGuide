package com.campusguide.campus.academic.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {

    @Size(max = 20, message = "Course code must not exceed 20 characters")
    private String courseCode;

    @Size(max = 200, message = "Course name must not exceed 200 characters")
    private String courseName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String department;

    @Min(value = 1, message = "Credits must be at least 1")
    private Integer credits;

    @Min(value = 1, message = "Semester must be at least 1")
    private Integer semester;

    private List<String> prerequisiteCourseIds;

    private Boolean elective;

    private Boolean active;
}
