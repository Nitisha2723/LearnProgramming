# Exercise 1: Build a Notes API with Tags

## Goal

Build a REST API for a simple note-taking application where notes can have multiple tags.

---

## Requirements

### Data Model

Each **note** has:
- `id` — integer, assigned by server
- `title` — string, required, 1–200 chars
- `content` — string, optional
- `tags` — list of strings, optional (default: empty list)
- `created_at` — datetime, assigned by server

### Endpoints to Implement

| Method | Path              | Description                          | Status |
|--------|-------------------|--------------------------------------|--------|
| POST   | `/notes`          | Create a note                        | 201    |
| GET    | `/notes`          | List all notes (with ?tag= filter)   | 200    |
| GET    | `/notes/{id}`     | Get a specific note                  | 200    |
| PUT    | `/notes/{id}`     | Update a note                        | 200    |
| DELETE | `/notes/{id}`     | Delete a note                        | 204    |
| GET    | `/notes/tags`     | List all unique tags in use          | 200    |

### Filter Support

`GET /notes?tag=python` should return only notes that have `"python"` in their tags list.

### URL Ordering Note

The `/notes/tags` endpoint must be defined **before** `/notes/{id}` in your router, otherwise FastAPI will try to parse `"tags"` as an integer ID and return 422.

---

## Pydantic Schemas to Create

```python
class NoteCreate(BaseModel):
    title: str = Field(..., min_length=1, max_length=200)
    content: Optional[str] = None
    tags: list[str] = []

class NoteUpdate(BaseModel):
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    content: Optional[str] = None
    tags: Optional[list[str]] = None

class NoteResponse(BaseModel):
    id: int
    title: str
    content: Optional[str]
    tags: list[str]
    created_at: datetime
```

---

## Steps

1. Create a new file `exercises/notes_api.py`
2. Define the Pydantic schemas above
3. Create an in-memory repository (dict + lock, same pattern as the Todo app)
4. Create a FastAPI app with all six endpoints
5. Add the `?tag=` query filter to `GET /notes`
6. Implement `GET /notes/tags` to return the unique tags in use

---

## Stretch Goals

Once the basic API works, try adding:

1. **Tag validation**: Ensure tags are lowercase and contain no spaces (use `field_validator`)
2. **Multiple tag filter**: `GET /notes?tag=python&tag=fastapi` — notes must have ALL specified tags
3. **Sorting**: `GET /notes?sort=created_at&order=desc`
4. **Pagination**: `GET /notes?page=1&per_page=10`
5. **Search**: `GET /notes?q=python` — search title and content

---

## Testing

Write tests for:
- Creating a note with and without tags
- Listing notes with and without the `?tag=` filter
- Getting a note by ID (found and not found)
- Updating a note
- Deleting a note
- The `/notes/tags` endpoint returns correct unique tags

---

## Running Your Solution

```bash
uvicorn exercises.notes_api:app --reload --port 8001
```

Then open `http://localhost:8001/docs` to try it interactively.

---

## Example curl Commands

```bash
# Create a note
curl -X POST http://localhost:8001/notes \
  -H "Content-Type: application/json" \
  -d '{"title": "Python tips", "content": "Use list comprehensions", "tags": ["python", "tips"]}'

# List all notes
curl http://localhost:8001/notes

# Filter by tag
curl "http://localhost:8001/notes?tag=python"

# Get a specific note
curl http://localhost:8001/notes/1

# List all tags
curl http://localhost:8001/notes/tags

# Update a note
curl -X PUT http://localhost:8001/notes/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated title", "tags": ["python", "updated"]}'

# Delete a note
curl -X DELETE http://localhost:8001/notes/1
```
