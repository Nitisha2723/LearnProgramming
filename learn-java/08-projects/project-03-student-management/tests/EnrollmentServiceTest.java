import model.Course;
import model.Enrollment;
import model.Enrollment.EnrollmentStatus;
import org.junit.jupiter.api.*;
import repository.InMemoryCourseRepository;
import repository.InMemoryEnrollmentRepository;
import service.EnrollmentService;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EnrollmentService}.
 */
@DisplayName("EnrollmentService Tests")
class EnrollmentServiceTest {

    private InMemoryCourseRepository     courseRepo;
    private InMemoryEnrollmentRepository enrollmentRepo;
    private EnrollmentService            service;

    // IDs used across tests
    private String student1;
    private String student2;
    private String student3;
    private String courseSmallId; // capacity=2
    private String courseLargeId; // capacity=30

    @BeforeEach
    void setUp() {
        courseRepo     = new InMemoryCourseRepository();
        enrollmentRepo = new InMemoryEnrollmentRepository();
        service        = new EnrollmentService(enrollmentRepo, courseRepo);

        student1 = "student-001";
        student2 = "student-002";
        student3 = "student-003";

        Course small = new Course("CS101", "Intro", "teacher-001", 2, 3);
        Course large = new Course("MATH201", "Calculus", "teacher-002", 30, 4);
        courseRepo.save(small);
        courseRepo.save(large);
        courseSmallId = small.getCourseId();
        courseLargeId = large.getCourseId();
    }

    // -------------------------------------------------------------------------
    // enrollStudent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("enrollStudent: creates active enrollment and updates course roster")
    void enrollStudent_success() {
        Enrollment e = service.enrollStudent(student1, courseSmallId);

        assertNotNull(e.getEnrollmentId());
        assertEquals(student1,          e.getStudentId());
        assertEquals(courseSmallId,     e.getCourseId());
        assertEquals(EnrollmentStatus.ACTIVE, e.getStatus());
        assertNotNull(e.getEnrolledDate());

        // Course roster should reflect the addition
        Course updated = courseRepo.findById(courseSmallId).get();
        assertEquals(1, updated.getEnrollmentCount());
        assertTrue(updated.getEnrolledStudentIds().contains(student1));
    }

    @Test
    @DisplayName("enrollStudent: throws when course is full")
    void enrollStudent_courseFull_throws() {
        service.enrollStudent(student1, courseSmallId);
        service.enrollStudent(student2, courseSmallId); // fills capacity=2

        assertThrows(IllegalStateException.class, () ->
                service.enrollStudent(student3, courseSmallId));
    }

    @Test
    @DisplayName("enrollStudent: throws when student already actively enrolled")
    void enrollStudent_alreadyEnrolled_throws() {
        service.enrollStudent(student1, courseSmallId);

        assertThrows(IllegalStateException.class, () ->
                service.enrollStudent(student1, courseSmallId));
    }

    @Test
    @DisplayName("enrollStudent: throws when course does not exist")
    void enrollStudent_unknownCourse_throws() {
        assertThrows(NoSuchElementException.class, () ->
                service.enrollStudent(student1, "bad-course-id"));
    }

    // -------------------------------------------------------------------------
    // dropCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("dropCourse: marks enrollment DROPPED and removes student from roster")
    void dropCourse_success() {
        Enrollment e = service.enrollStudent(student1, courseSmallId);

        Enrollment dropped = service.dropCourse(e.getEnrollmentId());

        assertEquals(EnrollmentStatus.DROPPED, dropped.getStatus());
        Course updated = courseRepo.findById(courseSmallId).get();
        assertFalse(updated.getEnrolledStudentIds().contains(student1));
    }

    @Test
    @DisplayName("dropCourse: throws when enrollment already dropped")
    void dropCourse_alreadyDropped_throws() {
        Enrollment e = service.enrollStudent(student1, courseSmallId);
        service.dropCourse(e.getEnrollmentId());

        assertThrows(IllegalStateException.class, () ->
                service.dropCourse(e.getEnrollmentId()));
    }

    @Test
    @DisplayName("dropCourse: throws when enrollment not found")
    void dropCourse_notFound_throws() {
        assertThrows(NoSuchElementException.class, () ->
                service.dropCourse("non-existent-id"));
    }

    // -------------------------------------------------------------------------
    // completeCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("completeCourse: marks enrollment COMPLETED")
    void completeCourse_success() {
        Enrollment e = service.enrollStudent(student1, courseSmallId);
        Enrollment completed = service.completeCourse(e.getEnrollmentId());

        assertEquals(EnrollmentStatus.COMPLETED, completed.getStatus());
    }

    // -------------------------------------------------------------------------
    // transferStudent
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transferStudent: drops source, creates active enrollment in target")
    void transferStudent_success() {
        Enrollment original = service.enrollStudent(student1, courseSmallId);

        Enrollment transferred = service.transferStudent(student1, courseSmallId, courseLargeId);

        // New enrollment in target course
        assertEquals(courseLargeId, transferred.getCourseId());
        assertEquals(EnrollmentStatus.ACTIVE, transferred.getStatus());

        // Old enrollment should now be DROPPED
        Enrollment old = enrollmentRepo.findById(original.getEnrollmentId()).get();
        assertEquals(EnrollmentStatus.DROPPED, old.getStatus());
    }

    @Test
    @DisplayName("transferStudent: throws when source enrollment not active")
    void transferStudent_notEnrolled_throws() {
        assertThrows(NoSuchElementException.class, () ->
                service.transferStudent(student1, courseSmallId, courseLargeId));
    }

    @Test
    @DisplayName("transferStudent: throws when target course is full")
    void transferStudent_targetFull_throws() {
        // Fill the large course with 2 students, then reduce capacity to 2 (hack via object ref)
        // Easier: enroll student1 in large, then make it "full" manually
        // Instead: use two small courses
        Course tiny = new Course("TINY", "Tiny Course", "t", 1, 1);
        courseRepo.save(tiny);
        String tinyId = tiny.getCourseId();
        service.enrollStudent(student2, tinyId); // fill tiny (cap=1)

        service.enrollStudent(student1, courseSmallId);

        assertThrows(IllegalStateException.class, () ->
                service.transferStudent(student1, courseSmallId, tinyId));
    }

    // -------------------------------------------------------------------------
    // getStudentEnrollments / getCourseEnrollments
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getStudentEnrollments: returns all enrollments for the student")
    void getStudentEnrollments_returnsAll() {
        service.enrollStudent(student1, courseSmallId);
        service.enrollStudent(student1, courseLargeId);

        assertEquals(2, service.getStudentEnrollments(student1).size());
    }

    @Test
    @DisplayName("getCourseEnrollments: returns all enrollments for the course")
    void getCourseEnrollments_returnsAll() {
        service.enrollStudent(student1, courseLargeId);
        service.enrollStudent(student2, courseLargeId);

        assertEquals(2, service.getCourseEnrollments(courseLargeId).size());
    }
}
