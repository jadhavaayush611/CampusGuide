package com.campusguide.personal.ai.atlas.planning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a high-level goal within an ExecutionPlan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningGoal implements Serializable {

    private static final long serialVersionUID = 1L;

    private String goalId;
    private String title;
    private String description;
    private int priority;
    private boolean mandatory;
    private String parentGoalId;
    private String targetDomain;

    @Builder.Default
    private GoalState state = GoalState.IDENTIFIED;

    @Builder.Default
    private List<SubGoal> subGoals = new ArrayList<>();
}
