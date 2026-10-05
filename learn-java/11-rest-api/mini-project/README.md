# Mini-Project: Note-Taking REST API

## Overview

Build a fully functional REST API for a note-taking application. Notes can have tags, allowing users to organize and filter their notes.

This mini-project consolidates everything from the module: layered architecture, DTOs, validation, exception handling, and proper HTTP semantics.

---

## Requirements

### The Note Resource

```
id          Long            (assigned by server)
title       String          required, 1–200 chars
content     String          required, 1–10000 chars
tags        List<String>    optional, each tag 1–50 chars, max 10 tags
createdAt   LocalDateTime   (assigned by server on create)
updatedAt   LocalDateTime   (updated on every modification)
```

### API Endpoints

| Method | URL | Description | Success Code |
|--------|-----|-------------|--------------|
| `POST` | `/api/v1/notes` | Create a new note | 201 Created |
| `GET` | `/api/v1/notes` | List all notes | 200 OK |
| `GET` | `/api/v1/notes?tag=work` | List notes filtered by tag | 200 OK |
| `GET` | `/api/v1/notes/{id}` | Get a note by ID | 200 OK |
| `PUT` | `/api/v1/notes/{id}` | Update a note (full replace) | 200 OK |
| `DELETE` | `/api/v1/notes/{id}` | Delete a note | 204 No Content |

---

## JSON Shapes

### Create Request — POST /api/v1/notes

```json
{
  "title": "Meeting Notes",
  "content": "Discussed project timeline and milestones...",
  "tags": ["work", "meetings", "q1-2024"]
}
```

### Note Response

```json
{
  "id": 1,
  "title": "Meeting Notes",
  "content": "Discussed project timeline and milestones...",
  "tags": ["work", "meetings", "q1-2024"],
  "createdAt": "2024-01-15T09:30:00",
  "updatedAt": "2024-01-15T09:30:00"
}
```

### Validation Error Response

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "title": "must not be blank",
    "content": "size must be between 1 and 10000"
  }
}
```

### Not Found Response

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Note with id 999 not found"
}
```

---

## Folder Structure to Create

```
notes-api/
├── NotesApiApplication.java
├── model/
│   └── Note.java
├── dto/
│   ├── CreateNoteRequest.java
│   ├── NoteResponse.java
│   └── UpdateNoteRequest.java  (or reuse CreateNoteRequest for PUT)
├── repository/
│   └── NoteRepository.java
├── service/
│   └── NoteService.java
├── controller/
│   └── NoteController.java
└── exception/
    ├── NoteNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## Implementation Guide

### Step 1: The Note Model

```java
public class Note {
    private Long id;
    private String title;
    private String content;
    private List<String> tags;      // can be null or empty
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // constructors, getters, setters
}
```

### Step 2: The Repository

Use `ConcurrentHashMap<Long, Note>` as the backing store. Add a method to search by tag:

```java
public List<Note> findByTag(String tag) {
    return store.values().stream()
            .filter(note -> note.getTags() != null && note.getTags().contains(tag))
            .toList();
}
```

### Step 3: The DTOs

**CreateNoteRequest** (also used for PUT updates):
```java
public record CreateNoteRequest(
        @NotBlank @Size(max = 200)
        String title,

        @NotBlank @Size(max = 10000)
        String content,

        @Size(max = 10, message = "Cannot have more than 10 tags")
        List<@NotBlank @Size(max = 50) String> tags
) {}
```

Note the nested validation on List elements: `List<@NotBlank @Size(max=50) String>` — each tag is validated individually.

**NoteResponse** — all fields including `createdAt` and `updatedAt`.

### Step 4: The Service

Key things to implement:
- `findAll(String tag)` — returns all notes when tag is null, filtered by tag otherwise
- `create(CreateNoteRequest)` — sets `createdAt = now()` and `updatedAt = now()`
- `update(Long id, CreateNoteRequest)` — updates all fields AND sets `updatedAt = now()`

The `updatedAt` field is a service concern — the controller passes the request to the service, and the service knows to stamp the current time on modification.

### Step 5: The Controller

```java
@GetMapping
public List<NoteResponse> getAllNotes(@RequestParam(required = false) String tag) {
    return noteService.findAll(tag);
}
```

The `?tag=work` query parameter maps directly to the `tag` parameter via `@RequestParam`.

---

## Validation to Implement

| Input | Expected Response |
|-------|------------------|
| `{"title": "", "content": "..."}` | 400, `errors.title: must not be blank` |
| `{"title": "T", "content": ""}` | 400, `errors.content: must not be blank` |
| `{"title": "T", "content": "C", "tags": ["", "work"]}` | 400, error on tags[0] |
| `GET /api/v1/notes/999` | 404, not found message |
| `DELETE /api/v1/notes/999` | 404, not found message |

---

## Testing Your API

Once the app is running on port 8080, test it with curl:

```bash
# 1. Create a work note
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sprint Planning",
    "content": "Discussed features for Q1. Committed to 15 story points.",
    "tags": ["work", "sprint", "planning"]
  }'

# 2. Create a personal note
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Book List",
    "content": "Books to read this year: Clean Code, Designing Data-Intensive Applications",
    "tags": ["personal", "reading"]
  }'

# 3. Get all notes
curl http://localhost:8080/api/v1/notes | jq .

# 4. Filter by tag
curl "http://localhost:8080/api/v1/notes?tag=work" | jq .

# 5. Get a specific note
curl http://localhost:8080/api/v1/notes/1

# 6. Update a note
curl -X PUT http://localhost:8080/api/v1/notes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Sprint Planning (Updated)",
    "content": "Updated notes after retrospective.",
    "tags": ["work", "sprint"]
  }'

# 7. Verify updatedAt changed
curl http://localhost:8080/api/v1/notes/1 | jq '{createdAt: .createdAt, updatedAt: .updatedAt}'

# 8. Delete a note
curl -X DELETE http://localhost:8080/api/v1/notes/2
# Response: 204 No Content

# 9. Try to get deleted note (should 404)
curl -i http://localhost:8080/api/v1/notes/2

# 10. Test validation — blank title
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Content-Type: application/json" \
  -d '{"title": "", "content": "some content"}'
# Response: 400 Bad Request with errors.title

# 11. Filter by non-existent tag (returns empty list, not 404)
curl "http://localhost:8080/api/v1/notes?tag=doesnotexist"
# Response: []
```

---

## Acceptance Criteria

- [ ] `POST /api/v1/notes` creates a note and returns 201 with body + Location header
- [ ] `GET /api/v1/notes` returns all notes as a JSON array
- [ ] `GET /api/v1/notes?tag=work` returns only notes tagged "work"
- [ ] `GET /api/v1/notes?tag=nonexistent` returns empty array, not 404
- [ ] `GET /api/v1/notes/{id}` returns the note or 404 if not found
- [ ] `PUT /api/v1/notes/{id}` updates the note and sets `updatedAt` to current time
- [ ] `DELETE /api/v1/notes/{id}` returns 204 or 404 if not found
- [ ] Blank title or content returns 400 with field-level errors
- [ ] Tags are validated per-element (each tag must be non-blank and ≤ 50 chars)
- [ ] More than 10 tags returns 400
- [ ] Code follows controller → service → repository layering

---

## Extension Ideas

Once the core API works, try these extensions:

1. **Search by content**: `GET /api/v1/notes?search=sprint` — returns notes whose content contains the search term (case-insensitive)
2. **Multiple tag filter**: `GET /api/v1/notes?tag=work&tag=sprint` — returns notes with ALL specified tags
3. **Sort by date**: `GET /api/v1/notes?sort=updatedAt` — returns most recently updated first
4. **Add `@WebMvcTest` tests** covering all endpoints and error cases
5. **Add `@SpringBootTest` integration test** that creates, reads, updates, and deletes a note in sequence
