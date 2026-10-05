# Layers and Structure

## Why Layered Architecture?

A well-structured Spring Boot application separates concerns into distinct layers. Each layer has a single responsibility and communicates only with adjacent layers.

```
HTTP Request
     ↓
┌─────────────────────────────────┐
│        Controller Layer         │  ← Handles HTTP, validates input, formats response
│   (@RestController)             │
└─────────────────────────────────┘
     ↓ calls
┌─────────────────────────────────┐
│         Service Layer           │  ← Business logic, orchestrates work
│       (@Service)                │
└─────────────────────────────────┘
     ↓ calls
┌─────────────────────────────────┐
│       Repository Layer          │  ← Data access (CRUD operations)
│      (@Repository)              │
└─────────────────────────────────┘
     ↓ reads/writes
┌─────────────────────────────────┐
│      Database / Data Store      │
└─────────────────────────────────┘
```

**Benefits:**
- **Testability**: each layer can be tested in isolation (mock the layer below)
- **Replaceability**: swap the repository (in-memory → JPA → MongoDB) without touching the controller
- **Clarity**: you always know where to find business logic (service), HTTP handling (controller), or data access (repository)

---

## Controller Layer

**Responsibility**: Handle HTTP concerns only. It should:
- Accept and validate HTTP requests
- Call the service layer
- Return appropriate HTTP responses (status codes, headers)
- Map DTOs — never pass raw entities to the service or return them to clients

**What it should NOT do:**
- Contain business logic
- Interact directly with the database
- Know about SQL, JPA, or any persistence mechanism

```java
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;

    // Constructor injection (preferred — see below)
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<TodoResponse> getAll(@RequestParam(required = false) Boolean completed) {
        return todoService.findAll(completed);  // Delegate to service
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoResponse create(@Valid @RequestBody CreateTodoRequest request) {
        return todoService.create(request);  // Delegate to service
    }
}
```

---

## Service Layer

**Responsibility**: Implement business logic. It should:
- Orchestrate operations across repositories
- Enforce business rules (e.g., "a completed todo cannot be updated")
- Throw business exceptions (`TodoNotFoundException`, `TodoAlreadyCompletedException`)
- Handle transactions (with `@Transactional` when using a real database)

**What it should NOT do:**
- Know about HTTP (no `HttpServletRequest`, no `ResponseEntity`)
- Directly manipulate raw HTTP headers or status codes

```java
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoResponse> findAll(Boolean completed) {
        return todoRepository.findAll().stream()
                .filter(todo -> completed == null || todo.isCompleted() == completed)
                .map(this::toResponse)
                .toList();
    }

    public TodoResponse findById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        return toResponse(todo);
    }

    public TodoResponse create(CreateTodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.title());
        todo.setDescription(request.description());
        todo.setCompleted(false);
        todo.setCreatedAt(LocalDateTime.now());
        Todo saved = todoRepository.save(todo);
        return toResponse(saved);
    }

    // Business logic: mark as complete is a domain operation
    public TodoResponse markComplete(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        if (todo.isCompleted()) {
            throw new IllegalStateException("Todo is already completed");
        }
        todo.setCompleted(true);
        return toResponse(todoRepository.save(todo));
    }

    private TodoResponse toResponse(Todo todo) {
        return new TodoResponse(todo.getId(), todo.getTitle(), 
                                todo.getDescription(), todo.isCompleted(), 
                                todo.getCreatedAt());
    }
}
```

---

## Repository Layer

**Responsibility**: Data access — CRUD operations against the data store.

When using Spring Data JPA:
```java
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByCompleted(boolean completed);
    List<Todo> findByTitleContainingIgnoreCase(String keyword);
}
```

For our in-memory implementation:
```java
@Repository
public class TodoRepository {

    private final Map<Long, Todo> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Todo> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Todo> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            todo.setId(idGenerator.getAndIncrement());
        }
        store.put(todo.getId(), todo);
        return todo;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
```

---

## Dependency Injection

Spring manages object creation and wiring. When Spring starts, it scans for `@Component`, `@Service`, `@Repository`, `@Controller` annotations, creates instances (beans), and injects them where needed.

### Three Ways to Inject (constructor is best)

**1. Constructor Injection (recommended)**
```java
@Service
public class TodoService {

    private final TodoRepository todoRepository;  // final = immutable after construction

    // Spring calls this constructor and injects TodoRepository
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }
}
```

Why constructor injection is best:
- `final` fields — object is always in a valid state
- Easy to test (just `new TodoService(mockRepo)` in tests)
- Obvious dependencies (listed in constructor signature)
- Catches circular dependencies at startup

**2. Field Injection (avoid)**
```java
@Service
public class TodoService {

    @Autowired  // Avoid: hides dependencies, hard to test, not null-safe
    private TodoRepository todoRepository;
}
```

**3. Setter Injection (for optional dependencies only)**
```java
@Service
public class TodoService {

    private MetricsService metricsService;  // optional

    @Autowired(required = false)
    public void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }
}
```

---

## DTOs — Data Transfer Objects

DTOs are objects specifically designed for transferring data between layers (or to/from the client). They are NOT the same as your database entities.

### Why Use DTOs Instead of Entities?

| Entity (internal) | DTO (external) |
|------------------|----------------|
| Maps to database table | Shaped for API consumers |
| May contain sensitive fields (passwordHash, internalNotes) | Exposes only what clients need |
| May have JPA annotations | Plain data, no framework annotations |
| May have circular references | Flat, serialization-safe |
| Tracks all database columns | May aggregate data from multiple sources |

### Example: The Problem Without DTOs

```java
// BAD: Exposing entity directly
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).get();
    // EXPOSES: passwordHash, creditCardNumber, internalAdminNotes, ...
}
```

### Request DTO (Java Record — preferred for immutable input)

```java
public record CreateTodoRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description
) {}
```

### Response DTO (Record — preferred for immutable output)

```java
public record TodoResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt
) {}
```

---

## Validation with Bean Validation (Jakarta Validation)

Spring Boot integrates with Jakarta Bean Validation. Annotate DTO fields with constraints and add `@Valid` to controller methods.

### Common Constraint Annotations

| Annotation | Description |
|-----------|-------------|
| `@NotNull` | Field must not be null |
| `@NotBlank` | String must not be null or empty (trims whitespace) |
| `@NotEmpty` | String/collection must not be null or empty (does NOT trim) |
| `@Size(min, max)` | String/collection length within bounds |
| `@Min(value)` | Number must be ≥ value |
| `@Max(value)` | Number must be ≤ value |
| `@Email` | Must be a valid email address |
| `@Pattern(regexp)` | Must match the regex |
| `@Positive` | Number must be > 0 |
| `@PositiveOrZero` | Number must be ≥ 0 |
| `@Past` | Date must be in the past |
| `@Future` | Date must be in the future |

### Applying Validation

```java
public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank @Email(message = "Must be a valid email")
        String email,

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
        String password,

        @Min(value = 0, message = "Age must be non-negative")
        @Max(value = 150)
        Integer age
) {}
```

```java
@PostMapping
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    // @Valid triggers validation — if any constraint fails,
    // Spring throws MethodArgumentNotValidException before this method body runs
    return ResponseEntity.created(...).body(userService.create(request));
}
```

### Nested Validation

```java
public record CreateOrderRequest(
        @NotNull
        Long customerId,

        @NotEmpty(message = "Order must have at least one item")
        @Valid  // Cascade validation to each item
        List<OrderItemRequest> items
) {}

public record OrderItemRequest(
        @NotNull Long productId,
        @Positive int quantity
) {}
```

---

## Exception Handling with @ControllerAdvice

`@ControllerAdvice` is a global exception handler. It intercepts exceptions thrown anywhere in your controllers and converts them into consistent HTTP responses.

### Without @ControllerAdvice

Each controller method would need try-catch blocks — messy and repetitive.

### With @ControllerAdvice

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle our custom "not found" exception → 404
    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TodoNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Handle validation failures → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        ValidationErrorResponse response = new ValidationErrorResponse(400, "Bad Request", errors);
        return ResponseEntity.badRequest().body(response);
    }

    // Catch-all for unexpected exceptions → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse(500, "Internal Server Error",
                "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Custom Exception

```java
public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(Long id) {
        super("Todo with id " + id + " not found");
    }
}
```

### Error Response DTO

```java
public record ErrorResponse(
        int status,
        String error,
        String message
) {}
```

---

## The Full Picture: Request Lifecycle

```
POST /api/v1/todos
{"title": "", "description": "test"}
    ↓
Controller receives request
    ↓
@Valid triggers validation on @RequestBody
    ↓ (title is blank — constraint violation!)
MethodArgumentNotValidException is thrown
    ↓
GlobalExceptionHandler.handleValidation() catches it
    ↓
Returns: 400 Bad Request
{
  "status": 400,
  "error": "Bad Request",
  "errors": {"title": "must not be blank"}
}
```

```
GET /api/v1/todos/999
    ↓
Controller calls service.findById(999)
    ↓
Service calls repository.findById(999)
    ↓
Repository returns Optional.empty()
    ↓
Service throws TodoNotFoundException("Todo with id 999 not found")
    ↓
GlobalExceptionHandler.handleNotFound() catches it
    ↓
Returns: 404 Not Found
{
  "status": 404,
  "error": "Not Found",
  "message": "Todo with id 999 not found"
}
```

---

## Summary

| Layer | Annotation | Responsibility |
|-------|-----------|---------------|
| Controller | `@RestController` | HTTP in/out, validation, routing |
| Service | `@Service` | Business logic, business rules |
| Repository | `@Repository` | Data access (CRUD) |

**Key principles:**
1. Layers only call downward (controller → service → repository)
2. Pass DTOs between controller and service, entities between service and repository
3. Use constructor injection for all dependencies
4. Throw domain exceptions in the service layer, translate them to HTTP responses in `@ControllerAdvice`
5. Validate at the controller boundary with `@Valid`
