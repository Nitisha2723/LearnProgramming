# Module 11: REST APIs with FastAPI

Build production-quality REST APIs using FastAPI — the modern, high-performance Python web framework.

---

## Learning Objectives

By completing this module you will be able to:

1. Explain REST principles and design resource-based URLs
2. Use HTTP methods (GET/POST/PUT/PATCH/DELETE) correctly
3. Return appropriate HTTP status codes (201, 204, 404, 422...)
4. Build a FastAPI application with path operations, path/query params, and request bodies
5. Validate request data with Pydantic `BaseModel` and `Field(...)`
6. Separate request and response schemas (never expose internal fields)
7. Use `response_model` to control what clients receive
8. Organise routes with `APIRouter`
9. Use dependency injection with `Depends()`
10. Write tests for every endpoint using `TestClient`

---

## Prerequisites

- Python 3.11+
- Module 05 (functions) and 06 (classes) completed
- Basic understanding of HTTP (what a request/response is)

---

## Setup

### Install Dependencies

```bash
pip install fastapi uvicorn httpx pytest
```

Or with a requirements file:

```bash
pip install -r requirements.txt
```

`requirements.txt`:
```
fastapi>=0.115.0
uvicorn>=0.30.0
httpx>=0.27.0
pytest>=8.0.0
```

### Running the Todo API

From the `11-rest-api/` directory (the parent of `code/`):

```bash
uvicorn code.main:app --reload
```

The server starts at `http://localhost:8000`.

Open **`http://localhost:8000/docs`** for the interactive Swagger UI — you can try every endpoint directly in the browser without any additional tools.

### Running the Tests

```bash
# From the 11-rest-api/ directory
pytest tests/ -v
```

---

## Directory Structure

```
11-rest-api/
├── README.md                    # This file
├── theory/
│   ├── 01-rest-fundamentals.md  # REST principles, HTTP methods, status codes
│   ├── 02-fastapi-basics.md     # FastAPI app, routes, params, exceptions
│   ├── 03-pydantic-and-validation.md  # Pydantic models, Field, validators
│   └── 04-testing-apis.md       # TestClient, fixtures, dependency injection
├── code/
│   ├── __init__.py
│   ├── main.py                  # FastAPI app + router setup
│   ├── models.py                # Pydantic schemas (request/response)
│   ├── repository.py            # In-memory data store
│   ├── dependencies.py          # Dependency injection (get_repository)
│   └── routers/
│       ├── __init__.py
│       └── todos.py             # All /todos endpoints
├── tests/
│   ├── __init__.py
│   ├── conftest.py              # pytest fixtures (fresh client per test)
│   └── test_todos.py            # Full test suite
├── exercises/
│   ├── exercise_01.md           # Build a Notes API with tags
│   └── solutions/
│       └── notes_api.py         # Complete solution
└── mini-project/
    └── README.md                # "Build a Recipe API" spec
```

---

## The Todo API

The `code/` directory contains a fully working Todo API. It demonstrates all the key FastAPI patterns.

### Endpoints

| Method | Path                            | Description                         | Status |
|--------|---------------------------------|-------------------------------------|--------|
| GET    | `/health`                       | Health check                        | 200    |
| GET    | `/api/v1/todos`                 | List all todos                      | 200    |
| GET    | `/api/v1/todos?completed=true`  | Filter by completion status         | 200    |
| GET    | `/api/v1/todos/{id}`            | Get a specific todo                 | 200    |
| POST   | `/api/v1/todos`                 | Create a new todo                   | 201    |
| PUT    | `/api/v1/todos/{id}`            | Update a todo                       | 200    |
| PATCH  | `/api/v1/todos/{id}/complete`   | Mark a todo as complete             | 200    |
| DELETE | `/api/v1/todos/{id}`            | Delete a todo                       | 204    |

### curl Examples

#### Health check

```bash
curl http://localhost:8000/health
```

#### Create a todo

```bash
curl -X POST http://localhost:8000/api/v1/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Buy groceries", "description": "Milk, eggs, bread"}'
```

Response (201 Created):
```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "completed": false,
  "created_at": "2025-01-15T10:30:00.123456"
}
```

#### List all todos

```bash
curl http://localhost:8000/api/v1/todos
```

#### Filter by completion status

```bash
# Completed todos only
curl "http://localhost:8000/api/v1/todos?completed=true"

# Pending todos only
curl "http://localhost:8000/api/v1/todos?completed=false"
```

#### Get a specific todo

```bash
curl http://localhost:8000/api/v1/todos/1
```

#### Update a todo

```bash
curl -X PUT http://localhost:8000/api/v1/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated title", "completed": true}'
```

#### Mark a todo as complete

```bash
curl -X PATCH http://localhost:8000/api/v1/todos/1/complete
```

#### Delete a todo

```bash
curl -X DELETE http://localhost:8000/api/v1/todos/1
```

Response: `204 No Content` (empty body)

---

## Key Patterns Demonstrated

### 1. Separate Request/Response Models

`code/models.py` shows three schemas for one resource:
- `TodoCreate` — what clients send (no `id`, no `created_at`)
- `TodoUpdate` — all optional for partial updates
- `TodoResponse` — what clients receive (includes server-generated fields)

### 2. Dependency Injection

`code/dependencies.py` defines `get_repository()` as a FastAPI dependency.
Routes declare it with `Depends(get_repository)` — FastAPI injects the
right instance automatically.

In tests (`conftest.py`), we replace it with a fresh empty repository:

```python
app.dependency_overrides[get_repository] = lambda: TodoRepository()
```

### 3. Proper Status Codes

- `POST /todos` returns **201 Created**
- `DELETE /todos/{id}` returns **204 No Content** (no body)
- Missing todo returns **404 Not Found**
- Bad request body returns **422 Unprocessable Entity** (automatic from Pydantic)

### 4. In-Memory Repository

`code/repository.py` shows the repository pattern with a thread-safe
in-memory dict. This is easily swapped for a real database without changing
the router code.

---

## Suggested Learning Path

1. Read `theory/01-rest-fundamentals.md` — understand the concepts
2. Read `theory/02-fastapi-basics.md` — learn FastAPI
3. Run the app: `uvicorn code.main:app --reload`
4. Explore the docs at `http://localhost:8000/docs`
5. Read `code/main.py`, `code/models.py`, `code/repository.py`, `code/routers/todos.py`
6. Run the tests: `pytest tests/ -v`
7. Read `theory/03-pydantic-and-validation.md`
8. Read `theory/04-testing-apis.md`
9. Complete `exercises/exercise_01.md`
10. Build the Recipe API (`mini-project/README.md`)

---

## Common Errors

### `ModuleNotFoundError: No module named 'code'`

Run uvicorn from the **`11-rest-api/`** directory (parent of `code/`):

```bash
# Correct — from 11-rest-api/
uvicorn code.main:app --reload

# Wrong — don't cd into code/
cd code && uvicorn main:app --reload  # relative imports break
```

### `ImportError: attempted relative import with no known parent package`

Same cause — run from the right directory.

### Tests failing with `ImportError`

Run pytest from the **`11-rest-api/`** directory:

```bash
# From 11-rest-api/
pytest tests/ -v
```

### `422 Unprocessable Entity` when testing

Check your JSON body — Pydantic validation failed. Look at the error response body for details:
```json
{"detail": [{"type": "...", "loc": ["body", "field_name"], "msg": "..."}]}
```

---

## Further Reading

- [FastAPI documentation](https://fastapi.tiangolo.com/) — excellent official docs
- [Pydantic v2 documentation](https://docs.pydantic.dev/latest/)
- [HTTP status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) — MDN reference
- [REST API Design Best Practices](https://restfulapi.net/)
