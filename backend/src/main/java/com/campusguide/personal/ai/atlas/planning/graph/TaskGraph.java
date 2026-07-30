package com.campusguide.personal.ai.atlas.planning.graph;

import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deterministic Directed Acyclic Graph (DAG) of PlanningTask nodes and TaskDependency edges.
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Map<String, PlanningTask> tasks = new ConcurrentHashMap<>();

    @Builder.Default
    private List<TaskDependency> dependencies = new ArrayList<>();

    public void addTask(PlanningTask task) {
        if (task != null && task.getTaskId() != null) {
            tasks.put(task.getTaskId(), task);
        }
    }

    public void addDependency(TaskDependency dependency) {
        if (dependency != null && dependency.getPredecessorTaskId() != null && dependency.getSuccessorTaskId() != null) {
            dependencies.add(dependency);
        }
    }

    public PlanningTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    public List<PlanningTask> getTaskList() {
        return new ArrayList<>(tasks.values());
    }

    public List<PlanningTask> getPredecessors(String taskId) {
        List<PlanningTask> result = new ArrayList<>();
        for (TaskDependency dep : dependencies) {
            if (dep.getSuccessorTaskId().equals(taskId)) {
                PlanningTask pred = tasks.get(dep.getPredecessorTaskId());
                if (pred != null) {
                    result.add(pred);
                }
            }
        }
        return result;
    }

    public List<PlanningTask> getSuccessors(String taskId) {
        List<PlanningTask> result = new ArrayList<>();
        for (TaskDependency dep : dependencies) {
            if (dep.getPredecessorTaskId().equals(taskId)) {
                PlanningTask succ = tasks.get(dep.getSuccessorTaskId());
                if (succ != null) {
                    result.add(succ);
                }
            }
        }
        return result;
    }

    /**
     * Checks if the graph contains any cycles using Kahn's Algorithm.
     */
    public boolean hasCycle() {
        try {
            topologicalSort();
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    /**
     * Performs topological sort of tasks using Kahn's Algorithm (in-degree BFS).
     * Deterministic ordering guaranteed by sorting tied task IDs lexicographically.
     */
    public List<PlanningTask> topologicalSort() {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String taskId : tasks.keySet()) {
            inDegree.put(taskId, 0);
        }
        for (TaskDependency dep : dependencies) {
            if (inDegree.containsKey(dep.getSuccessorTaskId())) {
                inDegree.put(dep.getSuccessorTaskId(), inDegree.get(dep.getSuccessorTaskId()) + 1);
            }
        }

        PriorityQueue<String> readyQueue = new PriorityQueue<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                readyQueue.add(entry.getKey());
            }
        }

        List<PlanningTask> sorted = new ArrayList<>();
        while (!readyQueue.isEmpty()) {
            String currentId = readyQueue.poll();
            PlanningTask task = tasks.get(currentId);
            if (task != null) {
                sorted.add(task);
            }

            for (TaskDependency dep : dependencies) {
                if (dep.getPredecessorTaskId().equals(currentId)) {
                    String succId = dep.getSuccessorTaskId();
                    if (inDegree.containsKey(succId)) {
                        int updated = inDegree.get(succId) - 1;
                        inDegree.put(succId, updated);
                        if (updated == 0) {
                            readyQueue.add(succId);
                        }
                    }
                }
            }
        }

        if (sorted.size() != tasks.size()) {
            log.error("Cycle detected in TaskGraph. Sorted count {} vs total tasks {}", sorted.size(), tasks.size());
            throw new IllegalStateException("Cycle detected in TaskGraph");
        }

        return sorted;
    }

    /**
     * Groups tasks into parallel execution batches where each batch can run concurrently.
     */
    public List<List<PlanningTask>> getParallelBatches() {
        if (hasCycle()) {
            return Collections.emptyList();
        }
        List<PlanningTask> sorted = topologicalSort();
        Map<String, Integer> batchLevel = new HashMap<>();

        for (PlanningTask task : sorted) {
            int maxPredLevel = -1;
            for (PlanningTask pred : getPredecessors(task.getTaskId())) {
                int level = batchLevel.getOrDefault(pred.getTaskId(), 0);
                if (level > maxPredLevel) {
                    maxPredLevel = level;
                }
            }
            batchLevel.put(task.getTaskId(), maxPredLevel + 1);
        }

        Map<Integer, List<PlanningTask>> batchesMap = new TreeMap<>();
        for (PlanningTask task : sorted) {
            int level = batchLevel.getOrDefault(task.getTaskId(), 0);
            batchesMap.computeIfAbsent(level, k -> new ArrayList<>()).add(task);
        }

        return new ArrayList<>(batchesMap.values());
    }

    /**
     * Calculates the critical path tasks and total duration.
     */
    public List<PlanningTask> calculateCriticalPath() {
        if (hasCycle()) {
            return Collections.emptyList();
        }
        List<PlanningTask> sorted = topologicalSort();
        Map<String, Double> earliestFinish = new HashMap<>();
        Map<String, String> bestPredecessor = new HashMap<>();

        for (PlanningTask task : sorted) {
            double duration = task.getEstimatedDurationMinutes();
            double maxPredFinish = 0.0;
            String bestPredId = null;

            for (TaskDependency dep : dependencies) {
                if (dep.getSuccessorTaskId().equals(task.getTaskId())) {
                    double predFinish = earliestFinish.getOrDefault(dep.getPredecessorTaskId(), 0.0) + dep.getLagMinutes();
                    if (predFinish > maxPredFinish) {
                        maxPredFinish = predFinish;
                        bestPredId = dep.getPredecessorTaskId();
                    }
                }
            }

            earliestFinish.put(task.getTaskId(), maxPredFinish + duration);
            if (bestPredId != null) {
                bestPredecessor.put(task.getTaskId(), bestPredId);
            }
        }

        // Find task with max finish time
        String endTaskId = null;
        double maxFinish = -1.0;
        for (Map.Entry<String, Double> entry : earliestFinish.entrySet()) {
            if (entry.getValue() > maxFinish) {
                maxFinish = entry.getValue();
                endTaskId = entry.getKey();
            }
        }

        LinkedList<PlanningTask> criticalPath = new LinkedList<>();
        String curr = endTaskId;
        while (curr != null) {
            PlanningTask t = tasks.get(curr);
            if (t != null) {
                criticalPath.addFirst(t);
            }
            curr = bestPredecessor.get(curr);
        }

        return criticalPath;
    }
}
