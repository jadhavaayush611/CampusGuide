package com.campusguide.personal.ai.atlas.planning.model;

/**
 * State of a PlanningGoal or SubGoal.
 */
public enum GoalState {
    IDENTIFIED,
    DECOMPOSED,
    SCHEDULED,
    FULFILLED,
    CANCELLED
}
