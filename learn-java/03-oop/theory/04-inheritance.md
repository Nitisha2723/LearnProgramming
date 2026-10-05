# Chapter 4: Inheritance

## What Is Inheritance?

Inheritance is a mechanism where one class (the **child** or **subclass**) automatically gets the fields and methods of another class (the **parent** or **superclass**). The child class can then add new features or change existing behaviors.

Think of it like biological inheritance:
- A German Shepherd *is an* Animal
- A Poodle *is an* Animal
- Both have DNA from the Animal "blueprint"
- But each also has specific dog breed characteristics

In code:
```
Animal (parent)
├── name
├── age
├── eat()
├── sleep()
└── makeSound()    ← different for each animal

    Dog (child of Animal)
    ├── [inherits name, age, eat(), sleep() from Animal]
    ├── breed        ← new field
    ├── makeSound()  ← overrides Animal's version
    └── fetch()      ← new method

    Cat (child of Animal)
    ├── [inherits name, age, eat(), sleep() from Animal]
    ├── isIndoor     ← new field
    ├── makeSound()  ← overrides Animal's version
    └── purr()       ← new method
```

---

## The `extends` Keyword

```java
public class Animal {
    protected String name;
    protected int age;
    
    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public void eat() {
        System.out.println(name + " is eating.");
    }
    
    public void sleep() {
        System.out.println(name + " is sleeping.");
    }
    
    public String makeSound() {
        return "...";  // generic animal sound
    }
}

public class Dog extends Animal {  // Dog inherits from Animal
    private String breed;
    
    public Dog(String name, int age, String breed) {
        super(name, age);    // Call Animal's constructor — REQUIRED
        this.breed = breed;
    }
    
    // New behavior specific to Dog
    public void fetch() {
        System.out.println(name + " fetches the ball!");
    }
    
    // Override Animal's makeSound
    @Override
    public String makeSound() {
        return "Woof!";
    }
}

// Usage:
Dog rex = new Dog("Rex", 3, "German Shepherd");
rex.eat();           // Inherited from Animal: "Rex is eating."
rex.sleep();         // Inherited from Animal: "Rex is sleeping."
rex.fetch();         // Dog's own method
System.out.println(rex.makeSound());  // "Woof!" — overridden version
```

---

## What Is Inherited? What Isn't?

### What IS inherited:
- All fields (public, protected, and default/package — but not private)
- All methods (public, protected, and default/package — but not private)
- Nested classes

### What is NOT inherited:
- **Constructors** — the child must define its own constructors
- **Private members** — but they exist in the object; the child just can't access them directly
- `static` members (they belong to the class, not the object)

```java
public class Animal {
    private String secretVaccineCode;    // NOT inherited (private)
    protected String name;               // IS inherited (protected)
    public int age;                      // IS inherited (public)
    
    public Animal(String name, int age) {  // Constructor NOT inherited
        this.name = name;
        this.age = age;
    }
    
    private void internalProcess() { }   // NOT accessible from Dog
    protected void cleanUp() { }         // IS accessible from Dog
    public void eat() { }                // IS accessible from Dog
}
```

Even though `secretVaccineCode` is not accessible in `Dog`, it still exists in every `Dog` object — the `Animal` constructor sets it, and Animal's own methods use it.

---

## The `super` Keyword

`super` refers to the parent class. It has two main uses:

### 1. `super(args)` — Call parent constructor

```java
public class Dog extends Animal {
    private String breed;
    
    public Dog(String name, int age, String breed) {
        super(name, age);  // MUST be first statement if called
        // This calls Animal(name, age), which initializes name and age fields
        this.breed = breed;
    }
}
```

**Rule:** If a parent class has no no-argument constructor, you MUST explicitly call `super(args)` as the first statement in every child constructor.

### 2. `super.method()` — Call parent's version of a method

```java
public class Dog extends Animal {
    @Override
    public void eat() {
        super.eat();  // Call Animal's eat() first
        System.out.println(name + " wags its tail after eating.");
    }
}

Dog rex = new Dog("Rex", 3, "Labrador");
rex.eat();
// Output:
// Rex is eating.     ← from super.eat()
// Rex wags its tail after eating.  ← additional Dog behavior
```

---

## Method Overriding

When a subclass provides its own implementation for a method inherited from the parent, that's called **method overriding**.

Rules:
- Same method name and parameter list as the parent
- Same return type (or a subtype — called **covariant return type**)
- Access modifier must be same or more permissive (can't make it more restrictive)
- Can't override `final` methods
- Can't override `static` methods (those are hidden, not overridden)

```java
public class Animal {
    public String makeSound() {
        return "...";
    }
    
    public String describe() {
        return "I am " + name + ", a " + getClass().getSimpleName();
    }
}

public class Dog extends Animal {
    @Override
    public String makeSound() {
        return "Woof!";   // Override — provides Dog-specific sound
    }
    // Note: describe() is NOT overridden — Dog inherits Animal's version
}

public class Cat extends Animal {
    @Override
    public String makeSound() {
        return "Meow!";   // Override — provides Cat-specific sound
    }
}
```

---

## The @Override Annotation

`@Override` tells the compiler "I intend to override a parent method." This is optional in terms of making the code work, but it is **essential good practice**:

```java
// Without @Override:
public class Dog extends Animal {
    public String makesound() {  // Typo! 's' is lowercase
        return "Woof!";
    }
    // This creates a NEW method, not an override!
    // Animal's makeSound() is still used — silent bug!
}

// With @Override:
public class Dog extends Animal {
    @Override
    public String makesound() {  // Typo! Compiler catches this!
        return "Woof!";           // COMPILE ERROR: makesound() doesn't exist in Animal
    }
}
```

**Always use `@Override` when overriding.** It costs nothing and prevents subtle bugs.

---

## The Object Class

Here's something profound: **every class in Java automatically extends `Object`** (from `java.lang.Object`).

```java
public class Dog extends Animal { ... }
// Is actually:
// Dog extends Animal extends Object
```

The `Object` class provides methods that every class has:

| Method | Purpose |
|--------|---------|
| `toString()` | Returns a string representation |
| `equals(Object o)` | Tests equality |
| `hashCode()` | Returns a hash code integer |
| `getClass()` | Returns the class of this object |
| `clone()` | Creates a shallow copy (use carefully) |

---

## Overriding `equals()` and `hashCode()`

Two of the most important `Object` methods to override are `equals()` and `hashCode()`.

### Default behavior (bad for most classes):
```java
Dog d1 = new Dog("Rex", 3, "Labrador");
Dog d2 = new Dog("Rex", 3, "Labrador");

// Default equals() checks REFERENCE EQUALITY (same object in memory)
System.out.println(d1 == d2);       // false (different objects)
System.out.println(d1.equals(d2));  // false (default equals uses ==)

// But we might want VALUE EQUALITY (same data)
```

### Overriding equals():
```java
public class Dog extends Animal {
    private String breed;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;           // Same reference → definitely equal
        if (obj == null) return false;           // null is never equal
        if (getClass() != obj.getClass()) return false;  // Different class → not equal
        
        Dog other = (Dog) obj;                   // Safe to cast now
        return age == other.age &&
               Objects.equals(name, other.name) &&
               Objects.equals(breed, other.breed);
    }
}
```

### Why you MUST also override `hashCode()`:

The **contract**: **If two objects are equal (by `equals()`), they must have the same `hashCode()`.**

This is required for objects to work correctly in hash-based collections like `HashMap` and `HashSet`:

```java
// If you override equals() but not hashCode():
Set<Dog> set = new HashSet<>();
Dog d1 = new Dog("Rex", 3, "Labrador");
set.add(d1);

Dog d2 = new Dog("Rex", 3, "Labrador");  // Same data as d1
System.out.println(set.contains(d2));    // FALSE! Bug! (because hashCodes differ)
```

Always override them together:

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Dog other = (Dog) obj;
    return age == other.age &&
           Objects.equals(name, other.name) &&
           Objects.equals(breed, other.breed);
}

@Override
public int hashCode() {
    return Objects.hash(name, age, breed);  // Same fields as equals()
}
```

---

## The `instanceof` Operator

`instanceof` checks if an object is an instance of a given class (or its subclasses):

```java
Animal a = new Dog("Rex", 3, "Labrador");

System.out.println(a instanceof Dog);     // true
System.out.println(a instanceof Animal);  // true (Dog IS an Animal)
System.out.println(a instanceof Cat);     // false

// Java 16+ pattern matching instanceof:
if (a instanceof Dog d) {
    // 'd' is automatically cast to Dog
    d.fetch();
}
```

---

## Inheritance Hierarchy: When to Use It

The critical question: when should you use inheritance vs. other approaches?

### The "Is-A" Test

Inheritance represents an "Is-A" relationship. Ask yourself:

> "Is a [Child] always a [Parent]?"

- Is a Dog always an Animal? **YES** → inheritance makes sense
- Is a Car always a Vehicle? **YES** → inheritance makes sense
- Is a Logger always a Manager? **NO** → don't use inheritance just to share utility methods

```java
// GOOD: Is-A relationship
class SavingsAccount extends BankAccount { }  // SavingsAccount IS A BankAccount

// BAD: Has-A relationship forced into Is-A
class Car extends Engine { }  // A Car HAS an Engine, doesn't IS an Engine
```

### Composition vs. Inheritance

For "Has-A" relationships, use **composition** (one class contains an instance of another):

```java
// BAD: Inheritance for a Has-A relationship
class Car extends Engine {
    // A car doesn't "is an" engine
}

// GOOD: Composition for a Has-A relationship
class Car {
    private Engine engine;     // Car HAS an engine
    private Transmission transmission;  // Car HAS a transmission
    
    public Car(Engine engine, Transmission transmission) {
        this.engine = engine;
        this.transmission = transmission;
    }
    
    public void start() {
        engine.start();        // Delegates to the engine
    }
}
```

**Rule of thumb:** Favor composition over inheritance when in doubt.

---

## Inheritance Pitfalls: The Fragile Base Class Problem

Inheritance creates tight coupling between parent and child. When the parent changes, children can break unexpectedly.

```java
// Parent class v1
public class CountingList extends ArrayList<String> {
    private int addCount = 0;
    
    @Override
    public boolean add(String e) {
        addCount++;
        return super.add(e);
    }
    
    @Override
    public boolean addAll(Collection<? extends String> c) {
        addCount += c.size();
        return super.addAll(c);
    }
    
    public int getAddCount() { return addCount; }
}

// Problem: ArrayList's addAll() calls add() internally!
// So addAll(list) of 3 items: adds 3 to addCount via addAll,
// then adds 3 more via each individual add() call
// addCount ends up as 6, not 3 — bug!
```

This is a famous example from Joshua Bloch's *Effective Java*. The lesson: inheritance requires deep knowledge of the parent class's implementation, not just its interface.

---

## Single vs. Multiple Inheritance

Java only allows **single inheritance** — a class can only have one direct parent:

```java
public class FlyingFish extends Fish, Bird { }  // COMPILE ERROR!
```

This is intentional. Multiple inheritance leads to the "diamond problem":

```
     Animal
    /       \
  Fish      Bird
    \       /
    FlyingFish   ← Which Animal.makeSound() do I inherit?
```

Java's solution: single inheritance of implementation, multiple inheritance of **interfaces** (covered in Chapter 6).

---

## Summary

```
Animal (superclass)          Dog (subclass extends Animal)
┌──────────────────┐         ┌──────────────────────────────┐
│ - name: String   │ ──────> │ Inherits: name, age, eat()   │
│ - age: int       │         │ Inherits: sleep(), describe() │
│ + eat(): void    │         │ New: breed, fetch()           │
│ + sleep(): void  │         │ Overrides: makeSound()        │
│ + makeSound()    │         └──────────────────────────────┘
└──────────────────┘
```

---

## Key Takeaways

1. Use `extends` to inherit from a parent class
2. Call `super(args)` in the child constructor to initialize inherited fields
3. Use `@Override` always when overriding — never omit it
4. Override `equals()` and `hashCode()` together, always
5. Apply the "Is-A" test before using inheritance
6. Prefer composition over inheritance for "Has-A" relationships
7. Be aware of the fragile base class problem with deep hierarchies
8. Java enforces single inheritance of classes, but allows multiple interface implementation

---

## What's Next

Now that we understand inheritance, we can explore **polymorphism** — the ability of a single variable to hold objects of different types, and of a single method call to invoke different implementations based on the actual object type.

```java
Animal a = new Dog("Rex", 3, "Lab");  // Reference of type Animal, object is Dog
a.makeSound();  // "Woof!" — calls Dog's version, not Animal's
```

This is one of the most powerful ideas in programming.
