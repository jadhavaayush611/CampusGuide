package com.campusguide.personal.ai.recommendation.engine;

import com.campusguide.personal.ai.recommendation.config.RecommendationProperties;
import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationSource;
import com.campusguide.personal.ai.recommendation.dto.RecommendationReason;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.academic.course.entity.Course;
import com.campusguide.academic.progress.entity.StudentProgress;
import com.campusguide.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AcademicRecommendationStrategy implements RecommendationStrategy {

    private final RecommendationProperties properties;

    @Override
    public List<RecommendationResponse> recommend(RecommendationUserContext context) {
        if (context == null || context.getUser() == null) {
            return Collections.emptyList();
        }

        User user = context.getUser();
        StudentProgress progress = context.getStudentProgress();
        List<Course> activeCourses = context.getAllActiveCourses();

        if (activeCourses == null || activeCourses.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> completedIds = context.getCompletedCourseIds() != null
                ? context.getCompletedCourseIds()
                : Collections.emptyList();

        int currentSemester = context.getCurrentSemester() != null ? context.getCurrentSemester() : 1;
        String userDept = user.getDepartment();

        // Map course ID to Course object for easy lookup
        Map<String, Course> courseMap = new HashMap<>();
        for (Course course : activeCourses) {
            courseMap.put(course.getId(), course);
        }

        List<RecommendationResponse> recommendations = new ArrayList<>();

        for (Course course : activeCourses) {
            // Skip already completed courses
            if (completedIds.contains(course.getId())) {
                continue;
            }

            boolean isDeptMatch = userDept != null && userDept.equalsIgnoreCase(course.getDepartment());
            if (!isDeptMatch) {
                continue; // Only recommend courses from the user's department for academic recommendations
            }

            // Check prerequisites
            List<String> prereqIds = course.getPrerequisiteCourseIds();
            List<Course> missingPrereqs = new ArrayList<>();
            if (prereqIds != null) {
                for (String prereqId : prereqIds) {
                    if (!completedIds.contains(prereqId)) {
                        Course prereqCourse = courseMap.get(prereqId);
                        if (prereqCourse != null) {
                            missingPrereqs.add(prereqCourse);
                        }
                    }
                }
            }

            if (missingPrereqs.isEmpty()) {
                // Course is ready to be taken
                double score = properties.getAcademic().getDepartmentWeight(); // Base score for department match
                String explanation = "This course is offered by your " + userDept + " department.";
                RecommendationReason reason = RecommendationReason.DEPARTMENT_MATCH;

                if (course.getSemester() != null) {
                    if (course.getSemester() == currentSemester) {
                        score = properties.getAcademic().getCurrentSemesterWeight();
                        explanation = "This course is on your roadmap for the current semester.";
                    } else if (course.getSemester() == currentSemester + 1) {
                        score = properties.getAcademic().getPrerequisiteWeight();
                        explanation = "This course is the next prerequisite in your roadmap.";
                        reason = RecommendationReason.PREREQUISITE_MATCH;
                    } else if (course.getSemester() > currentSemester + 1) {
                        score = properties.getAcademic().getDepartmentWeight();
                        explanation = "This course is on your degree roadmap for a future semester.";
                    }
                }

                recommendations.add(RecommendationResponse.builder()
                        .id(course.getId())
                        .title(course.getCourseCode() + " - " + course.getCourseName())
                        .description(course.getDescription())
                        .recommendationType(RecommendationType.ACADEMIC)
                        .recommendationSource(RecommendationSource.ROADMAP)
                        .reasonCode(reason)
                        .score(score)
                        .explanation(explanation)
                        .metadata(Map.of(
                                "courseCode", course.getCourseCode(),
                                "semester", course.getSemester() != null ? course.getSemester() : 0,
                                "credits", course.getCredits() != null ? course.getCredits() : 0
                        ))
                        .build());
            } else {
                // If this course belongs to the current or next semester, recommend its prerequisites instead
                if (course.getSemester() != null && (course.getSemester() == currentSemester || course.getSemester() == currentSemester + 1)) {
                    for (Course missingPrereq : missingPrereqs) {
                        recommendations.add(RecommendationResponse.builder()
                                .id(missingPrereq.getId())
                                .title(missingPrereq.getCourseCode() + " - " + missingPrereq.getCourseName())
                                .description(missingPrereq.getDescription())
                                .recommendationType(RecommendationType.ACADEMIC)
                                .recommendationSource(RecommendationSource.COURSE)
                                .reasonCode(RecommendationReason.PREREQUISITE_MATCH)
                                .score(properties.getAcademic().getMissingPrereqBoost())
                                .explanation("This course is a prerequisite for " + course.getCourseCode() + " in your roadmap.")
                                .metadata(Map.of(
                                        "courseCode", missingPrereq.getCourseCode(),
                                        "semester", missingPrereq.getSemester() != null ? missingPrereq.getSemester() : 0,
                                        "credits", missingPrereq.getCredits() != null ? missingPrereq.getCredits() : 0,
                                        "prerequisiteFor", course.getCourseCode()
                                ))
                                .build());
                    }
                }
            }
        }

        return recommendations;
    }

    @Override
    public RecommendationType getType() {
        return RecommendationType.ACADEMIC;
    }
}
