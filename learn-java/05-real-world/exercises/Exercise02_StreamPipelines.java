import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Exercise02_StreamPipelines.java
 *
 * GOAL: Practice writing stream pipelines for 5 different employee data queries.
 *
 * RULES:
 * - Each method MUST use streams — no for loops allowed
 * - No intermediate mutable state (no variables inside the pipeline)
 * - Use method references where possible
 */
public class Exercise02_StreamPipelines {

    record Employee(
        String id,
        String name,
        String department,
        String city,
        double salary,
        int yearsExperience,
        boolean isManager,
        List<String> skills
    ) {}

    // Test data
    private static final List<Employee> EMPLOYEES = List.of(
        new Employee("E01", "Alice",   "Engineering", "Berlin",  95000, 7,  true,  List.of("Java","AWS","Kubernetes")),
        new Employee("E02", "Bob",     "Marketing",   "Munich",  72000, 3,  false, List.of("Excel","Salesforce")),
        new Employee("E03", "Charlie", "Engineering", "Berlin",  88000, 5,  false, List.of("Java","Python","Docker")),
        new Employee("E04", "Diana",   "HR",          "Hamburg", 65000, 4,  false, List.of("HR Systems","Excel")),
        new Employee("E05", "Eve",     "Engineering", "Berlin", 105000, 10, true,  List.of("Java","Kubernetes","AWS")),
        new Employee("E06", "Frank",   "Marketing",   "Munich",  68000, 2,  false, List.of("Google Ads","Excel")),
        new Employee("E07", "Grace",   "Engineering", "Hamburg", 92000, 6,  true,  List.of("Python","TensorFlow","AWS")),
        new Employee("E08", "Henry",   "HR",          "Berlin",  63000, 1,  false, List.of("HR Systems")),
        new Employee("E09", "Ivan",    "Engineering", "Munich",  78000, 4,  false, List.of("Java","Spring","MySQL")),
        new Employee("E10", "Julia",   "Engineering", "Berlin",  99000, 8,  true,  List.of("Java","AWS","Terraform")),
        new Employee("E11", "Karl",    "Marketing",   "Hamburg", 75000, 5,  true,  List.of("Analytics","Excel","Python")),
        new Employee("E12", "Lisa",    "Engineering", "Munich",  82000, 3,  false, List.of("Python","ML","TensorFlow"))
    );

    // =========================================================
    // QUERY 1: Salary Summary by Department
    //
    // For each department, compute:
    //   - headcount, total salary, average salary, max salary
    //
    // Return: Map<String, String> where key = department name,
    //         value = formatted string like:
    //         "5 employees, avg=$90000, max=$105000, total=$450000"
    //
    // Hint: Collectors.groupingBy + Collectors.collectingAndThen
    //       or Collectors.toMap with manual aggregation
    // =========================================================
    public static Map<String, String> salarySummaryByDepartment() {
        // YOUR CODE HERE
        return new HashMap<>();
    }

    // =========================================================
    // QUERY 2: Skill Gap Analysis
    //
    // Find all skills that ONLY managers have (not held by any non-manager).
    // Return sorted alphabetically.
    //
    // Hint:
    //   - Get all skills of managers (flatMap)
    //   - Get all skills of non-managers (flatMap)
    //   - Compute the difference (manager skills NOT in non-manager skills)
    // =========================================================
    public static List<String> managerOnlySkills() {
        // YOUR CODE HERE
        return new ArrayList<>();
    }

    // =========================================================
    // QUERY 3: Multi-City Employees
    //
    // Hypothetical: some employees have roles in multiple cities.
    // Find all DISTINCT cities where Engineering employees work,
    // sorted alphabetically, formatted as a comma-separated string.
    //
    // Return: "Berlin, Hamburg, Munich" (or whatever cities exist)
    // =========================================================
    public static String engineeringCities() {
        // YOUR CODE HERE
        return "";
    }

    // =========================================================
    // QUERY 4: Promotion Eligibility
    //
    // An employee is eligible for promotion if:
    //   - 5+ years experience
    //   - salary < 95000
    //   - NOT already a manager
    //
    // Return a Map<String, Double> of eligible employee NAME → their salary
    // sorted by salary ascending (lowest paid first — they need it most)
    //
    // Hint: Use Collectors.toMap with a LinkedHashMap to preserve insertion order
    // =========================================================
    public static Map<String, Double> promotionCandidates() {
        // YOUR CODE HERE
        return new LinkedHashMap<>();
    }

    // =========================================================
    // QUERY 5: Department Skill Matrix
    //
    // For each department, list the unique skills that employees in
    // that department collectively have.
    //
    // Return: Map<String, List<String>>
    //   key = department name
    //   value = sorted list of unique skills
    //
    // Hint: groupingBy + flatMapping (Java 9+) or groupingBy + downstream stream
    // =========================================================
    public static Map<String, List<String>> departmentSkillMatrix() {
        // YOUR CODE HERE
        return new HashMap<>();
    }

    // =========================================================
    // Main — Test your solutions
    // =========================================================
    public static void main(String[] args) {
        System.out.println("=== Query 1: Salary Summary by Department ===");
        salarySummaryByDepartment().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %-12s %s%n", e.getKey(), e.getValue()));

        System.out.println("\n=== Query 2: Manager-Only Skills ===");
        System.out.println(managerOnlySkills());
        // Expected: skills that managers have but no non-manager has

        System.out.println("\n=== Query 3: Engineering Cities ===");
        System.out.println(engineeringCities());
        // Expected: "Berlin, Hamburg, Munich"

        System.out.println("\n=== Query 4: Promotion Candidates ===");
        promotionCandidates().forEach((name, salary) ->
            System.out.printf("  %-10s $%.0f%n", name, salary));

        System.out.println("\n=== Query 5: Department Skill Matrix ===");
        departmentSkillMatrix().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %-12s %s%n", e.getKey(), e.getValue()));
    }
}
