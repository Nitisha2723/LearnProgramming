package cli;

import model.Priority;
import model.Task;
import service.TaskService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Command-line interface for the TODO task manager.
 *
 * This class is responsible exclusively for user interaction: it reads
 * input, calls the service layer, and formats output. There is no business
 * logic here. If you find yourself writing an if-statement about task
 * priorities or completion rules in this class, move it to TaskService.
 */
public class TaskCli {

    private static final String SEPARATOR = "-------------------------------------------";
    private static final String THICK_SEPARATOR = "===========================================";

    private final TaskService taskService;
    private final Scanner scanner;
    private boolean running;

    /**
     * Creates the CLI with the given task service and System.in as input.
     *
     * @param taskService the service layer (must not be null)
     */
    public TaskCli(TaskService taskService) {
        if (taskService == null) {
            throw new IllegalArgumentException("TaskService must not be null.");
        }
        this.taskService = taskService;
        this.scanner = new Scanner(System.in);
        this.running = false;
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------

    /**
     * Starts the CLI. Blocks until the user chooses Quit (option 7).
     */
    public void run() {
        running = true;
        printWelcome();

        while (running) {
            printMenu();
            int choice = readMenuChoice(1, 7);
            handleMenuChoice(choice);
        }

        System.out.println();
        System.out.println(THICK_SEPARATOR);
        System.out.println("  Goodbye! Your tasks have been saved in memory.");
        System.out.println(THICK_SEPARATOR);
        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Menu dispatching
    // -------------------------------------------------------------------------

    private void handleMenuChoice(int choice) {
        System.out.println();
        switch (choice) {
            case 1 -> handleAddTask();
            case 2 -> handleListAll();
            case 3 -> handleCompleteTask();
            case 4 -> handleDeleteTask();
            case 5 -> handleFilterByPriority();
            case 6 -> handleViewStats();
            case 7 -> running = false;
        }
    }

    // -------------------------------------------------------------------------
    // Feature handlers
    // -------------------------------------------------------------------------

    private void handleAddTask() {
        System.out.println(SEPARATOR);
        System.out.println("  ADD TASK");
        System.out.println(SEPARATOR);

        System.out.print("Title: ");
        String title = scanner.nextLine().strip();

        System.out.print("Description (press Enter to skip): ");
        String description = scanner.nextLine().strip();

        Priority priority = readPriority();

        try {
            Task task = taskService.createTask(title, description, priority);
            System.out.println();
            System.out.println("  Task created successfully:");
            printTask(task);
        } catch (IllegalArgumentException e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }

    private void handleListAll() {
        System.out.println(SEPARATOR);
        System.out.println("  ALL TASKS");
        System.out.println(SEPARATOR);

        List<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks found. Add one with option 1.");
            return;
        }

        printTaskList(tasks);
    }

    private void handleCompleteTask() {
        System.out.println(SEPARATOR);
        System.out.println("  COMPLETE TASK");
        System.out.println(SEPARATOR);

        UUID id = readTaskId("Enter the task ID (first 8 characters): ");
        if (id == null) return;

        try {
            Task task = taskService.completeTask(id);
            System.out.println("  Task marked as complete:");
            printTask(task);
        } catch (NoSuchElementException e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }

    private void handleDeleteTask() {
        System.out.println(SEPARATOR);
        System.out.println("  DELETE TASK");
        System.out.println(SEPARATOR);

        // Show the task list first so the user can see IDs
        List<Task> tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("  No tasks to delete.");
            return;
        }
        printTaskList(tasks);

        UUID id = readTaskId("\nEnter the task ID to delete: ");
        if (id == null) return;

        System.out.print("  Are you sure? This cannot be undone. (y/N): ");
        String confirm = scanner.nextLine().strip().toLowerCase();
        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("  Deletion cancelled.");
            return;
        }

        try {
            taskService.deleteTask(id);
            System.out.println("  Task deleted.");
        } catch (NoSuchElementException e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }

    private void handleFilterByPriority() {
        System.out.println(SEPARATOR);
        System.out.println("  FILTER BY PRIORITY");
        System.out.println(SEPARATOR);

        Priority priority = readPriority();
        List<Task> tasks = taskService.getTasksByPriority(priority);

        System.out.println();
        System.out.printf("  Tasks with priority %s:%n", priority.getDisplayLabel());
        System.out.println(SEPARATOR);

        if (tasks.isEmpty()) {
            System.out.println("  No tasks found for this priority.");
        } else {
            printTaskList(tasks);
        }
    }

    private void handleViewStats() {
        System.out.println(SEPARATOR);
        System.out.println("  TASK STATISTICS");
        System.out.println(SEPARATOR);

        Map<String, Object> stats = taskService.getCompletionStats();

        System.out.printf("  Total tasks:      %d%n", stats.get("total"));
        System.out.printf("  Completed:        %d%n", stats.get("completed"));
        System.out.printf("  Pending:          %d%n", stats.get("pending"));
        System.out.printf("  Completion rate:  %.1f%%%n", stats.get("completionRate"));
        System.out.println(SEPARATOR);

        // Show a simple text progress bar
        int total = (int) stats.get("total");
        int completed = (int) stats.get("completed");
        if (total > 0) {
            int barWidth = 30;
            int filled = (int) Math.round((completed * (double) barWidth) / total);
            String bar = "[" + "#".repeat(filled) + "-".repeat(barWidth - filled) + "]";
            System.out.printf("  Progress: %s%n", bar);
        }
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    private int readMenuChoice(int min, int max) {
        while (true) {
            System.out.print("Choice (" + min + "-" + max + "): ");
            String input = scanner.nextLine().strip();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("  Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number.");
            }
        }
    }

    private Priority readPriority() {
        System.out.println("  Select priority:");
        System.out.println("    1. Low");
        System.out.println("    2. Medium");
        System.out.println("    3. High");
        System.out.println("    4. Urgent");

        int choice = readMenuChoice(1, 4);
        return switch (choice) {
            case 1 -> Priority.LOW;
            case 2 -> Priority.MEDIUM;
            case 3 -> Priority.HIGH;
            case 4 -> Priority.URGENT;
            default -> throw new IllegalStateException("Unreachable");
        };
    }

    /**
     * Reads a task ID from the user. The user may enter either the short
     * 8-character prefix shown in the task list or the full UUID.
     *
     * Returns null (and prints an error) if the input cannot be resolved to a UUID.
     */
    private UUID readTaskId(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().strip();
        if (input.isEmpty()) {
            System.out.println("  No ID entered. Operation cancelled.");
            return null;
        }

        // If the user entered a short 8-char prefix, try to find the full UUID
        if (input.length() == 8) {
            List<Task> all = taskService.getAllTasks();
            for (Task task : all) {
                if (task.getId().toString().startsWith(input)) {
                    return task.getId();
                }
            }
            System.out.println("  No task found with ID prefix: " + input);
            return null;
        }

        // Otherwise parse it as a full UUID
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("  Invalid ID format. Use the 8-character prefix shown in the task list.");
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Output helpers
    // -------------------------------------------------------------------------

    private void printWelcome() {
        System.out.println();
        System.out.println(THICK_SEPARATOR);
        System.out.println("          TODO TASK MANAGER");
        System.out.println("  Stay organised. One task at a time.");
        System.out.println(THICK_SEPARATOR);
    }

    private void printMenu() {
        System.out.println();
        System.out.println(THICK_SEPARATOR);
        System.out.println("  1. Add Task");
        System.out.println("  2. List All Tasks");
        System.out.println("  3. Complete Task");
        System.out.println("  4. Delete Task");
        System.out.println("  5. Filter by Priority");
        System.out.println("  6. View Stats");
        System.out.println("  7. Quit");
        System.out.println(THICK_SEPARATOR);
    }

    private void printTaskList(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("  %3d. %s%n", i + 1, tasks.get(i));
        }
        System.out.printf("%n  Total: %d task(s)%n", tasks.size());
    }

    private void printTask(Task task) {
        System.out.println("  " + task);
        if (!task.getDescription().isEmpty()) {
            System.out.println("       " + task.getDescription());
        }
    }
}
