package com.campusguide.personal.ai.atlas.decision.constraint;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constraint Engine evaluating permission, confidence, temporal, resource, and policy constraints.
 */
@Slf4j
@Component
public class ConstraintEngine {

    private final List<DecisionConstraint> customConstraints;

    public ConstraintEngine(List<DecisionConstraint> customConstraints) {
        this.customConstraints = customConstraints != null ? customConstraints : Collections.emptyList();
    }

    public Map<String, List<ConstraintViolation>> evaluateConstraints(List<DecisionCandidate> candidates, DecisionContext context) {
        Map<String, List<ConstraintViolation>> violationsMap = new ConcurrentHashMap<>();

        if (candidates == null || context == null) {
            return violationsMap;
        }

        double minConfidence = context.getConstraints() != null ? context.getConstraints().getMinConfidence() : 0.50;

        for (DecisionCandidate candidate : candidates) {
            List<ConstraintViolation> violations = new ArrayList<>();

            // 1. Confidence constraint
            if (candidate.getConfidenceScore() < minConfidence) {
                violations.add(ConstraintViolation.soft(
                        candidate.getCandidateId(),
                        "MinConfidenceConstraint",
                        ConstraintType.CONFIDENCE,
                        "Candidate confidence (" + candidate.getConfidenceScore() + ") below threshold (" + minConfidence + ")"
                ));
            }

            // 2. Resource & Feasibility constraint
            if (candidate.getFeasibilityScore() < 0.10) {
                violations.add(ConstraintViolation.hard(
                        candidate.getCandidateId(),
                        "ResourceFeasibilityConstraint",
                        ConstraintType.RESOURCE,
                        "Candidate feasibility score too low (" + candidate.getFeasibilityScore() + ")"
                ));
            }

            // 3. Custom / Pluggable constraints
            for (DecisionConstraint customConstraint : customConstraints) {
                try {
                    Optional<ConstraintViolation> violation = customConstraint.validate(candidate, context);
                    violation.ifPresent(violations::add);
                } catch (Exception e) {
                    log.error("Error evaluating constraint {} on candidate {}", 
                            customConstraint.getConstraintName(), candidate.getCandidateId(), e);
                }
            }

            violationsMap.put(candidate.getCandidateId(), violations);
        }

        return violationsMap;
    }

    public boolean hasHardViolation(String candidateId, Map<String, List<ConstraintViolation>> violationsMap) {
        List<ConstraintViolation> violations = violationsMap.get(candidateId);
        if (violations == null) return false;
        return violations.stream().anyMatch(v -> v.getSeverity() == ConstraintSeverity.HARD);
    }
}
