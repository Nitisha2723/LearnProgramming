# Case Study: URL Shortener (Bit.ly)

A URL shortener takes a long URL and generates a short alias. It's a classic system design question that tests database design, hash algorithms, caching, and scalability.

---

## 1. Requirements Clarification

Always start here. Ask the interviewer before designing.

### Functional Requirements

1. **Shorten URL**: Given a long URL, return a unique short URL (e.g., `bit.ly/xK3mPq`)
2. **Redirect**: Accessing the short URL redirects to the original long URL
3. **Custom aliases**: Users can optionally specify a custom short code (e.g., `bit.ly/my-blog`)
4. **Expiration**: URLs can have an expiration date; expired short URLs return 404
5. **Analytics** (nice-to-have): Track click counts per short URL

### Non-Functional Requirements

- **High availability**: 99.9% uptime — redirect failure means broken links
- **Low latency**: Redirects must be fast (< 100ms) — users won't notice
- **Scale**: Handle 100 million URLs created per day
- **Durability**: Short URLs should never be accidentally deleted

### Out of Scope (for this interview)

- User accounts and authentication
- URL preview / safety checking
- Dashboard with detailed analytics

---

## 2. Capacity Estimation

### Write Volume (URL Creation)

```
100 million new URLs/day
= 100,000,000 / 86,400 seconds
≈ 1,160 writes/second
≈ 1.2K writes/second
```

### Read Volume (Redirects)

Assume a 10:1 read-to-write ratio (most shortened URLs get shared and clicked many times):

```
10 * 1,160 = 11,600 reads/second
≈ ~12K reads/second
```

This is a read-heavy system. Design should prioritize read path optimization.

### Storage

Assume:
- Each URL record ≈ 500 bytes (short URL + original URL + metadata)
- Retain data for 10 years

```
100M URLs/day * 365 days/year * 10 years = 365 billion records
365 billion * 500 bytes = 182.5 TB
≈ 200 TB total storage
```

Not huge by modern standards — a distributed database can handle this.

### Bandwidth

```
Write bandwidth: 1,160 req/sec * 500 bytes ≈ 580 KB/sec
Read bandwidth: 11,600 req/sec * 500 bytes ≈ 5.8 MB/sec
```

Bandwidth is not a bottleneck.

---

## 3. High-Level Design

```
[Client]
   |
   ↓ HTTP POST /shorten  or  GET /{shortCode}
[Load Balancer]
   |
   ↓
[API Gateway / URL Service]
   |        |
   ↓        ↓
[Cache]  [Database]
(Redis)  (Primary + Replicas)
```

### Write Path (URL Shortening)

1. Client sends `POST /shorten` with `{ "url": "https://very-long-url.com/..." }`
2. API Gateway authenticates and routes to URL Service
3. URL Service generates a short code
4. Stores `short_code → long_url` mapping in database
5. Returns `{ "shortUrl": "https://bit.ly/xK3mPq" }`

### Read Path (Redirect)

1. Client requests `GET https://bit.ly/xK3mPq`
2. Load balancer routes to URL Service
3. URL Service checks cache (Redis) for short code
4. **Cache HIT**: Return HTTP 301/302 redirect immediately (fast path)
5. **Cache MISS**: Query database → populate cache → return redirect

---

## 4. Data Model

### Primary Table: `url_mappings`

```sql
CREATE TABLE url_mappings (
    short_code   VARCHAR(10)   PRIMARY KEY,   -- e.g., "xK3mPq"
    original_url TEXT          NOT NULL,       -- the full long URL
    user_id      BIGINT,                        -- NULL if anonymous
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP,                    -- NULL means never expires
    click_count  BIGINT        NOT NULL DEFAULT 0,
    is_custom    BOOLEAN       NOT NULL DEFAULT FALSE
);

-- Index for looking up by user
CREATE INDEX idx_url_mappings_user_id ON url_mappings(user_id);

-- Index for finding expired URLs (cleanup job)
CREATE INDEX idx_url_mappings_expires_at ON url_mappings(expires_at)
    WHERE expires_at IS NOT NULL;
```

### Notes on Schema Design

- **Primary key is `short_code`**: All lookups are by short code, so it's the PK
- **`original_url` as TEXT**: Long URLs can exceed VARCHAR limits
- **`expires_at` nullable**: NULL means the URL never expires
- **`click_count`**: Denormalized here for simplicity; at scale, use async counter increment via message queue

---

## 5. URL Shortening Algorithm

The core problem: given a long URL, generate a unique, short identifier.

### Option 1: MD5 Hash (Not Recommended)

Take the MD5 hash of the long URL, use the first 7 characters.

```java
String hash = DigestUtils.md5Hex(originalUrl);
String shortCode = hash.substring(0, 7);  // e.g., "a3f8d9c"
```

**Problem: Collisions**
- Different long URLs can produce the same 7-character prefix
- Two users shortening different URLs may get the same short code
- Handling collisions requires retrying with different substrings: complex and non-deterministic

**Problem: Not reversible**
- Can't tell from the short code what the original URL was (requires DB lookup — which you're doing anyway, so this isn't really a problem)

### Option 2: Base62 of Auto-Increment ID

Use a database auto-increment ID, then convert to Base62.

```java
// Auto-increment generates ID: 1234567
// Convert to Base62
String shortCode = toBase62(1234567);  // "5Bm"

// Base62 alphabet: 0-9 (10) + a-z (26) + A-Z (26) = 62 chars
// 62^7 = 3.5 trillion possible short codes (7 chars)
```

**Base62 encoding:**
```java
private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

String toBase62(long num) {
    StringBuilder sb = new StringBuilder();
    while (num > 0) {
        sb.insert(0, CHARS.charAt((int)(num % 62)));
        num /= 62;
    }
    // Pad to 7 chars for consistent length
    while (sb.length() < 7) sb.insert(0, '0');
    return sb.toString();
}
```

**Problem: Predictable**
- Short codes are sequential: xK3mPq, xK3mPr, xK3mPs
- Users can enumerate all URLs by incrementing the counter
- For public shorteners this may be acceptable; for private URLs it's a privacy issue

**Problem: Single point of failure**
- Auto-increment requires a single database as the source of truth
- Can't generate IDs across multiple nodes without coordination

### Option 3: Distributed Counter + Base62 (Recommended)

Use a distributed counter service (e.g., Redis INCR or a dedicated ID service like Twitter Snowflake) to generate unique IDs, then encode in Base62.

```java
// ID generation service (e.g., Redis INCR)
long id = redis.incr("url_counter");

// Convert to Base62
String shortCode = toBase62(id);

// Store in database
db.insert(shortCode, originalUrl, metadata);
```

**Why this works at scale:**
- Redis INCR is atomic — no two requests get the same counter value
- Counter service can be horizontally partitioned (each shard gets a range: shard 1 handles 0–1B, shard 2 handles 1B–2B)
- Base62 encoding makes short codes compact and URL-safe

**How many unique short codes can we generate?**

```
7 characters in Base62:
62^7 = 3,521,614,606,208 ≈ 3.5 trillion URLs

At 100M URLs/day:
3.5 trillion / 100M = 35,000 days ≈ 95 years before exhaustion
```

---

## 6. Database Choice

### For the URL Mappings

**Key-Value Store (DynamoDB, Redis)**: Ideal. Lookups are always by primary key (short code). No complex queries. Horizontal scaling built-in.

**Relational Database (PostgreSQL)**: Also works well. Primary key lookup by short code is fast. Supports user analytics queries.

**Recommended**: Use a combination:
- **DynamoDB or Cassandra** for the `short_code → original_url` mapping (high read throughput, scales automatically)
- **Redis** as a caching layer in front of the database

### Caching Strategy for Reads

The read path must be fast. 80% of traffic likely goes to 20% of URLs (Pareto principle). Cache the hot URLs.

```
Redis key: "url:{short_code}"
Redis value: "https://original-long-url.com/..."
TTL: 24 hours (or until URL is deleted/expired)
Eviction policy: LRU (evict least recently used URLs)
```

**Cache hit rate**: With 100M URLs total and Redis holding top 10M in memory, expect 80%+ cache hit rate.

**Redis memory needed**:
```
10M cached URLs * (10 bytes key + 200 bytes URL + Redis overhead ~50 bytes) = 2.6 GB
```
Very affordable.

### HTTP Redirect Codes

- **301 (Permanent Redirect)**: Browser caches the redirect and never calls the short URL service again. Saves server load but prevents click tracking.
- **302 (Temporary Redirect)**: Browser always calls the service. Enables click tracking and analytics. Slightly more latency.

Use 302 if click analytics matter; use 301 if you want to minimize server load.

---

## 7. Scaling

### Multiple API Servers

The URL Service is stateless. Scale horizontally:

```
[Client]
   |
[Load Balancer]
   |------------|------------|
[URL Service 1] [URL Service 2] [URL Service 3]
   |
[Redis Cache (shared)]
   |
[Database]
```

### Database Replication

- **Primary**: Handle all writes (URL creation)
- **Replicas (2–3)**: Handle all reads (redirects)

Since read:write = 10:1, three read replicas can handle 30x the write capacity.

### Global Distribution

For a global service, add CDN or deploy regionally:

```
US users → US region (URL Service + DB + Cache)
EU users → EU region (URL Service + DB + Cache)
          (databases replicate across regions for short URL lookups)
```

---

## 8. Edge Cases

### Collision Handling (for hash-based approach)

If short code `xK3mPq` already exists for a different URL:
1. Re-hash with a salt: `MD5(url + "salt1")`
2. Repeat until unique slot found
3. Maximum retries: 3 (after that, use a fallback like UUID)

With distributed counter approach, collisions are impossible by design.

### Custom Aliases

Users request a specific short code (e.g., `bit.ly/my-portfolio`):

1. Check if custom alias already exists in database
2. If taken: return error "Alias already in use"
3. If available: store with `is_custom = TRUE`

**Rate limiting**: Prevent users from squatting on common words. Enforce maximum alias length and allowed character set.

### URL Expiration

**Lookup time check**: When serving a redirect, check `expires_at`:
```java
if (mapping.expiresAt != null && mapping.expiresAt.isBefore(Instant.now())) {
    return HTTP 410 Gone;
}
```

**TTL in Redis**: Set `EXPIREAT` to match `expires_at` so Redis automatically evicts expired entries from cache.

**Database cleanup job**: Run a daily background job to delete expired mappings and free storage:
```sql
DELETE FROM url_mappings WHERE expires_at < NOW() AND expires_at IS NOT NULL;
```

### Same URL, Multiple Short Codes

If user shortens the same URL twice, should they get the same short code?

**Option A: Deduplicate** — Hash the original URL, check if already shortened, return existing short code. Saves storage, but users can't create two separate links to the same URL.

**Option B: Allow duplicates** — Each shortening creates a new record. Simpler, uses more storage, enables independent tracking of click counts.

**Recommendation**: Allow duplicates (Option B) — storage is cheap and it's simpler.

---

## Summary

| Design Decision | Choice | Rationale |
|----------------|--------|-----------|
| ID generation | Distributed counter + Base62 | Unique, no collisions, not sequential |
| Database | Cassandra/DynamoDB + Redis cache | Key-value access pattern, high read throughput |
| Read path | Cache-aside with Redis | 80%+ cache hit rate, sub-millisecond reads |
| Redirect code | HTTP 302 | Enables click tracking |
| Scaling | Stateless API servers + replicas | Simple horizontal scaling |
| Expiration | Redis TTL + cleanup job | Lazy deletion at read time + batch cleanup |
