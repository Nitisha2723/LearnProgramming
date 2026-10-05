# Case Study: Netflix

---

## 1. Clarify Requirements

**Functional:**
- Upload and store video content (admin/content team)
- Stream video at multiple quality levels (adaptive bitrate)
- Search and browse the content catalogue
- Personalised recommendations
- Resume playback from where the user left off

**Non-functional:**
- 99.99% availability for playback
- Low startup latency (< 2 seconds)
- Support 200M+ concurrent streams globally
- Smooth adaptive streaming (no buffering on quality switch)

---

## 2. Scale Estimation

```
Users:          220M subscribers; 100M concurrent streams at peak
Video storage:  1 video → 10 quality levels × 4 chunk sizes → 40 files
                Average film: 2 GB per quality = 20 GB total per film
                Library: 15,000 titles × 20 GB = 300 TB (raw catalogue)
                With encodings: ~3 PB total

Bandwidth:      100M streams × 5 Mbps avg = 500 Tbps outbound
                → impossible from a single data centre; must use CDN
```

---

## 3. High-Level Architecture

```
Content Team ──→ Content Ingestion Service ──→ Transcoding Pipeline
                                            ──→ Object Storage (S3)
                                            ──→ CDN (Open Connect)

User Request:
  Client ──→ CDN (Open Connect ISP-level node) ──→ Video stream
         ──→ API Gateway ──→ User Service
                         ──→ Catalogue Service
                         ──→ Recommendation Service
                         ──→ Playback Service (licensing + bookmark)
```

**Open Connect** is Netflix's own CDN, deployed at ISP data centres
worldwide. Most streams are served entirely from the ISP's local node,
never reaching Netflix's origin.

---

## 4. Content Delivery (Streaming)

### Adaptive Bitrate Streaming (ABR)

Video is pre-split into 4-second chunks and encoded at multiple bitrates:

```
Original ──→ Transcoder ──→ 4K (15 Mbps)
                        ──→ 1080p (8 Mbps)
                        ──→ 720p (5 Mbps)
                        ──→ 480p (2.5 Mbps)
                        ──→ 360p (1 Mbps)
```

Client player monitors bandwidth and switches quality level between chunks
without interrupting playback (HLS or DASH protocols).

### Transcoding Pipeline

```
Raw video upload
    ↓
Message Queue (Kafka) → Transcoding Workers (thousands of EC2 instances)
    ↓
Multi-bitrate encoded chunks
    ↓
S3 (origin storage)
    ↓
CDN nodes (pre-positioned popular content)
```

Netflix published their approach to parallel transcoding: one 2-hour film
splits into ~1,800 chunks, each transcoded independently across thousands
of workers — total time reduced from hours to minutes.

---

## 5. Recommendation System

Netflix's personalisation drives ~80% of viewing decisions.

### Architecture

```
User events (watches, pauses, ratings, search)
    ↓
Kafka event stream
    ↓
    ├── Real-time layer: Spark Streaming → update short-term profile
    └── Batch layer: Hadoop/Spark → train collaborative filtering models

Recommendation Service
    ↓ (reads pre-computed scores)
    ↓ personalised ranking
    ↓ A/B test layer (experimentation)
    ↓ homepage rows: "Top Picks", "Because you watched X", "Trending"
```

### Core Algorithms

**Collaborative filtering:**
```
Users who watched what you watched also watched Y
→ Matrix factorisation (SVD) on ratings/watch-history matrix
```

**Content-based filtering:**
```
Identify features of content you liked (genre, director, pacing)
→ Recommend content with similar features
```

**Contextual bandits:**
Netflix uses multi-armed bandit experiments to test thumbnail images,
row ordering, and recommendations simultaneously across user cohorts.

---

## 6. Database Architecture

| Service | Storage | Why |
|---------|---------|-----|
| User accounts | Cassandra | High availability, always accept writes |
| Viewing history | Cassandra | Append-heavy, time-series |
| Content catalogue | MySQL + ElasticSearch | Complex metadata + full-text search |
| Recommendations | Apache Cassandra | Pre-computed vectors, fast reads |
| Session/bookmark | EVCache (Memcached) | Ultra-low latency, ephemeral |

**EVCache** is Netflix's distributed cache. A bookmark write goes to
multiple cache nodes across availability zones simultaneously.

---

## 7. Playback Authorisation

Before streaming, the client must obtain a licence token:

```
1. Client → Playback Service: "start playing film X"
2. Playback Service checks: is user subscribed? has device limit been reached?
3. Returns: CDN URL list, DRM licence (Widevine/FairPlay), chapter timestamps
4. Client requests first chunk from CDN using signed URL
5. CDN validates signed URL → serves content
```

Signed URLs expire in 5 minutes — even if someone extracts the URL,
they can't share it.

---

## 8. Fault Tolerance: Chaos Engineering

Netflix invented **Chaos Monkey** — a tool that randomly terminates
production servers to prove the system handles failure gracefully.

Key patterns Netflix uses:

| Pattern | Implementation |
|---------|----------------|
| Fallback recommendations | If ML service fails, return trending list |
| CDN failover | Multiple CDN nodes per region; auto-reroute on failure |
| Circuit breakers | Hystrix library; fast-fail + fallback on downstream timeouts |
| Bulkhead | Each microservice has its own thread pool; one overload can't cascade |
| Multi-region active-active | US-East, US-West, EU run independently; route by latency |

---

## 9. Bottlenecks Summary

| Bottleneck | Solution |
|-----------|----------|
| Streaming bandwidth | ISP-level CDN (Open Connect) |
| Transcoding time | Massively parallel chunked encoding |
| Recommendation latency | Pre-computed offline; serve from Cassandra |
| Availability | Multi-region active-active + chaos testing |
| Licence validation speed | EVCache (sub-millisecond reads) |
| Startup time | Pre-buffer first 30s at CDN on trending content |
