import java.util.*;
import java.util.stream.Collectors;

/**
 * OptionalsDemo.java
 *
 * Demonstrates how to use Optional to write null-safe code:
 * - Creating Optionals
 * - Safe access patterns
 * - Chaining with map, flatMap, filter
 * - orElse, orElseGet, orElseThrow
 * - ifPresent, ifPresentOrElse
 * - Avoiding NPEs in real code
 */
public class OptionalsDemo {

    // =========================================================
    // Domain Model
    // =========================================================

    record Address(String street, String city, String country) {}

    static class User {
        private final int id;
        private final String name;
        private final String email;
        private final String phone;       // nullable
        private final Address address;    // nullable

        User(int id, String name, String email, String phone, Address address) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }

        int getId()            { return id; }
        String getName()       { return name; }
        String getEmail()      { return email; }

        // Returns Optional — signals that phone might not be set
        Optional<String> getPhone()       { return Optional.ofNullable(phone); }
        Optional<Address> getAddress()    { return Optional.ofNullable(address); }

        @Override public String toString() {
            return "User{id=" + id + ", name='" + name + "'}";
        }
    }

    // Simulated repository
    static class UserRepository {
        private final Map<Integer, User> store = new HashMap<>();

        UserRepository() {
            store.put(1, new User(1, "Alice", "alice@example.com",
                "+49 30 12345",
                new Address("Unter den Linden 1", "Berlin", "Germany")));
            store.put(2, new User(2, "Bob", "bob@example.com",
                null,  // No phone
                new Address("Musterstraße 5", "Munich", "Germany")));
            store.put(3, new User(3, "Charlie", "charlie@example.com",
                null,  // No phone
                null   // No address
            ));
        }

        // Returns Optional — explicitly says "user might not exist"
        Optional<User> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }

        Optional<User> findByEmail(String email) {
            return store.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
        }
    }

    // =========================================================
    // 1. Creating Optionals
    // =========================================================

    static void creatingOptionals() {
        System.out.println("=== Creating Optionals ===\n");

        Optional<String> withValue = Optional.of("Hello");
        Optional<String> empty     = Optional.empty();
        Optional<String> nullable  = Optional.ofNullable(null);  // Empty
        Optional<String> nullable2 = Optional.ofNullable("World"); // Present

        System.out.println("withValue: " + withValue);
        System.out.println("empty: " + empty);
        System.out.println("nullable (null): " + nullable);
        System.out.println("nullable2 (World): " + nullable2);

        System.out.println("\nisPresent: " + withValue.isPresent());
        System.out.println("isEmpty: " + empty.isEmpty());  // Java 11+

        // Don't do this — it's just a null check
        if (withValue.isPresent()) {
            System.out.println("Anti-pattern get(): " + withValue.get());
        }
    }

    // =========================================================
    // 2. Safe Access Patterns
    // =========================================================

    static void safeAccessDemo() {
        System.out.println("\n=== Safe Access Patterns ===\n");

        UserRepository repo = new UserRepository();

        // orElse: provide a default value
        User user = repo.findById(99).orElse(null);
        System.out.println("Non-existent user (orElse null): " + user);

        String name = repo.findById(99)
            .map(User::getName)
            .orElse("Guest");
        System.out.println("Name with default: " + name);

        // orElseGet: lazy evaluation (only called if empty)
        User defaultUser = repo.findById(99)
            .orElseGet(() -> new User(0, "Guest", "guest@example.com", null, null));
        System.out.println("Default user: " + defaultUser);

        // orElseThrow: require the value
        try {
            User required = repo.findById(99).orElseThrow(
                () -> new NoSuchElementException("User 99 not found")
            );
        } catch (NoSuchElementException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // ifPresent: only do something if value exists
        repo.findById(1).ifPresent(u ->
            System.out.println("\nFound user: " + u.getName())
        );

        // ifPresentOrElse (Java 9+)
        repo.findById(99).ifPresentOrElse(
            u -> System.out.println("Found: " + u.getName()),
            () -> System.out.println("User 99 not found (ifPresentOrElse)")
        );
    }

    // =========================================================
    // 3. Chaining: map, flatMap, filter
    // =========================================================

    static void chainingDemo() {
        System.out.println("\n=== Chaining Operations ===\n");

        UserRepository repo = new UserRepository();

        // map: transform the value if present
        Optional<String> email = repo.findById(1)
            .map(User::getEmail)
            .map(String::toUpperCase);
        System.out.println("Alice's email (uppercased): " + email.orElse("N/A"));

        // Chain multiple maps
        // Get user's city (User → Optional<Address> → String)
        // Note: User.getAddress() returns Optional<Address>, so we need flatMap
        Optional<String> city = repo.findById(1)
            .flatMap(User::getAddress)     // Optional<User> → Optional<Address>
            .map(Address::city);           // Optional<Address> → Optional<String>
        System.out.println("Alice's city: " + city.orElse("Unknown"));

        // User with no address
        Optional<String> charlieCity = repo.findById(3)
            .flatMap(User::getAddress)
            .map(Address::city);
        System.out.println("Charlie's city: " + charlieCity.orElse("Unknown"));

        // filter: make empty if condition not met
        Optional<User> germanUser = repo.findById(1)
            .filter(u -> u.getAddress()
                .map(Address::country)
                .map("Germany"::equals)
                .orElse(false));
        System.out.println("Alice is German: " + germanUser.isPresent());

        // Phone numbers across all users
        System.out.println("\nPhone numbers (only users who have one):");
        for (int id = 1; id <= 3; id++) {
            String phone = repo.findById(id)
                .flatMap(User::getPhone)
                .orElse("No phone");
            repo.findById(id)
                .ifPresent(u -> System.out.printf("  %-8s %s%n", u.getName() + ":", phone));
        }
        // Note: The above has a scoping issue — fixing with proper closure:
        List.of(1, 2, 3).forEach(id -> {
            String phoneLine = repo.findById(id)
                .map(u -> u.getName() + ": " +
                    u.getPhone().orElse("No phone"))
                .orElse("User " + id + " not found");
            System.out.println("  " + phoneLine);
        });
    }

    // =========================================================
    // 4. Optional in Stream Pipelines
    // =========================================================

    static void optionalInStreams() {
        System.out.println("\n=== Optionals in Streams ===\n");

        UserRepository repo = new UserRepository();
        List<Integer> ids = List.of(1, 2, 3, 99, 100);  // Some invalid IDs

        // Method 1: filter(Optional::isPresent).map(Optional::get) — verbose
        List<User> found1 = ids.stream()
            .map(repo::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        // Method 2: flatMap(Optional::stream) — elegant (Java 9+)
        List<User> found2 = ids.stream()
            .map(repo::findById)
            .flatMap(Optional::stream)  // Empty optionals are filtered out
            .collect(Collectors.toList());

        System.out.println("Found users: " + found2.stream()
            .map(User::getName)
            .collect(Collectors.joining(", ")));

        // Get all cities where we have users
        List<String> cities = ids.stream()
            .map(repo::findById)
            .flatMap(Optional::stream)
            .map(User::getAddress)
            .flatMap(Optional::stream)
            .map(Address::city)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Cities with users: " + cities);
    }

    // =========================================================
    // 5. Optional vs Null — the Difference in Practice
    // =========================================================

    static void optionalVsNull() {
        System.out.println("\n=== Optional vs Null ===\n");

        // Without Optional: every call site must remember to null-check
        // This is what old Java code looked like:
        UserRepository repo = new UserRepository();

        // Forgetting a null check = NPE at runtime
        // User user = repo.findById(99).orElse(null);  // Forces null handling
        // String city = user.getAddress().city;  // NPE if user is null!

        // With Optional: the type system reminds you
        String city = repo.findById(99)     // Optional<User>
            .flatMap(User::getAddress)       // Optional<Address>
            .map(Address::city)              // Optional<String>
            .orElse("City unknown");         // String
        // Impossible to forget the "not found" case — the API forces you to handle it

        System.out.println("Safe city access: " + city);

        // Anti-patterns to AVOID with Optional:
        Optional<User> opt = repo.findById(1);

        // ❌ Anti-pattern 1: isPresent() + get() (just a verbose null check)
        if (opt.isPresent()) {
            System.out.println("Anti-pattern: " + opt.get().getName());
        }

        // ✓ Better: use map/ifPresent
        opt.map(User::getName).ifPresent(n -> System.out.println("Better: " + n));

        // ❌ Anti-pattern 2: Optional as method parameter
        // void process(Optional<String> name) — don't do this!
        // Better: have two overloads, or use @Nullable annotation

        // ❌ Anti-pattern 3: Optional in collections
        // List<Optional<User>> — just use List<User> and filter nulls out beforehand
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        creatingOptionals();
        safeAccessDemo();
        chainingDemo();
        optionalInStreams();
        optionalVsNull();
    }
}
