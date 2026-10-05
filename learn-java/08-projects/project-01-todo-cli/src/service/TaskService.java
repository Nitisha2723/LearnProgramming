package service;

import model.Priority;
import model.Task;
import repository.TaskRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic for task management.
 *
 * TaskService owns all rules about what constitutes valid task data and
 * what operations are permitted. It delegates actual storage to a
 * {@link TaskRepository} injected via the constructor — this is manual
 * dependency injection, the same principle that frameworks like Spring
 * automate.
 *
 * The service layer knows nothing about how the user interacts with the
 * application (no Scanner, no System.out here — that belongs in the CLI).
 */
public class TaskService {

    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final TaskRepository repository;

    /**
     * Constructs a TaskService with the given repository.
     *
     * @param repository the storage backend (must not be null)
     */
    public TaskService(TaskRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("TaskRepository must not be null.");
        }
        this.repository = repository;
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Creates and saves a new task.
     *
     * @param title       non-blank, max 120 characters
     * @param description may be empty but not null, max 500 characters
     * @param priority    must not be null
     * @return the saved task
     * @throws IllegalArgumentException if title or description are invalid
     */
    public Task createTask(String title, String description, Priority priority) {
        validateTitle(title);
        validateDescription(description);
        if (priority == null) {
            throw new IllegalArgumentException("Priority must not be null.");
        }

        Task task = new Task(title, description == null ? "" : description, priority);
        return repository.save(task);
    }

    /**
     * Marks the task with the given id as completed.
     *
     * @param id the task's UUID
     * @return the updated task
     * @throws NoSuchElementException if no task with the given id exists
     */
    public Task completeTask(UUID id) {
        Task task = findOrThrow(id);
        task.complete();
        return repository.update(task);
    }

    /**
     * Updates the mutable fields of an existing task.
     *
     * Any parameter that is null is left unchanged on the task.
     *
     * @param id          the task's UUID
     * @param title       new title, or null to leave unchanged
     * @param description new description, or null to leave unchanged
     * @param priority    new priority, or null to leave unchanged
     * @return the updated task
     * @throws NoSuchElementException   if no task with the given id exists
     * @throws IllegalArgumentException if the new title is blank or too long
     */
    public Task updateTask(UUID id, String title, String description, Priority priority) {
        Task task = findOrThrow(id);

        if (title != null) {
            validateTitle(title);
        }
        if (description != null) {
            validateDescription(description);
        }

        task.update(title, description, priority);
        return repository.update(task);
    }

    /**
     * Deletes the task with the given id.
     *
     * @param id the task's UUID
     * @throws NoSuchElementException if no task with the given id exists
     */
    public void deleteTask(UUID id) {
        findOrThrow(id); // ensures the task exists before attempting delete
        boolean removed = repository.delete(id);
        if (!removed) {
            throw new NoSuchElementException("Failed to delete task with id " + id);
        }
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Returns all tasks sorted by priority descending (URGENT first),
     * then by creation time ascending (oldest first within the same priority).
     *
     * @return sorted list of all tasks
     */
    public List<Task> getAllTasks() {
        return repository.findAll().stream()
                .sorted(Comparator
                        .comparingInt((Task t) -> t.getPriority().getSortOrder())
                        .reversed()
                        .thenComparing(Task::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks with the given priority, sorted by creation time.
     *
     * @param priority the priority to filter by
     * @return list of matching tasks
     */
    public List<Task> getTasksByPriority(Priority priority) {
        return repository.findByPriority(priority).stream()
                .sorted(Comparator.comparing(Task::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks that have not yet been completed.
     *
     * @return list of pending tasks
     */
    public List<Task> getPendingTasks() {
        return repository.findByCompleted(false).stream()
                .sorted(Comparator
                        .comparingInt((Task t) -> t.getPriority().getSortOrder())
                        .reversed()
                        .thenComparing(Task::getCreatedAt))
                .collect(Collectors.toList());
    }

    /**
     * Returns all tasks that have been completed.
     *
     * @return list of completed tasks
     */
    public List<Task> getCompletedTasks() {
        return repository.findByCompleted(true).stream()
                .sorted(Comparator.comparing(Task::getCompletedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns a summary map of task statistics.
     *
     * Keys: "total", "completed", "pending", "completionRate"
     * The "completionRate" value is a Double in the range [0.0, 100.0].
     *
     * @return unmodifiable map of statistics
     */
    public Map<String, Object> getCompletionStats() {
        int total = repository.count();
        int completed = repository.findByCompleted(true).size();
        int pending = total - completed;
        double completionRate = total == 0 ? 0.0 : (completed * 100.0) / total;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pending", pending);
        stats.put("completionRate", Math.round(completionRate * 10.0) / 10.0);

        return Map.copyOf(stats);
    }

    // -------------------------------------------------------------------------
    // Validation helpers (package-private so tests can call them directly)
    // -------------------------------------------------------------------------

    /**
     * Validates a task title.
     *
     * @param title the title to validate
     * @throws IllegalArgumentException if title is null, blank, or exceeds 120 characters
     */
    void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank.");
        }
        if (title.strip().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Title must not exceed " + MAX_TITLE_LENGTH + " characters.");
        }
    }

    /**
     * Validates a task description.
     *
     * @param description the description to validate
     * @throws IllegalArgumentException if description exceeds 500 characters
     */
    void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Task findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No task found with id: " + id));
    }
}
