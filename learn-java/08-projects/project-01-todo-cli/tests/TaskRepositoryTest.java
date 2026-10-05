import model.Priority;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.InMemoryTaskRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link repository.InMemoryTaskRepository}.
 *
 * These tests verify the repository's behaviour directly without using
 * mocks — the repository is the unit under test here. For each test we
 * create a fresh repository instance to ensure complete isolation.
 */
class TaskRepositoryTest {

    private InMemoryTaskRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTaskRepository();
    }

    // -------------------------------------------------------------------------
    // save() and findById()
    // -------------------------------------------------------------------------

    @Test
    void testSaveAndFindById() {
        Task task = new Task("Buy milk", "", Priority.LOW);
        repository.save(task);

        Optional<Task> found = repository.findById(task.getId());

        assertTrue(found.isPresent());
        assertEquals(task, found.get());
    }

    @Test
    void testFindById_notFound_returnsEmpty() {
        Optional<Task> result = repository.findById(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void testFindById_null_returnsEmpty() {
        Optional<Task> result = repository.findById(null);

        assertFalse(result.isPresent());
    }

    @Test
    void testSave_duplicateId_throwsException() {
        Task task = new Task("Task", "", Priority.LOW);
        repository.save(task);

        assertThrows(IllegalArgumentException.class, () -> repository.save(task));
    }

    // -------------------------------------------------------------------------
    // findAll()
    // -------------------------------------------------------------------------

    @Test
    void testFindAll_emptyRepository() {
        List<Task> result = repository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_returnsAllSavedTasks() {
        Task t1 = new Task("Task 1", "", Priority.LOW);
        Task t2 = new Task("Task 2", "", Priority.HIGH);
        Task t3 = new Task("Task 3", "", Priority.MEDIUM);

        repository.save(t1);
        repository.save(t2);
        repository.save(t3);

        List<Task> all = repository.findAll();

        assertEquals(3, all.size());
        assertTrue(all.contains(t1));
        assertTrue(all.contains(t2));
        assertTrue(all.contains(t3));
    }

    @Test
    void testFindAll_returnedListIsImmutable() {
        repository.save(new Task("Task", "", Priority.LOW));
        List<Task> all = repository.findAll();

        assertThrows(UnsupportedOperationException.class, () -> all.add(new Task("X", "", Priority.LOW)));
    }

    // -------------------------------------------------------------------------
    // findByCompleted()
    // -------------------------------------------------------------------------

    @Test
    void testFindByCompleted_pending() {
        Task pending = new Task("Pending", "", Priority.LOW);
        Task completed = new Task("Completed", "", Priority.LOW);
        completed.complete();

        repository.save(pending);
        repository.save(completed);

        List<Task> pendingTasks = repository.findByCompleted(false);

        assertEquals(1, pendingTasks.size());
        assertEquals(pending, pendingTasks.get(0));
    }

    @Test
    void testFindByCompleted_completed() {
        Task pending = new Task("Pending", "", Priority.LOW);
        Task completed1 = new Task("Completed 1", "", Priority.HIGH);
        Task completed2 = new Task("Completed 2", "", Priority.MEDIUM);
        completed1.complete();
        completed2.complete();

        repository.save(pending);
        repository.save(completed1);
        repository.save(completed2);

        List<Task> completedTasks = repository.findByCompleted(true);

        assertEquals(2, completedTasks.size());
        assertTrue(completedTasks.contains(completed1));
        assertTrue(completedTasks.contains(completed2));
    }

    // -------------------------------------------------------------------------
    // findByPriority()
    // -------------------------------------------------------------------------

    @Test
    void testFindByPriority() {
        Task low1  = new Task("Low 1",  "", Priority.LOW);
        Task low2  = new Task("Low 2",  "", Priority.LOW);
        Task high  = new Task("High",   "", Priority.HIGH);

        repository.save(low1);
        repository.save(low2);
        repository.save(high);

        List<Task> lowTasks = repository.findByPriority(Priority.LOW);

        assertEquals(2, lowTasks.size());
        assertTrue(lowTasks.stream().allMatch(t -> t.getPriority() == Priority.LOW));
    }

    @Test
    void testFindByPriority_noneFound_returnsEmpty() {
        repository.save(new Task("Task", "", Priority.LOW));

        List<Task> urgent = repository.findByPriority(Priority.URGENT);

        assertTrue(urgent.isEmpty());
    }

    @Test
    void testFindByPriority_null_returnsEmpty() {
        repository.save(new Task("Task", "", Priority.LOW));

        List<Task> result = repository.findByPriority(null);

        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void testDelete_existingTask_returnsTrue() {
        Task task = new Task("Task", "", Priority.LOW);
        repository.save(task);

        boolean result = repository.delete(task.getId());

        assertTrue(result);
        assertFalse(repository.findById(task.getId()).isPresent());
    }

    @Test
    void testDelete_nonExistentTask_returnsFalse() {
        boolean result = repository.delete(UUID.randomUUID());

        assertFalse(result);
    }

    @Test
    void testDelete_null_returnsFalse() {
        boolean result = repository.delete(null);

        assertFalse(result);
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void testUpdate_existingTask() {
        Task task = new Task("Original", "", Priority.LOW);
        repository.save(task);

        task.update("Updated", "New desc", Priority.HIGH);
        Task updated = repository.update(task);

        assertEquals("Updated", updated.getTitle());
        assertEquals("New desc", updated.getDescription());
        assertEquals(Priority.HIGH, updated.getPriority());

        // Verify retrieval returns the updated version
        Task retrieved = repository.findById(task.getId()).orElseThrow();
        assertEquals("Updated", retrieved.getTitle());
    }

    @Test
    void testUpdate_nonExistentTask_throwsException() {
        Task task = new Task("Task", "", Priority.LOW);
        // Note: task was never saved

        assertThrows(NoSuchElementException.class, () -> repository.update(task));
    }

    // -------------------------------------------------------------------------
    // count()
    // -------------------------------------------------------------------------

    @Test
    void testCountIncreasesOnSave() {
        assertEquals(0, repository.count());

        repository.save(new Task("Task 1", "", Priority.LOW));
        assertEquals(1, repository.count());

        repository.save(new Task("Task 2", "", Priority.HIGH));
        assertEquals(2, repository.count());
    }

    @Test
    void testCountDecreasesOnDelete() {
        Task task = new Task("Task", "", Priority.LOW);
        repository.save(task);
        assertEquals(1, repository.count());

        repository.delete(task.getId());
        assertEquals(0, repository.count());
    }
}
