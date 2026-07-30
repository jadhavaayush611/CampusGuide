package com.campusguide.personal.ai.atlas.planning.decomposition;

import com.campusguide.personal.ai.atlas.planning.model.PlanningGoal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for goal decomposition templates and active goal hierarchies.
 */
@Slf4j
@Component("planningGoalRegistry")
public class GoalRegistry {

    private final Map<String, PlanningGoal> goalTemplates = new ConcurrentHashMap<>();
    private final Map<String, GoalHierarchy> activeHierarchies = new ConcurrentHashMap<>();

    public void registerTemplate(String actionType, PlanningGoal templateGoal) {
        if (actionType != null && templateGoal != null) {
            goalTemplates.put(actionType.toUpperCase(), templateGoal);
            log.debug("Registered goal template for action type: {}", actionType);
        }
    }

    public Optional<PlanningGoal> getTemplate(String actionType) {
        if (actionType == null) return Optional.empty();
        return Optional.ofNullable(goalTemplates.get(actionType.toUpperCase()));
    }

    public void registerHierarchy(String planId, GoalHierarchy hierarchy) {
        if (planId != null && hierarchy != null) {
            activeHierarchies.put(planId, hierarchy);
        }
    }

    public Optional<GoalHierarchy> getHierarchy(String planId) {
        if (planId == null) return Optional.empty();
        return Optional.ofNullable(activeHierarchies.get(planId));
    }

    public void clear() {
        activeHierarchies.clear();
    }
}
