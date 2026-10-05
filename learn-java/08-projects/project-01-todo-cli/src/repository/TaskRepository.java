package repository;

import model.Priority;
import model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contract for Task storage operations.
 *
 * The service layer depends only on this interface, never on a concrete
 * implementation. This means you can swap the in-memory store for a
 * file-backed or database-backed store without changing any business logic.
 */
public interface TaskRepository {

    /**
     * Returns all tasks in the repository.
     * The returned list is a snapshot — modifications to it do not affect storage.
     *
     * @return unmodifiable list of all tasks
     */
    List<Task> findAll();

    /**
     * Finds a single task by its unique identifier.
     *
     * @param id the task's UUID
     * @return Optional containing the task if found, empty Optional otherwise
     */
    Optional<Task> findById(UUID id);

    /**
     * Returns all tasks matching the given completion status.
     *
     * @param completed true to find completed tasks, false to find pending tasks
     * @return list of matching tasks
     */
    List<Task> findByCompleted(boolean completed);

    /**
     * Returns all tasks with the given priority level.
     *
     * @param priority the priority to filter by
     * @return list of matching tasks
     */
    List<Task> findByPriority(Priority priority);

    /**
     * Persists a new task.
     *
     * Callers must not call this method with a task whose id already exists
     * in the repository; use {@link #update(Task)} for that case.
     *
     * @param task the task to save
     * @return the saved task (same object)
     * @throws IllegalArgumentException if a task with the same id already exists
     */
    Task save(Task task);

    /**
     * Replaces an existing task in the repository.
     *
     * @param task the updated task (must already exist)
     * @return the updated task (same object)
     * @throws java.util.NoSuchElementException if no task with the same id exists
     */
    Task update(Task task);

    /**
     * Removes the task with the given id.
     *
     * @param id the id of the task to remove
     * @return true if a task was removed, false if no task had that id
     */
    boolean delete(UUID id);

    /**
     * Returns the total number of tasks in the repository.
     *
     * @return task count (>= 0)
     */
    int count();
}
