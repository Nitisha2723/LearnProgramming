/**
 * UserPreferenceManager.java
 *
 * Manages user notification preferences.
 * In a real system, this would integrate with the notification service
 * to automatically route notifications to the user's preferred channel.
 */

import java.util.*;

public class UserPreferenceManager {

    // =========================================================================
    // UserPreferences — value object
    // =========================================================================

    public static class UserPreferences {
        private final String userId;
        private Notification.Channel preferredChannel;
        private final Set<Notification.Channel> optedOutChannels;
        private String timezone;
        private String language;

        public UserPreferences(String userId, Notification.Channel preferredChannel) {
            this.userId = Objects.requireNonNull(userId);
            this.preferredChannel = Objects.requireNonNull(preferredChannel);
            this.optedOutChannels = new HashSet<>();
            this.timezone = "UTC";
            this.language = "en";
        }

        public String getUserId() { return userId; }
        public Notification.Channel getPreferredChannel() { return preferredChannel; }
        public void setPreferredChannel(Notification.Channel channel) { this.preferredChannel = channel; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public void optOut(Notification.Channel channel) { optedOutChannels.add(channel); }
        public void optIn(Notification.Channel channel) { optedOutChannels.remove(channel); }
        public boolean isOptedOut(Notification.Channel channel) { return optedOutChannels.contains(channel); }

        @Override
        public String toString() {
            return String.format("UserPreferences{userId=%s, preferred=%s, optedOut=%s, tz=%s}",
                userId, preferredChannel, optedOutChannels, timezone);
        }
    }

    // =========================================================================
    // Manager
    // =========================================================================

    private final Map<String, UserPreferences> preferences = new HashMap<>();

    public void savePreferences(UserPreferences prefs) {
        preferences.put(prefs.getUserId(), prefs);
    }

    public Optional<UserPreferences> getPreferences(String userId) {
        return Optional.ofNullable(preferences.get(userId));
    }

    /**
     * Get the effective channel for a user, respecting opt-outs.
     * Falls back to EMAIL if preferred channel is opted out.
     */
    public Notification.Channel getEffectiveChannel(String userId, Notification.Channel requestedChannel) {
        UserPreferences prefs = preferences.get(userId);
        if (prefs == null) return requestedChannel;

        // If the requested channel is opted out, use preferred
        if (prefs.isOptedOut(requestedChannel)) {
            Notification.Channel preferred = prefs.getPreferredChannel();
            if (!prefs.isOptedOut(preferred)) {
                return preferred;
            }
            // Fall back to EMAIL as a last resort
            return Notification.Channel.EMAIL;
        }

        return requestedChannel;
    }

    public void printAllPreferences() {
        System.out.println("User Preferences:");
        preferences.values().forEach(p -> System.out.println("  " + p));
    }
}
