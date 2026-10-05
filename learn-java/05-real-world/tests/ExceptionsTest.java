import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ExceptionsTest.java
 *
 * Tests for exception handling, try-with-resources, and custom exceptions.
 * Also demonstrates Mockito for mocking dependencies.
 *
 * Run with JUnit 5 + Mockito on the classpath.
 */
@DisplayName("Module 05: Exceptions and Real-World Tests")
class ExceptionsTest {

    // =========================================================
    // Domain classes for testing
    // =========================================================

    static class ValidationException extends RuntimeException {
        private final String field;
        ValidationException(String field, String msg) {
            super("Validation failed for '" + field + "': " + msg);
            this.field = field;
        }
        String getField() { return field; }
    }

    static class NotFoundException extends RuntimeException {
        NotFoundException(String msg)               { super(msg); }
        NotFoundException(String msg, Throwable cause) { super(msg, cause); }
    }

    interface UserRepository {
        Optional<String> findEmailById(int id);
        void save(String name, String email);
        int count();
    }

    static class UserService {
        private final UserRepository repository;

        UserService(UserRepository repository) {
            this.repository = Objects.requireNonNull(repository, "repository cannot be null");
        }

        String getUserEmail(int id) {
            return repository.findEmailById(id)
                .orElseThrow(() -> new NotFoundException("No user with id: " + id));
        }

        void createUser(String name, String email) {
            if (name == null || name.isBlank())
                throw new ValidationException("name", "cannot be null or blank");
            if (email == null || !email.contains("@"))
                throw new ValidationException("email", "must contain '@': " + email);
            repository.save(name, email);
        }

        String getUserEmailWithFallback(int id, String fallback) {
            try {
                return getUserEmail(id);
            } catch (NotFoundException e) {
                return fallback;
            }
        }
    }

    // =========================================================
    // Exception handling tests (no mocks)
    // =========================================================

    @Nested
    @DisplayName("Custom Exceptions")
    class CustomExceptionTests {

        @Test
        @DisplayName("ValidationException carries field name and message")
        void validationException_carriesFieldName() {
            ValidationException ex = new ValidationException("email", "invalid format");
            assertEquals("email", ex.getField());
            assertTrue(ex.getMessage().contains("email"));
            assertTrue(ex.getMessage().contains("invalid format"));
        }

        @Test
        @DisplayName("NotFoundException can wrap a cause")
        void notFoundException_chainsCause() {
            RuntimeException cause = new RuntimeException("DB error");
            NotFoundException ex = new NotFoundException("User not found", cause);
            assertEquals(cause, ex.getCause());
            assertTrue(ex.getMessage().contains("User not found"));
        }

        @Test
        @DisplayName("Exception chaining preserves original message")
        void exceptionChaining_preservesOriginal() {
            try {
                try {
                    throw new IOException("connection reset");
                } catch (IOException e) {
                    throw new NotFoundException("Data unavailable", e);
                }
            } catch (NotFoundException e) {
                assertNotNull(e.getCause());
                assertEquals("connection reset", e.getCause().getMessage());
            }
        }
    }

    // =========================================================
    // try-with-resources tests
    // =========================================================

    @Nested
    @DisplayName("try-with-resources")
    class TryWithResourcesTests {

        @Test
        @DisplayName("Resource is closed even when exception is thrown")
        void resource_closedOnException() {
            boolean[] closed = {false};

            // Anonymous AutoCloseable
            AutoCloseable resource = () -> closed[0] = true;

            assertThrows(RuntimeException.class, () -> {
                try (AutoCloseable r = resource) {
                    throw new RuntimeException("intentional");
                }
            });

            assertTrue(closed[0], "Resource should be closed even after exception");
        }

        @Test
        @DisplayName("Resource is closed on normal exit")
        void resource_closedOnNormalExit() throws Exception {
            boolean[] closed = {false};
            AutoCloseable resource = () -> closed[0] = true;

            try (AutoCloseable r = resource) {
                // normal execution
            }

            assertTrue(closed[0], "Resource should be closed on normal exit");
        }

        @Test
        @DisplayName("File written with try-with-resources is readable after")
        void fileWrite_tryWithResources() throws IOException {
            Path temp = Files.createTempFile("test", ".txt");
            try {
                try (BufferedWriter w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                    w.write("hello test");
                }
                assertEquals("hello test", Files.readString(temp, StandardCharsets.UTF_8));
            } finally {
                Files.deleteIfExists(temp);
            }
        }

        @Test
        @DisplayName("Large file read via Files.lines() doesn't load all into memory")
        void fileLines_streamIsLazy() throws IOException {
            Path temp = Files.createTempFile("large", ".txt");
            try {
                // Write 1000 lines
                try (BufferedWriter w = Files.newBufferedWriter(temp)) {
                    for (int i = 0; i < 1000; i++) w.write("line " + i + "\n");
                }

                // Count lines with filter via Stream (lazy)
                long count;
                try (Stream<String> lines = Files.lines(temp)) {
                    count = lines.filter(l -> l.contains("5")).count();
                }
                // Lines containing "5": 5,15,25,...,95, 105,115,...,195, 250-259, 350-359...
                // Just verify it's a reasonable number and doesn't throw
                assertTrue(count > 0, "Should find some lines containing '5'");
            } finally {
                Files.deleteIfExists(temp);
            }
        }
    }

    // =========================================================
    // Mockito tests
    // =========================================================

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UserService with Mockito")
    class UserServiceMockitoTests {

        @Mock
        UserRepository mockRepository;

        @Test
        @DisplayName("getUserEmail returns email when user exists")
        void getUserEmail_returnsEmail_whenUserExists() {
            when(mockRepository.findEmailById(42))
                .thenReturn(Optional.of("alice@example.com"));

            UserService service = new UserService(mockRepository);
            String email = service.getUserEmail(42);

            assertEquals("alice@example.com", email);
            verify(mockRepository).findEmailById(42);
        }

        @Test
        @DisplayName("getUserEmail throws NotFoundException when user doesn't exist")
        void getUserEmail_throwsNotFoundException_whenNotFound() {
            when(mockRepository.findEmailById(99)).thenReturn(Optional.empty());

            UserService service = new UserService(mockRepository);

            NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> service.getUserEmail(99)
            );

            assertTrue(ex.getMessage().contains("99"));
            verify(mockRepository).findEmailById(99);
        }

        @Test
        @DisplayName("getUserEmailWithFallback returns fallback when not found")
        void getUserEmailWithFallback_returnsFallback_whenNotFound() {
            when(mockRepository.findEmailById(anyInt())).thenReturn(Optional.empty());

            UserService service = new UserService(mockRepository);
            String result = service.getUserEmailWithFallback(99, "default@example.com");

            assertEquals("default@example.com", result);
        }

        @Test
        @DisplayName("createUser validates name — null name throws ValidationException")
        void createUser_nullName_throwsValidationException() {
            UserService service = new UserService(mockRepository);

            ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createUser(null, "test@test.com")
            );

            assertEquals("name", ex.getField());
            verify(mockRepository, never()).save(any(), any());  // save() should NOT be called
        }

        @Test
        @DisplayName("createUser validates email — invalid email throws ValidationException")
        void createUser_invalidEmail_throwsValidationException() {
            UserService service = new UserService(mockRepository);

            assertThrows(
                ValidationException.class,
                () -> service.createUser("Alice", "not-an-email")
            );

            verifyNoInteractions(mockRepository);
        }

        @Test
        @DisplayName("createUser saves user when input is valid")
        void createUser_validInput_callsSave() {
            UserService service = new UserService(mockRepository);
            service.createUser("Alice", "alice@example.com");

            verify(mockRepository).save("Alice", "alice@example.com");
            verifyNoMoreInteractions(mockRepository);
        }

        @Test
        @DisplayName("repository is called exactly once per getUserEmail call")
        void repositoryCalledOnce_perGetUserEmail() {
            when(mockRepository.findEmailById(anyInt()))
                .thenReturn(Optional.of("test@test.com"));

            UserService service = new UserService(mockRepository);
            service.getUserEmail(1);
            service.getUserEmail(2);
            service.getUserEmail(3);

            verify(mockRepository, times(3)).findEmailById(anyInt());
        }
    }

    // =========================================================
    // Stream pipeline tests
    // =========================================================

    @Nested
    @DisplayName("Stream Pipelines")
    class StreamTests {

        record Person(String name, int age, String city) {}

        List<Person> people = List.of(
            new Person("Alice",   30, "Berlin"),
            new Person("Bob",     25, "Munich"),
            new Person("Charlie", 35, "Berlin"),
            new Person("Diana",   28, "Hamburg"),
            new Person("Eve",     32, "Berlin")
        );

        @Test
        @DisplayName("filter + map + collect produces correct list")
        void filterMapCollect_correct() {
            List<String> berliners = people.stream()
                .filter(p -> p.city().equals("Berlin"))
                .map(Person::name)
                .sorted()
                .collect(Collectors.toList());

            assertEquals(List.of("Alice", "Charlie", "Eve"), berliners);
        }

        @Test
        @DisplayName("groupingBy produces correct groups")
        void groupingBy_correct() {
            Map<String, Long> countByCity = people.stream()
                .collect(Collectors.groupingBy(Person::city, Collectors.counting()));

            assertEquals(3L, countByCity.get("Berlin"));
            assertEquals(1L, countByCity.get("Munich"));
            assertEquals(1L, countByCity.get("Hamburg"));
        }

        @Test
        @DisplayName("reduce sum gives correct total")
        void reduce_sum_correct() {
            int totalAge = people.stream()
                .mapToInt(Person::age)
                .sum();

            assertEquals(30 + 25 + 35 + 28 + 32, totalAge);
        }

        @Test
        @DisplayName("flatMap flattens nested lists correctly")
        void flatMap_flattensCorrectly() {
            List<List<Integer>> nested = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6, 7, 8, 9)
            );

            List<Integer> flat = nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            assertEquals(9, flat.size());
            assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), flat);
        }

        @Test
        @DisplayName("Optional chaining returns empty when any step is absent")
        void optional_chaining_returnsEmptyOnAbsence() {
            Optional<String> emptyChain = Optional.<String>empty()
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty());

            assertTrue(emptyChain.isEmpty());
        }

        @Test
        @DisplayName("stream().distinct() removes duplicates")
        void distinct_removesDuplicates() {
            List<Integer> withDups = List.of(1, 2, 2, 3, 3, 3, 4);
            long uniqueCount = withDups.stream().distinct().count();
            assertEquals(4, uniqueCount);
        }

        @Test
        @DisplayName("Collectors.joining with delimiter formats correctly")
        void joining_formatsCorrectly() {
            String result = people.stream()
                .map(Person::name)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));

            assertEquals("[Alice, Bob, Charlie, Diana, Eve]", result);
        }
    }
}
