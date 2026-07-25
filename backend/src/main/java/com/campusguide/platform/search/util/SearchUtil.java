package com.campusguide.platform.search.util;

import java.util.List;

public class SearchUtil {

    private SearchUtil() {
        // Prevent instantiation
    }

    public static String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim();
    }

    public static double calculateRelevanceScore(String query, String title, String description, List<String> tags) {
        if (query == null || query.trim().isEmpty()) {
            return 0.0;
        }
        String normQuery = query.trim().toLowerCase();
        double score = 0.0;

        // Exact title match -> 1.0
        if (title != null && title.trim().equalsIgnoreCase(normQuery)) {
            score = Math.max(score, 1.0);
        }

        // Title contains query -> 0.9
        if (title != null && title.toLowerCase().contains(normQuery)) {
            score = Math.max(score, 0.9);
        }

        // Description contains query -> 0.7
        if (description != null && description.toLowerCase().contains(normQuery)) {
            score = Math.max(score, 0.7);
        }

        // Tag match -> 0.6
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null) {
                    String normTag = tag.trim().toLowerCase();
                    if (normTag.equals(normQuery) || normTag.contains(normQuery)) {
                        score = Math.max(score, 0.6);
                    }
                }
            }
        }

        return score;
    }
}
