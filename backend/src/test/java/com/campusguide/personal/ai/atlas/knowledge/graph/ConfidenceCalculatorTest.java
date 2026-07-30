package com.campusguide.personal.ai.atlas.knowledge.graph;

import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ConfidenceCalculator;
import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ConfidenceFactors;
import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ReasoningConfidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfidenceCalculatorTest {

    @Test
    @DisplayName("ConfidenceCalculator computes deterministic confidence score and level")
    void testConfidenceCalculation() {
        ConfidenceCalculator calculator = new ConfidenceCalculator();
        ConfidenceFactors factors = ConfidenceFactors.builder()
                .relationshipStrength(0.9)
                .retrievalConfidence(0.85)
                .evidenceQuality(0.95)
                .traversalDepth(1)
                .inferenceConfidence(0.90)
                .build();

        ReasoningConfidence confidence = calculator.calculate(factors);

        assertNotNull(confidence);
        assertTrue(confidence.getOverallScore() >= 0.8);
        assertEquals(ReasoningConfidence.ConfidenceLevel.HIGH, confidence.getLevel());
        assertNotNull(confidence.getExplanation());
    }

    @Test
    @DisplayName("Confidence applies depth penalty for deeper traversal paths")
    void testDepthPenalty() {
        ConfidenceCalculator calculator = new ConfidenceCalculator();
        ConfidenceFactors factorsDepth1 = ConfidenceFactors.builder().traversalDepth(1).build();
        ConfidenceFactors factorsDepth5 = ConfidenceFactors.builder().traversalDepth(5).build();

        ReasoningConfidence conf1 = calculator.calculate(factorsDepth1);
        ReasoningConfidence conf5 = calculator.calculate(factorsDepth5);

        assertTrue(conf1.getOverallScore() > conf5.getOverallScore());
    }
}
