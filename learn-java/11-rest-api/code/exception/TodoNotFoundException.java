package com.example.todoapi.exception;

/**
 * Thrown when a Todo with a given ID cannot be found.
 *
 * <p>This is a domain exception — it represents a business concept
 * ("this todo doesn't exist"), not a technical error.
 *
 * <p>Extends {@link RuntimeException} (unchecked) so callers are not
 * forced to declare or catch it. In a Spring REST API, unchecked exceptions
 * are the convention — they propagate up to {@link GlobalExceptionHandler}.
 *
 * <p>Usage in the service layer:
 * <pre>
 * Todo todo = todoRepository.findById(id)
 *         .orElseThrow(() -> new TodoNotFoundException(id));
 * </pre>
 *
 * <p>The {@link GlobalExceptionHandler} catches this and returns a 404 response.
 */
public class TodoNotFoundException extends RuntimeException {

    private final Long id;

    public TodoNotFoundException(Long id) {
        super("Todo with id " + id + " not found");
        this.id = id;
    }

    /**
     * Returns the ID that was not found — useful for logging or error details.
     */
    public Long getId() {
        return id;
    }
}
