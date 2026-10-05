# Chapter 2: Constructors and `this`

## The Problem with Manual Field Setting

In the previous chapter, we created objects like this:

```java
Book book = new Book();
book.title = "Clean Code";
book.author = "Robert C. Martin";
book.isbn = "978-0132350884";
book.available = true;
```

This is fragile. What if someone forgets to set `isbn`? What if `available` defaults to `false` but should default to `true`? What if `title` must never be empty? There's no way to enforce these rules with direct field assignment.

**Constructors** solve this problem.

---

## What Is a Constructor?

A **constructor** is a special method that runs automatically when you create an object with `new`. Its job is to initialize the object into a valid, consistent state.

Rules for constructors:
1. Name matches the class name exactly (including capitalization)
2. No return type (not even `void`)
3. Called automatically by `new`

```java
public class Book {
    String title;
    String author;
    String isbn;
    boolean available;
    
    // This is a constructor
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;  // sensible default — always starts available
    }
}

// Now creation is clean and safe:
Book book = new Book("Clean Code", "Robert C. Martin", "978-0132350884");
// available is automatically true — you can't forget to set it!
```

---

## The Default Constructor

If you write no constructors at all, Java silently provides a **default constructor** — one with no parameters that does nothing except create the object:

```java
public class Dog {
    String name;
    int age;
    // No constructor defined → Java provides: public Dog() {}
}

Dog d = new Dog();  // Works! Uses the default constructor
d.name = "Rex";
```

**Important caveat:** As soon as you define ANY constructor, Java stops providing the default constructor. If you still need a no-argument constructor, you must write it yourself.

```java
public class Dog {
    String name;
    
    public Dog(String name) {  // You defined a constructor
        this.name = name;
    }
}

Dog d = new Dog();          // COMPILE ERROR! No no-arg constructor anymore
Dog d = new Dog("Rex");     // Works
```

---

## Parameterized Constructors

A parameterized constructor takes arguments to initialize fields:

```java
public class BankAccount {
    String accountNumber;
    String owner;
    double balance;
    
    // Parameterized constructor
    public BankAccount(String accountNumber, String owner, double initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
    }
}

// Creating accounts:
BankAccount savings = new BankAccount("SAV-001", "Alice", 5000.0);
BankAccount checking = new BankAccount("CHK-002", "Bob", 1000.0);
```

You can add validation in the constructor:

```java
public BankAccount(String accountNumber, String owner, double initialBalance) {
    if (accountNumber == null || accountNumber.isEmpty()) {
        throw new IllegalArgumentException("Account number cannot be empty");
    }
    if (initialBalance < 0) {
        throw new IllegalArgumentException("Initial balance cannot be negative");
    }
    
    this.accountNumber = accountNumber;
    this.owner = owner;
    this.balance = initialBalance;
}
```

Now it's impossible to create an account in an invalid state. **This is one of the most powerful benefits of constructors.**

---

## Constructor Overloading

Just like methods, constructors can be overloaded — multiple constructors with different parameter lists:

```java
public class BankAccount {
    String accountNumber;
    String owner;
    double balance;
    String accountType;
    
    // Constructor 1: Full specification
    public BankAccount(String accountNumber, String owner, 
                       double initialBalance, String accountType) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
        this.accountType = accountType;
    }
    
    // Constructor 2: No initial balance (starts at 0)
    public BankAccount(String accountNumber, String owner, String accountType) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = 0.0;
        this.accountType = accountType;
    }
    
    // Constructor 3: Minimal — just account number and owner, defaults for rest
    public BankAccount(String accountNumber, String owner) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = 0.0;
        this.accountType = "CHECKING";  // default type
    }
}

// All of these are valid:
BankAccount a1 = new BankAccount("ACC-001", "Alice", 5000.0, "SAVINGS");
BankAccount a2 = new BankAccount("ACC-002", "Bob", "CHECKING");
BankAccount a3 = new BankAccount("ACC-003", "Charlie");
```

---

## The `this` Keyword

`this` refers to the **current object** — the object on which the method or constructor is being called.

### Use 1: Distinguishing fields from parameters

The most common use — when a constructor parameter has the same name as a field:

```java
public class Person {
    String name;   // field
    int age;       // field
    
    public Person(String name, int age) {  // parameters also named name, age
        // Without this, there's ambiguity:
        // name = name;  // This just assigns the parameter to itself — bug!
        
        // With this:
        this.name = name;   // field = parameter
        this.age = age;     // field = parameter
    }
}
```

The rule: `this.name` always refers to the field; plain `name` inside a constructor or method refers to the closest local variable/parameter.

### Use 2: Passing the current object as an argument

```java
public class Button {
    String label;
    
    public Button(String label) {
        this.label = label;
    }
    
    public void register(EventListener listener) {
        // Pass 'this' button to the listener so it knows which button was registered
        listener.onRegister(this);
    }
}
```

### Use 3: Returning the current object (for method chaining)

```java
public class Builder {
    private String name;
    private int age;
    
    public Builder setName(String name) {
        this.name = name;
        return this;  // Return the same object
    }
    
    public Builder setAge(int age) {
        this.age = age;
        return this;  // Return the same object
    }
}

// Enables fluent chaining:
Builder b = new Builder().setName("Alice").setAge(30);
```

---

## `this()` — Constructor Chaining

`this()` (note the parentheses) is a special call that invokes *another constructor in the same class*. This allows you to avoid duplicating initialization code:

```java
public class BankAccount {
    String accountNumber;
    String owner;
    double balance;
    String accountType;
    
    // The "master" constructor — all fields explicitly set
    public BankAccount(String accountNumber, String owner, 
                       double balance, String accountType) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
        this.accountType = accountType;
        System.out.println("Account created: " + accountNumber);
    }
    
    // Delegates to the master constructor with a default balance
    public BankAccount(String accountNumber, String owner, String accountType) {
        this(accountNumber, owner, 0.0, accountType);  // calls constructor above
    }
    
    // Delegates further with a default account type
    public BankAccount(String accountNumber, String owner) {
        this(accountNumber, owner, "CHECKING");  // calls constructor above
    }
}
```

**Rules for `this()`:**
- Must be the **first statement** in the constructor body
- Can only appear once per constructor
- Cannot create circular chains (A calls B calls A — compile error)

Why is this useful? Because if you have validation or logging in the "master" constructor, all other constructors benefit from it automatically through chaining.

---

## Copy Constructor

A copy constructor creates a new object as a copy of an existing one:

```java
public class Point {
    double x;
    double y;
    
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // Copy constructor
    public Point(Point other) {
        this(other.x, other.y);  // chains to the main constructor
    }
}

Point p1 = new Point(3.0, 4.0);
Point p2 = new Point(p1);  // Copy — p2 is independent from p1
p2.x = 10.0;               // Changes p2 only, p1 is unchanged
```

This is different from:
```java
Point p2 = p1;  // NOT a copy — both references point to the SAME object
p2.x = 10.0;    // Also changes p1.x!
```

---

## Builder Pattern Preview

When a class has many optional fields, constructors become unwieldy:

```java
// Painful — hard to read, easy to get parameter order wrong
Person p = new Person("Alice", 30, "alice@example.com", 
                       "123 Main St", "555-1234", "Engineer", null, true);
```

The **Builder pattern** solves this with a fluent, readable API:

```java
Person p = new Person.Builder("Alice", 30)  // required fields
    .email("alice@example.com")              // optional
    .address("123 Main St")                  // optional
    .phone("555-1234")                       // optional
    .occupation("Engineer")                  // optional
    .build();                                // create the object
```

Here's how the Builder pattern works:

```java
public class Person {
    // Required fields
    private final String name;
    private final int age;
    
    // Optional fields
    private final String email;
    private final String address;
    private final String phone;
    private final String occupation;
    
    // Private constructor — only the Builder can call it
    private Person(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.email = builder.email;
        this.address = builder.address;
        this.phone = builder.phone;
        this.occupation = builder.occupation;
    }
    
    // Static nested Builder class
    public static class Builder {
        // Required
        private final String name;
        private final int age;
        
        // Optional — with defaults
        private String email = "";
        private String address = "";
        private String phone = "";
        private String occupation = "";
        
        // Builder constructor takes required fields
        public Builder(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        // Each setter returns 'this' for chaining
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder occupation(String occupation) {
            this.occupation = occupation;
            return this;
        }
        
        // Terminal method — creates the actual Person
        public Person build() {
            return new Person(this);
        }
    }
    
    // Getters...
    public String getName() { return name; }
    public int getAge() { return age; }
    
    @Override
    public String toString() {
        return "Person{name=" + name + ", age=" + age + 
               ", email=" + email + "}";
    }
}

// Usage:
Person alice = new Person.Builder("Alice", 30)
    .email("alice@example.com")
    .occupation("Engineer")
    .build();

Person bob = new Person.Builder("Bob", 25)
    .phone("555-5678")
    .build();
```

You'll see this pattern throughout Java libraries: `StringBuilder`, `HttpClient`, `Stream` all use builder-like patterns.

---

## Common Constructor Mistakes

### Mistake 1: Forgetting `this` in parameter shadowing

```java
public class Dog {
    String name;
    
    public Dog(String name) {
        name = name;  // BUG! Assigns parameter to itself, field stays null
    }
}
```

Fix: `this.name = name;`

### Mistake 2: Doing complex work in constructors

```java
public class DataLoader {
    List<Record> data;
    
    public DataLoader(String filePath) {
        // BAD: Reading a file in the constructor
        // What if the file doesn't exist? What if reading fails?
        // Exceptions from constructors are messy
        data = readFile(filePath);
    }
}
```

Better: Use a factory method or separate `initialize()` method.

### Mistake 3: Calling overridable methods from constructors

```java
public class Base {
    public Base() {
        setup();  // DANGEROUS if overridden in a subclass
    }
    
    public void setup() {
        // ...
    }
}
```

This is a subtle bug we'll revisit in the inheritance chapter.

---

## Summary

| Concept | Purpose |
|---------|---------|
| Constructor | Initializes an object when created with `new` |
| Default constructor | Provided by Java when you write none; disappears when you add any constructor |
| Parameterized constructor | Takes arguments to set field values |
| Constructor overloading | Multiple constructors with different parameters |
| `this.field` | Refers to the instance field (vs. local variable) |
| `this()` | Calls another constructor in the same class (must be first statement) |
| Copy constructor | Creates a new object with the same values as an existing one |
| Builder pattern | Readable, flexible way to construct objects with many optional fields |

---

## Key Takeaways

1. Constructors guarantee objects are created in a valid state
2. Add validation in constructors — reject invalid inputs early
3. Use `this.field` to distinguish fields from constructor parameters
4. Use `this()` to chain constructors and avoid code duplication
5. The Builder pattern is your friend when there are many optional fields
6. Once you define any constructor, the default no-arg constructor disappears

---

## What's Next

Our `BankAccount` example still has a problem — its fields are public, so anyone can write `account.balance = 1000000.0` directly, bypassing all business rules. The next chapter on **encapsulation** shows how to protect your data.
