"""
Todos router — all endpoints for the /api/v1/todos resource.

Endpoints:
    GET    /todos              — list all todos (optional ?completed= filter)
    GET    /todos/{id}         — get a specific todo
    POST   /todos              — create a new todo (returns 201)
    PUT    /todos/{id}         — update a todo (full update)
    PATCH  /todos/{id}/complete — mark a todo as complete
    DELETE /todos/{id}         — delete a todo (returns 204)
"""

from fastapi import APIRouter, Depends, HTTPException, Query, Response, status

from ..dependencies import get_repository
from ..models import TodoCreate, TodoResponse, TodoUpdate
from ..repository import TodoRepository

router = APIRouter(
    prefix="/todos",
    tags=["todos"],
)


# ------------------------------------------------------------------
# GET /todos — list all todos
# ------------------------------------------------------------------


@router.get(
    "/",
    response_model=list[TodoResponse],
    summary="List all todos",
    response_description="A list of todos (may be empty)",
)
async def list_todos(
    completed: bool | None = Query(
        None,
        description="Filter by completion status. "
        "Omit to return all todos. "
        "Pass true/false to filter.",
    ),
    repo: TodoRepository = Depends(get_repository),
) -> list[TodoResponse]:
    """
    Retrieve all todos.

    - **completed=true**: only completed todos
    - **completed=false**: only pending todos
    - No filter: all todos
    """
    return repo.find_all(completed=completed)


# ------------------------------------------------------------------
# GET /todos/{todo_id} — get a single todo
# ------------------------------------------------------------------


@router.get(
    "/{todo_id}",
    response_model=TodoResponse,
    summary="Get a todo by ID",
    responses={404: {"description": "Todo not found"}},
)
async def get_todo(
    todo_id: int,
    repo: TodoRepository = Depends(get_repository),
) -> TodoResponse:
    """
    Retrieve a single todo by its numeric ID.

    Returns 404 if the todo does not exist.
    """
    todo = repo.find_by_id(todo_id)
    if todo is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Todo with id {todo_id} not found",
        )
    return todo


# ------------------------------------------------------------------
# POST /todos — create a new todo
# ------------------------------------------------------------------


@router.post(
    "/",
    response_model=TodoResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create a new todo",
    response_description="The newly created todo",
)
async def create_todo(
    todo: TodoCreate,
    repo: TodoRepository = Depends(get_repository),
) -> TodoResponse:
    """
    Create a new todo item.

    - **title**: required, 1–200 characters
    - **description**: optional

    Returns the created todo with its assigned `id` and `created_at` timestamp.
    """
    return repo.create(title=todo.title, description=todo.description)


# ------------------------------------------------------------------
# PUT /todos/{todo_id} — update a todo
# ------------------------------------------------------------------


@router.put(
    "/{todo_id}",
    response_model=TodoResponse,
    summary="Update a todo",
    responses={404: {"description": "Todo not found"}},
)
async def update_todo(
    todo_id: int,
    update: TodoUpdate,
    repo: TodoRepository = Depends(get_repository),
) -> TodoResponse:
    """
    Update an existing todo.

    You can update any combination of fields:
    - **title**: new title (1–200 chars)
    - **description**: new description (pass null to clear)
    - **completed**: new completion status

    Fields not provided in the request body remain unchanged.
    Returns 404 if the todo does not exist.
    """
    # Only pass the fields that were explicitly included in the request
    update_data = update.model_dump(exclude_unset=True)

    if not update_data:
        # Nothing to update — still return the current state
        todo = repo.find_by_id(todo_id)
        if todo is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Todo with id {todo_id} not found",
            )
        return todo

    updated = repo.update(todo_id, **update_data)
    if updated is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Todo with id {todo_id} not found",
        )
    return updated


# ------------------------------------------------------------------
# PATCH /todos/{todo_id}/complete — mark a todo as complete
# ------------------------------------------------------------------


@router.patch(
    "/{todo_id}/complete",
    response_model=TodoResponse,
    summary="Mark a todo as complete",
    responses={404: {"description": "Todo not found"}},
)
async def complete_todo(
    todo_id: int,
    repo: TodoRepository = Depends(get_repository),
) -> TodoResponse:
    """
    Mark a todo as completed.

    This is a convenience endpoint — equivalent to:
    `PUT /todos/{id}` with `{"completed": true}`.

    Returns 404 if the todo does not exist.
    """
    updated = repo.update(todo_id, completed=True)
    if updated is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Todo with id {todo_id} not found",
        )
    return updated


# ------------------------------------------------------------------
# DELETE /todos/{todo_id} — delete a todo
# ------------------------------------------------------------------


@router.delete(
    "/{todo_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete a todo",
    responses={
        204: {"description": "Todo successfully deleted"},
        404: {"description": "Todo not found"},
    },
)
async def delete_todo(
    todo_id: int,
    repo: TodoRepository = Depends(get_repository),
) -> Response:
    """
    Delete a todo by ID.

    Returns 204 No Content on success (no response body).
    Returns 404 if the todo does not exist.
    """
    deleted = repo.delete(todo_id)
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Todo with id {todo_id} not found",
        )
    # Return an explicit empty response with 204 status
    return Response(status_code=status.HTTP_204_NO_CONTENT)
