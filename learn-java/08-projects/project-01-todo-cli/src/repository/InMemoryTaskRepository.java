package repository;

import model.Priority;
import model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link TaskRepository}.
 *
 * Uses a {@link ConcurrentHashMap} so the repository is safe to access
 * from multiple threads without external synchronization on the caller side.
 *
 * All returned lists are copies — mutating them does not affect stored data.
 */
public class InMemoryTaskRepository implements TaskRepository {

    private final ConcurrentHashMap<UUID, Task> store = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // TaskRepository implementation
    // -------------------------------------------------------------------------

    @Override
    public List<Task> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    @Override
    public Optional<Task> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Task> findByCompleted(boolean completed) {
        return store.values().stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Task> findByPriority(Priority priority) {
        if (priority == null) {
            return Collections.emptyList();
        }
        return store.values().stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Task save(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Cannot save a null task.");
        }
        if (store.containsKey(task.getId())) {
            throw new IllegalArgumentException(
                    "A task with id " + task.getId() + " already exists. Use update() instead.");
        }
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public Task update(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Cannot update a null task.");
        }
        if (!store.containsKey(task.getId())) {
            throw new NoSuchElementException(
                    "No task found with id " + task.getId() + ". Cannot update.");
        }
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public boolean delete(UUID id) {
        if (id == null) {
            return false;
        }
        return store.remove(id) != null;
    }

    @Override
    public int count() {
        return store.size();
    }
}
