package com.campusguide.personal.ai.atlas.context.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class QueryAnalyzerTest {

    private QueryAnalyzer queryAnalyzer;
    private LocalDateTime referenceTime;

    @BeforeEach
    void setUp() {
        QueryNormalizer normalizer = new QueryNormalizer();
        TemporalExpressionResolver temporalResolver = new TemporalExpressionResolver();
        EntityExtractor extractor = new EntityExtractor();
        IntentDetector intentDetector = new IntentDetector();

        queryAnalyzer = new QueryAnalyzer(normalizer, temporalResolver, extractor, intentDetector);
        referenceTime = LocalDateTime.of(2026, 7, 29, 10, 0);
    }

    @Test
    @DisplayName("Should produce a complete QueryContext for an academic query with temporal expression")
    void testAnalyze_AcademicQuery() {
        String query = "Where is prof Smith for cs assign due tomorrow?";
        QueryContext qc = queryAnalyzer.analyze(query, referenceTime);

        assertNotNull(qc);
        assertEquals(query, qc.getRawQuery());
        assertTrue(qc.getNormalizedQuery().contains("professor"));
        assertTrue(qc.getNormalizedQuery().contains("assignment"));
        assertEquals(QueryIntent.ACADEMIC_INQUIRY, qc.getIntent());
        assertEquals(QueryDomain.ACADEMIC, qc.getDomainClassification());

        assertNotNull(qc.getTemporalInformation());
        assertTrue(qc.getTemporalInformation().isResolved());
        assertEquals("tomorrow", qc.getTemporalInformation().getRawExpression());

        assertFalse(qc.getEntities().isEmpty());
        assertTrue(qc.getRetrievalHints().contains("FETCH_ACADEMIC_SUMMARY"));
        assertTrue(qc.getConfidenceScore() >= 0.70);
    }

    @Test
    @DisplayName("Should handle empty query gracefully")
    void testAnalyze_EmptyQuery() {
        QueryContext qc = queryAnalyzer.analyze("", referenceTime);

        assertNotNull(qc);
        assertEquals(QueryIntent.GENERAL_CONVERSATION, qc.getIntent());
        assertEquals(QueryDomain.GENERAL, qc.getDomainClassification());
        assertEquals(0.50, qc.getConfidenceScore());
    }
}
