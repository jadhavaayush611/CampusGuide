package com.campusguide.personal.ai.atlas.planning.decomposition;

import com.campusguide.personal.ai.atlas.planning.model.PlanningGoal;
import com.campusguide.personal.ai.atlas.planning.model.SubGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates recursive hierarchical goal decomposition structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalHierarchy implements Serializable {

    private static final long serialVersionUID = 1L;

    private PlanningGoal rootGoal;

    @Builder.Default
    private List<SubGoal> subGoals = new ArrayList<>();

    private int depth;

    public int getTotalGoalCount() {
        return 1 + countSubGoals(subGoals);
    }

    public int getMandatoryGoalCount() {
        int count = (rootGoal != null && rootGoal.isMandatory()) ? 1 : 0;
        return count + countMandatory(subGoals);
    }

    public int getOptionalGoalCount() {
        return getTotalGoalCount() - getMandatoryGoalCount();
    }

    private int countSubGoals(List<SubGoal> goals) {
        if (goals == null || goals.isEmpty()) return 0;
        int sum = goals.size();
        for (SubGoal sg : goals) {
            sum += countSubGoals(sg.getChildSubGoals());
        }
        return sum;
    }

    private int countMandatory(List<SubGoal> goals) {
        if (goals == null || goals.isEmpty()) return 0;
        int sum = 0;
        for (SubGoal sg : goals) {
            if (sg.isMandatory()) sum++;
            sum += countMandatory(sg.getChildSubGoals());
        }
        return sum;
    }
}
