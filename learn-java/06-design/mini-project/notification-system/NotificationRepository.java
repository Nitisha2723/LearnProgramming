/**
 * NotificationRepository.java — Repository Pattern
 *
 * Abstracts the storage of notifications behind an interface.
 *
 * REPOSITORY PATTERN BENEFITS:
 *   - NotificationService doesn't know or care where notifications are stored
 *   - Tests can use InMemoryNotificationRepository (fast, no I/O)
 *   - Production can use a database repository (swapped in during setup)
 *   - Adding a new storage backend doesn't change any business logic
 */

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NotificationRepository {

    // =========================================================================
    // Repository Interface
    // =========================================================================

    public interface Repository {
        /** Save a new notification or update an existing one */
        void save(Notification notification);

        /** Find by unique ID */
        Optional<Notification> findById(String id);

        /** Find all notifications for a recipient */
        List<Notification> findByRecipient(String recipient);

        /** Find all notifications with a given status */
        List<Notification> findByStatus(Notification.Status status);

        /** Get all notifications */
        List<Notification> findAll();

        /** Total number of notifications stored */
        long count();

        /** Update the status of a notification */
        void updateStatus(String id, Notification.Status newStatus);
    }

    // =========================================================================
    // In-Memory Implementation — uses ConcurrentHashMap for thread safety
    // =========================================================================

    public static class InMemoryNotificationRepository implements Repository {

        // ConcurrentHashMap is thread-safe — multiple threads can read/write
        private final Map<String, Notification> store = new ConcurrentHashMap<>();

        @Override
        public void save(Notification notification) {
            store.put(notification.getId(), notification);
        }

        @Override
        public Optional<Notification> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Notification> findByRecipient(String recipient) {
            return store.values().stream()
                .filter(n -> recipient.equals(n.getRecipient()))
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
        }

        @Override
        public List<Notification> findByStatus(Notification.Status status) {
            return store.values().stream()
                .filter(n -> n.getStatus() == status)
                .collect(Collectors.toList());
        }

        @Override
        public List<Notification> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public void updateStatus(String id, Notification.Status newStatus) {
            Notification notification = store.get(id);
            if (notification != null) {
                notification.setStatus(newStatus);
            }
        }

        /** For testing/demo: clear all stored notifications */
        public void clear() {
            store.clear();
        }
    }
}
