package com.example.todoapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing todo.
 *
 * <p>PUT /api/v1/todos/{id} replaces the resource entirely, so all fields
 * are validated as required (same rules as create).
 *
 * <p>The {@code completed} field is included because a PUT request represents
 * a full replacement of the resource state.
 */
public record UpdateTodoRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        boolean completed

) {}
