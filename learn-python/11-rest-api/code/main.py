"""
Todo API — main application entry point.

Run with:
    uvicorn code.main:app --reload

Then open:
    http://localhost:8000/docs  — interactive API docs (Swagger UI)
    http://localhost:8000/redoc — alternative docs (ReDoc)
"""

from fastapi import FastAPI
from .routers import todos

app = FastAPI(
    title="Todo API",
    description=(
        "A fully-featured Todo REST API built with FastAPI. "
        "Demonstrates path parameters, query parameters, request/response "
        "models, dependency injection, and proper HTTP status codes."
    ),
    version="1.0.0",
)

# Include the todos router — all its routes will be prefixed with /api/v1
app.include_router(todos.router, prefix="/api/v1")


@app.get("/health", tags=["health"])
async def health_check() -> dict:
    """Health check endpoint — returns 200 when the service is running."""
    return {"status": "ok"}
