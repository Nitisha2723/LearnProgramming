package com.example.bookstore.exception;

public class AuthorHasBooksException extends RuntimeException {
    public AuthorHasBooksException(Long id) {
        super("Cannot delete author with id " + id + ": author has existing books. Delete the books first.");
    }
}
