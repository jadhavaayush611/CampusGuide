package com.campusguide.personal.ai.atlas.planning.model;

import com.campusguide.personal.ai.atlas.planning.graph.TaskState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single planning task in an ExecutionPlan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;
    private String goalId;
    private String title;
    private String description;

    @Builder.Default
    private TaskState state = TaskState.PENDING;

    private double estimatedDurationMinutes;
    private boolean mandatory;
    private boolean parallelizable;
    private boolean conditional;
    private String precondition;

    @Builder.Default
    private List<PlanningStep> steps = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> executionPayload = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();
}
