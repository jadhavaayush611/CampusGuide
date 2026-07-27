package com.campusguide.personal.achievement.exception;

import com.campusguide.common.exception.ResourceNotFoundException;

public class AchievementNotFoundException extends ResourceNotFoundException {
    public AchievementNotFoundException(String message) {
        super(message);
    }
}
