package com.campusguide.personal.ai.atlas.planning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a decomposed sub-goal in the GoalHierarchy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGoal implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subGoalId;
    private String parentGoalId;
    private String title;
    private String description;
    private int priority;
    private boolean mandatory;
    private boolean fulfilled;
    private double weight;

    @Builder.Default
    private GoalState state = GoalState.IDENTIFIED;

    @Builder.Default
    private List<SubGoal> childSubGoals = new ArrayList<>();
}
