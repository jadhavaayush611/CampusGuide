package com.campusguide.personal.ai.atlas.execution;

import com.campusguide.personal.ai.atlas.decision.candidate.CandidateRegistry;
import com.campusguide.personal.ai.atlas.decision.candidate.DecisionCandidateGenerator;
import com.campusguide.personal.ai.atlas.decision.candidate.DirectAnswerCandidateStrategy;
import com.campusguide.personal.ai.atlas.decision.candidate.FallbackCandidateStrategy;
import com.campusguide.personal.ai.atlas.decision.constraint.ConstraintEngine;
import com.campusguide.personal.ai.atlas.decision.engine.DecisionEngine;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionEvaluator;
import com.campusguide.personal.ai.atlas.decision.evaluation.DefaultEvaluationStrategy;
import com.campusguide.personal.ai.atlas.decision.explanation.DecisionExplanationEngine;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.decision.policy.DecisionPolicyEngine;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyRegistry;
import com.campusguide.personal.ai.atlas.decision.ranking.DecisionRanker;
import com.campusguide.personal.ai.atlas.decision.ranking.DeterministicRankingStrategy;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationEngine;
import com.campusguide.personal.ai.atlas.decision.utility.UtilityCalculator;

import com.campusguide.personal.ai.atlas.execution.approval.ApprovalEngine;
import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.engine.ExecutionPreparationEngine;
import com.campusguide.personal.ai.atlas.execution.engine.ExecutionPreparationPipeline;
import com.campusguide.personal.ai.atlas.execution.explanation.ExecutionExplanationEngine;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.model.WorkflowStatus;
import com.campusguide.personal.ai.atlas.execution.resource.ResourceAnalyzer;
import com.campusguide.personal.ai.atlas.execution.risk.RiskAssessmentEngine;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlanner;
import com.campusguide.personal.ai.atlas.execution.tool.CapabilityRegistry;
import com.campusguide.personal.ai.atlas.execution.tool.ToolResolver;
import com.campusguide.personal.ai.atlas.execution.validation.*;
import com.campusguide.personal.ai.atlas.execution.workflow.ExecutableWorkflowBuilder;
import com.campusguide.personal.ai.atlas.execution.workflow.WorkflowAssembler;
import com.campusguide.personal.ai.atlas.execution.workflow.WorkflowRegistry;

import com.campusguide.personal.ai.atlas.planning.constraint.PlanningConstraintSolver;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalDecomposer;
import com.campusguide.personal.ai.atlas.planning.decomposition.GoalRegistry;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningEngine;
import com.campusguide.personal.ai.atlas.planning.engine.PlanningPipeline;
import com.campusguide.personal.ai.atlas.planning.explanation.PlanningExplanationEngine;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionPreparationEngineIntegrationIT {

    private PlanningEngine planningEngine;
    private ExecutionPreparationEngine preparationEngine;

    @BeforeEach
    void setUp() {
        // Planning Engine Setup
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
        PlanningExplanationEngine planningExplanationEngine = new PlanningExplanationEngine();

        PlanningPipeline planningPipeline = new PlanningPipeline(decomposer, constraintSolver, scheduler, optimizer, planningExplanationEngine);
        planningEngine = new PlanningEngine(planningPipeline);

        // Execution Preparation Engine Setup
        WorkflowAssembler assembler = new WorkflowAssembler();
        WorkflowRegistry workflowRegistry = new WorkflowRegistry();
        ExecutableWorkflowBuilder workflowBuilder = new ExecutableWorkflowBuilder(assembler, workflowRegistry);

        ResourceAnalyzer resourceAnalyzer = new ResourceAnalyzer();
        CapabilityRegistry capabilityRegistry = new CapabilityRegistry();
        ToolResolver toolResolver = new ToolResolver(capabilityRegistry);

        List<ExecutionValidationRule> validationRules = List.of(
                new CompletenessValidationRule(),
                new DependencySatisfactionValidationRule(),
                new PolicyComplianceValidationRule(),
                new PermissionsValidationRule(),
                new CapabilityAvailabilityValidationRule(capabilityRegistry),
                new ExecutionReadinessValidationRule()
        );
        ExecutionValidator validator = new ExecutionValidator(validationRules);

        RiskAssessmentEngine riskEngine = new RiskAssessmentEngine();
        ApprovalEngine approvalEngine = new ApprovalEngine();
        RollbackPlanner rollbackPlanner = new RollbackPlanner();
        ExecutionExplanationEngine explanationEngine = new ExecutionExplanationEngine();

        ExecutionPreparationPipeline preparationPipeline = new ExecutionPreparationPipeline(
                workflowBuilder, resourceAnalyzer, toolResolver, validator,
                riskEngine, approvalEngine, rollbackPlanner, explanationEngine
        );

        preparationEngine = new ExecutionPreparationEngine(preparationPipeline);
    }

    @Test
    @DisplayName("End-to-end pipeline: DecisionOutcome -> ExecutionPlan -> ExecutionContext -> ExecutableWorkflow")
    void testEndToEndExecutionPreparationPipeline() {
        // Step 1: Create DecisionOutcome
        DecisionOutcome outcome = DecisionOutcome.fallback("out_e2e_1", "High confidence query action for student course schedule");

        // Step 2: Transform DecisionOutcome -> ExecutionPlan via PlanningEngine
        PlanningContext planningContext = PlanningContext.fromDecisionOutcome(outcome);
        ExecutionPlan plan = planningEngine.generatePlan(planningContext);

        assertNotNull(plan, "ExecutionPlan should not be null");
        assertNotNull(plan.getPlanId(), "Plan ID should be present");
        assertFalse(plan.getTasks().isEmpty(), "Plan tasks should not be empty");

        // Step 3: Transform ExecutionPlan -> ExecutableWorkflow via ExecutionPreparationEngine
        ExecutionContext executionContext = ExecutionContext.fromExecutionPlan(plan, planningContext);
        ExecutableWorkflow workflow = preparationEngine.prepareWorkflow(executionContext);

        // Step 4: Verify ExecutableWorkflow properties and independence
        assertNotNull(workflow, "ExecutableWorkflow should not be null");
        assertNotNull(workflow.getWorkflowId(), "Workflow ID should be generated");
        assertEquals(plan.getPlanId(), workflow.getPlanId(), "Workflow planId should match ExecutionPlan planId");
        assertNotNull(workflow.getStages(), "Execution stages should not be null");
        assertFalse(workflow.getStages().isEmpty(), "Execution stages should be populated");
        assertNotNull(workflow.getCheckpoints(), "Checkpoints should not be null");
        assertNotNull(workflow.getContract(), "ExecutionContract should be generated");
        assertNotNull(workflow.getMetadata(), "ExecutionMetadata should be generated");
        assertNotNull(workflow.getExplanation(), "ExecutionExplanation should be present");
        assertNotNull(workflow.getRiskAssessment(), "ExecutionRisk should be evaluated");
        assertNotNull(workflow.getRollbackPlan(), "RollbackPlan should be generated");
        assertNotNull(workflow.getApprovalRequirement(), "ApprovalRequirement should be generated");
        assertNotNull(workflow.getResourceRequirements(), "ResourceRequirements should be analyzed");

        // Verify status
        assertTrue(workflow.getStatus() == WorkflowStatus.READY || workflow.getStatus() == WorkflowStatus.APPROVAL_REQUIRED);
    }

    @Test
    @DisplayName("Handles null ExecutionPlan gracefully with fallback ExecutableWorkflow")
    void testNullExecutionPlanFallback() {
        ExecutableWorkflow workflow = preparationEngine.prepareWorkflow((ExecutionPlan) null);
        assertNotNull(workflow, "Fallback workflow should be returned");
        assertEquals(WorkflowStatus.DEGRADED, workflow.getStatus(), "Fallback workflow should be DEGRADED");
        assertNotNull(workflow.getWorkflowId());
    }

    @Test
    @DisplayName("Prepares approval requirement for restricted mutation actions")
    void testMutationActionApprovalRequirement() {
        DecisionOutcome outcome = DecisionOutcome.fallback("out_mut_1", "Mutation candidate requiring user approval");
        ExecutionPlan plan = planningEngine.generatePlan(outcome);
        ExecutionContext context = ExecutionContext.fromExecutionPlan(plan);
        context.getConstraints().setRequireExplicitApproval(true);

        ExecutableWorkflow workflow = preparationEngine.prepareWorkflow(context);

        assertNotNull(workflow);
        assertNotNull(workflow.getApprovalRequirement());
        assertTrue(workflow.getApprovalRequirement().isApprovalRequired(), "Mutation workflow should require approval");
        assertEquals(WorkflowStatus.APPROVAL_REQUIRED, workflow.getStatus(), "Workflow status should be APPROVAL_REQUIRED");
    }
}
