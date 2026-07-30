package com.campusguide.personal.ai.atlas.planning.graph;

import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskGraphTest {

    @Test
    @DisplayName("TaskGraph should correctly perform topological sort and detect cycles")
    void testTopologicalSortAndCycleDetection() {
        TaskGraph graph = new TaskGraph();

        PlanningTask t1 = PlanningTask.builder().taskId("t1").estimatedDurationMinutes(10.0).build();
        PlanningTask t2 = PlanningTask.builder().taskId("t2").estimatedDurationMinutes(20.0).build();
        PlanningTask t3 = PlanningTask.builder().taskId("t3").estimatedDurationMinutes(15.0).build();

        graph.addTask(t1);
        graph.addTask(t2);
        graph.addTask(t3);

        graph.addDependency(TaskDependency.builder().dependencyId("d1").predecessorTaskId("t1").successorTaskId("t2").build());
        graph.addDependency(TaskDependency.builder().dependencyId("d2").predecessorTaskId("t2").successorTaskId("t3").build());

        assertThat(graph.hasCycle()).isFalse();

        List<PlanningTask> sorted = graph.topologicalSort();
        assertThat(sorted).extracting(PlanningTask::getTaskId).containsExactly("t1", "t2", "t3");

        List<List<PlanningTask>> batches = graph.getParallelBatches();
        assertThat(batches).hasSize(3);

        List<PlanningTask> criticalPath = graph.calculateCriticalPath();
        assertThat(criticalPath).extracting(PlanningTask::getTaskId).containsExactly("t1", "t2", "t3");
    }

    @Test
    @DisplayName("TaskGraph should detect cycle when edge forms a loop")
    void testCycleDetection() {
        TaskGraph graph = new TaskGraph();
        PlanningTask t1 = PlanningTask.builder().taskId("t1").build();
        PlanningTask t2 = PlanningTask.builder().taskId("t2").build();
        graph.addTask(t1);
        graph.addTask(t2);

        graph.addDependency(TaskDependency.builder().dependencyId("d1").predecessorTaskId("t1").successorTaskId("t2").build());
        graph.addDependency(TaskDependency.builder().dependencyId("d2").predecessorTaskId("t2").successorTaskId("t1").build());

        assertThat(graph.hasCycle()).isTrue();
        assertThatThrownBy(graph::topologicalSort).isInstanceOf(IllegalStateException.class);
    }
}
