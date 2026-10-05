# Module 03: Object-Oriented Programming

OOP is the heart of Java. This module takes you from "writing code" to "designing systems."

## Prerequisites

Before starting this module, you should be comfortable with:
- Variables, types, and operators
- Control flow (if, for, while, switch)
- Methods and parameters
- Arrays and Strings

## What You'll Learn

The four pillars of OOP:

1. **Encapsulation** — protecting data, controlling access
2. **Inheritance** — building class hierarchies, reusing code
3. **Polymorphism** — one interface, many implementations
4. **Abstraction** — hiding complexity, defining contracts

## Directory Structure

```
03-oop/
├── README.md                     ← You are here
├── theory/                       ← Read first
│   ├── 01-classes-and-objects.md
│   ├── 02-constructors-and-this.md
│   ├── 03-encapsulation.md
│   ├── 04-inheritance.md
│   ├── 05-polymorphism.md
│   ├── 06-abstraction-and-interfaces.md
│   └── 07-oop-in-the-real-world.md
├── code/                         ← Working examples
│   ├── basics/
│   │   ├── BankAccount.java
│   │   └── BankAccountDemo.java
│   ├── inheritance/
│   │   ├── Animal.java
│   │   ├── Dog.java
│   │   ├── Cat.java
│   │   └── AnimalDemo.java
│   └── interfaces/
│       ├── Drawable.java
│       ├── Resizable.java
│       ├── Circle.java
│       ├── Rectangle.java
│       └── ShapesDemo.java
├── exercises/                    ← Practice problems
│   ├── README.md
│   ├── Exercise01_Library.java
│   ├── Exercise02_Vehicles.java
│   ├── Exercise03_PaymentSystem.java
│   └── solutions/
│       ├── Exercise01_Solution/
│       ├── Exercise02_Solution/
│       └── Exercise03_Solution/
├── tests/                        ← JUnit 5 test examples
│   ├── BankAccountTest.java
│   ├── AnimalTest.java
│   └── ShapeTest.java
└── mini-project/                 ← Capstone: School System
    ├── README.md
    └── school-system/
        ├── Person.java
        ├── Student.java
        ├── Teacher.java
        ├── Course.java
        ├── School.java
        └── SchoolDemo.java
```

## Recommended Path

### Day 1: Classes and Encapsulation
1. Read `theory/01-classes-and-objects.md`
2. Read `theory/02-constructors-and-this.md`
3. Read `theory/03-encapsulation.md`
4. Run `code/basics/BankAccountDemo.java`
5. Start `exercises/Exercise01_Library.java`

### Day 2: Inheritance
1. Read `theory/04-inheritance.md`
2. Run `code/inheritance/AnimalDemo.java`
3. Start `exercises/Exercise02_Vehicles.java`

### Day 3: Polymorphism and Abstraction
1. Read `theory/05-polymorphism.md`
2. Read `theory/06-abstraction-and-interfaces.md`
3. Run `code/interfaces/ShapesDemo.java`
4. Start `exercises/Exercise03_PaymentSystem.java`

### Day 4: Put It All Together
1. Read `theory/07-oop-in-the-real-world.md`
2. Study `mini-project/school-system/`
3. Run `SchoolDemo.java`
4. Extend the school system with your own features

## How to Compile and Run Examples

Each directory contains standalone Java files. To run an example:

```bash
# BankAccount demo
cd code/basics
javac BankAccount.java BankAccountDemo.java
java BankAccountDemo

# Animal hierarchy
cd code/inheritance
javac Animal.java Dog.java Cat.java AnimalDemo.java
java AnimalDemo

# Shapes with interfaces
cd code/interfaces
javac Drawable.java Resizable.java Circle.java Rectangle.java ShapesDemo.java
java ShapesDemo

# School system (mini-project)
cd mini-project/school-system
javac Person.java Student.java Teacher.java Course.java School.java SchoolDemo.java
java SchoolDemo
```

## Running Tests (requires JUnit 5)

```bash
# Download JUnit 5 (if not already present)
# Then compile and run tests:
javac -cp junit-5.jar tests/BankAccountTest.java code/basics/BankAccount.java
java -cp .:junit-5.jar org.junit.platform.console.ConsoleLauncher --scan-classpath
```

## Key Concepts Quick Reference

| Concept | Keyword | Example |
|---------|---------|---------|
| Define a class | `class` | `public class Dog { }` |
| Create an object | `new` | `Dog d = new Dog("Rex", 3)` |
| Inheritance | `extends` | `class Dog extends Animal` |
| Interface implementation | `implements` | `class Circle implements Drawable` |
| Abstract class | `abstract` | `public abstract class Animal` |
| Call parent constructor | `super()` | `super(name, age)` |
| Override | `@Override` | `@Override public String makeSound()` |
| Interface | `interface` | `public interface Drawable` |
| Default method | `default` | `default void info() { }` |

## The Four Pillars Summary

```
ENCAPSULATION: private fields + public methods
    → Controls who can change state
    → Enforces business rules
    → Example: BankAccount.balance is private; only deposit()/withdraw() can change it

INHERITANCE: child class extends parent class
    → Reuses code from parent
    → "Is-A" relationship
    → Example: Dog extends Animal (Dog IS an Animal)

POLYMORPHISM: same method, different behavior
    → One variable can hold different subtypes
    → The right method is chosen at runtime
    → Example: animal.makeSound() → "Woof!" if Dog, "Meow!" if Cat

ABSTRACTION: hide complexity, expose simplicity
    → Abstract class: partially implemented template
    → Interface: pure behavioral contract
    → Example: Drawable interface — draw(), hide() — without specifying HOW
```
