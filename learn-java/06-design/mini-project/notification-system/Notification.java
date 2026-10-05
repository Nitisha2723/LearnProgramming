/**
 * Notification.java — Built with the Builder Pattern
 *
 * An immutable notification object. Once created, it cannot be modified.
 * The Builder pattern handles the complex creation with validation.
 */

import java.time.LocalDateTime;
import java.util.*;

public class Notification {

    // =========================================================================
    // Enums
    // =========================================================================

    public enum Channel { EMAIL, SMS, PUSH, SLACK }

    public enum Priority {
        LOW(1), NORMAL(2), HIGH(3), CRITICAL(4);
        private final int level;
        Priority(int level) { this.level = level; }
        public int getLevel() { return level; }
    }

    public enum Status { PENDING, SENT, FAILED, DELIVERED }

    // =========================================================================
    // Fields — all final: immutable after construction
    // =========================================================================

    private final String id;
    private final String recipient;
    private final String subject;
    private final String body;
    private final Channel channel;
    private final Priority priority;
    private final Map<String, String> metadata;
    private final LocalDateTime createdAt;
    private Status status;   // mutable — status changes as notification is processed

    // =========================================================================
    // Private constructor — only Builder can create Notification
    // =========================================================================

    private Notification(Builder builder) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.recipient = builder.recipient;
        this.subject = builder.subject;
        this.body = builder.body;
        this.channel = builder.channel;
        this.priority = builder.priority;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(builder.metadata));
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public String getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Channel getChannel() { return channel; }
    public Priority getPriority() { return priority; }
    public Map<String, String> getMetadata() { return metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }

    /** Status is allowed to change (lifecycle: PENDING → SENT → DELIVERED / FAILED) */
    public void setStatus(Status status) { this.status = status; }

    // =========================================================================
    // Builder
    // =========================================================================

    /**
     * The Builder for Notification.
     *
     * Why Builder? Notification has 5 required fields and several optional ones.
     * Telescoping constructors (N-argument constructors for each combination) would be a nightmare.
     * The Builder gives us:
     *   - Named parameters (clear what each field is)
     *   - Validation in build()
     *   - Optional fields with sensible defaults
     *   - Immutable result
     */
    public static class Builder {
        // Required fields — must be set before build()
        private String recipient;
        private String subject;
        private String body;
        private Channel channel;

        // Optional fields with defaults
        private Priority priority = Priority.NORMAL;
        private Map<String, String> metadata = new HashMap<>();

        /** Set the recipient (email address, phone number, or user ID) */
        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;  // return this for method chaining
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder channel(Channel channel) {
            this.channel = channel;
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /** Add a single metadata key-value pair */
        public Builder metadata(String key, String value) {
            this.metadata.put(key, value);
            return this;
        }

        /** Add all metadata from a map */
        public Builder metadata(Map<String, String> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        /**
         * Build the Notification, validating required fields.
         *
         * @throws IllegalStateException if required fields are missing or invalid
         */
        public Notification build() {
            List<String> errors = new ArrayList<>();

            if (recipient == null || recipient.isBlank()) errors.add("recipient is required");
            if (subject == null || subject.isBlank()) errors.add("subject is required");
            if (body == null || body.isBlank()) errors.add("body is required");
            if (channel == null) errors.add("channel is required");

            if (!errors.isEmpty()) {
                throw new IllegalStateException("Cannot build Notification: " + String.join(", ", errors));
            }

            return new Notification(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Notification[id=%s, channel=%s, priority=%s, status=%s, to=%s, subject='%s']",
            id, channel, priority, status, recipient, subject);
    }
}
