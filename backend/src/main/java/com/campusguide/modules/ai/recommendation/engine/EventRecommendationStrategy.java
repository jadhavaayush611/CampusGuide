package com.campusguide.modules.ai.recommendation.engine;

import com.campusguide.modules.ai.recommendation.config.RecommendationProperties;
import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationSource;
import com.campusguide.modules.ai.recommendation.dto.RecommendationReason;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.modules.community.entity.Community;
import com.campusguide.modules.event.entity.Event;
import com.campusguide.modules.post.entity.Post;
import com.campusguide.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventRecommendationStrategy implements RecommendationStrategy {

    private final RecommendationProperties properties;

    @Override
    public List<RecommendationResponse> recommend(RecommendationUserContext context) {
        if (context == null || context.getUser() == null || context.getUpcomingEvents() == null) {
            return Collections.emptyList();
        }

        User user = context.getUser();
        List<Event> upcomingEvents = context.getUpcomingEvents();
        List<Post> userPosts = context.getUserPosts() != null ? context.getUserPosts() : Collections.emptyList();
        List<Community> communities = context.getAllActiveCommunities() != null ? context.getAllActiveCommunities() : Collections.emptyList();

        // 1. Identify communities the user participates in (where they have posted)
        Set<String> participatedCommunityIds = userPosts.stream()
                .map(Post::getCommunityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. Map participated community IDs to their council IDs
        Set<String> participatedCouncilIds = communities.stream()
                .filter(c -> participatedCommunityIds.contains(c.getId()))
                .map(Community::getCouncilId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<RecommendationResponse> recommendations = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Event event : upcomingEvents) {
            // Skip if user is already registered for the event
            if (event.getRegisteredUserIds() != null && event.getRegisteredUserIds().contains(user.getId())) {
                continue;
            }

            double score = properties.getEvent().getBaseWeight(); // Base score for upcoming events
            String explanation = "Upcoming campus event.";
            RecommendationReason reason = RecommendationReason.UPCOMING_DEADLINE; // Default reason for upcoming events
            boolean deptMatched = false;

            // Department check: matches user department
            String userDept = user.getDepartment();
            if (userDept != null && !userDept.isBlank()) {
                boolean titleMatch = event.getTitle() != null && event.getTitle().toLowerCase().contains(userDept.toLowerCase());
                boolean descMatch = event.getDescription() != null && event.getDescription().toLowerCase().contains(userDept.toLowerCase());

                if (titleMatch || descMatch) {
                    score += properties.getEvent().getDepartmentWeight();
                    explanation = "This event matches your " + userDept + " department.";
                    reason = RecommendationReason.DEPARTMENT_MATCH;
                    deptMatched = true;
                }
            }

            // Registered communities check: boost if organized by a council matching user's active communities
            if (event.getCouncilId() != null && participatedCouncilIds.contains(event.getCouncilId())) {
                score += properties.getEvent().getCommunityWeight();
                if (!deptMatched) {
                    explanation = "Organized by a council related to your active communities.";
                    reason = RecommendationReason.COMMUNITY_MATCH;
                }
            }

            // Upcoming deadlines check: boost if registration deadline is within 3 days
            if (event.getRegistrationDeadline() != null && event.getRegistrationDeadline().isAfter(now)) {
                long hoursToDeadline = Duration.between(now, event.getRegistrationDeadline()).toHours();
                if (hoursToDeadline <= 72) { // 3 days
                    score += properties.getEvent().getDeadlineWeight();
                    explanation = explanation + " Registration is closing soon!";
                    if (!deptMatched) {
                        reason = RecommendationReason.UPCOMING_DEADLINE;
                    }
                }
            }

            // Cap the score at 1.0
            score = Math.min(1.0, score);

            recommendations.add(RecommendationResponse.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .recommendationType(RecommendationType.EVENT)
                    .recommendationSource(RecommendationSource.EVENT)
                    .reasonCode(reason)
                    .score(score)
                    .explanation(explanation)
                    .metadata(Map.of(
                            "councilId", event.getCouncilId() != null ? event.getCouncilId() : "",
                            "startTime", event.getStartTime() != null ? event.getStartTime().toString() : "",
                            "location", event.getLocation() != null ? event.getLocation() : "",
                            "registrationDeadline", event.getRegistrationDeadline() != null ? event.getRegistrationDeadline().toString() : ""
                    ))
                    .build());
        }

        return recommendations;
    }

    @Override
    public RecommendationType getType() {
        return RecommendationType.EVENT;
    }
}
