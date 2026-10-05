# Scope and Memory

Understanding where variables live — and when they disappear — is fundamental to writing correct Java programs. This guide connects the code you write to what actually happens in memory.

---

## Variable Scope

**Scope** is the region of code where a variable exists and is accessible. Once execution leaves that region, the variable is gone.

### Local Variables

A **local variable** is declared inside a method (including its parameters). It exists only during that method's execution.

```java
public static void exampleMethod() {
    int x = 10;       // local variable — exists only inside this method
    System.out.println(x);
} // x ceases to exist here

public static void main(String[] args) {
    exampleMethod();
    // System.out.println(x); // COMPILE ERROR — x doesn't exist here
}
```

Local variables have **no default value**. Java forces you to initialize them before use:

```java
int count;
System.out.println(count); // COMPILE ERROR: variable count might not have been initialized
```

This is a safety feature — it prevents you from accidentally reading garbage values.

### Instance Variables

An **instance variable** is declared in a class but outside any method. It belongs to an *object* and lives as long as the object does.

```java
public class Person {
    String name;   // instance variable — one per Person object
    int age;       // instance variable — one per Person object
    
    public void introduce() {
        // Can access instance variables without any prefix
        System.out.println("I'm " + name + ", age " + age);
    }
}
```

Instance variables DO have default values: `0` for numeric types, `false` for boolean, `null` for objects.

### Class (Static) Variables

A **static variable** is declared with the `static` keyword. It belongs to the *class itself*, not any individual object. There is exactly one copy, shared among all instances.

```java
public class Counter {
    static int count = 0; // class variable — shared by ALL Counter objects
    
    public Counter() {
        count++; // increment the shared count each time a Counter is created
    }
}

Counter c1 = new Counter();
Counter c2 = new Counter();
System.out.println(Counter.count); // 2
```

---

## Block Scope

Variables declared inside a block `{ }` exist only within that block:

```java
for (int i = 0; i < 3; i++) {
    int doubled = i * 2;    // exists only inside this loop body
    System.out.println(doubled);
}
// System.out.println(i);       // COMPILE ERROR — i is out of scope
// System.out.println(doubled); // COMPILE ERROR — doubled is out of scope

if (true) {
    int temp = 42;
    System.out.println(temp); // fine here
}
// System.out.println(temp); // COMPILE ERROR — temp is out of scope
```

---

## Stack vs Heap

Java uses two regions of memory for storing data:

### The Stack

- Stores **method frames** (local variables + parameters)
- Operates like a stack of plates: last in, first out (LIFO)
- Allocation and deallocation are automatic and fast
- Has limited size (typically 512KB–1MB)

### The Heap

- Stores **objects** (everything created with `new`)
- Managed by the **Garbage Collector** (GC) — no manual deallocation
- Much larger than the stack
- Slightly slower to allocate than the stack

### Where does each type go?

| Type | Where it lives |
|------|---------------|
| Primitive local variable | Stack (in the method's frame) |
| Object reference (local) | Reference is on the Stack; the object it points to is on the Heap |
| Instance variable | Heap (inside the object) |
| Static variable | Special "class area" (part of the Heap in modern JVM) |

---

## What Happens When a Method is Called

Every method call creates a **stack frame** — a block of memory on the stack that contains:
- The method's parameters
- The method's local variables
- The return address (where to go when the method finishes)

### Step-by-step trace

```java
public static void main(String[] args) {
    int a = 5;
    int b = 3;
    int result = add(a, b);          // 1. call add()
    System.out.println(result);       // 4. execute after add() returns
}

public static int add(int x, int y) {  // 2. stack frame created for add()
    int sum = x + y;                    // 3. local variable sum = 8
    return sum;                         // 4. frame removed, returns 8
}
```

Stack state at the moment `add()` is running:

```
TOP (most recent)
┌──────────────────────────────┐
│ Frame: add()                 │
│   x   = 5  (parameter copy) │
│   y   = 3  (parameter copy) │
│   sum = 8  (local variable)  │
├──────────────────────────────┤
│ Frame: main()                │
│   args   = [...]             │
│   a      = 5                 │
│   b      = 3                 │
│   result = ? (not yet set)   │
└──────────────────────────────┘
BOTTOM
```

When `add()` returns:
1. The return value `8` is passed back
2. The `add()` frame is **popped off** the stack and its memory is freed
3. `result = 8` is set in `main()`'s frame

---

## What Happens When a Method Returns

When `return` executes:
1. The return value (if any) is placed where the caller can receive it
2. The current method's stack frame is **destroyed** — all local variables vanish
3. Execution resumes in the calling method at the point after the call

This is why local variables don't persist between calls:

```java
public static void count() {
    int n = 0;   // created fresh each call
    n++;
    System.out.println(n); // always prints 1
}

count(); // 1
count(); // 1 — n was NOT preserved from the previous call
count(); // 1 — each call gets its own fresh stack frame
```

---

## Why Local Variables Must Be Initialized

The compiler won't let you read a local variable before assigning it a value:

```java
int x;

if (condition) {
    x = 5;
}

System.out.println(x); // COMPILE ERROR: x might not have been initialized
```

Even though `x = 5` might run, the compiler can't guarantee it will (it depends on `condition`). So it forces you to ensure initialization before use.

The fix:

```java
int x = 0; // default value makes it always initialized

if (condition) {
    x = 5;
}

System.out.println(x); // OK — x is definitely initialized
```

---

## Visualizing Object References

When you create an object, the variable holds a *reference* (memory address), not the object itself:

```java
String s = "hello";
//    ↑         ↑
// reference   object on the heap
//  (stack)
```

```
Stack                 Heap
┌──────────────┐      ┌──────────────────────┐
│ s = [0x1F2A] │ ──→  │ String "hello"       │
└──────────────┘      │ at address 0x1F2A    │
                      └──────────────────────┘
```

When you do `String t = s;`, you copy the reference, not the object:

```
Stack                 Heap
┌──────────────┐      ┌──────────────────────┐
│ s = [0x1F2A] │ ──┐  │ String "hello"       │
│ t = [0x1F2A] │ ──┘→ │ at address 0x1F2A    │
└──────────────┘      └──────────────────────┘
```

Both `s` and `t` point to the same `String` object. Because `String` is immutable, this is perfectly safe.

---

## Garbage Collection

Java automatically reclaims heap memory for objects that are no longer reachable. You don't call `free()` or `delete` — the **Garbage Collector** (GC) handles it.

An object becomes eligible for collection when there are no more references pointing to it:

```java
String s = new String("hello"); // object created on heap, referenced by s
s = "world";                     // s now points to "world"; "hello" is unreferenced
                                  // "hello" is now eligible for garbage collection
```

You generally don't need to think about GC — it happens automatically. But understanding it helps you avoid memory leaks (which occur when you keep references to objects you no longer need).

---

## Summary

| Concept | Key Point |
|---------|-----------|
| Local scope | Exists only while the method runs |
| Block scope | Exists only within `{ }` |
| Static scope | Exists for the lifetime of the program |
| Stack | Stores frames; automatic, fast, limited |
| Heap | Stores objects; GC-managed, larger |
| Initialization | Local variables must be set before reading |
| Garbage Collection | Unreachable objects are automatically freed |

This understanding will serve you throughout your Java journey — especially when debugging `NullPointerException` and understanding why some operations affect the original object and others don't.
