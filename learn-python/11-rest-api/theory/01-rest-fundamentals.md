# REST Fundamentals

## What is REST?

REST (Representational State Transfer) is an architectural style for designing networked applications. It was defined by Roy Fielding in his 2000 doctoral dissertation and has become the dominant approach for building web APIs.

A REST API exposes **resources** (things) over HTTP using a consistent set of conventions. Clients interact with those resources using standard HTTP methods.

---

## Core REST Principles

### 1. Stateless

Every request from client to server must contain all information needed to understand the request. The server holds no session state between requests.

**What this means in practice:**
- No server-side sessions
- Authentication credentials sent with every request (e.g., `Authorization: Bearer <token>`)
- Each request is independent and self-contained

```
# Bad (stateful):
POST /login         → server remembers you're logged in
GET  /dashboard     → server uses session to know who you are

# Good (stateless):
GET /dashboard      → with Authorization header each time
```

### 2. Resource-Based URLs

URLs identify **things** (nouns), not actions (verbs). Resources are identified by URIs (Uniform Resource Identifiers).

```
# Bad (action-based, not RESTful):
GET  /getUsers
POST /createUser
POST /deleteUser?id=5
GET  /getUserById?id=5

# Good (resource-based):
GET    /users           → list all users
POST   /users           → create a user
GET    /users/5         → get user with id 5
PUT    /users/5         → replace user 5
PATCH  /users/5         → partially update user 5
DELETE /users/5         → delete user 5
```

### 3. Uniform Interface

All REST APIs use the same HTTP methods with the same semantics:
- `GET` — retrieve
- `POST` — create
- `PUT` — replace (full update)
- `PATCH` — partial update
- `DELETE` — remove

This predictability is what makes REST easy to learn and use.

### 4. Client-Server Separation

The client and server are independent. The client doesn't care how the server stores data; the server doesn't care how the client displays it.

### 5. Layered System

Clients don't need to know if they're talking to the actual server or a proxy/load balancer/cache in between.

### 6. Cacheable

Responses should indicate whether they can be cached. `GET` responses are typically cacheable; `POST`/`DELETE` are not.

---

## HTTP Methods

| Method   | Action            | Idempotent | Safe | Body? |
|----------|-------------------|:----------:|:----:|:-----:|
| `GET`    | Retrieve          | Yes        | Yes  | No    |
| `POST`   | Create            | No         | No   | Yes   |
| `PUT`    | Full replace      | Yes        | No   | Yes   |
| `PATCH`  | Partial update    | No*        | No   | Yes   |
| `DELETE` | Delete            | Yes        | No   | Rarely|

**Idempotent** = calling it multiple times has the same effect as calling it once.  
**Safe** = it doesn't modify server state.

### GET — Retrieve a resource or collection

```http
GET /api/v1/todos          HTTP/1.1
GET /api/v1/todos/42       HTTP/1.1
```

### POST — Create a new resource

```http
POST /api/v1/todos         HTTP/1.1
Content-Type: application/json

{"title": "Buy groceries", "description": "Milk, eggs, bread"}
```

### PUT — Replace an entire resource

```http
PUT /api/v1/todos/42       HTTP/1.1
Content-Type: application/json

{"title": "Buy groceries", "description": "Updated list", "completed": false}
```

All fields must be provided — missing fields are set to null/default.

### PATCH — Partially update a resource

```http
PATCH /api/v1/todos/42     HTTP/1.1
Content-Type: application/json

{"completed": true}
```

Only the provided fields are updated.

### DELETE — Remove a resource

```http
DELETE /api/v1/todos/42    HTTP/1.1
```

---

## HTTP Status Codes

Status codes communicate what happened. Group them by their first digit:

| Range | Meaning      |
|-------|--------------|
| 2xx   | Success      |
| 3xx   | Redirection  |
| 4xx   | Client error |
| 5xx   | Server error |

### Common Codes You'll Use

#### 2xx Success

| Code | Name                  | When to Use                                          |
|------|-----------------------|------------------------------------------------------|
| 200  | OK                    | Successful GET, PUT, PATCH                           |
| 201  | Created               | Successful POST that created a resource              |
| 204  | No Content            | Successful DELETE (no body to return)                |

#### 4xx Client Errors

| Code | Name                  | When to Use                                          |
|------|-----------------------|------------------------------------------------------|
| 400  | Bad Request           | Malformed request syntax                             |
| 401  | Unauthorized          | Authentication required but not provided             |
| 403  | Forbidden             | Authenticated but not allowed to access this resource|
| 404  | Not Found             | Resource doesn't exist                               |
| 409  | Conflict              | Resource already exists, or state conflict           |
| 422  | Unprocessable Entity  | Validation failed (FastAPI uses this for Pydantic)   |

#### 5xx Server Errors

| Code | Name                  | When to Use                                          |
|------|-----------------------|------------------------------------------------------|
| 500  | Internal Server Error | Unexpected server-side error (bug)                   |
| 503  | Service Unavailable   | Server is overloaded or down for maintenance         |

---

## JSON Request and Response

REST APIs almost universally use JSON (JavaScript Object Notation) as their data format.

### Response structure

There's no universal standard, but common conventions:

```json
// Single resource
{
  "id": 1,
  "title": "Buy groceries",
  "completed": false,
  "created_at": "2025-01-15T10:30:00Z"
}

// Collection
[
  {"id": 1, "title": "Buy groceries", "completed": false},
  {"id": 2, "title": "Call dentist", "completed": true}
]

// Error response
{
  "detail": "Todo with id 99 not found"
}
```

### Always set Content-Type

```http
Content-Type: application/json
Accept: application/json
```

---

## URL Design Best Practices

### Use nouns, not verbs

```
# Bad
GET /getAllTodos
POST /createTodo
DELETE /removeTodo/5

# Good
GET /todos
POST /todos
DELETE /todos/5
```

### Use plural nouns for collections

```
/users          (not /user)
/todos          (not /todo)
/articles       (not /article)
```

### Use hierarchical URLs for nested resources

```
GET /users/5/orders          → orders belonging to user 5
GET /users/5/orders/99       → specific order 99 of user 5
POST /users/5/orders         → create order for user 5
```

Don't go deeper than 2-3 levels — it becomes hard to manage.

### Version your API

```
/api/v1/todos
/api/v2/todos
```

This lets you make breaking changes without breaking existing clients.

### Use lowercase with hyphens

```
# Bad
/TodoItems
/todo_items

# Good
/todo-items
```

---

## Query Parameters vs Path Parameters vs Request Body

These are three different ways to pass data to an API.

### Path Parameters

Used to **identify a specific resource**. Part of the URL itself.

```
GET /todos/42         → todo_id = 42
GET /users/5/posts/7  → user_id = 5, post_id = 7
```

Use when:
- You're identifying a specific resource
- The value is required to locate the resource

### Query Parameters

Used for **filtering, sorting, pagination, searching**. Appended after `?`.

```
GET /todos?completed=true
GET /todos?page=2&per_page=10
GET /todos?sort=created_at&order=desc
GET /articles?search=python&tag=tutorial
```

Use when:
- The value is optional
- You're filtering a collection
- You're adding search/sort/pagination

### Request Body

Used for **creating or updating resource data**. Sent as JSON in the request body.

```http
POST /todos
Content-Type: application/json

{
  "title": "Learn FastAPI",
  "description": "Read the docs and build a project",
  "priority": "high"
}
```

Use when:
- Sending data for `POST`, `PUT`, `PATCH`
- The data is complex or large
- You have multiple fields

### Summary

| Where          | Used For                          | Example                    |
|----------------|-----------------------------------|----------------------------|
| Path param     | Identifying specific resource     | `/todos/42`                |
| Query param    | Filtering, sorting, pagination    | `/todos?completed=true`    |
| Request body   | Creating/updating resource data   | `{"title": "..."}` in POST |

---

## Summary

| Concept              | Key Point                                          |
|----------------------|----------------------------------------------------|
| Stateless            | No server sessions; all info in each request       |
| Resource URLs        | Nouns, not verbs (`/todos` not `/getTodos`)        |
| HTTP methods         | GET/POST/PUT/PATCH/DELETE have specific meanings   |
| Status codes         | 2xx=success, 4xx=client error, 5xx=server error   |
| JSON                 | Standard data format for REST APIs                 |
| API versioning       | Use `/api/v1/` prefix                              |
