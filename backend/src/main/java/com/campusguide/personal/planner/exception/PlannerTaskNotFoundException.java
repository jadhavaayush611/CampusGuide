package com.campusguide.personal.planner.exception;

import com.campusguide.common.exception.ResourceNotFoundException;

public class PlannerTaskNotFoundException extends ResourceNotFoundException {
    public PlannerTaskNotFoundException(String message) {
        super(message);
    }
}
