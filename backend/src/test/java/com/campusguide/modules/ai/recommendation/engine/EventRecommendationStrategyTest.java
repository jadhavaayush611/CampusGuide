package com.campusguide.modules.ai.recommendation.engine;

import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventRecommendationStrategyTest {

    private EventRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EventRecommendationStrategy(new com.campusguide.modules.ai.recommendation.config.RecommendationProperties());
    }

    @Test
    void recommend_DepartmentMatchingAndFiltering() {
        User user = User.builder()
                .id("student-1")
                .department("Computer Science")
                .build();

        LocalDateTime now = LocalDateTime.now();

        Event eventDeptMatch = Event.builder()
                .id("event-1")
                .title("Computer Science Department Hackathon")
                .description("Solve coding problems.")
                .startTime(now.plusDays(2))
                .registrationDeadline(now.plusDays(1))
                .registeredUserIds(new ArrayList<>())
                .isCancelled(false)
                .isDeleted(false)
                .build();

        Event eventNoMatch = Event.builder()
                .id("event-2")
                .title("Art Club Gathering")
                .description("Painting session.")
                .startTime(now.plusDays(5))
                .registrationDeadline(now.plusDays(4))
                .registeredUserIds(new ArrayList<>())
                .isCancelled(false)
                .isDeleted(false)
                .build();

        Event eventAlreadyRegistered = Event.builder()
                .id("event-3")
                .title("CS Coding Bootcamp")
                .description("Intensive Java coding.")
                .startTime(now.plusDays(2))
                .registrationDeadline(now.plusDays(1))
                .registeredUserIds(List.of("student-1")) // Already registered
                .isCancelled(false)
                .isDeleted(false)
                .build();

        RecommendationUserContext context = RecommendationUserContext.builder()
                .user(user)
                .upcomingEvents(List.of(eventDeptMatch, eventNoMatch, eventAlreadyRegistered))
                .build();

        List<RecommendationResponse> results = strategy.recommend(context);

        assertNotNull(results);
        assertEquals(2, results.size()); // Already registered event-3 should be filtered out

        // Check department match event (event-1)
        RecommendationResponse hackathonRec = results.stream()
                .filter(r -> r.getId().equals("event-1"))
                .findFirst()
                .orElse(null);
        assertNotNull(hackathonRec);
        assertEquals(RecommendationType.EVENT, hackathonRec.getRecommendationType());
        assertTrue(hackathonRec.getScore() > 0.70); // 0.4 base + 0.4 dept match + 0.1 deadline = 0.90
        assertTrue(hackathonRec.getExplanation().contains("Computer Science department"));

        // Check normal event (event-2)
        RecommendationResponse artRec = results.stream()
                .filter(r -> r.getId().equals("event-2"))
                .findFirst()
                .orElse(null);
        assertNotNull(artRec);
        assertEquals(0.40, artRec.getScore()); // Base score
        assertEquals("Upcoming campus event.", artRec.getExplanation());
    }

    @Test
    void recommend_EmptyOrNullContext() {
        assertTrue(strategy.recommend(null).isEmpty());
        assertTrue(strategy.recommend(RecommendationUserContext.builder().build()).isEmpty());
    }
}
