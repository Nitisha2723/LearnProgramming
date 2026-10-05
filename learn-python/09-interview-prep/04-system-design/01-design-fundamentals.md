# System Design Fundamentals

---

## 1. Core Concepts

### CAP Theorem
A distributed system can guarantee at most two of three properties:
- **C**onsistency — every read receives the most recent write
- **A**vailability — every request receives a response (may not be latest)
- **P**artition tolerance — system continues despite network partitions

**In practice:** partition tolerance is mandatory in real networks.
So the choice is between CP (choose consistency) and AP (choose availability).

### ACID vs BASE
| ACID (SQL) | BASE (NoSQL) |
|-----------|-------------|
| Atomicity | Basically Available |
| Consistency | Soft state |
| Isolation | Eventually consistent |
| Durability | |

---

## 2. Load Balancing

Distributes requests across multiple servers.

**Algorithms:**
- Round Robin — simple rotation
- Least Connections — to server with fewest active connections
- IP Hash — consistent mapping of client to server (session affinity)
- Weighted — proportional to server capacity

**Layers:**
- L4 (transport): routes by IP/port
- L7 (application): routes by HTTP content (URL, cookies, headers)

---

## 3. Caching

**Where to cache:**
- Browser cache (static assets)
- CDN (geographically distributed static content)
- Application-level (Redis, Memcached)
- Database query cache

**Eviction Policies:**
- LRU (Least Recently Used) — most common
- LFU (Least Frequently Used)
- TTL (Time-To-Live expiry)

**Cache invalidation strategies:**
- **Write-through**: write to cache and DB simultaneously (consistent, slower writes)
- **Write-behind (write-back)**: write to cache, async write to DB (fast writes, risk of loss)
- **Cache-aside (lazy loading)**: read from DB on miss, populate cache (most common)

---

## 4. Databases

### SQL vs NoSQL

| | SQL | NoSQL |
|--|-----|-------|
| Schema | Fixed | Flexible |
| Scaling | Vertical (hard to shard) | Horizontal (built for it) |
| Transactions | ACID | Varies |
| Query | Rich (JOINs) | Limited |
| Use case | Complex relations | High write throughput, flexible schema |

### Indexing
- B-tree index: O(log n) for range queries — default
- Hash index: O(1) exact lookups — poor for ranges
- Full-text index: for text search
- Composite index: multiple columns; order matters

### Sharding
Splitting data across multiple DB nodes.

- **Horizontal sharding**: split by row (user_id % N)
- **Consistent hashing**: minimises rebalancing when nodes added/removed
- **Problems**: cross-shard queries, re-sharding

### Replication
- **Primary-replica**: writes to primary, reads from replicas
- **Multi-primary**: multiple write nodes (conflict resolution needed)

---

## 5. Message Queues

Decouple producers from consumers; enable async processing.

```
Producer -> [Queue] -> Consumer
```

**Benefits:**
- Buffer bursts of requests
- Retry failed jobs
- Fan-out (one message → many consumers)

**Tools:** Kafka, RabbitMQ, AWS SQS, Redis Streams

**Patterns:**
- Task queue: distribute work across workers
- Pub/Sub: topic-based broadcast to subscribers
- Event streaming: ordered log, replay from any offset (Kafka)

---

## 6. API Design

### REST
- Stateless, resource-oriented
- HTTP verbs: GET, POST, PUT, PATCH, DELETE
- Status codes: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 404 Not Found, 500 Server Error

### GraphQL
- Single endpoint, client specifies exact fields
- Solves over/under-fetching
- More complex to cache

### gRPC
- Binary protocol (Protobuf), very fast
- Strongly typed schema
- Excellent for internal microservice communication

---

## 7. Estimations (Back-of-Envelope)

Always do these in interviews to show systems thinking:

```
Storage:
  1 million users × 1KB profile = 1 GB
  1 million users × 5 photos × 3MB avg = 15 TB

Traffic:
  Twitter: 500M tweets/day = ~6,000 tweets/second
  Read-heavy: 10:1 or 100:1 read:write ratio typical

Bandwidth:
  1 Gbps link can handle ~125 MB/s
  4K video: ~25 Mbps
```

Key numbers:
```
1 byte = 8 bits
1 KB = 10^3 bytes
1 MB = 10^6 bytes
1 GB = 10^9 bytes
1 TB = 10^12 bytes

MySQL/PostgreSQL: ~1,000 writes/sec per instance
Redis: ~100,000 ops/sec
CDN: reduces latency to ~10ms for static content
```
