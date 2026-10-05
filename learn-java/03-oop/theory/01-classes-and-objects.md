# Chapter 1: Classes and Objects

## The World Is Made of Objects

Look around you right now. You see a chair, a desk, a laptop, a coffee mug. Each of these is an *object* — it has properties (what it is) and behaviors (what it can do).

A **chair** has:
- Properties: color, material, number of legs, height
- Behaviors: adjust height, fold, unfold

A **bank account** has:
- Properties: account number, owner name, balance
- Behaviors: deposit, withdraw, transfer

Object-Oriented Programming (OOP) mirrors this reality. Instead of writing a sequence of instructions, you model your program as a collection of objects that interact with each other. This is why Java code feels more natural for large systems — it maps directly to how humans think about the world.

---

## What Is a Class?

A **class** is a blueprint, a template, a specification. It describes *what kind of thing* you want to create — what properties it should have and what it should be able to do.

Think of an architectural drawing for a house:
- The drawing shows: 3 bedrooms, 2 bathrooms, open kitchen
- The drawing *is not a house* — it is a description of a house
- You can build many houses from the same drawing, each slightly different

```
BLUEPRINT (Class)           HOUSES (Objects)
┌───────────────────┐       ┌──────────┐  ┌──────────┐  ┌──────────┐
│   House Blueprint │  ───> │ 42 Elm St│  │ 7 Oak Ave│  │ 99 Pine  │
│  - bedrooms: int  │       │ red, 3bd │  │ blue, 3bd│  │ white,3bd│
│  - color: String  │       └──────────┘  └──────────┘  └──────────┘
│  + open(): void   │
└───────────────────┘
```

In Java, a class looks like this:

```java
public class BankAccount {
    // Fields (properties) — what a BankAccount HAS
    String accountNumber;
    String owner;
    double balance;
    
    // Methods (behaviors) — what a BankAccount CAN DO
    void deposit(double amount) {
        balance += amount;
    }
    
    void withdraw(double amount) {
        balance -= amount;
    }
}
```

---

## What Is an Object?

An **object** is an *instance* of a class. When you actually create something from the blueprint, that living, breathing thing is an object.

Going back to the house analogy:
- Blueprint = class
- The actual house at 42 Elm Street = object (instance)
- The house at 7 Oak Avenue = another object, same class

```java
// Create an object (instantiate the class)
BankAccount myAccount = new BankAccount();
myAccount.accountNumber = "ACC-001";
myAccount.owner = "Alice";
myAccount.balance = 1000.0;

// Create another object — completely separate from the first
BankAccount yourAccount = new BankAccount();
yourAccount.accountNumber = "ACC-002";
yourAccount.owner = "Bob";
yourAccount.balance = 500.0;

// Each object is independent
myAccount.deposit(200);    // myAccount.balance is now 1200
yourAccount.deposit(100);  // yourAccount.balance is now 600
                            // myAccount is unchanged
```

Key insight: `myAccount` and `yourAccount` are two *separate objects*, both created from the same `BankAccount` class. They share the same structure (fields and methods) but have completely independent data.

---

## Real-World Examples

### Example 1: Car

```
CLASS: Car
├── Fields (state):
│   ├── make: String         (e.g., "Honda", "Toyota")
│   ├── model: String        (e.g., "Civic", "Corolla")
│   ├── year: int            (e.g., 2022)
│   ├── color: String        (e.g., "Red")
│   └── currentSpeed: double (e.g., 0.0)
│
└── Methods (behavior):
    ├── accelerate(double amount)
    ├── brake(double amount)
    ├── honk()
    └── getInfo(): String
```

```java
// Three different Car objects from the same Car class
Car hondaCivic = new Car();
hondaCivic.make = "Honda";
hondaCivic.model = "Civic";
hondaCivic.year = 2022;

Car toyotaCorolla = new Car();
toyotaCorolla.make = "Toyota";
toyotaCorolla.model = "Corolla";
toyotaCorolla.year = 2021;

Car teslaModel3 = new Car();
teslaModel3.make = "Tesla";
teslaModel3.model = "Model 3";
teslaModel3.year = 2023;
```

### Example 2: Student

```
CLASS: Student
├── Fields:
│   ├── name: String
│   ├── studentId: String
│   ├── grades: double[]
│   └── major: String
│
└── Methods:
    ├── calculateGPA(): double
    ├── addGrade(double grade)
    └── isHonorRoll(): boolean
```

---

## Class Anatomy: Fields and Methods

Every class has two main components:

### Fields (also called instance variables or attributes)
Fields represent the **state** of an object — what it knows, what it contains, its characteristics.

```java
public class Person {
    String name;        // What the person is called
    int age;            // How old they are
    String email;       // How to contact them
}
```

Each *object* has its own copy of these fields. If you change `person1.name`, `person2.name` is unaffected.

### Methods
Methods represent the **behavior** of an object — what it can do, what actions it can perform.

```java
public class Person {
    String name;
    int age;
    
    // A method — defines behavior
    void greet() {
        System.out.println("Hello, my name is " + name);
    }
    
    // A method that returns a value
    boolean isAdult() {
        return age >= 18;
    }
    
    // A method that takes parameters
    void birthday(int years) {
        age += years;
        System.out.println("Happy birthday! Now " + age + " years old.");
    }
}
```

---

## UML Class Diagram Notation

UML (Unified Modeling Language) provides a standard way to visually represent classes. You don't need to memorize all of it, but this basic notation is universally understood:

```
┌─────────────────────────────────┐
│         ClassName               │  ← Class Name (top section)
├─────────────────────────────────┤
│  - privateField: Type           │  ← Fields (middle section)
│  # protectedField: Type         │    - = private
│  + publicField: Type            │    # = protected
│                                 │    + = public
├─────────────────────────────────┤
│  + publicMethod(param): Return  │  ← Methods (bottom section)
│  - privateHelper(): void        │
└─────────────────────────────────┘
```

Example — BankAccount class:

```
┌──────────────────────────────────────────┐
│              BankAccount                 │
├──────────────────────────────────────────┤
│  - accountNumber: String                 │
│  - owner: String                         │
│  - balance: double                       │
├──────────────────────────────────────────┤
│  + BankAccount(accountNumber, owner)     │
│  + deposit(amount: double): void         │
│  + withdraw(amount: double): void        │
│  + getBalance(): double                  │
│  + toString(): String                    │
└──────────────────────────────────────────┘
```

---

## Class vs Object in Memory

This is crucial to understand deeply. Let's trace what happens in memory:

```java
// Step 1: Class definition — stored in the "method area" of JVM memory
// (like a template stored on disk)
public class Dog {
    String name;
    int age;
    
    void bark() {
        System.out.println(name + " says: Woof!");
    }
}

// Step 2: Create an object — stored in the "heap" memory
Dog rex = new Dog();     // Allocates memory on heap for a Dog
rex.name = "Rex";
rex.age = 3;

Dog buddy = new Dog();   // Allocates MORE memory on heap for ANOTHER Dog
buddy.name = "Buddy";
buddy.age = 5;
```

Memory picture:

```
STACK                    HEAP
┌─────────┐             ┌──────────────────┐
│  rex ───┼────────────>│  Dog object #1   │
├─────────┤             │  name = "Rex"    │
│ buddy ──┼──────────┐  │  age = 3         │
└─────────┘          │  └──────────────────┘
                     │
                     │  ┌──────────────────┐
                     └─>│  Dog object #2   │
                        │  name = "Buddy"  │
                        │  age = 5         │
                        └──────────────────┘
```

Key points:
- `rex` and `buddy` are **references** (like addresses) stored on the stack
- The actual Dog objects live on the **heap**
- The class itself (the template) lives in the **method area**
- Multiple references can point to the SAME object:

```java
Dog myDog = rex;  // myDog points to the same object as rex
myDog.age = 4;    // This changes rex.age too! Same object!
System.out.println(rex.age);  // Prints: 4
```

---

## The Null Reference

When you declare a variable but don't create an object, it's `null` — it points to nothing:

```java
Dog myDog;          // myDog is declared but null
myDog.bark();       // NullPointerException! There's no object to call bark() on

myDog = new Dog();  // Now myDog points to an actual object
myDog.bark();       // Works fine
```

---

## When to Create a Class: The Noun Test

A simple heuristic: **if you can describe something as a noun that has its own properties and behaviors, it's a good candidate for a class.**

Imagine you're building a library system. Look at the requirements:
> "The library manages books. Each book has a title, author, and ISBN. Members can check out books. The library keeps track of which books are available."

Nouns in the requirements:
- **Library** → class (has books, manages checkouts)
- **Book** → class (has title, author, ISBN, availability)
- **Member** → class (has name, ID, list of checked-out books)

What about adjectives or states? Those become *fields*:
- "available" → `boolean available` field in Book
- "checked out" → state tracked in the system

What about verbs? Those become *methods*:
- "check out" → `checkOut(Book book)` method in Library
- "return" → `returnBook(Book book)` method in Library

---

## Your First Complete Class

Let's put it all together:

```java
public class Book {
    // Fields — what every Book object has
    String title;
    String author;
    String isbn;
    int pageCount;
    boolean available;
    
    // Method — behavior of a Book
    String getSummary() {
        return "\"" + title + "\" by " + author + 
               " (" + pageCount + " pages)" +
               (available ? " - Available" : " - Checked out");
    }
    
    void checkOut() {
        if (available) {
            available = false;
            System.out.println("\"" + title + "\" has been checked out.");
        } else {
            System.out.println("Sorry, \"" + title + "\" is not available.");
        }
    }
    
    void returnBook() {
        available = true;
        System.out.println("\"" + title + "\" has been returned. Thank you!");
    }
}

// Using the Book class:
public class LibraryDemo {
    public static void main(String[] args) {
        Book book1 = new Book();
        book1.title = "Clean Code";
        book1.author = "Robert C. Martin";
        book1.isbn = "978-0132350884";
        book1.pageCount = 464;
        book1.available = true;
        
        System.out.println(book1.getSummary());
        book1.checkOut();
        System.out.println(book1.getSummary());
        book1.returnBook();
        System.out.println(book1.getSummary());
    }
}
```

Output:
```
"Clean Code" by Robert C. Martin (464 pages) - Available
"Clean Code" has been checked out.
"Clean Code" by Robert C. Martin (464 pages) - Checked out
"Clean Code" has been returned. Thank you!
"Clean Code" by Robert C. Martin (464 pages) - Available
```

---

## Summary

| Concept | Definition | Analogy |
|---------|-----------|---------|
| Class | Blueprint/template | Architectural drawing |
| Object | Instance of a class | Actual house built from drawing |
| Field | Property/state of object | Color, size of the house |
| Method | Behavior/action of object | Open door, turn on lights |
| Instantiation | Creating an object from a class | Building the house |
| Reference | Variable pointing to an object | Street address of the house |

---

## Key Takeaways

1. A class defines the structure; objects are the actual things
2. You can create many objects from one class
3. Each object has its own copy of fields
4. Objects live on the heap; references live on the stack
5. `new` keyword creates a new object on the heap
6. If a noun has properties and behaviors, it can be a class

---

## What's Next

In the next chapter, we'll learn about **constructors** — special methods that properly initialize objects when they're created, instead of setting fields manually after creation.

```java
// Instead of this (manual field setting):
Book b = new Book();
b.title = "Clean Code";
b.author = "Martin";

// We'll do this (constructor initializes everything):
Book b = new Book("Clean Code", "Martin", "978-0132350884", 464);
```

---

## A Note on Monetary Values: BigDecimal

When building financial applications (which you WILL do), never use `double` or `float` for money.

The reason:
```java
System.out.println(0.1 + 0.2);  // Prints: 0.30000000000000004 !!
```

This is not a Java bug — it is how floating-point numbers work in every language. Computers represent numbers in binary, and fractions like 0.1 cannot be expressed exactly in binary, just as 1/3 cannot be expressed exactly in decimal. The error is tiny, but money is exact. Multiply that tiny error across millions of transactions and you have a serious financial discrepancy.

Use `BigDecimal` instead:
```java
BigDecimal price = new BigDecimal("19.99");
BigDecimal tax = new BigDecimal("0.20");
BigDecimal total = price.add(tax.multiply(price));  // Exact!
```

Important: always construct `BigDecimal` from a `String`, not a `double`. `new BigDecimal(0.1)` still has the floating-point error; `new BigDecimal("0.1")` is exact.

This is a real-world concern — financial bugs have caused millions in losses. The rule is simple: use `BigDecimal` for money, `double` for scientific calculations where tiny rounding errors are acceptable.
