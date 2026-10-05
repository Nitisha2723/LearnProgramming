"""
pytest configuration and shared fixtures for the Todo API tests.

The key fixture here is 'client', which provides a fresh TestClient with an
empty repository for each test. This ensures test isolation — one test's
data cannot affect another test.
"""

import pytest
from fastapi.testclient import TestClient

from code.main import app
from code.dependencies import get_repository
from code.repository import TodoRepository


@pytest.fixture
def client() -> TestClient:
    """
    Provide a TestClient with a fresh, empty TodoRepository.

    By overriding the get_repository dependency, we replace the shared
    singleton repository (which persists between requests in production)
    with a brand-new empty one for this test only.

    The override is cleared after the test so it doesn't leak to other tests.
    """
    fresh_repo = TodoRepository()
    app.dependency_overrides[get_repository] = lambda: fresh_repo

    with TestClient(app) as test_client:
        yield test_client

    app.dependency_overrides.clear()
