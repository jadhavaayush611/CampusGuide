package com.campusguide.personal.achievement.exception;

import org.springframework.security.access.AccessDeniedException;

public class AchievementAccessDeniedException extends AccessDeniedException {
    public AchievementAccessDeniedException(String message) {
        super(message);
    }
}
