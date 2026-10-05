# Chapter 3: Encapsulation

## The Problem: Unprotected Data

Imagine a BankAccount where the balance field is public:

```java
public class BankAccount {
    public double balance;  // Anyone can access this directly!
}

// Anywhere in the program:
BankAccount account = new BankAccount("ACC-001", "Alice", 100.0);
account.balance = -9999999.0;  // Set balance to anything! No rules!
account.balance = Double.MAX_VALUE;  // Instant billionaire!
```

This is a disaster. The balance has no protection. Any code anywhere can set it to any value. Business rules — like "you can't withdraw more than your balance" — are meaningless.

**Encapsulation** solves this by restricting who can access and modify an object's internal state.

---

## Information Hiding: The Core Idea

Encapsulation is fundamentally about **information hiding** — keeping the internal implementation details private and exposing only a controlled interface.

Think of a television:
- You interact with it through buttons: On/Off, Volume Up/Down, Channel
- You never interact directly with the circuit boards inside
- The TV's internals can change completely (CRT → LCD → OLED) without affecting how you use the remote
- The interface (the buttons) stays stable

```
EXTERNAL VIEW              INTERNAL (HIDDEN)
┌─────────────────┐       ┌──────────────────────────┐
│   BankAccount   │       │  double balance           │
│   ─────────     │       │  List<Transaction> history│
│   deposit()     │<─────>│  validation rules         │
│   withdraw()    │       │  fee calculation          │
│   getBalance()  │       │  interest computation     │
└─────────────────┘       └──────────────────────────┘
     Public API                    Private internals
```

---

## Private Fields, Public Methods

The standard Java encapsulation pattern:

```java
public class BankAccount {
    // PRIVATE fields — no direct outside access
    private String accountNumber;
    private String owner;
    private double balance;
    
    public BankAccount(String accountNumber, String owner, double initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.balance = initialBalance;
    }
    
    // PUBLIC methods — controlled access to internal state
    public double getBalance() {
        return balance;
    }
    
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance += amount;  // Only this method can change balance via deposit
    }
    
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance -= amount;  // Withdrawal enforces rules
    }
}

// Now this is IMPOSSIBLE:
account.balance = -9999999.0;    // Compile error: balance is private
// Only possible through controlled methods:
account.deposit(100.0);          // Works — follows business rules
account.withdraw(9999999.0);     // Throws exception — insufficient funds
```

---

## Getters and Setters

Getters and setters (also called accessors and mutators) are the standard way to provide controlled access to private fields.

```java
public class Person {
    private String name;
    private int age;
    private String email;
    
    // Getter for name — just reads the value
    public String getName() {
        return name;
    }
    
    // Setter for name — can add validation
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name.trim();  // Also trims whitespace
    }
    
    // Getter for age
    public int getAge() {
        return age;
    }
    
    // Setter for age — validates range
    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        this.age = age;
    }
    
    // Getter for email
    public String getEmail() {
        return email;
    }
    
    // Setter for email — could validate format
    public void setEmail(String email) {
        if (email != null && !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
    }
}
```

### When NOT to Use Getters and Setters

This is important and often misunderstood. Mechanical getters/setters for every field is not good encapsulation — it just moves the problem:

```java
// BAD: This provides NO encapsulation benefit
public class BankAccount {
    private double balance;
    
    public double getBalance() { return balance; }
    
    // If you have a setter like this:
    public void setBalance(double balance) {
        this.balance = balance;  // No validation!
    }
}

// This is as bad as making balance public:
account.setBalance(-9999999.0);  // Still bypasses all business logic!
```

**Rules for getters and setters:**
1. **Always add getters for state that needs to be readable**
2. **Only add setters when the field legitimately needs to change after construction**
3. **Setters must enforce business rules**
4. **Prefer domain methods over setters when possible**

```java
// BAD: setter-based thinking
account.setBalance(account.getBalance() + 100.0);  // Caller does the work

// GOOD: domain method thinking
account.deposit(100.0);  // Object does the work, enforces its own rules
```

---

## The Real Purpose of Encapsulation

Encapsulation gives you three superpowers:

### 1. Control over your data
You decide what values are valid. Invalid data simply cannot enter your object.

### 2. Freedom to change your implementation
Nobody outside knows HOW you store or compute things — only WHAT you expose. You can completely rewrite your internals without breaking anything.

```java
public class Circle {
    private double radius;
    
    // Version 1: stores radius, computes area
    public double getArea() {
        return Math.PI * radius * radius;
    }
}

// Later, you decide to cache area for performance:
public class Circle {
    private double radius;
    private double cachedArea;  // new internal detail
    private boolean areaValid;  // another internal detail
    
    // The public interface UNCHANGED
    public double getArea() {
        if (!areaValid) {
            cachedArea = Math.PI * radius * radius;
            areaValid = true;
        }
        return cachedArea;
    }
}
// External code that calls circle.getArea() still works — it never knew the implementation
```

### 3. Maintaining invariants (rules that must always be true)

An invariant is a condition that must always hold true for your object:
- BankAccount: balance must always be >= 0
- Date: month must always be 1-12
- Circle: radius must always be > 0

With encapsulation, you enforce these in setters/constructors and they can never be violated.

---

## Immutable Objects: The Gold Standard

The ultimate form of encapsulation is the **immutable object** — once created, it cannot change. Its state is set in the constructor and never modified.

Benefits of immutability:
- Thread-safe by definition (no synchronization needed)
- Easy to reason about (state never changes unexpectedly)
- Safe to share and cache
- No defensive copying needed

```java
public final class ImmutableBankAccount {
    private final String accountNumber;  // final = can't be reassigned
    private final String owner;
    private final double balance;
    
    public ImmutableBankAccount(String accountNumber, String owner, double balance) {
        if (balance < 0) throw new IllegalArgumentException("Balance cannot be negative");
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
    }
    
    // Only getters — no setters!
    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public double getBalance() { return balance; }
    
    // Instead of mutating, return a NEW object with the updated value
    public ImmutableBankAccount deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        return new ImmutableBankAccount(accountNumber, owner, balance + amount);
    }
    
    public ImmutableBankAccount withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        return new ImmutableBankAccount(accountNumber, owner, balance - amount);
    }
}

// Usage:
ImmutableBankAccount account = new ImmutableBankAccount("ACC-001", "Alice", 1000.0);
ImmutableBankAccount afterDeposit = account.deposit(500.0);  // new object!
// account.balance is still 1000.0 — unchanged
// afterDeposit.balance is 1500.0
```

Java's built-in immutable classes: `String`, `Integer`, `Double`, `LocalDate`, `LocalTime`.

---

## Real Example: BankAccount Deep Dive

Let's see why `balance` being private is non-negotiable:

```java
public class BankAccount {
    private final String accountNumber;
    private double balance;
    private final List<String> transactionHistory = new ArrayList<>();
    
    public void deposit(double amount) {
        // Rule 1: Amount must be positive
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        
        balance += amount;
        
        // Rule 2: Every change gets recorded
        transactionHistory.add(String.format("Deposit: +%.2f (Balance: %.2f)", 
                                             amount, balance));
    }
    
    public void withdraw(double amount) {
        // Rule 1: Amount must be positive
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        
        // Rule 2: Cannot overdraw
        if (amount > balance) throw new IllegalStateException(
            String.format("Insufficient funds. Balance: %.2f, Requested: %.2f", 
                         balance, amount));
        
        balance -= amount;
        
        // Rule 3: Transaction recorded
        transactionHistory.add(String.format("Withdrawal: -%.2f (Balance: %.2f)", 
                                             amount, balance));
    }
}
```

If `balance` were public, ALL of these rules could be bypassed:
```java
account.balance -= 100;   // No record kept, no validation
account.balance = 0;      // Wipe out account silently
account.balance += 10000; // Fraudulent deposit
```

---

## Access Modifiers

Java has four access levels:

| Modifier | Class | Package | Subclass | World |
|----------|-------|---------|----------|-------|
| `public` | YES | YES | YES | YES |
| `protected` | YES | YES | YES | NO |
| (default/package) | YES | YES | NO | NO |
| `private` | YES | NO | NO | NO |

"Class" = code in the same class
"Package" = code in the same package (directory)
"Subclass" = code in a subclass (even in a different package)
"World" = any code anywhere

### When to use each:

```java
public class BankAccount {
    
    private double balance;         // PRIVATE: implementation detail, protect it
    
    private String secretKey;       // PRIVATE: definitely hide this
    
    protected String accountType;  // PROTECTED: subclasses may need direct access
    
    double internalScore;           // DEFAULT (package): used by other classes in 
                                    // same package (e.g., test code)
    
    public String accountNumber;    // PUBLIC: read-only info, safe to expose
    // Better: make this private with a getter
}
```

**General guidance:**
- Default to `private` for fields
- Methods that are part of the public contract: `public`
- Methods for internal use: `private`
- Methods for subclasses to override but not for public use: `protected`
- Default (package) access: useful for package-internal utilities

---

## Access Modifier Scope — Visual Guide

```
┌─────────────────────────────────────────────────────────────────┐
│                         Java Code Universe                       │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Same Package                         │    │
│  │                                                         │    │
│  │  ┌───────────────────────────────────────────────┐     │    │
│  │  │              Same Class                       │     │    │
│  │  │                                               │     │    │
│  │  │  private field  ◄────── accessible here only │     │    │
│  │  │                                               │     │    │
│  │  │  (default) field ◄─────────────────────────────────┤    │
│  │  │                                               │     │    │
│  │  │  protected field ◄──────────────────────────────────────┤
│  │  │  (also accessible in subclasses)              │     │    │
│  │  │                                               │     │    │
│  │  │  public field  ◄────────────────────────────────────────-┤
│  │  │                                               │     │    │
│  │  └───────────────────────────────────────────────┘     │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Defensive Copies for Mutable Fields

There's a subtle encapsulation issue with mutable fields. Even with getters, you can accidentally expose internals:

```java
public class Schedule {
    private List<String> appointments = new ArrayList<>();
    
    // DANGEROUS getter — exposes the internal list directly
    public List<String> getAppointments() {
        return appointments;  // Caller can modify this list!
    }
    
    // SAFE getter — returns a copy
    public List<String> getAppointments() {
        return new ArrayList<>(appointments);  // Defensive copy
    }
    
    // Or return unmodifiable view:
    public List<String> getAppointments() {
        return Collections.unmodifiableList(appointments);
    }
}

// Without defensive copy:
List<String> appts = schedule.getAppointments();
appts.clear();  // OOPS! Just cleared the schedule's internal list!

// With defensive copy or unmodifiable view:
List<String> appts = schedule.getAppointments();
appts.clear();  // Only clears the copy/throws exception — schedule unchanged
```

---

## Practical Encapsulation Checklist

When designing a class:

- [ ] Are all fields `private`?
- [ ] Are getters provided only for state that consumers need to read?
- [ ] Are setters provided only for state that can legitimately change?
- [ ] Do setters validate input?
- [ ] Do business methods enforce all relevant rules?
- [ ] Are mutable fields defensively copied in getters?
- [ ] Is the class `final` if it shouldn't be subclassed?
- [ ] Are there any ways to get the object into an invalid state?

---

## Summary

Encapsulation = **private data + public interface**

The benefits:
1. **Safety**: Invalid data cannot corrupt your objects
2. **Flexibility**: Change internals without breaking external code
3. **Maintainability**: Clear separation between interface and implementation
4. **Correctness**: Invariants are always maintained

---

## Key Takeaways

1. Make fields `private` by default — always
2. Expose behavior through methods, not fields
3. Getters and setters are NOT automatically good encapsulation — validate in setters, prefer domain methods
4. Immutable objects are the safest form of encapsulation
5. Be careful with mutable return values — use defensive copies
6. Use the least permissive access modifier that works

---

## What's Next

We have a solid `BankAccount` class. What if we want `SavingsAccount` and `CheckingAccount` that share most properties but behave slightly differently? The answer is **inheritance** — covered in the next chapter.
