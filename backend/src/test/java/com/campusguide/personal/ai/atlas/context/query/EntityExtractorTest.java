package com.campusguide.personal.ai.atlas.context.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityExtractorTest {

    private EntityExtractor entityExtractor;

    @BeforeEach
    void setUp() {
        entityExtractor = new EntityExtractor();
    }

    @Test
    @DisplayName("Should extract campus location entities")
    void testExtract_CampusLocation() {
        List<ExtractedEntity> entities = entityExtractor.extractEntities("Where is the science hall library?");

        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.CAMPUS_LOCATION && "science hall".equals(e.getName())));
        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.CAMPUS_LOCATION && "library".equals(e.getName())));
    }

    @Test
    @DisplayName("Should extract academic concepts and planner items")
    void testExtract_AcademicAndPlanner() {
        List<ExtractedEntity> entities = entityExtractor.extractEntities("Check my gpa and physics assignment deadline task");

        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.ACADEMIC_CONCEPT && "gpa".equals(e.getName())));
        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.ACADEMIC_CONCEPT && "assignment".equals(e.getName())));
        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.PLANNER_ITEM && "deadline".equals(e.getName())));
        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.PLANNER_ITEM && "task".equals(e.getName())));
    }

    @Test
    @DisplayName("Should extract aliases correctly")
    void testExtract_Aliases() {
        List<ExtractedEntity> entities = entityExtractor.extractEntities("Ask prof about hw at lib");

        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.ALIAS && "prof".equals(e.getName())));
        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.ALIAS && "hw".equals(e.getName())));
        assertTrue(entities.stream().anyMatch(e -> e.getType() == EntityType.ALIAS && "lib".equals(e.getName())));
    }
}
