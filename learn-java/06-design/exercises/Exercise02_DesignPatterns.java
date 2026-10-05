// EXERCISE 02: Build an E-Commerce Notification System
//
// Use design patterns to build a notification system for an e-commerce platform.
//
// REQUIREMENTS:
// 1. OBSERVER: Users can subscribe to order status updates (OrderTracker)
// 2. STRATEGY: Multiple notification channels (Email, SMS, Push, Slack)
//    - Each channel has different formatting and delivery logic
// 3. FACTORY: NotificationFactory creates the right notification type
// 4. BUILDER: NotificationMessage has subject, body, recipient, priority, metadata
//
// STARTER CODE IS PROVIDED BELOW.
// Complete all methods marked with: // TODO: Implement this
//
// HINTS: See theory/02-design-patterns-creational.md etc.
// SOLUTION: See solutions/Exercise02_Solution.java

import java.util.*;
import java.time.LocalDateTime;

// ============================================================
//  DATA MODEL
// ============================================================

/** Possible statuses an order can be in. */
enum OrderStatus {
    PLACED, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
}

/** Notification channels supported by the platform. */
enum ChannelType {
    EMAIL, SMS, PUSH, SLACK
}

/** Priority level affects how urgently a notification is delivered. */
enum Priority {
    LOW, NORMAL, HIGH, URGENT
}

// ============================================================
//  BUILDER PATTERN: NotificationMessage
//  A notification has many optional fields; a builder prevents
//  telescoping constructors and ensures required fields are set.
// ============================================================

/**
 * Immutable notification message.
 * Obtain instances via {@link NotificationMessage.Builder}.
 */
class NotificationMessage {

    // Required fields
    private final String recipient;   // e.g. email address, phone number, user ID
    private final String subject;
    private final String body;

    // Optional fields
    private final Priority priority;
    private final String templateId;
    private final Map<String, String> metadata; // key-value data for the channel
    private final LocalDateTime scheduledAt;    // null = send immediately
    private final String replyTo;

    // Private constructor: only the Builder can create instances
    private NotificationMessage(Builder builder) {
        this.recipient   = builder.recipient;
        this.subject     = builder.subject;
        this.body        = builder.body;
        this.priority    = builder.priority;
        this.templateId  = builder.templateId;
        this.metadata    = Collections.unmodifiableMap(builder.metadata);
        this.scheduledAt = builder.scheduledAt;
        this.replyTo     = builder.replyTo;
    }

    // Getters
    public String getRecipient()   { return recipient; }
    public String getSubject()     { return subject; }
    public String getBody()        { return body; }
    public Priority getPriority()  { return priority; }
    public String getTemplateId()  { return templateId; }
    public Map<String, String> getMetadata() { return metadata; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getReplyTo()     { return replyTo; }

    @Override
    public String toString() {
        return "NotificationMessage{"
                + "recipient='" + recipient + '\''
                + ", subject='" + subject + '\''
                + ", priority=" + priority
                + '}';
    }

    // ---- Builder -----------------------------------------------

    public static class Builder {

        // Required
        private final String recipient;
        private final String subject;
        private final String body;

        // Optional — defaults provided
        private Priority priority         = Priority.NORMAL;
        private String templateId         = null;
        private Map<String, String> metadata = new HashMap<>();
        private LocalDateTime scheduledAt = null;
        private String replyTo            = null;

        /**
         * Create a builder with the three required fields.
         *
         * @param recipient the destination address/ID (must not be blank)
         * @param subject   subject line (must not be blank)
         * @param body      message body (must not be blank)
         * @throws IllegalArgumentException if any required field is blank
         */
        public Builder(String recipient, String subject, String body) {
            // TODO: Implement this
            //   1. Validate that none of the three arguments are null or blank.
            //      Throw IllegalArgumentException with a descriptive message if they are.
            //   2. Assign the three fields.
            throw new UnsupportedOperationException("TODO: implement Builder constructor");
        }

        /** Sets the delivery priority. */
        public Builder priority(Priority priority) {
            // TODO: Implement this
            //   Validate that priority is not null, then set the field and return this.
            throw new UnsupportedOperationException("TODO: implement priority()");
        }

        /** Attaches a template identifier (optional). */
        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        /** Adds a single metadata key-value pair. */
        public Builder metadata(String key, String value) {
            // TODO: Implement this — add the pair to the metadata map, return this
            throw new UnsupportedOperationException("TODO: implement metadata()");
        }

        /** Sets a scheduled delivery time (null = immediate). */
        public Builder scheduledAt(LocalDateTime scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        /** Sets a reply-to address. */
        public Builder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        /**
         * Builds and returns the immutable {@link NotificationMessage}.
         *
         * @return new NotificationMessage
         */
        public NotificationMessage build() {
            // TODO: Implement this — return new NotificationMessage(this)
            throw new UnsupportedOperationException("TODO: implement build()");
        }
    }
}

// ============================================================
//  STRATEGY PATTERN: NotificationChannel
//  Each channel knows how to format AND deliver a message.
//  The OrderTracker can switch channels at runtime.
// ============================================================

/**
 * Strategy interface for a notification delivery channel.
 * Implement once per channel; swap channels without touching
 * any other class.
 */
interface NotificationChannel {
    /**
     * Send the notification.
     * @param message the notification to deliver
     */
    void send(NotificationMessage message);

    /** Returns a human-readable channel name for logging. */
    String getChannelName();
}

/**
 * EMAIL channel.
 * Formats messages with a subject header and HTML-like body.
 * Delivery: prints to console (simulating SMTP).
 */
class EmailChannel implements NotificationChannel {

    @Override
    public void send(NotificationMessage message) {
        // TODO: Implement this
        //   Print a formatted email to the console, e.g.:
        //   [EMAIL] To: <recipient>
        //   [EMAIL] Subject: <subject>
        //   [EMAIL] Priority: <priority>
        //   [EMAIL] Body:
        //   <body>
        //   [EMAIL] -------- sent --------
        throw new UnsupportedOperationException("TODO: implement EmailChannel.send()");
    }

    @Override
    public String getChannelName() { return "EMAIL"; }
}

/**
 * SMS channel.
 * Truncates body to 160 characters (SMS limit).
 * Delivery: prints to console (simulating SMS gateway).
 */
class SmsChannel implements NotificationChannel {

    private static final int SMS_MAX_LENGTH = 160;

    @Override
    public void send(NotificationMessage message) {
        // TODO: Implement this
        //   1. Build the SMS text: subject + ": " + body
        //   2. Truncate to SMS_MAX_LENGTH characters (append "..." if truncated)
        //   3. Print:
        //      [SMS] To: <recipient>
        //      [SMS] Message (<length> chars): <sms text>
        //      [SMS] -------- sent --------
        throw new UnsupportedOperationException("TODO: implement SmsChannel.send()");
    }

    @Override
    public String getChannelName() { return "SMS"; }
}

/**
 * PUSH notification channel.
 * Uses the metadata map to get a device token.
 * Delivery: prints to console (simulating Firebase/APNs).
 */
class PushChannel implements NotificationChannel {

    @Override
    public void send(NotificationMessage message) {
        // TODO: Implement this
        //   1. Get the device token from message.getMetadata().get("deviceToken")
        //      - If absent, print "[PUSH] No device token, skipping." and return.
        //   2. Print:
        //      [PUSH] Device: <deviceToken>
        //      [PUSH] Title: <subject>
        //      [PUSH] Body: <body (truncated to 100 chars)>
        //      [PUSH] -------- sent --------
        throw new UnsupportedOperationException("TODO: implement PushChannel.send()");
    }

    @Override
    public String getChannelName() { return "PUSH"; }
}

/**
 * SLACK channel.
 * Posts to a Slack webhook URL stored in metadata["webhookUrl"].
 * Delivery: prints to console (simulating Slack Incoming Webhooks).
 */
class SlackChannel implements NotificationChannel {

    @Override
    public void send(NotificationMessage message) {
        // TODO: Implement this
        //   1. Get webhook URL from metadata["webhookUrl"]
        //      - If absent, use "#notifications" as default channel.
        //   2. Format as Slack block-kit style output:
        //      [SLACK] Webhook: <url or "#notifications">
        //      [SLACK] *<subject>*
        //      [SLACK] <body>
        //      [SLACK] Priority: <priority>
        //      [SLACK] -------- sent --------
        throw new UnsupportedOperationException("TODO: implement SlackChannel.send()");
    }

    @Override
    public String getChannelName() { return "SLACK"; }
}

// ============================================================
//  FACTORY PATTERN: NotificationFactory
//  Central place to create channel instances.
//  Clients ask for a channel by type; they don't call "new".
// ============================================================

/**
 * Factory that creates {@link NotificationChannel} instances.
 */
class NotificationFactory {

    /**
     * Returns a channel for the given type.
     *
     * @param type the desired channel
     * @return appropriate NotificationChannel implementation
     * @throws IllegalArgumentException for unknown types
     */
    public static NotificationChannel createChannel(ChannelType type) {
        // TODO: Implement this
        //   Use a switch expression (or switch statement) over ChannelType values:
        //     EMAIL  -> return new EmailChannel()
        //     SMS    -> return new SmsChannel()
        //     PUSH   -> return new PushChannel()
        //     SLACK  -> return new SlackChannel()
        //   For anything unrecognised throw IllegalArgumentException.
        throw new UnsupportedOperationException("TODO: implement NotificationFactory.createChannel()");
    }
}

// ============================================================
//  OBSERVER PATTERN: OrderTracker
//  Observers register interest in order events.
//  When an order changes status the tracker notifies all
//  registered observers.
// ============================================================

/**
 * Observer interface — any class that wants to hear about
 * order status changes must implement this.
 */
interface OrderEventListener {
    /**
     * Called when an order's status changes.
     *
     * @param orderId   the affected order
     * @param newStatus the new status
     */
    void onOrderStatusChanged(String orderId, OrderStatus newStatus);
}

/**
 * Subject (Observable) — maintains the list of listeners and
 * notifies them when an order's status changes.
 */
class OrderTracker {

    // Map of orderId -> current status
    private final Map<String, OrderStatus> orders = new HashMap<>();

    // List of registered observers
    private final List<OrderEventListener> listeners = new ArrayList<>();

    // ---- Subscription management --------------------------------

    /**
     * Registers a listener to receive status change events.
     * Duplicate registrations are silently ignored.
     */
    public void addListener(OrderEventListener listener) {
        // TODO: Implement this — add listener if not already present
        throw new UnsupportedOperationException("TODO: implement addListener()");
    }

    /**
     * Removes a previously registered listener.
     * Removing an unknown listener is a no-op.
     */
    public void removeListener(OrderEventListener listener) {
        // TODO: Implement this
        throw new UnsupportedOperationException("TODO: implement removeListener()");
    }

    /** Returns how many listeners are currently registered. */
    public int listenerCount() {
        // TODO: Implement this
        throw new UnsupportedOperationException("TODO: implement listenerCount()");
    }

    // ---- Order tracking -----------------------------------------

    /**
     * Registers a new order at PLACED status and notifies listeners.
     */
    public void placeOrder(String orderId) {
        // TODO: Implement this
        //   1. Store orderId -> OrderStatus.PLACED in the map.
        //   2. Call notifyListeners(orderId, OrderStatus.PLACED).
        throw new UnsupportedOperationException("TODO: implement placeOrder()");
    }

    /**
     * Updates an order's status and notifies all listeners.
     *
     * @throws IllegalArgumentException if orderId is unknown
     */
    public void updateStatus(String orderId, OrderStatus newStatus) {
        // TODO: Implement this
        //   1. Check orderId is in the map; throw IllegalArgumentException if not.
        //   2. Update the status in the map.
        //   3. Call notifyListeners(orderId, newStatus).
        throw new UnsupportedOperationException("TODO: implement updateStatus()");
    }

    /** Returns the current status of an order, or null if unknown. */
    public OrderStatus getStatus(String orderId) {
        return orders.get(orderId);
    }

    // ---- Private helpers ----------------------------------------

    /** Notifies all registered listeners. */
    private void notifyListeners(String orderId, OrderStatus status) {
        // TODO: Implement this — iterate listeners and call onOrderStatusChanged
        throw new UnsupportedOperationException("TODO: implement notifyListeners()");
    }
}

// ============================================================
//  CONCRETE OBSERVERS
//  Each observer decides what to do when it hears an event.
// ============================================================

/**
 * Sends a notification via the configured channel whenever
 * an order reaches a notable status.
 */
class NotificationObserver implements OrderEventListener {

    private NotificationChannel channel;
    private final String recipientAddress;

    public NotificationObserver(NotificationChannel channel, String recipientAddress) {
        this.channel          = channel;
        this.recipientAddress = recipientAddress;
    }

    /** Allows hot-swapping the notification channel (Strategy). */
    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onOrderStatusChanged(String orderId, OrderStatus newStatus) {
        // TODO: Implement this
        //   1. Create a NotificationMessage using NotificationMessage.Builder with:
        //      - recipient = recipientAddress
        //      - subject = "Order " + orderId + " status update"
        //      - body = "Your order " + orderId + " is now: " + newStatus.name()
        //      - Set priority to URGENT if newStatus is CANCELLED, else NORMAL
        //      - Add metadata "orderId" -> orderId
        //      - Add metadata "deviceToken" -> "device-" + recipientAddress (for PUSH demo)
        //      - Add metadata "webhookUrl" -> "https://hooks.slack.com/demo" (for SLACK demo)
        //   2. Call channel.send(message)
        throw new UnsupportedOperationException("TODO: implement NotificationObserver.onOrderStatusChanged()");
    }
}

/**
 * Logs all order status changes to console (audit trail).
 * Does NOT send notifications — it just records events.
 */
class AuditLogObserver implements OrderEventListener {

    private final List<String> log = new ArrayList<>();

    @Override
    public void onOrderStatusChanged(String orderId, OrderStatus newStatus) {
        // TODO: Implement this
        //   1. Build a log entry string: "[<timestamp>] Order <orderId> -> <newStatus>"
        //   2. Add it to the log list.
        //   3. Print it to console.
        throw new UnsupportedOperationException("TODO: implement AuditLogObserver.onOrderStatusChanged()");
    }

    /** Returns all logged entries (for testing). */
    public List<String> getLog() { return Collections.unmodifiableList(log); }
}

// ============================================================
//  MAIN — wires everything together
//  When all TODOs are implemented, this should run without
//  throwing UnsupportedOperationException.
// ============================================================
public class Exercise02_DesignPatterns {

    public static void main(String[] args) {

        System.out.println("========== E-Commerce Notification System Demo ==========\n");

        // 1. Create an OrderTracker (subject / observable)
        OrderTracker tracker = new OrderTracker();

        // 2. Create an audit observer that records all events
        AuditLogObserver auditor = new AuditLogObserver();
        tracker.addListener(auditor);

        // 3. Create notification observers — one per customer using EMAIL initially
        NotificationChannel emailChannel = NotificationFactory.createChannel(ChannelType.EMAIL);
        NotificationObserver alice = new NotificationObserver(emailChannel, "alice@example.com");
        NotificationObserver bob   = new NotificationObserver(
                NotificationFactory.createChannel(ChannelType.SMS), "+1-555-0199");

        tracker.addListener(alice);
        tracker.addListener(bob);

        System.out.println("Registered listeners: " + tracker.listenerCount()); // should be 3

        // 4. Place and progress an order
        System.out.println("\n--- Placing order ORD-001 ---");
        tracker.placeOrder("ORD-001");

        System.out.println("\n--- Confirming order ORD-001 ---");
        tracker.updateStatus("ORD-001", OrderStatus.CONFIRMED);

        System.out.println("\n--- Shipping order ORD-001 ---");
        tracker.updateStatus("ORD-001", OrderStatus.SHIPPED);

        // 5. Alice switches to PUSH notifications mid-stream (Strategy swap)
        System.out.println("\n--- Alice switches to PUSH notifications ---");
        alice.setChannel(NotificationFactory.createChannel(ChannelType.PUSH));

        System.out.println("\n--- Delivering order ORD-001 ---");
        tracker.updateStatus("ORD-001", OrderStatus.DELIVERED);

        // 6. Bob unsubscribes — should not receive further events
        System.out.println("\n--- Bob unsubscribes ---");
        tracker.removeListener(bob);
        System.out.println("Registered listeners: " + tracker.listenerCount()); // should be 2

        // 7. Place a second order; Bob should NOT see events
        System.out.println("\n--- Placing & cancelling order ORD-002 ---");
        tracker.placeOrder("ORD-002");
        tracker.updateStatus("ORD-002", OrderStatus.CANCELLED);

        // 8. Show the audit trail
        System.out.println("\n========== Audit Log ==========");
        auditor.getLog().forEach(System.out::println);

        // 9. Builder demo — build a high-priority scheduled message
        System.out.println("\n========== Builder Demo ==========");
        NotificationMessage urgentMsg = new NotificationMessage.Builder(
                    "ceo@company.com",
                    "ALERT: High-value order cancelled",
                    "Order ORD-002 worth $9,999 was cancelled by the customer.")
                .priority(Priority.URGENT)
                .metadata("orderId", "ORD-002")
                .metadata("webhookUrl", "https://hooks.slack.com/ceo-alerts")
                .replyTo("support@company.com")
                .build();

        System.out.println("Built message: " + urgentMsg);
        NotificationFactory.createChannel(ChannelType.SLACK).send(urgentMsg);

        System.out.println("\n========== Demo Complete ==========");
    }
}
