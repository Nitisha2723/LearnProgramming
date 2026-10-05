/**
 * NotificationSystemDemo.java
 *
 * Demonstrates the complete notification system.
 *
 * Run this file to see all patterns working together:
 *   - Builder: Notification is created with builder syntax
 *   - Factory: Common notifications created via factory methods
 *   - Strategy: Different channels deliver notifications differently
 *   - Observer: AuditLogger, MetricsCollector, AlertMonitor react to events
 *   - Repository: All notifications are stored and queryable
 *   - DIP: NotificationService depends on interfaces, not implementations
 */

import java.util.*;

public class NotificationSystemDemo {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║     E-Commerce Notification System Demo        ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();

        // =====================================================================
        // SYSTEM SETUP
        // Wire everything together. In a real app, Spring does this for you.
        // =====================================================================

        System.out.println("▶ Setting up notification system...");

        // Repository — where notifications are stored
        NotificationRepository.InMemoryNotificationRepository repo =
            new NotificationRepository.InMemoryNotificationRepository();

        // Service — core orchestrator (depends on interface, not InMemoryRepository)
        NotificationService service = new NotificationService(repo);

        // Register channel strategies
        service.registerChannel(Notification.Channel.EMAIL,
            new NotificationChannel.EmailNotificationChannel("smtp.example.com", "noreply@shop.example.com"));
        service.registerChannel(Notification.Channel.SMS,
            new NotificationChannel.SMSNotificationChannel("Twilio"));
        service.registerChannel(Notification.Channel.PUSH,
            new NotificationChannel.PushNotificationChannel("com.example.shop"));
        service.registerChannel(Notification.Channel.SLACK,
            new NotificationChannel.SlackNotificationChannel("https://hooks.slack.com/services/T00/B00/xxx"));

        // Register observers
        NotificationListeners.AuditLogger auditLogger = new NotificationListeners.AuditLogger();
        NotificationListeners.MetricsCollector metrics = new NotificationListeners.MetricsCollector();
        NotificationListeners.AlertMonitor alertMonitor = new NotificationListeners.AlertMonitor();

        service.addListener(auditLogger);
        service.addListener(metrics);
        service.addListener(alertMonitor);

        System.out.println("✓ System ready: 4 channels, 3 observers\n");

        // =====================================================================
        // SCENARIO 1: New Order Placed — Email Confirmation
        // Demonstrates: Factory (creates notification), Email channel delivery
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SCENARIO 1: Order Confirmation (Email)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Notification orderConfirmation = NotificationFactory.createOrderConfirmation(
            "alice@example.com", "ORD-20241015-001", 149.99);

        NotificationChannel.DeliveryResult result1 = service.send(orderConfirmation);
        System.out.println("Result: " + result1);
        System.out.println();

        // =====================================================================
        // SCENARIO 2: Bulk Promotional Campaign
        // Demonstrates: sendBatch(), Factory, Low-priority notifications
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SCENARIO 2: Bulk Promotional Campaign");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        List<Notification> promos = Arrays.asList(
            NotificationFactory.createPromotion("alice@example.com", "SAVE20", 20.0),
            NotificationFactory.createPromotion("bob@example.com", "SAVE20", 20.0),
            NotificationFactory.createPromotion("charlie@example.com", "SAVE20", 20.0),
            NotificationFactory.createPromotion("diana@example.com", "SAVE20", 20.0),
            NotificationFactory.createPromotion("evan@example.com", "SAVE20", 20.0)
        );

        List<NotificationChannel.DeliveryResult> promoResults = service.sendBatch(promos);
        long promoSuccess = promoResults.stream().filter(NotificationChannel.DeliveryResult::isSuccess).count();
        System.out.println("Sent " + promoSuccess + "/" + promoResults.size() + " promotional emails");
        System.out.println();

        // =====================================================================
        // SCENARIO 3: Critical — Password Reset (High Priority)
        // Demonstrates: Priority system, CRITICAL notifications get extra logging
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SCENARIO 3: Password Reset (High Priority)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        Notification passwordReset = NotificationFactory.createPasswordReset(
            "alice@example.com", "RESET-abc123xyz");
        service.send(passwordReset);
        System.out.println();

        // Critical security alert
        Notification securityAlert = NotificationFactory.createSecurityAlert(
            "alice@example.com",
            "Your account was logged in from a new device: Windows 11, New York, USA");
        service.send(securityAlert);
        System.out.println();

        // =====================================================================
        // SCENARIO 4: Shipping Updates — SMS
        // Demonstrates: SMS channel, metadata usage, channel-specific validation
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SCENARIO 4: Shipping Updates (SMS)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        List<Notification> shippingAlerts = Arrays.asList(
            NotificationFactory.createShippingAlert("+1-555-0100", "ORD-001", "UPS-7X9K2"),
            NotificationFactory.createShippingAlert("+44-20-7946-0958", "ORD-002", "FDX-8L3M1"),
            NotificationFactory.createShippingAlert("+1-555-0200", "ORD-003", "DHL-9N4P7")
        );
        service.sendBatch(shippingAlerts);
        System.out.println();

        // =====================================================================
        // SCENARIO 5: Push Notification (device token required in metadata)
        // Demonstrates: Builder pattern, metadata, validation failure handling
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SCENARIO 5: Push Notifications");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Successful push (has device token)
        Notification pushWithToken = new Notification.Builder()
            .recipient("alice")
            .channel(Notification.Channel.PUSH)
            .priority(Notification.Priority.HIGH)
            .subject("Order on its way!")
            .body("Your order is out for delivery today!")
            .metadata("deviceToken", "abc123def456ghi789jkl012mno345pqr678")
            .build();
        service.send(pushWithToken);

        // Failed push (missing device token — validation catches it)
        Notification pushWithoutToken = new Notification.Builder()
            .recipient("bob")
            .channel(Notification.Channel.PUSH)
            .priority(Notification.Priority.NORMAL)
            .subject("New message!")
            .body("You have a new message.")
            .build();  // No device token in metadata!
        service.send(pushWithoutToken);
        System.out.println();

        // =====================================================================
        // SCENARIO 6: Failure Scenario — invalid email address
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  SCENARIO 6: Error Handling");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Invalid email — will fail validation in email channel
        Notification badEmail = new Notification.Builder()
            .recipient("not-an-email-address")
            .channel(Notification.Channel.EMAIL)
            .subject("This will fail")
            .body("Bad recipient")
            .build();
        NotificationChannel.DeliveryResult failResult = service.send(badEmail);
        System.out.println("Expected failure: " + failResult);
        System.out.println();

        // =====================================================================
        // FINAL REPORT
        // =====================================================================

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  FINAL REPORTS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        service.printStats();
        metrics.printReport();

        // Show notifications for a specific user
        System.out.println("Notifications for alice@example.com:");
        service.getNotificationsFor("alice@example.com")
            .forEach(n -> System.out.println("  " + n));

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  Demo complete. Patterns demonstrated:");
        System.out.println("  ✓ Builder   — Notification.Builder");
        System.out.println("  ✓ Factory   — NotificationFactory");
        System.out.println("  ✓ Strategy  — Email/SMS/Push/Slack channels");
        System.out.println("  ✓ Observer  — AuditLogger, MetricsCollector, AlertMonitor");
        System.out.println("  ✓ Repository — InMemoryNotificationRepository");
        System.out.println("  ✓ DIP       — Service depends on interfaces");
        System.out.println("  ✓ SRP       — Each class has one job");
        System.out.println("  ✓ OCP       — Add channel without modifying service");
        System.out.println("═══════════════════════════════════════════════");
    }
}
