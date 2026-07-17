package com.campusguide.modules.ai.recommendation.engine;

import com.campusguide.modules.ai.recommendation.dto.RecommendationResponse;
import com.campusguide.modules.ai.recommendation.dto.RecommendationType;
import com.campusguide.modules.ai.recommendation.dto.RecommendationUserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationEngineTest {

    private RecommendationEngine engine;
    private RecommendationStrategy mockStrategy1;
    private RecommendationStrategy mockStrategy2;

    @BeforeEach
    void setUp() {
        mockStrategy1 = mock(RecommendationStrategy.class);
        mockStrategy2 = mock(RecommendationStrategy.class);
        engine = new RecommendationEngine(List.of(mockStrategy1, mockStrategy2));
    }

    @Test
    void generateAllRecommendations_AggregatesDeduplicatesAndSorts() {
        RecommendationUserContext context = RecommendationUserContext.builder().build();

        // Mock recommendations from strategy 1
        RecommendationResponse rec1 = RecommendationResponse.builder()
                .id("item-1")
                .title("Rec 1")
                .recommendationType(RecommendationType.ACADEMIC)
                .score(0.85)
                .explanation("Ex 1")
                .build();

        // Mock a duplicate with lower score
        RecommendationResponse rec1Duplicate = RecommendationResponse.builder()
                .id("item-1")
                .title("Rec 1")
                .recommendationType(RecommendationType.ACADEMIC)
                .score(0.60)
                .explanation("Ex 1 lower score")
                .build();

        // Mock recommendations from strategy 2
        RecommendationResponse rec2 = RecommendationResponse.builder()
                .id("item-2")
                .title("Rec 2")
                .recommendationType(RecommendationType.EVENT)
                .score(0.95)
                .explanation("Ex 2")
                .build();

        when(mockStrategy1.recommend(context)).thenReturn(List.of(rec1, rec1Duplicate));
        when(mockStrategy2.recommend(context)).thenReturn(List.of(rec2));

        List<RecommendationResponse> results = engine.generateAllRecommendations(context);

        assertNotNull(results);
        assertEquals(2, results.size()); // Duplicate item-1 should be removed, leaving 2 items total

        // Verify sorting order: highest score first (rec2 with 0.95, then rec1 with 0.85)
        assertEquals("item-2", results.get(0).getId());
        assertEquals(0.95, results.get(0).getScore());

        assertEquals("item-1", results.get(1).getId());
        assertEquals(0.85, results.get(1).getScore()); // Higher score of the duplicate is preserved
    }

    @Test
    void generateRecommendationsByType_ExecutesOnlyMatchingStrategy() {
        RecommendationUserContext context = RecommendationUserContext.builder().build();

        when(mockStrategy1.getType()).thenReturn(RecommendationType.ACADEMIC);
        when(mockStrategy2.getType()).thenReturn(RecommendationType.EVENT);

        RecommendationResponse rec1 = RecommendationResponse.builder()
                .id("item-1")
                .title("Rec 1")
                .recommendationType(RecommendationType.ACADEMIC)
                .score(0.85)
                .explanation("Ex 1")
                .build();

        when(mockStrategy1.recommend(context)).thenReturn(List.of(rec1));

        List<RecommendationResponse> results = engine.generateRecommendationsByType(context, RecommendationType.ACADEMIC);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("item-1", results.get(0).getId());

        // Verify strategy 2 (EVENT) was never invoked for ACADEMIC filter
        verify(mockStrategy2, never()).recommend(any());
        verify(mockStrategy1, times(1)).recommend(context);
    }
}
