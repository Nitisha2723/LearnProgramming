# Learning Roadmap — From Zero to Architect

> **Realistic time estimates based on ~2 hours/day of focused study**

This roadmap guides you from absolute beginner to professional-level developer in either Java or Python (or both). The path is structured in five phases, each building on the last. Be patient with yourself — mastery takes time, and consistency beats intensity every time.

---

## Overview

| Phase | Java | Python | Time |
|-------|------|--------|------|
| Phase 1: Foundations | Modules 01-02 | Modules 01-02 | 2-3 weeks |
| Phase 2: OOP & Data Structures | Modules 03-04 | Modules 03-04 | 3-4 weeks |
| Phase 3: Real-World & Design | Modules 05-06 | Modules 05-06 | 4-5 weeks |
| Phase 4: Advanced & Projects | Modules 07-08 | Modules 07-08 | 4-5 weeks |
| Phase 5: Professional | Modules 09-11 | Modules 09-11 | 4-6 weeks |
| **Total** | | | **~4-5 months** |

Four to five months sounds like a lot. It isn't — not for the level of skill you'll reach. Most people spend years wandering between tutorials without building this depth. Stay the course.

---

## Phase 1 — Foundations (Weeks 1–3)

### What You'll Learn
- Setting up your development environment
- Variables, data types, and operators
- Control flow: conditionals and loops
- Arrays (Java) and lists (Python)
- Writing and calling functions/methods
- Reading error messages and debugging

### Week-by-Week Breakdown

**Week 1:** Set up your environment, get your first "Hello, World!" running. Work through data types, operators, and how to print output. Understand how the computer reads your code line by line.

**Week 2:** Tackle conditionals (`if`/`else`) and loops (`for`, `while`). Practice with arrays and lists — iterating, indexing, slicing. This week will feel repetitive. Good. Repetition builds muscle memory.

**Week 3:** Learn to write your own functions/methods. Build your first mini-projects using everything from Weeks 1-2. Solidify your understanding before moving on.

### Milestones — What You Can BUILD

By the end of Phase 1 you should be able to build:
- A command-line calculator (addition, subtraction, multiplication, division)
- FizzBuzz (classic, but teaches loops and conditionals together)
- A basic string manipulator (reverse a string, count vowels, check palindrome)

If you can build these from scratch without looking up syntax, you're ready for Phase 2.

### Knowledge Check
- Java: `learn-java/knowledge-checks/01-quiz.html` and `learn-java/knowledge-checks/02-quiz.html`
- Target score: 80% or higher before proceeding

### Common Pitfalls
- **Don't memorize syntax — understand concepts.** You can always look up how to write a for-loop; you can't look up how to think algorithmically. Focus on *why*, not *how*.
- Use the cheat sheets. They exist for exactly this phase.
- Avoid "tutorial hell" — watching videos feels productive but isn't. Write code. Break things. Fix them.
- If you're stuck for more than 30 minutes, look at the hint, then understand why it works. Don't just copy it.

---

## Phase 2 — OOP & Data Structures (Weeks 4–7)

### What You'll Learn
- Object-oriented programming: classes, objects, constructors
- Inheritance and polymorphism
- Interfaces (Java) and Abstract Base Classes (Python)
- Core collections: HashMap/dict, ArrayList/list, Set
- Introduction to Big-O notation

### Week-by-Week Breakdown

**Week 4:** Shift your mental model from "procedures" to "objects." Learn to define classes, create objects, write constructors, and add methods. Build a simple `Person` or `BankAccount` class.

**Week 5:** Extend your classes using inheritance. Learn why interfaces (Java) and ABCs (Python) exist and when to use them. Understand polymorphism through real examples — not abstract definitions.

**Week 6:** Master the workhorse data structures: dictionaries/HashMaps, lists/ArrayLists, and Sets. Know when to use each one. Understand how they behave under the hood (at a high level).

**Week 7:** Get a practical introduction to Big-O. You don't need to prove theorems — you need to know that looping inside a loop is usually O(n²) and that HashMap lookups are O(1). Build this phase's mini-project: a grade calculator or bank account system using proper OOP.

### Milestones — What You Can BUILD

By the end of Phase 2 you should be able to:
- Model real-world problems as a set of interacting classes
- Choose the right data structure for the job (list vs. set vs. map)
- Build a simple inventory or grade management system from scratch

### Knowledge Check
- Java: `learn-java/knowledge-checks/03-quiz.html` and `learn-java/knowledge-checks/04-quiz.html`
- Target score: 80% or higher before proceeding

### Common Pitfalls
- **Over-engineering too early.** Not everything needs to be a class. Start simple.
- Confusing *inheritance* with *composition*. Inheritance means "is-a"; composition means "has-a". When in doubt, compose.
- Skipping Big-O because it "feels like math." The practical intuition takes one week to build and pays dividends forever.
- Not practicing enough. OOP only clicks when you've written 10-15 classes yourself.

---

## Phase 3 — Real-World & Design (Weeks 8–12)

### What You'll Learn
- Exception handling and defensive programming
- File I/O — reading and writing files
- Streams (Java) and generators (Python) — functional programming style
- SOLID principles (one per day — don't rush this)
- Design patterns: Factory, Strategy, Observer, Decorator, and others
- Professional testing: TDD, mocking, parametrized tests

### Week-by-Week Breakdown

**Week 8:** Learn to handle errors gracefully with exceptions. Practice file I/O — read a CSV, write a log file. Build programs that don't crash on bad input.

**Week 9:** Discover streams and generators. Learn `map`, `filter`, `reduce`. Write more expressive code with less noise. Functional thinking makes you a better OOP programmer, not a worse one.

**Weeks 10-11:** Spend one day on each SOLID principle — don't try to absorb them all at once. Then move to patterns: two or three per week. Don't memorize pattern code; understand the *problem each pattern solves*. When you see that problem in real code, you'll know what to reach for.

**Week 12:** Testing deep-dive. Write tests first (TDD). Learn to mock dependencies. Write parametrized tests that cover edge cases. By the end of this week, writing tests should feel natural, not like a chore.

### Milestones — What You Can BUILD

By the end of Phase 3 you should be able to:
- Write clean, testable code that follows SOLID principles
- Add proper error handling to any program
- Process files and data streams with functional-style pipelines
- Apply 3-4 design patterns appropriately in a project

### Knowledge Check
- Java: `learn-java/knowledge-checks/05-quiz.html` and `learn-java/knowledge-checks/06-quiz.html`
- Target score: 80% or higher before proceeding

### Common Pitfalls
- **Pattern overuse.** Applying patterns where they're not needed is worse than not knowing them. "When all you have is a hammer..." Study the problem each pattern solves first.
- Skipping tests or treating them as optional. Untested code is unfinished code.
- Rushing SOLID. One principle per day means actually reflecting on it — find examples in code you've already written, not just the textbook example.
- Making exceptions too broad. Catching `Exception` (Java) or bare `except:` (Python) hides real problems.

---

## Phase 4 — Advanced & Projects (Weeks 13–17)

### What You'll Learn
- Generics (Java) and type hints/typing module (Python)
- Concurrency basics: threads, async fundamentals
- Building a substantial multi-file capstone project
- Advanced topics: async I/O, Java memory model
- Refactoring with design patterns

### Week-by-Week Breakdown

**Week 13:** Generics and type safety. Understand why they exist and how to use them without fighting the type system. Add type hints to your Python code; write generic Java classes. Your IDE will start helping you instead of getting in your way.

**Weeks 14-15:** Build your capstone project. Choose one:
- **Banking system**: accounts, transactions, fraud detection, reporting
- **E-commerce system**: products, cart, orders, discounts, inventory

This is the most important part of the entire roadmap. A real project forces you to make architectural decisions. Don't use a tutorial — plan it yourself, build it yourself, break it yourself, fix it yourself. Two weeks is not a lot of time. Scope accordingly.

**Week 16:** Advanced topics. Async I/O (Python's `asyncio`, Java's CompletableFuture). The Java memory model. Thread safety basics. You don't need to master concurrency — you need to know enough to not shoot yourself in the foot.

**Week 17:** Return to your capstone. Refactor it with fresh eyes. Apply design patterns where they actually help. Write tests for the tricky parts. Polish the README. This project goes on your GitHub.

### Milestones — What You Can BUILD

By the end of Phase 4 you should have:
- A complete, multi-file project on GitHub with a proper README
- Tests for the core business logic
- Clean code that a senior developer would not be embarrassed to read

### Knowledge Check
- Java: `learn-java/knowledge-checks/07-quiz.html` and `learn-java/knowledge-checks/08-quiz.html`
- Target score: 80% or higher before proceeding

### Common Pitfalls
- **Scope creep on the capstone.** Keep it small enough to finish in two weeks. A finished small project beats an unfinished large one every time.
- Treating concurrency as an afterthought. Even if you don't use threads now, understanding thread safety prevents subtle bugs later.
- Not putting the project on GitHub. Code that exists only on your machine doesn't count as a portfolio project.
- Skipping the refactor week. The first version of any project is a draft. Refactoring is how you learn to write better first drafts.

---

## Phase 5 — Professional (Weeks 18–23)

### What You'll Learn
- Database access: JDBC (Java) or SQLAlchemy (Python)
- Building REST APIs: Spring Boot (Java) or FastAPI (Python)
- System design fundamentals
- Data structures and algorithms for interviews
- Mock interviews and behavioral preparation

### Week-by-Week Breakdown

**Week 18:** Database module. Connect to a real database. Write queries in code. Understand connection pooling, transactions, and the N+1 problem. Add persistence to your capstone project.

**Weeks 19-20:** Build a REST API. Expose your capstone's functionality over HTTP. Understand routes, request/response cycles, status codes, JSON serialization, and basic authentication. Deploy it locally; bonus points for deploying to a cloud platform.

**Week 21:** System design fundamentals. How do large systems handle millions of users? Load balancing, caching, databases at scale, message queues, microservices vs. monoliths. You won't design these systems yet — but you'll understand the vocabulary and tradeoffs well enough to discuss them in an interview.

**Week 22:** Algorithm and data structure interview prep. Focus on: arrays, strings, hash maps, trees, and dynamic programming basics. Do 15-20 LeetCode problems — easy and medium. The goal is pattern recognition, not memorizing solutions.

**Week 23:** Mock interviews and behavioral prep. Practice explaining your code out loud. Prepare your "tell me about a project you're proud of" story using your capstone. Review the STAR method for behavioral questions. Do at least two mock technical interviews with a friend or service.

### Milestones — What You Can BUILD and DEMONSTRATE

By the end of Phase 5 you should be ready for:
- Junior to mid-level developer interviews
- Discussing architecture decisions and tradeoffs
- Deploying a working REST API connected to a database
- Solving medium-difficulty algorithm problems under time pressure

### Knowledge Check
- Java: `learn-java/knowledge-checks/09-quiz.html`, `10-quiz.html`, and `11-quiz.html`
- Target score: 80% or higher on each

### Common Pitfalls
- **LeetCode grinding instead of understanding.** Doing 200 problems mechanically is less valuable than deeply understanding 50. Know *why* each solution works.
- Neglecting system design because it feels abstract. Interviewers at senior-leaning roles care about this more than algorithms.
- Not practicing talking through your thought process. Silent coding in an interview is a red flag regardless of how good your code is.
- Forgetting to polish your GitHub. Recruiters look at it. Clean READMEs, consistent commits, a pinned capstone project — these matter.

---

## Study Tips

**Consistency beats intensity.** Two hours every day is far more effective than eight hours on Saturday. Your brain consolidates learning during sleep. Give it something to work with each night.

**The learning loop that works:**
1. Read the theory (understand the *why*)
2. Run the demo code (see it working)
3. Do the exercises (get your hands dirty)
4. Build the mini-project (synthesize everything)

**Don't skip the tests.** Writing tests isn't busywork — it's how professional developers verify their code actually works. The testing modules will teach you to think like a senior engineer.

**Use flashcards during downtime.** The `concept-cards/` folder has pre-built flashcards. Five minutes on the bus, five minutes waiting in line — these add up to hours of spaced repetition over a month.

**Take the quizzes.** After each module, take the knowledge check quiz. Score below 80%? Don't move on — go back and re-read the areas where you struggled. A weak foundation makes every subsequent phase harder.

**Build something real, even if it's small.** You will remember the bug you spent three hours tracking down far longer than any theory you read. Learning sticks through doing. Every project, no matter how simple, teaches you something a tutorial cannot.

**Talk about what you're learning.** Explain concepts to a friend, write a brief blog post, or just narrate your code out loud. The Feynman technique works: if you can't explain it simply, you don't understand it yet.

---

## For Different Starting Points

### Complete Beginner
Start at Phase 1 and go at whatever pace lets you actually understand the material. There is no prize for finishing faster than someone else. If Week 1 takes two weeks, that's fine — the goal is mastery, not speed.

### Knows Another Language
Skim the Phase 1-2 theory sections quickly. Spend your time on the exercises and quizzes — they'll reveal gaps in your understanding. You'll move through these phases in half the time, but don't skip the mini-projects.

### Knows Java, Learning Python (or Vice Versa)
Go straight to the Java-vs-Python cheat sheet at `cheat-sheets/shared/java-vs-python.html`. Then for each module, take the quiz first. Only study the sections where you scored below 80%. This approach lets you focus on what's genuinely new rather than reviewing what you already know.

### Interview Prep Focus
Move through Phases 1-3 at an accelerated pace (aim for 6-8 weeks total if you have prior programming experience). Then spend the bulk of your time on Phase 5, particularly Weeks 21-23. Do Phase 4's capstone project — having something to talk about in interviews is non-negotiable.

---

## Resources

| Resource | Location |
|----------|----------|
| Visualizations | `visualizations/README.md` |
| Cheat Sheets | `cheat-sheets/README.md` |
| Flashcards | `concept-cards/README.md` |
| Java vs Python Side-by-Side | `cheat-sheets/shared/java-vs-python.html` |

---

> The best time to start was six months ago. The second best time is today.
>
> Open Module 01 and write your first line of code.
