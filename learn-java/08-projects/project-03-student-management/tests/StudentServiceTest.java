import model.Student;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import repository.InMemoryStudentRepository;
import service.StudentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StudentService}.
 *
 * Each test uses a fresh {@link InMemoryStudentRepository} to ensure full isolation.
 */
@DisplayName("StudentService Tests")
class StudentServiceTest {

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(new InMemoryStudentRepository());
    }

    // -------------------------------------------------------------------------
    // registerStudent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("registerStudent: creates student with correct fields")
    void registerStudent_success() {
        Student s = studentService.registerStudent(
                "Alice", "Johnson", "alice@test.com",
                LocalDate.of(2000, 6, 15), "CS");

        assertNotNull(s.getStudentId());
        assertEquals("Alice",   s.getFirstName());
        assertEquals("Johnson", s.getLastName());
        assertEquals("alice@test.com", s.getEmail());
        assertEquals("CS",      s.getMajor());
        assertNotNull(s.getEnrolledAt());
    }

    @Test
    @DisplayName("registerStudent: duplicate email throws IllegalArgumentException")
    void registerStudent_duplicateEmail_throws() {
        studentService.registerStudent("Alice", "A", "dup@test.com",
                LocalDate.of(2000, 1, 1), "CS");

        assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("Bob", "B", "dup@test.com",
                        LocalDate.of(2001, 1, 1), "Math"));
    }

    @Test
    @DisplayName("registerStudent: blank firstName throws IllegalArgumentException")
    void registerStudent_blankFirstName_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                studentService.registerStudent("  ", "Johnson", "x@x.com",
                        LocalDate.now(), "CS"));
    }

    // -------------------------------------------------------------------------
    // getStudent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getStudent: returns correct student by ID")
    void getStudent_success() {
        Student created = studentService.registerStudent("Bob", "Smith", "bob@test.com",
                LocalDate.of(1999, 3, 10), "Math");

        Student found = studentService.getStudent(created.getStudentId());
        assertEquals(created.getStudentId(), found.getStudentId());
    }

    @Test
    @DisplayName("getStudent: unknown ID throws NoSuchElementException")
    void getStudent_notFound_throws() {
        assertThrows(NoSuchElementException.class, () ->
                studentService.getStudent("non-existent-id"));
    }

    // -------------------------------------------------------------------------
    // updateStudent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateStudent: updates only provided fields")
    void updateStudent_partialUpdate() {
        Student s = studentService.registerStudent("Carol", "White", "carol@test.com",
                LocalDate.of(2001, 7, 22), "Physics");

        Student updated = studentService.updateStudent(
                s.getStudentId(), null, null, null, null, "Engineering");

        assertEquals("Engineering", updated.getMajor());
        assertEquals("Carol",       updated.getFirstName()); // unchanged
    }

    @Test
    @DisplayName("updateStudent: changing email to already-used email throws")
    void updateStudent_duplicateEmail_throws() {
        Student s1 = studentService.registerStudent("Alice", "A", "alice@test.com",
                LocalDate.of(2000, 1, 1), "CS");
        studentService.registerStudent("Bob", "B", "bob@test.com",
                LocalDate.of(2000, 1, 1), "CS");

        assertThrows(IllegalArgumentException.class, () ->
                studentService.updateStudent(s1.getStudentId(), null, null,
                        "bob@test.com", null, null));
    }

    // -------------------------------------------------------------------------
    // deleteStudent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteStudent: removes student successfully")
    void deleteStudent_success() {
        Student s = studentService.registerStudent("Dave", "D", "dave@test.com",
                LocalDate.of(2000, 1, 1), "CS");

        assertTrue(studentService.deleteStudent(s.getStudentId()));
        assertThrows(NoSuchElementException.class, () ->
                studentService.getStudent(s.getStudentId()));
    }

    // -------------------------------------------------------------------------
    // getStudentsByMajor
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getStudentsByMajor: returns only matching students")
    void getStudentsByMajor() {
        studentService.registerStudent("A", "A", "a@test.com", LocalDate.now(), "CS");
        studentService.registerStudent("B", "B", "b@test.com", LocalDate.now(), "CS");
        studentService.registerStudent("C", "C", "c@test.com", LocalDate.now(), "Math");

        List<Student> csStudents = studentService.getStudentsByMajor("CS");
        assertEquals(2, csStudents.size());
        csStudents.forEach(s -> assertEquals("CS", s.getMajor()));
    }

    // -------------------------------------------------------------------------
    // CSV Export / Import
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("exportToCsv: writes header and one row per student")
    void exportToCsv_writesCorrectLines(@TempDir Path tempDir) throws IOException {
        studentService.registerStudent("Eve", "E", "eve@test.com",
                LocalDate.of(2000, 1, 1), "CS");
        studentService.registerStudent("Frank", "F", "frank@test.com",
                LocalDate.of(2001, 2, 2), "Math");

        Path csvFile = tempDir.resolve("students.csv");
        studentService.exportToCsv(csvFile.toString());

        List<String> lines = Files.readAllLines(csvFile);
        assertEquals(3, lines.size()); // 1 header + 2 data rows
        assertTrue(lines.get(0).startsWith("studentId,"));
        assertTrue(lines.get(1).contains("Eve"));
    }

    @Test
    @DisplayName("importFromCsv: creates students from CSV file")
    void importFromCsv_createsStudents(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("import.csv");
        String content = "firstName,lastName,email,dateOfBirth,major\n"
                + "Grace,G,grace@test.com,2000-03-15,CS\n"
                + "Henry,H,henry@test.com,1999-11-30,Math\n";
        Files.writeString(csvFile, content);

        int imported = studentService.importFromCsv(csvFile.toString());
        assertEquals(2, imported);

        List<Student> all = studentService.getAllStudents();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(s -> "grace@test.com".equals(s.getEmail())));
    }

    @Test
    @DisplayName("importFromCsv: skips duplicate emails, returns count of new students")
    void importFromCsv_skipsDuplicates(@TempDir Path tempDir) throws IOException {
        studentService.registerStudent("Grace", "G", "grace@test.com",
                LocalDate.of(2000, 3, 15), "CS");

        Path csvFile = tempDir.resolve("import.csv");
        String content = "firstName,lastName,email,dateOfBirth,major\n"
                + "Grace,G,grace@test.com,2000-03-15,CS\n"   // duplicate
                + "Iris,I,iris@test.com,2001-01-01,Physics\n";
        Files.writeString(csvFile, content);

        int imported = studentService.importFromCsv(csvFile.toString());
        assertEquals(1, imported); // only Iris is new
    }
}
