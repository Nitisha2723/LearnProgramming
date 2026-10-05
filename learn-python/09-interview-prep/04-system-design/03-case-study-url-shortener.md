# Case Study: URL Shortener (TinyURL / Bitly)

---

## 1. Clarify Requirements

**Functional:**
- Given a long URL, return a short URL (e.g., `https://tiny.url/abc123`)
- Given a short URL, redirect to the original long URL
- (Optional) Custom aliases
- (Optional) Expiry time

**Non-functional:**
- Low latency redirects (< 10ms)
- Highly available (99.99% uptime)
- Short URLs are permanent unless explicitly set to expire
- Not required: real-time analytics (can be async)

---

## 2. Scale Estimation

```
Write (create short URL): 100M URLs/day = ~1,200 write/s
Read (redirect):          100:1 read/write = 120,000 read/s

Storage per URL: 500 bytes average
5 years: 100M * 365 * 5 * 500B ≈ 90 TB

Short code: 6 chars, base62 → 62^6 ≈ 56 billion unique URLs
```

---

## 3. API Design

```
POST /urls
  Request:  {"long_url": "https://...", "custom_alias": "myalias", "ttl_days": 30}
  Response: {"short_url": "https://tiny.url/abc123", "created_at": "..."}

GET /{short_code}
  Response: HTTP 301/302 redirect to long URL

DELETE /urls/{short_code}
  Response: 204 No Content
```

**301 vs 302:**
- 301 Permanent: browser caches redirect → less server load, can't update
- 302 Temporary: browser re-queries every time → more server load, can update

Use 302 if you need analytics or want to change the redirect later.

---

## 4. High-Level Design

```
Client → CDN/Load Balancer → URL Shortener Service → DB (reads)
                                                   → Cache (Redis)
                           → DB (writes, Cassandra or MySQL)
```

**Short code generation options:**

**Option A: Hash-based**
```python
import hashlib
import base62

def shorten(long_url: str) -> str:
    hash_bytes = hashlib.md5(long_url.encode()).digest()
    number = int.from_bytes(hash_bytes[:6], "big")
    return base62.encodebytes(number)[:6]
```
- Pros: stateless, same URL → same code
- Cons: collisions possible; need collision handling

**Option B: Counter-based (preferred)**
```python
# Centralised counter (or distributed snowflake ID)
def shorten(long_url: str, counter: int) -> str:
    return base62_encode(counter)
```
- Pros: no collisions guaranteed
- Cons: counter service can be a bottleneck (use pre-allocated ranges)

---

## 5. Database Design

```sql
CREATE TABLE urls (
    short_code  VARCHAR(8)   PRIMARY KEY,
    long_url    TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    expires_at  TIMESTAMP,
    created_by  VARCHAR(64)
);

CREATE INDEX idx_long_url ON urls(long_url);  -- for dedup checks
```

**Cassandra** is a good fit: high write throughput, primary key = short_code.

---

## 6. Redirect Flow (Critical Path)

```
1. Client requests GET /abc123
2. CDN checks if short code is cached → hit? return 302 immediately
3. App layer checks Redis → hit? return 302
4. DB lookup (Cassandra) → found? cache in Redis with TTL, return 302
5. Not found → 404
```

**Redis caching:** `SET abc123 https://long-url.com/page EX 86400`

---

## 7. Bottlenecks and Solutions

| Bottleneck | Solution |
|-----------|----------|
| Write throughput | Async writes, partition by short_code |
| Read throughput | Redis cache (99% hit rate) + CDN |
| Counter service | Pre-allocate ranges per app server |
| Hot URLs | CDN caching for popular redirects |
| Data expiry | Background job sweeps expired rows |

---

## 8. Failure Modes

- **DB down**: serve from Redis cache (degraded — no new URLs)
- **Redis down**: fall back to DB (slower but functional)
- **Counter service down**: use UUID-based fallback
- **Data loss**: replicate DB across 3+ availability zones
