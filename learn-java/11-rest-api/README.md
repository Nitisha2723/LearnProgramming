# Module 11: Building REST APIs with Spring Boot

## Learning Objectives

By the end of this module you will be able to:

- Explain REST principles: stateless, uniform interface, resource-based URLs
- Choose the right HTTP method (GET/POST/PUT/PATCH/DELETE) for each operation
- Return the correct HTTP status code (200/201/204/400/401/403/404/409/500)
- Build a layered Spring Boot application (controller → service → repository)
- Write request and response DTOs using Java records
- Validate incoming requests with Jakarta Bean Validation (`@Valid`, `@NotBlank`, etc.)
- Handle errors globally with `@ControllerAdvice`
- Test REST controllers with `@WebMvcTest` and `MockMvc`
- Test your API manually with curl

---

## Prerequisites

- Java 21
- Maven 3.8+
- Module 10 (Database) or equivalent Spring basics
- Familiarity with Java records and streams

---

## Setup

### 1. Add Spring Boot dependencies

Add the following to your `pom.xml` (see `pom-additions.xml` for the full snippet):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.2.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    <version>3.2.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>3.2.0</version>
    <scope>test</scope>
</dependency>
```

### 2. Create the source directory structure

Copy the files from `code/` into:
```
src/main/java/com/example/todoapi/
src/main/resources/application.properties
src/test/java/com/example/todoapi/controller/
```

### 3. Create application.properties

Create `src/main/resources/application.properties`:
```properties
server.port=8080
spring.application.name=todo-api
spring.jackson.serialization.write-dates-as-timestamps=false
```

The `write-dates-as-timestamps=false` setting makes Jackson serialize `LocalDateTime` as `"2024-01-15T10:30:00"` instead of a numeric array.

### 4. Run the application

```bash
# With Maven
mvn spring-boot:run

# Or compile and run the JAR
mvn package
java -jar target/todo-api-1.0.0.jar
```

The app starts on `http://localhost:8080`. You'll see output like:
```
Started TodoApiApplication in 2.3 seconds
```

---

## Module Contents

```
11-rest-api/
├── theory/
│   ├── 01-rest-fundamentals.md    ← REST principles, HTTP methods, status codes, URL design
│   ├── 02-spring-boot-basics.md   ← @SpringBootApplication, @RestController, annotations
│   ├── 03-layers-and-structure.md ← Controller/Service/Repository, DTOs, validation
│   └── 04-testing-apis.md         ← @WebMvcTest, MockMvc, Postman, curl
├── code/
│   ├── TodoApiApplication.java         ← Entry point
│   ├── model/Todo.java                 ← Domain entity
│   ├── dto/
│   │   ├── CreateTodoRequest.java      ← Input DTO with validation
│   │   ├── UpdateTodoRequest.java      ← Update DTO
│   │   └── TodoResponse.java           ← Output DTO
│   ├── repository/TodoRepository.java  ← In-memory data store
│   ├── service/TodoService.java        ← Business logic
│   ├── controller/TodoController.java  ← HTTP endpoints
│   └── exception/
│       ├── TodoNotFoundException.java      ← Domain exception
│       └── GlobalExceptionHandler.java     ← @ControllerAdvice
├── tests/
│   └── TodoControllerTest.java        ← @WebMvcTest controller tests
├── exercises/
│   ├── Exercise01.md                  ← Build a BookStore API
│   └── solutions/                     ← Complete BookStore solution
├── mini-project/
│   └── README.md                      ← Build a Note-Taking API (spec)
├── pom-additions.xml                  ← Dependencies to add to pom.xml
└── README.md                          ← This file
```

---

## The Todo API

The code directory contains a complete, runnable Todo REST API. Once you have Spring Boot dependencies set up and the app running, these are all the endpoints:

### Endpoints

| Method | URL | Description | Status |
|--------|-----|-------------|--------|
| `GET` | `/api/v1/todos` | List all todos | 200 |
| `GET` | `/api/v1/todos?completed=true` | List completed todos | 200 |
| `GET` | `/api/v1/todos/{id}` | Get todo by ID | 200 / 404 |
| `POST` | `/api/v1/todos` | Create a todo | 201 |
| `PUT` | `/api/v1/todos/{id}` | Update a todo | 200 / 404 |
| `PATCH` | `/api/v1/todos/{id}/complete` | Mark todo complete | 200 / 404 / 409 |
| `DELETE` | `/api/v1/todos/{id}` | Delete a todo | 204 / 404 |

### Testing with curl

```bash
# Create a todo
curl -X POST http://localhost:8080/api/v1/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn Spring Boot", "description": "Build REST APIs"}'

# Expected: 201 Created
# {
#   "id": 1,
#   "title": "Learn Spring Boot",
#   "description": "Build REST APIs",
#   "completed": false,
#   "createdAt": "2024-01-15T10:30:00"
# }

# Get all todos
curl http://localhost:8080/api/v1/todos

# Get all completed todos
curl "http://localhost:8080/api/v1/todos?completed=true"

# Get todo by ID
curl http://localhost:8080/api/v1/todos/1

# Update a todo
curl -X PUT http://localhost:8080/api/v1/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn Spring Boot", "description": "Updated description", "completed": false}'

# Mark todo as complete
curl -X PATCH http://localhost:8080/api/v1/todos/1/complete

# Delete a todo
curl -X DELETE http://localhost:8080/api/v1/todos/1
# Expected: 204 No Content (empty body)

# Try to get deleted todo (should 404)
curl -i http://localhost:8080/api/v1/todos/1
# Expected: 404 Not Found + error body

# Test validation — blank title
curl -X POST http://localhost:8080/api/v1/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "", "description": "test"}'
# Expected: 400 Bad Request + {"errors": {"title": "must not be blank"}}

# Test validation — title too long
curl -X POST http://localhost:8080/api/v1/todos \
  -H "Content-Type: application/json" \
  -d "{\"title\": \"$(python3 -c 'print("A"*201)')\"}"
# Expected: 400 Bad Request
```

---

## Suggested Learning Order

1. Read `theory/01-rest-fundamentals.md` — understand the concepts first
2. Read `theory/02-spring-boot-basics.md` — learn the Spring annotations
3. Read `theory/03-layers-and-structure.md` — understand the architecture
4. Walk through the `code/` files in this order:
   - `model/Todo.java` — the domain object
   - `dto/CreateTodoRequest.java` and `dto/TodoResponse.java` — the API contract
   - `repository/TodoRepository.java` — data access
   - `service/TodoService.java` — business logic
   - `controller/TodoController.java` — HTTP endpoints
   - `exception/GlobalExceptionHandler.java` — error handling
5. Run the application and test it with curl (commands above)
6. Read `theory/04-testing-apis.md` — understand how to test REST APIs
7. Study `tests/TodoControllerTest.java` — see how @WebMvcTest works
8. Do `exercises/Exercise01.md` — build the BookStore API yourself
9. Do the `mini-project/` — build the Note-Taking API independently

---

## Key Concepts Summary

### URL Design
```
/api/v1/todos          ← collection resource (plural noun)
/api/v1/todos/1        ← single resource (ID in path)
/api/v1/todos?completed=true  ← filtering (query params)
/api/v1/todos/1/complete      ← sub-resource action
```

### HTTP Method → Action Mapping
```
GET    → read (safe, idempotent)
POST   → create (not idempotent)
PUT    → replace entirely (idempotent)
PATCH  → partial update
DELETE → remove (idempotent)
```

### Status Codes
```
200 OK          ← GET, PUT, PATCH success
201 Created     ← POST success (include Location header)
204 No Content  ← DELETE success
400 Bad Request ← validation failed
404 Not Found   ← resource doesn't exist
409 Conflict    ← business rule violation
500 Server Error ← unexpected failure
```

### Layer Responsibilities
```
Controller  ← HTTP in/out, validation, routing
Service     ← business logic, domain exceptions
Repository  ← data access, CRUD
```

---

## Common Mistakes

| Mistake | Correct Approach |
|---------|-----------------|
| `GET /getTodos` | `GET /todos` — HTTP method is the verb |
| Returning entity directly from controller | Use response DTOs |
| Business logic in the controller | Move to service layer |
| `@Autowired` field injection | Constructor injection |
| Returning `null` for not found | Throw `NotFoundException`, handle in `@ControllerAdvice` |
| POST returning 200 | POST should return 201 Created |
| DELETE returning the deleted item | DELETE returns 204 No Content |
| Exposing stack traces in error responses | Return generic messages, log internally |
