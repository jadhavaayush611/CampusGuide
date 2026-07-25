package com.campusguide.personal.ai.recommendation.engine;

import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RecommendationEngine {

    private final List<RecommendationStrategy> strategies;

    // Constructor injection: Spring automatically discovers and injects all RecommendationStrategy beans
    public RecommendationEngine(List<RecommendationStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Executes all discovered strategies, aggregates and filters results, and sorts by score.
     *
     * @param context the user profile and data context
     * @return sorted list of all recommendations
     */
    public List<RecommendationResponse> generateAllRecommendations(RecommendationUserContext context) {
        List<RecommendationResponse> all = new ArrayList<>();
        for (RecommendationStrategy strategy : strategies) {
            all.addAll(strategy.recommend(context));
        }
        return processRecommendations(all);
    }

    /**
     * Executes only the strategy supporting the specified type, filters and sorts results.
     *
     * @param context the user profile and data context
     * @param type the recommendation type to filter by
     * @return sorted list of recommendations of the specified type
     */
    public List<RecommendationResponse> generateRecommendationsByType(RecommendationUserContext context, RecommendationType type) {
        List<RecommendationResponse> all = new ArrayList<>();
        for (RecommendationStrategy strategy : strategies) {
            if (strategy.getType() == type) {
                all.addAll(strategy.recommend(context));
            }
        }
        return processRecommendations(all);
    }

    /**
     * Aggregates recommendations, removes duplicates (by type and ID), and sorts by score descending.
     */
    private List<RecommendationResponse> processRecommendations(List<RecommendationResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return Collections.emptyList();
        }

        // Remove duplicates: key is RecommendationType + "_" + ID (or Title if ID is null)
        Map<String, RecommendationResponse> uniqueMap = new LinkedHashMap<>();
        for (RecommendationResponse resp : responses) {
            String key = resp.getRecommendationType() + "_" + (resp.getId() != null ? resp.getId() : resp.getTitle());
            // If duplicate exists, keep the one with the higher score
            if (uniqueMap.containsKey(key)) {
                if (resp.getScore() > uniqueMap.get(key).getScore()) {
                    uniqueMap.put(key, resp);
                }
            } else {
                uniqueMap.put(key, resp);
            }
        }

        // Sort by score in descending order
        return uniqueMap.values().stream()
                .sorted(Comparator.comparing(RecommendationResponse::getScore, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
