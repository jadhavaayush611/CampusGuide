package com.campusguide.personal.achievement.exception;

import com.campusguide.common.exception.ConflictException;

public class AchievementAlreadyExistsException extends ConflictException {
    public AchievementAlreadyExistsException(String message) {
        super(message);
    }
}
