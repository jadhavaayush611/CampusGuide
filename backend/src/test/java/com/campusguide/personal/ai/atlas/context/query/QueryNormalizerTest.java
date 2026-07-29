package com.campusguide.personal.ai.atlas.context.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryNormalizerTest {

    private QueryNormalizer queryNormalizer;

    @BeforeEach
    void setUp() {
        queryNormalizer = new QueryNormalizer();
    }

    @Test
    @DisplayName("Should normalize common academic abbreviations")
    void testNormalize_AcademicAbbreviations() {
        String input = "Where is prof Smith for cs assign 1?";
        String result = queryNormalizer.normalize(input);

        assertTrue(result.contains("professor"));
        assertTrue(result.contains("computer science"));
        assertTrue(result.contains("assignment"));
    }

    @Test
    @DisplayName("Should normalize campus locations and terms")
    void testNormalize_CampusTerms() {
        String input = "Is the lib or cafe open near dorm?";
        String result = queryNormalizer.normalize(input);

        assertTrue(result.contains("library"));
        assertTrue(result.contains("cafeteria"));
        assertTrue(result.contains("dormitory"));
    }

    @Test
    @DisplayName("Should handle null and empty queries gracefully")
    void testNormalize_NullAndEmpty() {
        assertEquals("", queryNormalizer.normalize(null));
        assertEquals("", queryNormalizer.normalize("   "));
    }
}
