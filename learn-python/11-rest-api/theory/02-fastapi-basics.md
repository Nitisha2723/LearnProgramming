# FastAPI Basics

## What is FastAPI?

FastAPI is a modern, fast web framework for building APIs with Python. It was created by Sebastián Ramírez and released in 2018. It has quickly become one of the most popular Python web frameworks.

Key characteristics:
- **Fast**: One of the fastest Python frameworks, on par with Node.js and Go (powered by Starlette + uvicorn)
- **ASGI**: Uses the Asynchronous Server Gateway Interface — supports `async/await` throughout
- **Automatic validation**: Uses Pydantic to validate request data automatically
- **Auto-generated docs**: Interactive OpenAPI documentation at `/docs` (Swagger UI) and `/redoc`
- **Type-safe**: Leverages Python type hints for both validation and editor autocomplete
- **Standards-based**: OpenAPI and JSON Schema

---

## Installation

```bash
pip install fastapi uvicorn
```

- `fastapi` — the framework
- `uvicorn` — the ASGI server that runs your app

---

## Your First FastAPI App

```python
# main.py
from fastapi import FastAPI

app = FastAPI(title="My First API", version="1.0.0")

@app.get("/")
async def root():
    return {"message": "Hello World"}
```

Run it:
```bash
uvicorn main:app --reload
```

- `main` — the filename (`main.py`)
- `app` — the FastAPI instance
- `--reload` — auto-restart when you change code

Visit `http://localhost:8000` for your API, or `http://localhost:8000/docs` for the interactive docs.

---

## FastAPI() App Configuration

```python
app = FastAPI(
    title="Todo API",
    description="A simple todo list API to demonstrate FastAPI",
    version="1.0.0",
    docs_url="/docs",      # Swagger UI (default: /docs)
    redoc_url="/redoc",    # ReDoc (default: /redoc)
    openapi_url="/openapi.json",  # OpenAPI schema
)
```

---

## Path Operations (Route Handlers)

A "path operation" is FastAPI's term for a route handler — a function tied to a URL path and HTTP method.

```python
@app.get("/items")           # GET /items
@app.post("/items")          # POST /items
@app.put("/items/{id}")      # PUT /items/{id}
@app.patch("/items/{id}")    # PATCH /items/{id}
@app.delete("/items/{id}")   # DELETE /items/{id}
```

The decorated function is called when a matching request arrives.

```python
@app.get("/hello")
async def say_hello():
    return {"message": "Hello!"}
```

FastAPI automatically:
- Serializes the returned dict to JSON
- Sets `Content-Type: application/json`
- Sets status code 200

---

## Path Parameters

Path parameters are variable parts of the URL, declared with `{name}` in the route and as function arguments.

```python
@app.get("/users/{user_id}")
async def get_user(user_id: int):
    return {"user_id": user_id}
```

The type annotation (`int`) tells FastAPI to:
1. Parse the string from the URL into an integer
2. Return 422 if it can't be parsed (e.g., `/users/abc`)
3. Document the type in OpenAPI

```python
# Multiple path params
@app.get("/users/{user_id}/posts/{post_id}")
async def get_post(user_id: int, post_id: int):
    return {"user_id": user_id, "post_id": post_id}
```

**Important**: Fixed path segments take priority over parameters. Define `/users/me` before `/users/{user_id}`:

```python
@app.get("/users/me")          # must come first!
async def get_current_user():
    return {"user": "me"}

@app.get("/users/{user_id}")
async def get_user(user_id: int):
    return {"user_id": user_id}
```

---

## Query Parameters

Query parameters are declared as function arguments that are NOT path parameters.

```python
@app.get("/todos")
async def list_todos(skip: int = 0, limit: int = 10):
    return {"skip": skip, "limit": limit}
```

Request: `GET /todos?skip=20&limit=5`

- Optional by default (have default values)
- Type-converted and validated automatically
- `skip: int = 0` → required to be an integer, defaults to 0

### Optional query parameters

```python
from typing import Optional

@app.get("/todos")
async def list_todos(completed: Optional[bool] = None):
    if completed is None:
        return {"filter": "all"}
    return {"filter": "completed" if completed else "pending"}
```

Request: `GET /todos?completed=true` or `GET /todos?completed=false` or `GET /todos`

### Using Query() for more control

```python
from fastapi import Query

@app.get("/todos")
async def list_todos(
    q: Optional[str] = Query(None, min_length=3, max_length=50),
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1, le=100),
):
    return {"q": q, "skip": skip, "limit": limit}
```

---

## Request Body with Pydantic BaseModel

For `POST`, `PUT`, `PATCH` requests, you define the expected body using a Pydantic model.

```python
from pydantic import BaseModel
from typing import Optional

class Item(BaseModel):
    name: str
    price: float
    is_available: bool = True
    description: Optional[str] = None

@app.post("/items")
async def create_item(item: Item):
    return item
```

FastAPI:
- Parses the JSON body
- Validates all fields (type, required/optional)
- Returns 422 if validation fails
- Passes the validated `item` object to your function

### Example request

```http
POST /items
Content-Type: application/json

{
  "name": "Laptop",
  "price": 999.99,
  "description": "A great laptop"
}
```

### Mixing path, query, and body

```python
@app.put("/items/{item_id}")
async def update_item(
    item_id: int,          # path param
    q: Optional[str] = None,  # query param
    item: Item = None,     # request body
):
    result = {"item_id": item_id}
    if q:
        result["q"] = q
    if item:
        result["item"] = item.model_dump()
    return result
```

FastAPI knows which is which:
- If it's in the path template `{item_id}` → path param
- If it's a Pydantic model → request body
- Otherwise → query param

---

## HTTPException

Raise `HTTPException` to return error responses:

```python
from fastapi import HTTPException, status

@app.get("/todos/{todo_id}")
async def get_todo(todo_id: int):
    todo = find_todo(todo_id)  # your lookup logic
    if todo is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Todo with id {todo_id} not found"
        )
    return todo
```

Using `status` constants is preferred over raw numbers — it's more readable and self-documenting.

```python
from fastapi import status

status.HTTP_200_OK           # 200
status.HTTP_201_CREATED      # 201
status.HTTP_204_NO_CONTENT   # 204
status.HTTP_400_BAD_REQUEST  # 400
status.HTTP_401_UNAUTHORIZED # 401
status.HTTP_403_FORBIDDEN    # 403
status.HTTP_404_NOT_FOUND    # 404
status.HTTP_422_UNPROCESSABLE_ENTITY  # 422
status.HTTP_500_INTERNAL_SERVER_ERROR # 500
```

### Custom status code for success

```python
from fastapi import status

@app.post("/todos", status_code=status.HTTP_201_CREATED)
async def create_todo(todo: TodoCreate):
    new_todo = save_to_db(todo)
    return new_todo
```

---

## Automatic OpenAPI Documentation

When you run your FastAPI app, you get free interactive documentation:

- **Swagger UI** at `http://localhost:8000/docs`
  - Try out endpoints directly in the browser
  - See request/response schemas
  - Explore authentication

- **ReDoc** at `http://localhost:8000/redoc`
  - More structured, read-only documentation

- **OpenAPI JSON** at `http://localhost:8000/openapi.json`
  - Machine-readable spec (can be imported into Postman, etc.)

These are generated automatically from your type annotations and docstrings.

---

## Async vs Sync Handlers

FastAPI supports both async and sync handlers:

```python
# Async — preferred for I/O operations (database, HTTP requests, etc.)
@app.get("/async-example")
async def async_handler():
    result = await some_async_operation()
    return result

# Sync — use for CPU-bound work or when using sync libraries
@app.get("/sync-example")
def sync_handler():
    result = some_sync_operation()
    return result
```

For in-memory operations (like our todo app), either works fine. For database calls, always use `async`.

---

## APIRouter — Organizing Routes

As your app grows, you'll want to split routes into separate files using `APIRouter`:

```python
# routers/todos.py
from fastapi import APIRouter

router = APIRouter(
    prefix="/todos",
    tags=["todos"],
)

@router.get("/")
async def list_todos():
    return []

@router.post("/", status_code=201)
async def create_todo():
    return {}
```

```python
# main.py
from fastapi import FastAPI
from .routers import todos

app = FastAPI()
app.include_router(todos.router, prefix="/api/v1")
# Routes will be: GET /api/v1/todos, POST /api/v1/todos, etc.
```

---

## uvicorn — Running the Server

```bash
# Basic
uvicorn main:app

# With auto-reload (development)
uvicorn main:app --reload

# Change host/port
uvicorn main:app --host 0.0.0.0 --port 8080

# Package (e.g., code/main.py with code/__init__.py)
uvicorn code.main:app --reload
```

---

## Complete Example

```python
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel
from typing import Optional

app = FastAPI(title="Simple Note API")

# In-memory storage
notes: dict[int, dict] = {}
next_id = 1


class NoteCreate(BaseModel):
    title: str
    content: Optional[str] = None


class NoteResponse(BaseModel):
    id: int
    title: str
    content: Optional[str]


@app.get("/notes", response_model=list[NoteResponse])
async def list_notes():
    return list(notes.values())


@app.post("/notes", response_model=NoteResponse, status_code=status.HTTP_201_CREATED)
async def create_note(note: NoteCreate):
    global next_id
    new_note = {"id": next_id, "title": note.title, "content": note.content}
    notes[next_id] = new_note
    next_id += 1
    return new_note


@app.get("/notes/{note_id}", response_model=NoteResponse)
async def get_note(note_id: int):
    note = notes.get(note_id)
    if not note:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Note {note_id} not found",
        )
    return note


@app.delete("/notes/{note_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_note(note_id: int):
    if note_id not in notes:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")
    del notes[note_id]
```

---

## Summary

| Concept             | How It Works in FastAPI                              |
|---------------------|------------------------------------------------------|
| Path operations     | `@app.get/post/put/patch/delete("/path")`            |
| Path params         | `{id}` in route + `id: int` in function             |
| Query params        | Function args with defaults (not in path template)   |
| Request body        | Pydantic `BaseModel` as function arg type            |
| Error responses     | `raise HTTPException(status_code=..., detail=...)`  |
| Custom status codes | `@app.post("/", status_code=201)`                    |
| Router splitting    | `APIRouter` + `app.include_router()`                 |
| Running             | `uvicorn main:app --reload`                          |
| Interactive docs    | `http://localhost:8000/docs`                         |
