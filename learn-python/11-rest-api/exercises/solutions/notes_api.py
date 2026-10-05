"""
Solution for Exercise 1: Notes API with Tags

A complete FastAPI application demonstrating:
- Resource-based URL design
- Pydantic schemas for request/response
- In-memory repository with thread safety
- Optional filtering (tag filter)
- URL ordering (fixed paths before parameterized)

Run with:
    uvicorn exercises.solutions.notes_api:app --reload --port 8001
"""

from datetime import datetime
from functools import lru_cache
from threading import Lock
from typing import Optional

from fastapi import APIRouter, Depends, FastAPI, HTTPException, Query, Response, status
from pydantic import BaseModel, Field, field_validator

# ============================================================
# Pydantic Schemas
# ============================================================


class NoteCreate(BaseModel):
    """Request schema for creating a note."""

    title: str = Field(..., min_length=1, max_length=200, description="Note title")
    content: Optional[str] = Field(None, description="Note body text")
    tags: list[str] = Field(default_factory=list, description="Tag list")

    @field_validator("tags", mode="before")
    @classmethod
    def normalise_tags(cls, tags: list) -> list[str]:
        """Lowercase all tags and strip whitespace."""
        return [str(t).strip().lower() for t in tags if str(t).strip()]


class NoteUpdate(BaseModel):
    """Request schema for updating a note — all fields optional."""

    title: Optional[str] = Field(None, min_length=1, max_length=200)
    content: Optional[str] = None
    tags: Optional[list[str]] = None

    @field_validator("tags", mode="before")
    @classmethod
    def normalise_tags(cls, tags: Optional[list]) -> Optional[list[str]]:
        if tags is None:
            return None
        return [str(t).strip().lower() for t in tags if str(t).strip()]


class NoteResponse(BaseModel):
    """Response schema — what clients receive."""

    id: int
    title: str
    content: Optional[str]
    tags: list[str]
    created_at: datetime


# ============================================================
# In-memory Repository
# ============================================================


class NoteRepository:
    def __init__(self) -> None:
        self._notes: dict[int, dict] = {}
        self._next_id: int = 1
        self._lock = Lock()

    def find_all(self, tag: Optional[str] = None) -> list[NoteResponse]:
        with self._lock:
            items = list(self._notes.values())
        if tag:
            items = [n for n in items if tag in n["tags"]]
        return [NoteResponse(**n) for n in items]

    def find_by_id(self, note_id: int) -> Optional[NoteResponse]:
        with self._lock:
            item = self._notes.get(note_id)
        return NoteResponse(**item) if item else None

    def create(
        self,
        title: str,
        content: Optional[str],
        tags: list[str],
    ) -> NoteResponse:
        with self._lock:
            note_id = self._next_id
            self._next_id += 1
            note = {
                "id": note_id,
                "title": title,
                "content": content,
                "tags": tags,
                "created_at": datetime.now(),
            }
            self._notes[note_id] = note
        return NoteResponse(**note)

    def update(self, note_id: int, **fields) -> Optional[NoteResponse]:
        with self._lock:
            note = self._notes.get(note_id)
            if note is None:
                return None
            for key, value in fields.items():
                note[key] = value
            updated = dict(note)
        return NoteResponse(**updated)

    def delete(self, note_id: int) -> bool:
        with self._lock:
            if note_id not in self._notes:
                return False
            del self._notes[note_id]
            return True

    def all_tags(self) -> list[str]:
        """Return a sorted list of all unique tags currently in use."""
        with self._lock:
            tag_set: set[str] = set()
            for note in self._notes.values():
                tag_set.update(note["tags"])
        return sorted(tag_set)


# ============================================================
# Dependency Injection
# ============================================================


@lru_cache(maxsize=1)
def get_note_repository() -> NoteRepository:
    return NoteRepository()


# ============================================================
# Router
# ============================================================

router = APIRouter(prefix="/notes", tags=["notes"])


@router.get("/tags", response_model=list[str], summary="List all tags in use")
async def list_tags(
    repo: NoteRepository = Depends(get_note_repository),
) -> list[str]:
    """
    Return a sorted list of all unique tags currently assigned to notes.

    This endpoint must be defined BEFORE /notes/{id} so that the path
    segment "tags" is not interpreted as an integer note ID.
    """
    return repo.all_tags()


@router.get("/", response_model=list[NoteResponse], summary="List all notes")
async def list_notes(
    tag: Optional[str] = Query(None, description="Filter notes by tag"),
    repo: NoteRepository = Depends(get_note_repository),
) -> list[NoteResponse]:
    """
    Retrieve all notes.

    Use `?tag=python` to return only notes that include that tag.
    """
    return repo.find_all(tag=tag)


@router.get("/{note_id}", response_model=NoteResponse, summary="Get a note")
async def get_note(
    note_id: int,
    repo: NoteRepository = Depends(get_note_repository),
) -> NoteResponse:
    """Get a specific note by its ID. Returns 404 if not found."""
    note = repo.find_by_id(note_id)
    if note is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Note with id {note_id} not found",
        )
    return note


@router.post(
    "/",
    response_model=NoteResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create a note",
)
async def create_note(
    note: NoteCreate,
    repo: NoteRepository = Depends(get_note_repository),
) -> NoteResponse:
    """Create a new note. Returns 201 with the created note."""
    return repo.create(title=note.title, content=note.content, tags=note.tags)


@router.put("/{note_id}", response_model=NoteResponse, summary="Update a note")
async def update_note(
    note_id: int,
    update: NoteUpdate,
    repo: NoteRepository = Depends(get_note_repository),
) -> NoteResponse:
    """
    Update an existing note.

    Only provided fields are changed.
    Returns 404 if the note does not exist.
    """
    update_data = update.model_dump(exclude_unset=True)

    if not update_data:
        note = repo.find_by_id(note_id)
        if note is None:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")
        return note

    updated = repo.update(note_id, **update_data)
    if updated is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Note with id {note_id} not found",
        )
    return updated


@router.delete(
    "/{note_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Delete a note",
)
async def delete_note(
    note_id: int,
    repo: NoteRepository = Depends(get_note_repository),
) -> Response:
    """Delete a note. Returns 204 with no body on success."""
    deleted = repo.delete(note_id)
    if not deleted:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Note with id {note_id} not found",
        )
    return Response(status_code=status.HTTP_204_NO_CONTENT)


# ============================================================
# Application
# ============================================================

app = FastAPI(
    title="Notes API",
    description="Exercise 1 solution: a Notes API with tag support.",
    version="1.0.0",
)

app.include_router(router)


@app.get("/health", tags=["health"])
async def health_check() -> dict:
    return {"status": "ok"}
