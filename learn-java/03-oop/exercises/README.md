# Exercises

These exercises reinforce the OOP concepts from the theory and code examples.

## Instructions

Each exercise file contains:
1. **Clear requirements** for what to build
2. **Design guidance** explaining key decisions
3. **Commented-out tests** to verify your implementation
4. **Stretch goals** for extra challenge

## The Exercises

### Exercise 01: Library System
**File:** `Exercise01_Library.java`
**Concepts:** Encapsulation, class design, collections

Build a library management system with `Book` and `Library` classes.
Focus on proper encapsulation — only `checkOut()` and `returnBook()` should change a book's availability.

### Exercise 02: Vehicle Hierarchy
**File:** `Exercise02_Vehicles.java`
**Concepts:** Inheritance, abstract classes, polymorphism

Build a vehicle hierarchy with `Vehicle` (abstract), `Car`, `ElectricCar`, and `Motorcycle`.
Focus on how abstract methods force subclasses to provide their own implementation.

### Exercise 03: Payment System
**File:** `Exercise03_PaymentSystem.java`
**Concepts:** Interfaces, multiple implementations, Open/Closed Principle

Build a payment system where `ShoppingCart.checkout(Payable payment)` works with any payment type.
This is the cleanest demonstration of why interfaces matter.

## How to Work Through These

1. Read the full exercise file before writing any code
2. Sketch the class structure on paper first (UML or CRC cards)
3. Write one class at a time and test it
4. Uncomment the tests as you implement each feature
5. All tests should pass before moving to stretch goals

## Solutions

Solutions are in the `solutions/` directory. Try to complete each exercise yourself before looking!

- `solutions/Exercise01_Solution/` — Library system solution
- `solutions/Exercise02_Solution/` — Vehicle hierarchy solution
- `solutions/Exercise03_Solution/` — Payment system solution

## What to Focus On

For each exercise, ask yourself:
- Are all fields private?
- Does every public method validate its inputs?
- Am I using inheritance because of an "Is-A" relationship?
- Does polymorphism make my code more flexible?
- Would adding a new type require changing existing code? (It shouldn't)
