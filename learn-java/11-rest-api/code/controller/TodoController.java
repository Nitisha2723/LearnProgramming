package com.example.todoapi.controller;

import com.example.todoapi.dto.CreateTodoRequest;
import com.example.todoapi.dto.TodoResponse;
import com.example.todoapi.dto.UpdateTodoRequest;
import com.example.todoapi.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * REST controller for the Todo resource.
 *
 * <p>Endpoints exposed:
 * <pre>
 * GET    /api/v1/todos                  → list all (optional ?completed=true/false filter)
 * GET    /api/v1/todos/{id}             → get one by ID
 * POST   /api/v1/todos                  → create new (returns 201 Created)
 * PUT    /api/v1/todos/{id}             → replace entirely
 * PATCH  /api/v1/todos/{id}/complete    → mark as completed
 * DELETE /api/v1/todos/{id}             → delete (returns 204 No Content)
 * </pre>
 *
 * <p>This class is responsible ONLY for HTTP concerns:
 * <ul>
 *   <li>Routing HTTP requests to the right method</li>
 *   <li>Extracting data from the request (path vars, query params, body)</li>
 *   <li>Validating input ({@code @Valid})</li>
 *   <li>Returning appropriate HTTP responses ({@link ResponseEntity})</li>
 * </ul>
 *
 * <p>All business logic lives in {@link TodoService}. The controller never
 * calls the repository directly.
 */
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;

    // Constructor injection — makes this class easy to unit-test
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/todos
    // -------------------------------------------------------------------------

    /**
     * Returns all todos, optionally filtered by completion status.
     *
     * <p>Examples:
     * <pre>
     *   GET /api/v1/todos               → all todos
     *   GET /api/v1/todos?completed=true   → only completed todos
     *   GET /api/v1/todos?completed=false  → only incomplete todos
     * </pre>
     *
     * @param completed optional filter; null means "return all"
     * @return 200 OK with list of todos (empty list if none match)
     */
    @GetMapping
    public List<TodoResponse> getAllTodos(
            @RequestParam(required = false) Boolean completed) {
        return todoService.findAll(completed);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/todos/{id}
    // -------------------------------------------------------------------------

    /**
     * Returns a single todo by its ID.
     *
     * @param id the todo ID from the URL path
     * @return 200 OK with the todo
     *         404 Not Found if no todo with this ID exists (via GlobalExceptionHandler)
     */
    @GetMapping("/{id}")
    public TodoResponse getTodoById(@PathVariable Long id) {
        return todoService.findById(id);
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/todos
    // -------------------------------------------------------------------------

    /**
     * Creates a new todo.
     *
     * <p>{@code @Valid} triggers Jakarta Bean Validation on the request body.
     * If validation fails (blank title, etc.), Spring throws
     * {@code MethodArgumentNotValidException} before this method body runs.
     * The {@link com.example.todoapi.exception.GlobalExceptionHandler} converts it to 400.
     *
     * <p>Returns:
     * <ul>
     *   <li>201 Created with the newly created todo in the body</li>
     *   <li>Location header pointing to the new resource URL</li>
     * </ul>
     *
     * @param request validated create request from the JSON body
     * @return 201 Created with the created todo
     */
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @Valid @RequestBody CreateTodoRequest request) {
        TodoResponse created = todoService.create(request);

        // Best practice: include Location header pointing to the new resource
        URI location = URI.create("/api/v1/todos/" + created.id());
        return ResponseEntity
                .created(location)  // Sets status 201 + Location header
                .body(created);
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/todos/{id}
    // -------------------------------------------------------------------------

    /**
     * Replaces a todo entirely (PUT = full replacement).
     *
     * <p>All fields in the request body replace the existing todo's fields.
     * Missing optional fields are set to null/default (not preserved).
     *
     * @param id      the todo ID from the URL path
     * @param request the validated update request
     * @return 200 OK with the updated todo
     *         404 Not Found if the todo does not exist
     */
    @PutMapping("/{id}")
    public TodoResponse updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTodoRequest request) {
        return todoService.update(id, request);
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/todos/{id}/complete
    // -------------------------------------------------------------------------

    /**
     * Marks a todo as completed (PATCH = partial update).
     *
     * <p>This uses PATCH because it changes only one specific aspect of the
     * resource (its completion state), not the whole resource.
     *
     * <p>The sub-resource path {@code /complete} makes the intent very clear
     * compared to a generic PATCH on the resource.
     *
     * @param id the todo ID from the URL path
     * @return 200 OK with the updated todo
     *         404 Not Found if the todo does not exist
     *         400 Bad Request if the todo is already completed
     */
    @PatchMapping("/{id}/complete")
    public TodoResponse completeTodo(@PathVariable Long id) {
        return todoService.markComplete(id);
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/todos/{id}
    // -------------------------------------------------------------------------

    /**
     * Deletes a todo.
     *
     * <p>Returns 204 No Content on success — there is no body to return
     * since the resource no longer exists.
     *
     * @param id the todo ID from the URL path
     * @return 204 No Content
     *         404 Not Found if the todo does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();  // 204 No Content, no body
    }
}
