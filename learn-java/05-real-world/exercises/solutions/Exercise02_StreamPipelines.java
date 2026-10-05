import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Solution to Exercise02_StreamPipelines
 */
public class Exercise02_StreamPipelines {

    record Employee(
        String id, String name, String department, String city,
        double salary, int yearsExperience, boolean isManager, List<String> skills
    ) {}

    private static final List<Employee> EMPLOYEES = List.of(
        new Employee("E01","Alice",   "Engineering","Berlin",  95000,7, true, List.of("Java","AWS","Kubernetes")),
        new Employee("E02","Bob",     "Marketing",  "Munich",  72000,3, false,List.of("Excel","Salesforce")),
        new Employee("E03","Charlie", "Engineering","Berlin",  88000,5, false,List.of("Java","Python","Docker")),
        new Employee("E04","Diana",   "HR",         "Hamburg", 65000,4, false,List.of("HR Systems","Excel")),
        new Employee("E05","Eve",     "Engineering","Berlin", 105000,10,true, List.of("Java","Kubernetes","AWS")),
        new Employee("E06","Frank",   "Marketing",  "Munich",  68000,2, false,List.of("Google Ads","Excel")),
        new Employee("E07","Grace",   "Engineering","Hamburg", 92000,6, true, List.of("Python","TensorFlow","AWS")),
        new Employee("E08","Henry",   "HR",         "Berlin",  63000,1, false,List.of("HR Systems")),
        new Employee("E09","Ivan",    "Engineering","Munich",  78000,4, false,List.of("Java","Spring","MySQL")),
        new Employee("E10","Julia",   "Engineering","Berlin",  99000,8, true, List.of("Java","AWS","Terraform")),
        new Employee("E11","Karl",    "Marketing",  "Hamburg", 75000,5, true, List.of("Analytics","Excel","Python")),
        new Employee("E12","Lisa",    "Engineering","Munich",  82000,3, false,List.of("Python","ML","TensorFlow"))
    );

    public static Map<String, String> salarySummaryByDepartment() {
        return EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    emps -> {
                        DoubleSummaryStatistics stats = emps.stream()
                            .mapToDouble(Employee::salary).summaryStatistics();
                        return String.format("%d employees, avg=$%.0f, max=$%.0f, total=$%.0f",
                            emps.size(), stats.getAverage(), stats.getMax(), stats.getSum());
                    }
                )
            ));
    }

    public static List<String> managerOnlySkills() {
        Set<String> managerSkills = EMPLOYEES.stream()
            .filter(Employee::isManager)
            .flatMap(e -> e.skills().stream())
            .collect(Collectors.toSet());

        Set<String> nonManagerSkills = EMPLOYEES.stream()
            .filter(e -> !e.isManager())
            .flatMap(e -> e.skills().stream())
            .collect(Collectors.toSet());

        return managerSkills.stream()
            .filter(skill -> !nonManagerSkills.contains(skill))
            .sorted()
            .collect(Collectors.toList());
    }

    public static String engineeringCities() {
        return EMPLOYEES.stream()
            .filter(e -> e.department().equals("Engineering"))
            .map(Employee::city)
            .distinct()
            .sorted()
            .collect(Collectors.joining(", "));
    }

    public static Map<String, Double> promotionCandidates() {
        return EMPLOYEES.stream()
            .filter(e -> e.yearsExperience() >= 5)
            .filter(e -> e.salary() < 95000)
            .filter(e -> !e.isManager())
            .sorted(Comparator.comparingDouble(Employee::salary))
            .collect(Collectors.toMap(
                Employee::name,
                Employee::salary,
                (a, b) -> a,
                LinkedHashMap::new
            ));
    }

    public static Map<String, List<String>> departmentSkillMatrix() {
        return EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.collectingAndThen(
                    Collectors.flatMapping(
                        e -> e.skills().stream(),
                        Collectors.toSet()
                    ),
                    skills -> skills.stream().sorted().collect(Collectors.toList())
                )
            ));
    }

    public static void main(String[] args) {
        System.out.println("=== Query 1: Salary Summary by Department ===");
        salarySummaryByDepartment().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %-12s %s%n", e.getKey(), e.getValue()));

        System.out.println("\n=== Query 2: Manager-Only Skills ===");
        System.out.println(managerOnlySkills());

        System.out.println("\n=== Query 3: Engineering Cities ===");
        System.out.println(engineeringCities());

        System.out.println("\n=== Query 4: Promotion Candidates ===");
        promotionCandidates().forEach((name, salary) ->
            System.out.printf("  %-10s $%.0f%n", name, salary));

        System.out.println("\n=== Query 5: Department Skill Matrix ===");
        departmentSkillMatrix().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %-12s %s%n", e.getKey(), e.getValue()));
    }
}
