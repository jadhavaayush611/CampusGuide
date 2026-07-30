package com.campusguide.personal.ai.atlas.planning.constraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates details of a constraint violation found during solver evaluation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstraintViolationInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String constraintId;
    private ConstraintType type;
    private String taskId;
    private String message;
    private boolean hardViolation;
}
