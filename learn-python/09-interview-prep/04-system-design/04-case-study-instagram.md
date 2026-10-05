# Case Study: Instagram

---

## 1. Clarify Requirements

**Functional:**
- Upload photos and short videos
- Follow / unfollow users
- View a personalised feed of followed users' posts
- Like and comment on posts
- Search users and hashtags

**Non-functional:**
- Highly available (photos must always be viewable)
- Feed load < 500ms
- Eventual consistency is acceptable for feeds
- Scale: 1 billion users, 100M daily actives

---

## 2. Scale Estimation

```
Photos uploaded: 100M DAU × 2 uploads/day = 200M photos/day
Photo size average: 3 MB
Storage: 200M × 3MB = 600 GB/day → ~220 TB/year

Read QPS (feed + explore): 100:1 read ratio → ~2M reads/sec

Follower graph: 1B users × 200 follows avg = 200B edges (needs graph DB or wide-column)
```

---

## 3. High-Level Architecture

```
Client (iOS/Android/Web)
    ↓
CDN (CloudFront) ← static assets, media
    ↓
API Gateway / Load Balancer
    ↓
Microservices:
  ├── User Service       → PostgreSQL (user profiles)
  ├── Media Service      → Object Storage (S3) + CDN
  ├── Follow Service     → Graph (Cassandra edges)
  ├── Feed Service       → Redis + Cassandra
  ├── Post Service       → Cassandra (posts, metadata)
  ├── Like Service       → Cassandra counter tables
  ├── Comment Service    → Cassandra
  └── Search Service     → Elasticsearch
```

---

## 4. Media Storage

Upload flow:
```
1. Client → Pre-signed S3 URL from API
2. Client uploads directly to S3 (bypasses app servers)
3. S3 triggers Lambda → resize/transcode images
4. Multiple sizes stored: thumbnail, medium, full
5. CloudFront CDN serves all media
```

**Why direct-to-S3:** removes app server from upload path; handles large files
without memory pressure.

---

## 5. Feed Generation

Two approaches:

### Push (Fan-out on write)
```
User posts → write to followers' feed caches immediately
```
- Pros: fast reads (O(1) per feed request)
- Cons: Celebrities with 10M followers → 10M writes per post (fan-out storm)

### Pull (Fan-out on read)
```
User requests feed → fetch posts from each followed user → merge
```
- Pros: no fan-out storm
- Cons: O(n) reads at query time; slow for users with many follows

### Hybrid (Instagram's approach)
- Users with < N followers: push to followers' feed caches
- Celebrities (> N followers): pull-on-read; merged into feed at request time
- Feed cache: Redis sorted set (score = timestamp)

```python
# Redis: store (post_id, timestamp) per user
# ZADD user:{user_id}:feed {timestamp} {post_id}
# ZREVRANGE user:{user_id}:feed 0 19  → latest 20 posts
```

---

## 6. Follower Graph

```
Cassandra table:
  follower_id → [followed_id_1, followed_id_2, ...]  (who user follows)
  followed_id → [follower_id_1, follower_id_2, ...]  (who follows user)
```

Cassandra handles wide rows (millions of followers per celebrity) efficiently.

---

## 7. Search

**Hashtag search:**
- Elasticsearch index on post text and hashtags
- Near real-time indexing via Kafka consumer

**User search:**
- Elasticsearch for fuzzy search
- Prefix trie in Redis for autocomplete

---

## 8. Bottlenecks

| Component | Bottleneck | Solution |
|-----------|-----------|----------|
| Media upload | Bandwidth/latency | Pre-signed S3 URLs, CDN |
| Feed generation | Celebrity fan-out | Hybrid push/pull |
| Follower graph | Wide rows | Cassandra |
| Feed reads | High QPS | Redis sorted sets |
| Search | Full-text | Elasticsearch |
| Notifications | Fan-out | Kafka pub/sub |
