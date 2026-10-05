# Project 03: Student Management System

A comprehensive student management system demonstrating **SOLID principles**, **MVC architecture**, and **professional Java practices**.

## Features

- **Student Management** — Register, update, delete, and search students
- **Teacher Management** — Manage teacher profiles and course assignments
- **Course Management** — Create courses with capacity limits and credit hours
- **Enrollment System** — Enroll/drop students with business rule validation
- **Grade Tracking** — Assign grades, calculate GPA, generate transcripts
- **CSV Import/Export** — Bulk import students from CSV, export data for reporting

## Architecture

```
src/
├── model/          # Domain objects (Student, Teacher, Course, Enrollment, Grade)
├── repository/     # Data access layer (interfaces + in-memory implementations)
├── service/        # Business logic layer
└── Main.java       # Demo entry point

tests/
├── StudentServiceTest.java
├── EnrollmentServiceTest.java
└── GradeServiceTest.java
```

### Layers

| Layer | Responsibility |
|-------|----------------|
| Model | Plain Java objects representing domain entities |
| Repository | Data access abstraction (CRUD operations) |
| Service | Business logic, validation, orchestration |

## Design Principles Demonstrated

- **S**ingle Responsibility — Each class has one reason to change
- **O**pen/Closed — Add new repository implementations without changing services
- **L**iskov Substitution — InMemory repositories are drop-in replacements
- **I**nterface Segregation — Separate repository interfaces per entity
- **D**ependency Inversion — Services depend on repository interfaces, not concretions

## What You'll Learn

- Designing repository and service layers with interfaces
- UUID-based entity identification
- Business rule validation (e.g., can't enroll in a full course)
- GPA calculation weighted by credit hours
- File I/O for CSV import/export using `BufferedReader`/`PrintWriter`
- Comprehensive JUnit 5 testing including file-based tests with temp files
- Handling `Optional` correctly throughout the codebase

## Running the Demo

```bash
cd src
javac model/*.java repository/*.java service/*.java Main.java
java Main
```

## Running Tests

Requires JUnit 5 on the classpath:

```bash
cd tests
javac -cp .:junit-5.jar ../src/**/*.java *.java
java -cp .:junit-5.jar org.junit.platform.console.ConsoleLauncher --scan-classpath
```

## Sample Output

```
=== Student Management System Demo ===

Registered student: Alice Johnson (CS)
Registered student: Bob Smith (Math)

Created course: CS101 - Introduction to Programming (3 credits, max 2 students)
Created course: MATH201 - Calculus I (4 credits, max 30 students)

Enrolled Alice in CS101
Enrolled Bob in CS101
CS101 is now full.

Assigned grade 95.0 to Alice in CS101 -> Letter: A, Points: 4.0
Assigned grade 78.0 to Bob in CS101   -> Letter: C, Points: 2.0

=== Alice's Transcript ===
Student: Alice Johnson
Major: CS
...
GPA: 4.00
```
