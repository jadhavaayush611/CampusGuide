package com.campusguide.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAtlasNotFoundException(com.campusguide.personal.ai.atlas.exception.AtlasNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleAtlasForbiddenException(com.campusguide.personal.ai.atlas.exception.AtlasForbiddenException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.notification.exception.NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotificationNotFoundException(com.campusguide.personal.notification.exception.NotificationNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.planner.exception.PlannerTaskNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePlannerTaskNotFoundException(com.campusguide.personal.planner.exception.PlannerTaskNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.calendar.exception.CalendarEntryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCalendarEntryNotFoundException(com.campusguide.personal.calendar.exception.CalendarEntryNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.achievement.exception.AchievementNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAchievementNotFoundException(com.campusguide.personal.achievement.exception.AchievementNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.notification.exception.ScheduledNotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleScheduledNotificationNotFoundException(com.campusguide.personal.notification.exception.ScheduledNotificationNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.notification.exception.ScheduledNotificationAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleScheduledNotificationAccessDeniedException(com.campusguide.personal.notification.exception.ScheduledNotificationAccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.notification.exception.ScheduledNotificationValidationException.class)
    public ResponseEntity<Map<String, String>> handleScheduledNotificationValidationException(com.campusguide.personal.notification.exception.ScheduledNotificationValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorisedException(UnauthorisedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<com.campusguide.platform.auth.dto.response.ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        com.campusguide.platform.auth.dto.response.ValidationErrorResponse response = com.campusguide.platform.auth.dto.response.ValidationErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<com.campusguide.platform.auth.dto.response.ValidationErrorResponse> handleConstraintViolationException(jakarta.validation.ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.contains(".") ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;
            errors.put(fieldName, violation.getMessage());
        });

        com.campusguide.platform.auth.dto.response.ValidationErrorResponse response = com.campusguide.platform.auth.dto.response.ValidationErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(com.campusguide.platform.auth.exception.EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(com.campusguide.platform.auth.exception.EmailAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(com.campusguide.platform.auth.exception.UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameAlreadyExistsException(com.campusguide.platform.auth.exception.UsernameAlreadyExistsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateKeyException(org.springframework.dao.DuplicateKeyException ex) {
        Map<String, String> error = new HashMap<>();
        String msg = ex.getMessage();
        if (msg != null && msg.contains("email")) {
            error.put("error", "Email already exists");
        } else if (msg != null && msg.contains("username")) {
            error.put("error", "Username already exists");
        } else {
            error.put("error", "Resource already exists");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "File size exceeds the maximum limit of 20MB");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(MultipartException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.exception.AiGatewayException.class)
    public ResponseEntity<Map<String, String>> handleAiGatewayException(com.campusguide.personal.ai.exception.AiGatewayException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.exception.PromptBuildException.class)
    public ResponseEntity<Map<String, String>> handlePromptBuildException(com.campusguide.personal.ai.exception.PromptBuildException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.exception.ConversationContextException.class)
    public ResponseEntity<Map<String, String>> handleConversationContextException(com.campusguide.personal.ai.exception.ConversationContextException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasPromptValidationException.class)
    public ResponseEntity<Map<String, String>> handleAtlasPromptValidationException(com.campusguide.personal.ai.atlas.exception.AtlasPromptValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException.class)
    public ResponseEntity<Map<String, String>> handleAtlasRateLimitException(com.campusguide.personal.ai.atlas.exception.AtlasRateLimitException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasAuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAtlasAuthenticationException(com.campusguide.personal.ai.atlas.exception.AtlasAuthenticationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasConfigurationException.class)
    public ResponseEntity<Map<String, String>> handleAtlasConfigurationException(com.campusguide.personal.ai.atlas.exception.AtlasConfigurationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasProviderUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleAtlasProviderUnavailableException(com.campusguide.personal.ai.atlas.exception.AtlasProviderUnavailableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleAtlasTimeoutException(com.campusguide.personal.ai.atlas.exception.AtlasTimeoutException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
    }

    @ExceptionHandler(com.campusguide.personal.ai.atlas.exception.AtlasProviderException.class)
    public ResponseEntity<Map<String, String>> handleAtlasProviderException(com.campusguide.personal.ai.atlas.exception.AtlasProviderException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request received: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Malformed JSON request or invalid field format");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch in request parameter or path variable: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid parameter value or format");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Unexpected error occurred in application context: ", ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}