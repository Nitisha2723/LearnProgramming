# Pydantic and Validation

## What is Pydantic?

Pydantic is a data validation library for Python. It uses type annotations to define the shape of data and validates/coerces values at runtime.

FastAPI uses Pydantic v2 for:
- Validating request bodies
- Validating query parameters
- Defining response shapes
- Serializing responses to JSON

---

## BaseModel Basics

```python
from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class User(BaseModel):
    id: int
    name: str
    email: str
    age: Optional[int] = None
    created_at: datetime = None
```

Pydantic validates data when you create an instance:

```python
# Valid
user = User(id=1, name="Alice", email="alice@example.com")
print(user.name)         # "Alice"
print(user.model_dump()) # {'id': 1, 'name': 'Alice', 'email': 'alice@example.com', 'age': None, ...}

# Invalid — raises ValidationError
user = User(id="not-a-number", name="Alice", email="alice@example.com")
```

---

## Field Types

Pydantic supports all standard Python types plus many more:

```python
from pydantic import BaseModel
from typing import Optional, List, Dict, Literal
from datetime import datetime, date
from uuid import UUID
from decimal import Decimal

class Example(BaseModel):
    # Primitive types
    name: str
    count: int
    price: float
    is_active: bool

    # Optional (can be None)
    description: Optional[str] = None

    # Collections
    tags: list[str] = []
    metadata: dict[str, str] = {}

    # Nested model
    address: "Address" = None

    # Constrained types
    status: Literal["active", "inactive", "pending"] = "active"

    # Date/time
    created_at: datetime
    birth_date: Optional[date] = None

    # UUID
    token: Optional[UUID] = None
```

---

## Field() — Constraints and Metadata

`Field()` adds validation constraints and documentation metadata to individual fields.

```python
from pydantic import BaseModel, Field
from typing import Optional

class TodoCreate(BaseModel):
    title: str = Field(
        ...,                    # ... means required (no default)
        min_length=1,           # minimum string length
        max_length=200,         # maximum string length
        description="The todo title",  # shows in OpenAPI docs
        examples=["Buy groceries"],    # example value in docs
    )
    description: Optional[str] = Field(
        None,
        max_length=1000,
    )
    priority: int = Field(
        default=1,
        ge=1,    # greater than or equal to 1
        le=5,    # less than or equal to 5
        description="Priority level from 1 (low) to 5 (high)",
    )
    price: float = Field(..., gt=0, description="Must be positive")
```

### Common Field constraints

| Constraint | Applies To    | Meaning                         |
|------------|---------------|---------------------------------|
| `min_length` | str         | Minimum string length           |
| `max_length` | str         | Maximum string length           |
| `pattern`  | str           | Regex pattern the string must match |
| `ge`       | int, float    | Greater than or equal to        |
| `gt`       | int, float    | Strictly greater than           |
| `le`       | int, float    | Less than or equal to           |
| `lt`       | int, float    | Strictly less than              |
| `min_items`| list          | Minimum number of items         |
| `max_items`| list          | Maximum number of items         |

---

## model_validator — Cross-Field Validation

Sometimes you need to validate across multiple fields at once. Use `@model_validator`.

```python
from pydantic import BaseModel, model_validator
from typing import Optional

class DateRange(BaseModel):
    start_date: str
    end_date: str

    @model_validator(mode="after")
    def check_dates(self):
        if self.end_date < self.start_date:
            raise ValueError("end_date must be after start_date")
        return self

class CreateEvent(BaseModel):
    name: str
    max_attendees: Optional[int] = None
    is_unlimited: bool = False

    @model_validator(mode="after")
    def check_capacity(self):
        if not self.is_unlimited and self.max_attendees is None:
            raise ValueError(
                "max_attendees is required when is_unlimited is False"
            )
        return self
```

The `mode="after"` validator runs after all fields are validated individually. You can also use `mode="before"` to transform raw input before field validation.

---

## field_validator — Single-Field Validation

For validating or transforming a single field:

```python
from pydantic import BaseModel, field_validator

class User(BaseModel):
    username: str
    email: str
    age: int

    @field_validator("username")
    @classmethod
    def username_alphanumeric(cls, v: str) -> str:
        if not v.replace("_", "").replace("-", "").isalnum():
            raise ValueError("username must be alphanumeric (underscores and hyphens allowed)")
        return v.lower()  # normalize to lowercase

    @field_validator("email")
    @classmethod
    def email_has_at(cls, v: str) -> str:
        if "@" not in v:
            raise ValueError("invalid email address")
        return v.lower()

    @field_validator("age")
    @classmethod
    def age_must_be_adult(cls, v: int) -> int:
        if v < 18:
            raise ValueError("must be 18 or older")
        return v
```

---

## Request vs Response Models — Separating Schemas

A critical pattern: use **different models for requests and responses**.

Why? Because:
- You never want to expose internal fields (password hashes, internal IDs)
- Request fields differ from response fields (e.g., `id` and `created_at` are set by server, not client)
- You can evolve request and response independently

```python
from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional

# What the CLIENT sends when creating a todo
class TodoCreate(BaseModel):
    title: str = Field(..., min_length=1, max_length=200)
    description: Optional[str] = None

# What the CLIENT sends when updating a todo (all fields optional)
class TodoUpdate(BaseModel):
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = None
    completed: Optional[bool] = None

# What the SERVER returns (includes server-generated fields)
class TodoResponse(BaseModel):
    id: int
    title: str
    description: Optional[str]
    completed: bool
    created_at: datetime

# NEVER expose internal storage details:
class TodoInternal(BaseModel):
    id: int
    title: str
    description: Optional[str]
    completed: bool
    created_at: datetime
    _deleted: bool = False          # internal, never returned
    _owner_user_id: int = None      # internal, never returned
```

---

## response_model — Controlling API Output

Use `response_model` in the path operation decorator to:
1. Filter out fields not in the response model
2. Validate that your code returns the right shape
3. Generate accurate OpenAPI docs

```python
from fastapi import FastAPI

app = FastAPI()

@app.get("/todos/{todo_id}", response_model=TodoResponse)
async def get_todo(todo_id: int):
    # This might have extra internal fields
    todo = get_from_db(todo_id)
    # FastAPI will filter it through TodoResponse before returning
    return todo


@app.get("/todos", response_model=list[TodoResponse])
async def list_todos():
    todos = get_all_from_db()
    return todos  # list of dicts or TodoInternal objects, filtered to TodoResponse
```

This means even if your internal representation has `_owner_user_id`, it won't appear in the API response.

---

## model_dump() and model_dump(exclude_unset=True)

```python
todo = TodoUpdate(title="New title")  # only title provided

# Default: includes all fields (even unset ones as None)
todo.model_dump()
# {"title": "New title", "description": None, "completed": None}

# exclude_unset: only fields explicitly provided by the caller
todo.model_dump(exclude_unset=True)
# {"title": "New title"}
```

`exclude_unset=True` is essential for PATCH endpoints — you only want to update the fields the client actually sent:

```python
@app.patch("/todos/{todo_id}", response_model=TodoResponse)
async def update_todo(todo_id: int, update: TodoUpdate):
    existing = get_todo(todo_id)
    if not existing:
        raise HTTPException(status_code=404)

    # Only update fields that were explicitly provided
    update_data = update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(existing, key, value)

    return existing
```

---

## Nested Models

```python
from pydantic import BaseModel
from typing import Optional

class Address(BaseModel):
    street: str
    city: str
    country: str
    postal_code: Optional[str] = None

class UserCreate(BaseModel):
    name: str
    email: str
    address: Optional[Address] = None

# FastAPI handles nested validation automatically
@app.post("/users")
async def create_user(user: UserCreate):
    return user
```

Request body:
```json
{
  "name": "Alice",
  "email": "alice@example.com",
  "address": {
    "street": "123 Main St",
    "city": "Anytown",
    "country": "US"
  }
}
```

---

## Validation Errors (422 Response)

When validation fails, FastAPI returns a 422 response with details:

```json
{
  "detail": [
    {
      "type": "string_too_short",
      "loc": ["body", "title"],
      "msg": "String should have at least 1 character",
      "input": "",
      "ctx": {"min_length": 1}
    }
  ]
}
```

Each error has:
- `type` — the validation error type
- `loc` — where in the request the error occurred (`["body", "title"]`)
- `msg` — human-readable error message
- `input` — the value that failed validation

---

## Complete Example

```python
from pydantic import BaseModel, Field, model_validator, field_validator
from typing import Optional, Literal
from datetime import datetime

class UserCreate(BaseModel):
    """Schema for creating a new user."""
    username: str = Field(..., min_length=3, max_length=50)
    email: str = Field(..., description="User's email address")
    password: str = Field(..., min_length=8)
    password_confirm: str
    role: Literal["admin", "user", "moderator"] = "user"
    age: Optional[int] = Field(None, ge=13, le=120)

    @field_validator("email")
    @classmethod
    def email_lowercase(cls, v: str) -> str:
        if "@" not in v:
            raise ValueError("Invalid email format")
        return v.lower()

    @model_validator(mode="after")
    def passwords_match(self):
        if self.password != self.password_confirm:
            raise ValueError("Passwords do not match")
        return self


class UserResponse(BaseModel):
    """Schema for returning user data — never includes password."""
    id: int
    username: str
    email: str
    role: str
    created_at: datetime

    model_config = {"from_attributes": True}  # allows creating from ORM objects
```

---

## Summary

| Feature               | How to Use                                              |
|-----------------------|---------------------------------------------------------|
| Basic model           | `class Foo(BaseModel): field: type`                    |
| Required field        | `field: str = Field(...)`                               |
| Optional field        | `field: Optional[str] = None`                           |
| String constraints    | `Field(..., min_length=1, max_length=200)`              |
| Number constraints    | `Field(..., ge=0, le=100)`                              |
| Cross-field validation| `@model_validator(mode="after")`                        |
| Single-field validation| `@field_validator("field_name")`                       |
| Response filtering    | `@app.get("/", response_model=ResponseModel)`           |
| PATCH support         | `update.model_dump(exclude_unset=True)`                 |
| Serialize to dict     | `instance.model_dump()`                                 |
