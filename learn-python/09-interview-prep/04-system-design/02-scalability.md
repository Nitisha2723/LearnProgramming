# Scalability

---

## 1. Vertical vs Horizontal Scaling

| | Vertical (Scale Up) | Horizontal (Scale Out) |
|--|--------------------|-----------------------|
| How | Bigger machine (more CPU/RAM) | More machines |
| Limit | Hardware ceiling | Virtually unlimited |
| Cost | Expensive beyond a point | Commodity hardware |
| Downtime | Usually required | None (rolling deploys) |
| Complexity | Simple | Requires distributed design |

**Rule:** start vertical, scale horizontal when needed.

---

## 2. Microservices vs Monolith

### Monolith
- Single deployable unit
- Simple to develop and test initially
- Scales as a whole (wasteful if only one component is hot)
- Fault isolation is weak

### Microservices
- Each service deployed independently
- Can scale individual components
- Technology flexibility per service
- More operational complexity (networking, versioning, distributed tracing)

**When to split:** when a single team can no longer hold the codebase in their
heads, or when scaling requirements diverge significantly between components.

---

## 3. Database Scaling Patterns

### Read Replicas
```
              ┌─ Read Replica
Write → Primary ─┤
              └─ Read Replica
```
- Directs read load away from primary
- Replication lag is a concern

### Caching Layer
```
Client → Cache (Redis) → DB
```
- 99% of reads served from cache
- Cache invalidation is the hard part

### Database Sharding
```
User ID 0-999  → Shard A
User ID 1000-1999 → Shard B
```
- Distributes both reads and writes
- Cross-shard queries are expensive
- Consistent hashing minimises resharding

### CQRS (Command Query Responsibility Segregation)
- Separate write models (commands) from read models (queries)
- Read model can be denormalised, optimised for specific queries

---

## 4. CDN (Content Delivery Network)

Distributes static content (images, JS, CSS, video) to edge nodes close to users.

```
User in Tokyo → CDN Edge (Tokyo) → miss? → Origin (US)
             → CDN Edge (Tokyo)   → hit?  → return cached content
```

**Benefits:**
- Reduced latency (geographic proximity)
- Reduced origin server load
- DDoS protection at edge
- Improved availability

---

## 5. Rate Limiting

Protect your service from abuse and ensure fair usage.

**Algorithms:**
- **Token Bucket**: fill bucket at constant rate; each request consumes a token
- **Leaky Bucket**: requests queued at fixed output rate
- **Fixed Window**: count per time window
- **Sliding Window Log**: precise but memory-intensive

**Implementation:** typically in a reverse proxy (Nginx, API Gateway) backed
by Redis for distributed state.

---

## 6. Service Discovery and Load Balancing

As services scale, static configuration becomes impractical.

**Service Discovery (e.g., Consul, Kubernetes DNS):**
- Services register on startup
- Other services query the registry
- Health checks remove unhealthy instances

**Client-Side Load Balancing:**
```
Client → Service Registry → [server list] → pick one → Server
```

**Server-Side Load Balancing (proxy):**
```
Client → Load Balancer → Server pool
```

---

## 7. Distributed Tracing and Observability

### The Three Pillars
1. **Metrics** — time-series aggregates (QPS, error rate, latency p99)
2. **Logs** — structured event records
3. **Traces** — end-to-end request path across services

### Key Metrics to Track
- **Latency** — p50, p95, p99 (not just average!)
- **Error rate** — 5xx responses / total requests
- **Throughput** — requests per second
- **Saturation** — queue depth, CPU %, memory %

---

## 8. Circuit Breaker Pattern

Prevent cascading failures when a downstream service is unhealthy.

```
States:
  CLOSED → normal, requests pass through
  OPEN   → fast-fail, no requests sent to downstream
  HALF_OPEN → probe: send one request; if OK → CLOSED, else → OPEN
```

```python
class CircuitBreaker:
    def __init__(self, failure_threshold=5, recovery_timeout=60):
        self.failures = 0
        self.threshold = failure_threshold
        self.state = "CLOSED"
        self.last_failure_time = None

    def call(self, fn, *args, **kwargs):
        if self.state == "OPEN":
            import time
            if time.time() - self.last_failure_time > self.recovery_timeout:
                self.state = "HALF_OPEN"
            else:
                raise Exception("Circuit is OPEN")
        try:
            result = fn(*args, **kwargs)
            self.failures = 0
            self.state = "CLOSED"
            return result
        except Exception:
            self.failures += 1
            self.last_failure_time = __import__("time").time()
            if self.failures >= self.threshold:
                self.state = "OPEN"
            raise
```
