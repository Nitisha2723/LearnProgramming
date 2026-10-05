package com.example.todoapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Todo REST API.
 *
 * <p>{@code @RestControllerAdvice} (= {@code @ControllerAdvice} + {@code @ResponseBody})
 * makes this class intercept exceptions thrown by ANY controller in the application.
 * This centralizes error handling — no try-catch blocks in controllers.
 *
 * <p>Without this class, Spring would return its default error response (from
 * {@code BasicErrorController}), which is less informative and harder to control.
 *
 * <p>Handlers are matched by exception type. The most specific match wins.
 * The catch-all {@code Exception.class} handler is a safety net for anything not
 * explicitly handled.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Domain exceptions
    // -------------------------------------------------------------------------

    /**
     * Handles {@link TodoNotFoundException} → 404 Not Found.
     *
     * <p>Thrown by the service when a todo with a given ID doesn't exist.
     */
    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTodoNotFound(
            TodoNotFoundException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles {@link IllegalStateException} → 409 Conflict.
     *
     * <p>Thrown by the service when a business rule is violated,
     * e.g., trying to complete an already-completed todo.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, WebRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // -------------------------------------------------------------------------
    // Validation exceptions
    // -------------------------------------------------------------------------

    /**
     * Handles {@link MethodArgumentNotValidException} → 400 Bad Request.
     *
     * <p>Thrown by Spring when {@code @Valid} validation on a {@code @RequestBody}
     * fails (e.g., blank title, oversized field, invalid email).
     *
     * <p>Extracts all field errors and returns them as a map:
     * <pre>
     * {
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Validation failed",
     *   "errors": {
     *     "title": "must not be blank",
     *     "email": "must be a valid email address"
     *   }
     * }
     * </pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Collect all field-level validation errors into a map
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        ValidationErrorResponse error = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(error);
    }

    // -------------------------------------------------------------------------
    // Catch-all
    // -------------------------------------------------------------------------

    /**
     * Catch-all handler for any unhandled exception → 500 Internal Server Error.
     *
     * <p>In production, you would:
     * <ul>
     *   <li>Log the full exception (stack trace) here</li>
     *   <li>Return a generic message (never leak internal details to clients)</li>
     *   <li>Potentially alert your monitoring system (Sentry, Datadog, etc.)</li>
     * </ul>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, WebRequest request) {

        // In production: log.error("Unhandled exception", ex);
        // Do NOT return ex.getMessage() — it may leak internal implementation details

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // -------------------------------------------------------------------------
    // Error response DTOs (inner records for simplicity)
    // -------------------------------------------------------------------------

    /**
     * Standard error response body.
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp,
            String path
    ) {}

    /**
     * Error response body for validation failures — includes per-field errors.
     */
    public record ValidationErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp,
            String path,
            Map<String, String> errors
    ) {}
}
