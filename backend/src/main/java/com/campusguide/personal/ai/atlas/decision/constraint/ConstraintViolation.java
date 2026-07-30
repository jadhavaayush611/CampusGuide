package com.campusguide.personal.ai.atlas.decision.constraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Encapsulates details of a constraint violation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstraintViolation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateId;
    private String constraintName;
    private ConstraintType constraintType;
    private String violationReason;
    private ConstraintSeverity severity;

    public static ConstraintViolation hard(String candidateId, String name, ConstraintType type, String reason) {
        return ConstraintViolation.builder()
                .candidateId(candidateId)
                .constraintName(name)
                .constraintType(type)
                .violationReason(reason)
                .severity(ConstraintSeverity.HARD)
                .build();
    }

    public static ConstraintViolation soft(String candidateId, String name, ConstraintType type, String reason) {
        return ConstraintViolation.builder()
                .candidateId(candidateId)
                .constraintName(name)
                .constraintType(type)
                .violationReason(reason)
                .severity(ConstraintSeverity.SOFT)
                .build();
    }
}
