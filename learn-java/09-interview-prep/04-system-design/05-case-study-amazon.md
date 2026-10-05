# Case Study: Amazon E-Commerce Platform

Designing an Amazon-scale e-commerce system covers product catalog, inventory, cart, order processing, payments, and recommendations. This question tests your ability to decompose a complex domain into coherent services.

---

## 1. Requirements Clarification

### Functional Requirements

1. **Product catalog**: Browse and search millions of products
2. **Product search**: Full-text search with filters (category, price, rating)
3. **Inventory management**: Track stock levels per product/location
4. **Shopping cart**: Add/remove items; cart persists across sessions
5. **Order processing**: Place order, track status through fulfillment
6. **Payments**: Charge credit card; handle failures gracefully
7. **Recommendations**: "Customers who bought this also bought..."

### Non-Functional Requirements

- **Scale**: Millions of products, millions of users, thousands of orders/second during peak (Black Friday)
- **Availability**: 99.99% uptime for checkout (revenue-critical)
- **Consistency**: Inventory must be accurate (no overselling)
- **Performance**: Search results < 200ms, product page < 100ms
- **Fraud protection**: Payment fraud detection

### Out of Scope

- Seller portal / vendor management
- Fulfillment and warehouse operations
- Returns and refunds (complex but similar patterns)
- Reviews (separate service, similar to tweet storage)

---

## 2. High-Level Architecture

```
[Client: Web/Mobile]
        |
   [API Gateway]  ← Authentication, rate limiting
        |
   [Load Balancer]
        |
+-------+--------+--------+--------+-------+
|       |        |        |        |       |
[Product] [Search] [Cart] [Order] [Inventory]
[Service] [Service][Service][Service][Service]
        |        |               |
   [Product DB] [Elasticsearch] [Order DB]
   [Redis Cache]                [Inventory DB]
        |
      [CDN]
   (product images)
        |
   [Recommendation
      Engine]
```

---

## 3. Product Catalog Service

### Data Model

```sql
-- Categories (hierarchical)
CREATE TABLE categories (
    category_id   BIGINT PRIMARY KEY,
    parent_id     BIGINT REFERENCES categories(category_id),
    name          VARCHAR(200) NOT NULL,
    path          VARCHAR(500),  -- materialized path: /Electronics/Phones/Smartphones
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Products
CREATE TABLE products (
    product_id     BIGINT PRIMARY KEY,
    seller_id      BIGINT NOT NULL,
    category_id    BIGINT REFERENCES categories(category_id),
    title          VARCHAR(500) NOT NULL,
    description    TEXT,
    brand          VARCHAR(200),
    price          DECIMAL(12, 2) NOT NULL,
    currency       CHAR(3) DEFAULT 'USD',
    rating         DECIMAL(3, 2),     -- denormalized avg rating
    review_count   INT DEFAULT 0,     -- denormalized count
    is_active      BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT NOW(),
    updated_at     TIMESTAMP DEFAULT NOW()
);

-- Product attributes (flexible schema for varied product types)
CREATE TABLE product_attributes (
    product_id   BIGINT REFERENCES products(product_id),
    attr_name    VARCHAR(100) NOT NULL,
    attr_value   TEXT NOT NULL,
    PRIMARY KEY (product_id, attr_name)
);
-- Example: product_id=123, attr_name="color", attr_value="red"
--          product_id=123, attr_name="size", attr_value="XL"

-- Product images
CREATE TABLE product_images (
    image_id      BIGINT PRIMARY KEY,
    product_id    BIGINT REFERENCES products(product_id),
    cdn_url       TEXT NOT NULL,
    display_order INT NOT NULL,
    is_primary    BOOLEAN DEFAULT FALSE
);
```

### Full-Text Search with Elasticsearch

PostgreSQL full-text search doesn't scale to millions of products with complex faceted search. Use Elasticsearch:

```
Product saved to PostgreSQL
         ↓
Change Data Capture (Debezium) detects change
         ↓
Publishes to Kafka
         ↓
Elasticsearch Indexer consumes and indexes product document
```

**Elasticsearch document structure:**
```json
{
  "product_id": 12345,
  "title": "Apple iPhone 15 Pro Max 256GB",
  "description": "Latest Apple smartphone...",
  "brand": "Apple",
  "category": "Electronics > Phones > Smartphones",
  "price": 1199.99,
  "rating": 4.8,
  "attributes": {
    "color": "Natural Titanium",
    "storage": "256GB",
    "operating_system": "iOS"
  },
  "is_active": true
}
```

**Search request:**
```json
GET /products/_search
{
  "query": { "multi_match": { "query": "iphone 15", "fields": ["title^3", "brand^2", "description"] }},
  "filter": [
    { "range": { "price": { "gte": 500, "lte": 1500 }}},
    { "term": { "brand": "Apple" }}
  ],
  "sort": [{ "rating": "desc" }, { "_score": "desc" }],
  "from": 0, "size": 20
}
```

The `^3` weight boost means title matches count 3x more than description matches.

### Product Image CDN

Product images are stored in S3 and served via CDN:
- Upload path: Seller uploads → S3 → image resizer Lambda creates thumbnails (200x200, 400x400, 800x800) → CDN caches
- Read path: User browser → CDN edge → (miss) → S3

Images are immutable (URL contains hash of content). Once uploaded, they never change — CDN can cache forever.

### Product Page Caching

Product pages are read millions of times per day but change infrequently:
```
Cache-aside: Redis key = "product:{product_id}"
TTL = 30 minutes (accept 30-min stale price/stock)
Invalidate on price change, description update, etc.
```

---

## 4. Inventory Service

### The Inventory Accuracy Challenge

Inventory is one of the hardest problems in e-commerce. Two simultaneous buyers for the last item must not both succeed.

**The race condition:**
```
Stock: 1 unit remaining
Thread A: reads stock = 1, decides to proceed
Thread B: reads stock = 1, decides to proceed
Thread A: decrements stock to 0, confirms order
Thread B: decrements stock to -1, confirms order ← OVERSOLD!
```

### Solution: Optimistic Locking

```sql
-- Inventory table
CREATE TABLE inventory (
    product_id    BIGINT PRIMARY KEY,
    warehouse_id  BIGINT NOT NULL,
    quantity      INT NOT NULL CHECK (quantity >= 0),
    reserved      INT NOT NULL DEFAULT 0,  -- held in active carts
    version       BIGINT NOT NULL DEFAULT 0,  -- for optimistic locking
    updated_at    TIMESTAMP DEFAULT NOW()
);

-- Available quantity = quantity - reserved

-- Optimistic lock update (fails if version has changed since we read)
UPDATE inventory
SET quantity = quantity - 1,
    version = version + 1,
    updated_at = NOW()
WHERE product_id = :product_id
  AND quantity - reserved > 0       -- has available stock
  AND version = :version_we_read;   -- no one else changed it

-- Check rows affected: if 0, someone else updated first → retry
```

If the update affects 0 rows, another transaction modified the inventory between our read and write. Retry the transaction.

### Cart Reservation Model

The flow is designed to prevent overselling:

**Add to cart:**
```
1. Reserve stock: UPDATE inventory SET reserved = reserved + quantity WHERE ...
2. Save cart item to Cart Service (Redis + DB)
3. Reservation expires after 30 minutes if user doesn't checkout
```

**Checkout:**
```
1. Payment authorized
2. Convert reservation to actual deduction:
   UPDATE inventory SET quantity = quantity - :qty, reserved = reserved - :qty WHERE ...
3. Create order record
```

**Cart abandoned:**
```
1. Background job runs every 5 minutes
2. Finds expired reservations (cart items not checked out in 30 min)
3. Releases reserved stock: UPDATE inventory SET reserved = reserved - :qty WHERE ...
```

### Inventory DB Choice

PostgreSQL with ACID transactions. Inventory updates require strict consistency — "eventually consistent" is not acceptable for stock management. Overselling harms customers and revenue.

---

## 5. Order Processing

### Order State Machine

```
PENDING_PAYMENT
     ↓ (payment authorized)
CONFIRMED
     ↓ (warehouse picks items)
PROCESSING
     ↓ (shipped from warehouse)
SHIPPED
     ↓ (carrier delivers)
DELIVERED
     ↓ (return window closed)
COMPLETED

Parallel path:
PENDING_PAYMENT → PAYMENT_FAILED → CANCELLED
CONFIRMED → CANCELLED (user cancels before processing)
```

```sql
CREATE TABLE orders (
    order_id        BIGINT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    status          VARCHAR(30) NOT NULL,  -- state machine status
    total_amount    DECIMAL(12, 2) NOT NULL,
    currency        CHAR(3) DEFAULT 'USD',
    shipping_addr   JSONB NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items (
    order_item_id   BIGINT PRIMARY KEY,
    order_id        BIGINT REFERENCES orders(order_id),
    product_id      BIGINT NOT NULL,
    quantity        INT NOT NULL,
    unit_price      DECIMAL(12, 2) NOT NULL,
    subtotal        DECIMAL(12, 2) NOT NULL
);

-- Immutable audit log of all state transitions
CREATE TABLE order_events (
    event_id        BIGINT PRIMARY KEY,
    order_id        BIGINT REFERENCES orders(order_id),
    event_type      VARCHAR(50) NOT NULL,  -- CREATED, PAYMENT_AUTHORIZED, SHIPPED, etc.
    event_data      JSONB,
    created_at      TIMESTAMP DEFAULT NOW()
);
```

### Saga Pattern for Distributed Transactions

Placing an order spans multiple services: Inventory, Payment, Notification, Fulfillment. In a microservices architecture, these can't share a database transaction.

The **Saga pattern** breaks a distributed transaction into a sequence of local transactions, with compensating transactions for rollback.

**Order placement saga (choreography-based):**

```
1. OrderService: Create order (status=PENDING_PAYMENT)
   → Publishes: OrderCreated event

2. InventoryService: Reserve stock
   → Success: Publishes InventoryReserved
   → Failure: Publishes InventoryReservationFailed

3. PaymentService: Charge customer
   → Success: Publishes PaymentAuthorized
   → Failure: Publishes PaymentFailed
           → InventoryService compensates: releases reservation

4. OrderService: Confirm order (status=CONFIRMED)
   → Publishes OrderConfirmed

5. NotificationService: Send confirmation email
   → Publishes EmailSent
```

If payment fails at step 3, the InventoryService listens for `PaymentFailed` and releases the reservation. Each service only knows about its own step and compensation.

### Idempotency in Payment Processing

Payment is the most critical service. Network failures may cause retries; we must not charge a customer twice.

**Idempotency key pattern:**
```
Client generates: idempotency_key = UUID
Client sends: POST /payments { idempotency_key: "abc-123", amount: 99.99, ... }

PaymentService:
1. Check if idempotency_key already processed
   SELECT * FROM payment_requests WHERE idempotency_key = 'abc-123'
2. If found: return the existing result (don't charge again)
3. If not found: process payment, store { idempotency_key, result } with 24-hour TTL
```

The idempotency key table prevents double charging on retry.

---

## 6. Recommendation Engine

### Collaborative Filtering — "Users Who Bought X Also Bought Y"

Collaborative filtering finds patterns in user behavior without needing to understand product content.

**Item-to-item collaborative filtering (Amazon's original algorithm):**

```
Step 1: For each product pair (A, B), count how many users bought both
  user_1: bought A, B, C
  user_2: bought A, C, D
  user_3: bought B, D, E

  co-occurrence matrix:
  A-B: 1 (user_1)
  A-C: 2 (user_1, user_2)
  A-D: 1 (user_2)
  B-D: 1 (user_3)

Step 2: Normalize by popularity (avoid recommending popular items to everyone)
Step 3: For user viewing product A, recommend top similar products (A-C: 2, A-B: 1, A-D: 1)
```

**Implementation:**
- Batch job (Spark) runs nightly on order history
- Precomputes "similar items" for each product
- Stores in Redis: `similar:{product_id}` → [product_id_1, product_id_2, ...]
- Read path: `LRANGE similar:12345 0 9` → 10 recommendations

### Content-Based Filtering

Recommend products similar to what a user is viewing, based on product attributes.

```
User views: iPhone 15 Pro (brand=Apple, category=Smartphone, price=1199)
→ Recommend other products with high attribute similarity:
   - iPhone 15 (same brand, category, similar price)
   - iPhone 15 Plus (same brand, category)
   - Samsung Galaxy S24 (same category, similar price — diversified)
```

Use Elasticsearch's "more like this" query for content-based recommendations.

### Real-Time vs Batch Recommendations

**Batch (offline)**: Nightly Spark job computes recommendations for all products/users.
- High quality (uses all historical data)
- Stale by up to 24 hours
- Cheap to serve (just Redis lookups)

**Real-time (online)**: Recommendations updated based on current session behavior.
- Faster to reflect browsing session
- More complex (streaming ML pipeline with Kafka + Flink)
- Good for: "you just viewed X, here's similar Y"

In practice, use batch for "bought together" recommendations and real-time for "currently browsing" suggestions.

---

## 7. Database Choices Summary

| Service | Database | Why |
|---------|----------|-----|
| Product catalog | PostgreSQL | ACID, relational queries, complex attributes |
| Product search | Elasticsearch | Full-text search, faceted filters, aggregations |
| Inventory | PostgreSQL | ACID required for stock deduction — no overselling |
| Cart | Redis | Temporary data, fast read/write, TTL support |
| Orders | PostgreSQL | ACID for order + payment consistency |
| Recommendations | Redis | Pre-computed, fast lookup by product_id |
| Product images | S3 + CDN | Object storage scales independently |
| User sessions | Redis | Fast lookup, TTL for expiry |

---

## 8. Handling Peak Traffic (Black Friday)

During peak events, traffic can spike 10–100x above normal.

**Pre-scaling:**
- Spin up additional instances 2 hours before sale begins
- Warm Redis caches with popular products
- Disable or degrade non-critical features (reviews loading, recommendations can be async)

**Queue-based load leveling:**
- Order creation goes into a Kafka queue
- Fixed-size worker pool processes orders at sustainable rate
- Users see "order confirmed" after queue commit (not after full processing)
- Prevents database overload

**Circuit breakers:**
- Inventory service adds circuit breaker around DB writes
- If DB is overloaded, temporarily reject new orders with "try again in 1 minute"
- Better to reject gracefully than to crash

**Rate limiting:**
- Per-user rate limit on checkout attempts (5 per minute)
- Prevents bots from purchasing entire inventory

**Flash sale pre-reservation:**
- For limited items (e.g., 100 units at sale price), pre-load Redis with a counter
- Decrement counter atomically in Redis (`DECR` is atomic)
- Only if Redis counter > 0, proceed with DB transaction
- Prevents 10,000 DB transactions for 100 items

---

## Summary

| Challenge | Solution |
|-----------|----------|
| Product search at scale | Elasticsearch with CDC sync from PostgreSQL |
| Inventory accuracy | Optimistic locking + cart reservation model |
| Distributed transactions | Saga pattern with Kafka choreography |
| Double charging | Idempotency keys with 24-hour storage |
| Recommendations | Batch collaborative filtering (nightly Spark) + Redis cache |
| Peak traffic | Queue-based load leveling + Redis counter for flash sales |
| Product images | S3 + CDN with content-addressed immutable URLs |
