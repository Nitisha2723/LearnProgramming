package com.example.todoapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new todo.
 *
 * <p>Using a Java {@code record} for request DTOs is idiomatic in modern Java:
 * <ul>
 *   <li>Immutable — fields cannot be changed after construction</li>
 *   <li>Compact — no boilerplate getters, equals, hashCode, toString</li>
 *   <li>Clear intent — this is a data carrier, not a full domain object</li>
 * </ul>
 *
 * <p>The validation annotations ({@code @NotBlank}, {@code @Size}) are enforced
 * when the controller method is annotated with {@code @Valid @RequestBody CreateTodoRequest}.
 * If validation fails, Spring throws {@code MethodArgumentNotValidException}
 * which is handled by {@link com.example.todoapi.exception.GlobalExceptionHandler}.
 */
public record CreateTodoRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description

) {
    // Records are final and their canonical constructor is auto-generated.
    // Validation annotations on record components are automatically applied
    // to the canonical constructor parameters AND to the accessor methods.

    // You can add a compact constructor to add extra validation logic:
    public CreateTodoRequest {
        // Trim whitespace from title if present
        if (title != null) {
            title = title.trim();
        }
    }
}
