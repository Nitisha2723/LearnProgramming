# Testing Deep Dive

## Building on the Fundamentals

In module 02, you learned the basics of JUnit 5: writing test methods, using assertions, and the structure of a test class. This module goes deeper — into test doubles, mocking with Mockito, integration testing, and testing strategies.

---

## Test Doubles: Mocks, Stubs, Fakes, Spies

A **test double** is any object that substitutes for a real object in a test. Martin Fowler defined four distinct types:

### Stub

A stub provides **canned answers** to calls made during the test. It doesn't verify calls — it just returns what you tell it to.

```java
// Stub: just returns data, no verification
interface UserRepository {
    Optional<User> findById(int id);
}

// Stub implementation
class UserRepositoryStub implements UserRepository {
    @Override
    public Optional<User> findById(int id) {
        return Optional.of(new User(id, "Alice", "alice@example.com"));
    }
}
```

### Mock

A mock is a stub that also **verifies interactions** — it records what was called and lets you assert that specific calls happened.

```java
// With Mockito:
UserRepository mockRepo = mock(UserRepository.class);
when(mockRepo.findById(42)).thenReturn(Optional.of(new User(42, "Alice", ...)));

// After the test:
verify(mockRepo).findById(42);  // Assert that findById was called with 42
verify(mockRepo, times(1)).findById(anyInt());  // Called exactly once
verify(mockRepo, never()).save(any());  // save() was never called
```

### Fake

A fake is a **working implementation** that takes shortcuts not suitable for production. It's more realistic than a stub/mock but lighter than the real thing.

```java
// Fake: a real in-memory implementation
class FakeUserRepository implements UserRepository {
    private final Map<Integer, User> store = new HashMap<>();
    
    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }
    
    @Override
    public User save(User user) {
        store.put(user.getId(), user);
        return user;
    }
}
// Used in tests instead of a real database
```

### Spy

A spy wraps a **real object** but intercepts some calls. Useful when you want real behavior for most methods but want to verify/stub specific ones.

```java
List<String> realList = new ArrayList<>();
List<String> spy = spy(realList);

// Real behavior:
spy.add("Alice");  // Actually adds to realList

// Intercepted behavior:
doReturn(99).when(spy).size();  // Override size() to return 99
spy.add("Bob");
System.out.println(spy.size()); // 99 (stubbed), not 2 (real)

verify(spy).add("Alice");
```

---

## Mockito: The Standard Java Mocking Library

### Setup

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.7.0</version>
    <scope>test</scope>
</dependency>
```

### Creating Mocks

```java
// Method 1: Programmatic
UserRepository repo = mock(UserRepository.class);

// Method 2: Annotation (requires @ExtendWith(MockitoExtension.class))
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    EmailService emailService;

    @InjectMocks
    UserService userService;  // Mockito injects mocks into this class
}
```

### Stubbing with when().thenReturn()

```java
// Return a specific value
when(repo.findById(42)).thenReturn(Optional.of(new User(42, "Alice")));

// Return for any argument
when(repo.findById(anyInt())).thenReturn(Optional.empty());

// Return different values on successive calls
when(repo.findAll())
    .thenReturn(List.of(user1))
    .thenReturn(List.of(user1, user2));  // Second call returns this

// Throw an exception
when(repo.findById(99)).thenThrow(new RuntimeException("Database error"));

// Return based on argument
when(repo.findById(anyInt())).thenAnswer(invocation -> {
    int id = invocation.getArgument(0);
    return id > 0 ? Optional.of(new User(id, "User" + id)) : Optional.empty();
});
```

### Verifying Interactions

```java
// Basic verification — was this method called?
verify(emailService).sendWelcomeEmail(any(User.class));

// With specific arguments
verify(emailService).sendEmail("alice@example.com", "Welcome!");

// Verify call count
verify(repo, times(2)).findById(anyInt());
verify(repo, atLeast(1)).findById(anyInt());
verify(repo, atMost(3)).findById(anyInt());
verify(repo, never()).delete(any());

// Verify no more interactions
verifyNoMoreInteractions(emailService);
verifyNoInteractions(emailService);  // Never called at all

// Capture arguments
ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
verify(repo).save(userCaptor.capture());
User savedUser = userCaptor.getValue();
assertEquals("Alice", savedUser.getName());
```

---

## Testing Exceptions with assertThrows

```java
@Test
void testUserNotFound() {
    when(repo.findById(99)).thenReturn(Optional.empty());

    // Verify that the right exception is thrown
    UserNotFoundException exception = assertThrows(
        UserNotFoundException.class,
        () -> userService.getUserOrThrow(99)
    );

    // Verify exception message
    assertEquals("User not found: 99", exception.getMessage());
}

@Test
void testNullArgumentRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> userService.createUser(null)
    );
}
```

---

## Unit Tests vs Integration Tests

### Unit Tests

- Test a **single class or method** in isolation
- All dependencies are mocked/stubbed
- Fast (milliseconds), no external resources
- Most tests should be unit tests

```java
// Unit test: test UserService in isolation
// UserRepository is mocked — no database
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock  UserRepository repo;
    @InjectMocks  UserService service;

    @Test
    void createUser_savesAndReturnsUser() {
        User user = new User("Alice", "alice@example.com");
        when(repo.save(any())).thenReturn(user);

        User result = service.createUser("Alice", "alice@example.com");
        assertEquals("Alice", result.getName());
        verify(repo).save(any(User.class));
    }
}
```

### Integration Tests

- Test **multiple components together**, often with real databases/files
- Slower, may need test containers or in-memory databases (H2)
- Test that components actually work together
- Fewer than unit tests (the test pyramid)

```java
// Integration test: test the real repository with H2 in-memory database
@SpringBootTest
@AutoConfigureTestDatabase(replace = EMBEDDED)
class UserRepositoryIntegrationTest {
    @Autowired UserRepository repo;

    @Test
    void findById_returnsUser_whenExists() {
        User saved = repo.save(new User("Alice", "alice@example.com"));
        Optional<User> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }
}
```

### The Test Pyramid

```
        /\
       /  \
      / E2E\     (few — expensive, slow, fragile)
     /──────\
    /        \
   / Integration\ (some — test component boundaries)
  /──────────────\
 /                \
/   Unit Tests     \ (many — fast, isolated, cheap)
────────────────────
```

---

## Testing With Test Data: Builders and Object Mothers

### Test Data Builder Pattern

When creating test objects with many fields, builders make tests readable:

```java
// Without builder: which field is which?
User user = new User("Alice", "alice@example.com", 25, "New York",
                     "ACTIVE", true, false, new Date());

// With test builder: self-documenting
User user = UserTestBuilder.aUser()
    .withName("Alice")
    .withEmail("alice@example.com")
    .withAge(25)
    .withStatus("ACTIVE")
    .build();

// Test builder implementation
class UserTestBuilder {
    private String name = "Test User";     // Sensible defaults
    private String email = "test@test.com";
    private int age = 25;
    private String status = "ACTIVE";

    static UserTestBuilder aUser() { return new UserTestBuilder(); }

    UserTestBuilder withName(String name) { this.name = name; return this; }
    UserTestBuilder withEmail(String email) { this.email = email; return this; }
    UserTestBuilder withAge(int age) { this.age = age; return this; }
    UserTestBuilder withStatus(String s) { this.status = s; return this; }

    User build() { return new User(name, email, age, status); }
}
```

### Object Mother Pattern

A class that provides pre-built objects for tests:

```java
class TestUsers {
    static User alice() {
        return new User("Alice", "alice@example.com", 30, "ACTIVE");
    }

    static User blockedUser() {
        return new User("Bob", "bob@example.com", 25, "BLOCKED");
    }

    static User adminUser() {
        return new User("Admin", "admin@example.com", 35, "ADMIN");
    }
}

// Usage in tests
@Test
void blockedUserCannotLogin() {
    User blocked = TestUsers.blockedUser();
    assertThrows(AccountBlockedException.class, () -> authService.login(blocked));
}
```

---

## Code Coverage

**Code coverage** measures what percentage of your source code is executed by your tests.

Types:
- **Line coverage**: What % of lines were executed?
- **Branch coverage**: What % of if/else branches were taken?
- **Method coverage**: What % of methods were called?

### What Coverage Tells You

Coverage shows **what isn't tested**. High coverage doesn't mean tests are good — it means code was executed.

```java
// This has 100% coverage but tests nothing meaningful:
@Test
void badTest() {
    new UserService().createUser("Alice", "alice@example.com");
    // No assertions! Just calling the code.
}
```

### What Coverage Doesn't Tell You

- Whether tests verify the right behavior
- Whether edge cases are tested
- Whether assertions are meaningful

**Rule of thumb:** Aim for 70-80% coverage. Don't chase 100% — it leads to shallow tests.

---

## Mutation Testing

Mutation testing goes further than code coverage. It **introduces small bugs** (mutations) into your code and checks if your tests catch them:

```java
// Original code
int add(int a, int b) { return a + b; }

// Mutation: change + to *
int add(int a, int b) { return a * b; }  // Should FAIL your test!

// If your test only does:
assertEquals(6, add(2, 3));  // ❌ Passes for + AND * when result happens to match!

// Better test:
assertEquals(5, add(2, 3));  // 5 ≠ 6, catches the mutation
assertEquals(7, add(3, 4));  // Also catches subtraction mutation
```

**PIT** (Pitest) is the most popular Java mutation testing tool.

---

## Test Organization

### Mirror Main Code Structure

```
src/
├── main/java/
│   └── com/example/
│       ├── user/
│       │   ├── UserService.java
│       │   └── UserRepository.java
│       └── order/
│           └── OrderService.java
└── test/java/
    └── com/example/
        ├── user/
        │   ├── UserServiceTest.java       ← unit test
        │   └── UserRepositoryTest.java    ← integration test
        └── order/
            └── OrderServiceTest.java
```

### Naming Conventions

```java
// Method naming: methodName_condition_expectedResult
@Test void createUser_withValidData_returnsUser() { ... }
@Test void createUser_withNullName_throwsException() { ... }
@Test void findById_withUnknownId_returnsEmpty() { ... }

// Or: Given-When-Then structure
@Test void given_userDoesNotExist_when_findById_then_returnsEmpty() { ... }
```

### Nested Test Classes for Grouping

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {
        @Test void withValidData_returnsUser() { ... }
        @Test void withNullName_throwsException() { ... }
    }

    @Nested
    @DisplayName("findUser")
    class FindUserTests {
        @Test void withExistingId_returnsUser() { ... }
        @Test void withUnknownId_returnsEmpty() { ... }
    }
}
```
