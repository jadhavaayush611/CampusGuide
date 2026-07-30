package com.campusguide.personal.ai.atlas.planning;

import com.campusguide.personal.ai.atlas.decision.candidate.CandidateRegistry;
import com.campusguide.personal.ai.atlas.decision.candidate.DecisionCandidateGenerator;
import com.campusguide.personal.ai.atlas.decision.candidate.DirectAnswerCandidateStrategy;
import com.campusguide.personal.ai.atlas.decision.candidate.FallbackCandidateStrategy;
import com.campusguide.personal.ai.atlas.decision.constraint.ConstraintEngine;
import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.engine.DecisionEngine;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionEvaluator;
import com.campusguide.personal.ai.atlas.decision.evaluation.DefaultEvaluationStrategy;
import com.campusguide.personal.ai.atlas.decision.explanation.DecisionExplanationEngine;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.decision.policy.DecisionPolicyEngine;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyRegistry;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanker;
import com.campusguide.personal.ai.atlas.decision.ranking.DeterministicRankingStrategy;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationEngine;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityCalculator;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.GraphContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.context.PermissionContext;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import com.campusguide.personal.ai.atlas.planning.constraint.PlanningConstraintSolver;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalDecomposer;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalRegistry;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningEngine;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningPipeline;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningStrategy;
import com.campusguide.personal.ai.atlas.planning.explanation.PlanningExplanationEngine;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import com.campusguide.personal.ai.atlas.planning.model.PlanStatus;
import com.campusguide.personal.ai.atlas.planning.optimization.PlanOptimizer;
import com.campusguide.personal.ai.atlas.planning.optimization.strategy.*;
import com.campusguide.personal.ai.atlas.planning.scheduling.Scheduler;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.DeadlineAwareSchedulingStrategy;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.EarliestCompletionSchedulingStrategy;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.PreferenceAwareSchedulingStrategy;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.PriorityAwareSchedulingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PlanningEngineIntegrationIT {

    private DecisionEngine decisionEngine;
    private PlanningEngine planningEngine;

    @BeforeEach
    void setUp() {
        // Build Decision Engine components
        CandidateRegistry candidateRegistry = new CandidateRegistry(Arrays.asList(
                new DirectAnswerCandidateStrategy(),
                new FallbackCandidateStrategy()
        ));
        DecisionCandidateGenerator candidateGenerator = new DecisionCandidateGenerator(candidateRegistry);
        DecisionPolicyEngine policyEngine = new DecisionPolicyEngine(new PolicyRegistry(Collections.emptyList()));
        ConstraintEngine constraintEngine = new ConstraintEngine(Collections.emptyList());
        DecisionEvaluator evaluator = new DecisionEvaluator(new DefaultEvaluationStrategy());
        UtilityCalculator utilityCalculator = new UtilityCalculator();
        DecisionRanker ranker = new DecisionRanker(new DeterministicRankingStrategy());
        RecommendationEngine recommendationEngine = new RecommendationEngine();
        DecisionExplanationEngine decExplanationEngine = new DecisionExplanationEngine();

        decisionEngine = new DecisionEngine(
                candidateGenerator, policyEngine, constraintEngine, evaluator, utilityCalculator,
                ranker, recommendationEngine, decExplanationEngine
        );

        // Build Planning Engine components
        GoalDecomposer decomposer = new GoalDecomposer(new GoalRegistry());
        PlanningConstraintSolver constraintSolver = new PlanningConstraintSolver();
        Scheduler scheduler = new Scheduler(Arrays.asList(
                new EarliestCompletionSchedulingStrategy(),
                new DeadlineAwareSchedulingStrategy(),
                new PriorityAwareSchedulingStrategy(),
                new PreferenceAwareSchedulingStrategy()
        ));
        PlanOptimizer optimizer = new PlanOptimizer(Arrays.asList(
                new CompletionTimeOptimizationStrategy(),
                new DependencyReductionOptimizationStrategy(),
                new ResourceUtilizationOptimizationStrategy(),
                new UserConvenienceOptimizationStrategy(),
                new PlanSimplicityOptimizationStrategy()
        ));
        PlanningExplanationEngine explanationEngine = new PlanningExplanationEngine();

        PlanningPipeline pipeline = new PlanningPipeline(decomposer, constraintSolver, scheduler, optimizer, explanationEngine);
        planningEngine = new PlanningEngine(pipeline);
    }

    @Test
    @DisplayName("End-to-End DecisionOutcome -> ExecutionPlan transformation pipeline")
    void testEndToEndDecisionToPlanningFlow() {
        // 1. Prepare GraphContext and ReasoningEvidence
        PermissionContext perm = PermissionContext.builder().userId("student_123").build();
        GraphContext graphContext = GraphContext.builder()
                .permissionContext(perm)
                .confidenceThreshold(0.70)
                .build();

        ReasoningEvidence evidence = ReasoningEvidence.builder()
                .evidenceId("ev_it_01")
                .objectiveDescription("Locate Science Building and find open study room")
                .reasoningSummaryText("Reasoning path confirms Science Hall Room 302 is available")
                .confidence(0.88)
                .build();

        // 2. Execute DecisionEngine -> DecisionOutcome
        DecisionOutcome decisionOutcome = decisionEngine.makeDecision(graphContext, evidence);
        assertThat(decisionOutcome).isNotNull();

        // 3. Execute PlanningEngine -> ExecutionPlan
        ExecutionPlan plan = planningEngine.generatePlan(decisionOutcome);

        // 4. Assert ExecutionPlan invariants
        assertThat(plan).isNotNull();
        assertThat(plan.getPlanId()).startsWith("plan_");
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.READY);
        assertThat(plan.getGoal()).isNotNull();
        assertThat(plan.getTasks()).isNotEmpty();
        assertThat(plan.getSchedule()).isNotNull();
        assertThat(plan.getExplanation()).isNotNull();
        assertThat(plan.getMetrics()).isNotNull();
        assertThat(plan.getMetrics().getPlanningLatencyMs()).isGreaterThanOrEqualTo(0);
        assertThat(plan.getMetrics().getTotalTasks()).isEqualTo(plan.getTasks().size());
        assertThat(plan.getExplanation().getPrimaryRationale()).isNotEmpty();
    }

    @Test
    @DisplayName("End-to-End ExecutionPlan with thorough planning strategy")
    void testThoroughPlanningStrategy() {
        DecisionOutcome outcome = DecisionOutcome.fallback("out_thorough", "Thorough strategy test");
        ExecutionPlan plan = planningEngine.generatePlan(
                com.campusguide.personal.ai.atlas.planning.context.PlanningContext.fromDecisionOutcome(outcome),
                PlanningStrategy.thoroughStrategy()
        );

        assertThat(plan).isNotNull();
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.READY);
        assertThat(plan.getExplanation()).isNotNull();
    }
}
