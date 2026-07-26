package com.campusguide.platform.auth.validation;

public final class ValidationMessages {

    private ValidationMessages() {
        // Utility class
    }

    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String EMAIL_MAX_LENGTH = "Email must not exceed 255 characters";

    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String USERNAME_SIZE = "Username must be between 3 and 50 characters";
    public static final String USERNAME_PATTERN = "Username must contain only alphanumeric characters, underscores, dots, or hyphens";
    public static final String USERNAME_PATTERN_REGEXP = "^[a-zA-Z0-9_.-]+$";

    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_STRENGTH = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character";

    public static final String EMAIL_OR_USERNAME_REQUIRED = "Email or username is required";

    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token is required";

    public static final String VALIDATION_FAILED = "Validation failed";
}
