package com.campusguide.personal.ai.atlas.decision.engine;

import com.campusguide.personal.ai.atlas.decision.candidate.DecisionCandidateGenerator;
import com.campusguide.personal.ai.atlas.decision.constraint.ConstraintEngine;
import com.campusguide.personal.ai.atlas.decision.constraint.ConstraintViolation;
import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionEvaluator;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.explanation.DecisionExplanation;
import com.campusguide.personal.ai.atlas.decision.explanation.DecisionExplanationEngine;
import com.campusguide.personal.ai.atlas.decision.metrics.DecisionMetrics;
import com.campusguide.personal.ai.atlas.decision.model.*;
import com.campusguide.personal.ai.atlas.decision.policy.DecisionPolicyEngine;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanker;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanking;
import com.campusguide.personal.ai.atlas.decision.recommendation.Recommendation;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationBundle;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationEngine;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityCalculator;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityScore;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider-independent Decision Intelligence Engine.
 * Orchestrates Candidate Generation -> Policy Enforcement -> Constraint Verification ->
 * Utility Evaluation -> Candidate Ranking -> Recommendation Engine -> Decision Explanation.
 */
@Slf4j
@Service
public class DecisionEngine {

    private final DecisionCandidateGenerator candidateGenerator;
    private final DecisionPolicyEngine policyEngine;
    private final ConstraintEngine constraintEngine;
    private final DecisionEvaluator evaluator;
    private final UtilityCalculator utilityCalculator;
    private final DecisionRanker ranker;
    private final RecommendationEngine recommendationEngine;
    private final DecisionExplanationEngine explanationEngine;

    public DecisionEngine(DecisionCandidateGenerator candidateGenerator,
                          DecisionPolicyEngine policyEngine,
                          ConstraintEngine constraintEngine,
                          DecisionEvaluator evaluator,
                          UtilityCalculator utilityCalculator,
                          DecisionRanker ranker,
                          @org.springframework.beans.factory.annotation.Qualifier("atlasDecisionRecommendationEngine") RecommendationEngine recommendationEngine,
                          DecisionExplanationEngine explanationEngine) {
        this.candidateGenerator = candidateGenerator;
        this.policyEngine = policyEngine;
        this.constraintEngine = constraintEngine;
        this.evaluator = evaluator;
        this.utilityCalculator = utilityCalculator;
        this.ranker = ranker;
        this.recommendationEngine = recommendationEngine;
        this.explanationEngine = explanationEngine;
    }

    /**
     * Helper entry point transforming GraphContext and ReasoningEvidence into DecisionOutcome.
     */
    public DecisionOutcome makeDecision(GraphContext graphContext, ReasoningEvidence evidence) {
        DecisionContext context = DecisionContext.fromReasoning(graphContext, evidence);
        return makeDecision(context);
    }

    /**
     * Primary entry point executing full decision pipeline on DecisionContext.
     */
    public DecisionOutcome makeDecision(DecisionContext context) {
        long startTime = System.currentTimeMillis();
        String outcomeId = "out_" + UUID.randomUUID().toString().substring(0, 8);

        if (context == null) {
            log.warn("Null DecisionContext passed to DecisionEngine");
            return DecisionOutcome.fallback(outcomeId, "Null DecisionContext provided");
        }

        try {
            // 1. Candidate Generation
            List<DecisionCandidate> candidates = candidateGenerator.generateCandidates(context);

            // 2. Policy Enforcement
            PolicyComplianceResult policyCompliance = policyEngine.evaluatePolicies(candidates, context);

            // 3. Constraint Verification
            Map<String, List<ConstraintViolation>> constraintViolations = constraintEngine.evaluateConstraints(candidates, context);

            // 4. Candidate Evaluation
            Map<String, DecisionScore> scores = evaluator.evaluateCandidates(candidates, context, policyCompliance);

            // 5. Utility Calculation
            Map<String, UtilityScore> utilities = new ConcurrentHashMap<>();
            for (DecisionCandidate cand : candidates) {
                DecisionScore score = scores.get(cand.getCandidateId());
                UtilityScore uScore = utilityCalculator.calculateUtility(cand, context, score);
                utilities.put(cand.getCandidateId(), uScore);
            }

            // 6. Decision Ranking
            DecisionRanking ranking = ranker.rankCandidates(candidates, scores, utilities);

            // 7. Recommendation Engine
            RecommendationBundle bundle = recommendationEngine.buildBundle(ranking, policyCompliance, utilities);

            // 8. Explainability
            DecisionExplanation explanation = explanationEngine.generateExplanation(context, bundle, policyCompliance, scores, utilities);

            // 9. Build Decision object
            Recommendation primaryRec = bundle.getPrimaryRecommendation();
            DecisionCandidate selectedAction = primaryRec != null ? primaryRec.getCandidate() : null;
            DecisionStatus status = selectedAction != null ? DecisionStatus.APPROVED : DecisionStatus.REJECTED;

            Decision decision = Decision.builder()
                    .decisionId("dec_" + outcomeId)
                    .objective(context.getObjective())
                    .candidates(candidates)
                    .selectedCandidate(selectedAction)
                    .confidence(selectedAction != null ? selectedAction.getConfidenceScore() : 0.0)
                    .reasoningEvidence(context.getReasoningEvidence())
                    .rationale(explanation.getPrimaryRationale())
                    .policyCompliance(policyCompliance)
                    .metadata(DecisionMetadata.createDefault(context.getRequestMetadata() != null ? context.getRequestMetadata().getTraceId() : null))
                    .createdAt(Instant.now())
                    .evaluatedAt(Instant.now())
                    .build();

            // 10. Compute Metrics (Observability without sensitive data)
            long latency = System.currentTimeMillis() - startTime;
            Map<String, Integer> candidatesByActionType = new HashMap<>();
            for (DecisionCandidate c : candidates) {
                candidatesByActionType.merge(c.getActionType(), 1, Integer::sum);
            }

            DecisionMetrics metrics = DecisionMetrics.builder()
                    .decisionLatencyMs(latency)
                    .totalCandidatesGenerated(candidates.size())
                    .totalPoliciesEvaluated(policyCompliance.getAppliedPolicies().size())
                    .rejectedCandidateCount(bundle.getRejectedCandidates().size())
                    .selectedCandidateUtility(primaryRec != null ? primaryRec.getUtility() : 0.0)
                    .selectedCandidateConfidence(selectedAction != null ? selectedAction.getConfidenceScore() : 0.0)
                    .candidatesByActionType(candidatesByActionType)
                    .rejectionsByPolicy(new HashMap<>())
                    .build();

            Map<String, Object> executionHints = new HashMap<>();
            if (primaryRec != null && primaryRec.getExecutionPayload() != null) {
                executionHints.putAll(primaryRec.getExecutionPayload());
            }

            return DecisionOutcome.builder()
                    .outcomeId(outcomeId)
                    .decision(decision)
                    .status(status)
                    .selectedAction(selectedAction)
                    .executionHints(executionHints)
                    .recommendationBundle(bundle)
                    .explanation(explanation)
                    .metrics(metrics)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Unhandled exception during DecisionEngine execution for contextId={}", context.getContextId(), e);
            return DecisionOutcome.fallback(outcomeId, "Error during decision evaluation: " + e.getMessage());
        }
    }
}
