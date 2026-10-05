# Mini-Project: Build a Recipe API

## Overview

Build a complete REST API for managing cooking recipes. This project pulls together everything from this module: FastAPI, Pydantic validation, in-memory storage, HTTP methods, and status codes.

---

## Learning Goals

By completing this project you will:
- Design a REST API from a written spec
- Model complex data with nested Pydantic models
- Implement search and filtering endpoints
- Write a complete test suite
- Use dependency injection for clean architecture

---

## Data Model

### Recipe

| Field          | Type            | Required | Constraints                       |
|----------------|-----------------|----------|-----------------------------------|
| `id`           | int             | server   | Auto-assigned                     |
| `name`         | str             | yes      | 1–200 chars                       |
| `description`  | str             | no       | Max 1000 chars                    |
| `ingredients`  | list of str     | yes      | At least 1 item                   |
| `instructions` | str             | yes      | Min 10 chars                      |
| `prep_time`    | int             | yes      | Minutes, must be >= 1             |
| `cook_time`    | int             | no       | Minutes, must be >= 0             |
| `servings`     | int             | no       | Default 1, must be >= 1           |
| `vegetarian`   | bool            | no       | Default False                     |
| `created_at`   | datetime        | server   | Auto-assigned                     |

---

## Endpoints to Implement

### Core CRUD

| Method | Path                | Description                          | Status |
|--------|---------------------|--------------------------------------|--------|
| POST   | `/recipes`          | Create a new recipe                  | 201    |
| GET    | `/recipes`          | List all recipes                     | 200    |
| GET    | `/recipes/{id}`     | Get a specific recipe                | 200    |
| PUT    | `/recipes/{id}`     | Update a recipe                      | 200    |
| DELETE | `/recipes/{id}`     | Delete a recipe                      | 204    |

### Search and Filtering

| Method | Path                         | Description                               |
|--------|------------------------------|-------------------------------------------|
| GET    | `/recipes?vegetarian=true`   | Filter vegetarian recipes                 |
| GET    | `/recipes?max_prep_time=30`  | Filter by max prep time (minutes)         |
| GET    | `/recipes/search?q=pasta`    | Search by name or ingredients             |

**Important**: Define `/recipes/search` before `/recipes/{id}` in your router.

---

## Pydantic Schemas

Design your own schemas. Here are the three you'll need:

### RecipeCreate (request body for POST)
- All fields the client provides
- Proper validation constraints

### RecipeUpdate (request body for PUT)
- All fields optional (PATCH-style behaviour)

### RecipeResponse (returned to clients)
- All fields including server-assigned ones (`id`, `created_at`)

---

## Project Structure

```
mini-project/
├── README.md           # this file
└── recipe_api.py       # your implementation (single file is fine)
```

---

## Step-by-Step Guide

### Step 1: Define your schemas

```python
from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional

class RecipeCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=200)
    ingredients: list[str] = Field(..., min_length=1)
    instructions: str = Field(..., min_length=10)
    prep_time: int = Field(..., ge=1)
    # ... add the rest
```

### Step 2: Create the repository

Follow the same pattern as `code/repository.py`:
- A dict keyed by integer ID
- A threading.Lock for safety
- Methods: `find_all`, `find_by_id`, `create`, `update`, `delete`
- Add a `search(query)` method that checks name and ingredients

### Step 3: Implement the router

```python
from fastapi import APIRouter, Depends, HTTPException, Query, status

router = APIRouter(prefix="/recipes", tags=["recipes"])

# Remember: /recipes/search must come BEFORE /recipes/{id}
@router.get("/search", ...)
async def search_recipes(q: str = Query(..., min_length=1), ...):
    ...

@router.get("/{recipe_id}", ...)
async def get_recipe(recipe_id: int, ...):
    ...
```

### Step 4: Add filtering to GET /recipes

```python
@router.get("/")
async def list_recipes(
    vegetarian: Optional[bool] = None,
    max_prep_time: Optional[int] = Query(None, ge=1),
    ...
):
    ...
```

### Step 5: Write tests

At minimum, test:
- Creating a recipe with valid data → 201
- Creating a recipe with missing required fields → 422
- Getting a recipe that doesn't exist → 404
- Filtering by `?vegetarian=true`
- Searching with `?q=pasta`
- Deleting a recipe → 204

---

## Example API Requests

```bash
# Start the server
uvicorn mini-project.recipe_api:app --reload --port 8002

# Create a recipe
curl -X POST http://localhost:8002/recipes \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Spaghetti Carbonara",
    "ingredients": ["spaghetti", "eggs", "pancetta", "parmesan", "black pepper"],
    "instructions": "Cook pasta. Fry pancetta. Mix eggs and cheese. Combine off heat.",
    "prep_time": 10,
    "cook_time": 20,
    "servings": 4,
    "vegetarian": false
  }'

# List all recipes
curl http://localhost:8002/recipes

# Filter vegetarian
curl "http://localhost:8002/recipes?vegetarian=true"

# Filter by prep time
curl "http://localhost:8002/recipes?max_prep_time=30"

# Search
curl "http://localhost:8002/recipes/search?q=pasta"

# Get specific recipe
curl http://localhost:8002/recipes/1

# Update a recipe
curl -X PUT http://localhost:8002/recipes/1 \
  -H "Content-Type: application/json" \
  -d '{"servings": 2}'

# Delete a recipe
curl -X DELETE http://localhost:8002/recipes/1
```

---

## Stretch Goals

1. **Ingredient count filter**: `GET /recipes?max_ingredients=5` — recipes with 5 or fewer ingredients
2. **Total time**: Add a computed property `total_time = prep_time + (cook_time or 0)` to the response
3. **Tag system**: Add a `tags` field (like in Exercise 1) and a `GET /recipes/tags` endpoint
4. **Sorting**: `GET /recipes?sort=prep_time&order=asc`
5. **Pagination**: `GET /recipes?page=1&per_page=10`

---

## Success Criteria

Your implementation is complete when:
- All 8 endpoints work correctly
- POST returns 201 with the created recipe
- GET list supports `?vegetarian=` and `?max_prep_time=` filters
- GET search returns results matching name or ingredients
- DELETE returns 204 with no body
- All error cases return appropriate 4xx codes
- You have at least 10 passing tests
