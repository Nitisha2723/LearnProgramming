# Exercise 01: BookStore REST API

## Objective

Build a REST API for a simple bookstore that manages books and authors. You'll practice all the patterns from the module: layered architecture, DTOs, validation, and exception handling.

---

## Domain

A **BookStore** manages two resources:
- **Author**: has a name and a biography
- **Book**: has a title, ISBN, publication year, and belongs to one author

---

## Requirements

### Endpoints to implement

#### Authors

| Method | URL | Description | Success Code |
|--------|-----|-------------|--------------|
| `GET` | `/api/v1/authors` | List all authors | 200 |
| `GET` | `/api/v1/authors/{id}` | Get author by ID | 200 |
| `POST` | `/api/v1/authors` | Create author | 201 |
| `PUT` | `/api/v1/authors/{id}` | Update author | 200 |
| `DELETE` | `/api/v1/authors/{id}` | Delete author | 204 |

#### Books

| Method | URL | Description | Success Code |
|--------|-----|-------------|--------------|
| `GET` | `/api/v1/books` | List all books (optional `?authorId=` filter) | 200 |
| `GET` | `/api/v1/books/{id}` | Get book by ID | 200 |
| `POST` | `/api/v1/books` | Create book | 201 |
| `PUT` | `/api/v1/books/{id}` | Update book | 200 |
| `DELETE` | `/api/v1/books/{id}` | Delete book | 204 |

---

## Models

### Author

```
id          Long          (assigned by server)
name        String        required, 2–100 chars
biography   String        optional, max 2000 chars
```

### Book

```
id              Long      (assigned by server)
title           String    required, 1–300 chars
isbn            String    required, must match pattern: \d{3}-\d{10}
publicationYear int       required, must be between 1000 and current year
authorId        Long      required, must reference an existing author
```

---

## Files to Create

```
code/
├── BookStoreApplication.java
├── model/
│   ├── Author.java
│   └── Book.java
├── dto/
│   ├── CreateAuthorRequest.java
│   ├── CreateBookRequest.java
│   ├── AuthorResponse.java
│   └── BookResponse.java
├── repository/
│   ├── AuthorRepository.java
│   └── BookRepository.java
├── service/
│   ├── AuthorService.java
│   └── BookService.java
├── controller/
│   ├── AuthorController.java
│   └── BookController.java
└── exception/
    ├── AuthorNotFoundException.java
    ├── BookNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## Step-by-Step Instructions

### Step 1: Create the Model classes

Create `Author.java` and `Book.java` as plain Java classes with fields, constructors, and getters/setters.

`Book` has an `authorId` field (a `Long`) — not a full `Author` object. When returning a `BookResponse`, you may include the author's name for convenience.

### Step 2: Create the Repositories

Both repositories use `ConcurrentHashMap<Long, T>` and `AtomicLong` for ID generation.

Add to `BookRepository`:
```java
public List<Book> findByAuthorId(Long authorId) {
    return store.values().stream()
            .filter(b -> authorId.equals(b.getAuthorId()))
            .toList();
}
```

### Step 3: Create the DTOs

**CreateAuthorRequest** — validate `name` as `@NotBlank @Size(min=2, max=100)`

**CreateBookRequest** — validate all fields. For ISBN pattern: `@Pattern(regexp = "\\d{3}-\\d{10}", message = "ISBN must be in format 978-1234567890")`

**AuthorResponse** — id, name, biography

**BookResponse** — id, title, isbn, publicationYear, authorId, authorName (from the author)

### Step 4: Create the Services

**AuthorService**: straightforward CRUD. On delete, consider what happens to books by this author — for this exercise, either forbid deletion if the author has books, or allow it (your choice, but document it in a comment).

**BookService**: when creating a book, verify the `authorId` exists:
```java
if (!authorRepository.existsById(request.authorId())) {
    throw new AuthorNotFoundException(request.authorId());
}
```

When returning a `BookResponse`, look up the author's name:
```java
Author author = authorRepository.findById(book.getAuthorId()).orElse(null);
String authorName = author != null ? author.getName() : "Unknown";
return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(),
                        book.getPublicationYear(), book.getAuthorId(), authorName);
```

### Step 5: Create the Controllers

Both controllers follow the same pattern as `TodoController`. Use `@RequestMapping("/api/v1/authors")` and `@RequestMapping("/api/v1/books")`.

For the book list endpoint, add optional filtering:
```java
@GetMapping
public List<BookResponse> getAll(@RequestParam(required = false) Long authorId) {
    return bookService.findAll(authorId);
}
```

### Step 6: Create the Exception Handler

Create a `GlobalExceptionHandler` similar to the Todo API's, handling:
- `AuthorNotFoundException` → 404
- `BookNotFoundException` → 404
- `MethodArgumentNotValidException` → 400
- `Exception` → 500

---

## Validation Challenges

Try to handle these edge cases:

1. **Create a book with an invalid ISBN** → should get 400 with field error on `isbn`
2. **Create a book referencing a non-existent author** → should get 404
3. **Delete an author that has books** → your choice: allow (leaves orphaned books) or forbid (return 409 Conflict)
4. **Create a book with a publication year in the future** → should get 400

---

## Test Scenarios (manual testing with curl)

```bash
# 1. Create an author
curl -X POST http://localhost:8080/api/v1/authors \
  -H "Content-Type: application/json" \
  -d '{"name": "J.K. Rowling", "biography": "British author"}'

# 2. Create a book (use the author ID from step 1)
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title": "Harry Potter", "isbn": "978-0439708180", "publicationYear": 1998, "authorId": 1}'

# 3. List all books by author
curl "http://localhost:8080/api/v1/books?authorId=1"

# 4. Try invalid ISBN (should get 400)
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title": "Test", "isbn": "invalid-isbn", "publicationYear": 2020, "authorId": 1}'

# 5. Try non-existent author (should get 404)
curl -X POST http://localhost:8080/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{"title": "Test", "isbn": "978-0000000000", "publicationYear": 2020, "authorId": 999}'
```

---

## Bonus Challenges

1. **Add `GET /api/v1/authors/{id}/books`** — returns all books by a specific author (nested resource)
2. **Add pagination** — support `?page=0&size=10` on the list endpoints
3. **Add `GET /api/v1/books?search=harry`** — search books by title (case-insensitive contains)
4. **Write `@WebMvcTest` tests** for both controllers

---

## Success Criteria

- [ ] All 10 endpoints return the correct status codes
- [ ] Invalid requests return 400 with field-level error details
- [ ] References to non-existent authors return 404
- [ ] Code is organized in controller/service/repository/dto/exception layers
- [ ] No business logic in the controller
- [ ] No HTTP-specific code in the service
