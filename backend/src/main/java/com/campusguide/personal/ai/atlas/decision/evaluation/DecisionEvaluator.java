package com.campusguide.personal.ai.atlas.decision.evaluation;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component for evaluating candidate decisions deterministically.
 */
@Slf4j
@Component
public class DecisionEvaluator {

    private final EvaluationStrategy evaluationStrategy;

    public DecisionEvaluator(EvaluationStrategy evaluationStrategy) {
        this.evaluationStrategy = evaluationStrategy;
    }

    public Map<String, DecisionScore> evaluateCandidates(List<DecisionCandidate> candidates, 
                                                         DecisionContext context, 
                                                         PolicyComplianceResult policyCompliance) {
        Map<String, DecisionScore> scoreMap = new ConcurrentHashMap<>();

        if (candidates != null) {
            for (DecisionCandidate candidate : candidates) {
                try {
                    DecisionScore score = evaluationStrategy.evaluateCandidate(candidate, context, policyCompliance);
                    scoreMap.put(candidate.getCandidateId(), score);
                } catch (Exception e) {
                    log.error("Failed to evaluate candidate {}", candidate.getCandidateId(), e);
                    scoreMap.put(candidate.getCandidateId(), DecisionScore.zero(candidate.getCandidateId()));
                }
            }
        }

        log.debug("Evaluated {} candidates using strategy {}", scoreMap.size(), evaluationStrategy.getStrategyName());
        return scoreMap;
    }
}
