import model.Priority;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.TaskRepository;
import service.TaskService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link service.TaskService}.
 *
 * The repository is mocked with Mockito so these tests exercise the
 * business logic in TaskService in complete isolation from any storage
 * implementation. This is the standard approach for testing service classes.
 *
 * Mockito wiring:
 *   @Mock           — creates a mock for the annotated type
 *   @InjectMocks    — creates the class under test and injects mocks into it
 *   @ExtendWith(MockitoExtension.class) — activates Mockito annotations
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Write tests", "JUnit 5 with Mockito", Priority.HIGH);
    }

    // -------------------------------------------------------------------------
    // createTask()
    // -------------------------------------------------------------------------

    @Test
    void testCreateTask_success() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.createTask("Write tests", "Some description", Priority.HIGH);

        assertNotNull(created);
        assertEquals("Write tests", created.getTitle());
        assertEquals(Priority.HIGH, created.getPriority());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTask_emptyTitle_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("", "description", Priority.LOW));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void testCreateTask_blankTitle_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("   ", "description", Priority.LOW));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void testCreateTask_nullTitle_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(null, "description", Priority.LOW));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void testCreateTask_nullPriority_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask("Title", "description", null));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void testCreateTask_titleTooLong_throwsException() {
        String longTitle = "a".repeat(121);
        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(longTitle, "", Priority.LOW));
    }

    // -------------------------------------------------------------------------
    // completeTask()
    // -------------------------------------------------------------------------

    @Test
    void testCompleteTask_success() {
        UUID taskId = sampleTask.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.update(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.completeTask(taskId);

        assertTrue(result.isCompleted());
        assertNotNull(result.getCompletedAt());
        verify(taskRepository).update(sampleTask);
    }

    @Test
    void testCompleteTask_notFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(taskRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> taskService.completeTask(unknownId));

        verify(taskRepository, never()).update(any());
    }

    // -------------------------------------------------------------------------
    // deleteTask()
    // -------------------------------------------------------------------------

    @Test
    void testDeleteTask_success() {
        UUID taskId = sampleTask.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.delete(taskId)).thenReturn(true);

        assertDoesNotThrow(() -> taskService.deleteTask(taskId));

        verify(taskRepository).delete(taskId);
    }

    @Test
    void testDeleteTask_notFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(taskRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> taskService.deleteTask(unknownId));

        verify(taskRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // getTasksByPriority()
    // -------------------------------------------------------------------------

    @Test
    void testGetTasksByPriority() {
        Task low1 = new Task("Low task 1", "", Priority.LOW);
        Task low2 = new Task("Low task 2", "", Priority.LOW);
        Task high = new Task("High task", "", Priority.HIGH);

        when(taskRepository.findByPriority(Priority.LOW)).thenReturn(List.of(low1, low2));

        List<Task> result = taskService.getTasksByPriority(Priority.LOW);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getPriority() == Priority.LOW));
    }

    // -------------------------------------------------------------------------
    // getCompletionStats()
    // -------------------------------------------------------------------------

    @Test
    void testGetCompletionStats() {
        Task t1 = new Task("Task 1", "", Priority.HIGH);
        Task t2 = new Task("Task 2", "", Priority.HIGH);
        Task t3 = new Task("Task 3", "", Priority.LOW);
        t1.complete();
        t2.complete();

        when(taskRepository.count()).thenReturn(3);
        when(taskRepository.findByCompleted(true)).thenReturn(List.of(t1, t2));

        Map<String, Object> stats = taskService.getCompletionStats();

        assertEquals(3, stats.get("total"));
        assertEquals(2, stats.get("completed"));
        assertEquals(1, stats.get("pending"));
        // 2/3 = 66.7%
        assertEquals(66.7, stats.get("completionRate"));
    }

    @Test
    void testGetCompletionStats_empty() {
        when(taskRepository.count()).thenReturn(0);
        when(taskRepository.findByCompleted(true)).thenReturn(List.of());

        Map<String, Object> stats = taskService.getCompletionStats();

        assertEquals(0, stats.get("total"));
        assertEquals(0, stats.get("completed"));
        assertEquals(0, stats.get("pending"));
        assertEquals(0.0, stats.get("completionRate"));
    }

    // -------------------------------------------------------------------------
    // getAllTasks() — sort order
    // -------------------------------------------------------------------------

    @Test
    void testGetAllTasks_sortedByPriorityDescending() {
        Task low  = new Task("Low task",    "", Priority.LOW);
        Task high = new Task("High task",   "", Priority.HIGH);
        Task med  = new Task("Medium task", "", Priority.MEDIUM);

        when(taskRepository.findAll()).thenReturn(List.of(low, high, med));

        List<Task> result = taskService.getAllTasks();

        assertEquals(Priority.HIGH,   result.get(0).getPriority());
        assertEquals(Priority.MEDIUM, result.get(1).getPriority());
        assertEquals(Priority.LOW,    result.get(2).getPriority());
    }

    // -------------------------------------------------------------------------
    // updateTask()
    // -------------------------------------------------------------------------

    @Test
    void testUpdateTask_success() {
        UUID taskId = sampleTask.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.update(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = taskService.updateTask(taskId, "New title", null, Priority.URGENT);

        assertEquals("New title", updated.getTitle());
        assertEquals(Priority.URGENT, updated.getPriority());
    }

    @Test
    void testUpdateTask_blankTitle_throwsException() {
        UUID taskId = sampleTask.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTask(taskId, "   ", null, null));
    }
}
