package com.campusguide.personal.ai.atlas.decision.constraint;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;

import java.util.Optional;

/**
 * Interface representing a individual constraint validator.
 */
public interface DecisionConstraint {

    String getConstraintName();

    ConstraintType getConstraintType();

    ConstraintSeverity getSeverity();

    Optional<ConstraintViolation> validate(DecisionCandidate candidate, DecisionContext context);
}
