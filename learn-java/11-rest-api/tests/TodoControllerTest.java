package com.example.todoapi.controller;

import com.example.todoapi.dto.CreateTodoRequest;
import com.example.todoapi.dto.TodoResponse;
import com.example.todoapi.dto.UpdateTodoRequest;
import com.example.todoapi.exception.GlobalExceptionHandler;
import com.example.todoapi.exception.TodoNotFoundException;
import com.example.todoapi.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for {@link TodoController}.
 *
 * <p>{@code @WebMvcTest(TodoController.class)} loads ONLY:
 * <ul>
 *   <li>The specified controller ({@link TodoController})</li>
 *   <li>Spring MVC infrastructure (DispatcherServlet, Jackson, validation)</li>
 *   <li>{@code @ControllerAdvice} beans ({@link GlobalExceptionHandler})</li>
 * </ul>
 *
 * <p>The service is NOT loaded — it is mocked with {@code @MockBean}.
 * This means tests run fast and test the controller in isolation.
 *
 * <p>Each test follows the Arrange-Act-Assert pattern:
 * <pre>
 * // Arrange: set up the mock
 * when(todoService.findById(1L)).thenReturn(response);
 *
 * // Act + Assert: make the HTTP call and verify the response
 * mockMvc.perform(get("/api/v1/todos/1"))
 *        .andExpect(status().isOk())
 *        .andExpect(jsonPath("$.id").value(1));
 * </pre>
 */
@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

    // -------------------------------------------------------------------------
    // GET /api/v1/todos
    // -------------------------------------------------------------------------

    @Test
    void getAllTodos_returnsEmptyList() throws Exception {
        // Arrange
        when(todoService.findAll(null)).thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(get("/api/v1/todos"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllTodos_returnsTwoTodos() throws Exception {
        // Arrange
        List<TodoResponse> todos = List.of(
                new TodoResponse(1L, "First Todo", "Description 1", false, NOW),
                new TodoResponse(2L, "Second Todo", "Description 2", true, NOW)
        );
        when(todoService.findAll(null)).thenReturn(todos);

        // Act + Assert
        mockMvc.perform(get("/api/v1/todos"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].id", is(1)))
               .andExpect(jsonPath("$[0].title", is("First Todo")))
               .andExpect(jsonPath("$[1].id", is(2)))
               .andExpect(jsonPath("$[1].completed", is(true)));
    }

    @Test
    void getAllTodos_withCompletedFilter_passesFilterToService() throws Exception {
        // Arrange
        TodoResponse completedTodo = new TodoResponse(1L, "Done", null, true, NOW);
        when(todoService.findAll(true)).thenReturn(List.of(completedTodo));

        // Act + Assert
        mockMvc.perform(get("/api/v1/todos").param("completed", "true"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].completed", is(true)));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/todos/{id}
    // -------------------------------------------------------------------------

    @Test
    void getTodoById_existingId_returnsTodo() throws Exception {
        // Arrange
        TodoResponse response = new TodoResponse(1L, "Learn Spring", "Build APIs", false, NOW);
        when(todoService.findById(1L)).thenReturn(response);

        // Act + Assert
        mockMvc.perform(get("/api/v1/todos/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.title", is("Learn Spring")))
               .andExpect(jsonPath("$.description", is("Build APIs")))
               .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void getTodoById_notFound_returns404() throws Exception {
        // Arrange: service throws not found for id 999
        when(todoService.findById(999L))
                .thenThrow(new TodoNotFoundException(999L));

        // Act + Assert
        mockMvc.perform(get("/api/v1/todos/999"))
               .andDo(print())  // helpful for debugging — prints request/response
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status", is(404)))
               .andExpect(jsonPath("$.message", is("Todo with id 999 not found")));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/todos
    // -------------------------------------------------------------------------

    @Test
    void createTodo_validRequest_returnsCreated() throws Exception {
        // Arrange
        CreateTodoRequest request = new CreateTodoRequest("Learn Spring Boot", "Build REST APIs");
        TodoResponse response = new TodoResponse(1L, "Learn Spring Boot", "Build REST APIs", false, NOW);
        when(todoService.create(any(CreateTodoRequest.class))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())          // 201
               .andExpect(header().exists("Location"))   // Location header set
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.title", is("Learn Spring Boot")))
               .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void createTodo_withBlankTitle_returns400() throws Exception {
        // Arrange: title is blank — should fail @NotBlank validation
        String requestBody = """
                {
                  "title": "",
                  "description": "some description"
                }
                """;

        // Act + Assert: no service call should happen, validation fails first
        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
               .andExpect(status().isBadRequest())       // 400
               .andExpect(jsonPath("$.status", is(400)))
               .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createTodo_withWhitespaceTitleOnly_returns400() throws Exception {
        // @NotBlank trims whitespace, so "   " is considered blank
        String requestBody = """
                {
                  "title": "   ",
                  "description": "test"
                }
                """;

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createTodo_titleExceedsMaxLength_returns400() throws Exception {
        // Title > 200 chars — should fail @Size(max=200) validation
        String longTitle = "A".repeat(201);
        String requestBody = """
                {
                  "title": "%s"
                }
                """.formatted(longTitle);

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void createTodo_withoutDescription_succeeds() throws Exception {
        // Description is optional — should succeed without it
        String requestBody = """
                {
                  "title": "Valid Title"
                }
                """;
        TodoResponse response = new TodoResponse(1L, "Valid Title", null, false, NOW);
        when(todoService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
               .andExpect(status().isCreated());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/todos/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateTodo_validRequest_returnsUpdated() throws Exception {
        // Arrange
        UpdateTodoRequest request = new UpdateTodoRequest("Updated Title", "Updated desc", true);
        TodoResponse response = new TodoResponse(1L, "Updated Title", "Updated desc", true, NOW);
        when(todoService.update(eq(1L), any(UpdateTodoRequest.class))).thenReturn(response);

        // Act + Assert
        mockMvc.perform(put("/api/v1/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.title", is("Updated Title")))
               .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void updateTodo_notFound_returns404() throws Exception {
        // Arrange
        UpdateTodoRequest request = new UpdateTodoRequest("Title", null, false);
        when(todoService.update(eq(999L), any()))
                .thenThrow(new TodoNotFoundException(999L));

        // Act + Assert
        mockMvc.perform(put("/api/v1/todos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/todos/{id}/complete
    // -------------------------------------------------------------------------

    @Test
    void completeTodo_returnsUpdatedTodo() throws Exception {
        // Arrange
        TodoResponse response = new TodoResponse(1L, "Learn Spring", null, true, NOW);
        when(todoService.markComplete(1L)).thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch("/api/v1/todos/1/complete"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void completeTodo_alreadyCompleted_returns409() throws Exception {
        // Arrange: service throws when todo is already completed
        when(todoService.markComplete(1L))
                .thenThrow(new IllegalStateException("Todo with id 1 is already completed"));

        // Act + Assert
        mockMvc.perform(patch("/api/v1/todos/1/complete"))
               .andExpect(status().isConflict());  // 409
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/todos/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteTodo_returns204() throws Exception {
        // Arrange: delete is a void method
        doNothing().when(todoService).delete(1L);

        // Act + Assert
        mockMvc.perform(delete("/api/v1/todos/1"))
               .andExpect(status().isNoContent())   // 204
               .andExpect(jsonPath("$").doesNotExist());  // no response body
    }

    @Test
    void deleteTodo_notFound_returns404() throws Exception {
        // Arrange
        doThrow(new TodoNotFoundException(999L)).when(todoService).delete(999L);

        // Act + Assert
        mockMvc.perform(delete("/api/v1/todos/999"))
               .andExpect(status().isNotFound());   // 404
    }
}
