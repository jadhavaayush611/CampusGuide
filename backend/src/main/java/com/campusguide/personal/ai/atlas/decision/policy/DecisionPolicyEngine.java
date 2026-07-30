package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Engine for evaluating policy rules against candidate decisions.
 */
@Slf4j
@Component
public class DecisionPolicyEngine {

    private final PolicyRegistry policyRegistry;

    public DecisionPolicyEngine(PolicyRegistry policyRegistry) {
        this.policyRegistry = policyRegistry;
    }

    public PolicyComplianceResult evaluatePolicies(List<DecisionCandidate> candidates, DecisionContext context) {
        List<PolicyRule> activeRules = policyRegistry.getActiveRules();
        List<String> appliedPolicyNames = new ArrayList<>();
        for (PolicyRule rule : activeRules) {
            appliedPolicyNames.add(rule.getRuleName());
        }

        Map<String, List<PolicyEvaluationResult>> candidateEvaluations = new ConcurrentHashMap<>();
        boolean overallCompliant = true;

        if (candidates != null) {
            for (DecisionCandidate candidate : candidates) {
                List<PolicyEvaluationResult> evalResults = new ArrayList<>();
                for (PolicyRule rule : activeRules) {
                    try {
                        PolicyEvaluationResult eval = rule.evaluate(candidate, context);
                        evalResults.add(eval);
                        if (eval.getStatus() == PolicyEvaluationResult.Status.DENIED) {
                            overallCompliant = false;
                        }
                    } catch (Exception e) {
                        log.error("Failed to evaluate policy rule {} on candidate {}", rule.getRuleId(), candidate.getCandidateId(), e);
                        evalResults.add(PolicyEvaluationResult.deny(rule.getRuleId(), rule.getRuleName(), "Evaluation exception: " + e.getMessage()));
                        overallCompliant = false;
                    }
                }
                candidateEvaluations.put(candidate.getCandidateId(), evalResults);
            }
        }

        log.debug("Evaluated {} policies across {} candidates. Overall compliant: {}", activeRules.size(), 
                candidates != null ? candidates.size() : 0, overallCompliant);

        return PolicyComplianceResult.builder()
                .fullyCompliant(overallCompliant)
                .appliedPolicies(appliedPolicyNames)
                .candidateEvaluations(candidateEvaluations)
                .build();
    }
}
