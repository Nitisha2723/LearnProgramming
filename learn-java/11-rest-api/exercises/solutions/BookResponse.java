package com.example.bookstore.dto;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        int publicationYear,
        Long authorId,
        String authorName   // Denormalized for client convenience
) {}
