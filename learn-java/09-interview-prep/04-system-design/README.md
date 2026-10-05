# System Design Interview Preparation

System design interviews assess your ability to architect scalable, reliable, and maintainable systems. Unlike coding interviews with a single correct answer, system design is open-ended — interviewers evaluate your thought process, communication, and ability to make informed trade-offs.

---

## What to Expect

- **Duration**: 45–60 minutes
- **Format**: Whiteboard or virtual diagramming (Excalidraw, Miro)
- **Level**: Expected more from senior/staff engineers; juniors may face simplified variants
- **Goal**: Show you can think at scale and communicate engineering decisions clearly

---

## How to Structure Your Answer

A strong system design answer follows a predictable arc. Practice this framework until it's automatic:

### Step 1: Requirements Clarification (5 min)

Never dive straight into design. Ask clarifying questions to show you think before you build.

**Functional requirements** (what the system should do):
- What are the core features? (List them, confirm which are in scope)
- What are the inputs and outputs?
- Any specific user flows?

**Non-functional requirements** (quality attributes):
- How many users / requests per second?
- What latency is acceptable? (p99 < 100ms?)
- How durable must data be? Can we afford data loss?
- Is the system read-heavy, write-heavy, or balanced?
- Geographic distribution? Single region or global?

**Out of scope** (explicitly state what you are NOT designing):
- Helps manage time; shows good judgment

### Step 2: Capacity Estimation (5 min)

Back-of-the-envelope math shows you understand scale implications.

Common estimates:
- **QPS**: requests/day ÷ 86,400 seconds/day (use 100K for rough math)
- **Storage**: object count × average object size
- **Bandwidth**: QPS × average payload size

If you're off by 2x, that's fine. The point is demonstrating you think about scale before designing.

### Step 3: High-Level Design (15 min)

Draw the major components and data flows. Start simple, then add complexity.

Typical components to consider:
- **Client** (web, mobile)
- **DNS + CDN** (for static assets)
- **Load Balancer**
- **API servers** (stateless, horizontally scalable)
- **Cache** (Redis/Memcached)
- **Primary database**
- **Message queue** (for async processing)
- **Background workers**
- **Object storage** (S3 for files/images)

### Step 4: Deep Dive (20 min)

The interviewer will guide this — they want to explore areas of interest or challenge you. Common deep dives:

- **Database schema**: Tables, indexes, query patterns
- **API design**: Endpoints, request/response shapes
- **Bottlenecks**: Where does the design break at 10x scale?
- **Specific components**: How does the cache invalidation work? How do you handle failures?

### Step 5: Trade-offs and Discussion (10 min)

Proactively address what your design doesn't handle well:
- "We chose SQL for consistency, but at massive scale we'd need sharding"
- "Fan-out on write is fast for reads but hurts users with millions of followers"
- "This is a single region design; multi-region would require conflict resolution"

Interviewers want to see that you understand the limitations of your choices.

---

## Common Topics

The files in this section cover:

| File | Topic |
|------|-------|
| `01-design-fundamentals.md` | Core building blocks: load balancing, caching, databases, message queues, microservices |
| `02-scalability.md` | Scaling strategies: horizontal scaling, sharding, CAP theorem, rate limiting |
| `03-case-study-url-shortener.md` | Classic design: URL shortener (Bit.ly) |
| `04-case-study-twitter.md` | Advanced design: Twitter/social feed (fan-out problem) |
| `05-case-study-amazon.md` | E-commerce: product catalog, inventory, order processing |

---

## Common Mistakes to Avoid

1. **Jumping to solutions without clarifying requirements** — Always ask first
2. **Ignoring scale** — Your design should handle the stated traffic, not just work in theory
3. **Not discussing trade-offs** — Every decision has pros and cons; acknowledge them
4. **Being too detailed too early** — Start high-level, go deep only when asked
5. **Silence** — Think out loud; interviewers evaluate your reasoning, not just your diagram
6. **Premature optimization** — Don't add Kafka/microservices/sharding until you justify the need

---

## Recommended Practice Approach

1. For each topic in these notes, be able to explain it in 2 minutes from memory
2. Practice drawing a system on paper/whiteboard before the interview
3. Do mock interviews with a friend or on Pramp/interviewing.io
4. For each case study, practice estimating capacity without looking at the numbers

---

## Useful Resources

- *Designing Data-Intensive Applications* by Martin Kleppmann (the bible of system design)
- *System Design Interview* by Alex Xu (concise, interview-focused)
- High Scalability blog: highscalability.com
- ByteByteGo newsletter by Alex Xu
