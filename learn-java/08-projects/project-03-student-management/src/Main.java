import model.Course;
import model.Enrollment;
import model.Grade;
import model.Student;
import model.Teacher;
import repository.InMemoryCourseRepository;
import repository.InMemoryEnrollmentRepository;
import repository.InMemoryGradeRepository;
import repository.InMemoryStudentRepository;
import service.CourseService;
import service.EnrollmentService;
import service.GradeService;
import service.StudentService;

import java.time.LocalDate;

/**
 * Demonstrates the Student Management System end-to-end.
 *
 * Run from the {@code src/} directory:
 * <pre>
 *   javac model/*.java repository/*.java service/*.java Main.java
 *   java Main
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Student Management System Demo ===\n");

        // ---- Wire up repositories ----
        InMemoryStudentRepository    studentRepo    = new InMemoryStudentRepository();
        InMemoryCourseRepository     courseRepo     = new InMemoryCourseRepository();
        InMemoryEnrollmentRepository enrollmentRepo = new InMemoryEnrollmentRepository();
        InMemoryGradeRepository      gradeRepo      = new InMemoryGradeRepository();

        // ---- Wire up services ----
        StudentService    studentService    = new StudentService(studentRepo);
        CourseService     courseService     = new CourseService(courseRepo);
        EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepo, courseRepo);
        GradeService      gradeService      = new GradeService(gradeRepo, enrollmentRepo, courseRepo, studentRepo);

        // ---- Create teachers ----
        Teacher drSmith = new Teacher("Dr.", "Smith", "smith@uni.edu", "Computer Science");
        Teacher drJones = new Teacher("Dr.", "Jones", "jones@uni.edu", "Mathematics");
        System.out.println("Created teachers: " + drSmith.getFullName() + ", " + drJones.getFullName());

        // ---- Create courses ----
        // CS101 has capacity 2 to demo "full" scenario
        Course cs101 = courseService.createCourse("CS101", "Intro to Programming",
                drSmith.getTeacherId(), 2, 3);
        Course math201 = courseService.createCourse("MATH201", "Calculus I",
                drJones.getTeacherId(), 30, 4);
        System.out.println("Created course: " + cs101.getCourseCode()
                + " - " + cs101.getCourseName() + " (capacity=" + cs101.getMaxCapacity() + ")");
        System.out.println("Created course: " + math201.getCourseCode()
                + " - " + math201.getCourseName());

        // ---- Register students ----
        Student alice = studentService.registerStudent(
                "Alice", "Johnson", "alice@example.com",
                LocalDate.of(2002, 5, 15), "Computer Science");
        Student bob = studentService.registerStudent(
                "Bob", "Smith", "bob@example.com",
                LocalDate.of(2001, 9, 20), "Computer Science");
        Student carol = studentService.registerStudent(
                "Carol", "White", "carol@example.com",
                LocalDate.of(2003, 1, 10), "Mathematics");
        System.out.println("\nRegistered students:");
        studentService.getAllStudents().forEach(s ->
                System.out.println("  " + s.getFullName() + " (" + s.getMajor() + ")"));

        // ---- Enroll students ----
        System.out.println("\nEnrolling students...");
        Enrollment eAliceCS  = enrollmentService.enrollStudent(alice.getStudentId(), cs101.getCourseId());
        Enrollment eBobCS    = enrollmentService.enrollStudent(bob.getStudentId(),   cs101.getCourseId());
        Enrollment eCarolMath = enrollmentService.enrollStudent(carol.getStudentId(), math201.getCourseId());
        Enrollment eAliceMath = enrollmentService.enrollStudent(alice.getStudentId(), math201.getCourseId());

        // Demo: course full
        try {
            enrollmentService.enrollStudent(carol.getStudentId(), cs101.getCourseId());
        } catch (IllegalStateException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        System.out.println("CS101 enrollment: "
                + courseRepo.findById(cs101.getCourseId()).get().getEnrollmentCount()
                + "/" + cs101.getMaxCapacity() + " (full=" + cs101.isFull() + ")");

        // ---- Assign grades ----
        System.out.println("\nAssigning grades...");
        Grade gAliceCS  = gradeService.assignGrade(alice.getStudentId(), cs101.getCourseId(),
                eAliceCS.getEnrollmentId(), 95.0);
        Grade gBobCS    = gradeService.assignGrade(bob.getStudentId(),   cs101.getCourseId(),
                eBobCS.getEnrollmentId(), 78.0);
        Grade gCarolMath = gradeService.assignGrade(carol.getStudentId(), math201.getCourseId(),
                eCarolMath.getEnrollmentId(), 88.0);
        Grade gAliceMath = gradeService.assignGrade(alice.getStudentId(), math201.getCourseId(),
                eAliceMath.getEnrollmentId(), 82.0);

        System.out.printf("Alice  CS101:   %.1f -> %s (%.1f pts)%n",
                gAliceCS.getScore(), gAliceCS.getLetterGrade(), gAliceCS.getGradePoints());
        System.out.printf("Bob    CS101:   %.1f -> %s (%.1f pts)%n",
                gBobCS.getScore(), gBobCS.getLetterGrade(), gBobCS.getGradePoints());
        System.out.printf("Carol  MATH201: %.1f -> %s (%.1f pts)%n",
                gCarolMath.getScore(), gCarolMath.getLetterGrade(), gCarolMath.getGradePoints());
        System.out.printf("Alice  MATH201: %.1f -> %s (%.1f pts)%n",
                gAliceMath.getScore(), gAliceMath.getLetterGrade(), gAliceMath.getGradePoints());

        // ---- GPA ----
        System.out.printf("%nAlice's GPA: %.2f%n",  gradeService.calculateGpa(alice.getStudentId()));
        System.out.printf("Bob's GPA:   %.2f%n",    gradeService.calculateGpa(bob.getStudentId()));
        System.out.printf("Carol's GPA: %.2f%n",    gradeService.calculateGpa(carol.getStudentId()));

        // ---- Transcript ----
        System.out.println();
        System.out.println(gradeService.generateTranscript(alice.getStudentId()));

        // ---- Class Report ----
        System.out.println(gradeService.getClassReport(cs101.getCourseId()));

        // ---- CSV Export / Import demo ----
        String exportPath = "/tmp/students_export.csv";
        studentService.exportToCsv(exportPath);
        System.out.println("Exported " + studentRepo.count() + " students to " + exportPath);

        // Clear and re-import
        studentRepo.delete(alice.getStudentId());
        studentRepo.delete(bob.getStudentId());
        studentRepo.delete(carol.getStudentId());
        int imported = studentService.importFromCsv(exportPath);
        System.out.println("Re-imported " + imported + " students from CSV.");
        System.out.println("Total students: " + studentRepo.count());

        System.out.println("\n=== Demo complete ===");
    }
}
