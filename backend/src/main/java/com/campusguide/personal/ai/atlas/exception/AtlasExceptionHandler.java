package com.campusguide.personal.ai.atlas.exception;

import com.campusguide.personal.ai.atlas.dto.AtlasErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Standardized global exception handler for Atlas API Platform controllers.
 * Maps application and runtime exceptions to AtlasErrorResponse contracts.
 */
@RestControllerAdvice(basePackages = "com.campusguide.personal.ai.atlas.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class AtlasExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AtlasErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = (error instanceof FieldError fieldError) ? fieldError.getField() : error.getObjectName();
            details.put(fieldName, error.getDefaultMessage());
        });

        String firstErrorMessage = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Validation failed"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        AtlasErrorResponse errorResponse = AtlasErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(firstErrorMessage)
                .message("Request payload validation failed")
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AtlasErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String prop = violation.getPropertyPath().toString();
            String field = prop.contains(".") ? prop.substring(prop.lastIndexOf('.') + 1) : prop;
            details.put(field, violation.getMessage());
        });

        AtlasErrorResponse errorResponse = AtlasErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ex.getMessage())
                .message("Constraint violation")
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AtlasPromptValidationException.class)
    public ResponseEntity<AtlasErrorResponse> handlePromptValidation(
            AtlasPromptValidationException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AtlasNotFoundException.class)
    public ResponseEntity<AtlasErrorResponse> handleNotFound(
            AtlasNotFoundException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AtlasAuthenticationException.class)
    public ResponseEntity<AtlasErrorResponse> handleAuthentication(
            AtlasAuthenticationException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AtlasForbiddenException.class)
    public ResponseEntity<AtlasErrorResponse> handleForbidden(
            AtlasForbiddenException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AtlasErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied: Insufficient privileges", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(AtlasRateLimitException.class)
    public ResponseEntity<AtlasErrorResponse> handleRateLimit(
            AtlasRateLimitException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    @ExceptionHandler(AtlasProviderException.class)
    public ResponseEntity<AtlasErrorResponse> handleProvider(
            AtlasProviderException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(AtlasProviderUnavailableException.class)
    public ResponseEntity<AtlasErrorResponse> handleProviderUnavailable(
            AtlasProviderUnavailableException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(AtlasTimeoutException.class)
    public ResponseEntity<AtlasErrorResponse> handleTimeout(
            AtlasTimeoutException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.GATEWAY_TIMEOUT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(AtlasExecutionException.class)
    public ResponseEntity<AtlasErrorResponse> handleExecution(
            AtlasExecutionException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(AtlasConfigurationException.class)
    public ResponseEntity<AtlasErrorResponse> handleConfiguration(
            AtlasConfigurationException ex, HttpServletRequest request) {
        AtlasErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(AtlasException.class)
    public ResponseEntity<AtlasErrorResponse> handleAtlasException(
            AtlasException ex, HttpServletRequest request) {
        HttpStatus status = mapCategoryToStatus(ex.getCategory());
        AtlasErrorResponse errorResponse = buildErrorResponse(status, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AtlasErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception in Atlas controller: {}", ex.getMessage(), ex);
        AtlasErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private AtlasErrorResponse buildErrorResponse(HttpStatus status, String errorMessage, String path) {
        return AtlasErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(errorMessage)
                .message(errorMessage)
                .path(path)
                .details(Map.of())
                .build();
    }

    private HttpStatus mapCategoryToStatus(AtlasErrorCategory category) {
        if (category == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        return switch (category) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case AUTHENTICATION -> HttpStatus.UNAUTHORIZED;
            case AUTHORIZATION -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case EXECUTION_FAILURE -> HttpStatus.INTERNAL_SERVER_ERROR;
            case RATE_LIMIT, QUOTA_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case PROVIDER_TRANSIENT, PROVIDER_PERMANENT -> HttpStatus.BAD_GATEWAY;
            case CIRCUIT_BREAKER_OPEN -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
