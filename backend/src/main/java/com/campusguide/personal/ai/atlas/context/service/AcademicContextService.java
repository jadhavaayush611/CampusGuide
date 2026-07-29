package com.campusguide.personal.ai.atlas.context.service;

import com.campusguide.campus.academic.progress.entity.StudentProgress;
import com.campusguide.campus.academic.progress.repository.StudentProgressRepository;
import com.campusguide.personal.ai.atlas.context.model.AcademicContext;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for querying, summarizing, and normalizing Academic domain context.
 */
@Service
@Slf4j
public class AcademicContextService {

    private static final int MAX_COURSES = 5;
    private final StudentProgressRepository studentProgressRepository;

    public AcademicContextService(@Autowired(required = false) StudentProgressRepository studentProgressRepository) {
        this.studentProgressRepository = studentProgressRepository;
    }

    /**
     * Queries, filters, and normalizes academic context with deterministic ordering and bounded limits.
     *
     * @param userId target user ID
     * @param request chat request
     * @return normalized AcademicContext
     */
    public AcademicContext getAcademicContext(String userId, AtlasChatRequest request) {
        String department = "Computer Science";
        String degreeProgram = "Computer Science Undergraduate";
        String academicStanding = "GOOD_STANDING";
        Double gpa = 3.8;
        Integer completedCredits = 60;
        List<String> currentCourses = List.of("CS101 - Intro to CS", "CS201 - Data Structures");

        if (request != null && request.getContextPlaceholders() != null) {
            Object deptPlaceholder = request.getContextPlaceholders().get("department");
            if (deptPlaceholder != null && StringUtils.hasText(deptPlaceholder.toString())) {
                department = deptPlaceholder.toString();
                degreeProgram = department + " Undergraduate";
            }
        }

        if (studentProgressRepository != null && StringUtils.hasText(userId)) {
            try {
                Optional<StudentProgress> progressOpt = studentProgressRepository.findByStudentId(userId);
                if (progressOpt.isPresent()) {
                    StudentProgress sp = progressOpt.get();
                    if (sp.getCurrentGpa() != null) {
                        gpa = sp.getCurrentGpa();
                    }
                    if (sp.getTotalCreditsEarned() != null) {
                        completedCredits = sp.getTotalCreditsEarned();
                    }
                    if (sp.getCompletedCourseIds() != null && !sp.getCompletedCourseIds().isEmpty()) {
                        currentCourses = sp.getCompletedCourseIds().stream()
                                .distinct()
                                .sorted()
                                .limit(MAX_COURSES)
                                .collect(Collectors.toList());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch academic progress for userId [{}]: {}", userId, e.getMessage());
            }
        }

        // Ensure currentCourses is distinct, sorted deterministically, bounded
        List<String> boundedCourses = currentCourses.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .limit(MAX_COURSES)
                .collect(Collectors.toList());

        String summary = String.format("Academic context summary: %s undergraduate (%s, GPA: %.2f).",
                department, academicStanding, gpa);

        return AcademicContext.builder()
                .department(department)
                .degreeProgram(degreeProgram)
                .academicStanding(academicStanding)
                .gpa(gpa)
                .completedCredits(completedCredits)
                .currentCourses(boundedCourses)
                .summary(summary)
                .build();
    }
}
