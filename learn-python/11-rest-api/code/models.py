"""
Pydantic schemas (models) for the Todo API.

Three models:
- TodoCreate   — what the client sends when creating a todo
- TodoUpdate   — what the client sends when updating (all fields optional)
- TodoResponse — what the server returns to the client
"""

from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional


class TodoCreate(BaseModel):
    """
    Request schema for creating a new todo.

    Only the client-supplied fields — the server generates id, created_at, etc.
    """

    title: str = Field(
        ...,
        min_length=1,
        max_length=200,
        description="The todo title. Cannot be empty.",
        examples=["Buy groceries"],
    )
    description: Optional[str] = Field(
        None,
        max_length=1000,
        description="Optional longer description.",
        examples=["Milk, eggs, bread, and coffee"],
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "title": "Buy groceries",
                "description": "Milk, eggs, bread, and coffee",
            }
        }
    }


class TodoUpdate(BaseModel):
    """
    Request schema for updating an existing todo.

    All fields are optional — only provided fields are changed.
    This enables both PUT (full update) and PATCH (partial update).
    """

    title: Optional[str] = Field(
        None,
        min_length=1,
        max_length=200,
        description="New title. If omitted, the existing title is kept.",
    )
    description: Optional[str] = Field(
        None,
        description="New description. Pass null to clear the description.",
    )
    completed: Optional[bool] = Field(
        None,
        description="Set completion status. If omitted, status is unchanged.",
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "title": "Buy groceries — updated list",
                "completed": True,
            }
        }
    }


class TodoResponse(BaseModel):
    """
    Response schema returned to clients.

    This controls exactly what fields appear in the API response.
    Internal storage details are never exposed.
    """

    id: int = Field(description="Unique identifier assigned by the server.")
    title: str
    description: Optional[str]
    completed: bool
    created_at: datetime = Field(description="ISO 8601 timestamp of creation.")

    model_config = {
        "json_schema_extra": {
            "example": {
                "id": 1,
                "title": "Buy groceries",
                "description": "Milk, eggs, bread",
                "completed": False,
                "created_at": "2025-01-15T10:30:00",
            }
        }
    }
