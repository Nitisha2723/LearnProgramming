package com.example.bookstore.repository;

import com.example.bookstore.model.Author;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class AuthorRepository {

    private final ConcurrentHashMap<Long, Author> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Author> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Author> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public Author save(Author author) {
        if (author.getId() == null) {
            author.setId(idGenerator.getAndIncrement());
        }
        store.put(author.getId(), author);
        return author;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
