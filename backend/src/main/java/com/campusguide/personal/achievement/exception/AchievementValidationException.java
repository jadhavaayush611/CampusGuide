package com.campusguide.personal.achievement.exception;

import com.campusguide.common.exception.BadRequestException;

public class AchievementValidationException extends BadRequestException {
    public AchievementValidationException(String message) {
        super(message);
    }
}
