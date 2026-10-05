# Testing Deep Dive

This module builds on the pytest basics from earlier modules and dives into
mocking, advanced fixtures, configuration, and code coverage.

---

## Test Doubles in Python: `unittest.mock`

When testing code that depends on external systems (files, databases, HTTP, time),
you replace those dependencies with "test doubles" — objects that simulate the real thing.

### Types of Test Doubles

| Term       | What it does                                      |
|------------|---------------------------------------------------|
| **Mock**   | Records all calls; you assert what was called     |
| **Stub**   | Returns fixed values; ignores calls it doesn't care about |
| **Spy**    | Wraps the real thing; records calls but still calls it |
| **Fake**   | Simpler working implementation (in-memory DB, etc.)|

Python's `unittest.mock` provides `Mock` and `MagicMock` that can do all of these.

---

## `Mock` and `MagicMock`

```python
from unittest.mock import Mock, MagicMock

m = Mock()

# Mock auto-creates attributes and methods on access
m.some_method(1, 2, 3)
m.attribute = "value"

# Assert on calls
m.some_method.assert_called_once_with(1, 2, 3)
m.some_method.assert_called()

# Return values
m.return_value = 42
result = m()     # 42

m.method.return_value = "hello"
print(m.method())    # "hello"

# Side effects
m.method.side_effect = ValueError("Something failed")
# m.method()  ← raises ValueError

# Side effect as function
m.method.side_effect = lambda x: x * 2
print(m.method(5))   # 10
```

### MagicMock

`MagicMock` extends `Mock` by supporting dunder methods:

```python
m = MagicMock()
len(m)           # Works (returns Mock)
m[0]             # Works
m + 1            # Works
with m:          # Works — supports __enter__/__exit__
    pass

# Configure return values
m.__len__.return_value = 5
print(len(m))    # 5

m.__getitem__.return_value = "item"
print(m[42])     # "item"
```

---

## `patch` — Replace Objects in Tests

`patch` temporarily replaces an object in a module with a mock during the test.

### As a decorator

```python
from unittest.mock import patch

@patch("mymodule.requests.get")
def test_api_call(mock_get):
    # mock_get replaces requests.get for the duration of this test
    mock_get.return_value.json.return_value = {"users": ["alice", "bob"]}
    mock_get.return_value.status_code = 200

    result = mymodule.fetch_users()

    mock_get.assert_called_once_with("https://api.example.com/users")
    assert result == ["alice", "bob"]
```

### As a context manager

```python
with patch("mymodule.open", mock_open(read_data="line1\nline2\n")) as mock_f:
    result = mymodule.read_config()
    mock_f.assert_called_once_with("config.txt")
```

### `patch.object` — Patch a Method on an Existing Object

```python
class MyService:
    def fetch_data(self):
        return requests.get("...").json()

service = MyService()

with patch.object(service, "fetch_data", return_value={"key": "value"}):
    result = service.fetch_data()
    assert result == {"key": "value"}
```

---

## Mocking Common Dependencies

### Mocking File I/O

```python
from unittest.mock import patch, mock_open

def read_config(path):
    with open(path) as f:
        return json.load(f)

def test_read_config():
    fake_content = '{"host": "localhost", "port": 5432}'

    with patch("builtins.open", mock_open(read_data=fake_content)):
        result = read_config("config.json")

    assert result == {"host": "localhost", "port": 5432}
```

### Mocking HTTP Requests

```python
import requests
from unittest.mock import patch, Mock

def fetch_user(user_id):
    response = requests.get(f"https://api.example.com/users/{user_id}")
    response.raise_for_status()
    return response.json()

def test_fetch_user():
    mock_response = Mock()
    mock_response.json.return_value = {"id": 1, "name": "Alice"}
    mock_response.status_code = 200
    mock_response.raise_for_status = Mock()   # Do nothing

    with patch("requests.get", return_value=mock_response) as mock_get:
        result = fetch_user(1)
        mock_get.assert_called_once_with("https://api.example.com/users/1")
        assert result["name"] == "Alice"
```

### Mocking `datetime.now()`

```python
from datetime import datetime
from unittest.mock import patch

def get_greeting():
    hour = datetime.now().hour
    if hour < 12:
        return "Good morning"
    elif hour < 17:
        return "Good afternoon"
    return "Good evening"

def test_morning_greeting():
    fake_now = datetime(2024, 1, 15, 9, 30)   # 9:30 AM
    with patch("mymodule.datetime") as mock_dt:
        mock_dt.now.return_value = fake_now
        assert get_greeting() == "Good morning"

def test_evening_greeting():
    fake_now = datetime(2024, 1, 15, 19, 0)   # 7:00 PM
    with patch("mymodule.datetime") as mock_dt:
        mock_dt.now.return_value = fake_now
        assert get_greeting() == "Good evening"
```

---

## `pytest-mock` — Simpler Mocking

The `pytest-mock` plugin provides a `mocker` fixture that integrates better with pytest:

```bash
pip install pytest-mock
```

```python
def test_api_call(mocker):
    mock_get = mocker.patch("mymodule.requests.get")
    mock_get.return_value.json.return_value = {"users": ["alice"]}

    result = mymodule.fetch_users()

    mock_get.assert_called_once()
    assert result == ["alice"]

# spy — wrap the real function to track calls
def test_with_spy(mocker):
    spy = mocker.spy(mymodule, "helper_function")
    mymodule.do_something()
    assert spy.call_count == 1
```

The `mocker` fixture automatically undoes all patches at the end of the test —
no need for `with` or `@patch` decorators.

---

## pytest Fixtures: Advanced Patterns

### Fixture Scope

By default, a fixture runs before each test. You can change this:

```python
import pytest

@pytest.fixture(scope="function")   # default — once per test
def func_fixture():
    return "function-scoped"

@pytest.fixture(scope="class")      # once per test class
def class_fixture():
    return "class-scoped"

@pytest.fixture(scope="module")     # once per test file
def module_fixture():
    db = create_test_database()
    yield db
    db.cleanup()

@pytest.fixture(scope="session")    # once per entire test run
def session_fixture():
    server = start_test_server()
    yield server
    server.stop()
```

**Use wider scopes for expensive setup** (database connections, server startup).

### Fixture Teardown with `yield`

```python
@pytest.fixture
def temp_directory():
    import tempfile
    import shutil
    tmpdir = tempfile.mkdtemp()
    yield tmpdir              # Test runs here
    shutil.rmtree(tmpdir)     # Cleanup runs after test
```

### Fixtures Using Other Fixtures

```python
@pytest.fixture
def db_connection():
    conn = create_connection("sqlite:///:memory:")
    yield conn
    conn.close()

@pytest.fixture
def user_table(db_connection):    # Inject db_connection
    db_connection.execute("CREATE TABLE users (id INT, name TEXT)")
    yield db_connection
    db_connection.execute("DROP TABLE users")
```

### Parametrized Fixtures

```python
@pytest.fixture(params=["sqlite", "postgres", "mysql"])
def database(request):
    db = create_db(request.param)
    yield db
    db.close()

# This test will run 3 times, once for each database type
def test_insert(database):
    database.insert({"id": 1, "name": "Alice"})
    result = database.query("SELECT * FROM users")
    assert len(result) == 1
```

---

## `conftest.py` — Shared Fixtures

`conftest.py` is automatically loaded by pytest. Fixtures defined there are
available to all tests in the same directory and subdirectories.

```
tests/
├── conftest.py          ← shared fixtures for all tests
├── test_users.py
├── test_orders.py
└── integration/
    ├── conftest.py      ← shared fixtures for integration tests only
    └── test_api.py
```

```python
# tests/conftest.py
import pytest
from myapp import create_app, db

@pytest.fixture(scope="session")
def app():
    app = create_app({"TESTING": True, "DATABASE": ":memory:"})
    yield app

@pytest.fixture(scope="function")
def client(app):
    return app.test_client()

@pytest.fixture(scope="function")
def db_session(app):
    with app.app_context():
        db.create_all()
        yield db.session
        db.session.remove()
        db.drop_all()
```

---

## `pytest.ini` Configuration

```ini
# pytest.ini (or pyproject.toml [tool.pytest.ini_options])
[pytest]
testpaths = tests
addopts = --strict-markers -v
markers =
    slow: marks tests as slow (deselect with -m "not slow")
    integration: marks integration tests
    unit: marks unit tests
filterwarnings =
    error
    ignore::DeprecationWarning:some_library
```

Run only unit tests: `pytest -m unit`
Skip slow tests: `pytest -m "not slow"`

---

## Code Coverage with `pytest-cov`

```bash
pip install pytest-cov

# Run tests with coverage
pytest --cov=mypackage tests/

# Generate HTML report
pytest --cov=mypackage --cov-report=html tests/
# Open htmlcov/index.html

# Fail if coverage below threshold
pytest --cov=mypackage --cov-fail-under=80 tests/
```

```ini
# .coveragerc
[run]
source = mypackage
omit =
    */tests/*
    */migrations/*

[report]
exclude_lines =
    if TYPE_CHECKING:
    raise NotImplementedError
    pragma: no cover
```

---

## Property-Based Testing with Hypothesis

Hypothesis generates test cases automatically, finding edge cases you didn't think of.

```bash
pip install hypothesis
```

```python
from hypothesis import given, strategies as st

@given(st.lists(st.integers()))
def test_sort_length_preserved(lst):
    """Sorting a list should not change its length."""
    sorted_lst = sorted(lst)
    assert len(sorted_lst) == len(lst)

@given(st.text())
def test_reverse_twice_is_identity(text):
    """Reversing a string twice gives back the original."""
    assert text[::-1][::-1] == text

@given(
    st.integers(min_value=1),
    st.integers(min_value=1),
)
def test_gcd_properties(a, b):
    from math import gcd
    g = gcd(a, b)
    assert a % g == 0    # g divides a
    assert b % g == 0    # g divides b
```

Hypothesis runs your test with many generated inputs and shrinks failing cases
to the minimal reproducer.

---

## Integration Tests

Unit tests test functions in isolation. Integration tests test how components work together.

```python
# tests/integration/test_pipeline.py
import pytest
from pathlib import Path

@pytest.fixture
def sample_csv(tmp_path):
    """Create a real CSV file for integration testing."""
    csv_file = tmp_path / "students.csv"
    csv_file.write_text(
        "name,math,science,english\n"
        "Alice,90,85,88\n"
        "Bob,75,92,80\n"
        "Carol,88,79,95\n"
    )
    return csv_file

def test_full_pipeline(sample_csv):
    """Test the entire CSV processing pipeline end-to-end."""
    from myapp.pipeline import process_grades_csv

    result = process_grades_csv(sample_csv)

    assert result["student_count"] == 3
    assert result["top_student"] == "Carol"
    assert abs(result["class_average"] - 85.1) < 0.5
```

Use `tmp_path` (a built-in pytest fixture) for temporary directories that are
automatically cleaned up after the test.
