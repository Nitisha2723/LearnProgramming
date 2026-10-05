import cli.TaskCli;
import repository.InMemoryTaskRepository;
import repository.TaskRepository;
import service.TaskService;

/**
 * Application entry point.
 *
 * This class is the composition root — the one place in the application
 * that knows about all the concrete types and wires them together.
 *
 * The wiring order is:
 *   1. Create the repository (storage layer)
 *   2. Create the service (business logic layer), injecting the repository
 *   3. Create the CLI (presentation layer), injecting the service
 *   4. Start the CLI
 *
 * Nothing else in the application ever creates these objects directly.
 * Every other class receives its dependencies through its constructor,
 * which makes each class independently testable.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Storage layer
        TaskRepository repository = new InMemoryTaskRepository();

        // 2. Business logic layer
        TaskService taskService = new TaskService(repository);

        // 3. Presentation layer
        TaskCli cli = new TaskCli(taskService);

        // 4. Start
        cli.run();
    }
}
