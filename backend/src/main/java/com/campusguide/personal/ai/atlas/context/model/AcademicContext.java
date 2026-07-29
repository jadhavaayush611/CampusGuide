package com.campusguide.personal.ai.atlas.context.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Strongly-typed domain context model for Academic.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicContext {

    private String department;
    private String degreeProgram;
    private String academicStanding;
    private Double gpa;
    private Integer completedCredits;

    @Builder.Default
    private List<String> currentCourses = Collections.emptyList();

    private String summary;
}
