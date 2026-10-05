import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StreamsTest.java
 *
 * Comprehensive JUnit 5 tests for Java 8+ stream pipeline operations.
 * Covers: filter, map, flatMap, reduce, collectors, groupingBy, Optional integration.
 */
@DisplayName("Module 05: Stream Pipeline Tests")
class StreamsTest {

    // =========================================================
    // Shared test data
    // =========================================================

    record Employee(
        String name,
        String department,
        String city,
        double salary,
        int yearsExp,
        boolean isManager,
        List<String> skills
    ) {}

    static final List<Employee> EMPLOYEES = List.of(
        new Employee("Alice",   "Engineering", "Berlin",  95000, 7,  true,  List.of("Java","AWS","Kubernetes")),
        new Employee("Bob",     "Marketing",   "Munich",  72000, 3,  false, List.of("Excel","Salesforce")),
        new Employee("Charlie", "Engineering", "Berlin",  88000, 5,  false, List.of("Java","Python","Docker")),
        new Employee("Diana",   "HR",          "Hamburg", 65000, 4,  false, List.of("HR Systems","Excel")),
        new Employee("Eve",     "Engineering", "Berlin", 105000, 10, true,  List.of("Java","Kubernetes","AWS")),
        new Employee("Frank",   "Marketing",   "Munich",  68000, 2,  false, List.of("Google Ads","Excel")),
        new Employee("Grace",   "Engineering", "Hamburg", 92000, 6,  true,  List.of("Python","TensorFlow","AWS")),
        new Employee("Henry",   "HR",          "Berlin",  63000, 1,  false, List.of("HR Systems")),
        new Employee("Ivan",    "Engineering", "Munich",  78000, 4,  false, List.of("Java","Spring","MySQL")),
        new Employee("Julia",   "Engineering", "Berlin",  99000, 8,  true,  List.of("Java","AWS","Terraform")),
        new Employee("Karl",    "Marketing",   "Hamburg", 75000, 5,  true,  List.of("Analytics","Excel","Python")),
        new Employee("Lisa",    "Engineering", "Munich",  82000, 3,  false, List.of("Python","ML","TensorFlow"))
    );

    // =========================================================
    // Basic stream operations
    // =========================================================

    @Nested
    @DisplayName("Basic Stream Operations")
    class BasicTests {

        @Test
        @DisplayName("filter: only engineering employees")
        void filter_engineeringOnly() {
            long count = EMPLOYEES.stream()
                .filter(e -> e.department().equals("Engineering"))
                .count();

            assertEquals(6, count);
        }

        @Test
        @DisplayName("map: extract unique department names")
        void map_distinctDepartments() {
            Set<String> depts = EMPLOYEES.stream()
                .map(Employee::department)
                .collect(Collectors.toSet());

            assertTrue(depts.contains("Engineering"));
            assertTrue(depts.contains("Marketing"));
            assertTrue(depts.contains("HR"));
            assertEquals(3, depts.size());
        }

        @Test
        @DisplayName("sorted: employees by salary descending")
        void sorted_bySalaryDesc() {
            List<String> top3 = EMPLOYEES.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(3)
                .map(Employee::name)
                .collect(Collectors.toList());

            assertEquals("Eve", top3.get(0));   // 105000
            assertEquals("Julia", top3.get(1)); // 99000
            assertEquals("Alice", top3.get(2)); // 95000
        }

        @Test
        @DisplayName("anyMatch: at least one manager exists")
        void anyMatch_hasManagers() {
            assertTrue(EMPLOYEES.stream().anyMatch(Employee::isManager));
        }

        @Test
        @DisplayName("allMatch: all salaries positive")
        void allMatch_allSalariesPositive() {
            assertTrue(EMPLOYEES.stream().allMatch(e -> e.salary() > 0));
        }

        @Test
        @DisplayName("noneMatch: no negative experience")
        void noneMatch_noNegativeExp() {
            assertTrue(EMPLOYEES.stream().noneMatch(e -> e.yearsExp() < 0));
        }

        @Test
        @DisplayName("findFirst: returns deterministic result on sorted stream")
        void findFirst_onSortedStream() {
            Optional<Employee> lowestPaid = EMPLOYEES.stream()
                .sorted(Comparator.comparingDouble(Employee::salary))
                .findFirst();

            assertTrue(lowestPaid.isPresent());
            assertEquals("Henry", lowestPaid.get().name()); // 63000
        }
    }

    // =========================================================
    // flatMap tests
    // =========================================================

    @Nested
    @DisplayName("flatMap Operations")
    class FlatMapTests {

        @Test
        @DisplayName("flatMap: collect all unique skills across employees")
        void flatMap_allUniqueSkills() {
            Set<String> allSkills = EMPLOYEES.stream()
                .flatMap(e -> e.skills().stream())
                .collect(Collectors.toSet());

            assertTrue(allSkills.contains("Java"));
            assertTrue(allSkills.contains("AWS"));
            assertTrue(allSkills.contains("TensorFlow"));
            // Should have many unique skills
            assertTrue(allSkills.size() > 5);
        }

        @Test
        @DisplayName("flatMap: count total skill mentions (not unique)")
        void flatMap_totalSkillMentions() {
            long total = EMPLOYEES.stream()
                .flatMap(e -> e.skills().stream())
                .count();

            // Sum of all skill list sizes
            int expected = EMPLOYEES.stream()
                .mapToInt(e -> e.skills().size())
                .sum();

            assertEquals(expected, total);
        }

        @Test
        @DisplayName("flatMap: flatten nested integer lists")
        void flatMap_nestedLists() {
            List<List<Integer>> nested = List.of(
                List.of(1, 2, 3),
                List.of(4, 5),
                List.of(6)
            );

            List<Integer> flat = nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            assertEquals(List.of(1, 2, 3, 4, 5, 6), flat);
        }

        @Test
        @DisplayName("flatMap: skill popularity — Java should be most used")
        void flatMap_skillFrequency() {
            Map<String, Long> skillCounts = EMPLOYEES.stream()
                .flatMap(e -> e.skills().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            // Java appears in Alice, Charlie, Eve, Ivan, Julia = 5 times
            assertEquals(5L, skillCounts.getOrDefault("Java", 0L));
            // AWS appears in Alice, Eve, Grace, Julia = 4 times
            assertEquals(4L, skillCounts.getOrDefault("AWS", 0L));
        }
    }

    // =========================================================
    // Collectors tests
    // =========================================================

    @Nested
    @DisplayName("Collectors")
    class CollectorTests {

        @Test
        @DisplayName("groupingBy: employees grouped by department")
        void groupingBy_department() {
            Map<String, List<Employee>> byDept = EMPLOYEES.stream()
                .collect(Collectors.groupingBy(Employee::department));

            assertEquals(6, byDept.get("Engineering").size());
            assertEquals(3, byDept.get("Marketing").size());
            assertEquals(2, byDept.get("HR").size());
        }

        @Test
        @DisplayName("groupingBy + counting: headcount by department")
        void groupingBy_counting() {
            Map<String, Long> counts = EMPLOYEES.stream()
                .collect(Collectors.groupingBy(
                    Employee::department,
                    Collectors.counting()
                ));

            assertEquals(6L, counts.get("Engineering"));
            assertEquals(3L, counts.get("Marketing"));
            assertEquals(2L, counts.get("HR"));
        }

        @Test
        @DisplayName("groupingBy + averagingDouble: avg salary by department")
        void groupingBy_averageSalary() {
            Map<String, Double> avgSalary = EMPLOYEES.stream()
                .collect(Collectors.groupingBy(
                    Employee::department,
                    Collectors.averagingDouble(Employee::salary)
                ));

            double engAvg = (95000 + 88000 + 105000 + 92000 + 78000 + 99000) / 6.0;
            assertEquals(engAvg, avgSalary.get("Engineering"), 0.01);
        }

        @Test
        @DisplayName("partitioningBy: split managers from non-managers")
        void partitioningBy_managers() {
            Map<Boolean, List<Employee>> partitioned = EMPLOYEES.stream()
                .collect(Collectors.partitioningBy(Employee::isManager));

            long managerCount = EMPLOYEES.stream().filter(Employee::isManager).count();
            assertEquals((int)managerCount, partitioned.get(true).size());
            assertEquals(EMPLOYEES.size() - (int)managerCount, partitioned.get(false).size());
        }

        @Test
        @DisplayName("joining: CSV of sorted employee names")
        void joining_csvNames() {
            String csv = EMPLOYEES.stream()
                .map(Employee::name)
                .sorted()
                .collect(Collectors.joining(","));

            assertTrue(csv.startsWith("Alice"));
            assertTrue(csv.contains(","));
            // Should have 11 commas for 12 employees
            assertEquals(11, csv.chars().filter(c -> c == ',').count());
        }

        @Test
        @DisplayName("toMap: name to salary map")
        void toMap_nameToSalary() {
            Map<String, Double> nameSalary = EMPLOYEES.stream()
                .collect(Collectors.toMap(
                    Employee::name,
                    Employee::salary
                ));

            assertEquals(95000.0, nameSalary.get("Alice"), 0.01);
            assertEquals(105000.0, nameSalary.get("Eve"), 0.01);
        }

        @Test
        @DisplayName("summarizingDouble: full stats on engineering salaries")
        void summarizingDouble_engSalaries() {
            DoubleSummaryStatistics stats = EMPLOYEES.stream()
                .filter(e -> e.department().equals("Engineering"))
                .collect(Collectors.summarizingDouble(Employee::salary));

            assertEquals(6, stats.getCount());
            assertEquals(105000.0, stats.getMax(), 0.01);
            assertEquals(78000.0, stats.getMin(), 0.01);
        }
    }

    // =========================================================
    // Reduce and numeric streams
    // =========================================================

    @Nested
    @DisplayName("Reduce and Numeric Streams")
    class ReduceTests {

        @Test
        @DisplayName("sum: total salary via reduce")
        void reduce_totalSalary() {
            double total = EMPLOYEES.stream()
                .mapToDouble(Employee::salary)
                .reduce(0, Double::sum);

            double expected = EMPLOYEES.stream()
                .mapToDouble(Employee::salary)
                .sum();

            assertEquals(expected, total, 0.01);
        }

        @Test
        @DisplayName("IntStream.range generates correct sequence")
        void intStream_range() {
            List<Integer> seq = IntStream.range(1, 6)
                .boxed()
                .collect(Collectors.toList());

            assertEquals(List.of(1, 2, 3, 4, 5), seq);
        }

        @Test
        @DisplayName("IntStream.rangeClosed is inclusive")
        void intStream_rangeClosed_isInclusive() {
            int sum = IntStream.rangeClosed(1, 10).sum();
            assertEquals(55, sum); // 1+2+...+10 = 55
        }

        @Test
        @DisplayName("reduce with identity 1 multiplies correctly")
        void reduce_product() {
            int product = IntStream.rangeClosed(1, 5)
                .reduce(1, (a, b) -> a * b);

            assertEquals(120, product); // 5! = 120
        }

        @Test
        @DisplayName("max on stream uses Comparator correctly")
        void max_withComparator() {
            Optional<Employee> richest = EMPLOYEES.stream()
                .max(Comparator.comparingDouble(Employee::salary));

            assertTrue(richest.isPresent());
            assertEquals("Eve", richest.get().name());
            assertEquals(105000.0, richest.get().salary(), 0.01);
        }
    }

    // =========================================================
    // Optional integration
    // =========================================================

    @Nested
    @DisplayName("Optional Integration with Streams")
    class OptionalStreamTests {

        @Test
        @DisplayName("Optional.stream() Java 9: filter out empties from List<Optional>")
        void optionalStream_filtersEmpties() {
            List<Optional<String>> optionals = List.of(
                Optional.of("Alice"),
                Optional.empty(),
                Optional.of("Bob"),
                Optional.empty(),
                Optional.of("Charlie")
            );

            List<String> names = optionals.stream()
                .flatMap(Optional::stream) // Java 9+
                .collect(Collectors.toList());

            assertEquals(List.of("Alice", "Bob", "Charlie"), names);
        }

        @Test
        @DisplayName("orElse provides fallback for empty Optional")
        void optional_orElse_fallback() {
            Optional<Employee> found = EMPLOYEES.stream()
                .filter(e -> e.name().equals("NonExistent"))
                .findFirst();

            String name = found.map(Employee::name).orElse("Unknown");
            assertEquals("Unknown", name);
        }

        @Test
        @DisplayName("map transforms Optional value")
        void optional_map_transforms() {
            Optional<Double> salary = EMPLOYEES.stream()
                .filter(e -> e.name().equals("Alice"))
                .findFirst()
                .map(Employee::salary);

            assertTrue(salary.isPresent());
            assertEquals(95000.0, salary.get(), 0.01);
        }

        @Test
        @DisplayName("filter on Optional narrows value")
        void optional_filter_narrows() {
            Optional<Employee> highEarner = EMPLOYEES.stream()
                .filter(e -> e.name().equals("Alice"))
                .findFirst()
                .filter(e -> e.salary() > 100000); // Alice earns 95k, so this should be empty

            assertTrue(highEarner.isEmpty());
        }

        @Test
        @DisplayName("orElseThrow throws if empty")
        void optional_orElseThrow() {
            assertThrows(
                NoSuchElementException.class,
                () -> EMPLOYEES.stream()
                    .filter(e -> e.name().equals("Nobody"))
                    .findFirst()
                    .orElseThrow()
            );
        }
    }

    // =========================================================
    // Method references
    // =========================================================

    @Nested
    @DisplayName("Method References")
    class MethodReferenceTests {

        @Test
        @DisplayName("Static method reference: Integer::parseInt")
        void staticMethodRef_parseInt() {
            List<Integer> numbers = Stream.of("1", "2", "3", "4", "5")
                .map(Integer::parseInt)
                .collect(Collectors.toList());

            assertEquals(List.of(1, 2, 3, 4, 5), numbers);
        }

        @Test
        @DisplayName("Instance method reference on type: String::toUpperCase")
        void instanceMethodRefOnType_toUpperCase() {
            List<String> upper = Stream.of("alice", "bob", "charlie")
                .map(String::toUpperCase)
                .collect(Collectors.toList());

            assertEquals(List.of("ALICE", "BOB", "CHARLIE"), upper);
        }

        @Test
        @DisplayName("Constructor reference: ArrayList::new")
        void constructorRef_arrayList() {
            List<List<String>> nested = Stream.of("a,b", "c,d,e", "f")
                .map(s -> Arrays.asList(s.split(",")))
                .collect(Collectors.toCollection(ArrayList::new));

            assertEquals(3, nested.size());
        }

        @Test
        @DisplayName("Instance method reference on object: list::add used with forEach")
        void instanceMethodRefOnObject() {
            List<String> collected = new ArrayList<>();

            Stream.of("x", "y", "z").forEach(collected::add);

            assertEquals(List.of("x", "y", "z"), collected);
        }
    }

    // =========================================================
    // Edge cases and empty streams
    // =========================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty stream: count returns 0")
        void emptyStream_count() {
            long count = Stream.empty().count();
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Empty stream: findFirst returns empty Optional")
        void emptyStream_findFirst_empty() {
            Optional<String> result = Stream.<String>empty().findFirst();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Single element stream: reduce returns that element")
        void singleElement_reduce() {
            Optional<Integer> result = Stream.of(42).reduce(Integer::sum);
            assertTrue(result.isPresent());
            assertEquals(42, result.get());
        }

        @Test
        @DisplayName("Stream.of works with varargs")
        void streamOf_varargs() {
            List<String> result = Stream.of("a", "b", "c").collect(Collectors.toList());
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Collections.unmodifiableList prevents mutation after collect")
        void collected_list_isMutable() {
            // Collectors.toList() returns a mutable list
            List<String> mutable = Stream.of("a", "b").collect(Collectors.toList());
            assertDoesNotThrow(() -> mutable.add("c"));
            assertEquals(3, mutable.size());
        }

        @Test
        @DisplayName("Collectors.toUnmodifiableList prevents mutation")
        void toUnmodifiableList_isImmutable() {
            List<String> immutable = Stream.of("a", "b")
                .collect(Collectors.toUnmodifiableList());

            assertThrows(UnsupportedOperationException.class, () -> immutable.add("c"));
        }
    }

    // =========================================================
    // Parallel streams
    // =========================================================

    @Nested
    @DisplayName("Parallel Streams")
    class ParallelStreamTests {

        @Test
        @DisplayName("parallel sum equals sequential sum")
        void parallel_sum_equalsSequential() {
            double sequential = EMPLOYEES.stream()
                .mapToDouble(Employee::salary)
                .sum();

            double parallel = EMPLOYEES.parallelStream()
                .mapToDouble(Employee::salary)
                .sum();

            assertEquals(sequential, parallel, 0.001);
        }

        @Test
        @DisplayName("parallel count equals sequential count")
        void parallel_count_equalsSequential() {
            long sequential = EMPLOYEES.stream()
                .filter(e -> e.department().equals("Engineering"))
                .count();

            long parallel = EMPLOYEES.parallelStream()
                .filter(e -> e.department().equals("Engineering"))
                .count();

            assertEquals(sequential, parallel);
        }
    }
}
