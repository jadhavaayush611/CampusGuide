package com.campusguide.personal.ai.atlas.planning.constraint;

import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result returned by PlanningConstraintSolver after evaluating constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstraintResolution implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean satisfied;

    @Builder.Default
    private List<ConstraintViolationInfo> violations = new ArrayList<>();

    private TaskGraph adjustedTaskGraph;
    private String resolutionNotes;
}
