package com.campusguide.personal.ai.atlas.planning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an atomic step within a PlanningTask.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningStep implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stepId;
    private String taskId;
    private String title;
    private int orderIndex;
    private String stepType;
    private boolean mandatory;
    private String status;

    @Builder.Default
    private Map<String, Object> parameters = new ConcurrentHashMap<>();
}
