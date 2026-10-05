package com.example.bookstore.dto;

public record AuthorResponse(Long id, String name, String biography) {
    public static AuthorResponse from(com.example.bookstore.model.Author author) {
        return new AuthorResponse(author.getId(), author.getName(), author.getBiography());
    }
}
