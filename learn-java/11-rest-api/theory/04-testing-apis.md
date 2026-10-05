# Testing REST APIs

## Testing Strategy

A good REST API test suite has multiple layers:

```
┌────────────────────────────────────────────────────────┐
│               End-to-End Tests                         │  ← Postman, curl, RestAssured
│         (Full stack, real HTTP calls)                  │   Few, slow, expensive
├────────────────────────────────────────────────────────┤
│            Integration Tests                           │  ← @SpringBootTest
│      (Spring context + real DB or test DB)             │   Some, moderate speed
├────────────────────────────────────────────────────────┤
│             Slice Tests                                │  ← @WebMvcTest
│     (Controller layer only, mocked service)            │   Many, fast
├────────────────────────────────────────────────────────┤
│              Unit Tests                                │  ← Plain JUnit
│       (Service, repository, pure logic)                │   Most, very fast
└────────────────────────────────────────────────────────┘
```

---

## @SpringBootTest — Full Integration Tests

`@SpringBootTest` loads the **complete Spring application context**. It's slow (starts the whole app) but tests the real interaction between all layers.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TodoApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createTodo_andRetrieveIt() {
        // Create
        CreateTodoRequest request = new CreateTodoRequest("Learn Testing", "Write tests");
        ResponseEntity<TodoResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/todos", request, TodoResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = createResponse.getBody().id();

        // Retrieve
        ResponseEntity<TodoResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/todos/" + id, TodoResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().title()).isEqualTo("Learn Testing");
    }
}
```

**`WebEnvironment` options:**
- `RANDOM_PORT` — starts a real server on a random port (avoids port conflicts)
- `DEFINED_PORT` — starts on `server.port` from application.properties
- `MOCK` — (default) creates a mock web environment, no real server started

---

## @WebMvcTest — Controller Slice Tests

`@WebMvcTest` loads **only the controller layer** and its Spring MVC infrastructure. The service and repository are NOT loaded — you mock them with `@MockBean`.

**Advantages:**
- Much faster than `@SpringBootTest` (no full context)
- Tests controller in isolation (routing, validation, serialization)
- Forces you to specify what the service does in each test

```java
@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;  // For serializing request bodies
}
```

---

## MockMvc — Simulated HTTP Calls

`MockMvc` simulates HTTP requests without starting a real server. Requests go directly through the Spring MVC infrastructure (routing, filters, serialization).

### Basic Pattern

```java
mockMvc.perform(/* HTTP request */)
       .andExpect(/* assertion */)
       .andExpect(/* another assertion */);
```

### GET Request

```java
@Test
void getAllTodos_returnsEmptyList() throws Exception {
    // Arrange: stub the service
    when(todoService.findAll(null)).thenReturn(List.of());

    // Act + Assert
    mockMvc.perform(get("/api/v1/todos"))
           .andExpect(status().isOk())
           .andExpect(content().contentType(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$").isArray())
           .andExpect(jsonPath("$.length()").value(0));
}
```

### POST Request

```java
@Test
void createTodo_returnsCreated() throws Exception {
    // Arrange
    CreateTodoRequest request = new CreateTodoRequest("Learn Spring", "Build APIs");
    TodoResponse response = new TodoResponse(1L, "Learn Spring", "Build APIs", false, 
                                              LocalDateTime.now());
    when(todoService.create(any())).thenReturn(response);

    // Act + Assert
    mockMvc.perform(post("/api/v1/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(1))
           .andExpect(jsonPath("$.title").value("Learn Spring"))
           .andExpect(jsonPath("$.completed").value(false));
}
```

### DELETE Request

```java
@Test
void deleteTodo_returns204() throws Exception {
    doNothing().when(todoService).delete(1L);

    mockMvc.perform(delete("/api/v1/todos/1"))
           .andExpect(status().isNoContent());
}
```

---

## MockMvc Methods Reference

### perform() — HTTP methods

```java
mockMvc.perform(get("/api/v1/todos"))
mockMvc.perform(post("/api/v1/todos").contentType(...).content(...))
mockMvc.perform(put("/api/v1/todos/1").contentType(...).content(...))
mockMvc.perform(patch("/api/v1/todos/1/complete"))
mockMvc.perform(delete("/api/v1/todos/1"))
```

### Request modifiers

```java
.contentType(MediaType.APPLICATION_JSON)              // Content-Type header
.content(objectMapper.writeValueAsString(obj))        // Request body
.header("Authorization", "Bearer " + token)           // Custom header
.param("completed", "true")                           // Query parameter
.accept(MediaType.APPLICATION_JSON)                   // Accept header
```

### andExpect() — Assertions

```java
// Status
.andExpect(status().isOk())                          // 200
.andExpect(status().isCreated())                     // 201
.andExpect(status().isNoContent())                   // 204
.andExpect(status().isBadRequest())                  // 400
.andExpect(status().isNotFound())                    // 404
.andExpect(status().is(409))                         // any code

// Content type
.andExpect(content().contentType(MediaType.APPLICATION_JSON))

// JSON path assertions (see below)
.andExpect(jsonPath("$.id").value(1))
.andExpect(jsonPath("$.title").value("Learn Spring"))
.andExpect(jsonPath("$").isArray())
.andExpect(jsonPath("$.length()").value(3))
.andExpect(jsonPath("$[0].id").value(1))
```

### andReturn() — extracting the response

```java
MvcResult result = mockMvc.perform(get("/api/v1/todos/1"))
        .andExpect(status().isOk())
        .andReturn();

String json = result.getResponse().getContentAsString();
TodoResponse todo = objectMapper.readValue(json, TodoResponse.class);
assertThat(todo.title()).isEqualTo("Learn Spring");
```

---

## JSONPath

JSONPath is an expression language for navigating JSON. In tests, `jsonPath("expression")` lets you assert specific values.

```java
// JSON: {"id": 1, "title": "Learn Spring", "completed": false, "tags": ["java", "spring"]}
jsonPath("$.id").value(1)                        // root-level field
jsonPath("$.title").value("Learn Spring")
jsonPath("$.completed").value(false)
jsonPath("$.tags[0]").value("java")              // first array element
jsonPath("$.tags.length()").value(2)             // array length

// JSON: [{"id": 1}, {"id": 2}, {"id": 3}]
jsonPath("$").isArray()                          // is array
jsonPath("$.length()").value(3)                  // array length
jsonPath("$[0].id").value(1)                     // first element's id
jsonPath("$[*].completed").value(everyItem(false)) // all items have completed=false

// Existence checks
jsonPath("$.id").exists()
jsonPath("$.internalField").doesNotExist()
```

---

## Complete Test Class Example

```java
@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTodos_returnsEmptyList() throws Exception {
        when(todoService.findAll(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/todos"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllTodos_withCompletedFilter_returnsFilteredList() throws Exception {
        TodoResponse completed = new TodoResponse(1L, "Done task", null, true, LocalDateTime.now());
        when(todoService.findAll(true)).thenReturn(List.of(completed));

        mockMvc.perform(get("/api/v1/todos").param("completed", "true"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void createTodo_returnsCreated() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest("Learn Spring", "Build REST APIs");
        TodoResponse response = new TodoResponse(1L, "Learn Spring", "Build REST APIs", 
                                                  false, LocalDateTime.now());
        when(todoService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.title").value("Learn Spring"))
               .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void createTodo_withBlankTitle_returns400() throws Exception {
        String badRequest = """
                {
                  "title": "",
                  "description": "some description"
                }
                """;

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequest))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void getTodoById_notFound_returns404() throws Exception {
        when(todoService.findById(999L))
                .thenThrow(new TodoNotFoundException(999L));

        mockMvc.perform(get("/api/v1/todos/999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").value("Todo with id 999 not found"));
    }

    @Test
    void deleteTodo_returns204() throws Exception {
        doNothing().when(todoService).delete(1L);

        mockMvc.perform(delete("/api/v1/todos/1"))
               .andExpect(status().isNoContent());
    }
}
```

---

## Testing with Postman

Postman is a GUI tool for manually testing APIs.

### Basic Usage

1. **Create a request**: Select method (GET/POST/...), enter URL
2. **For POST/PUT**: Go to Body → raw → JSON, enter the JSON body
3. **Send**: Click Send, inspect response

### Postman Collection Example

```
Todo API Collection
├── GET    http://localhost:8080/api/v1/todos
├── GET    http://localhost:8080/api/v1/todos/1
├── POST   http://localhost:8080/api/v1/todos
│          Body: {"title": "Learn Spring", "description": "..."}
├── PUT    http://localhost:8080/api/v1/todos/1
│          Body: {"title": "Updated", "description": "...", "completed": false}
├── PATCH  http://localhost:8080/api/v1/todos/1/complete
└── DELETE http://localhost:8080/api/v1/todos/1
```

### Environment Variables in Postman
```
baseUrl = http://localhost:8080
→ Use {{baseUrl}}/api/v1/todos in requests
```

---

## Testing with curl

curl is a command-line tool for making HTTP requests — great for quick testing in a terminal.

### GET Requests

```bash
# Get all todos
curl http://localhost:8080/api/v1/todos

# Get all todos — pretty-print JSON (requires jq)
curl http://localhost:8080/api/v1/todos | jq .

# Get with query parameter
curl "http://localhost:8080/api/v1/todos?completed=true"

# Get by ID
curl http://localhost:8080/api/v1/todos/1

# Show response headers
curl -i http://localhost:8080/api/v1/todos/1
```

### POST — Create

```bash
curl -X POST http://localhost:8080/api/v1/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn Spring Boot", "description": "Build REST APIs"}'
```

### PUT — Update

```bash
curl -X PUT http://localhost:8080/api/v1/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated Title", "description": "Updated description", "completed": false}'
```

### PATCH — Partial Update

```bash
curl -X PATCH http://localhost:8080/api/v1/todos/1/complete
```

### DELETE

```bash
curl -X DELETE http://localhost:8080/api/v1/todos/1

# Show status code
curl -o /dev/null -s -w "%{http_code}\n" -X DELETE http://localhost:8080/api/v1/todos/1
```

### Useful curl Flags

| Flag | Purpose |
|------|---------|
| `-X METHOD` | HTTP method (default: GET) |
| `-H "Header: value"` | Add request header |
| `-d 'body'` | Request body |
| `-i` | Show response headers |
| `-s` | Silent mode (no progress bar) |
| `-o /dev/null` | Discard response body |
| `-w "%{http_code}"` | Print status code after request |
| `-v` | Verbose (show full request and response) |

---

## Testing Tips

1. **Test the happy path first**: verify the expected 200/201/204 responses with valid input
2. **Test validation**: verify 400 responses for each invalid field
3. **Test not found**: verify 404 for non-existent IDs
4. **Test the response body**: don't just check status codes — verify the JSON shape
5. **Use descriptive test names**: `createTodo_withBlankTitle_returns400` tells a story
6. **One assertion per test (ideally)**: easier to pinpoint what broke
7. **Mock the layer below**: in `@WebMvcTest`, mock the service; in service tests, mock the repository

---

## Summary

| Tool | Layer tested | Speed | Use case |
|------|-------------|-------|----------|
| `@WebMvcTest` + `MockMvc` | Controller only | Fast | Unit-level controller tests |
| `@SpringBootTest` | Full stack | Slow | Integration tests |
| Postman | Real HTTP | Manual | Exploratory / manual testing |
| curl | Real HTTP | Manual | Quick checks, scripting |

The most valuable test type for a REST API is `@WebMvcTest` — it's fast, catches routing issues, validates serialization, and confirms your validation annotations work correctly.
