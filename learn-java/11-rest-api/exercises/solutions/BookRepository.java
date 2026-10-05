package com.example.bookstore.repository;

import com.example.bookstore.model.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {

    private final ConcurrentHashMap<Long, Book> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Book> findByAuthorId(Long authorId) {
        return store.values().stream()
                .filter(book -> authorId.equals(book.getAuthorId()))
                .toList();
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public boolean existsByAuthorId(Long authorId) {
        return store.values().stream().anyMatch(b -> authorId.equals(b.getAuthorId()));
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idGenerator.getAndIncrement());
        }
        store.put(book.getId(), book);
        return book;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
