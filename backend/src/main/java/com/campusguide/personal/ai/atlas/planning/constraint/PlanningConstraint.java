package com.campusguide.personal.ai.atlas.planning.constraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constraint definition evaluated by PlanningConstraintSolver.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningConstraint implements Serializable {

    private static final long serialVersionUID = 1L;

    private String constraintId;
    private String name;

    @Builder.Default
    private ConstraintType type = ConstraintType.POLICY;

    private boolean mandatory;

    @Builder.Default
    private Map<String, Object> parameters = new ConcurrentHashMap<>();
}
