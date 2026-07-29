package com.campusguide.personal.ai.atlas.context.query;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Extracts entities from user queries supporting campus-specific entities,
 * academic concepts, planner items, dates/times, and aliases.
 */
@Component
public class EntityExtractor {

    private static final List<String> CAMPUS_LOCATIONS = List.of(
            "library", "cafeteria", "student center", "science hall", "engineering block",
            "dorms", "dormitory", "gym", "parking", "auditorium", "lab", "main gate",
            "admin building", "hostel", "sports complex"
    );

    private static final List<String> ACADEMIC_CONCEPTS = List.of(
            "gpa", "grade", "course", "assignment", "homework", "exam", "quiz",
            "syllabus", "transcript", "registration", "major", "credit", "lecture",
            "professor", "department", "calculus", "physics", "computer science",
            "midterm", "final exam", "degree"
    );

    private static final List<String> PLANNER_ITEMS = List.of(
            "task", "todo", "deadline", "priority", "project", "list", "reminder",
            "pending", "submission", "due date", "kanban"
    );

    private static final List<String> ALIASES = List.of(
            "prof", "hw", "lib", "sched", "sub", "dept", "calc", "cs", "cafe", "canteen"
    );

    public List<ExtractedEntity> extractEntities(String text) {
        List<ExtractedEntity> entities = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return entities;
        }

        String lower = text.toLowerCase(Locale.ROOT);

        // 1. Campus Location Entities
        for (String location : CAMPUS_LOCATIONS) {
            int idx = lower.indexOf(location);
            if (idx != -1) {
                entities.add(ExtractedEntity.builder()
                        .name(location)
                        .type(EntityType.CAMPUS_LOCATION)
                        .normalizedName(location)
                        .confidence(0.95)
                        .startPosition(idx)
                        .endPosition(idx + location.length())
                        .build());
            }
        }

        // 2. Academic Concept Entities
        for (String concept : ACADEMIC_CONCEPTS) {
            int idx = lower.indexOf(concept);
            if (idx != -1) {
                entities.add(ExtractedEntity.builder()
                        .name(concept)
                        .type(EntityType.ACADEMIC_CONCEPT)
                        .normalizedName(concept)
                        .confidence(0.90)
                        .startPosition(idx)
                        .endPosition(idx + concept.length())
                        .build());
            }
        }

        // 3. Planner Item Entities
        for (String item : PLANNER_ITEMS) {
            int idx = lower.indexOf(item);
            if (idx != -1) {
                entities.add(ExtractedEntity.builder()
                        .name(item)
                        .type(EntityType.PLANNER_ITEM)
                        .normalizedName(item)
                        .confidence(0.88)
                        .startPosition(idx)
                        .endPosition(idx + item.length())
                        .build());
            }
        }

        // 4. Aliases
        for (String alias : ALIASES) {
            int idx = lower.indexOf(alias);
            if (idx != -1) {
                entities.add(ExtractedEntity.builder()
                        .name(alias)
                        .type(EntityType.ALIAS)
                        .normalizedName(alias)
                        .confidence(0.85)
                        .startPosition(idx)
                        .endPosition(idx + alias.length())
                        .build());
            }
        }

        return entities;
    }
}
