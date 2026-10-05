import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * INTRODUCTION TO MOCKITO — Test Doubles in Java
 *
 * Why mock?
 * When testing OrderService or UserService, we don't want to hit a real database,
 * send real emails, or call real external APIs. That would make our tests:
 *   - Slow (network/disk I/O)
 *   - Fragile (fail if DB is down)
 *   - Side-effecting (create real data)
 *
 * Mocks let us SIMULATE dependencies and verify interactions in memory.
 *
 * Setup: Add to pom.xml:
 *   <dependency>
 *     <groupId>org.mockito</groupId>
 *     <artifactId>mockito-junit-jupiter</artifactId>
 *     <version>5.8.0</version>
 *     <scope>test</scope>
 *   </dependency>
 *
 * GLOSSARY
 * --------
 * Mock     — a fake object that records calls; returns empty/null by default
 * Stub     — telling a mock what to return: when(...).thenReturn(...)
 * Verify   — checking that a mock method was (or was not) called
 * Spy      — wraps a REAL object; calls real methods unless you stub specific ones
 * Captor   — captures the argument that was passed to a mock method
 */
@ExtendWith(MockitoExtension.class)  // Integrates Mockito with JUnit 5
@DisplayName("Mockito Introduction - Mocking Fundamentals")
class MockitoIntroTest {

    // =========================================================================
    // SIMPLE DOMAIN CLASSES (defined inline so this file is self-contained)
    // In a real project these would be in separate files.
    // =========================================================================

    /** Represents a user in our system. */
    record User(String id, String name, String email) {}

    /** Repository interface — abstracts database access. */
    interface UserRepository {
        Optional<User> findById(String id);
        List<User> findAll();
        void save(User user);
        boolean existsByEmail(String email);
        void deleteById(String id);
    }

    /** Email service interface — abstracts sending emails. */
    interface EmailService {
        void sendWelcomeEmail(String email, String name);
        void sendDeletionNotice(String email);
    }

    /** The class under test — depends on UserRepository and EmailService. */
    static class UserService {
        private final UserRepository userRepository;
        private final EmailService emailService;

        UserService(UserRepository userRepository, EmailService emailService) {
            this.userRepository = userRepository;
            this.emailService = emailService;
        }

        User getUserById(String id) {
            return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        }

        User registerUser(String name, String email) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalStateException("Email already registered: " + email);
            }
            var user = new User("generated-id", name, email);
            userRepository.save(user);
            emailService.sendWelcomeEmail(email, name);
            return user;
        }

        List<User> getAllUsers() {
            return userRepository.findAll();
        }

        void deleteUser(String id) {
            var user = getUserById(id);  // throws if not found
            userRepository.deleteById(id);
            emailService.sendDeletionNotice(user.email());
        }
    }

    // =========================================================================
    // MOCKITO ANNOTATIONS
    //
    // @Mock — creates a mock (fake) implementation of the interface.
    //         All methods return empty/null/zero by default.
    // @InjectMocks — creates the class under test and injects @Mock fields
    //                into its constructor automatically.
    // =========================================================================

    @Mock
    UserRepository userRepository;   // Mockito creates a fake UserRepository

    @Mock
    EmailService emailService;        // Mockito creates a fake EmailService

    @InjectMocks
    UserService userService;          // Mockito creates UserService with the mocks above

    // =========================================================================
    // 1. BASIC STUBBING: when().thenReturn()
    // =========================================================================

    /**
     * CONCEPT: Stubbing tells a mock what to return when a specific method is called.
     * Use when(mock.method(args)).thenReturn(value) to set up a stub.
     *
     * Without stubbing, mock methods return:
     *   - null for objects
     *   - 0 for numbers
     *   - false for booleans
     *   - empty collections for List/Set/Map
     */
    @Test
    @DisplayName("when().thenReturn() — stub a mock to return a specific value")
    void stubbingWithThenReturn() {
        // Arrange — set up the mock to return a specific User when findById is called
        var alice = new User("1", "Alice", "alice@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(alice));

        // Act — call the service method, which internally calls userRepository.findById()
        User result = userService.getUserById("1");

        // Assert — the service returned what the repository mock provided
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
    }

    /**
     * CONCEPT: Different stubs for different arguments.
     * You can stub the same method multiple times with different arguments.
     */
    @Test
    @DisplayName("Different arguments can return different stubbed values")
    void stubbingWithDifferentArguments() {
        // Arrange
        var alice = new User("1", "Alice", "alice@example.com");
        var bob   = new User("2", "Bob",   "bob@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(alice));
        when(userRepository.findById("2")).thenReturn(Optional.of(bob));

        // Act
        User user1 = userService.getUserById("1");
        User user2 = userService.getUserById("2");

        // Assert
        assertEquals("Alice", user1.name());
        assertEquals("Bob",   user2.name());
    }

    /**
     * CONCEPT: Stubbing a method that returns a collection.
     */
    @Test
    @DisplayName("Stub a method returning a List")
    void stubbingAListReturn() {
        // Arrange
        var users = Arrays.asList(
            new User("1", "Alice", "alice@example.com"),
            new User("2", "Bob",   "bob@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).name());
    }

    // =========================================================================
    // 2. STUBBING EXCEPTIONS: when().thenThrow()
    // =========================================================================

    /**
     * CONCEPT: You can make a mock method throw an exception to test error handling.
     * This simulates scenarios like "database is down" or "user not found".
     */
    @Test
    @DisplayName("when().thenThrow() — simulate errors from dependencies")
    void stubbingWithThenThrow() {
        // Arrange — repository returns empty for ID "999" (user does not exist)
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert — the service should throw when user is not found
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getUserById("999")
        );
        assertTrue(ex.getMessage().contains("999"),
            "Exception message should mention the missing ID.");
    }

    /**
     * CONCEPT: Simulating a duplicate email registration attempt.
     * The mock returns true for existsByEmail to simulate "already registered".
     */
    @Test
    @DisplayName("Service throws when email is already registered")
    void registrationFailsForDuplicateEmail() {
        // Arrange — email already exists in the "database"
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class,
            () -> userService.registerUser("Alice2", "alice@example.com"),
            "Should throw when email is already taken.");
    }

    // =========================================================================
    // 3. VERIFY: was this method called?
    // =========================================================================

    /**
     * CONCEPT: verify() checks that a mock method WAS called.
     * This verifies side effects — "did the service actually save to the DB?"
     * "did it send a welcome email?"
     *
     * verify(mock).method(args) — called exactly once (default)
     */
    @Test
    @DisplayName("verify() — confirm a mock method was called during registration")
    void verifyInteractionsOnRegistration() {
        // Arrange — email does not exist yet
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);

        // Act
        userService.registerUser("Bob", "bob@example.com");

        // Assert — verify that the service called save() and sent a welcome email
        verify(userRepository).save(any(User.class));       // save was called once
        verify(emailService).sendWelcomeEmail("bob@example.com", "Bob");
    }

    /**
     * CONCEPT: verifyNoInteractions() checks that a mock was NEVER called.
     * Useful for confirming that a failed operation did not trigger side effects.
     */
    @Test
    @DisplayName("verifyNoInteractions() — no email sent when registration fails")
    void noEmailSentOnFailedRegistration() {
        // Arrange — email already taken, registration will fail
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // Act — attempt registration (will throw)
        assertThrows(IllegalStateException.class,
            () -> userService.registerUser("Duplicate", "taken@example.com"));

        // Assert — email service should NOT have been called at all
        verifyNoInteractions(emailService);
    }

    // =========================================================================
    // 4. VERIFY WITH CALL COUNT: verify(mock, times(n))
    // =========================================================================

    /**
     * CONCEPT: You can specify exactly how many times you expect a method to be called.
     *   times(1)   — exactly once (this is the default for verify())
     *   times(2)   — exactly twice
     *   never()    — never called
     *   atLeast(n) — at least n times
     *   atMost(n)  — at most n times
     */
    @Test
    @DisplayName("verify with times() — check exact call count")
    void verifyCallCount() {
        // Arrange
        var alice = new User("1", "Alice", "alice@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(alice));

        // Act — call getUserById twice
        userService.getUserById("1");
        userService.getUserById("1");

        // Assert — findById should have been called exactly twice
        verify(userRepository, times(2)).findById("1");

        // save and emailService should NEVER have been called
        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    // =========================================================================
    // 5. ARGUMENT CAPTOR: capture what was passed to a mock
    // =========================================================================

    /**
     * CONCEPT: ArgumentCaptor captures the argument passed to a mock method,
     * letting you inspect it in your assertions.
     *
     * Use this when you want to verify not just THAT a method was called,
     * but WHAT argument it was called with — especially useful when the
     * argument is created inside the method under test (so you cannot
     * pass it in from the test).
     */
    @Test
    @DisplayName("ArgumentCaptor — capture and inspect what was passed to a mock")
    void argumentCaptorExample() {
        // Arrange
        when(userRepository.existsByEmail("charlie@example.com")).thenReturn(false);

        // Act
        userService.registerUser("Charlie", "charlie@example.com");

        // Capture the User object that was passed to save()
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        // Now inspect the captured value
        User savedUser = userCaptor.getValue();
        assertEquals("Charlie", savedUser.name(),
            "The saved user should have the name we passed in.");
        assertEquals("charlie@example.com", savedUser.email(),
            "The saved user should have the email we passed in.");
        assertNotNull(savedUser.id(),
            "The service should have generated an ID.");
    }

    // =========================================================================
    // 6. SPY: partial mocking of a real object
    // =========================================================================

    /**
     * CONCEPT: A spy wraps a REAL object. By default, all method calls go to
     * the real implementation. You can stub specific methods to override them.
     *
     * Use a spy when:
     * - You want to test the real implementation mostly
     * - But override one or two methods for test isolation
     *
     * Warning: Overuse of spies is a code smell — it often means the class
     * under test has too many responsibilities. Prefer pure mocks when possible.
     */
    @Test
    @DisplayName("spy() — calls real methods but allows selective stubbing")
    void spyExample() {
        // Create a real ArrayList and wrap it in a spy
        var realList = new java.util.ArrayList<String>();
        var spyList  = spy(realList);   // wraps the real list

        // Add to the REAL list through the spy — calls the real add() method
        spyList.add("hello");
        spyList.add("world");

        // The real list now has 2 elements
        assertEquals(2, spyList.size());   // calls real size()

        // Override one method — stub size() to return 42
        doReturn(42).when(spyList).size();

        // Now size() returns the stubbed value, not the real value
        assertEquals(42, spyList.size());

        // But the real data is still there — get() uses the real implementation
        assertEquals("hello", spyList.get(0));
    }

    /**
     * CONCEPT: Spying on a service to verify a specific internal path was taken.
     * This is a more realistic use of spy().
     */
    @Test
    @DisplayName("Spy on UserService to verify delete sends notification email")
    void spyOnServiceToVerifyEmailOnDelete() {
        // Arrange — mock the repository to return a known user
        var alice = new User("1", "Alice", "alice@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(alice));

        // Act — delete the user
        userService.deleteUser("1");

        // Assert — verify repository.deleteById AND email were called
        verify(userRepository).deleteById("1");
        verify(emailService).sendDeletionNotice("alice@example.com");
    }
}
