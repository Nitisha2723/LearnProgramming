package service;

import model.Course;
import model.Enrollment;
import model.Enrollment.EnrollmentStatus;
import repository.CourseRepository;
import repository.EnrollmentRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Business logic for enrolling and managing student–course relationships.
 */
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository     courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository     = courseRepository;
    }

    // -------------------------------------------------------------------------
    // Core enrollment operations
    // -------------------------------------------------------------------------

    /**
     * Enrolls a student in a course.
     *
     * <p>Business rules:</p>
     * <ul>
     *   <li>Course must exist.</li>
     *   <li>Course must not be full.</li>
     *   <li>Student must not already have an ACTIVE enrollment in that course.</li>
     * </ul>
     *
     * @return the newly created {@link Enrollment}
     */
    public Enrollment enrollStudent(String studentId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Course not found: " + courseId));

        if (course.isFull()) {
            throw new IllegalStateException(
                    "Course " + course.getCourseCode() + " is full.");
        }

        // Check for existing ACTIVE enrollment
        enrollmentRepository.findByStudentAndCourse(studentId, courseId)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .ifPresent(e -> {
                    throw new IllegalStateException(
                            "Student is already enrolled in course: " + courseId);
                });

        // Update the course's roster
        course.addStudent(studentId);
        courseRepository.update(course);

        // Persist the enrollment record
        Enrollment enrollment = new Enrollment(studentId, courseId);
        return enrollmentRepository.save(enrollment);
    }

    /**
     * Drops (withdraws) a student from a course.
     *
     * @param enrollmentId the UUID of the enrollment to drop
     * @throws NoSuchElementException if the enrollment does not exist
     */
    public Enrollment dropCourse(String enrollmentId) {
        Enrollment enrollment = getEnrollment(enrollmentId);

        enrollment.drop(); // throws if already dropped/completed

        // Remove from course roster
        courseRepository.findById(enrollment.getCourseId()).ifPresent(course -> {
            course.removeStudent(enrollment.getStudentId());
            courseRepository.update(course);
        });

        return enrollmentRepository.update(enrollment);
    }

    /**
     * Marks an enrollment as completed (end of semester).
     *
     * @param enrollmentId the UUID of the enrollment to complete
     */
    public Enrollment completeCourse(String enrollmentId) {
        Enrollment enrollment = getEnrollment(enrollmentId);
        enrollment.complete();
        return enrollmentRepository.update(enrollment);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public List<Enrollment> getStudentEnrollments(String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> getCourseEnrollments(String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    /**
     * Returns only ACTIVE enrollments for a student.
     */
    public List<Enrollment> getActiveEnrollments(String studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Transfer
    // -------------------------------------------------------------------------

    /**
     * Moves a student from one course to another atomically.
     *
     * <p>Drops the original enrollment and creates a new one in the target course.
     * If the target course is full the operation is aborted (original enrollment
     * remains ACTIVE).</p>
     *
     * @return the new enrollment in the target course
     */
    public Enrollment transferStudent(String studentId, String fromCourseId, String toCourseId) {
        // Find the existing active enrollment
        Enrollment existing = enrollmentRepository
                .findByStudentAndCourse(studentId, fromCourseId)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException(
                        "No active enrollment for student " + studentId
                                + " in course " + fromCourseId));

        // Drop from the source (validates target first to keep atomicity)
        Course target = courseRepository.findById(toCourseId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Target course not found: " + toCourseId));
        if (target.isFull()) {
            throw new IllegalStateException(
                    "Target course " + target.getCourseCode() + " is full.");
        }

        dropCourse(existing.getEnrollmentId());
        return enrollStudent(studentId, toCourseId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Enrollment getEnrollment(String enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Enrollment not found: " + enrollmentId));
    }
}
