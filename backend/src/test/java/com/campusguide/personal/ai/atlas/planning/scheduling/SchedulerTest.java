package com.campusguide.personal.ai.atlas.planning.scheduling;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;
import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.DeadlineAwareSchedulingStrategy;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.EarliestCompletionSchedulingStrategy;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.PreferenceAwareSchedulingStrategy;
import com.campusguide.personal.ai.atlas.planning.scheduling.strategy.PriorityAwareSchedulingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerTest {

    @Test
    @DisplayName("Scheduler should execute selected strategy and produce valid schedule")
    void testSchedulerExecution() {
        EarliestCompletionSchedulingStrategy s1 = new EarliestCompletionSchedulingStrategy();
        DeadlineAwareSchedulingStrategy s2 = new DeadlineAwareSchedulingStrategy();
        PriorityAwareSchedulingStrategy s3 = new PriorityAwareSchedulingStrategy();
        PreferenceAwareSchedulingStrategy s4 = new PreferenceAwareSchedulingStrategy();

        Scheduler scheduler = new Scheduler(Arrays.asList(s1, s2, s3, s4));

        TaskGraph graph = new TaskGraph();
        PlanningTask t1 = PlanningTask.builder().taskId("t1").estimatedDurationMinutes(15.0).mandatory(true).build();
        graph.addTask(t1);

        PlanningContext context = PlanningContext.fromDecisionOutcome(DecisionOutcome.fallback("out_sched", "Schedule test"));

        Schedule schedule = scheduler.schedule(graph, context);
        assertThat(schedule).isNotNull();
        assertThat(schedule.getScheduledTasks()).hasSize(1);
        assertThat(schedule.getStrategyUsed()).isEqualTo("EARLIEST_COMPLETION");
        assertThat(schedule.getStartTime()).isNotNull();
        assertThat(schedule.getEndTime()).isNotNull();
    }
}
