package model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single task in the TODO system.
 *
 * A Task is a domain object: it holds its own state and knows how to
 * transition itself (e.g., marking itself complete), but it does not
 * know anything about storage or business rules. Those belong to the
 * repository and service layers respectively.
 */
public class Task {

    private final UUID id;
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    /**
     * Creates a new Task.
     *
     * The id is auto-generated and the createdAt timestamp is set to now.
     * The task starts in an uncompleted state.
     *
     * @param title       Short summary of the task (required, non-blank)
     * @param description Longer description (may be empty, not null)
     * @param priority    Priority level (required)
     */
    public Task(String title, String description, Priority priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank.");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description must not be null. Use empty string instead.");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority must not be null.");
        }

        this.id = UUID.randomUUID();
        this.title = title.strip();
        this.description = description.strip();
        this.priority = priority;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.completedAt = null;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    // -------------------------------------------------------------------------
    // State transitions
    // -------------------------------------------------------------------------

    /**
     * Marks this task as completed.
     *
     * Sets completed to true and records the completion timestamp.
     * Calling this method on an already-completed task is a no-op.
     */
    public void complete() {
        if (!this.completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Updates the mutable fields of this task.
     *
     * Any parameter that is null is left unchanged, allowing callers
     * to update only the fields they care about.
     *
     * @param title       New title, or null to keep existing
     * @param description New description, or null to keep existing
     * @param priority    New priority, or null to keep existing
     */
    public void update(String title, String description, Priority priority) {
        if (title != null && !title.isBlank()) {
            this.title = title.strip();
        }
        if (description != null) {
            this.description = description.strip();
        }
        if (priority != null) {
            this.priority = priority;
        }
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Returns a human-readable one-line summary of this task.
     *
     * Format: [DONE] HIGH   | Buy groceries
     *         [    ] MEDIUM | Write unit tests
     */
    @Override
    public String toString() {
        String statusBadge = completed ? "[DONE]" : "[    ]";
        String shortId = id.toString().substring(0, 8);
        return String.format("%s %-6s | %-12s | %s  (%s)",
                statusBadge,
                priority.getDisplayLabel(),
                shortId,
                title,
                completed ? "completed" : "pending");
    }

    /**
     * Two tasks are equal if and only if they share the same UUID.
     *
     * This means a task retrieved from storage equals the original task
     * object that was used to create it.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
