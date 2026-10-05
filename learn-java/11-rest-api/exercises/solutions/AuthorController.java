package com.example.bookstore.controller;

import com.example.bookstore.dto.AuthorResponse;
import com.example.bookstore.dto.CreateAuthorRequest;
import com.example.bookstore.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<AuthorResponse> getAll() {
        return authorService.findAll();
    }

    @GetMapping("/{id}")
    public AuthorResponse getById(@PathVariable Long id) {
        return authorService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody CreateAuthorRequest request) {
        AuthorResponse created = authorService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/authors/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AuthorResponse update(@PathVariable Long id, @Valid @RequestBody CreateAuthorRequest request) {
        return authorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
