package com.campusguide.personal.ai.atlas.context.query;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Deterministic, provider-independent, rule-based intent detector for Atlas.
 */
@Component
public class IntentDetector {

    public IntentResult detectIntent(String query, List<ExtractedEntity> entities) {
        if (query == null || query.isBlank()) {
            return new IntentResult(QueryIntent.GENERAL_CONVERSATION, QueryDomain.GENERAL, 0.50);
        }

        String lower = query.toLowerCase(Locale.ROOT);

        // Count category keyword matches
        int academicMatches = countMatches(lower, "grade", "gpa", "course", "assignment", "homework", "exam", "syllabus", "professor", "transcript", "credit", "lecture", "major", "department", "degree", "quiz");
        int plannerMatches = countMatches(lower, "task", "todo", "deadline", "priority", "project", "list", "pending", "finish", "due", "focus");
        int calendarMatches = countMatches(lower, "schedule", "calendar", "event", "meeting", "class time", "timings", "appointment", "free time", "busy", "slot", "when is", "today", "tomorrow");
        int campusMatches = countMatches(lower, "where is", "location", "building", "library", "cafeteria", "canteen", "map", "direction", "hall", "lab", "dorm", "hostel", "auditorium", "parking", "gym", "navigate", "how to get to");
        int userMatches = countMatches(lower, "profile", "my name", "who am i", "my info", "my email", "my role", "student id", "user info", "student name");
        int generalMatches = countMatches(lower, "hi", "hello", "hey", "thanks", "thank you", "who are you", "what can you do");

        // Entity boost
        if (entities != null) {
            for (ExtractedEntity entity : entities) {
                if (entity.getType() == EntityType.CAMPUS_LOCATION) campusMatches += 2;
                if (entity.getType() == EntityType.ACADEMIC_CONCEPT) academicMatches += 2;
                if (entity.getType() == EntityType.PLANNER_ITEM) plannerMatches += 2;
            }
        }

        // Determine max category
        int max = Math.max(academicMatches, Math.max(plannerMatches, Math.max(calendarMatches, Math.max(campusMatches, Math.max(userMatches, generalMatches)))));

        if (max == 0) {
            return new IntentResult(QueryIntent.UNKNOWN, QueryDomain.GENERAL, 0.40);
        }

        if (max == academicMatches) {
            double confidence = Math.min(0.95, 0.70 + (0.10 * academicMatches));
            return new IntentResult(QueryIntent.ACADEMIC_INQUIRY, QueryDomain.ACADEMIC, confidence);
        } else if (max == plannerMatches) {
            double confidence = Math.min(0.95, 0.70 + (0.10 * plannerMatches));
            return new IntentResult(QueryIntent.PLANNER_LOOKUP, QueryDomain.PLANNER, confidence);
        } else if (max == calendarMatches) {
            double confidence = Math.min(0.95, 0.70 + (0.10 * calendarMatches));
            return new IntentResult(QueryIntent.CALENDAR_EVENT, QueryDomain.CALENDAR, confidence);
        } else if (max == campusMatches) {
            double confidence = Math.min(0.95, 0.70 + (0.10 * campusMatches));
            return new IntentResult(QueryIntent.CAMPUS_NAVIGATION, QueryDomain.CAMPUS, confidence);
        } else if (max == userMatches) {
            double confidence = Math.min(0.95, 0.75 + (0.10 * userMatches));
            return new IntentResult(QueryIntent.USER_PROFILE, QueryDomain.USER, confidence);
        } else {
            double confidence = Math.min(0.90, 0.65 + (0.10 * generalMatches));
            return new IntentResult(QueryIntent.GENERAL_CONVERSATION, QueryDomain.GENERAL, confidence);
        }
    }

    private int countMatches(String text, String... keywords) {
        int count = 0;
        for (String kw : keywords) {
            if (text.contains(kw)) {
                count++;
            }
        }
        return count;
    }

    public record IntentResult(QueryIntent intent, QueryDomain domain, double confidence) {}
}
