# Code Examples

This directory contains working Java code demonstrating all OOP concepts.

## Structure

```
code/
├── basics/               ← Start here
│   ├── BankAccount.java       (full encapsulation, annotated)
│   └── BankAccountDemo.java   (run this to see it in action)
│
├── inheritance/          ← After reading theory/04-inheritance.md
│   ├── Animal.java            (abstract class)
│   ├── Dog.java               (extends Animal, adds Dog behavior)
│   ├── Cat.java               (extends Animal, adds Cat behavior)
│   └── AnimalDemo.java        (demonstrates polymorphism)
│
└── interfaces/           ← After reading theory/06-abstraction-and-interfaces.md
    ├── Drawable.java          (interface with default method)
    ├── Resizable.java         (interface with default methods)
    ├── Circle.java            (implements Drawable + Resizable)
    ├── Rectangle.java         (implements Drawable + Resizable)
    └── ShapesDemo.java        (demonstrates multiple interfaces + polymorphism)
```

## How to Compile and Run

Each subdirectory is independent. Navigate to the directory and compile:

```bash
# From the basics/ directory:
cd basics/
javac BankAccount.java BankAccountDemo.java
java BankAccountDemo

# From the inheritance/ directory:
cd inheritance/
javac Animal.java Dog.java Cat.java AnimalDemo.java
java AnimalDemo

# From the interfaces/ directory:
cd interfaces/
javac Drawable.java Resizable.java Circle.java Rectangle.java ShapesDemo.java
java ShapesDemo
```

## Key Concepts in Each File

### BankAccount.java
- All fields `private` (encapsulation)
- `final` fields for immutable identity (accountNumber, owner)
- Validation in constructor and methods
- Defensive copy in `getTransactionHistory()`
- `equals()` and `hashCode()` overridden consistently
- Commented design decisions throughout

### Animal hierarchy
- `Animal` is `abstract` — cannot instantiate directly
- `makeSound()` is `abstract` — must be overridden
- `describe()` is concrete and uses the abstract `makeSound()` (polymorphism)
- `Dog` and `Cat` call `super(name, age)` in their constructors
- `@Override` used consistently
- `Dog.eat()` calls `super.eat()` to extend (not replace) behavior

### Shape interfaces
- `Drawable` and `Resizable` are separate, focused interfaces
- `Circle` and `Rectangle` implement BOTH
- Default methods provide reusable behavior
- Static method demonstrates interface-level utilities
- `ShapesDemo.drawAll(List<Drawable>)` works for all Drawable types
