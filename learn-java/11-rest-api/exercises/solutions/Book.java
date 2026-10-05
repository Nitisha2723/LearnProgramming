package com.example.bookstore.model;

/**
 * Book domain entity.
 * authorId is a foreign key reference — not an embedded Author object.
 */
public class Book {

    private Long id;
    private String title;
    private String isbn;
    private int publicationYear;
    private Long authorId;

    public Book() {}

    public Book(Long id, String title, String isbn, int publicationYear, Long authorId) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.authorId = authorId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public int getPublicationYear() { return publicationYear; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
}
