package com.example.todoapi.service;

import com.example.todoapi.dto.CreateTodoRequest;
import com.example.todoapi.dto.TodoResponse;
import com.example.todoapi.dto.UpdateTodoRequest;
import com.example.todoapi.exception.TodoNotFoundException;
import com.example.todoapi.model.Todo;
import com.example.todoapi.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for Todo business logic.
 *
 * <p>The service layer sits between the controller and the repository:
 * <ul>
 *   <li>Controller delegates to the service (never calls repository directly)</li>
 *   <li>Service calls the repository for data access</li>
 * </ul>
 *
 * <p>This layer is responsible for:
 * <ul>
 *   <li>Implementing business rules</li>
 *   <li>Orchestrating operations (e.g., calling multiple repos, sending events)</li>
 *   <li>Throwing meaningful domain exceptions ({@link TodoNotFoundException})</li>
 *   <li>Mapping between domain entities and DTOs</li>
 * </ul>
 *
 * <p>The {@code @Service} annotation marks this as a Spring-managed bean.
 * Constructor injection is used — no {@code @Autowired} field injection.
 * This makes the class easy to unit test: {@code new TodoService(mockRepo)}.
 */
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    // Constructor injection — the preferred way to inject dependencies.
    // Spring automatically injects TodoRepository when creating this bean.
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Returns all todos, optionally filtered by completion status.
     *
     * @param completed if non-null, only returns todos matching this status
     * @return list of todo responses
     */
    public List<TodoResponse> findAll(Boolean completed) {
        return todoRepository.findAll().stream()
                // Filter by completed status if the parameter was provided
                .filter(todo -> completed == null || todo.isCompleted() == completed)
                .map(TodoResponse::from)
                .toList();
    }

    /**
     * Finds a todo by its ID.
     *
     * @param id the todo ID
     * @return the todo response
     * @throws TodoNotFoundException if no todo with the given ID exists
     */
    public TodoResponse findById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        return TodoResponse.from(todo);
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Creates a new todo from the given request.
     *
     * <p>The service sets the creation timestamp and initial completed state.
     * This is business logic — not the controller's concern.
     *
     * @param request the validated create request DTO
     * @return the created todo as a response DTO
     */
    public TodoResponse create(CreateTodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setCompleted(false);          // New todos are always incomplete
        todo.setCreatedAt(LocalDateTime.now());

        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    /**
     * Replaces a todo entirely (PUT semantics).
     *
     * <p>All fields in the request overwrite the existing todo. The creation
     * timestamp is preserved because it is not part of the update contract.
     *
     * @param id      the ID of the todo to update
     * @param request the update request with new field values
     * @return the updated todo as a response DTO
     * @throws TodoNotFoundException if no todo with the given ID exists
     */
    public TodoResponse update(Long id, UpdateTodoRequest request) {
        Todo existing = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));

        // Replace all mutable fields (PUT = full replacement)
        existing.setTitle(request.title());
        existing.setDescription(request.description());
        existing.setCompleted(request.completed());
        // Note: createdAt is NOT updated — it is immutable once set

        Todo saved = todoRepository.save(existing);
        return TodoResponse.from(saved);
    }

    /**
     * Marks a todo as completed (PATCH semantics — partial update).
     *
     * <p>This is a domain operation — "completing a todo" is a business concept,
     * not just a field change. Encapsulating it in the service makes the intent clear.
     *
     * @param id the ID of the todo to complete
     * @return the updated todo as a response DTO
     * @throws TodoNotFoundException if no todo with the given ID exists
     * @throws IllegalStateException if the todo is already completed
     */
    public TodoResponse markComplete(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));

        if (todo.isCompleted()) {
            throw new IllegalStateException("Todo with id " + id + " is already completed");
        }

        todo.setCompleted(true);
        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    /**
     * Deletes a todo by its ID.
     *
     * @param id the ID of the todo to delete
     * @throws TodoNotFoundException if no todo with the given ID exists
     */
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new TodoNotFoundException(id);
        }
        todoRepository.deleteById(id);
    }
}
