package com.campusguide.personal.ai.atlas.planning.decomposition;

import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.model.GoalState;
import com.campusguide.personal.ai.atlas.planning.model.PlanningGoal;
import com.campusguide.personal.ai.atlas.planning.model.SubGoal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service decomposing PlanningContext into recursive hierarchical goal structures.
 */
@Slf4j
@Component("planningGoalDecomposer")
public class GoalDecomposer {

    private final GoalRegistry goalRegistry;

    public GoalDecomposer(GoalRegistry goalRegistry) {
        this.goalRegistry = goalRegistry;
    }

    public GoalHierarchy decompose(PlanningContext context) {
        if (context == null) {
            log.warn("Null PlanningContext provided for decomposition");
            return createFallbackHierarchy("Null context");
        }

        DecisionOutcome outcome = context.getDecisionOutcome();
        DecisionCandidate candidate = outcome != null ? outcome.getSelectedAction() : null;
        String actionType = candidate != null ? candidate.getActionType() : "GENERAL_ACTION";
        String description = candidate != null ? candidate.getDescription() : "Default execution task";

        // Check if registry has template
        Optional<PlanningGoal> templateOpt = goalRegistry.getTemplate(actionType);
        PlanningGoal rootGoal;
        if (templateOpt.isPresent()) {
            rootGoal = templateOpt.get();
        } else {
            rootGoal = PlanningGoal.builder()
                    .goalId("goal_" + UUID.randomUUID().toString().substring(0, 8))
                    .title(candidate != null ? candidate.getCandidateId() : "Primary Goal")
                    .description(description)
                    .priority(8)
                    .mandatory(true)
                    .targetDomain(context.getObjective() != null ? context.getObjective().getTargetDomain() : "CAMPUS")
                    .state(GoalState.IDENTIFIED)
                    .build();
        }

        int maxDepth = context.getScope() != null ? context.getScope().getMaxDecompositionDepth() : 3;
        List<SubGoal> subGoals = decomposeRecursively(rootGoal.getGoalId(), actionType, description, 1, maxDepth, context.getScope().isAllowOptionalTasks());
        rootGoal.setSubGoals(subGoals);
        rootGoal.setState(GoalState.DECOMPOSED);

        GoalHierarchy hierarchy = GoalHierarchy.builder()
                .rootGoal(rootGoal)
                .subGoals(subGoals)
                .depth(calculateActualDepth(subGoals, 1))
                .build();

        if (context.getContextId() != null) {
            goalRegistry.registerHierarchy(context.getContextId(), hierarchy);
        }

        return hierarchy;
    }

    private List<SubGoal> decomposeRecursively(String parentGoalId, String actionType, String description, int currentDepth, int maxDepth, boolean allowOptional) {
        List<SubGoal> list = new ArrayList<>();
        if (currentDepth >= maxDepth) {
            return list;
        }

        // SubGoal 1: Mandatory validation & context preparation
        String sg1Id = "sub_" + parentGoalId + "_prep_" + currentDepth;
        SubGoal prepSubGoal = SubGoal.builder()
                .subGoalId(sg1Id)
                .parentGoalId(parentGoalId)
                .title("Prepare Context & Requirements")
                .description("Validate prerequisites and parameters for " + description)
                .priority(9)
                .mandatory(true)
                .fulfilled(false)
                .weight(0.4)
                .state(GoalState.IDENTIFIED)
                .build();
        list.add(prepSubGoal);

        // SubGoal 2: Mandatory core execution phase
        String sg2Id = "sub_" + parentGoalId + "_exec_" + currentDepth;
        SubGoal execSubGoal = SubGoal.builder()
                .subGoalId(sg2Id)
                .parentGoalId(parentGoalId)
                .title("Execute Core Operations")
                .description("Perform main operations for action type: " + actionType)
                .priority(10)
                .mandatory(true)
                .fulfilled(false)
                .weight(0.5)
                .state(GoalState.IDENTIFIED)
                .build();
        list.add(execSubGoal);

        // SubGoal 3: Optional verification / telemetry phase
        if (allowOptional) {
            String sg3Id = "sub_" + parentGoalId + "_verify_" + currentDepth;
            SubGoal verifySubGoal = SubGoal.builder()
                    .subGoalId(sg3Id)
                    .parentGoalId(parentGoalId)
                    .title("Verify Outcome & Notify")
                    .description("Post-execution verification and status logging")
                    .priority(3)
                    .mandatory(false)
                    .fulfilled(false)
                    .weight(0.1)
                    .state(GoalState.IDENTIFIED)
                    .build();
            list.add(verifySubGoal);
        }

        return list;
    }

    private int calculateActualDepth(List<SubGoal> subGoals, int currentLevel) {
        if (subGoals == null || subGoals.isEmpty()) return currentLevel;
        int max = currentLevel;
        for (SubGoal sg : subGoals) {
            int d = calculateActualDepth(sg.getChildSubGoals(), currentLevel + 1);
            if (d > max) max = d;
        }
        return max;
    }

    private GoalHierarchy createFallbackHierarchy(String rationale) {
        PlanningGoal root = PlanningGoal.builder()
                .goalId("goal_fallback")
                .title("Fallback Goal")
                .description(rationale)
                .priority(1)
                .mandatory(true)
                .state(GoalState.IDENTIFIED)
                .build();

        return GoalHierarchy.builder()
                .rootGoal(root)
                .subGoals(new ArrayList<>())
                .depth(1)
                .build();
    }
}
