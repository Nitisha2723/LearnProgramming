package com.example.bookstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class BookStoreExceptionHandler {

    @ExceptionHandler({AuthorNotFoundException.class, BookNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage(),
                LocalDateTime.now(), getPath(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AuthorHasBooksException.class)
    public ResponseEntity<ErrorResponse> handleConflict(AuthorHasBooksException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(409, "Conflict", ex.getMessage(),
                LocalDateTime.now(), getPath(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                fieldErrors.put(e.getField(), e.getDefaultMessage()));
        ValidationErrorResponse error = new ValidationErrorResponse(400, "Bad Request",
                "Validation failed", LocalDateTime.now(), getPath(request), fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred", LocalDateTime.now(), getPath(request));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    public record ErrorResponse(int status, String error, String message,
                                 LocalDateTime timestamp, String path) {}

    public record ValidationErrorResponse(int status, String error, String message,
                                           LocalDateTime timestamp, String path,
                                           Map<String, String> errors) {}
}
