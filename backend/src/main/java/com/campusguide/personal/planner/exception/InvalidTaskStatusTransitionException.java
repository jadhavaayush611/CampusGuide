package com.campusguide.personal.planner.exception;

import com.campusguide.common.exception.BadRequestException;

public class InvalidTaskStatusTransitionException extends BadRequestException {
    public InvalidTaskStatusTransitionException(String message) {
        super(message);
    }
}
