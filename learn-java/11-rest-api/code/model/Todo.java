package com.example.todoapi.model;

import java.time.LocalDateTime;

/**
 * The Todo entity — the core domain object.
 *
 * <p>This is an internal model used within the application. It is NOT exposed
 * directly in API responses. Instead, {@link com.example.todoapi.dto.TodoResponse}
 * is used to control exactly what fields the client sees.
 *
 * <p>In a production app this would be a JPA entity annotated with @Entity,
 * @Table, @Id, @Column etc. For this learning module we use a plain POJO with
 * an in-memory repository.
 */
public class Todo {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Todo() {
    }

    public Todo(Long id, String title, String description, boolean completed, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    /**
     * Factory method for creating a new (unsaved) todo.
     * The repository will assign the ID when saved.
     */
    public static Todo create(String title, String description) {
        Todo todo = new Todo();
        todo.title = title;
        todo.description = description;
        todo.completed = false;
        todo.createdAt = LocalDateTime.now();
        return todo;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Todo{id=" + id + ", title='" + title + "', completed=" + completed + "}";
    }
}
