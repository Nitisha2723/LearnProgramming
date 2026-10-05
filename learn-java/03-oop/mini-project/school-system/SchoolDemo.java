/**
 * SchoolDemo — comprehensive demonstration of all OOP concepts
 * using the school management system.
 *
 * OBSERVE:
 * 1. Encapsulation: all operations go through methods, not direct field access
 * 2. Inheritance: Student and Teacher both extend Person
 * 3. Polymorphism: List<Person> holds both Students and Teachers
 *                  person.displayInfo() calls the right version for each
 * 4. Abstraction: Person is abstract (no raw Person objects), each has getRole()
 * 5. Composition: School HAS Students, Teachers, Courses
 *                 Course HAS a Teacher (not IS a Teacher)
 */
public class SchoolDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("SCHOOL MANAGEMENT SYSTEM — Complete OOP Demonstration");
        System.out.println("=".repeat(70));

        // =====================================================================
        // 1. Create the school
        // =====================================================================
        School school = new School("Oakwood Academy", "123 Learning Lane, Knowledge City");
        System.out.println("\nSchool created: " + school);

        // =====================================================================
        // 2. Create teachers (Person subtypes — inheritance)
        // =====================================================================
        System.out.println("\n--- Creating Teachers ---");
        Teacher drSmith = new Teacher("T001", "Dr. Sarah Smith", 45, "s.smith@oakwood.edu",
                                       "EMP001", "Computer Science", 85000.0);
        drSmith.addQualification("PhD in Computer Science");
        drSmith.addQualification("Oracle Certified Java Professional");

        Teacher profJohnson = new Teacher("T002", "Prof. Michael Johnson", 52, "m.johnson@oakwood.edu",
                                          "EMP002", "Mathematics", 78000.0);
        profJohnson.addQualification("PhD in Applied Mathematics");
        profJohnson.addQualification("20 years teaching experience");

        Teacher msLee = new Teacher("T003", "Ms. Amy Lee", 35, "a.lee@oakwood.edu",
                                     "EMP003", "English", 65000.0);
        msLee.addQualification("MA in English Literature");

        // Add teachers to school
        school.addTeacher(drSmith);
        school.addTeacher(profJohnson);
        school.addTeacher(msLee);

        // =====================================================================
        // 3. Create courses (Composition — Course HAS a Teacher)
        // =====================================================================
        System.out.println("\n--- Creating Courses ---");
        Course javaProgramming = new Course("CS101", "Java Programming", drSmith, 25, 4);
        javaProgramming.setDescription("Introduction to OOP using Java");

        Course dataStructures = new Course("CS201", "Data Structures", drSmith, 20, 4);
        Course calculus = new Course("MATH101", "Calculus I", profJohnson, 30, 3);
        Course techWriting = new Course("ENG201", "Technical Writing", msLee, 20, 3);

        school.addCourse(javaProgramming);
        school.addCourse(dataStructures);
        school.addCourse(calculus);
        school.addCourse(techWriting);

        // =====================================================================
        // 4. Create students (Person subtypes — inheritance)
        // =====================================================================
        System.out.println("\n--- Creating Students ---");
        Student alice = new Student("P001", "Alice Chen", 20, "alice@student.oakwood.edu",
                                    "STU001", "Computer Science", 2);
        Student bob = new Student("P002", "Bob Williams", 19, "bob@student.oakwood.edu",
                                  "STU002", "Mathematics", 1);
        Student charlie = new Student("P003", "Charlie Davis", 21, "charlie@student.oakwood.edu",
                                      "STU003", "Computer Science", 3);
        Student diana = new Student("P004", "Diana Martinez", 20, "diana@student.oakwood.edu",
                                    "STU004", "English", 2);
        Student evan = new Student("P005", "Evan Thompson", 22, "evan@student.oakwood.edu",
                                   "STU005", "Computer Science", 4);

        school.addStudent(alice);
        school.addStudent(bob);
        school.addStudent(charlie);
        school.addStudent(diana);
        school.addStudent(evan);

        // =====================================================================
        // 5. Enroll students in courses
        // =====================================================================
        System.out.println("\n--- Enrolling Students ---");
        javaProgramming.enroll(alice);
        javaProgramming.enroll(bob);
        javaProgramming.enroll(charlie);
        javaProgramming.enroll(evan);

        dataStructures.enroll(alice);
        dataStructures.enroll(charlie);
        dataStructures.enroll(evan);

        calculus.enroll(alice);
        calculus.enroll(bob);
        calculus.enroll(diana);

        techWriting.enroll(diana);
        techWriting.enroll(charlie);

        // Test duplicate enrollment
        javaProgramming.enroll(alice);  // Should show warning

        // =====================================================================
        // 6. Record grades
        // =====================================================================
        System.out.println("\n--- Recording Grades ---");
        javaProgramming.recordGrade(alice, 95.0);
        javaProgramming.recordGrade(bob, 78.5);
        javaProgramming.recordGrade(charlie, 88.0);
        javaProgramming.recordGrade(evan, 72.0);

        dataStructures.recordGrade(alice, 92.0);
        dataStructures.recordGrade(charlie, 85.0);
        dataStructures.recordGrade(evan, 68.0);

        calculus.recordGrade(alice, 87.0);
        calculus.recordGrade(bob, 91.0);
        calculus.recordGrade(diana, 79.0);

        techWriting.recordGrade(diana, 94.0);
        techWriting.recordGrade(charlie, 83.0);

        // =====================================================================
        // 7. Demonstrate polymorphism
        // =====================================================================
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DEMONSTRATING POLYMORPHISM");
        System.out.println("=".repeat(70));
        System.out.println("\nCalling person.displayInfo() for ALL people (students + teachers):");
        System.out.println("Each person.displayInfo() is DIFFERENT — but we call the same method!");
        System.out.println("This is runtime polymorphism — dynamic dispatch.\n");

        for (Person person : school.getAllPeople()) {
            // person.getRole() and person.displayInfo() are abstract methods
            // Each subclass provides its own implementation
            // The JVM decides which version to call at RUNTIME
            System.out.printf("Role determined at runtime: %s%n", person.getRole());
            person.displayInfo();
            System.out.println();
        }

        // =====================================================================
        // 8. Course rosters
        // =====================================================================
        System.out.println("=".repeat(70));
        System.out.println("COURSE ROSTERS");
        System.out.println("=".repeat(70));

        javaProgramming.printRoster();
        calculus.printRoster();

        // =====================================================================
        // 9. School reports
        // =====================================================================
        school.printAcademicReport();
        school.printHonorRoll();

        // =====================================================================
        // 10. Advanced operations
        // =====================================================================
        System.out.println("\n--- Additional Operations ---");

        // Teacher raise (encapsulation: giveRaise validates %)
        drSmith.giveRaise(5.0);

        // Student birthday (encapsulation: haveBirthday() increments age)
        alice.haveBirthday();

        // Student advancing year
        bob.advanceYear();

        // Find top student using polymorphism
        Student top = school.getTopStudent();
        System.out.println("\nTop student: " + top);
        System.out.printf("Overall school GPA: %.2f%n", school.getSchoolAverageGPA());

        // =====================================================================
        // 11. Test encapsulation
        // =====================================================================
        System.out.println("\n--- Testing Encapsulation ---");

        System.out.println("Trying to enroll in a full course:");
        // Fill up techWriting course to max (20 capacity)
        // (We already have 2 — add 18 more)
        for (int i = 0; i < 18; i++) {
            Student s = new Student("TEMP" + i, "Student " + i, 20, null,
                                   "S" + i, "General", 1);
            techWriting.enroll(s);
        }
        // Now try to enroll one more
        techWriting.enroll(alice);  // Should fail — course is full

        System.out.println("\nTrying to give invalid grade:");
        try {
            javaProgramming.recordGrade(alice, 150.0);  // Over 100
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\nTrying to grade non-enrolled student:");
        Student newStudent = new Student("P999", "Zara Khan", 21, null, "STU999", "CS");
        try {
            javaProgramming.recordGrade(newStudent, 85.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(70));
        System.out.println("School Summary: " + school);
        System.out.println("=".repeat(70));
    }
}
