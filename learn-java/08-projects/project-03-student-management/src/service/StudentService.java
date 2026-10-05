package service;

import model.Student;
import repository.StudentRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Business logic for student lifecycle management.
 *
 * <p>Depends on {@link StudentRepository} via constructor injection — the
 * concrete implementation (in-memory, database, …) is chosen by the caller,
 * keeping this class open for extension and closed for modification.</p>
 */
public class StudentService {

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DT_FMT    = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /**
     * Registers a new student.
     *
     * @return the newly created {@link Student}
     * @throws IllegalArgumentException if the email is already in use
     */
    public Student registerStudent(String firstName, String lastName, String email,
                                   LocalDate dateOfBirth, String major) {
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name is required.");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email is required.");
        if (studentRepository.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Email already in use: " + email);

        Student student = new Student(firstName, lastName, email, dateOfBirth, major);
        return studentRepository.save(student);
    }

    /**
     * Retrieves a student by ID.
     *
     * @throws NoSuchElementException if no student with that ID exists
     */
    public Student getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Student not found: " + studentId));
    }

    /**
     * Updates mutable fields of an existing student.
     */
    public Student updateStudent(String studentId, String firstName, String lastName,
                                 String email, LocalDate dateOfBirth, String major) {
        Student student = getStudent(studentId);

        if (firstName  != null && !firstName.isBlank())  student.setFirstName(firstName);
        if (lastName   != null && !lastName.isBlank())   student.setLastName(lastName);
        if (email      != null && !email.isBlank()) {
            // make sure new email is not taken by a *different* student
            studentRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getStudentId().equals(studentId)) {
                    throw new IllegalArgumentException("Email already in use: " + email);
                }
            });
            student.setEmail(email);
        }
        if (dateOfBirth != null) student.setDateOfBirth(dateOfBirth);
        if (major       != null && !major.isBlank()) student.setMajor(major);

        return studentRepository.update(student);
    }

    /**
     * Deletes a student.
     *
     * @return {@code true} if deleted
     * @throws NoSuchElementException if not found
     */
    public boolean deleteStudent(String studentId) {
        getStudent(studentId); // throws if absent
        return studentRepository.delete(studentId);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByMajor(String major) {
        return studentRepository.findByMajor(major);
    }

    // -------------------------------------------------------------------------
    // CSV Import / Export
    // -------------------------------------------------------------------------

    /**
     * Reads students from a CSV file and registers them.
     *
     * <p>Expected CSV format (header line required):</p>
     * <pre>firstName,lastName,email,dateOfBirth,major</pre>
     *
     * @param filePath absolute or relative path to the CSV file
     * @return number of students successfully imported
     * @throws IOException if the file cannot be read
     */
    public int importFromCsv(String filePath) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 5) continue; // skip malformed lines

                String firstName   = parts[0].trim();
                String lastName    = parts[1].trim();
                String email       = parts[2].trim();
                LocalDate dob      = LocalDate.parse(parts[3].trim(), DATE_FMT);
                String major       = parts[4].trim();

                try {
                    registerStudent(firstName, lastName, email, dob, major);
                    count++;
                } catch (IllegalArgumentException e) {
                    // skip duplicates / invalid rows but continue importing
                    System.err.println("Skipping row: " + e.getMessage());
                }
            }
        }
        return count;
    }

    /**
     * Writes all students to a CSV file.
     *
     * <p>Output format:</p>
     * <pre>studentId,firstName,lastName,email,dateOfBirth,major,enrolledAt</pre>
     *
     * @param filePath path to write (file is created or overwritten)
     * @throws IOException if the file cannot be written
     */
    public void exportToCsv(String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.println("studentId,firstName,lastName,email,dateOfBirth,major,enrolledAt");
            for (Student s : studentRepository.findAll()) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        s.getStudentId(),
                        s.getFirstName(),
                        s.getLastName(),
                        s.getEmail(),
                        s.getDateOfBirth().format(DATE_FMT),
                        s.getMajor(),
                        s.getEnrolledAt().format(DT_FMT));
            }
        }
    }
}
