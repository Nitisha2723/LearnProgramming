import java.util.*;

/**
 * StackQueueDemo.java
 *
 * Real-world demonstrations of Stack, Queue, Deque, and PriorityQueue:
 * - Browser history (Stack)
 * - Task processing queue (Queue)
 * - Priority task queue (PriorityQueue)
 * - Sliding window with Deque
 */
public class StackQueueDemo {

    // =========================================================
    // 1. Browser History — Stack Pattern
    // =========================================================

    static class BrowserHistory {
        private final Deque<String> backStack  = new ArrayDeque<>();
        private final Deque<String> forwardStack = new ArrayDeque<>();
        private String currentUrl = "about:blank";

        void visit(String url) {
            // New navigation clears the forward history (like a real browser)
            backStack.push(currentUrl);
            forwardStack.clear();
            currentUrl = url;
            System.out.println("Visited: " + url);
        }

        String back() {
            if (backStack.isEmpty()) {
                System.out.println("No history to go back to");
                return currentUrl;
            }
            forwardStack.push(currentUrl);
            currentUrl = backStack.pop();
            System.out.println("Back → " + currentUrl);
            return currentUrl;
        }

        String forward() {
            if (forwardStack.isEmpty()) {
                System.out.println("No forward history");
                return currentUrl;
            }
            backStack.push(currentUrl);
            currentUrl = forwardStack.pop();
            System.out.println("Forward → " + currentUrl);
            return currentUrl;
        }

        void status() {
            System.out.println("Current: " + currentUrl);
            System.out.println("Back:    " + backStack);
            System.out.println("Forward: " + forwardStack);
        }
    }

    static void browserHistoryDemo() {
        System.out.println("=== Browser History (Stack) ===\n");

        BrowserHistory browser = new BrowserHistory();
        browser.visit("https://google.com");
        browser.visit("https://java.com");
        browser.visit("https://stackoverflow.com");
        browser.visit("https://github.com");

        System.out.println();
        browser.status();

        System.out.println();
        browser.back();
        browser.back();
        browser.status();

        System.out.println();
        browser.forward();
        browser.status();

        System.out.println();
        browser.visit("https://docs.oracle.com");  // New visit clears forward
        browser.status();
    }

    // =========================================================
    // 2. Task Processing Queue — FIFO Pattern
    // =========================================================

    record Task(int id, String type, String description) {
        @Override public String toString() {
            return String.format("Task#%d[%s]: %s", id, type, description);
        }
    }

    static class TaskProcessor {
        private final Queue<Task> queue = new ArrayDeque<>();
        private int processedCount = 0;

        void submit(Task task) {
            queue.offer(task);
            System.out.println("Submitted: " + task);
        }

        void processNext() {
            Task task = queue.poll();
            if (task == null) {
                System.out.println("No tasks to process");
                return;
            }
            processedCount++;
            System.out.println("Processing: " + task + " (done in 'background')");
        }

        void processAll() {
            System.out.println("Processing all " + queue.size() + " remaining tasks...");
            while (!queue.isEmpty()) {
                processNext();
            }
        }

        void status() {
            System.out.println("Queue size: " + queue.size() + ", Processed: " + processedCount);
            if (!queue.isEmpty()) {
                System.out.println("Next in line: " + queue.peek());
            }
        }
    }

    static void taskQueueDemo() {
        System.out.println("\n=== Task Queue (FIFO) ===\n");

        TaskProcessor processor = new TaskProcessor();

        // Tasks are submitted in various order
        processor.submit(new Task(1, "EMAIL",  "Send welcome email to new user"));
        processor.submit(new Task(2, "RESIZE", "Resize uploaded avatar image"));
        processor.submit(new Task(3, "EMAIL",  "Send weekly newsletter"));
        processor.submit(new Task(4, "REPORT", "Generate monthly sales report"));
        processor.submit(new Task(5, "BACKUP", "Backup user data"));

        System.out.println();
        processor.status();

        System.out.println();
        processor.processNext();
        processor.processNext();
        processor.status();

        System.out.println();
        processor.processAll();
    }

    // =========================================================
    // 3. Priority Task Queue — PriorityQueue
    // =========================================================

    enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    record PriorityTask(int id, Priority priority, String description)
        implements Comparable<PriorityTask> {

        @Override
        public int compareTo(PriorityTask other) {
            // Higher priority ordinal = higher priority enum value = processed first
            return other.priority().ordinal() - this.priority().ordinal();
        }

        @Override public String toString() {
            return String.format("[%s] Task#%d: %s", priority, id, description);
        }
    }

    static void priorityQueueDemo() {
        System.out.println("\n=== Priority Queue ===\n");

        PriorityQueue<PriorityTask> pq = new PriorityQueue<>();

        // Tasks submitted in random order
        pq.offer(new PriorityTask(1, Priority.LOW,      "Cleanup old log files"));
        pq.offer(new PriorityTask(2, Priority.HIGH,     "Restart failing service"));
        pq.offer(new PriorityTask(3, Priority.MEDIUM,   "Send daily report"));
        pq.offer(new PriorityTask(4, Priority.CRITICAL, "Database connection pool exhausted!"));
        pq.offer(new PriorityTask(5, Priority.LOW,      "Update documentation"));
        pq.offer(new PriorityTask(6, Priority.HIGH,     "Deploy security patch"));
        pq.offer(new PriorityTask(7, Priority.MEDIUM,   "Process payment batch"));

        System.out.println("Tasks will be processed in priority order (not submission order):\n");
        int pos = 1;
        while (!pq.isEmpty()) {
            System.out.println(pos++ + ". Processing: " + pq.poll());
        }
    }

    // =========================================================
    // 4. Deque as Sliding Window Maximum
    // =========================================================

    static void slidingWindowDemo() {
        System.out.println("\n=== Sliding Window with Deque ===\n");

        // Scenario: CPU utilization readings every second
        // Find the maximum utilization in any 3-second window
        int[] cpuReadings = {50, 80, 30, 70, 90, 60, 40, 95, 20, 75};
        int windowSize = 3;

        System.out.println("CPU readings: " + Arrays.toString(cpuReadings));
        System.out.println("Window size: " + windowSize + " seconds\n");

        Deque<Integer> windowDeque = new ArrayDeque<>(); // Stores indices
        List<Integer> maxInEachWindow = new ArrayList<>();

        for (int i = 0; i < cpuReadings.length; i++) {
            // Remove indices outside the current window
            while (!windowDeque.isEmpty() && windowDeque.peekFirst() < i - windowSize + 1) {
                windowDeque.pollFirst();
            }

            // Remove indices whose CPU values are smaller than current
            // (They can never be the max of any future window that includes current)
            while (!windowDeque.isEmpty() && cpuReadings[windowDeque.peekLast()] < cpuReadings[i]) {
                windowDeque.pollLast();
            }

            windowDeque.offerLast(i);

            // We have a full window
            if (i >= windowSize - 1) {
                int windowMax = cpuReadings[windowDeque.peekFirst()];
                maxInEachWindow.add(windowMax);
                int windowStart = i - windowSize + 1;
                System.out.printf("Window [%d..%d] = %s → max = %d%n",
                    windowStart, i,
                    Arrays.toString(Arrays.copyOfRange(cpuReadings, windowStart, i + 1)),
                    windowMax);
            }
        }

        System.out.println("\nAll window maximums: " + maxInEachWindow);
        System.out.println("Overall peak: " + Collections.max(maxInEachWindow) + "%");
    }

    // =========================================================
    // 5. Stack for Expression Evaluation
    // =========================================================

    /**
     * Evaluate a postfix (Reverse Polish Notation) expression.
     * Example: "3 4 + 5 *" = (3 + 4) * 5 = 35
     * No parentheses needed — operator follows its operands.
     */
    static int evaluatePostfix(String expression) {
        Deque<Integer> operandStack = new ArrayDeque<>();

        for (String token : expression.split("\\s+")) {
            switch (token) {
                case "+" -> {
                    int b = operandStack.pop(), a = operandStack.pop();
                    operandStack.push(a + b);
                }
                case "-" -> {
                    int b = operandStack.pop(), a = operandStack.pop();
                    operandStack.push(a - b);
                }
                case "*" -> {
                    int b = operandStack.pop(), a = operandStack.pop();
                    operandStack.push(a * b);
                }
                case "/" -> {
                    int b = operandStack.pop(), a = operandStack.pop();
                    operandStack.push(a / b);
                }
                default -> operandStack.push(Integer.parseInt(token));
            }
        }

        return operandStack.pop();
    }

    static void expressionEvaluationDemo() {
        System.out.println("\n=== Expression Evaluation with Stack ===\n");

        // Postfix (RPN) expressions
        String[] expressions = {
            "3 4 +",           // 3 + 4 = 7
            "5 3 - 2 *",       // (5 - 3) * 2 = 4
            "2 3 4 * +",       // 2 + (3 * 4) = 14
            "15 7 1 1 + - / 3 * 2 1 1 + + -"  // Complex expression = 5
        };

        for (String expr : expressions) {
            System.out.printf("  %s = %d%n", expr, evaluatePostfix(expr));
        }
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        browserHistoryDemo();
        taskQueueDemo();
        priorityQueueDemo();
        slidingWindowDemo();
        expressionEvaluationDemo();
    }
}
