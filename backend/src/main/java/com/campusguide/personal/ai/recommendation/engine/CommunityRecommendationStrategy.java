package com.campusguide.personal.ai.recommendation.engine;

import com.campusguide.personal.ai.recommendation.config.RecommendationProperties;
import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationSource;
import com.campusguide.personal.ai.recommendation.dto.RecommendationReason;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.platform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommunityRecommendationStrategy implements RecommendationStrategy {

    private final RecommendationProperties properties;

    @Override
    public List<RecommendationResponse> recommend(RecommendationUserContext context) {
        if (context == null || context.getUser() == null || context.getAllActiveCommunities() == null) {
            return Collections.emptyList();
        }

        User user = context.getUser();
        List<Community> communities = context.getAllActiveCommunities();
        List<Post> userPosts = context.getUserPosts() != null ? context.getUserPosts() : Collections.emptyList();

        // 1. Identify communities the user is already participating in (has posted in)
        Set<String> participatedCommunityIds = userPosts.stream()
                .map(Post::getCommunityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<RecommendationResponse> recommendations = new ArrayList<>();
        String userDept = user.getDepartment();
        String userBio = user.getBio() != null ? user.getBio().toLowerCase() : "";

        // Extract keywords from bio
        Set<String> bioKeywords = new HashSet<>();
        if (!userBio.isBlank()) {
            String[] words = userBio.split("\\s+");
            for (String word : words) {
                String cleanWord = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                if (cleanWord.length() > 3) {
                    bioKeywords.add(cleanWord);
                }
            }
        }

        for (Community community : communities) {
            // Duplicate prevention: do not recommend communities the user is already active in
            if (participatedCommunityIds.contains(community.getId())) {
                continue;
            }

            double score = properties.getCommunity().getBaseWeight(); // Base score for active communities
            String explanation = "Join this active campus community.";
            RecommendationReason reason = RecommendationReason.COMMUNITY_MATCH;
            boolean deptMatched = false;

            // Department recommendation
            if (userDept != null && !userDept.isBlank()) {
                boolean nameMatch = community.getName() != null && community.getName().toLowerCase().contains(userDept.toLowerCase());
                boolean descMatch = community.getDescription() != null && community.getDescription().toLowerCase().contains(userDept.toLowerCase());

                if (nameMatch || descMatch) {
                    score += properties.getCommunity().getDepartmentWeight();
                    explanation = "Students in your " + userDept + " department frequently join this community.";
                    reason = RecommendationReason.DEPARTMENT_MATCH;
                    deptMatched = true;
                }
            }

            // Shared interests recommendation (based on bio keywords)
            if (!bioKeywords.isEmpty()) {
                boolean interestMatch = false;
                String communityName = community.getName() != null ? community.getName().toLowerCase() : "";
                String communityDesc = community.getDescription() != null ? community.getDescription().toLowerCase() : "";

                for (String keyword : bioKeywords) {
                    if (communityName.contains(keyword) || communityDesc.contains(keyword)) {
                        interestMatch = true;
                        break;
                    }
                }

                if (interestMatch) {
                    score += properties.getCommunity().getInterestWeight();
                    if (!deptMatched) {
                        explanation = "This community matches interests from your bio.";
                        reason = RecommendationReason.COMMUNITY_MATCH;
                    }
                }
            }

            // Cap the score at 1.0
            score = Math.min(1.0, score);

            recommendations.add(RecommendationResponse.builder()
                    .id(community.getId())
                    .title(community.getName())
                    .description(community.getDescription())
                    .recommendationType(RecommendationType.COMMUNITY)
                    .recommendationSource(RecommendationSource.COMMUNITY)
                    .reasonCode(reason)
                    .score(score)
                    .explanation(explanation)
                    .metadata(Map.of(
                            "councilId", community.getCouncilId() != null ? community.getCouncilId() : "",
                            "memberCount", community.getMemberCount() != null ? community.getMemberCount() : 0
                    ))
                    .build());
        }

        return recommendations;
    }

    @Override
    public RecommendationType getType() {
        return RecommendationType.COMMUNITY;
    }
}
