# Chapter 6: Abstraction and Interfaces

## Abstraction: Hide Complexity, Expose Simplicity

When you drive a car, you use a steering wheel, pedals, and gear shift. You don't think about:
- How the fuel injection system works
- How the anti-lock brakes calculate slip ratios
- How the transmission manages gear ratios

All that complexity is hidden. You interact with a **simplified interface** that exposes just what you need.

This is **abstraction** — concealing implementation complexity behind a clean, simple interface.

In Java, abstraction is achieved through:
1. **Abstract classes** — partially implemented blueprints
2. **Interfaces** — pure behavioral contracts

---

## Abstract Classes

An **abstract class** is a class that:
- Cannot be instantiated directly (you can't do `new Animal()`)
- Can have abstract methods (declared but not implemented)
- Can also have concrete methods (fully implemented)
- Can have fields

Use an abstract class when:
- You want to provide some common implementation that subclasses share
- You want to enforce that subclasses implement certain methods
- The class represents a concept that shouldn't exist on its own

```java
public abstract class Animal {
    protected String name;
    protected int age;
    
    // Constructor — even abstract classes have constructors
    // (called by subclass constructors via super())
    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    // Abstract method — NO body, just declaration with semicolon
    // Every concrete subclass MUST implement this
    public abstract String makeSound();
    
    // Abstract method — subclasses must also provide this
    public abstract String getType();
    
    // Concrete method — fully implemented, shared by all subclasses
    public void sleep() {
        System.out.println(name + " is sleeping. Zzz...");
    }
    
    // Concrete method that USES the abstract method
    // Works because subclass will provide the implementation
    public void describe() {
        System.out.printf("%s is a %s who says '%s'%n", 
                         name, getType(), makeSound());
    }
    
    // Getters
    public String getName() { return name; }
    public int getAge() { return age; }
}

// Concrete subclass — MUST implement all abstract methods
public class Dog extends Animal {
    private String breed;
    
    public Dog(String name, int age, String breed) {
        super(name, age);
        this.breed = breed;
    }
    
    @Override
    public String makeSound() { return "Woof!"; }  // Required implementation
    
    @Override
    public String getType() { return "Dog (" + breed + ")"; }  // Required
    
    // Dog-specific method
    public void fetch() {
        System.out.println(name + " fetches the ball!");
    }
}

// Another concrete subclass
public class Cat extends Animal {
    private boolean isIndoor;
    
    public Cat(String name, int age, boolean isIndoor) {
        super(name, age);
        this.isIndoor = isIndoor;
    }
    
    @Override
    public String makeSound() { return "Meow!"; }
    
    @Override
    public String getType() { 
        return isIndoor ? "Indoor Cat" : "Outdoor Cat"; 
    }
}

// Usage:
// Animal a = new Animal("Generic", 0);  // COMPILE ERROR: Animal is abstract!

Dog rex = new Dog("Rex", 3, "German Shepherd");
rex.describe();   // Rex is a Dog (German Shepherd) who says 'Woof!'
rex.sleep();      // Rex is sleeping. Zzz...
rex.fetch();      // Rex fetches the ball!

Cat luna = new Cat("Luna", 2, true);
luna.describe();  // Luna is a Indoor Cat who says 'Meow!'
```

---

## Abstract Classes in Template Method Pattern

One of the most elegant uses of abstract classes is the Template Method Pattern — defining an algorithm's skeleton in the abstract class, letting subclasses fill in specific steps:

```java
public abstract class DataProcessor {
    // Template method — defines the algorithm structure
    public final void process() {   // 'final' prevents overriding the template
        readData();
        processData();
        writeResults();
        cleanup();
    }
    
    protected abstract void readData();      // Subclass provides
    protected abstract void processData();   // Subclass provides
    
    protected void writeResults() {          // Default implementation
        System.out.println("Writing results...");
    }
    
    protected void cleanup() {               // Default — can be overridden
        System.out.println("Cleanup complete.");
    }
}

public class CsvProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("Reading CSV file...");
    }
    
    @Override
    protected void processData() {
        System.out.println("Processing CSV data...");
    }
}

public class DatabaseProcessor extends DataProcessor {
    @Override
    protected void readData() {
        System.out.println("Querying database...");
    }
    
    @Override
    protected void processData() {
        System.out.println("Processing database records...");
    }
    
    @Override
    protected void writeResults() {  // Override default behavior
        System.out.println("Writing results back to database...");
    }
}
```

---

## Interfaces: Pure Contracts

An **interface** defines a contract — a set of methods that any implementing class promises to provide. Think of it as a purely behavioral specification.

The USB analogy:
- USB is an interface (a contract/standard)
- Your keyboard, mouse, phone charger all "implement" USB
- Any device implementing USB works with any USB port
- The USB port doesn't care what device is connected — as long as it speaks USB

```java
// Interface — defines the contract
public interface Drawable {
    void draw();           // Abstract by default (no body needed)
    void hide();
    
    // Default method (Java 8+) — has a body, inherited by implementers
    default String getStatus() {
        return "Drawable object";
    }
    
    // Static method in interface (Java 8+)
    static boolean isValid(Drawable d) {
        return d != null;
    }
    
    // Constants are implicitly public static final
    double DEFAULT_OPACITY = 1.0;
}

public interface Resizable {
    void resize(double factor);
    
    default void doubleSize() {
        resize(2.0);
    }
    
    default void halfSize() {
        resize(0.5);
    }
}

// A class can implement MULTIPLE interfaces
public class Circle implements Drawable, Resizable {
    private double x, y;
    private double radius;
    
    public Circle(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    @Override
    public void draw() {
        System.out.printf("Drawing circle at (%.1f, %.1f) with radius %.1f%n",
                         x, y, radius);
    }
    
    @Override
    public void hide() {
        System.out.println("Hiding circle.");
    }
    
    @Override
    public void resize(double factor) {
        radius *= factor;
        System.out.printf("Circle resized. New radius: %.1f%n", radius);
    }
    
    public double getArea() {
        return Math.PI * radius * radius;
    }
}

// Usage:
Circle c = new Circle(0, 0, 5.0);
c.draw();        // Implemented by Circle
c.hide();        // Implemented by Circle
c.doubleSize();  // Default method from Resizable
c.draw();        // Circle at new size
c.getStatus();   // Default method from Drawable
```

---

## Abstract Class vs Interface: When to Use Which

This is the most important decision in this chapter:

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| Can have fields | YES | Only constants (public static final) |
| Can have constructors | YES | NO |
| Can have non-abstract methods | YES | Only default/static (Java 8+) |
| Inheritance | Single (extends one) | Multiple (implements many) |
| When to use | Shared implementation + IS-A | Behavioral contract + CAPABLE-OF |

### Use an Abstract Class when:
- Subclasses share substantial implementation code
- You need constructors, instance fields, or protected methods
- The relationship is "is-a" (Dog is-a Animal)
- You're modeling a hierarchy where the parent "type" makes sense

### Use an Interface when:
- Defining a capability (can-fly, is-comparable, can-draw)
- Multiple inheritance of behavior is needed
- You want to decouple the contract from the implementation
- Different, unrelated classes should share a common API

```java
// Abstract class — shared state and behavior
abstract class Vehicle {
    protected String make;
    protected String model;
    protected double fuelLevel;
    
    // Shared implementation
    public void refuel(double amount) {
        fuelLevel = Math.min(1.0, fuelLevel + amount);
    }
    
    // Required by subclasses
    public abstract void start();
    public abstract double calculateFuelEfficiency();
}

// Interface — capability contract
interface Trackable {
    Location getCurrentLocation();
    void setLocation(Location location);
    List<Location> getHistory();
}

interface Insurable {
    double calculatePremium();
    String getPolicyNumber();
}

// Car is a Vehicle (abstract class) AND is Trackable and Insurable (interfaces)
class Car extends Vehicle implements Trackable, Insurable {
    private Location location;
    private List<Location> locationHistory = new ArrayList<>();
    private String policyNumber;
    
    @Override
    public void start() {
        System.out.println("Car starts with key ignition");
    }
    
    @Override
    public double calculateFuelEfficiency() {
        return 30.0; // mpg
    }
    
    @Override
    public Location getCurrentLocation() {
        return location;
    }
    
    @Override
    public void setLocation(Location loc) {
        locationHistory.add(loc);
        this.location = loc;
    }
    
    @Override
    public List<Location> getHistory() {
        return Collections.unmodifiableList(locationHistory);
    }
    
    @Override
    public double calculatePremium() {
        return 1200.0;
    }
    
    @Override
    public String getPolicyNumber() {
        return policyNumber;
    }
}
```

---

## Multiple Interface Implementation: Java's Answer to Multiple Inheritance

Java only allows extending one class, but you can implement as many interfaces as you want:

```java
public class Rectangle implements Drawable, Resizable, Comparable<Rectangle> {
    private double width, height;
    
    @Override
    public void draw() { System.out.println("Drawing rectangle"); }
    
    @Override
    public void hide() { System.out.println("Hiding rectangle"); }
    
    @Override
    public void resize(double factor) {
        width *= factor;
        height *= factor;
    }
    
    @Override
    public int compareTo(Rectangle other) {
        return Double.compare(this.getArea(), other.getArea());
    }
    
    public double getArea() { return width * height; }
}

// Now Rectangle can be used as Drawable, Resizable, or Comparable:
Drawable d = new Rectangle(4, 6);
d.draw();

Resizable r = new Rectangle(4, 6);
r.doubleSize();

List<Rectangle> rects = new ArrayList<>();
Collections.sort(rects);  // Works because Rectangle is Comparable
```

---

## Default Methods and the Diamond Problem

When two interfaces have the same default method, the implementing class must resolve the conflict:

```java
interface A {
    default String hello() { return "Hello from A"; }
}

interface B {
    default String hello() { return "Hello from B"; }
}

class C implements A, B {
    // MUST override — compiler forces you to resolve the conflict
    @Override
    public String hello() {
        return A.super.hello() + " and " + B.super.hello();
        // Or: return "Hello from C";
    }
}
```

---

## Functional Interfaces

A **functional interface** has exactly one abstract method. These are the basis for lambda expressions (covered in later modules):

```java
@FunctionalInterface  // Optional annotation — enforces one abstract method
public interface Runnable {
    void run();  // Single abstract method
}

@FunctionalInterface
public interface Comparable<T> {
    int compareTo(T other);  // Single abstract method
}

@FunctionalInterface
public interface Comparator<T> {
    int compare(T a, T b);  // Single abstract method
    // (default and static methods don't count)
}

// All built into Java:
// - Runnable: tasks that can be run
// - Callable: tasks that return a value
// - Comparable: objects with a natural ordering
// - Comparator: external ordering strategy
// - Iterator: step through a collection
// - ActionListener: respond to UI events
```

Using a functional interface with a lambda (preview):
```java
Runnable task = () -> System.out.println("Hello from a lambda!");
task.run();  // Hello from a lambda!
```

---

## Interface Segregation Principle

Don't create huge interfaces with many unrelated methods. Split them into focused, specific interfaces:

```java
// BAD: Fat interface that forces implementers to provide unrelated methods
interface AllInOne {
    void print();
    void scan();
    void fax();
    void copy();
    void staple();
}

// A simple printer must implement fax() and staple() even though it can't do them!
class SimplePrinter implements AllInOne {
    @Override
    public void print() { System.out.println("Printing..."); }
    
    @Override
    public void scan() { throw new UnsupportedOperationException(); }  // Can't scan!
    
    @Override
    public void fax() { throw new UnsupportedOperationException(); }    // Can't fax!
    
    // etc...
}

// GOOD: Segregated interfaces — implement only what you support
interface Printable {
    void print();
}

interface Scannable {
    void scan();
}

interface Faxable {
    void fax();
}

class SimplePrinter implements Printable {
    @Override
    public void print() { System.out.println("Printing..."); }
}

class AllInOnePrinter implements Printable, Scannable, Faxable {
    @Override
    public void print() { System.out.println("Printing..."); }
    
    @Override
    public void scan() { System.out.println("Scanning..."); }
    
    @Override
    public void fax() { System.out.println("Faxing..."); }
}
```

---

## Evolution: How Interfaces Changed in Java 8

Before Java 8, interfaces could only have abstract methods. Adding a method to an interface would break all implementing classes.

Java 8+ introduced:
- **Default methods**: have a body, implementing classes can override or inherit
- **Static methods**: utility methods belonging to the interface itself

```java
public interface Collection<E> {
    // Abstract methods (still there)
    int size();
    boolean isEmpty();
    boolean add(E e);
    
    // Default method (Java 8+) — doesn't break existing implementations
    default void forEach(Consumer<? super E> action) {
        for (E e : this) {
            action.accept(e);
        }
    }
    
    // Static utility method
    static <E> Collection<E> emptyCollection() {
        return Collections.emptyList();
    }
}
```

---

## The `implements` Keyword vs `extends`

| Keyword | Used With | Allows Multiple? |
|---------|-----------|-----------------|
| `extends` | Class extending a class | No (single inheritance) |
| `extends` | Interface extending an interface | Yes! |
| `implements` | Class implementing interfaces | Yes |

```java
// Interface inheriting from multiple interfaces
interface FullyCapable extends Drawable, Resizable, Serializable {
    void doEverything();
}

// Class implementing multiple interfaces, extending one class
class SuperShape extends Shape implements Drawable, Resizable, Comparable<SuperShape> {
    // ...
}
```

---

## Summary

```
ABSTRACTION TOOLS IN JAVA
│
├── Abstract Class
│   ├── Has abstract methods (must be implemented by subclass)
│   ├── Can have concrete methods (shared implementation)
│   ├── Can have fields and constructors
│   ├── One per class hierarchy (single inheritance)
│   └── Use for: "is-a" with shared code
│
└── Interface
    ├── All methods abstract by default (unless default/static)
    ├── No instance fields (only constants)
    ├── No constructors
    ├── Multiple per class (multiple interface implementation)
    └── Use for: "can-do" behavioral contracts
```

---

## Key Takeaways

1. Abstract classes cannot be instantiated — they exist to be extended
2. Abstract methods in abstract classes force subclasses to implement them
3. Interfaces define behavioral contracts — what a type can do, not what it is
4. A class can extend only one class but implement many interfaces
5. Default methods (Java 8+) allow interfaces to evolve without breaking implementations
6. Prefer interfaces for defining capabilities; use abstract classes for shared implementation
7. Keep interfaces focused (Interface Segregation Principle)
8. Functional interfaces (one abstract method) enable lambda expressions

---

## What's Next

With all four OOP pillars covered (encapsulation, inheritance, polymorphism, abstraction), the final chapter shows how these concepts apply in real-world systems — how Netflix, Amazon, and banks model their domains using exactly the techniques you've just learned.
