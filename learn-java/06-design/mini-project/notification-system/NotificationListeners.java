/**
 * NotificationListeners.java — Observer Implementations
 *
 * Concrete observers that react to notification events.
 * They are completely decoupled from NotificationService
 * — neither knows about the other directly.
 */

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.*;

public class NotificationListeners {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    // =========================================================================
    // Audit Logger — records every notification attempt
    // =========================================================================

    public static class AuditLogger implements NotificationService.NotificationListener {

        private final List<String> log = new ArrayList<>();

        @Override
        public void onNotificationSent(Notification n, NotificationChannel.DeliveryResult result) {
            String entry = String.format("[AUDIT] %s SENT    | id=%-8s | channel=%-5s | priority=%-8s | to=%s | msgId=%s",
                java.time.LocalDateTime.now().format(FORMATTER),
                n.getId(), n.getChannel(), n.getPriority(),
                n.getRecipient(), result.getMessageId());
            log.add(entry);
            System.out.println(entry);
        }

        @Override
        public void onNotificationFailed(Notification n, String reason) {
            String entry = String.format("[AUDIT] %s FAILED  | id=%-8s | channel=%-5s | priority=%-8s | to=%s | reason=%s",
                java.time.LocalDateTime.now().format(FORMATTER),
                n.getId(), n.getChannel(), n.getPriority(),
                n.getRecipient(), reason);
            log.add(entry);
            System.out.println(entry);
        }

        public void printFullLog() {
            System.out.println("\n=== COMPLETE AUDIT LOG ===");
            log.forEach(System.out::println);
            System.out.println("=========================\n");
        }

        public List<String> getLog() { return Collections.unmodifiableList(log); }
    }

    // =========================================================================
    // Metrics Collector — tracks counts and statistics
    // =========================================================================

    public static class MetricsCollector implements NotificationService.NotificationListener {

        private final AtomicLong totalSent = new AtomicLong(0);
        private final AtomicLong totalFailed = new AtomicLong(0);
        private final Map<Notification.Channel, AtomicLong> sentByChannel = new HashMap<>();
        private final Map<Notification.Channel, AtomicLong> failedByChannel = new HashMap<>();

        public MetricsCollector() {
            // Initialize counters for each channel
            for (Notification.Channel c : Notification.Channel.values()) {
                sentByChannel.put(c, new AtomicLong(0));
                failedByChannel.put(c, new AtomicLong(0));
            }
        }

        @Override
        public void onNotificationSent(Notification n, NotificationChannel.DeliveryResult result) {
            totalSent.incrementAndGet();
            sentByChannel.get(n.getChannel()).incrementAndGet();
        }

        @Override
        public void onNotificationFailed(Notification n, String reason) {
            totalFailed.incrementAndGet();
            failedByChannel.get(n.getChannel()).incrementAndGet();
        }

        public void printReport() {
            long total = totalSent.get() + totalFailed.get();
            System.out.println("\n=== METRICS REPORT ===");
            System.out.println("Total attempts: " + total);
            System.out.println("Sent:           " + totalSent.get());
            System.out.println("Failed:         " + totalFailed.get());
            if (total > 0) {
                System.out.printf("Success rate:   %.1f%%%n", (totalSent.get() * 100.0) / total);
            }
            System.out.println("\nBy channel:");
            for (Notification.Channel c : Notification.Channel.values()) {
                long sent = sentByChannel.get(c).get();
                long failed = failedByChannel.get(c).get();
                if (sent + failed > 0) {
                    System.out.printf("  %-6s: %d sent, %d failed%n", c, sent, failed);
                }
            }
            System.out.println("======================\n");
        }

        public long getTotalSent() { return totalSent.get(); }
        public long getTotalFailed() { return totalFailed.get(); }
    }

    // =========================================================================
    // Alert Monitor — raises alerts for critical failures
    // =========================================================================

    public static class AlertMonitor implements NotificationService.NotificationListener {

        private static final double FAILURE_RATE_THRESHOLD = 0.10; // 10%
        private int totalAttempts = 0;
        private int failures = 0;

        @Override
        public void onNotificationSent(Notification n, NotificationChannel.DeliveryResult result) {
            totalAttempts++;
            checkFailureRate();

            // Alert on critical priority always
            if (n.getPriority() == Notification.Priority.CRITICAL) {
                System.out.println("[ALERT] CRITICAL notification delivered: " + n.getSubject());
            }
        }

        @Override
        public void onNotificationFailed(Notification n, String reason) {
            totalAttempts++;
            failures++;

            // Always alert on critical failures
            if (n.getPriority() == Notification.Priority.CRITICAL) {
                System.out.println("[ALERT] !!! CRITICAL NOTIFICATION FAILED !!!");
                System.out.println("[ALERT]     Recipient: " + n.getRecipient());
                System.out.println("[ALERT]     Subject: " + n.getSubject());
                System.out.println("[ALERT]     Reason: " + reason);
            }

            checkFailureRate();
        }

        private void checkFailureRate() {
            if (totalAttempts >= 5) {  // don't alert on first few attempts
                double failureRate = (double) failures / totalAttempts;
                if (failureRate > FAILURE_RATE_THRESHOLD) {
                    System.out.printf("[ALERT] High failure rate detected: %.1f%% (%d/%d)%n",
                        failureRate * 100, failures, totalAttempts);
                }
            }
        }
    }
}
