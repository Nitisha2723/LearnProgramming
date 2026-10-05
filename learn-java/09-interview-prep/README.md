# Module 09 — Interview Preparation

This module prepares you for technical interviews at software companies. It covers every major category that appears in Java engineering interviews: language knowledge, data structures and algorithms, system design, and behavioral questions.

---

## What This Module Covers

| Section | Files | What You Learn |
|---|---|---|
| Java Core Questions | 50 Q&A | JVM, strings, collections, exceptions, generics, Java 8+ |
| OOP Questions | 30 Q&A | SOLID, design patterns, real-world design scenarios |
| DS/A Problems | 48 problems + solutions | Arrays, strings, linked lists, trees, DP |
| System Design | 5 guides | Fundamentals, scalability, 3 case studies |
| Behavioral | 20 STAR answers | Common behavioral questions with full answers |
| Coding Challenges | 13 challenges | Easy, medium, hard — all with complete solutions |

---

## How to Use This Module

### If your interview is in 2 weeks

**Week 1 — Foundations:**
- Read all 50 Java core Q&A. Quiz yourself without looking.
- Read the OOP Q&A. Focus on SOLID principles and pattern identification.
- Solve all 5 easy coding challenges without looking at solutions.
- Read system design fundamentals (sections 1 and 2).

**Week 2 — Depth:**
- Solve the DS/A problems in arrays and strings sections.
- Tackle the 5 medium coding challenges.
- Study the URL shortener case study in depth.
- Practice STAR answers out loud (not silently — must practice speaking).
- Attempt 1-2 hard challenges.

### If your interview is in 4+ weeks

Take the two-week plan above but repeat it twice. The second pass focuses on weak areas from the first pass.

---

## Interview Stages

Most software engineering interviews have 4 stages:

**1. Phone Screen (30-45 min)**
Recruiter or engineer. Usually 1-2 easy/medium coding questions. Mostly language fundamentals.

**2. Technical Interview (45-60 min)**
1-2 coding problems. May include code review, debugging, or design discussion. Medium to hard difficulty.

**3. System Design Interview (45-60 min)** (usually for senior roles)
Design a large-scale system: "design Twitter" or "design a URL shortener." Open-ended, evaluates architecture thinking.

**4. Behavioral Interview (30-45 min)**
STAR-format questions about past experiences. Evaluates soft skills, culture fit, ownership.

---

## Directory Structure

```
09-interview-prep/
├── README.md                      ← You are here
├── 01-java-core-questions/
│   ├── README.md
│   └── answers.md                 50 complete Q&A
├── 02-oop-questions/
│   ├── README.md
│   └── answers.md                 30 complete Q&A
├── 03-data-structures-algorithms/
│   ├── README.md
│   ├── arrays/
│   │   ├── problems.md            10 problems with walkthroughs
│   │   └── ArrayProblems.java     Complete solutions
│   ├── strings/
│   │   ├── problems.md
│   │   └── StringProblems.java
│   ├── linked-list/
│   │   ├── problems.md
│   │   └── LinkedListProblems.java
│   ├── trees/
│   │   ├── problems.md
│   │   └── TreeProblems.java
│   └── dynamic-programming/
│       ├── problems.md
│       └── DPProblems.java
├── 04-system-design/
│   ├── README.md
│   ├── 01-design-fundamentals.md
│   ├── 02-scalability.md
│   ├── 03-case-study-url-shortener.md
│   ├── 04-case-study-twitter.md
│   └── 05-case-study-amazon.md
├── 05-behavioral/
│   ├── README.md
│   └── star-method-answers.md     20 complete STAR answers
└── 06-coding-challenges/
    ├── README.md
    ├── easy/   (5 problems)
    ├── medium/ (5 problems)
    └── hard/   (3 problems)
```

---

## Tips for Java Interviews Specifically

1. **Know your data structures cold.** When to use ArrayList vs LinkedList, HashMap vs TreeMap, when to prefer a Queue vs Deque. These come up in both Q&A and coding sections.

2. **Understand Big O, not just correct code.** Interviewers will always ask "what's the time and space complexity?" Answer before they ask.

3. **Java 8 features are heavily tested.** Streams, lambdas, Optional, functional interfaces — be fluent with these. Many candidates still write verbose Java 7 style code.

4. **Know exception handling deeply.** Checked vs unchecked, try-with-resources, when to use each type of exception — these are common screen questions.

5. **Concurrency is a senior-level differentiator.** Understanding synchronized, volatile, happens-before, and common concurrency patterns sets strong candidates apart.

6. **For DS/A: practice writing code on paper or a plain text editor.** No IDE autocomplete in interviews.

7. **System design is about communication.** Ask clarifying questions, think out loud, discuss trade-offs. The interviewer wants to see how you think, not just what the final design is.
