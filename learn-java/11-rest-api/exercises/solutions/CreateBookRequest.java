package com.example.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Year;

public record CreateBookRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 300, message = "Title must be at most 300 characters")
        String title,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "\\d{3}-\\d{10}", message = "ISBN must be in format 978-1234567890")
        String isbn,

        @Min(value = 1000, message = "Publication year must be at least 1000")
        // Note: for dynamic max (current year), a custom validator would be needed.
        // For simplicity we use a fixed max here.
        @Max(value = 2100, message = "Publication year cannot be in the far future")
        int publicationYear,

        @NotNull(message = "Author ID is required")
        Long authorId
) {}
