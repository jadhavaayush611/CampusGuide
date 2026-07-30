package com.campusguide.personal.ai.atlas.planning.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Directed dependency link between predecessor and successor tasks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDependency implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dependencyId;
    private String predecessorTaskId;
    private String successorTaskId;

    @Builder.Default
    private DependencyType dependencyType = DependencyType.HARD;

    private double lagMinutes;
    private String condition;
}
