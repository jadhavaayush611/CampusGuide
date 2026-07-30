package com.campusguide.personal.ai.atlas.execution.runtime.rollback;

import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlan;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Facade consuming RollbackPlans from ExecutableWorkflow and executing deterministic compensating actions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RollbackExecutor {

    private final RollbackCoordinator rollbackCoordinator;

    public boolean rollbackWorkflow(WorkflowInstance instance, String reason) {
        if (instance == null) {
            log.warn("Cannot rollback null WorkflowInstance");
            return false;
        }

        ExecutableWorkflow workflow = instance.getWorkflow();
        RollbackPlan plan = workflow != null ? workflow.getRollbackPlan() : null;
        if (plan == null) {
            plan = RollbackPlan.empty(instance.getWorkflowId());
        }

        return rollbackCoordinator.executeRollback(instance, plan, reason);
    }
}
