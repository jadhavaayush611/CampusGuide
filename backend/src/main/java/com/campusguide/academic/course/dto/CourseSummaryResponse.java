package com.campusguide.academic.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummaryResponse {

    private String id;
    private String courseCode;
    private String courseName;
    private String department;
    private Integer credits;
    private Integer semester;
    private Boolean elective;
}
