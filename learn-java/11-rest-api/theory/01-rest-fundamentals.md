# REST Fundamentals

## What is REST?

REST (Representational State Transfer) is an architectural style for designing networked applications. Coined by Roy Fielding in 2000, REST defines a set of constraints that — when followed — produce a web service that is scalable, simple, and maintainable.

A service that follows REST conventions is called **RESTful**.

---

## The Six REST Constraints

### 1. Stateless
Every request from the client to the server must contain all information needed to understand it. The server stores **no session state** between requests.

- Each request is self-contained
- Authentication tokens (e.g., JWT) travel with every request
- This makes the server easy to scale horizontally (any server can handle any request)

```
BAD  → Server remembers "user is logged in" between requests
GOOD → Every request includes an Authorization header
```

### 2. Uniform Interface
The interface between client and server is standardized. The four sub-constraints are:
- **Resource identification**: resources are identified by URIs (`/api/v1/users/42`)
- **Manipulation through representations**: clients manipulate resources by sending representations (JSON)
- **Self-descriptive messages**: each message includes enough info to describe how to process it (Content-Type header)
- **HATEOAS** (optional in practice): responses include links to related actions

### 3. Client-Server
The UI (client) and data storage (server) are separated. They can evolve independently. A React frontend and a Spring Boot backend know nothing about each other's internals — they only share the API contract.

### 4. Cacheable
Responses must define themselves as cacheable or non-cacheable. Caching reduces server load and improves performance. HTTP cache headers (`Cache-Control`, `ETag`) implement this.

### 5. Layered System
The client cannot tell whether it is connected directly to the end server or an intermediary (load balancer, CDN, proxy). Layers can be added transparently.

### 6. Code on Demand (optional)
Servers can extend client functionality by transferring executable code (e.g., JavaScript). This constraint is rarely applied to REST APIs.

---

## Resources and URLs

In REST, everything is a **resource** — a noun, not a verb.

### URL Design Best Practices

| Good | Bad | Why |
|------|-----|-----|
| `GET /api/v1/users` | `GET /getUsers` | Use nouns, not verbs |
| `GET /api/v1/users/42` | `GET /getUserById?id=42` | Use path variables for IDs |
| `POST /api/v1/users` | `POST /createUser` | HTTP method conveys the action |
| `GET /api/v1/users/42/orders` | `GET /getUserOrders?userId=42` | Nest related resources |
| `GET /api/v1/todos?completed=true` | `GET /getCompletedTodos` | Use query params for filtering |
| `/api/v1/users` | `/api/v1/Users` | Use lowercase |
| `/api/v1/blog-posts` | `/api/v1/blogPosts` | Use hyphens, not camelCase |

### Versioning
Always version your API. When breaking changes are needed, you create a new version without breaking existing clients.

```
/api/v1/users   ← current
/api/v2/users   ← next major version (breaking changes)
```

### Nesting
Use nesting to express ownership or relationship (up to 2 levels deep):

```
GET  /api/v1/users/42/orders         ← orders belonging to user 42
GET  /api/v1/users/42/orders/7       ← specific order of user 42
POST /api/v1/users/42/orders         ← create order for user 42
```

Avoid deep nesting (`/users/42/orders/7/items/3/reviews`) — it becomes hard to maintain. Flatten when nesting exceeds 2 levels.

---

## HTTP Methods

HTTP methods (also called verbs) define the **action** to perform on a resource.

| Method | Action | Idempotent? | Safe? | Body? |
|--------|--------|-------------|-------|-------|
| `GET` | Read / retrieve | Yes | Yes | No |
| `POST` | Create | No | No | Yes |
| `PUT` | Replace entirely | Yes | No | Yes |
| `PATCH` | Partial update | No | No | Yes |
| `DELETE` | Remove | Yes | No | No |

**Idempotent**: calling the same request multiple times produces the same result.  
**Safe**: the operation has no side effects (doesn't modify data).

### GET
Retrieve a resource or collection. Never use GET to modify data.

```
GET /api/v1/todos          → returns all todos
GET /api/v1/todos/1        → returns todo with id=1
GET /api/v1/todos?completed=true  → filtered list
```

### POST
Create a new resource. The server assigns the ID. Not idempotent — calling it twice creates two records.

```
POST /api/v1/todos
Body: {"title": "Buy groceries"}
Response: 201 Created + the newly created todo (with server-assigned id)
```

### PUT
Replace a resource entirely. If a field is missing from the body, it is cleared/reset.

```
PUT /api/v1/todos/1
Body: {"title": "Buy groceries", "completed": true, "description": ""}
→ Replaces the entire todo
```

### PATCH
Update a resource partially. Only send the fields you want to change.

```
PATCH /api/v1/todos/1/complete
→ Marks todo 1 as completed (no body needed for a simple state change)

PATCH /api/v1/todos/1
Body: {"description": "Only update this field"}
→ Updates only description, leaves everything else unchanged
```

### DELETE
Remove a resource. Returns 204 No Content on success.

```
DELETE /api/v1/todos/1
→ Deletes todo 1, returns 204
```

---

## HTTP Status Codes

Status codes tell the client what happened. They are grouped by range:

- **2xx** — Success
- **3xx** — Redirection
- **4xx** — Client error (the request was wrong)
- **5xx** — Server error (the server failed)

### Success Codes

| Code | Name | When to use |
|------|------|-------------|
| `200 OK` | Success | GET, PUT, PATCH responses |
| `201 Created` | Created | After a successful POST that creates a resource |
| `204 No Content` | No body | DELETE, or any successful operation with no response body |

### Client Error Codes

| Code | Name | When to use |
|------|------|-------------|
| `400 Bad Request` | Validation failed | Malformed JSON, missing required fields, invalid values |
| `401 Unauthorized` | Not authenticated | No token, expired token |
| `403 Forbidden` | Not authorized | Valid token but insufficient permissions |
| `404 Not Found` | Resource missing | `/api/v1/todos/999` where 999 doesn't exist |
| `409 Conflict` | State conflict | Creating a duplicate, version conflict |
| `422 Unprocessable Entity` | Semantic error | Syntactically valid but semantically wrong |

### Server Error Codes

| Code | Name | When to use |
|------|------|-------------|
| `500 Internal Server Error` | Unexpected failure | Unhandled exceptions, database down |
| `503 Service Unavailable` | Service down | Maintenance, overloaded |

### 401 vs 403 — the common confusion

```
401 Unauthorized  → "I don't know who you are" (not authenticated)
403 Forbidden     → "I know who you are, but you can't do this" (not authorized)
```

---

## Request and Response Structure

### HTTP Request
```
POST /api/v1/todos HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Accept: application/json

{
  "title": "Learn Spring Boot",
  "description": "Build a REST API"
}
```

**Parts:**
- **Request line**: method + URL + HTTP version
- **Headers**: metadata (Content-Type, Authorization, Accept)
- **Body**: data payload (JSON for POST/PUT/PATCH)

### HTTP Response
```
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/v1/todos/7

{
  "id": 7,
  "title": "Learn Spring Boot",
  "description": "Build a REST API",
  "completed": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Parts:**
- **Status line**: HTTP version + status code + reason phrase
- **Headers**: metadata about the response
- **Body**: the resource representation (usually JSON)

---

## JSON in REST APIs

JSON (JavaScript Object Notation) is the de facto standard for REST API payloads.

### JSON Data Types
```json
{
  "id": 42,                         // number
  "title": "Learn REST",            // string
  "completed": false,               // boolean
  "tags": ["java", "spring"],       // array
  "author": {                       // object
    "name": "Alice",
    "email": "alice@example.com"
  },
  "deletedAt": null                 // null
}
```

### Naming Conventions
Use **camelCase** for JSON field names in Java APIs (matches Java conventions):

```json
{
  "createdAt": "2024-01-15T10:30:00",   // camelCase
  "firstName": "Alice",
  "isCompleted": true
}
```

### Date Formats
Use **ISO 8601** format for dates and times:
```json
{
  "createdAt": "2024-01-15T10:30:00Z",      // UTC
  "dueDate": "2024-01-20",                   // date only
  "startTime": "2024-01-15T10:30:00+02:00"  // with timezone offset
}
```

---

## Common API Response Patterns

### Single Resource
```json
{
  "id": 1,
  "title": "Learn REST",
  "completed": false
}
```

### Collection
```json
[
  {"id": 1, "title": "Learn REST"},
  {"id": 2, "title": "Build API"}
]
```

### Paginated Collection
```json
{
  "data": [
    {"id": 1, "title": "Learn REST"},
    {"id": 2, "title": "Build API"}
  ],
  "page": 1,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

### Error Response
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Todo with id 999 not found",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/v1/todos/999"
}
```

### Validation Error Response
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "title": "must not be blank",
    "email": "must be a valid email address"
  }
}
```

---

## Summary

REST is a style, not a protocol. The key ideas to remember:
1. **Resources are nouns**: `/users`, `/todos`, `/orders`
2. **HTTP methods express actions**: GET reads, POST creates, PUT replaces, PATCH updates, DELETE removes
3. **Status codes communicate outcome**: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 404 Not Found
4. **Stateless**: every request is self-contained
5. **Version your API**: `/api/v1/...`
6. **Use consistent naming**: lowercase, hyphens for URLs; camelCase for JSON fields
