package com.campusguide.personal.planner.exception;

import com.campusguide.common.exception.BadRequestException;

public class PlannerTaskValidationException extends BadRequestException {
    public PlannerTaskValidationException(String message) {
        super(message);
    }
}
