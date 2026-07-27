package com.campusguide.personal.planner.exception;

import org.springframework.security.access.AccessDeniedException;

public class PlannerTaskAccessDeniedException extends AccessDeniedException {
    public PlannerTaskAccessDeniedException(String message) {
        super(message);
    }
}
