# Case Study: Twitter (Social Feed)

Twitter-style social feed is one of the most challenging system design questions because of the "fan-out problem" — how do you efficiently deliver tweets to millions of followers?

---

## 1. Requirements Clarification

### Functional Requirements

1. **Post tweet**: Users can post text tweets (up to 280 characters) with optional media
2. **Follow users**: Users can follow/unfollow other users
3. **Home timeline**: View tweets from people you follow, in reverse chronological order
4. **Like a tweet**: Click a heart; see like count
5. **Retweet**: Re-share another user's tweet to your followers
6. **User profile**: View a user's tweets in reverse chronological order

### Non-Functional Requirements

- **Scale**: 100 million Daily Active Users (DAU), 500 million tweets/day
- **Performance**: Home timeline loads in < 200ms
- **Availability**: 99.9% uptime
- **Eventual consistency**: OK if a tweet shows up in follower timelines a few seconds late
- **Read-heavy**: Reads >> Writes (many people read; fewer post)

### Out of Scope

- Direct messages
- Trending topics
- Search
- Notifications

---

## 2. Capacity Estimation

### Tweets (Writes)

```
500 million tweets/day
= 500,000,000 / 86,400
≈ 5,800 tweets/second (peak: ~15,000/sec)
```

### Timeline Reads

```
100M DAU, each opens app ~5 times/day, reads 20 tweets per open
= 100M * 5 * 20 = 10 billion timeline reads/day
= 10,000,000,000 / 86,400 ≈ 115,000 reads/second
```

This is 20x the write rate. Read optimization is critical.

### Storage

```
Tweet text: 500M tweets/day * 280 bytes ≈ 140 GB/day text
Media: 10% of tweets have photos (2MB avg) → 50M * 2MB = 100 TB/day media
Tweet metadata (IDs, timestamps, likes): 500M * 100 bytes = 50 GB/day

Text + metadata: ~190 GB/day
Media: stored in object storage (S3), served via CDN
```

---

## 3. Core Challenges

The straightforward parts:
- Storing tweets: write tweet to DB, return
- Storing follows: write to a join table
- Getting a user's own tweets: query by user_id

**THE HARD PROBLEM: Home Timeline**

User A follows 500 people. Each posts several tweets per day. When User A opens their app, they expect to see the 20 most recent tweets from all 500 people they follow. How do you compute this efficiently at scale?

Naive approach:
```sql
-- For User A's timeline:
SELECT * FROM tweets
WHERE user_id IN (SELECT followee_id FROM follows WHERE follower_id = :user_a)
ORDER BY created_at DESC
LIMIT 20;
```

At 100M users with 500 follows each, this query is catastrophically slow. This is why fan-out strategies exist.

---

## 4. Data Model

### Tables

```sql
-- Users
CREATE TABLE users (
    user_id       BIGINT PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    display_name  VARCHAR(100),
    bio           TEXT,
    follower_count INT DEFAULT 0,    -- denormalized for performance
    following_count INT DEFAULT 0,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Tweets
CREATE TABLE tweets (
    tweet_id      BIGINT PRIMARY KEY,   -- Twitter uses Snowflake IDs
    user_id       BIGINT NOT NULL REFERENCES users(user_id),
    content       VARCHAR(280) NOT NULL,
    media_url     TEXT,
    like_count    INT DEFAULT 0,
    retweet_count INT DEFAULT 0,
    reply_to_id   BIGINT,              -- NULL if not a reply
    created_at    TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_tweets_user_id ON tweets(user_id, created_at DESC);

-- Follows (social graph)
CREATE TABLE follows (
    follower_id   BIGINT NOT NULL REFERENCES users(user_id),
    followee_id   BIGINT NOT NULL REFERENCES users(user_id),
    created_at    TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (follower_id, followee_id)
);
CREATE INDEX idx_follows_followee_id ON follows(followee_id);  -- "who follows me?"

-- Likes
CREATE TABLE likes (
    user_id    BIGINT NOT NULL,
    tweet_id   BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, tweet_id)
);
```

### Timeline Cache (Redis)

The most important data structure in the system:

```
Redis Sorted Set: key = "timeline:{user_id}"
                  member = tweet_id (as string)
                  score = unix timestamp of tweet

Example: timeline:user:12345
  tweet_id: 987654321  score: 1700000100  (most recent)
  tweet_id: 987654320  score: 1700000050
  tweet_id: 987654319  score: 1700000010
  ...
  (keep last 800 tweet_ids per user)
```

Operations:
- `ZADD timeline:{user_id} {timestamp} {tweet_id}` — add tweet to timeline
- `ZREVRANGEBYSCORE timeline:{user_id} +inf -inf LIMIT 0 20` — get 20 most recent
- `ZREMRANGEBYRANK timeline:{user_id} 0 -801` — trim to 800 entries (keep most recent 800)

---

## 5. Fan-out Approaches

### Fan-out on Write (Push Model)

When a tweet is posted, immediately write it to all followers' timeline caches.

```
User posts tweet
   ↓
Tweet stored in DB
   ↓
Fan-out worker looks up all followers (e.g., 1,000 followers)
   ↓
Write tweet_id to 1,000 Redis sorted sets
```

**Read path**: `ZREVRANGE timeline:{user_id} 0 19` — instant, no computation

**Pros:**
- Home timeline reads are extremely fast (just a Redis lookup)
- Consistent and pre-computed

**Cons:**
- Writing one tweet by a celebrity with 50M followers = 50M Redis writes
- This is the "celebrity problem" or "hot user problem"
- Write amplification: 1 tweet → 50M cache operations

### Fan-out on Read (Pull Model)

When a user opens their timeline, compute it on the fly.

```
User requests timeline
   ↓
Look up all followees (the 500 people User A follows)
   ↓
Fetch recent tweets from each followee
   ↓
Merge and sort by timestamp
   ↓
Return top 20
```

**Pros:**
- Writing a tweet is simple (just store in DB)
- No write amplification for celebrities

**Cons:**
- Reading the timeline is slow: N+1 queries for N followees
- Expensive aggregation at read time
- Scales poorly as follower count increases

### Hybrid Approach (Twitter's Actual Strategy)

The best approach combines both:

**Regular users** (< X followers, e.g., < 1 million): Use fan-out on write.
- Most users have few followers, so write fan-out is cheap
- Their followers get fast pre-computed timelines

**Celebrities** (> X followers): Use fan-out on read.
- Don't fan out celebrity tweets to millions of Redis sets
- When a user loads their timeline, merge:
  1. Pre-computed timeline from Redis (from regular users they follow)
  2. Recent tweets fetched live from each celebrity they follow
  3. Merge and return top 20

This gives fast reads for most users while avoiding the celebrity write amplification problem.

---

## 6. Timeline Service — Detailed Design

### Write Path (Posting a Tweet)

```
1. Client sends POST /tweets { content: "Hello world" }
2. Tweet Service generates Snowflake ID, stores tweet in DB
3. Publishes event to Kafka: { tweet_id, user_id, timestamp }
4. Fan-out Worker(s) consume from Kafka:
   a. Look up all followers: SELECT follower_id FROM follows WHERE followee_id = :user_id
   b. For each follower, check if user is a celebrity (skip fan-out)
   c. ZADD timeline:{follower_id} {timestamp} {tweet_id}
   d. ZREMRANGEBYRANK timeline:{follower_id} 0 -801  (trim to 800)
5. Return tweet to client immediately (don't wait for fan-out)
```

**Why Kafka?** Fan-out is asynchronous. The tweet is stored immediately; fan-out happens in the background. This decouples tweet creation from timeline update and allows the fan-out workers to scale independently.

### Read Path (Loading Home Timeline)

```
1. Client requests GET /timeline?user_id=12345&limit=20
2. Timeline Service:
   a. Get top 20 tweet_ids from Redis: ZREVRANGE timeline:12345 0 19
   b. Identify celebrities the user follows (separate lookup)
   c. Fetch recent tweets from celebrities from DB/cache
   d. Merge celebrity tweets with regular tweets, sort by timestamp
   e. Hydrate tweet_ids → full tweet objects (batch fetch from Tweet Service)
   f. Return to client
```

**Tweet hydration**: Redis stores only tweet_ids. To return full tweets, batch-fetch from a tweet cache:
```
MGET tweet:{tweet_id_1} tweet:{tweet_id_2} tweet:{tweet_id_3} ...
```
Each tweet is cached individually in Redis with a 24-hour TTL.

### What if a user's timeline isn't in Redis?

(New user, cache eviction, user hasn't visited in days)

On cache miss:
1. Query the database for the user's followees
2. For each followee, fetch their recent tweets
3. Merge and populate the Redis timeline cache
4. Return results

This is expensive but happens rarely (only on cache miss for the user's timeline).

---

## 7. Scaling

### Tweet Service

Stateless. Horizontally scale behind a load balancer. Each instance reads/writes to the same database.

### Fan-out Workers

Stateless consumers from Kafka. Scale by adding consumer instances to the Kafka consumer group. Kafka distributes partitions among consumers automatically.

Number of fan-out workers needed:
```
5,800 tweets/second
Average 500 followers per tweet → 5,800 * 500 = 2.9 million Redis writes/second
Redis can handle ~100K writes/second per instance
Need: 2.9M / 100K = ~30 Redis instances (sharded)
```

### Database Sharding

```
Tweet DB: Shard by user_id (all tweets by a user on same shard)
          → User's timeline query (SELECT WHERE user_id = ?) hits one shard

Follow DB: Two copies:
           - Shard by follower_id (for "who do I follow?")
           - Shard by followee_id (for "who follows me?" — fan-out)
```

### Cache Sharding

Redis timeline caches sharded by user_id using consistent hashing:

```
hash(user_id) → Redis shard 1, 2, 3, ... N
```

Adding a shard only moves 1/N of keys, not all of them (consistent hashing benefit).

### CDN for Media

Photos and videos serve from CDN. The media service stores originals in S3; CDN edges cache frequently-accessed media near users.

### Geographic Distribution

Deploy multiple regions. Users are routed to their nearest region. Each region has its own:
- Tweet services
- Timeline caches
- Database replicas (reads local; writes may need to go to a primary)

Fanout happens within-region for local followers. Cross-region fanout uses a global message bus.

---

## 8. Additional Considerations

### Snowflake IDs (Tweet ID Generation)

Twitter uses Snowflake: a distributed ID generation system that produces time-sorted 64-bit IDs.

```
64-bit ID structure:
[1 bit: sign][41 bits: timestamp][10 bits: machine ID][12 bits: sequence]

41-bit timestamp: milliseconds since custom epoch → ~69 years
12-bit sequence: up to 4,096 IDs per millisecond per machine
```

Benefits:
- IDs are roughly sortable by time (no need to separately sort by `created_at`)
- Generated without database coordination (machines assign themselves unique IDs)

### Like Count

Likes are high-volume writes. Don't update the tweet row directly (heavy write amplification). Instead:
- Accept like events into a Kafka stream
- Batch-aggregate like counts every 5 seconds
- Write aggregated counts back to the tweet record

Accept occasional inconsistency in like counts (eventually consistent).

### Delete Tweet / GDPR

Deleting a tweet requires:
1. Remove from tweet DB
2. Remove from all timeline caches (fan-out deletion is expensive)

Practical approach:
- Mark tweet as deleted in DB (soft delete)
- When loading timeline, filter out deleted tweet_ids
- Background job cleans up cache entries asynchronously

---

## Summary

| Component | Design Choice | Rationale |
|-----------|--------------|-----------|
| Tweet storage | Cassandra/PostgreSQL sharded by user_id | Write-heavy, time-series access pattern |
| Fan-out | Kafka + async fan-out workers | Decouple write from fan-out; handle celebrity traffic |
| Celebrity problem | Hybrid: fan-out on read for celebrities | Avoid write amplification for high-follower accounts |
| Timeline cache | Redis sorted sets (score = timestamp) | O(1) timeline reads |
| ID generation | Snowflake | Distributed, time-sortable, no DB coordination |
| Media | S3 + CDN | Object storage scales independently; CDN for low latency |
