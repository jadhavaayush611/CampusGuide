package com.campusguide.personal.planner.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlannerTaskExceptionTest {

    @Test
    void testPlannerTaskNotFoundException() {
        PlannerTaskNotFoundException ex = new PlannerTaskNotFoundException("Task not found");
        assertEquals("Task not found", ex.getMessage());
    }

    @Test
    void testPlannerTaskAccessDeniedException() {
        PlannerTaskAccessDeniedException ex = new PlannerTaskAccessDeniedException("Access denied");
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testPlannerTaskValidationException() {
        PlannerTaskValidationException ex = new PlannerTaskValidationException("Validation error");
        assertEquals("Validation error", ex.getMessage());
    }

    @Test
    void testInvalidTaskStatusTransitionException() {
        InvalidTaskStatusTransitionException ex = new InvalidTaskStatusTransitionException("Invalid transition");
        assertEquals("Invalid transition", ex.getMessage());
    }
}
