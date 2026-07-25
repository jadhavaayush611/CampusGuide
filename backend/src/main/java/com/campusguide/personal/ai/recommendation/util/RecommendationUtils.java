package com.campusguide.personal.ai.recommendation.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class RecommendationUtils {

    private RecommendationUtils() {
        // Prevent instantiation
    }

    /**
     * Rounds a double score to 2 decimal places.
     *
     * @param score raw double score
     * @return rounded score in range 0.0 - 1.0
     */
    public static double roundScore(double score) {
        double clamped = Math.max(0.0, Math.min(1.0, score));
        return BigDecimal.valueOf(clamped)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
