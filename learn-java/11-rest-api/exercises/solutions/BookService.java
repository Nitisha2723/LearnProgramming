package com.example.bookstore.service;

import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.dto.CreateBookRequest;
import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.model.Author;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    public List<BookResponse> findAll(Long authorId) {
        List<Book> books = authorId != null
                ? bookRepository.findByAuthorId(authorId)
                : bookRepository.findAll();
        return books.stream()
                .map(this::toResponse)
                .toList();
    }

    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return toResponse(book);
    }

    public BookResponse create(CreateBookRequest request) {
        // Verify author exists — cross-resource validation in the service
        if (!authorRepository.existsById(request.authorId())) {
            throw new AuthorNotFoundException(request.authorId());
        }
        Book book = new Book();
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPublicationYear(request.publicationYear());
        book.setAuthorId(request.authorId());
        return toResponse(bookRepository.save(book));
    }

    public BookResponse update(Long id, CreateBookRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        if (!authorRepository.existsById(request.authorId())) {
            throw new AuthorNotFoundException(request.authorId());
        }
        existing.setTitle(request.title());
        existing.setIsbn(request.isbn());
        existing.setPublicationYear(request.publicationYear());
        existing.setAuthorId(request.authorId());
        return toResponse(bookRepository.save(existing));
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        String authorName = authorRepository.findById(book.getAuthorId())
                .map(Author::getName)
                .orElse("Unknown");
        return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(),
                book.getPublicationYear(), book.getAuthorId(), authorName);
    }
}
