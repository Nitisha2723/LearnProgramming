package com.example.todoapi.dto;

import java.time.LocalDateTime;

/**
 * DTO for returning todo data to API clients.
 *
 * <p>This is what clients see — never expose the internal {@link com.example.todoapi.model.Todo}
 * entity directly. Using a separate response DTO lets you:
 * <ul>
 *   <li>Control exactly which fields are visible</li>
 *   <li>Rename fields for the API without changing the domain model</li>
 *   <li>Add computed/aggregated fields (e.g., {@code isOverdue})</li>
 *   <li>Change the internal model without breaking the API contract</li>
 *   <li>Avoid leaking internal/sensitive fields</li>
 * </ul>
 *
 * <p>Jackson serializes this record to JSON automatically:
 * <pre>
 * {
 *   "id": 1,
 *   "title": "Learn Spring Boot",
 *   "description": "Build a REST API",
 *   "completed": false,
 *   "createdAt": "2024-01-15T10:30:00"
 * }
 * </pre>
 */
public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt
) {

    /**
     * Factory method to convert from the internal domain entity to this response DTO.
     *
     * <p>Centralizing this mapping here (or in a separate mapper class) keeps
     * the conversion logic in one place.
     */
    public static TodoResponse from(com.example.todoapi.model.Todo todo) {
        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt()
        );
    }
}
