# System Design Fundamentals

Understanding these building blocks lets you compose them into complete system designs. Each concept appears repeatedly across different design questions.

---

## 1. Load Balancing

### What It Is and Why It's Needed

A load balancer distributes incoming network traffic across multiple servers. Without it, a single server becomes a bottleneck and a single point of failure. With it, you can:

- **Scale horizontally**: Add more servers to handle more traffic
- **Increase availability**: If one server dies, others continue serving requests
- **Improve latency**: Route requests to the least-loaded or geographically closest server

```
Client → Load Balancer → [Server 1]
                       → [Server 2]
                       → [Server 3]
```

### Load Balancing Algorithms

**Round Robin**
Requests are distributed sequentially: Server 1 → Server 2 → Server 3 → Server 1 → ...

- Simple and even distribution
- Problem: Doesn't account for varying request complexity (a slow request on Server 1 means it gets the same number of requests as a fast Server 2)
- Best for: Homogeneous servers with similar request types

**Weighted Round Robin**
Like round robin, but servers with higher capacity get proportionally more requests.

- Server with weight 3 gets 3x the requests of a server with weight 1
- Best for: Mixed hardware in your server fleet

**Least Connections**
New requests are routed to the server with the fewest active connections.

- More adaptive than round robin for long-running connections
- Best for: WebSocket connections, streaming, or workloads with high variance in processing time

**IP Hash (Sticky Sessions)**
The client's IP address is hashed to consistently route them to the same server.

- Same client always hits the same server
- Problem: Breaks if server count changes (rehashing required)
- Use case: Stateful applications where session data is stored on-server (but note: better to make services stateless)

**Least Response Time**
Routes to the server with the lowest latency + fewest connections.

- Most accurate but requires active latency measurement
- Common in modern load balancers like HAProxy, Nginx

### L4 vs L7 Load Balancing

**L4 (Transport Layer)**
- Operates at TCP/UDP level
- Looks at IP address and port number only
- Does NOT inspect packet content
- Very fast (low overhead)
- Cannot make routing decisions based on HTTP headers, URL paths, or cookies
- Example: AWS Network Load Balancer (NLB)

**L7 (Application Layer)**
- Operates at HTTP/HTTPS level
- Can inspect headers, URL paths, cookies, and body content
- Can route `/api/*` to API servers and `/static/*` to file servers
- Can do SSL termination
- More flexible but slightly higher overhead
- Example: AWS Application Load Balancer (ALB), Nginx, HAProxy

**When to use which:**
- Use L7 for web applications that need content-based routing
- Use L4 for raw performance (gaming, real-time systems) or non-HTTP protocols

### Health Checks

Load balancers continuously probe servers to detect failures:

- **Active health check**: LB sends periodic HTTP requests (e.g., `GET /health`) and removes unresponsive servers
- **Passive health check**: LB monitors actual traffic responses and marks servers unhealthy after N consecutive failures
- Servers that fail health checks are removed from rotation until they recover

```
LB → Server 1 /health → 200 OK ✓
LB → Server 2 /health → timeout → removed from pool
LB → Server 3 /health → 200 OK ✓
```

---

## 2. Caching

### Why Caching Matters

Accessing data has a cost:
- RAM access: ~100 nanoseconds
- SSD read: ~100 microseconds (1,000x slower)
- Network call (same datacenter): ~1 millisecond (10,000x slower)
- Database query (with disk I/O): ~10–100 milliseconds

Caching stores frequently accessed data in fast memory so expensive operations don't repeat. A cache hit serving 90% of requests can reduce database load by 10x.

### Cache Patterns

**Cache-Aside (Lazy Loading)**
Application code manages the cache explicitly.

```
READ:
1. Check cache for key
2. If HIT → return cached value
3. If MISS → query database, store in cache, return value

WRITE:
1. Update database
2. Invalidate (delete) cached entry
```

- Most common pattern
- Cache only contains actually-requested data (no wasted memory)
- First request after cache eviction always hits the database (cache miss penalty)
- Risk: stale data if DB updated without invalidating cache

**Read-Through**
Cache sits in front of database; application always talks to cache.

```
READ:
1. Request goes to cache
2. If MISS → cache fetches from DB, stores, returns
3. Application always reads from cache only
```

- Simpler application code (no cache miss logic)
- Cache manages its own population
- First read is still slow (cache miss); can warm cache on startup

**Write-Through**
Every write goes to both cache and database synchronously.

```
WRITE:
1. Write to cache
2. Immediately write to database (synchronous)
3. Both always consistent
```

- Cache is always up-to-date
- Write latency is higher (two writes instead of one)
- Good for data that is frequently read after being written

**Write-Back (Write-Behind)**
Write to cache immediately; write to database asynchronously later.

```
WRITE:
1. Write to cache
2. Return success immediately
3. Background job flushes cache to database
```

- Very fast writes (only one operation in the critical path)
- Risk: data loss if cache fails before DB flush
- Good for: high-write workloads where some data loss is acceptable (e.g., analytics counters, non-critical metrics)

### Redis vs Memcached

| Feature | Redis | Memcached |
|---------|-------|-----------|
| Data structures | Rich (strings, lists, sets, sorted sets, hashes, streams) | Only strings |
| Persistence | Yes (RDB snapshots + AOF logs) | No (in-memory only) |
| Replication | Master-replica with Redis Sentinel/Cluster | No built-in replication |
| Pub/Sub | Yes | No |
| Lua scripting | Yes | No |
| Multi-threading | Single-threaded (by design) | Multi-threaded |
| Cluster mode | Redis Cluster (built-in sharding) | Manual client-side sharding |

**Choose Redis when**: You need persistence, complex data structures, pub/sub messaging, or sorted sets (leaderboards)

**Choose Memcached when**: You need maximum throughput for simple key-value caching and can sacrifice persistence

In practice: Redis has largely replaced Memcached in new architectures.

### Cache Invalidation Strategies

Cache invalidation is famously difficult. The two fundamental strategies:

**TTL (Time-To-Live)**
Every cached entry has an expiry time. After TTL, the cache evicts the entry and the next request fetches fresh data.

- Simple to implement
- Risk: serving stale data until TTL expires
- Good for: data that changes infrequently or where some staleness is acceptable

**Event-Based Invalidation**
When data changes, the application explicitly deletes or updates the cache entry.

```java
// On database update
db.updateUser(userId, newData);
cache.delete("user:" + userId);  // Explicit invalidation
```

- Cache is always fresh
- Requires careful coordination between write paths
- Risk: cache stampede — if many requests hit at once after invalidation, all miss cache and hammer the DB simultaneously

**Cache Stampede (Thundering Herd) Solution**:
- Use a lock: first thread to miss acquires a lock, fetches from DB, populates cache; other threads wait
- Or: probabilistic early expiration — randomly refresh cache slightly before TTL expires

### Cache Eviction Policies

When cache is full, which entry to evict?

**LRU (Least Recently Used)**
- Evicts the entry that was accessed least recently
- Works well when recent accesses predict future accesses
- Default Redis eviction policy (`allkeys-lru`)

**LFU (Least Frequently Used)**
- Evicts the entry that has been accessed fewest times
- Better for workloads where some keys are always popular
- Redis supports `allkeys-lfu`

**FIFO (First In, First Out)**
- Evicts oldest entries regardless of access frequency
- Simple but ignores access patterns
- Rarely optimal

**Random**
- Evicts a random entry
- Surprisingly good in practice, very low overhead

**MRU (Most Recently Used)**
- Evicts the most recently used entry
- Useful for sequential scans where future requests won't reuse recent data

### CDN as a Cache

A Content Delivery Network is a distributed cache of static and semi-static content placed geographically close to users.

- User in Tokyo requesting a JS file from a US-based server → 150ms latency
- User in Tokyo requesting from a Tokyo CDN edge node → 5ms latency

CDNs cache: HTML, CSS, JavaScript, images, videos, API responses (with appropriate Cache-Control headers)

**How it works:**
1. First request to a CDN edge node → cache miss → CDN fetches from origin server → caches it
2. Subsequent requests from the same region → served from CDN cache → fast

CDN providers: Cloudflare, AWS CloudFront, Akamai, Fastly

---

## 3. Databases

### SQL vs NoSQL Trade-offs

**SQL (Relational Databases)**
- Data stored in tables with defined schemas
- Relationships expressed via foreign keys and JOINs
- ACID guarantees
- Strong consistency
- Examples: PostgreSQL, MySQL, Oracle, SQL Server

**NoSQL**
- Flexible schemas (or schema-less)
- Horizontal scaling built-in (most)
- BASE properties instead of ACID
- Various data models: document, key-value, columnar, graph
- Examples: MongoDB, Cassandra, Redis, DynamoDB, Neo4j

### ACID Properties (SQL)

**Atomicity**: A transaction either fully completes or fully rolls back. No partial transactions. ("All or nothing")

**Consistency**: A transaction takes the database from one valid state to another. Constraints (foreign keys, unique, not-null) are always satisfied.

**Isolation**: Concurrent transactions behave as if they ran sequentially. One transaction's partial state is invisible to others.

**Durability**: Once a transaction is committed, it persists even if the system crashes immediately after.

ACID matters when:
- Money is moving between accounts
- Inventory is being decremented
- Any operation that must not partially complete

### BASE Properties (NoSQL)

**Basically Available**: The system guarantees availability, even at the cost of consistency.

**Soft State**: The state of the system may change over time without input (due to eventual consistency propagation).

**Eventually Consistent**: Given enough time with no new writes, all nodes will converge to the same value.

BASE trades consistency for availability and partition tolerance, as described by the CAP theorem.

### When to Use Which Database

| Use Case | Database Type | Example |
|----------|--------------|---------|
| User accounts, financial transactions | Relational | PostgreSQL |
| Product catalog with varied attributes | Document | MongoDB |
| User sessions, rate limiting, caching | Key-Value | Redis, DynamoDB |
| User activity feeds, time-series metrics | Columnar | Cassandra, HBase |
| Social graphs, recommendation engines | Graph | Neo4j |
| Full-text search | Search engine | Elasticsearch |

**Rule of thumb**: Start with PostgreSQL. Add specialized databases only when you have a clear need that relational databases cannot efficiently serve.

### Indexing

An index is a data structure that enables fast data retrieval without scanning every row.

```sql
-- Without index: full table scan O(n)
SELECT * FROM users WHERE email = 'alice@example.com';

-- With index on email: O(log n) B-tree lookup
CREATE INDEX idx_users_email ON users(email);
```

**Index types:**
- **B-tree index**: Default. Works for equality (`=`) and range queries (`<`, `>`, `BETWEEN`). Ordered.
- **Hash index**: Only equality. Faster for exact matches but no range queries.
- **Composite index**: Index on multiple columns. Order matters: index on `(last_name, first_name)` helps queries filtering on `last_name` but not queries on `first_name` alone.
- **Partial index**: Index only a subset of rows. Useful for indexing only active records.

**Index trade-offs:**
- Reads get faster
- Writes get slower (index must be updated on every INSERT/UPDATE/DELETE)
- Indexes consume disk space
- Don't over-index: a table with 10 indexes has 10x the write overhead

**Covering index**: An index that contains all columns needed to satisfy a query, so the database doesn't need to look up the actual row.

### N+1 Problem

A common performance bug in ORM usage.

```java
// N+1 problem: 1 query for authors + N queries for each author's books
List<Author> authors = db.query("SELECT * FROM authors");  // 1 query
for (Author author : authors) {
    List<Book> books = db.query("SELECT * FROM books WHERE author_id = ?", author.getId());  // N queries
}
// Total: 1 + N queries!
```

**Solution: JOIN or eager loading**

```java
// Single query with JOIN
List<AuthorWithBooks> results = db.query("""
    SELECT a.*, b.*
    FROM authors a
    LEFT JOIN books b ON b.author_id = a.id
""");
// Total: 1 query
```

ORMs (Hibernate, JPA) often hide this. Use SQL query logging in development to detect N+1 issues.

---

## 4. Message Queues

### What Problems They Solve

**Decoupling**: Producer and consumer don't need to know about each other or be available simultaneously.

**Asynchronous processing**: Work that doesn't need to happen synchronously can be deferred. Example: after placing an order, send a confirmation email asynchronously — don't make the user wait.

**Load leveling**: Absorb traffic spikes. If 10,000 orders arrive simultaneously, the queue buffers them and workers process at a sustainable rate.

**Reliability**: If a downstream service is down, messages wait in the queue until it recovers.

```
Without queue:
Order Service → Email Service (if email service is down, order fails!)

With queue:
Order Service → Queue → Email Worker → Email Service
(if email service is down, messages queue up; order still succeeds)
```

### Kafka vs RabbitMQ

**Apache Kafka**
- Distributed log (append-only log)
- Messages are retained for a configurable period (days/weeks), not deleted after consumption
- Multiple consumer groups can independently consume the same messages
- High throughput (millions of messages/second)
- Consumer tracks its own offset (position in the log)
- Strong ordering within a partition
- Best for: event streaming, audit logs, replaying events, data pipelines

**RabbitMQ**
- Traditional message broker
- Messages are deleted after consumption (or moved to dead letter queue)
- Rich routing rules (direct, topic, fanout exchanges)
- Push-based delivery (broker pushes to consumers)
- Built-in message acknowledgment and retry
- Lower throughput than Kafka but simpler to set up
- Best for: task queues, RPC patterns, point-to-point messaging

| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| Message retention | Time-based (retained after consumption) | Deleted after ACK |
| Throughput | Very high (millions/sec) | High (tens of thousands/sec) |
| Consumer model | Pull-based | Push-based |
| Ordering | Per partition | Per queue |
| Replay | Yes (seek to offset) | No |
| Complexity | Higher | Lower |
| Use case | Streaming, event log | Task queues, RPC |

### Delivery Semantics

**At-most-once**: Messages may be lost but never duplicated.
- Producer fires and forgets, no retries
- Acceptable for: metrics, logging where some loss is tolerable

**At-least-once**: Messages are never lost but may be delivered multiple times.
- Producer retries on failure; consumer may process same message twice
- Consumers must be **idempotent** (processing the same message twice has the same effect as once)
- Most common default

**Exactly-once**: Messages are delivered exactly once.
- Most complex and expensive to implement
- Kafka supports exactly-once semantics (EOS) for Kafka-to-Kafka processing
- For end-to-end exactly-once, you need idempotent consumers + transactional producers

**Making consumers idempotent:**
- Track processed message IDs in a database
- Use deterministic IDs for operations (e.g., order ID as idempotency key)

### Dead Letter Queues (DLQ)

A DLQ receives messages that cannot be processed after N retries.

```
Message → Processing fails → Retry 1 → Retry 2 → Retry 3 → Dead Letter Queue
```

Benefits:
- Failed messages are not lost; they're available for investigation
- Main queue stays unblocked (bad messages don't block good ones)
- Ops team can inspect DLQ messages and decide: fix bug and replay, or discard

---

## 5. CDN (Content Delivery Network)

### How CDNs Work

A CDN is a geographically distributed network of proxy servers (edge nodes) that cache content close to end users.

```
User (Tokyo) → DNS resolves to nearest CDN edge (Tokyo) → Cache HIT → Fast response
                                                         → Cache MISS → Fetch from origin (US) → Cache → Return
```

**What CDNs cache:**
- Static assets: HTML, CSS, JavaScript, images, fonts, videos
- API responses (with proper Cache-Control headers)
- Dynamically generated pages (edge-side includes)

**Benefits:**
- Reduced latency (serve from nearby edge)
- Reduced load on origin servers
- Better availability (CDN absorbs DDoS attacks)
- Bandwidth savings

### Push vs Pull CDN

**Pull CDN (Lazy)**
- CDN fetches content from origin on first request (cache miss)
- Content cached at edge until TTL expires
- Simple to set up; no upload step needed
- Initial request is slow (origin fetch); subsequent requests are fast
- Good for: frequently accessed content with unpredictable access patterns

**Push CDN (Eager)**
- You upload content to CDN when it's created/updated
- Content exists at edge before any user requests it
- All requests are cache hits
- Good for: content that is accessed frequently and you know ahead of time (e.g., software release files)
- Requires managing CDN uploads in your deployment pipeline

### When to Use a CDN

Use CDN for:
- Public-facing websites with geographically distributed users
- Large media files (images, videos)
- High-traffic applications where origin servers would be overwhelmed

CDN is less useful for:
- Internal applications (no geographic distribution needed)
- Highly dynamic content that changes per-user (though edge computing is changing this)
- Very small applications with all users in one region

---

## 6. Microservices

### Monolith vs Microservices Trade-offs

**Monolith**
All functionality in a single deployable unit.

Pros:
- Simple to develop (single codebase)
- Simple to deploy (one artifact)
- No network latency between components
- Easy transactions (single database)
- Easy to debug (single process)

Cons:
- Scaling: must scale entire application even if only one component needs more resources
- Deployment: any change requires redeploying everything
- Technology: locked to one language/framework
- Team: all engineers work in one codebase (coordination overhead grows)
- Reliability: one bug can take down the entire application

**Microservices**
Each service is an independent deployable unit with its own database.

Pros:
- Independent scaling (scale only the services that need it)
- Independent deployment (teams deploy without coordination)
- Technology diversity (each service can use best tool for its job)
- Fault isolation (failure in one service doesn't cascade)

Cons:
- Distributed systems complexity (network failures, partial failures, latency)
- No distributed transactions (must use patterns like Saga)
- More infrastructure overhead (N databases, N deployments)
- Harder to debug (traces span multiple services)
- Operational complexity (service discovery, monitoring, tracing)

**Recommendation**: Start with a monolith. Extract services when you have clear scaling needs or team coordination problems, not upfront.

### API Gateway Pattern

An API Gateway is a single entry point for all client requests to a microservices system.

```
Client → API Gateway → User Service
                     → Order Service
                     → Payment Service
                     → Product Service
```

Responsibilities of API Gateway:
- **Request routing**: Route `/users/*` to User Service, `/orders/*` to Order Service
- **Authentication**: Verify JWTs before forwarding to services
- **Rate limiting**: Prevent abuse
- **SSL termination**: Handle HTTPS, forward HTTP internally
- **Request transformation**: Aggregate multiple service calls into one response
- **Logging and tracing**: Centralized request logging

Examples: AWS API Gateway, Kong, Nginx, Traefik

### Service Discovery

Microservices need to find each other. Service discovery provides a registry of service locations.

**Client-side discovery**: Service queries registry, gets IP/port, calls directly.
```
Order Service → Service Registry (get Payment Service address) → Payment Service
```

**Server-side discovery**: Load balancer or API gateway queries registry and routes requests.
```
Order Service → Load Balancer → (LB queries registry) → Payment Service
```

Tools: Netflix Eureka, Consul, Kubernetes DNS (services register automatically)

### Circuit Breaker Pattern

Prevents cascading failures when a service is slow or unavailable.

```
States:
CLOSED (normal) → failures exceed threshold → OPEN (reject all requests)
                                                 ↓ after timeout
                                             HALF-OPEN (test one request)
                                             → success → CLOSED
                                             → failure → OPEN
```

**Without circuit breaker**: All threads block waiting for a slow service → system slows down → more requests queue → cascade failure

**With circuit breaker**: After N failures, circuit opens → requests fail fast → system stays healthy → circuit tests periodically for recovery

Libraries: Netflix Hystrix (deprecated), Resilience4j (Java), Polly (.NET)

---

## Key Takeaways

| Component | Primary Purpose | Key Trade-off |
|-----------|----------------|---------------|
| Load Balancer | Distribute traffic, eliminate SPOF | L4 (fast) vs L7 (flexible) |
| Cache | Reduce latency, reduce DB load | Consistency vs performance |
| SQL Database | ACID transactions, complex queries | Scaling requires sharding |
| NoSQL Database | Horizontal scale, flexible schema | Eventual consistency |
| Message Queue | Decoupling, async processing | At-least-once requires idempotency |
| CDN | Reduce latency for static content | Push (eager) vs Pull (lazy) |
| Microservices | Independent scaling and deployment | Distributed systems complexity |
