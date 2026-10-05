# Spring Boot Basics

## What is Spring Boot?

Spring Boot is an opinionated framework built on top of the Spring Framework. It eliminates the boilerplate configuration that made traditional Spring development tedious, letting you focus on writing business logic.

**Key features:**
- **Auto-configuration**: Spring Boot detects libraries on the classpath and configures them automatically. Add `spring-boot-starter-web` and you get a fully configured embedded Tomcat, Jackson (JSON), and Spring MVC — no XML configuration needed.
- **Embedded server**: Tomcat is bundled inside your JAR. You run `java -jar myapp.jar` and you have a server — no separate server installation required.
- **Starter POMs**: `spring-boot-starter-*` dependencies bundle everything needed for a particular feature (web, data, security, etc.).
- **Production-ready**: Actuator endpoints (`/health`, `/metrics`) are available out of the box.

### Spring vs Spring Boot

| Spring Framework | Spring Boot |
|-----------------|-------------|
| Requires explicit configuration | Auto-configures based on classpath |
| Deploy to external server | Embedded server in the JAR |
| Complex XML or Java config | Minimal config via `application.properties` |
| Full control | Conventions over configuration |

---

## Project Structure

A typical Spring Boot REST API follows this layout:

```
src/
├── main/
│   ├── java/
│   │   └── com/example/todoapi/
│   │       ├── TodoApiApplication.java      ← main class
│   │       ├── controller/
│   │       │   └── TodoController.java
│   │       ├── service/
│   │       │   └── TodoService.java
│   │       ├── repository/
│   │       │   └── TodoRepository.java
│   │       ├── model/
│   │       │   └── Todo.java
│   │       ├── dto/
│   │       │   ├── CreateTodoRequest.java
│   │       │   └── TodoResponse.java
│   │       └── exception/
│   │           ├── GlobalExceptionHandler.java
│   │           └── TodoNotFoundException.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/example/todoapi/
            └── controller/
                └── TodoControllerTest.java
```

---

## The Entry Point: @SpringBootApplication

```java
package com.example.todoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoApiApplication.class, args);
    }
}
```

`@SpringBootApplication` is a convenience annotation combining three annotations:

| Annotation | Purpose |
|-----------|---------|
| `@Configuration` | Marks this class as a source of Spring bean definitions |
| `@EnableAutoConfiguration` | Tells Spring Boot to configure beans based on classpath |
| `@ComponentScan` | Scans the package (and subpackages) for components |

---

## @RestController

`@RestController` marks a class as a web controller where every method returns data (serialized to JSON) rather than a view name.

It combines:
- `@Controller` — registers the class as a Spring MVC controller
- `@ResponseBody` — serializes return values to JSON automatically

```java
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {
    // all methods in this class return JSON
}
```

`@RequestMapping("/api/v1/todos")` sets the base URL for all methods in this controller.

---

## Mapping HTTP Methods

Spring provides dedicated annotations for each HTTP method:

```java
@GetMapping("/api/v1/todos")          // GET
@PostMapping("/api/v1/todos")         // POST
@PutMapping("/api/v1/todos/{id}")     // PUT
@PatchMapping("/api/v1/todos/{id}")   // PATCH
@DeleteMapping("/api/v1/todos/{id}")  // DELETE
```

These are shortcuts for `@RequestMapping(method = RequestMethod.GET)` etc.

### Full Example

```java
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    @GetMapping
    public List<TodoResponse> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TodoResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse created = service.create(request);
        URI location = URI.create("/api/v1/todos/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TodoResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTodoRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/complete")
    public TodoResponse markComplete(@PathVariable Long id) {
        return service.markComplete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Extracting Data from Requests

### @PathVariable — URL path segments

```java
// URL: GET /api/v1/todos/42
@GetMapping("/{id}")
public TodoResponse getById(@PathVariable Long id) {
    // id = 42
}

// Multiple path variables
// URL: GET /api/v1/users/5/orders/12
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(@PathVariable Long userId, @PathVariable Long orderId) { }
```

### @RequestParam — query parameters

```java
// URL: GET /api/v1/todos?completed=true&page=2
@GetMapping
public List<TodoResponse> getAll(
        @RequestParam(required = false) Boolean completed,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    // completed = true, page = 2, size = 20 (default)
}
```

### @RequestBody — JSON request body

```java
// Body: {"title": "Learn Spring", "description": "..."}
@PostMapping
public ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
    // Spring deserializes the JSON body into CreateTodoRequest
    // @Valid triggers bean validation
}
```

### @RequestHeader — HTTP headers

```java
@GetMapping
public List<TodoResponse> getAll(
        @RequestHeader("Authorization") String authHeader,
        @RequestHeader(value = "Accept-Language", required = false) String lang) { }
```

---

## ResponseEntity<T>

`ResponseEntity<T>` gives you full control over the HTTP response: status code, headers, and body.

```java
// Return 200 OK with body (equivalent to just returning the object)
return ResponseEntity.ok(todo);

// Return 201 Created with Location header and body
URI location = URI.create("/api/v1/todos/" + created.id());
return ResponseEntity.created(location).body(created);

// Return 204 No Content (no body)
return ResponseEntity.noContent().build();

// Return 404 Not Found with error body
return ResponseEntity.notFound().build();

// Return 400 Bad Request with custom body
return ResponseEntity.badRequest().body(errorDetails);

// Custom status code
return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
```

When you just want to return data with 200 OK, you can skip `ResponseEntity` and return the object directly. Spring wraps it in a 200 OK response automatically.

```java
// These are equivalent for happy-path GET:
@GetMapping("/{id}")
public TodoResponse getById(@PathVariable Long id) {  // 200 OK implicitly
    return service.findById(id);
}

@GetMapping("/{id}")
public ResponseEntity<TodoResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));  // 200 OK explicitly
}
```

Use `ResponseEntity` when you need a non-200 status, custom headers, or conditional responses.

---

## application.properties

Configuration lives in `src/main/resources/application.properties`:

```properties
# Server
server.port=8080
server.servlet.context-path=/

# Application name (shows in logs)
spring.application.name=todo-api

# Jackson (JSON serialization)
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.default-property-inclusion=non_null

# Logging
logging.level.root=INFO
logging.level.com.example.todoapi=DEBUG

# Actuator
management.endpoints.web.exposure.include=health,info
```

Or use YAML format (`application.yml`):
```yaml
server:
  port: 8080

spring:
  application:
    name: todo-api
  jackson:
    serialization:
      write-dates-as-timestamps: false

logging:
  level:
    com.example.todoapi: DEBUG
```

---

## Common Annotations Cheat Sheet

| Annotation | Package | Purpose |
|-----------|---------|---------|
| `@SpringBootApplication` | `org.springframework.boot.autoconfigure` | Entry point, enables auto-config + component scan |
| `@RestController` | `org.springframework.web.bind.annotation` | Marks class as REST controller |
| `@RequestMapping` | `org.springframework.web.bind.annotation` | Maps URL prefix to a controller |
| `@GetMapping` | `org.springframework.web.bind.annotation` | Maps GET request to method |
| `@PostMapping` | `org.springframework.web.bind.annotation` | Maps POST request to method |
| `@PutMapping` | `org.springframework.web.bind.annotation` | Maps PUT request to method |
| `@PatchMapping` | `org.springframework.web.bind.annotation` | Maps PATCH request to method |
| `@DeleteMapping` | `org.springframework.web.bind.annotation` | Maps DELETE request to method |
| `@PathVariable` | `org.springframework.web.bind.annotation` | Binds URL path segment to method parameter |
| `@RequestParam` | `org.springframework.web.bind.annotation` | Binds query parameter to method parameter |
| `@RequestBody` | `org.springframework.web.bind.annotation` | Deserializes JSON body to method parameter |
| `@Valid` | `jakarta.validation` | Triggers bean validation on the annotated parameter |
| `@Service` | `org.springframework.stereotype` | Marks class as service layer bean |
| `@Repository` | `org.springframework.stereotype` | Marks class as data access layer bean |
| `@Component` | `org.springframework.stereotype` | Generic Spring-managed component |
| `@Bean` | `org.springframework.context.annotation` | Marks method as producing a Spring bean |

---

## Auto-Configuration in Practice

When you add `spring-boot-starter-web` to your classpath, Spring Boot automatically configures:

1. **DispatcherServlet** — the front controller that routes requests to your `@RestController` methods
2. **Jackson ObjectMapper** — serializes/deserializes Java objects to/from JSON
3. **Embedded Tomcat** on port 8080
4. **Error handling** — default `/error` endpoint
5. **Content negotiation** — determines response format based on `Accept` header

You override auto-configuration by declaring your own bean of the same type, or via `application.properties`.

---

## How a Request Flows Through Spring Boot

```
HTTP Request
    ↓
Embedded Tomcat (listens on :8080)
    ↓
DispatcherServlet (Spring MVC front controller)
    ↓
HandlerMapping (finds @RestController method by URL + HTTP method)
    ↓
HandlerInterceptors (if any — auth, logging, rate limiting)
    ↓
@RestController method executes
    ↓
Return value serialized to JSON by Jackson
    ↓
HTTP Response sent back to client
```

---

## Summary

- Spring Boot = Spring Framework + auto-configuration + embedded server
- `@SpringBootApplication` bootstraps everything
- `@RestController` + `@GetMapping/@PostMapping/...` expose HTTP endpoints
- `@PathVariable` extracts path segments, `@RequestParam` extracts query params, `@RequestBody` deserializes the JSON body
- `ResponseEntity<T>` gives full control over the HTTP response
- `application.properties` configures the application without code
