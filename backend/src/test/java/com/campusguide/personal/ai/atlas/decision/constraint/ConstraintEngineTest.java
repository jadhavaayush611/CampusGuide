package com.campusguide.personal.ai.atlas.decision.constraint;

import com.campusguide.personal.ai.atlas.decision.context.DecisionConstraints;
import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintEngineTest {

    private ConstraintEngine constraintEngine;

    @BeforeEach
    void setUp() {
        constraintEngine = new ConstraintEngine(Collections.emptyList());
    }

    @Test
    @DisplayName("ConstraintEngine detects soft confidence violation when candidate confidence is below threshold")
    void testConstraintEngineSoftConfidenceViolation() {
        DecisionCandidate lowConfCand = DecisionCandidate.simple("cand_low_conf", "DIRECT_ANSWER", "Low confidence", 0.30);

        DecisionContext context = DecisionContext.builder()
                .constraints(DecisionConstraints.builder().minConfidence(0.60).build())
                .build();

        Map<String, List<ConstraintViolation>> violationsMap = constraintEngine.evaluateConstraints(List.of(lowConfCand), context);

        assertTrue(violationsMap.containsKey(lowConfCand.getCandidateId()));
        List<ConstraintViolation> violations = violationsMap.get(lowConfCand.getCandidateId());
        assertEquals(1, violations.size());
        assertEquals(ConstraintSeverity.SOFT, violations.get(0).getSeverity());
        assertEquals(ConstraintType.CONFIDENCE, violations.get(0).getConstraintType());
        assertFalse(constraintEngine.hasHardViolation(lowConfCand.getCandidateId(), violationsMap));
    }

    @Test
    @DisplayName("ConstraintEngine detects hard resource/feasibility violation when feasibility score is zero")
    void testConstraintEngineHardResourceViolation() {
        DecisionCandidate unfeasibleCand = DecisionCandidate.builder()
                .candidateId("cand_unfeasible")
                .actionType("ACTION")
                .confidenceScore(0.90)
                .feasibilityScore(0.0)
                .build();

        DecisionContext context = DecisionContext.builder().build();

        Map<String, List<ConstraintViolation>> violationsMap = constraintEngine.evaluateConstraints(List.of(unfeasibleCand), context);

        assertTrue(constraintEngine.hasHardViolation(unfeasibleCand.getCandidateId(), violationsMap));
    }
}
