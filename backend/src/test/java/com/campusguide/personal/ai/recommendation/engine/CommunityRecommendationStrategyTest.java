package com.campusguide.personal.ai.recommendation.engine;

import com.campusguide.personal.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.personal.ai.recommendation.dto.RecommendationType;
import com.campusguide.personal.ai.recommendation.dto.RecommendationUserContext;
import com.campusguide.campus.community.entity.Community;
import com.campusguide.campus.post.entity.Post;
import com.campusguide.platform.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommunityRecommendationStrategyTest {

    private CommunityRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new CommunityRecommendationStrategy(new com.campusguide.personal.ai.recommendation.config.RecommendationProperties());
    }

    @Test
    void recommend_DepartmentMatchAndDuplicatePrevention() {
        User user = User.builder()
                .id("student-1")
                .department("Computer Science")
                .bio("Interested in coding and algorithms.")
                .build();

        Community commCS = Community.builder()
                .id("comm-cs")
                .name("Computer Science Society")
                .description("Discussion for CS majors.")
                .isActive(true)
                .build();

        Community commRobotics = Community.builder()
                .id("comm-robotics")
                .name("Computer Science Robotics Lab")
                .description("Autonomous systems.")
                .isActive(true)
                .build();

        Community commPainting = Community.builder()
                .id("comm-painting")
                .name("Campus Painting Club")
                .description("Weekly painting classes.")
                .isActive(true)
                .build();

        // Mock a post that shows user already participates in commCS
        Post userPost = Post.builder()
                .id("post-1")
                .authorId("student-1")
                .communityId("comm-cs")
                .isDeleted(false)
                .build();

        RecommendationUserContext context = RecommendationUserContext.builder()
                .user(user)
                .allActiveCommunities(List.of(commCS, commRobotics, commPainting))
                .userPosts(List.of(userPost))
                .build();

        List<RecommendationResponse> results = strategy.recommend(context);

        assertNotNull(results);
        assertEquals(2, results.size()); // commCS should be excluded (duplicate prevention)

        // Check department match event (commRobotics)
        RecommendationResponse roboticsRec = results.stream()
                .filter(r -> r.getId().equals("comm-robotics"))
                .findFirst()
                .orElse(null);
        assertNotNull(roboticsRec);
        assertEquals(RecommendationType.COMMUNITY, roboticsRec.getRecommendationType());
        assertTrue(roboticsRec.getScore() > 0.80); // 0.4 base + 0.45 dept match + 0.15 bio keyword = 1.0 (capped)
        assertTrue(roboticsRec.getExplanation().contains("Computer Science department"));

        // Check normal event (commPainting)
        RecommendationResponse paintingRec = results.stream()
                .filter(r -> r.getId().equals("comm-painting"))
                .findFirst()
                .orElse(null);
        assertNotNull(paintingRec);
        assertEquals(0.40, paintingRec.getScore()); // Base score
        assertTrue(paintingRec.getExplanation().contains("active campus community"));
    }

    @Test
    void recommend_EmptyOrNullContext() {
        assertTrue(strategy.recommend(null).isEmpty());
        assertTrue(strategy.recommend(RecommendationUserContext.builder().build()).isEmpty());
    }
}
