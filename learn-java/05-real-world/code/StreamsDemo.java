import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * StreamsDemo.java
 *
 * Comprehensive demonstration of Java 8 Streams:
 * - filter, map, flatMap, sorted, distinct, limit
 * - collect, forEach, count, reduce
 * - groupingBy, joining, partitioning
 * - Method references
 * - Real-world employee data pipeline
 */
public class StreamsDemo {

    // =========================================================
    // Domain Model
    // =========================================================

    record Employee(
        String id,
        String name,
        String department,
        String city,
        double salary,
        int yearsExperience,
        List<String> skills
    ) {}

    record Department(String name, String manager, String location) {}

    static final List<Employee> EMPLOYEES = List.of(
        new Employee("E01", "Alice Chen",    "Engineering",  "Berlin",  95000, 7,  List.of("Java", "Kotlin", "AWS")),
        new Employee("E02", "Bob Martinez",  "Marketing",    "Munich",  72000, 3,  List.of("Excel", "Salesforce")),
        new Employee("E03", "Charlie Davis", "Engineering",  "Berlin",  88000, 5,  List.of("Java", "Python", "Docker")),
        new Employee("E04", "Diana Patel",   "HR",           "Hamburg", 65000, 4,  List.of("HR Systems", "Excel")),
        new Employee("E05", "Eve Wilson",    "Engineering",  "Berlin", 105000, 10, List.of("Java", "Kubernetes", "AWS")),
        new Employee("E06", "Frank Johnson", "Marketing",    "Munich",  68000, 2,  List.of("Google Ads", "Excel")),
        new Employee("E07", "Grace Kim",     "Engineering",  "Hamburg", 92000, 6,  List.of("Python", "TensorFlow", "AWS")),
        new Employee("E08", "Henry Brown",   "HR",           "Berlin",  63000, 1,  List.of("HR Systems")),
        new Employee("E09", "Ivan Lee",      "Engineering",  "Munich",  78000, 4,  List.of("Java", "Spring", "MySQL")),
        new Employee("E10", "Julia White",   "Engineering",  "Berlin",  99000, 8,  List.of("Java", "AWS", "Terraform"))
    );

    // =========================================================
    // 1. Basic Operations
    // =========================================================

    static void basicOperationsDemo() {
        System.out.println("=== Basic Stream Operations ===\n");

        // Filter
        System.out.println("Engineers in Berlin:");
        EMPLOYEES.stream()
            .filter(e -> e.department().equals("Engineering"))
            .filter(e -> e.city().equals("Berlin"))
            .map(Employee::name)
            .forEach(name -> System.out.println("  " + name));

        // Map + sorted + limit
        System.out.println("\nTop 3 earners:");
        EMPLOYEES.stream()
            .sorted(Comparator.comparingDouble(Employee::salary).reversed())
            .limit(3)
            .forEach(e -> System.out.printf("  %-15s $%.0f%n", e.name(), e.salary()));

        // count
        long seniorDevs = EMPLOYEES.stream()
            .filter(e -> e.department().equals("Engineering"))
            .filter(e -> e.yearsExperience() >= 5)
            .count();
        System.out.println("\nSenior engineers (5+ years): " + seniorDevs);

        // distinct
        List<String> uniqueCities = EMPLOYEES.stream()
            .map(Employee::city)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Cities: " + uniqueCities);

        // anyMatch, allMatch, noneMatch
        boolean anyRemote = EMPLOYEES.stream().anyMatch(e -> e.city().equals("Remote"));
        boolean allPaid   = EMPLOYEES.stream().allMatch(e -> e.salary() > 0);
        System.out.println("Anyone remote: " + anyRemote);
        System.out.println("All have salary: " + allPaid);
    }

    // =========================================================
    // 2. flatMap — Flatten Nested Structures
    // =========================================================

    static void flatMapDemo() {
        System.out.println("\n=== flatMap Demo ===\n");

        // Collect all unique skills across all employees
        List<String> allSkills = EMPLOYEES.stream()
            .flatMap(e -> e.skills().stream())   // Employee → Stream<String>
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("All skills: " + allSkills);

        // Count how many employees know each skill
        Map<String, Long> skillCounts = EMPLOYEES.stream()
            .flatMap(e -> e.skills().stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        System.out.println("\nSkill popularity:");
        skillCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> System.out.printf("  %-15s : %d employees%n", e.getKey(), e.getValue()));

        // Find employees who know Java AND AWS
        List<String> javaAwsDevs = EMPLOYEES.stream()
            .filter(e -> e.skills().containsAll(List.of("Java", "AWS")))
            .map(Employee::name)
            .collect(Collectors.toList());
        System.out.println("\nJava + AWS developers: " + javaAwsDevs);
    }

    // =========================================================
    // 3. Collectors: Grouping, Joining, Partitioning
    // =========================================================

    static void collectorsDemo() {
        System.out.println("\n=== Collectors Demo ===\n");

        // groupingBy department
        Map<String, List<Employee>> byDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(Employee::department));

        System.out.println("Employees per department:");
        byDept.forEach((dept, emps) ->
            System.out.printf("  %-12s : %d employees%n", dept, emps.size()));

        // groupingBy with downstream: average salary per department
        Map<String, Double> avgSalaryByDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.averagingDouble(Employee::salary)
            ));

        System.out.println("\nAverage salary by department:");
        avgSalaryByDept.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(e -> System.out.printf("  %-12s : $%.0f%n", e.getKey(), e.getValue()));

        // groupingBy with downstream: names per city
        Map<String, String> namesByCity = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::city,
                Collectors.mapping(
                    Employee::name,
                    Collectors.joining(", ")
                )
            ));
        System.out.println("\nEmployees by city:");
        namesByCity.forEach((city, names) ->
            System.out.printf("  %-10s : %s%n", city, names));

        // partitioningBy
        double avgSalary = EMPLOYEES.stream()
            .mapToDouble(Employee::salary)
            .average().orElse(0);

        Map<Boolean, List<Employee>> aboveBelow = EMPLOYEES.stream()
            .collect(Collectors.partitioningBy(e -> e.salary() > avgSalary));

        System.out.printf("%nAverage salary: $%.0f%n", avgSalary);
        System.out.println("Above average: " +
            aboveBelow.get(true).stream().map(Employee::name).collect(Collectors.joining(", ")));
        System.out.println("At/below average: " +
            aboveBelow.get(false).stream().map(Employee::name).collect(Collectors.joining(", ")));

        // joining
        String allNames = EMPLOYEES.stream()
            .map(Employee::name)
            .sorted()
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("\nAll employees: " + allNames);

        // toMap
        Map<String, Double> salaryById = EMPLOYEES.stream()
            .collect(Collectors.toMap(Employee::id, Employee::salary));
        System.out.println("\nE05 salary: $" + salaryById.get("E05"));
    }

    // =========================================================
    // 4. Reduce and Numeric Streams
    // =========================================================

    static void reduceAndNumericStreams() {
        System.out.println("\n=== Reduce and Numeric Streams ===\n");

        // Reduce: sum
        double totalPayroll = EMPLOYEES.stream()
            .mapToDouble(Employee::salary)
            .sum();
        System.out.printf("Total payroll: $%.0f%n", totalPayroll);

        // Numeric statistics
        DoubleSummaryStatistics stats = EMPLOYEES.stream()
            .mapToDouble(Employee::salary)
            .summaryStatistics();

        System.out.printf("Salary stats: min=$%.0f, max=$%.0f, avg=$%.0f, count=%d%n",
            stats.getMin(), stats.getMax(), stats.getAverage(), stats.getCount());

        // reduce to find most experienced
        Optional<Employee> mostExp = EMPLOYEES.stream()
            .reduce((a, b) -> a.yearsExperience() >= b.yearsExperience() ? a : b);
        mostExp.ifPresent(e ->
            System.out.printf("Most experienced: %s (%d years)%n",
                e.name(), e.yearsExperience()));

        // Concatenate names with reduce
        Optional<String> names = EMPLOYEES.stream()
            .limit(3)
            .map(Employee::name)
            .reduce((a, b) -> a + " | " + b);
        System.out.println("First 3 names: " + names.orElse("none"));

        // IntStream range
        int sumTo10 = IntStream.rangeClosed(1, 10).sum();
        System.out.println("Sum 1-10: " + sumTo10);

        // Generate a sequence
        List<Integer> squares = IntStream.rangeClosed(1, 5)
            .map(n -> n * n)
            .boxed()
            .collect(Collectors.toList());
        System.out.println("Squares 1-5: " + squares);
    }

    // =========================================================
    // 5. Method References
    // =========================================================

    static void methodReferencesDemo() {
        System.out.println("\n=== Method References ===\n");

        List<String> names = List.of("Charlie", "Alice", "Dave", "Bob", "Eve");

        // Static method reference: Math::abs
        List<Integer> nums = List.of(-3, 1, -5, 2, -1, 4);
        List<Integer> absolute = nums.stream()
            .map(Math::abs)
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Absolute values: " + absolute);

        // Instance method on type: String::toUpperCase
        List<String> upper = names.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());
        System.out.println("Uppercase names: " + upper);

        // Instance method on type: String::length
        names.stream()
            .sorted(Comparator.comparingInt(String::length).thenComparing(Function.identity()))
            .forEach(n -> System.out.println("  " + n + " (len=" + n.length() + ")"));

        // Instance method on specific object: System.out::println
        System.out.println("\nPrinting with method ref:");
        names.stream().limit(3).forEach(System.out::println);

        // Constructor reference: ArrayList::new
        Map<String, List<Employee>> groups = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.toCollection(ArrayList::new)
            ));
        System.out.println("\nGroup types: " +
            groups.values().stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .distinct()
                .findFirst()
                .orElse("?"));
    }

    // =========================================================
    // 6. Real-World Pipeline: Salary Report
    // =========================================================

    static void realWorldPipelineDemo() {
        System.out.println("\n=== Real-World Pipeline: Monthly Salary Report ===\n");

        // Build a comprehensive department report
        System.out.printf("%-12s %8s %8s %8s %8s%n",
            "Department", "Staff", "Avg Sal", "Min Sal", "Max Sal");
        System.out.println("-".repeat(52));

        EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                TreeMap::new,  // Sorted by department name
                Collectors.toList()
            ))
            .forEach((dept, emps) -> {
                DoubleSummaryStatistics s = emps.stream()
                    .mapToDouble(Employee::salary)
                    .summaryStatistics();
                System.out.printf("%-12s %8d $%7.0f $%7.0f $%7.0f%n",
                    dept, emps.size(), s.getAverage(), s.getMin(), s.getMax());
            });

        // Find employees eligible for promotion (5+ years, salary below dept average)
        System.out.println("\nPromotion candidates (5+ years, below dept avg salary):");

        Map<String, Double> deptAvgSalary = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.averagingDouble(Employee::salary)
            ));

        EMPLOYEES.stream()
            .filter(e -> e.yearsExperience() >= 5)
            .filter(e -> e.salary() < deptAvgSalary.getOrDefault(e.department(), Double.MAX_VALUE))
            .sorted(Comparator.comparing(Employee::department)
                .thenComparingDouble(Employee::salary))
            .forEach(e -> System.out.printf("  %-15s %-12s $%.0f (dept avg: $%.0f)%n",
                e.name(), e.department(), e.salary(),
                deptAvgSalary.get(e.department())));
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        basicOperationsDemo();
        flatMapDemo();
        collectorsDemo();
        reduceAndNumericStreams();
        methodReferencesDemo();
        realWorldPipelineDemo();
    }
}
