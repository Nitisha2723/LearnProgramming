package com.example.bookstore.service;

import com.example.bookstore.dto.AuthorResponse;
import com.example.bookstore.dto.CreateAuthorRequest;
import com.example.bookstore.exception.AuthorNotFoundException;
import com.example.bookstore.exception.AuthorHasBooksException;
import com.example.bookstore.model.Author;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream()
                .map(AuthorResponse::from)
                .toList();
    }

    public AuthorResponse findById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        return AuthorResponse.from(author);
    }

    public AuthorResponse create(CreateAuthorRequest request) {
        Author author = new Author();
        author.setName(request.name());
        author.setBiography(request.biography());
        return AuthorResponse.from(authorRepository.save(author));
    }

    public AuthorResponse update(Long id, CreateAuthorRequest request) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
        existing.setName(request.name());
        existing.setBiography(request.biography());
        return AuthorResponse.from(authorRepository.save(existing));
    }

    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFoundException(id);
        }
        // Business rule: cannot delete an author who has books
        if (bookRepository.existsByAuthorId(id)) {
            throw new AuthorHasBooksException(id);
        }
        authorRepository.deleteById(id);
    }
}
