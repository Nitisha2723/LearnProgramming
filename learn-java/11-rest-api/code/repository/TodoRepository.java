package com.example.todoapi.repository;

import com.example.todoapi.model.Todo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for Todo entities.
 *
 * <p>This class replaces a real database for learning purposes. It stores
 * todos in a {@link ConcurrentHashMap}, making it thread-safe for use in
 * a multi-threaded web application.
 *
 * <p>In a production application, this would be replaced by:
 * <ul>
 *   <li>A Spring Data JPA repository interface (extending {@code JpaRepository<Todo, Long>})</li>
 *   <li>A Spring Data MongoDB repository</li>
 *   <li>Any other persistence mechanism</li>
 * </ul>
 *
 * <p>The {@code @Repository} annotation:
 * <ul>
 *   <li>Marks this class as a Spring-managed bean (component scanning picks it up)</li>
 *   <li>Enables Spring's exception translation (persistence exceptions → Spring DataAccessException)</li>
 * </ul>
 */
@Repository
public class TodoRepository {

    /**
     * The in-memory data store.
     * ConcurrentHashMap is used for thread safety — multiple HTTP requests
     * can arrive concurrently and modify the map simultaneously.
     */
    private final ConcurrentHashMap<Long, Todo> store = new ConcurrentHashMap<>();

    /**
     * Auto-incrementing ID generator.
     * AtomicLong ensures thread-safe ID generation without synchronization.
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Returns all todos as a new list (defensive copy).
     * The returned list is not backed by the store — modifications to it
     * do not affect the stored data.
     */
    public List<Todo> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Finds a todo by its ID.
     *
     * @param id the todo ID
     * @return an Optional containing the todo if found, empty if not
     */
    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Checks whether a todo with the given ID exists.
     *
     * @param id the todo ID
     * @return true if a todo with this ID is stored
     */
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    /**
     * Returns the total number of stored todos.
     */
    public long count() {
        return store.size();
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Saves a new todo or updates an existing one.
     *
     * <p>If {@code todo.getId()} is null, a new ID is assigned (create).
     * If an ID is already set, the todo is stored under that ID (update/replace).
     *
     * @param todo the todo to save
     * @return the saved todo (with ID set if it was a create operation)
     */
    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            // Create: assign a new ID
            todo.setId(idGenerator.getAndIncrement());
        }
        store.put(todo.getId(), todo);
        return todo;
    }

    /**
     * Deletes a todo by its ID.
     * No-op if the ID does not exist.
     *
     * @param id the ID of the todo to delete
     */
    public void deleteById(Long id) {
        store.remove(id);
    }

    /**
     * Removes all todos. Useful for resetting state in tests.
     */
    public void deleteAll() {
        store.clear();
        idGenerator.set(1);
    }
}
