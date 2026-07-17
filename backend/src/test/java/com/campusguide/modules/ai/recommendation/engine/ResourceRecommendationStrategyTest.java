package com.campusguide.modules.ai.recommendation.engine;

import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.resource.entity.Resource;
import com.campusguide.modules.semester.entity.SemesterPlan;
import com.campusguide.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceRecommendationStrategyTest {

    private ResourceRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ResourceRecommendationStrategy(new com.campusguide.modules.ai.recommendation.config.RecommendationProperties());
    }

    @Test
    void recommend_CourseMatchingAndRelevanceScoring() {
        User user = User.builder()
                .id("student-1")
                .department("Computer Science")
                .build();

        Course courseCurrent = Course.builder()
                .id("course-ds")
                .courseCode("CS102")
                .courseName("Data Structures")
                .department("Computer Science")
                .semester(1)
                .active(true)
                .build();

        Course courseRoadmap = Course.builder()
                .id("course-algo")
                .courseCode("CS201")
                .courseName("Algorithms")
                .department("Computer Science")
                .semester(2)
                .active(true)
                .build();

        SemesterPlan plan = SemesterPlan.builder()
                .studentId("student-1")
                .semesterNumber(1)
                .plannedCourseIds(List.of("course-ds"))
                .build();

        Resource resEnrolled = Resource.builder()
                .id("res-1")
                .title("Advanced Tree Algorithms Guide")
                .description("Advanced topics on binary search trees.")
                .tags(List.of("CS102", "Trees"))
                .isDeleted(false)
                .build();

        Resource resRoadmap = Resource.builder()
                .id("res-2")
                .title("Sorting Cheat Sheet")
                .description("Algorithms quick reference.")
                .tags(List.of("CS201", "Sorting"))
                .isDeleted(false)
                .build();

        Resource resOther = Resource.builder()
                .id("res-3")
                .title("General Campus Map")
                .description("Map of campus.")
                .tags(List.of("campus"))
                .isDeleted(false)
                .build();

        RecommendationUserContext context = RecommendationUserContext.builder()
                .user(user)
                .currentSemester(1)
                .allActiveCourses(List.of(courseCurrent, courseRoadmap))
                .semesterPlans(List.of(plan))
                .allActiveResources(List.of(resEnrolled, resRoadmap, resOther))
                .build();

        List<RecommendationResponse> results = strategy.recommend(context);

        assertNotNull(results);
        assertEquals(3, results.size());

        // Enrolled course resource (res-1)
        RecommendationResponse enrolledRec = results.stream()
                .filter(r -> r.getId().equals("res-1"))
                .findFirst()
                .orElse(null);
        assertNotNull(enrolledRec);
        assertEquals(RecommendationType.RESOURCE, enrolledRec.getRecommendationType());
        assertEquals(0.90, enrolledRec.getScore());
        assertTrue(enrolledRec.getExplanation().contains("relevant to your enrolled Data Structures course"));

        // Roadmap course resource (res-2)
        RecommendationResponse roadmapRec = results.stream()
                .filter(r -> r.getId().equals("res-2"))
                .findFirst()
                .orElse(null);
        assertNotNull(roadmapRec);
        assertEquals(0.70, roadmapRec.getScore());
        assertTrue(roadmapRec.getExplanation().contains("relevant to Algorithms on your degree roadmap"));

        // General resource (res-3)
        RecommendationResponse otherRec = results.stream()
                .filter(r -> r.getId().equals("res-3"))
                .findFirst()
                .orElse(null);
        assertNotNull(otherRec);
        assertEquals(0.30, otherRec.getScore()); // Base score
    }

    @Test
    void recommend_EmptyOrNullContext() {
        assertTrue(strategy.recommend(null).isEmpty());
        assertTrue(strategy.recommend(RecommendationUserContext.builder().build()).isEmpty());
    }
}
