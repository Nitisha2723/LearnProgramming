# Mini Project: School Management System

A complete, working school management system demonstrating all four OOP pillars working together.

## What This Demonstrates

### Encapsulation
Every field in every class is `private`. State is only modified through methods that enforce business rules:
- Student grades cannot be set to values outside 0-100
- Course enrollment is capped at `maxCapacity`
- Person names and ages are validated in the constructor

### Inheritance
```
Person (abstract)
├── Student
│   - studentId, major, grades, yearOfStudy
│   - addGrade(), calculateGPA(), isHonorRoll()
└── Teacher
    - employeeId, department, salary, qualifications
    - giveRaise(), addQualification()
```
Both `Student` and `Teacher` call `super(personId, name, age, email)` in their constructors to initialize the shared `Person` fields.

### Polymorphism
- `school.getAllPeople()` returns a `List<Person>` containing both Students and Teachers
- Calling `person.displayInfo()` for each entry calls the correct version (Student's or Teacher's)
- `person.getRole()` returns "Student" or "Teacher" based on the actual object type — not the reference type

### Abstraction
- `Person` is abstract — you can never write `new Person(...)` directly
- `getRole()` is abstract in `Person` — every concrete subclass must define it
- `displayInfo()` is a template method in `Person` that uses the abstract `getRole()`

### Composition
- `School` HAS students, teachers, and courses (not IS any of them)
- `Course` HAS a Teacher (not IS a Teacher)

## Files

| File | Role |
|------|------|
| `Person.java` | Abstract base class |
| `Student.java` | Student extending Person |
| `Teacher.java` | Teacher extending Person |
| `Course.java` | Course with enrollment management |
| `School.java` | Top-level container with reports |
| `SchoolDemo.java` | Full demonstration driver |

## How to Compile and Run

```bash
cd mini-project/school-system/
javac Person.java Student.java Teacher.java Course.java School.java SchoolDemo.java
java SchoolDemo
```

## Expected Output Highlights

```
DEMONSTRATING POLYMORPHISM
Calling person.displayInfo() for ALL people (students + teachers):
Each person.displayInfo() is DIFFERENT — but we call the same method!

Role determined at runtime: Student
[Student] P001 Alice Chen (age 20) - alice@student.oakwood.edu
  Student ID: STU001 | Major: Computer Science | Year: 2
  GPA: 3.73 (A) | Grade: 3 courses | HONOR ROLL

Role determined at runtime: Teacher
[Teacher] T001 Dr. Sarah Smith (age 45) - s.smith@oakwood.edu
  Employee ID: EMP001 | Dept: Computer Science | Salary: $85000.00
  Courses taught: 2 | Qualifications: 2
  Qualifications: [PhD in Computer Science, Oracle Certified Java Professional]
```
