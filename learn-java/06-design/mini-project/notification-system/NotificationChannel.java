/**
 * NotificationChannel.java — Strategy Pattern
 *
 * Each notification channel is a strategy for delivering a notification.
 * The NotificationService uses whichever channel strategy is appropriate
 * without knowing the details of delivery.
 *
 * STRATEGY PATTERN BENEFITS:
 *   - Add a new channel (WhatsApp) by adding a class — don't touch existing code (OCP)
 *   - Swap channels at runtime
 *   - Test NotificationService with a mock channel
 */

import java.time.LocalDateTime;

public class NotificationChannel {

    // =========================================================================
    // DeliveryResult — holds the outcome of a send attempt
    // =========================================================================

    public static class DeliveryResult {
        private final boolean success;
        private final String messageId;
        private final String errorMessage;
        private final LocalDateTime deliveredAt;

        private DeliveryResult(boolean success, String messageId, String errorMessage) {
            this.success = success;
            this.messageId = messageId;
            this.errorMessage = errorMessage;
            this.deliveredAt = success ? LocalDateTime.now() : null;
        }

        public static DeliveryResult success(String messageId) {
            return new DeliveryResult(true, messageId, null);
        }

        public static DeliveryResult failure(String reason) {
            return new DeliveryResult(false, null, reason);
        }

        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getErrorMessage() { return errorMessage; }
        public LocalDateTime getDeliveredAt() { return deliveredAt; }

        @Override
        public String toString() {
            return success
                ? "DeliveryResult{SUCCESS, msgId=" + messageId + "}"
                : "DeliveryResult{FAILED, reason=" + errorMessage + "}";
        }
    }

    // =========================================================================
    // Channel Interface — the Strategy
    // =========================================================================

    public interface Channel {
        DeliveryResult send(Notification notification);
        String getChannelName();
        boolean isAvailable();
    }

    // =========================================================================
    // EMAIL Channel
    // =========================================================================

    public static class EmailNotificationChannel implements Channel {

        private final String smtpServer;
        private final String fromAddress;

        public EmailNotificationChannel(String smtpServer, String fromAddress) {
            this.smtpServer = smtpServer;
            this.fromAddress = fromAddress;
        }

        @Override
        public DeliveryResult send(Notification notification) {
            // Validate email format
            if (!notification.getRecipient().contains("@")) {
                return DeliveryResult.failure("Invalid email address: " + notification.getRecipient());
            }

            // Simulate email delivery
            System.out.printf("  [EMAIL] Sending via %s%n", smtpServer);
            System.out.printf("          From: %s%n", fromAddress);
            System.out.printf("          To:   %s%n", notification.getRecipient());
            System.out.printf("          Subj: %s%n", notification.getSubject());
            System.out.printf("          Body: %s%n", notification.getBody().substring(0,
                Math.min(60, notification.getBody().length())) + "...");

            // Simulate network latency (removed Thread.sleep to keep demo fast)
            String messageId = "EMAIL-" + System.currentTimeMillis();
            return DeliveryResult.success(messageId);
        }

        @Override
        public String getChannelName() { return "EMAIL"; }

        @Override
        public boolean isAvailable() { return true; }
    }

    // =========================================================================
    // SMS Channel
    // =========================================================================

    public static class SMSNotificationChannel implements Channel {

        private static final int MAX_SMS_CHARS = 160;
        private final String smsProvider;

        public SMSNotificationChannel(String smsProvider) {
            this.smsProvider = smsProvider;
        }

        @Override
        public DeliveryResult send(Notification notification) {
            // Validate phone number format (basic check)
            String phone = notification.getRecipient();
            if (!phone.matches("[+]?[0-9\\-\\s]+") || phone.replaceAll("[^0-9]", "").length() < 7) {
                return DeliveryResult.failure("Invalid phone number: " + phone);
            }

            // SMS body must fit within 160 characters
            String body = notification.getBody();
            if (body.length() > MAX_SMS_CHARS) {
                System.out.printf("  [SMS] Warning: message truncated from %d to %d chars%n",
                    body.length(), MAX_SMS_CHARS);
                body = body.substring(0, MAX_SMS_CHARS - 3) + "...";
            }

            System.out.printf("  [SMS]  Via %s → %s%n", smsProvider, phone);
            System.out.printf("         Text: %s%n", body);

            String messageId = "SMS-" + System.currentTimeMillis();
            return DeliveryResult.success(messageId);
        }

        @Override
        public String getChannelName() { return "SMS"; }

        @Override
        public boolean isAvailable() { return true; }
    }

    // =========================================================================
    // PUSH Channel
    // =========================================================================

    public static class PushNotificationChannel implements Channel {

        private final String appId;

        public PushNotificationChannel(String appId) {
            this.appId = appId;
        }

        @Override
        public DeliveryResult send(Notification notification) {
            String deviceToken = notification.getMetadata().get("deviceToken");
            if (deviceToken == null || deviceToken.isBlank()) {
                return DeliveryResult.failure("No device token in metadata for recipient: "
                    + notification.getRecipient());
            }

            System.out.printf("  [PUSH] App: %s, Device: %s%n", appId, deviceToken.substring(0, 8) + "...");
            System.out.printf("         Title: %s%n", notification.getSubject());
            System.out.printf("         Body:  %s%n", notification.getBody().substring(0,
                Math.min(40, notification.getBody().length())) + "...");
            System.out.printf("         Priority: %s%n", notification.getPriority());

            String messageId = "PUSH-" + System.currentTimeMillis();
            return DeliveryResult.success(messageId);
        }

        @Override
        public String getChannelName() { return "PUSH"; }

        @Override
        public boolean isAvailable() { return true; }
    }

    // =========================================================================
    // SLACK Channel
    // =========================================================================

    public static class SlackNotificationChannel implements Channel {

        private final String webhookUrl;

        public SlackNotificationChannel(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        @Override
        public DeliveryResult send(Notification notification) {
            if (!webhookUrl.startsWith("https://hooks.slack.com/")) {
                return DeliveryResult.failure("Invalid Slack webhook URL");
            }

            String channel = notification.getMetadata().getOrDefault("slackChannel", "#general");
            System.out.printf("  [SLACK] Channel: %s%n", channel);
            System.out.printf("          *%s*%n", notification.getSubject());
            System.out.printf("          %s%n", notification.getBody());

            String messageId = "SLACK-" + System.currentTimeMillis();
            return DeliveryResult.success(messageId);
        }

        @Override
        public String getChannelName() { return "SLACK"; }

        @Override
        public boolean isAvailable() { return webhookUrl != null && !webhookUrl.isBlank(); }
    }
}
