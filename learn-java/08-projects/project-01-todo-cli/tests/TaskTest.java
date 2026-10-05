import model.Priority;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link model.Task} model class.
 *
 * These tests verify the Task's own behaviour in isolation —
 * no repository, no service, no dependencies.
 */
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Buy groceries", "Milk, eggs, bread", Priority.MEDIUM);
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Test
    void testTaskCreationSetsDefaultFields() {
        assertNotNull(task.getId(), "id should be auto-generated");
        assertEquals("Buy groceries", task.getTitle());
        assertEquals("Milk, eggs, bread", task.getDescription());
        assertEquals(Priority.MEDIUM, task.getPriority());
        assertFalse(task.isCompleted(), "new task should not be completed");
        assertNotNull(task.getCreatedAt(), "createdAt should be set");
    }

    @Test
    void testCreatedAtIsSetToApproximatelyNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Task freshTask = new Task("Fresh task", "", Priority.LOW);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(freshTask.getCreatedAt().isAfter(before),
                "createdAt should be after the time just before construction");
        assertTrue(freshTask.getCreatedAt().isBefore(after),
                "createdAt should be before the time just after construction");
    }

    @Test
    void testCompletedAtIsNullBeforeCompletion() {
        assertNull(task.getCompletedAt(),
                "completedAt must be null before the task is completed");
    }

    // -------------------------------------------------------------------------
    // complete()
    // -------------------------------------------------------------------------

    @Test
    void testCompleteTask() {
        task.complete();

        assertTrue(task.isCompleted());
    }

    @Test
    void testCompletedAtSetAfterCompletion() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        task.complete();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(task.getCompletedAt(), "completedAt should be set after completion");
        assertTrue(task.getCompletedAt().isAfter(before));
        assertTrue(task.getCompletedAt().isBefore(after));
    }

    @Test
    void testCompleteTaskIsIdempotent() {
        task.complete();
        LocalDateTime firstCompletedAt = task.getCompletedAt();

        // Completing again should not change completedAt
        task.complete();

        assertEquals(firstCompletedAt, task.getCompletedAt(),
                "completedAt should not change on subsequent calls to complete()");
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void testUpdateTask() {
        task.update("Updated title", "Updated description", Priority.HIGH);

        assertEquals("Updated title", task.getTitle());
        assertEquals("Updated description", task.getDescription());
        assertEquals(Priority.HIGH, task.getPriority());
    }

    @Test
    void testUpdateWithNullTitleLeavesOriginal() {
        task.update(null, "New description", null);

        assertEquals("Buy groceries", task.getTitle(),
                "title should not change when null is passed");
        assertEquals("New description", task.getDescription());
        assertEquals(Priority.MEDIUM, task.getPriority(),
                "priority should not change when null is passed");
    }

    @Test
    void testUpdateWithBlankTitleLeavesOriginal() {
        task.update("   ", "New description", null);

        assertEquals("Buy groceries", task.getTitle(),
                "title should not change when blank string is passed");
    }

    // -------------------------------------------------------------------------
    // equals() and hashCode()
    // -------------------------------------------------------------------------

    @Test
    void testEqualsBasedOnId() {
        // Same object is equal to itself
        assertEquals(task, task);

        // Different object with same id (simulate by using the same task reference)
        Task samePriority = new Task("Different title", "", Priority.HIGH);
        assertNotEquals(task, samePriority,
                "two tasks with different IDs must not be equal");
    }

    @Test
    void testHashCodeConsistentWithEquals() {
        Task sameTask = task;
        assertEquals(task.hashCode(), sameTask.hashCode());
    }

    @Test
    void testNotEqualToNull() {
        assertNotEquals(null, task);
    }

    @Test
    void testNotEqualToOtherType() {
        assertNotEquals("some string", task);
    }

    // -------------------------------------------------------------------------
    // Constructor validation
    // -------------------------------------------------------------------------

    @Test
    void testConstructorRejectsNullTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> new Task(null, "desc", Priority.LOW));
    }

    @Test
    void testConstructorRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> new Task("   ", "desc", Priority.LOW));
    }

    @Test
    void testConstructorRejectsNullPriority() {
        assertThrows(IllegalArgumentException.class,
                () -> new Task("Title", "desc", null));
    }

    @Test
    void testConstructorRejectsNullDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new Task("Title", null, Priority.LOW));
    }

    // -------------------------------------------------------------------------
    // toString()
    // -------------------------------------------------------------------------

    @Test
    void testToStringContainsDoneForCompletedTask() {
        task.complete();
        assertTrue(task.toString().contains("[DONE]"),
                "toString of completed task should contain [DONE]");
    }

    @Test
    void testToStringContainsEmptyBracketsForPendingTask() {
        assertTrue(task.toString().contains("[    ]"),
                "toString of pending task should contain [    ]");
    }

    @Test
    void testToStringContainsTitle() {
        assertTrue(task.toString().contains("Buy groceries"));
    }
}
