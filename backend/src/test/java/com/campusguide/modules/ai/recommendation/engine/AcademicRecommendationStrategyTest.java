package com.campusguide.modules.ai.recommendation.engine;

import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.modules.course.entity.Course;
import com.campusguide.modules.progress.entity.StudentProgress;
import com.campusguide.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcademicRecommendationStrategyTest {

    private AcademicRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AcademicRecommendationStrategy(new com.campusguide.modules.ai.recommendation.config.RecommendationProperties());
    }

    @Test
    void recommend_MatchingRoadmapAndPrerequisites() {
        User user = User.builder()
                .id("student-1")
                .department("Computer Science")
                .build();

        Course courseA = Course.builder()
                .id("course-A")
                .courseCode("CS101")
                .courseName("Intro to Computer Science")
                .department("Computer Science")
                .semester(1)
                .active(true)
                .build();

        Course courseB = Course.builder()
                .id("course-B")
                .courseCode("CS102")
                .courseName("Data Structures")
                .department("Computer Science")
                .semester(2)
                .prerequisiteCourseIds(List.of("course-A"))
                .active(true)
                .build();

        Course courseC = Course.builder()
                .id("course-C")
                .courseCode("CS201")
                .courseName("Algorithms")
                .department("Computer Science")
                .semester(2)
                .prerequisiteCourseIds(List.of("course-B"))
                .active(true)
                .build();

        // Case 1: Student completed CS101, is in semester 1.
        // Should recommend CS102 (semester 2, prerequisites met) with high score (0.90).
        // Should recommend CS102 as prerequisite for CS201 (since CS201 prerequisites CS102 which is missing).
        RecommendationUserContext context = RecommendationUserContext.builder()
                .user(user)
                .currentSemester(1)
                .completedCourseIds(List.of("course-A"))
                .allActiveCourses(List.of(courseA, courseB, courseC))
                .build();

        List<RecommendationResponse> results = strategy.recommend(context);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        // Check if CS102 is recommended for next semester (score 0.90)
        RecommendationResponse cs102Rec = results.stream()
                .filter(r -> r.getId().equals("course-B") && r.getScore() == 0.90)
                .findFirst()
                .orElse(null);
        assertNotNull(cs102Rec);
        assertEquals(RecommendationType.ACADEMIC, cs102Rec.getRecommendationType());
        assertTrue(cs102Rec.getExplanation().contains("next prerequisite"));

        // Check if CS102 is also recommended as a prerequisite warning suggestion for CS201 (score 0.80)
        RecommendationResponse cs102PrereqRec = results.stream()
                .filter(r -> r.getId().equals("course-B") && r.getScore() == 0.80)
                .findFirst()
                .orElse(null);
        assertNotNull(cs102PrereqRec);
        assertTrue(cs102PrereqRec.getExplanation().contains("prerequisite for CS201"));
    }

    @Test
    void recommend_EmptyOrNullContext() {
        assertTrue(strategy.recommend(null).isEmpty());
        assertTrue(strategy.recommend(RecommendationUserContext.builder().build()).isEmpty());
    }
}
