/**
 * NotificationService.java — Observer Pattern + Dependency Inversion Principle
 *
 * The core service that orchestrates notification sending.
 *
 * PATTERNS USED:
 *
 * OBSERVER: NotificationListenerInterface observers are notified when
 * notifications are sent or fail. NotificationService doesn't know who
 * is listening — it just broadcasts events.
 *
 * STRATEGY: NotificationService holds a map of channel strategies.
 * It delegates to the right strategy without knowing delivery details.
 *
 * DIP: NotificationService depends on the Repository interface, not
 * a concrete class. This makes it testable without a real database.
 */

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationService {

    // =========================================================================
    // Observer Interface
    // =========================================================================

    public interface NotificationListener {
        void onNotificationSent(Notification notification, NotificationChannel.DeliveryResult result);
        void onNotificationFailed(Notification notification, String reason);
    }

    // =========================================================================
    // NotificationService
    // =========================================================================

    // Dependencies — all interfaces, not concrete classes (DIP)
    private final NotificationRepository.Repository repository;

    // Strategy map: channel type → channel implementation
    private final Map<Notification.Channel, NotificationChannel.Channel> channels = new HashMap<>();

    // Observer list — CopyOnWriteArrayList for thread-safe iteration while adding/removing
    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Constructor injection — dependencies are provided from outside.
     * This means we can test with mock/in-memory implementations.
     */
    public NotificationService(NotificationRepository.Repository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    // =========================================================================
    // Channel Management
    // =========================================================================

    /** Register a delivery channel for a notification channel type */
    public void registerChannel(Notification.Channel channelType,
                                NotificationChannel.Channel channelImpl) {
        channels.put(channelType, channelImpl);
    }

    // =========================================================================
    // Observer Management
    // =========================================================================

    public void addListener(NotificationListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    // =========================================================================
    // Core Send Logic
    // =========================================================================

    /**
     * Send a single notification.
     *
     * Flow:
     *   1. Look up the channel strategy
     *   2. Save to repository (PENDING)
     *   3. Send via channel
     *   4. Update status in repository
     *   5. Notify all observers (success or failure)
     *
     * @return DeliveryResult indicating success or failure
     */
    public NotificationChannel.DeliveryResult send(Notification notification) {
        Objects.requireNonNull(notification, "notification cannot be null");

        // 1. Check channel is registered
        NotificationChannel.Channel channel = channels.get(notification.getChannel());
        if (channel == null) {
            String reason = "No channel registered for: " + notification.getChannel();
            notification.setStatus(Notification.Status.FAILED);
            repository.save(notification);
            notifyFailure(notification, reason);
            return NotificationChannel.DeliveryResult.failure(reason);
        }

        if (!channel.isAvailable()) {
            String reason = channel.getChannelName() + " channel is not available";
            notification.setStatus(Notification.Status.FAILED);
            repository.save(notification);
            notifyFailure(notification, reason);
            return NotificationChannel.DeliveryResult.failure(reason);
        }

        // 2. Save as PENDING
        repository.save(notification);

        // 3. Attempt delivery
        NotificationChannel.DeliveryResult result;
        try {
            result = channel.send(notification);
        } catch (Exception e) {
            result = NotificationChannel.DeliveryResult.failure("Channel threw exception: " + e.getMessage());
        }

        // 4. Update status based on result
        Notification.Status newStatus = result.isSuccess()
            ? Notification.Status.SENT
            : Notification.Status.FAILED;
        notification.setStatus(newStatus);
        repository.updateStatus(notification.getId(), newStatus);

        // 5. Notify observers
        if (result.isSuccess()) {
            notifySuccess(notification, result);
        } else {
            notifyFailure(notification, result.getErrorMessage());
        }

        return result;
    }

    /** Send multiple notifications */
    public List<NotificationChannel.DeliveryResult> sendBatch(List<Notification> notifications) {
        List<NotificationChannel.DeliveryResult> results = new ArrayList<>();
        for (Notification n : notifications) {
            results.add(send(n));
        }
        return results;
    }

    // =========================================================================
    // Queries
    // =========================================================================

    public List<Notification> getSentNotifications() {
        return repository.findByStatus(Notification.Status.SENT);
    }

    public List<Notification> getFailedNotifications() {
        return repository.findByStatus(Notification.Status.FAILED);
    }

    public List<Notification> getNotificationsFor(String recipient) {
        return repository.findByRecipient(recipient);
    }

    /** Print a stats summary */
    public void printStats() {
        long total = repository.count();
        long sent = repository.findByStatus(Notification.Status.SENT).size();
        long failed = repository.findByStatus(Notification.Status.FAILED).size();
        long pending = repository.findByStatus(Notification.Status.PENDING).size();

        System.out.println("\n=== NOTIFICATION STATS ===");
        System.out.println("Total:   " + total);
        System.out.println("Sent:    " + sent);
        System.out.println("Failed:  " + failed);
        System.out.println("Pending: " + pending);
        if (total > 0) {
            System.out.printf("Success rate: %.1f%%%n", (sent * 100.0) / total);
        }
        System.out.println("==========================\n");
    }

    // =========================================================================
    // Observer notification helpers
    // =========================================================================

    private void notifySuccess(Notification notification, NotificationChannel.DeliveryResult result) {
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotificationSent(notification, result);
            } catch (Exception e) {
                // Observers must not crash the service
                System.err.println("Listener threw exception: " + e.getMessage());
            }
        }
    }

    private void notifyFailure(Notification notification, String reason) {
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotificationFailed(notification, reason);
            } catch (Exception e) {
                System.err.println("Listener threw exception: " + e.getMessage());
            }
        }
    }
}
