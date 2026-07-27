package com.campusguide.personal.ai.recommendation.engine;

import com.campusguide.campus.event.entity.Event;
import com.campusguide.personal.ai.recommendation.config.RecommendationProperties;
import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.platform.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventRecommendationStrategyTest {

    private EventRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EventRecommendationStrategy(new RecommendationProperties());
    }

    @Test
    void recommend_DepartmentMatchingAndFiltering() {
        User user = User.builder()
                .id("student-1")
                .username("student1")
                .build();

        LocalDateTime now = LocalDateTime.now();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Event eventDeptMatch = Event.builder()
                .id(id1)
                .title("Computer Science Department Hackathon")
                .description("Solve coding problems.")
                .venue("Main Hall")
                .startTime(now.plusDays(2))
                .registrationEnd(now.plusDays(1))
                .build();

        Event eventNoMatch = Event.builder()
                .id(id2)
                .title("Art Club Gathering")
                .description("Painting session.")
                .venue("Studio 1")
                .startTime(now.plusDays(5))
                .registrationEnd(now.plusDays(4))
                .build();

        RecommendationUserContext context = RecommendationUserContext.builder()
                .user(user)
                .upcomingEvents(List.of(eventDeptMatch, eventNoMatch))
                .build();

        List<RecommendationResponse> results = strategy.recommend(context);

        assertNotNull(results);
        assertEquals(2, results.size());

        // Check department match event (event-1)
        RecommendationResponse hackathonRec = results.stream()
                .filter(r -> r.getId().equals(id1.toString()))
                .findFirst()
                .orElse(null);
        assertNotNull(hackathonRec);
        assertEquals(RecommendationType.EVENT, hackathonRec.getRecommendationType());
        assertTrue(hackathonRec.getScore() >= 0.40);
        assertTrue(hackathonRec.getExplanation().contains("campus event"));

        // Check normal event (event-2)
        RecommendationResponse artRec = results.stream()
                .filter(r -> r.getId().equals(id2.toString()))
                .findFirst()
                .orElse(null);
        assertNotNull(artRec);
        assertEquals(0.40, artRec.getScore()); // Base score
        assertEquals("Upcoming campus event.", artRec.getExplanation());
    }

    @Test
    void recommend_EmptyOrNullContext() {
        assertTrue(strategy.recommend(null).isEmpty());
    }
}
