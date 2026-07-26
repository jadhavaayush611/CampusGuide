package com.campusguide.personal.ai.recommendation.engine;

import com.campusguide.personal.ai.recommendation.config.RecommendationProperties;
import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationSource;
import com.campusguide.personal.ai.recommendation.dto.RecommendationReason;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.academic.course.entity.Course;
import com.campusguide.campus.resource.entity.Resource;
import com.campusguide.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ResourceRecommendationStrategy implements RecommendationStrategy {

    private final RecommendationProperties properties;

    @Override
    public List<RecommendationResponse> recommend(RecommendationUserContext context) {
        if (context == null || context.getUser() == null || context.getAllActiveResources() == null) {
            return Collections.emptyList();
        }

        User user = context.getUser();
        List<Resource> resources = context.getAllActiveResources();
        List<Course> activeCourses = context.getAllActiveCourses() != null ? context.getAllActiveCourses() : Collections.emptyList();
        List<String> completedIds = context.getCompletedCourseIds() != null ? context.getCompletedCourseIds() : Collections.emptyList();
        int currentSemester = context.getCurrentSemester() != null ? context.getCurrentSemester() : 1;

        // 1. Determine enrolled (current semester) courses
        // First look at semester plans in the context to get current semester course IDs
        Set<String> currentCourseIds = new HashSet<>();
        if (context.getSemesterPlans() != null) {
            context.getSemesterPlans().stream()
                    .filter(p -> Integer.valueOf(currentSemester).equals(p.getSemesterNumber()))
                    .flatMap(p -> p.getPlannedCourseIds() != null ? p.getPlannedCourseIds().stream() : java.util.stream.Stream.empty())
                    .forEach(currentCourseIds::add);
        }

        // Fallback: If no semester plan, assume active courses in user's department matching current semester
        if (currentCourseIds.isEmpty()) {
            activeCourses.stream()
                    .filter(c -> c.getSemester() != null && c.getSemester() == currentSemester)
                    .map(Course::getId)
                    .forEach(currentCourseIds::add);
        }

        // 2. Identify roadmap courses (not completed)
        List<Course> roadmapCourses = activeCourses.stream()
                .filter(c -> !completedIds.contains(c.getId()))
                .toList();

        List<Course> currentCourses = activeCourses.stream()
                .filter(c -> currentCourseIds.contains(c.getId()))
                .toList();

        List<RecommendationResponse> recommendations = new ArrayList<>();

        for (Resource resource : resources) {
            if (Boolean.TRUE.equals(resource.getIsDeleted())) {
                continue;
            }

            double score = properties.getResource().getBaseWeight(); // Base score
            String explanation = "Educational resource available on CampusGuide.";
            RecommendationReason reason = RecommendationReason.POPULAR_RESOURCE;
            boolean courseMatched = false;

            // Search matching against current semester courses
            for (Course course : currentCourses) {
                if (matchesCourse(resource, course)) {
                    score = properties.getResource().getEnrolledWeight(); // High score for current enrolled courses
                    explanation = "This document is relevant to your enrolled " + course.getCourseName() + " course.";
                    reason = RecommendationReason.PREREQUISITE_MATCH;
                    courseMatched = true;
                    break;
                }
            }

            // Search matching against other roadmap courses if not matched with current
            if (!courseMatched) {
                for (Course course : roadmapCourses) {
                    if (matchesCourse(resource, course)) {
                        score = properties.getResource().getRoadmapWeight(); // Good score for roadmap courses
                        explanation = "This document is relevant to " + course.getCourseName() + " on your degree roadmap.";
                        reason = RecommendationReason.PREREQUISITE_MATCH;
                        courseMatched = true;
                        break;
                    }
                }
            }

            // Department match boost (if uploader/council matches department keywords)
            String userDepartment = null;
            if (!courseMatched && userDepartment != null && !userDepartment.isBlank()) {
                String dept = userDepartment.toLowerCase();
                boolean tagMatch = resource.getTags() != null && resource.getTags().stream().anyMatch(t -> t.toLowerCase().contains(dept));
                boolean titleMatch = resource.getTitle() != null && resource.getTitle().toLowerCase().contains(dept);

                if (tagMatch || titleMatch) {
                    score = properties.getResource().getDepartmentWeight();
                    explanation = "This resource is relevant to your " + userDepartment + " department.";
                    reason = RecommendationReason.DEPARTMENT_MATCH;
                }
            }

            recommendations.add(RecommendationResponse.builder()
                    .id(resource.getId())
                    .title(resource.getTitle())
                    .description(resource.getDescription())
                    .recommendationType(RecommendationType.RESOURCE)
                    .recommendationSource(RecommendationSource.RESOURCE)
                    .reasonCode(reason)
                    .score(score)
                    .explanation(explanation)
                    .metadata(Map.of(
                            "fileName", resource.getOriginalFileName() != null ? resource.getOriginalFileName() : "",
                            "fileType", resource.getFileType() != null ? resource.getFileType() : "",
                            "downloadUrl", resource.getDownloadUrl() != null ? resource.getDownloadUrl() : ""
                    ))
                    .build());
        }

        return recommendations;
    }

    private boolean matchesCourse(Resource resource, Course course) {
        String code = course.getCourseCode().toLowerCase();
        String name = course.getCourseName().toLowerCase();

        // 1. Tag match
        if (resource.getTags() != null) {
            for (String tag : resource.getTags()) {
                String lowerTag = tag.toLowerCase();
                if (lowerTag.contains(code) || lowerTag.contains(name) || name.contains(lowerTag)) {
                    return true;
                }
            }
        }

        // 2. Title match
        if (resource.getTitle() != null) {
            String lowerTitle = resource.getTitle().toLowerCase();
            if (lowerTitle.contains(code) || lowerTitle.contains(name)) {
                return true;
            }
        }

        // 3. Description match
        if (resource.getDescription() != null) {
            String lowerDesc = resource.getDescription().toLowerCase();
            if (lowerDesc.contains(code) || lowerDesc.contains(name)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public RecommendationType getType() {
        return RecommendationType.RESOURCE;
    }
}
