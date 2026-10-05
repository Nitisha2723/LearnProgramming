/**
 * SOLUTION — Exercise 02: E-Commerce Notification System
 *
 * Patterns used:
 *   OBSERVER  — OrderTracker (subject) + OrderEventListener implementations
 *   STRATEGY  — NotificationChannel (EmailChannel, SmsChannel, PushChannel, SlackChannel)
 *   FACTORY   — NotificationFactory creates channels by ChannelType
 *   BUILDER   — NotificationMessage.Builder for constructing messages
 *
 * All patterns integrate in the demo main() at the bottom.
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ============================================================
//  DATA MODEL ENUMS
// ============================================================

enum Sol2_OrderStatus {
    PLACED, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
}

enum Sol2_ChannelType {
    EMAIL, SMS, PUSH, SLACK
}

enum Sol2_Priority {
    LOW, NORMAL, HIGH, URGENT
}

// ============================================================
//  BUILDER PATTERN: NotificationMessage
// ============================================================

/**
 * Immutable value object representing a notification to be delivered.
 *
 * Builder pattern reasons:
 *   - Three required fields; five optional — a telescoping constructor would
 *     be unreadable.
 *   - Immutability is enforced: the private constructor is the only path in.
 *   - Required-field validation is centralised in the Builder constructor.
 */
class Sol2_NotificationMessage {

    // Required
    private final String recipient;
    private final String subject;
    private final String body;

    // Optional
    private final Sol2_Priority priority;
    private final String templateId;
    private final Map<String, String> metadata;
    private final LocalDateTime scheduledAt;
    private final String replyTo;

    private Sol2_NotificationMessage(Builder b) {
        this.recipient   = b.recipient;
        this.subject     = b.subject;
        this.body        = b.body;
        this.priority    = b.priority;
        this.templateId  = b.templateId;
        this.metadata    = Collections.unmodifiableMap(new HashMap<>(b.metadata));
        this.scheduledAt = b.scheduledAt;
        this.replyTo     = b.replyTo;
    }

    public String getRecipient()  { return recipient; }
    public String getSubject()    { return subject; }
    public String getBody()       { return body; }
    public Sol2_Priority getPriority() { return priority; }
    public String getTemplateId() { return templateId; }
    public Map<String, String> getMetadata() { return metadata; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getReplyTo()    { return replyTo; }

    @Override
    public String toString() {
        return "NotificationMessage{"
                + "recipient='" + recipient + '\''
                + ", subject='" + subject + '\''
                + ", priority=" + priority
                + ", scheduled=" + (scheduledAt != null ? scheduledAt : "immediate")
                + '}';
    }

    // ---- Builder -----------------------------------------------

    public static class Builder {

        private final String recipient;
        private final String subject;
        private final String body;

        private Sol2_Priority priority    = Sol2_Priority.NORMAL;
        private String templateId         = null;
        private Map<String, String> metadata = new HashMap<>();
        private LocalDateTime scheduledAt = null;
        private String replyTo            = null;

        /**
         * Enforces required fields at construction time.
         * Fail-fast validation prevents building an unusable message.
         */
        public Builder(String recipient, String subject, String body) {
            if (recipient == null || recipient.isBlank())
                throw new IllegalArgumentException("recipient must not be blank");
            if (subject == null || subject.isBlank())
                throw new IllegalArgumentException("subject must not be blank");
            if (body == null || body.isBlank())
                throw new IllegalArgumentException("body must not be blank");
            this.recipient = recipient;
            this.subject   = subject;
            this.body      = body;
        }

        public Builder priority(Sol2_Priority priority) {
            if (priority == null) throw new IllegalArgumentException("priority must not be null");
            this.priority = priority;
            return this;
        }

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        /** Fluent: each call adds one key-value pair to the metadata map. */
        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder scheduledAt(LocalDateTime scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Sol2_NotificationMessage build() {
            return new Sol2_NotificationMessage(this);
        }
    }
}

// ============================================================
//  STRATEGY PATTERN: NotificationChannel
// ============================================================

/**
 * Strategy interface — each implementation encapsulates the
 * formatting and delivery logic for one channel.
 *
 * Switching channels at runtime is as simple as calling
 *   observer.setChannel(newChannel)
 * without touching any other code.
 */
interface Sol2_NotificationChannel {
    void send(Sol2_NotificationMessage message);
    String getChannelName();
}

/**
 * EMAIL strategy.
 * Real implementation would call an SMTP/SES client.
 */
class Sol2_EmailChannel implements Sol2_NotificationChannel {

    @Override
    public void send(Sol2_NotificationMessage message) {
        System.out.println("[EMAIL] To       : " + message.getRecipient());
        System.out.println("[EMAIL] Subject  : " + message.getSubject());
        System.out.println("[EMAIL] Priority : " + message.getPriority());
        if (message.getReplyTo() != null) {
            System.out.println("[EMAIL] Reply-To : " + message.getReplyTo());
        }
        System.out.println("[EMAIL] Body:");
        System.out.println("        " + message.getBody());
        System.out.println("[EMAIL] -------- sent --------");
    }

    @Override public String getChannelName() { return "EMAIL"; }
}

/**
 * SMS strategy.
 * Enforces the 160-character limit; truncates with ellipsis if needed.
 */
class Sol2_SmsChannel implements Sol2_NotificationChannel {

    private static final int SMS_MAX_LENGTH = 160;

    @Override
    public void send(Sol2_NotificationMessage message) {
        String raw  = message.getSubject() + ": " + message.getBody();
        String text = raw.length() <= SMS_MAX_LENGTH
                ? raw
                : raw.substring(0, SMS_MAX_LENGTH - 3) + "...";

        System.out.println("[SMS] To      : " + message.getRecipient());
        System.out.println("[SMS] Message (" + text.length() + " chars): " + text);
        System.out.println("[SMS] -------- sent --------");
    }

    @Override public String getChannelName() { return "SMS"; }
}

/**
 * PUSH strategy.
 * Reads the device token from message metadata.
 */
class Sol2_PushChannel implements Sol2_NotificationChannel {

    @Override
    public void send(Sol2_NotificationMessage message) {
        String deviceToken = message.getMetadata().get("deviceToken");
        if (deviceToken == null || deviceToken.isBlank()) {
            System.out.println("[PUSH] No device token, skipping.");
            return;
        }

        String body = message.getBody();
        if (body.length() > 100) body = body.substring(0, 97) + "...";

        System.out.println("[PUSH] Device : " + deviceToken);
        System.out.println("[PUSH] Title  : " + message.getSubject());
        System.out.println("[PUSH] Body   : " + body);
        System.out.println("[PUSH] -------- sent --------");
    }

    @Override public String getChannelName() { return "PUSH"; }
}

/**
 * SLACK strategy.
 * Posts to a webhook URL found in metadata, defaulting to #notifications.
 */
class Sol2_SlackChannel implements Sol2_NotificationChannel {

    @Override
    public void send(Sol2_NotificationMessage message) {
        String webhook = message.getMetadata().getOrDefault("webhookUrl", "#notifications");

        System.out.println("[SLACK] Webhook  : " + webhook);
        System.out.println("[SLACK] *" + message.getSubject() + "*");
        System.out.println("[SLACK] " + message.getBody());
        System.out.println("[SLACK] Priority : " + message.getPriority());
        System.out.println("[SLACK] -------- sent --------");
    }

    @Override public String getChannelName() { return "SLACK"; }
}

// ============================================================
//  FACTORY PATTERN: NotificationFactory
// ============================================================

/**
 * Factory — centralises channel creation.
 *
 * Benefits:
 *   - Callers never type "new EmailChannel()" — they ask by type.
 *   - Adding a new channel type only requires editing the factory,
 *     not the callers (OCP-friendly).
 *   - Easy to extend to a caching / singleton factory later.
 */
class Sol2_NotificationFactory {

    private Sol2_NotificationFactory() {} // utility class — no instances

    public static Sol2_NotificationChannel createChannel(Sol2_ChannelType type) {
        return switch (type) {
            case EMAIL -> new Sol2_EmailChannel();
            case SMS   -> new Sol2_SmsChannel();
            case PUSH  -> new Sol2_PushChannel();
            case SLACK -> new Sol2_SlackChannel();
        };
    }
}

// ============================================================
//  OBSERVER PATTERN: OrderTracker (Subject)
// ============================================================

/**
 * Observer interface — implemented by anyone interested in order events.
 */
interface Sol2_OrderEventListener {
    void onOrderStatusChanged(String orderId, Sol2_OrderStatus newStatus);
}

/**
 * Subject — maintains the list of listeners and the order state map.
 *
 * Observer pattern notes:
 *   - addListener() / removeListener() control the subscription list.
 *   - updateStatus() updates state then calls notifyListeners().
 *   - Listeners are notified in registration order.
 *   - A CopyOnWriteArrayList would be safer under concurrency, but
 *     ArrayList is clearer for teaching.
 */
class Sol2_OrderTracker {

    private final Map<String, Sol2_OrderStatus> orders    = new LinkedHashMap<>();
    private final List<Sol2_OrderEventListener> listeners = new ArrayList<>();

    // ---- Subscription management --------------------------------

    public void addListener(Sol2_OrderEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Sol2_OrderEventListener listener) {
        listeners.remove(listener);
    }

    public int listenerCount() {
        return listeners.size();
    }

    // ---- Order tracking -----------------------------------------

    public void placeOrder(String orderId) {
        orders.put(orderId, Sol2_OrderStatus.PLACED);
        notifyListeners(orderId, Sol2_OrderStatus.PLACED);
    }

    public void updateStatus(String orderId, Sol2_OrderStatus newStatus) {
        if (!orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Unknown order: " + orderId);
        }
        orders.put(orderId, newStatus);
        notifyListeners(orderId, newStatus);
    }

    public Sol2_OrderStatus getStatus(String orderId) {
        return orders.get(orderId);
    }

    // ---- Private helpers ----------------------------------------

    /** Iterates all listeners and fires the event on each. */
    private void notifyListeners(String orderId, Sol2_OrderStatus status) {
        // Iterate over a snapshot so a listener can unsubscribe during notification
        List<Sol2_OrderEventListener> snapshot = new ArrayList<>(listeners);
        for (Sol2_OrderEventListener listener : snapshot) {
            listener.onOrderStatusChanged(orderId, status);
        }
    }
}

// ============================================================
//  CONCRETE OBSERVERS
// ============================================================

/**
 * Observer + Strategy combo:
 *   - Implements OrderEventListener (Observer)
 *   - Holds a NotificationChannel that can be swapped at runtime (Strategy)
 *
 * When a status changes, it builds a NotificationMessage (Builder) and
 * delivers it via the current channel.
 */
class Sol2_NotificationObserver implements Sol2_OrderEventListener {

    private Sol2_NotificationChannel channel;
    private final String recipientAddress;

    public Sol2_NotificationObserver(Sol2_NotificationChannel channel, String recipientAddress) {
        this.channel          = Objects.requireNonNull(channel);
        this.recipientAddress = Objects.requireNonNull(recipientAddress);
    }

    /** Hot-swap the delivery channel (Strategy pattern). */
    public void setChannel(Sol2_NotificationChannel channel) {
        this.channel = Objects.requireNonNull(channel);
        System.out.println("[Observer] " + recipientAddress
                + " switched to channel: " + channel.getChannelName());
    }

    @Override
    public void onOrderStatusChanged(String orderId, Sol2_OrderStatus newStatus) {
        // Use Builder to construct the message — optional fields set fluently
        Sol2_NotificationMessage message = new Sol2_NotificationMessage.Builder(
                    recipientAddress,
                    "Order " + orderId + " status update",
                    "Your order " + orderId + " is now: " + newStatus.name())
                .priority(newStatus == Sol2_OrderStatus.CANCELLED
                        ? Sol2_Priority.URGENT
                        : Sol2_Priority.NORMAL)
                .metadata("orderId", orderId)
                .metadata("deviceToken",  "device-" + recipientAddress) // for PUSH
                .metadata("webhookUrl",   "https://hooks.slack.com/demo") // for SLACK
                .build();

        channel.send(message);
    }
}

/**
 * Pure Observer — records all status changes for auditing.
 * Does not send notifications; has no knowledge of channels.
 * Single Responsibility: record events.
 */
class Sol2_AuditLogObserver implements Sol2_OrderEventListener {

    private final List<String> log = new ArrayList<>();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onOrderStatusChanged(String orderId, Sol2_OrderStatus newStatus) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] "
                + "Order " + orderId + " -> " + newStatus;
        log.add(entry);
        System.out.println("[AUDIT] " + entry);
    }

    public List<String> getLog() { return Collections.unmodifiableList(log); }
}

// ============================================================
//  DEMO MAIN
// ============================================================
public class Exercise02_Solution {

    public static void main(String[] args) {

        System.out.println("========== E-Commerce Notification System Demo ==========\n");

        // ---- Wire up the Observer network ----------------------

        Sol2_OrderTracker tracker = new Sol2_OrderTracker();

        // Audit observer records every event
        Sol2_AuditLogObserver auditor = new Sol2_AuditLogObserver();
        tracker.addListener(auditor);

        // Alice gets EMAIL notifications (Strategy: EmailChannel)
        Sol2_NotificationObserver alice = new Sol2_NotificationObserver(
                Sol2_NotificationFactory.createChannel(Sol2_ChannelType.EMAIL),
                "alice@example.com");

        // Bob gets SMS notifications (Strategy: SmsChannel)
        Sol2_NotificationObserver bob = new Sol2_NotificationObserver(
                Sol2_NotificationFactory.createChannel(Sol2_ChannelType.SMS),
                "+1-555-0199");

        tracker.addListener(alice);
        tracker.addListener(bob);

        System.out.println("Listeners registered: " + tracker.listenerCount()); // 3

        // ---- Progress order ORD-001 ----------------------------

        System.out.println("\n--- Placing order ORD-001 ---");
        tracker.placeOrder("ORD-001");

        System.out.println("\n--- Confirming order ORD-001 ---");
        tracker.updateStatus("ORD-001", Sol2_OrderStatus.CONFIRMED);

        System.out.println("\n--- Shipping order ORD-001 ---");
        tracker.updateStatus("ORD-001", Sol2_OrderStatus.SHIPPED);

        // ---- Alice switches to PUSH (Strategy swap at runtime) -

        System.out.println("\n--- Alice switches to PUSH notifications ---");
        alice.setChannel(Sol2_NotificationFactory.createChannel(Sol2_ChannelType.PUSH));

        System.out.println("\n--- Delivering order ORD-001 ---");
        tracker.updateStatus("ORD-001", Sol2_OrderStatus.DELIVERED);

        // ---- Bob unsubscribes (Observer: removeListener) -------

        System.out.println("\n--- Bob unsubscribes ---");
        tracker.removeListener(bob);
        System.out.println("Listeners registered: " + tracker.listenerCount()); // 2

        // ---- Order ORD-002: Bob should NOT receive events ------

        System.out.println("\n--- Placing & cancelling order ORD-002 (Bob is unsubscribed) ---");
        tracker.placeOrder("ORD-002");
        tracker.updateStatus("ORD-002", Sol2_OrderStatus.CANCELLED);

        // ---- Show audit trail ----------------------------------

        System.out.println("\n========== Audit Log ==========");
        auditor.getLog().forEach(System.out::println);

        // ---- Builder standalone demo ---------------------------

        System.out.println("\n========== Builder Pattern Demo ==========");
        Sol2_NotificationMessage scheduled = new Sol2_NotificationMessage.Builder(
                    "ceo@company.com",
                    "ALERT: High-value order cancelled",
                    "Order ORD-002 worth $9,999 was cancelled by the customer.")
                .priority(Sol2_Priority.URGENT)
                .metadata("orderId",    "ORD-002")
                .metadata("webhookUrl", "https://hooks.slack.com/ceo-alerts")
                .replyTo("support@company.com")
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();

        System.out.println("Built: " + scheduled);
        Sol2_NotificationFactory.createChannel(Sol2_ChannelType.SLACK).send(scheduled);

        // ---- Factory standalone demo ---------------------------

        System.out.println("\n========== Factory Pattern Demo ==========");
        Sol2_NotificationMessage quick = new Sol2_NotificationMessage.Builder(
                    "+44-7700-900123",
                    "Flash sale",
                    "50% off everything for the next 2 hours! Use code FLASH50.")
                .priority(Sol2_Priority.HIGH)
                .build();

        for (Sol2_ChannelType type : Sol2_ChannelType.values()) {
            System.out.println("\nDelivering via " + type + ":");
            Sol2_NotificationFactory.createChannel(type).send(quick);
        }

        System.out.println("\n========== Demo Complete ==========");
    }
}
