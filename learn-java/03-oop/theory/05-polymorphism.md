# Chapter 5: Polymorphism

## Poly = Many, Morph = Forms

Polymorphism (from Greek: "many forms") means that a single thing can take multiple forms. In Java, this means:
- A variable of type `Animal` can hold a `Dog`, a `Cat`, or any other Animal subclass
- A method call like `animal.makeSound()` can invoke different code depending on the actual object type

This sounds simple, but it's one of the most powerful ideas in all of programming.

---

## Compile-Time Polymorphism: Method Overloading

The first form of polymorphism is **method overloading** — multiple methods with the same name but different parameters. The compiler decides which one to call at compile time (hence "compile-time"):

```java
public class Calculator {
    // Same name "add", different parameter types
    public int add(int a, int b) {
        return a + b;
    }
    
    public double add(double a, double b) {
        return a + b;
    }
    
    public int add(int a, int b, int c) {
        return a + b + c;
    }
    
    public String add(String a, String b) {
        return a + b;  // String concatenation
    }
}

Calculator calc = new Calculator();
calc.add(1, 2);           // Calls add(int, int) → 3
calc.add(1.5, 2.5);       // Calls add(double, double) → 4.0
calc.add(1, 2, 3);        // Calls add(int, int, int) → 6
calc.add("Hello", " World");  // Calls add(String, String) → "Hello World"
```

The compiler looks at the argument types and selects the matching method. This decision is made at **compile time** — before the program even runs.

Rules for valid overloading:
- Must differ in number of parameters, OR type of parameters, OR both
- Return type alone is NOT enough (two methods with same parameters but different return types → compile error)

---

## Runtime Polymorphism: The Real Power

**Runtime polymorphism** (also called **dynamic dispatch**) is the more powerful form. The method to call is decided at **runtime** based on the actual type of the object.

### Setting the Scene

```java
public abstract class Animal {
    protected String name;
    
    public Animal(String name) {
        this.name = name;
    }
    
    public abstract String makeSound();  // Must be implemented by subclasses
    
    public void describeYourself() {
        System.out.println("I am " + name + " and I say: " + makeSound());
    }
}

public class Dog extends Animal {
    public Dog(String name) { super(name); }
    
    @Override
    public String makeSound() { return "Woof!"; }
}

public class Cat extends Animal {
    public Cat(String name) { super(name); }
    
    @Override
    public String makeSound() { return "Meow!"; }
}

public class Cow extends Animal {
    public Cow(String name) { super(name); }
    
    @Override
    public String makeSound() { return "Moo!"; }
}
```

### The Polymorphic Variable

A variable declared as `Animal` can hold any object that IS an Animal:

```java
Animal animal;             // Reference type is Animal

animal = new Dog("Rex");   // Object type is Dog — this is called UPCASTING
animal.makeSound();        // "Woof!" — calls Dog's version!

animal = new Cat("Whiskers");  // Same variable, now points to a Cat
animal.makeSound();            // "Meow!" — calls Cat's version!

animal = new Cow("Bessie");    // Now points to a Cow
animal.makeSound();            // "Moo!" — calls Cow's version!
```

The **reference type** (`Animal`) determines what methods you can CALL.
The **actual object type** (`Dog`, `Cat`, `Cow`) determines WHICH implementation runs.

---

## Dynamic Dispatch: How It Works

When you call `animal.makeSound()`, Java doesn't look at the declared type of `animal` to decide which code to run. Instead, it looks at the actual object at runtime:

```
Runtime call: animal.makeSound()
                    │
                    ▼
         What type is the actual object?
                    │
         ┌─────────────────────┐
         │  is it a Dog?  → "Woof!"  │
         │  is it a Cat?  → "Meow!"  │
         │  is it a Cow?  → "Moo!"   │
         └─────────────────────┘
```

This is dynamic dispatch — the dispatch of the method call is dynamic (happens at runtime, not compile time).

---

## The Power of Polymorphism: Write to the Interface

Here's where polymorphism becomes transformative. Instead of writing code for each specific type, you write code that works with any compatible type:

```java
// Without polymorphism — you need a case for every type:
void processAnimal(Object animal) {
    if (animal instanceof Dog) {
        ((Dog) animal).makeSound();
    } else if (animal instanceof Cat) {
        ((Cat) animal).makeSound();
    } else if (animal instanceof Cow) {
        ((Cow) animal).makeSound();
    }
    // Add a new animal type? Must modify this method!
}

// WITH polymorphism — works for ALL Animal subclasses:
void processAnimal(Animal animal) {
    System.out.println(animal.makeSound());  // Just works!
    // Add Lion, Tiger, Bear? This method needs ZERO changes!
}
```

And with arrays/collections:

```java
// A zoo full of different animals
Animal[] zoo = {
    new Dog("Rex"),
    new Cat("Whiskers"),
    new Cow("Bessie"),
    new Dog("Buddy"),
    new Cat("Luna")
};

// Make them all speak — works for any animal type
for (Animal animal : zoo) {
    animal.describeYourself();
}
// Output:
// I am Rex and I say: Woof!
// I am Whiskers and I say: Meow!
// I am Bessie and I say: Moo!
// I am Buddy and I say: Woof!
// I am Luna and I say: Meow!
```

---

## Real-World Example: Payment System

This is the classic example of polymorphism at its most useful:

```java
public interface Payable {
    void process(double amount);
    String getPaymentMethod();
}

public class CreditCardPayment implements Payable {
    private String cardNumber;
    
    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void process(double amount) {
        System.out.printf("Charging $%.2f to credit card ending in %s%n",
                         amount, cardNumber.substring(cardNumber.length() - 4));
    }
    
    @Override
    public String getPaymentMethod() { return "Credit Card"; }
}

public class PayPalPayment implements Payable {
    private String email;
    
    public PayPalPayment(String email) {
        this.email = email;
    }
    
    @Override
    public void process(double amount) {
        System.out.printf("Processing $%.2f via PayPal account: %s%n", amount, email);
    }
    
    @Override
    public String getPaymentMethod() { return "PayPal"; }
}

public class CryptoPayment implements Payable {
    private String walletAddress;
    private String currency;
    
    public CryptoPayment(String walletAddress, String currency) {
        this.walletAddress = walletAddress;
        this.currency = currency;
    }
    
    @Override
    public void process(double amount) {
        System.out.printf("Sending %.6f %s to wallet %s%n",
                         amount / 50000, currency, walletAddress);
    }
    
    @Override
    public String getPaymentMethod() { return currency + " Crypto"; }
}

// The checkout process — works with ANY payment method
public class ShoppingCart {
    private List<Double> items = new ArrayList<>();
    
    public void addItem(double price) {
        items.add(price);
    }
    
    public double getTotal() {
        return items.stream().mapToDouble(Double::doubleValue).sum();
    }
    
    // This method works for CreditCard, PayPal, Crypto, or ANY future payment type
    public void checkout(Payable payment) {
        double total = getTotal();
        System.out.println("Checkout using: " + payment.getPaymentMethod());
        payment.process(total);
        System.out.printf("Payment of $%.2f complete!%n", total);
    }
}

// Usage:
ShoppingCart cart = new ShoppingCart();
cart.addItem(29.99);
cart.addItem(49.99);

// Pay with different methods — same checkout() code handles all
cart.checkout(new CreditCardPayment("1234-5678-9012-3456"));
cart.checkout(new PayPalPayment("alice@example.com"));
cart.checkout(new CryptoPayment("0xABC123...", "BTC"));
```

If the business adds "Apple Pay" next year:
```java
public class ApplePayPayment implements Payable {
    @Override
    public void process(double amount) { ... }
    @Override
    public String getPaymentMethod() { return "Apple Pay"; }
}

// ShoppingCart.checkout() needs NO changes!
cart.checkout(new ApplePayPayment("device-id-123"));
```

This is the **Open/Closed Principle**: open for extension (add new types), closed for modification (existing code unchanged).

---

## Upcasting and Downcasting

### Upcasting (implicit, always safe)

Treating a more specific type as a more general type:

```java
Dog rex = new Dog("Rex", 3);
Animal animal = rex;  // Upcast — automatic, no cast needed
// animal can only call Animal methods now
// even though the underlying object is a Dog
```

Upcasting is always safe because a Dog is guaranteed to be an Animal.

### Downcasting (explicit, requires care)

Going from a general type back to a specific type:

```java
Animal animal = new Dog("Rex", 3);  // We know it's a Dog

// Downcast — explicit cast required
Dog dog = (Dog) animal;  // We're telling the compiler: "trust me, it's a Dog"
dog.fetch();             // Now we can access Dog-specific methods

// DANGEROUS: downcasting to wrong type
Animal animal2 = new Cat("Whiskers");
Dog dog2 = (Dog) animal2;  // Compiles but throws ClassCastException at runtime!
```

### Safe downcasting with `instanceof`

```java
Animal animal = getAnimal();  // returns some animal, don't know which type

// Check before cast
if (animal instanceof Dog dog) {  // Java 16+ pattern matching
    dog.fetch();
}

// Older syntax (Java < 16):
if (animal instanceof Dog) {
    Dog dog = (Dog) animal;
    dog.fetch();
}
```

---

## Polymorphism with Collections

One of the most practical uses of polymorphism is storing diverse objects in collections:

```java
// A list that can hold any Shape (polymorphic collection)
List<Shape> shapes = new ArrayList<>();
shapes.add(new Circle(5.0));
shapes.add(new Rectangle(4.0, 6.0));
shapes.add(new Triangle(3.0, 4.0, 5.0));
shapes.add(new Circle(2.5));

// Calculate total area — works for all shape types
double totalArea = 0;
for (Shape shape : shapes) {
    totalArea += shape.getArea();  // Dynamic dispatch!
}
System.out.println("Total area: " + totalArea);

// Find the largest shape
Shape largest = shapes.get(0);
for (Shape shape : shapes) {
    if (shape.getArea() > largest.getArea()) {
        largest = shape;
    }
}
System.out.println("Largest: " + largest);
```

---

## Liskov Substitution Principle

Named after Barbara Liskov, this principle states:

> **Any code that uses a Parent class should work correctly if a Child class is substituted for the Parent.**

In other words: a subclass should be a proper substitute for its parent.

```java
// GOOD — Square IS a proper Rectangle (in geometry)
// Wait... or is it?

class Rectangle {
    protected double width;
    protected double height;
    
    public void setWidth(double w) { this.width = w; }
    public void setHeight(double h) { this.height = h; }
    
    public double getArea() { return width * height; }
}

class Square extends Rectangle {
    @Override
    public void setWidth(double w) {
        this.width = w;
        this.height = w;  // A square must have equal sides
    }
    
    @Override
    public void setHeight(double h) {
        this.width = h;   // Must also update width
        this.height = h;
    }
}

// This test method should work for any Rectangle:
void testRectangle(Rectangle r) {
    r.setWidth(5);
    r.setHeight(4);
    assert r.getArea() == 20 : "Expected 20, got " + r.getArea();
}

testRectangle(new Rectangle());  // PASSES: area = 5 * 4 = 20
testRectangle(new Square());     // FAILS: area = 4 * 4 = 16!
// Square breaks the contract of Rectangle — LSP violation!
```

The lesson: just because a mathematical square IS a rectangle doesn't mean a `Square` class SHOULD extend `Rectangle` in code. Inheritance must preserve behavioral contracts, not just conceptual relationships.

---

## Method Overloading vs Method Overriding

Common source of confusion:

| Aspect | Overloading | Overriding |
|--------|-------------|------------|
| Class | Same class | Parent and child class |
| Method name | Same | Same |
| Parameters | Different | Same |
| Return type | Can differ | Same (or subtype) |
| Polymorphism | Compile-time | Runtime |
| Annotation | None needed | `@Override` |
| `static` OK? | Yes | No (static methods are hidden, not overridden) |

```java
class Printer {
    // OVERLOADING — same class, different parameters
    public void print(String text) { ... }
    public void print(int number) { ... }
    public void print(String text, boolean bold) { ... }
}

class ColorPrinter extends Printer {
    // OVERRIDING — child class, same signature
    @Override
    public void print(String text) {
        // Different implementation
    }
}
```

---

## Summary

Polymorphism enables you to:

1. **Write flexible code** — methods that accept `Animal` work for Dog, Cat, Cow, and any future animal
2. **Avoid if-else chains** — no need to check the type and branch
3. **Support extension without modification** — add new types without changing existing code
4. **Express the right level of abstraction** — work with the concept (Animal, Payment) not the specifics (Dog, CreditCard)

---

## Key Takeaways

1. Method overloading = compile-time polymorphism (same method name, different params)
2. Method overriding = runtime polymorphism (subclass provides its own implementation)
3. Dynamic dispatch = the JVM decides which method to call based on the actual object type
4. Upcasting is automatic and always safe; downcasting requires explicit cast and `instanceof` check
5. Write methods that take the most general type that makes sense (prefer `Animal` over `Dog`)
6. Liskov Substitution Principle: a subclass must be behaviorally substitutable for its parent

---

## What's Next

We've seen how abstract base classes enable polymorphism. The next chapter covers **abstraction** in depth — abstract classes and interfaces — which are the formal mechanisms for defining the contracts that make polymorphism work.
