package com.campusguide.personal.ai.atlas.context.query;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Standardizes abbreviations, aliases, and conversational phrasing in user queries.
 */
@Component
public class QueryNormalizer {

    private final Map<String, String> ABBREVIATION_MAP;

    public QueryNormalizer() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("prof", "professor");
        map.put("profs", "professors");
        map.put("assign", "assignment");
        map.put("assigns", "assignments");
        map.put("hw", "homework");
        map.put("lib", "library");
        map.put("sched", "schedule");
        map.put("sub", "submission");
        map.put("subs", "submissions");
        map.put("dept", "department");
        map.put("calc", "calculus");
        map.put("cs", "computer science");
        map.put("cafe", "cafeteria");
        map.put("canteen", "cafeteria");
        map.put("gpa", "grade point average");
        map.put("dorm", "dormitory");
        map.put("req", "requirement");
        map.put("reqs", "requirements");
        map.put("exam", "examination");
        map.put("exams", "examinations");
        ABBREVIATION_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Normalizes an incoming raw user query.
     *
     * @param rawQuery input text
     * @return standardized, normalized string
     */
    public String normalize(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }

        // Clean extra spaces & basic punctuation trim
        String text = rawQuery.trim().replaceAll("\\s+", " ");

        // Split words and replace exact abbreviations preserving context
        String[] tokens = text.split(" ");
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String cleanedToken = token.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

            if (ABBREVIATION_MAP.containsKey(cleanedToken)) {
                String replacement = ABBREVIATION_MAP.get(cleanedToken);
                // Keep trailing punctuation if any (like ? or !)
                String trailing = token.replaceAll("^[a-zA-Z0-9]+", "");
                normalized.append(replacement).append(trailing);
            } else {
                normalized.append(token);
            }

            if (i < tokens.length - 1) {
                normalized.append(" ");
            }
        }

        return normalized.toString();
    }
}
