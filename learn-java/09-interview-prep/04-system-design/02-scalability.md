# Scalability

Scalability is a system's ability to handle increasing load without degrading performance. Understanding scalability patterns is core to system design interviews.

---

## 1. Horizontal vs Vertical Scaling

### Vertical Scaling (Scale Up)

Add more resources to the existing machine: more CPU, more RAM, faster disk.

```
Before: [Server 16GB RAM, 4 CPU]
After:  [Server 128GB RAM, 32 CPU]
```

**Pros:**
- Simple: no application changes needed
- No distributed systems complexity
- Strong consistency (single machine)

**Cons:**
- Hardware limits: there's a maximum machine size
- Single point of failure: one machine means one failure domain
- Expensive: high-end hardware has diminishing returns and premium pricing
- Requires downtime to upgrade hardware

**When vertical scaling is appropriate:**
- In the short term while you architect a horizontal solution
- For databases that are hard to distribute (sometimes cheaper to buy a bigger machine)
- For systems with strict sequential consistency requirements

### Horizontal Scaling (Scale Out)

Add more machines to the pool; distribute work across them.

```
Before: [Server 1]
After:  [Server 1] [Server 2] [Server 3] [Server 4]
```

**Pros:**
- Theoretically unlimited scale
- High availability: individual machine failures don't take down the system
- Commodity hardware: cheaper per unit than high-end servers
- No downtime to add capacity (add machines while running)

**Cons:**
- Requires distributed systems design
- Applications must be stateless (or state must be externalized)
- More complex operations (monitoring, deployments)
- Network communication between nodes introduces latency

**When to scale horizontally:**
- When you hit the vertical scaling ceiling
- When availability requirements demand no single point of failure
- For stateless services (API servers, web servers) — trivially horizontal

---

## 2. Stateless Services

### Why Statelessness Enables Scaling

A **stateful service** stores session data in memory on the server. When user Alice is on Server 1, all her requests must go to Server 1 because her session data lives there.

```
Stateful problem:
User Alice → Load Balancer → Server 1 (has Alice's session)
           → [Can't route to Server 2 — no session data there!]
```

A **stateless service** stores no session data locally. Any server can handle any request.

```
Stateless (good):
User Alice → Load Balancer → Server 1 (looks up session in Redis)
           OR → Server 2 (same Redis — same result)
           OR → Server 3 (same Redis — same result)
```

Stateless services scale horizontally by definition: add servers, distribute traffic.

### Session Management Approaches

**Approach 1: Sticky Sessions (Avoid if possible)**
Load balancer routes all requests from a user to the same server (IP hash or session cookie).

- Enables stateful servers
- Problem: if that server dies, session is lost
- Problem: limits load balancing effectiveness (can't move load freely)
- Problem: cache warmup when the session "sticks" to a cold server

**Approach 2: Centralized Session Store (Redis)**
Sessions stored in a shared Redis cluster. Any server can look up any session.

```java
// Spring Session with Redis
@EnableRedisHttpSession
// All sessions automatically stored in Redis
// Any server in the pool can handle any request
```

- Simple to implement
- Redis is fast enough that the overhead is acceptable
- Sessions survive server restarts
- Redis becomes a dependency (but Redis is highly available)

**Approach 3: JWT (JSON Web Token)**
Session state encoded in the token and sent by the client on every request. No server-side session storage.

```
1. User logs in
2. Server creates JWT: {userId: 123, roles: ["admin"], exp: 1234567890}
3. Server signs JWT with secret key → sends to client
4. Client sends JWT in Authorization header on every request
5. Any server verifies signature, extracts claims → no DB/Redis lookup needed
```

Pros:
- Truly stateless: no session storage required
- Works well for APIs and microservices

Cons:
- Can't revoke a JWT before it expires (need a token blacklist, which re-introduces state)
- JWT grows large if you put too much in the payload
- Must refresh before expiry (access token + refresh token pattern)

---

## 3. Database Scaling

### Read Replicas

Most web applications are read-heavy (reads >> writes). Split the workload:

```
Writes → Primary (master) database
Reads  → Replica 1
         Replica 2
         Replica 3
```

- Primary handles all writes and replicates changes to replicas asynchronously
- Reads are distributed across replicas → 3x read capacity
- Replicas lag slightly behind primary (typically milliseconds)

**Important:** Don't read your own writes from replicas immediately after writing. Either:
- Read from primary for a short window after a write ("read-after-write consistency")
- Use synchronous replication for critical data (slower writes)

### Sharding (Horizontal Partitioning)

When a single database can't handle write volume or data volume, split data across multiple database servers.

```
Without sharding:
All data → [DB Server]

With sharding:
Users 0-999999   → [Shard 1]
Users 1000000-1999999 → [Shard 2]
Users 2000000+   → [Shard 3]
```

Each shard is an independent database. Queries for User 5000 go only to Shard 1.

**Trade-offs of sharding:**
- Cross-shard JOINs are very difficult (data is on different servers)
- Cross-shard transactions require distributed transaction protocols
- Application must know which shard to query (routing layer)
- Resharding (adding shards later) is painful

### Sharding Strategies

**Range-Based Sharding**
Partition by range of a key value.

```
user_id 1–1M      → Shard 1
user_id 1M–2M     → Shard 2
user_id 2M–3M     → Shard 3
```

- Simple to implement
- Enables range queries on the shard key
- Risk of hotspots: if most active users are in one range, one shard is overloaded

**Hash-Based Sharding**
Apply a hash function to the key; route to shard based on hash value.

```
shard = hash(user_id) % number_of_shards
```

- Even distribution (assuming good hash function)
- No range queries possible on shard key
- Changing number of shards requires rehashing and moving data

**Geographic Sharding**
Route users to a shard in their region.

```
Users in US → US Shard
Users in EU → EU Shard
Users in Asia → Asia Shard
```

- Low latency (data close to users)
- Satisfies data residency requirements (GDPR: EU data stays in EU)
- Imbalance possible if one region grows faster

**Directory-Based Sharding**
A lookup service maintains a mapping from key to shard.

```
lookup_service.getShard(user_id) → returns "shard_3"
```

- Most flexible (can reassign individual keys)
- Lookup service becomes a bottleneck and single point of failure

### Consistent Hashing

Standard hash-based sharding breaks when you add/remove nodes: `hash(key) % N` changes for all keys when N changes.

Consistent hashing minimizes data movement when the cluster size changes.

**How it works:**
1. Arrange hash values in a ring (0 to 2^32)
2. Hash each server's name onto the ring
3. Hash each key onto the ring
4. Key is stored on the next server clockwise

```
        0
      /   \
  Server A  Server B
      \   /
        Server C
```

When a server is added or removed, only its immediate neighbors are affected — the rest of the ring is unchanged.

**Virtual nodes**: Each physical server gets multiple positions on the ring, providing more even distribution. If Server A has 3 virtual nodes: A1, A2, A3 are placed at different ring positions.

Used in: Amazon DynamoDB, Apache Cassandra, Memcached client libraries

---

## 4. CAP Theorem

### The Theorem

In a distributed system, you can guarantee at most two of three properties:

- **Consistency (C)**: Every read returns the most recent write (or an error)
- **Availability (A)**: Every request receives a response (not guaranteed to be the most recent data)
- **Partition Tolerance (P)**: The system continues to operate despite network partitions (nodes unable to communicate)

**Key insight**: Network partitions are unavoidable in distributed systems. You cannot build a distributed system that is immune to network failures. Therefore, the real choice is between **consistency** and **availability** when a partition occurs.

### Simple Analogy

Imagine a bank with two branches (Branch A and Branch B) that sync their ledgers:

- Normally: a deposit at Branch A is visible at Branch B within seconds ✓
- Network partition: the two branches can't communicate

**CP choice (Consistent over Available)**: Branch B refuses to serve customers until network is restored. Data is always correct, but service is unavailable.

**AP choice (Available over Consistent)**: Branch B continues serving customers with potentially stale data. Service stays up, but data may be temporarily inconsistent.

### CP vs AP Systems

**CP Systems** (prefer consistency over availability during partitions)
- RDBMS (PostgreSQL, MySQL) — refuse to serve if can't confirm consistency
- HBase, Zookeeper, etcd
- Use when: financial transactions, inventory management, any system where stale data causes harm

**AP Systems** (prefer availability over consistency during partitions)
- Cassandra, DynamoDB, CouchDB
- DNS — gives potentially stale IP addresses but always responds
- Use when: social feeds, product catalogs, CDN — where stale data for a few seconds is acceptable

### What "Eventual Consistency" Means in Practice

"Eventual consistency" means: if no new writes occur, all nodes will converge to the same value *eventually*.

- "Eventually" is usually milliseconds to seconds, not hours or days
- The gap is caused by asynchronous replication between nodes

**Practical implications:**
- User updates their profile → immediate request might show old data (different replica)
- Solution: read-your-writes consistency: route user's reads to the primary for a short window
- Solution: use version vectors or timestamps to detect conflicts
- Solution: design the application to tolerate brief inconsistency (most social apps do)

---

## 5. Rate Limiting

### Why It Matters

Rate limiting controls how many requests a client can make in a given time window.

**Purposes:**
- **Prevent abuse**: Stop one user from hammering the API (intentional or bugs)
- **Ensure fair use**: Ensure one client doesn't starve others
- **Cost control**: Prevent runaway processes from generating huge API bills
- **Security**: Slow down brute-force attacks on login endpoints

### Token Bucket Algorithm

Each user has a "bucket" that holds tokens:
- Tokens are added at a constant rate (e.g., 10 tokens/second)
- Bucket has a maximum capacity (e.g., 100 tokens)
- Each request consumes one token
- If bucket is empty, request is rejected

```java
class TokenBucket {
    private double tokens;
    private final double maxTokens;
    private final double refillRate;  // tokens per second
    private long lastRefill;

    boolean allowRequest() {
        refill();
        if (tokens >= 1) {
            tokens--;
            return true;
        }
        return false;  // Rate limited
    }

    void refill() {
        long now = System.currentTimeMillis();
        double elapsed = (now - lastRefill) / 1000.0;
        tokens = Math.min(maxTokens, tokens + elapsed * refillRate);
        lastRefill = now;
    }
}
```

**Characteristics:**
- Allows bursting up to bucket capacity
- Steady-state rate is limited by refill rate
- Good for: APIs that want to allow occasional bursts

### Leaky Bucket Algorithm

Requests enter the bucket; they are processed at a constant rate regardless of input rate. If the bucket is full, requests are dropped.

```
Input: [burst of 100 requests] → Bucket (capacity 50) → Output: 10 req/sec (constant)
```

**Characteristics:**
- Smooth output rate (no bursts)
- Requests queue in the bucket up to capacity
- Good for: traffic shaping where downstream systems can't handle bursts (e.g., external API calls)

**Token Bucket vs Leaky Bucket:**
- Token Bucket: allows bursting; output rate can vary
- Leaky Bucket: smooth output; strictly constant rate

### Implementation Approaches

**In-process (single server)**
- Simple but doesn't work across multiple instances

**Redis (distributed)**
```
# Redis INCR + EXPIRE pattern
key = "rate_limit:{user_id}:{window}"
count = INCR(key)
if count == 1: EXPIRE(key, 60)  # Set 60-second window on first request
if count > 100: reject request
```

**Dedicated services**: AWS API Gateway (built-in), Nginx (limit_req module), Cloudflare

---

## 6. Back-of-the-Envelope Estimation

### Why It Matters

Interviewers expect you to estimate scale before designing. Estimates show you understand the implications of your design choices.

### How to Approach

1. Get requirements: users, requests per day, data retention
2. Estimate QPS: `requests_per_day / 86,400` (use 100,000 for easier math)
3. Estimate peak QPS: `average_QPS * 2` to `* 10` depending on traffic patterns
4. Estimate storage: `records_per_day * avg_record_size * retention_days`
5. Estimate bandwidth: `QPS * avg_response_size`

### Common Numbers to Memorize

**Time units:**
- 1 day = 86,400 seconds ≈ 100,000 seconds (for rough math)
- 1 month ≈ 30 days = 2.5 million seconds
- 1 year ≈ 365 days = 31.5 million seconds

**Storage conversions:**
- 1 KB = 10^3 bytes
- 1 MB = 10^6 bytes
- 1 GB = 10^9 bytes
- 1 TB = 10^12 bytes
- 1 PB = 10^15 bytes
- A tweet (140 chars) ≈ 140 bytes
- A typical web page ≈ 100 KB
- A photo (compressed) ≈ 1–3 MB
- A video (compressed, 1 min HD) ≈ 100 MB

**Latency numbers everyone should know (Jeff Dean's numbers, approximate):**

| Operation | Approximate Latency |
|-----------|---------------------|
| L1 cache reference | 1 ns |
| L2 cache reference | 4 ns |
| Main memory (RAM) access | 100 ns |
| SSD random read | 100 µs (100,000 ns) |
| HDD seek | 10 ms (10,000,000 ns) |
| Packet within same datacenter | 0.5 ms |
| Packet US to Europe and back | 150 ms |

Key ratios to internalize:
- Memory is 1,000x faster than SSD
- SSD is 100x faster than HDD
- Same-datacenter network is 1,000x faster than transcontinental

### Example Estimation: Photo Storage System

Requirements: Instagram-like app, 100M DAU, 2% of users upload 1 photo/day

- **Write QPS**: 100M * 0.02 = 2M photos/day = 2M / 86400 ≈ 23 photos/second
- **Read QPS**: each user views 10 photos/day = 1B photo views/day = 11,600/second
- **Storage per day**: 2M photos * 2MB = 4 TB/day
- **Storage per year**: 4 TB * 365 = ~1.5 PB/year
- **Bandwidth (upload)**: 23 photos/sec * 2 MB = 46 MB/sec
- **Bandwidth (download)**: 11,600 req/sec * 2 MB = 23 GB/sec → clearly need CDN

This estimation tells you:
- Need distributed object storage (S3-like) — 1.5 PB is too large for a single server
- Need CDN for 23 GB/sec download bandwidth
- Write path is manageable (23/sec); read path needs heavy caching
