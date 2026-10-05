# Java Core Interview Questions

This module contains 50 essential Java core interview questions organized by topic. Whether you are preparing for a junior, mid-level, or senior Java developer interview, these questions cover the fundamental concepts that interviewers consistently ask about.

## Topics Covered

### 1. JVM and Memory Model (Q1, Q13, Q49, Q50)
- JDK vs JRE vs JVM architecture
- Garbage collection algorithms
- Stack vs heap memory
- Memory errors (OOM vs StackOverflow)

### 2. Strings (Q2, Q3, Q4, Q39, Q40)
- String immutability and the String pool
- String equality pitfalls
- StringBuilder vs StringBuffer
- String.intern()

### 3. Collections Framework (Q6, Q7, Q8, Q9, Q28, Q29, Q30)
- ArrayList vs LinkedList internals
- HashMap deep dive (buckets, load factor, treeification)
- HashMap vs HashTable vs ConcurrentHashMap
- Iterator fail-fast vs fail-safe behavior
- Set implementations: HashSet, TreeSet, LinkedHashSet

### 4. Exceptions (Q10, Q11, Q12)
- Checked vs unchecked exceptions
- throw vs throws keywords
- try-with-resources and AutoCloseable

### 5. OOP Basics (Q14-Q17, Q42-Q46)
- final / finally / finalize
- Overloading vs overriding
- static keyword
- Abstract class vs interface
- Enums with methods and fields
- Deep copy vs shallow copy
- instanceof and pattern matching

### 6. Generics (Q18, Q19)
- Why generics exist
- Type erasure mechanism

### 7. Java 8+ Features (Q20-Q27)
- Lambda expressions
- Functional interfaces
- Stream API
- Optional
- Method references
- Default methods in interfaces

### 8. Concurrency (Q33-Q38)
- volatile keyword
- synchronized methods and blocks
- Deadlock detection and prevention
- Thread vs Runnable
- ExecutorService
- Java Memory Model happens-before

### 9. Misc and Advanced (Q31, Q32, Q41, Q47, Q48)
- Serializable vs Externalizable
- transient keyword
- Reflection
- var keyword (Java 10)
- Sealed classes (Java 17)

---

## How to Study

**Step 1: Read Before You Practice.** Read each answer fully. The explanation paragraphs contain the nuance that differentiates good answers from great answers.

**Step 2: Practice Out Loud.** Close the answers file and try to answer each question verbally. Time yourself: aim for 1 to 3 minutes per answer.

**Step 3: Write Code for Key Questions.** Several questions benefit from writing actual code: a custom HashMap-like structure, a thread-safe Singleton, a Stream pipeline, or a deadlock scenario and its fix.

**Step 4: Know the Why.** Interviewers follow up. For every "what" answer, also know the "why": Why is String immutable? Why use ExecutorService instead of raw threads?

**Step 5: Connect Concepts.** String immutability leads to the String pool leads to intern(). HashMap internals lead to load factor leads to ConcurrentHashMap. Java 8 lambdas lead to functional interfaces lead to the Stream API.

---

## Recommended Study Schedule

- Day 1: Q1-Q10 (JVM, Strings, Collections basics)
- Day 2: Q11-Q20 (Exceptions, OOP, Generics, Java 8 intro)
- Day 3: Q21-Q30 (Java 8 deep dive, Collections advanced)
- Day 4: Q31-Q40 (Serialization, Concurrency, Strings advanced)
- Day 5: Q41-Q50 (Reflection, Modern Java, Memory)
- Day 6-7: Full review and mock interview practice
