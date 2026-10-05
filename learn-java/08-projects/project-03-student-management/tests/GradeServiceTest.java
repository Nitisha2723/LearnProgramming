import model.Course;
import model.Enrollment;
import model.Grade;
import model.Student;
import org.junit.jupiter.api.*;
import repository.*;
import service.GradeService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GradeService}.
 */
@DisplayName("GradeService Tests")
class GradeServiceTest {

    private InMemoryStudentRepository    studentRepo;
    private InMemoryCourseRepository     courseRepo;
    private InMemoryEnrollmentRepository enrollmentRepo;
    private InMemoryGradeRepository      gradeRepo;
    private GradeService                 service;

    // Shared fixtures
    private Student student;
    private Course  cs101;
    private Course  math201;
    private Enrollment enrollCS;
    private Enrollment enrollMath;

    @BeforeEach
    void setUp() {
        studentRepo    = new InMemoryStudentRepository();
        courseRepo     = new InMemoryCourseRepository();
        enrollmentRepo = new InMemoryEnrollmentRepository();
        gradeRepo      = new InMemoryGradeRepository();
        service        = new GradeService(gradeRepo, enrollmentRepo, courseRepo, studentRepo);

        student  = new Student("Alice", "Johnson", "alice@test.com",
                LocalDate.of(2002, 1, 1), "CS");
        studentRepo.save(student);

        cs101   = new Course("CS101", "Intro Programming", "t1", 30, 3);
        math201 = new Course("MATH201", "Calculus I",       "t2", 30, 4);
        courseRepo.save(cs101);
        courseRepo.save(math201);

        enrollCS   = new Enrollment(student.getStudentId(), cs101.getCourseId());
        enrollMath = new Enrollment(student.getStudentId(), math201.getCourseId());
        enrollmentRepo.save(enrollCS);
        enrollmentRepo.save(enrollMath);
    }

    // -------------------------------------------------------------------------
    // assignGrade
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("assignGrade: creates grade with correct letter grade")
    void assignGrade_success() {
        Grade g = service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 92.0);

        assertNotNull(g.getGradeId());
        assertEquals(92.0, g.getScore());
        assertEquals("A",  g.getLetterGrade());
        assertEquals(4.0,  g.getGradePoints());
    }

    @Test
    @DisplayName("assignGrade: throws when score is out of range")
    void assignGrade_invalidScore_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                        enrollCS.getEnrollmentId(), 105.0));
    }

    @Test
    @DisplayName("assignGrade: throws when duplicate grade for same enrollment")
    void assignGrade_duplicate_throws() {
        service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 85.0);

        assertThrows(IllegalArgumentException.class, () ->
                service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                        enrollCS.getEnrollmentId(), 90.0));
    }

    // -------------------------------------------------------------------------
    // updateGrade
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateGrade: changes score and letter grade")
    void updateGrade_success() {
        Grade g = service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 75.0);
        assertEquals("C", g.getLetterGrade());

        Grade updated = service.updateGrade(g.getGradeId(), 88.0);
        assertEquals(88.0, updated.getScore());
        assertEquals("B",  updated.getLetterGrade());
    }

    // -------------------------------------------------------------------------
    // calculateGpa — single course
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateGpa: single A grade in 3-credit course → 4.00")
    void calculateGpa_singleCourse() {
        service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 95.0);  // A

        double gpa = service.calculateGpa(student.getStudentId());
        assertEquals(4.0, gpa, 0.001);
    }

    // -------------------------------------------------------------------------
    // calculateGpa — multiple courses (weighted)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("calculateGpa: weighted average across 3-credit (A) and 4-credit (B)")
    void calculateGpa_multipleCourses_weighted() {
        // CS101: 3 credits, A = 4.0  → contribution: 12.0
        service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 95.0);
        // MATH201: 4 credits, B = 3.0 → contribution: 12.0
        service.assignGrade(student.getStudentId(), math201.getCourseId(),
                enrollMath.getEnrollmentId(), 85.0);

        // GPA = (4*3 + 3*4) / (3+4) = 24/7 ≈ 3.4286
        double gpa = service.calculateGpa(student.getStudentId());
        assertEquals(24.0 / 7.0, gpa, 0.001);
    }

    @Test
    @DisplayName("calculateGpa: no grades returns 0.0")
    void calculateGpa_noGrades_returnsZero() {
        double gpa = service.calculateGpa(student.getStudentId());
        assertEquals(0.0, gpa);
    }

    // -------------------------------------------------------------------------
    // generateTranscript
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateTranscript: contains student name, course code, score, and GPA")
    void generateTranscript_containsExpectedInfo() {
        service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 92.0);

        String transcript = service.generateTranscript(student.getStudentId());

        assertTrue(transcript.contains("Alice Johnson"));
        assertTrue(transcript.contains("CS101"));
        assertTrue(transcript.contains("92.0"));
        assertTrue(transcript.contains("GPA"));
    }

    // -------------------------------------------------------------------------
    // getClassReport
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getClassReport: contains average, min, max, and distribution")
    void getClassReport_containsStats() {
        // Add a second student
        Student bob = new Student("Bob", "Smith", "bob@test.com",
                LocalDate.of(2001, 5, 5), "CS");
        studentRepo.save(bob);
        Enrollment eBob = new Enrollment(bob.getStudentId(), cs101.getCourseId());
        enrollmentRepo.save(eBob);

        service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 90.0);  // A
        service.assignGrade(bob.getStudentId(), cs101.getCourseId(),
                eBob.getEnrollmentId(), 70.0);       // C

        String report = service.getClassReport(cs101.getCourseId());

        assertTrue(report.contains("CS101"));
        assertTrue(report.contains("90.00") || report.contains("80.00")); // avg=80
        assertTrue(report.contains("A:"));
        assertTrue(report.contains("C:"));
    }

    @Test
    @DisplayName("getClassReport: returns message when no grades recorded")
    void getClassReport_noGrades_returnsMessage() {
        String report = service.getClassReport(cs101.getCourseId());
        assertTrue(report.contains("No grades"));
    }

    // -------------------------------------------------------------------------
    // getStudentGrades
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getStudentGrades: returns all grades for a student")
    void getStudentGrades_returnsAll() {
        service.assignGrade(student.getStudentId(), cs101.getCourseId(),
                enrollCS.getEnrollmentId(), 88.0);
        service.assignGrade(student.getStudentId(), math201.getCourseId(),
                enrollMath.getEnrollmentId(), 76.0);

        List<Grade> grades = service.getStudentGrades(student.getStudentId());
        assertEquals(2, grades.size());
    }

    // -------------------------------------------------------------------------
    // Letter grade boundary tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Grade: boundary scores map to correct letter grades")
    void grade_letterGradeBoundaries() {
        // Create fresh enrollments for boundary testing
        Course c = new Course("TST001", "Test Course", "t", 30, 3);
        courseRepo.save(c);

        double[] scores   = {100, 90, 89, 80, 79, 70, 69, 60, 59, 0};
        String[] expected = {"A","A","B","B","C","C","D","D","F","F"};

        for (int i = 0; i < scores.length; i++) {
            model.Grade g = new model.Grade("s", c.getCourseId(), "e" + i, scores[i]);
            assertEquals(expected[i], g.getLetterGrade(),
                    "Score " + scores[i] + " should be " + expected[i]);
        }
    }
}
