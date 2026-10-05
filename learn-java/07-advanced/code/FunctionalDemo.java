/**
 * FunctionalDemo.java
 *
 * Demonstrates functional programming in Java:
 *   1. Lambda expressions — syntax and target typing
 *   2. Built-in functional interfaces (Function, Predicate, Consumer, Supplier)
 *   3. Method references — all four types
 *   4. Stream pipelines — intermediate and terminal operations
 *   5. Function composition — andThen(), compose()
 *   6. Optional — null-safe chaining
 *   7. Custom functional interfaces
 */

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class FunctionalDemo {

    // ==========================================================================
    // Supporting data model
    // ==========================================================================

    record Employee(String name, String department, double salary, boolean active) {}

    // ==========================================================================
    // 1. Lambda expressions
    // ==========================================================================

    static void demonstrateLambdas() {
        System.out.println("--- Lambda Expressions ---");

        // Before lambdas: anonymous class
        Comparator<String> oldWay = new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        };

        // With lambda
        Comparator<String> byLength = (a, b) -> a.length() - b.length();

        // Single parameter (no parens needed)
        Function<String, Integer> getLength = s -> s.length();

        // No parameters
        Runnable printHello = () -> System.out.println("Hello from lambda!");

        // Multi-line lambda with block body
        Function<Integer, String> fizzbuzz = n -> {
            if (n % 15 == 0) return "FizzBuzz";
            if (n % 3 == 0) return "Fizz";
            if (n % 5 == 0) return "Buzz";
            return String.valueOf(n);
        };

        List<String> words = Arrays.asList("banana", "apple", "kiwi", "cherry");
        words.sort(byLength);
        System.out.println("Sorted by length: " + words);

        printHello.run();

        System.out.println("FizzBuzz 15: " + fizzbuzz.apply(15));
        System.out.println("FizzBuzz 9: " + fizzbuzz.apply(9));
        System.out.println();
    }

    // ==========================================================================
    // 2. Functional interfaces
    // ==========================================================================

    static void demonstrateFunctionalInterfaces() {
        System.out.println("--- Functional Interfaces ---");

        // Function<T, R> — transform T to R
        Function<String, Integer> length = String::length;
        Function<Integer, String> intToStr = n -> "Number: " + n;
        System.out.println("Length of 'Hello': " + length.apply("Hello"));
        System.out.println("Int to string: " + intToStr.apply(42));

        // Predicate<T> — test a condition
        Predicate<String> isLong = s -> s.length() > 5;
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> isLongAndStartsWithA = isLong.and(startsWithA);
        Predicate<String> isLongOrStartsWithA = isLong.or(startsWithA);
        Predicate<String> isShort = isLong.negate();

        System.out.println("'Alice' isLong? " + isLong.test("Alice"));
        System.out.println("'Alexander' isLongAndStartsWithA? " + isLongAndStartsWithA.test("Alexander"));
        System.out.println("'Bob' isShort? " + isShort.test("Bob"));

        // Consumer<T> — consume, no return
        Consumer<String> print = s -> System.out.println("  >> " + s);
        Consumer<String> printUppercase = s -> System.out.println("  >> " + s.toUpperCase());
        Consumer<String> both = print.andThen(printUppercase);
        System.out.println("Both consumers:");
        both.accept("hello");

        // Supplier<T> — produce, no input
        Supplier<List<String>> listFactory = ArrayList::new;
        Supplier<UUID> idGen = UUID::randomUUID;
        List<String> newList = listFactory.get();
        newList.add("item");
        System.out.println("New list: " + newList);
        System.out.println("New UUID: " + idGen.get());

        // BiFunction<T, U, R>
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println("Repeat 'Ha' 3 times: " + repeat.apply("Ha", 3));

        // UnaryOperator<T>
        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> upper = String::toUpperCase;
        System.out.println("Trim+upper: '" + trim.andThen(upper).apply("  hello  ") + "'");

        System.out.println();
    }

    // ==========================================================================
    // 3. Method references
    // ==========================================================================

    static void demonstrateMethodReferences() {
        System.out.println("--- Method References ---");

        // Type 1: Static method — ClassName::staticMethod
        Function<String, Integer> parse = Integer::parseInt;
        Function<Double, Double> abs = Math::abs;
        System.out.println("parseInt('42'): " + parse.apply("42"));
        System.out.println("abs(-3.14): " + abs.apply(-3.14));

        // Type 2: Instance method of a specific object — instance::method
        String prefix = "Hello, ";
        Function<String, String> greet = prefix::concat;
        System.out.println("Greet: " + greet.apply("World"));

        // Type 3: Instance method of an arbitrary object — ClassName::instanceMethod
        // First arg becomes the receiver
        Function<String, String> toLower = String::toLowerCase;
        Function<String, Integer> strLength = String::length;
        System.out.println("toLower('HELLO'): " + toLower.apply("HELLO"));

        // Type 4: Constructor reference — ClassName::new
        Function<String, StringBuilder> sbFactory = StringBuilder::new;
        Supplier<ArrayList<String>> listFactory = ArrayList::new;
        StringBuilder sb = sbFactory.apply("initial");
        System.out.println("StringBuilder: " + sb);

        // Real-world: method references in streams
        List<String> names = Arrays.asList("ALICE", "BOB", "CHARLIE");
        List<String> lowered = names.stream()
            .map(String::toLowerCase)       // type 3: instance method of each element
            .collect(Collectors.toList());
        System.out.println("Lowered: " + lowered);

        List<Integer> lengths = names.stream()
            .map(String::length)            // type 3
            .collect(Collectors.toList());
        System.out.println("Lengths: " + lengths);

        System.out.println();
    }

    // ==========================================================================
    // 4. Stream pipelines
    // ==========================================================================

    static void demonstrateStreams() {
        System.out.println("--- Stream Pipelines ---");

        List<Employee> employees = Arrays.asList(
            new Employee("Alice", "Engineering", 95000, true),
            new Employee("Bob", "Marketing", 72000, true),
            new Employee("Charlie", "Engineering", 105000, true),
            new Employee("Dave", "HR", 60000, false),
            new Employee("Eve", "Engineering", 88000, true),
            new Employee("Frank", "Marketing", 45000, false),
            new Employee("Grace", "HR", 68000, true)
        );

        // Basic pipeline: filter → map → collect
        List<String> highPaidEngineers = employees.stream()
            .filter(Employee::active)                       // only active
            .filter(e -> e.department().equals("Engineering")) // only engineering
            .filter(e -> e.salary() > 90000)                // high salary
            .map(Employee::name)                            // get names
            .sorted()                                       // alphabetical
            .collect(Collectors.toList());
        System.out.println("High-paid engineers: " + highPaidEngineers);

        // Aggregation
        double avgSalary = employees.stream()
            .filter(Employee::active)
            .mapToDouble(Employee::salary)
            .average()
            .orElse(0.0);
        System.out.printf("Average active salary: $%.2f%n", avgSalary);

        // Counting
        long engineeringCount = employees.stream()
            .filter(e -> e.department().equals("Engineering"))
            .count();
        System.out.println("Engineering count: " + engineeringCount);

        // groupingBy
        Map<String, List<Employee>> byDept = employees.stream()
            .collect(Collectors.groupingBy(Employee::department));
        System.out.println("Departments: " + byDept.keySet());

        Map<String, Double> avgSalaryByDept = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::department,
                Collectors.averagingDouble(Employee::salary)
            ));
        System.out.println("Avg salary by dept:");
        avgSalaryByDept.forEach((dept, avg) ->
            System.out.printf("  %s: $%.0f%n", dept, avg));

        // partitioningBy
        Map<Boolean, List<Employee>> activePartition = employees.stream()
            .collect(Collectors.partitioningBy(Employee::active));
        System.out.println("Active: " + activePartition.get(true).stream().map(Employee::name).toList());
        System.out.println("Inactive: " + activePartition.get(false).stream().map(Employee::name).toList());

        // joining
        String nameList = employees.stream()
            .filter(Employee::active)
            .map(Employee::name)
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Active employees: " + nameList);

        // flatMap
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2, 3),
            Arrays.asList(4, 5),
            Arrays.asList(6, 7, 8, 9)
        );
        List<Integer> flat = nested.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        System.out.println("Flattened: " + flat);

        // reduce
        int sum = IntStream.rangeClosed(1, 10).reduce(0, Integer::sum);
        System.out.println("Sum 1..10: " + sum);

        System.out.println();
    }

    // ==========================================================================
    // 5. Function composition
    // ==========================================================================

    static void demonstrateComposition() {
        System.out.println("--- Function Composition ---");

        Function<String, String> trim = String::trim;
        Function<String, String> lower = String::toLowerCase;
        Function<String, Boolean> validate = s -> s.length() >= 3;

        // andThen: f.andThen(g) applies f first, then g
        Function<String, String> normalise = trim.andThen(lower);
        System.out.println("Normalised '  HELLO  ': '" + normalise.apply("  HELLO  ") + "'");

        // Chain longer
        Function<String, Boolean> normAndValidate = trim.andThen(lower).andThen(validate);
        System.out.println("'  AB  ' valid? " + normAndValidate.apply("  AB  "));
        System.out.println("'  Alice  ' valid? " + normAndValidate.apply("  Alice  "));

        // compose: f.compose(g) applies g first, then f  (reverse of andThen)
        Function<Integer, Integer> times2 = x -> x * 2;
        Function<Integer, Integer> plus3 = x -> x + 3;

        Function<Integer, Integer> times2ThenPlus3 = plus3.compose(times2); // apply times2 first, then plus3
        Function<Integer, Integer> plus3ThenTimes2 = plus3.andThen(times2); // apply plus3 first, then times2

        System.out.println("times2ThenPlus3(5) = " + times2ThenPlus3.apply(5)); // (5*2)+3 = 13
        System.out.println("plus3ThenTimes2(5) = " + plus3ThenTimes2.apply(5)); // (5+3)*2 = 16

        // Predicate composition
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isPositiveEven = isPositive.and(isEven);
        Predicate<Integer> isNegativeOrOdd = isPositiveEven.negate();

        System.out.println("4 is positive even? " + isPositiveEven.test(4));
        System.out.println("3 is positive even? " + isPositiveEven.test(3));
        System.out.println("-2 is negativeOrOdd? " + isNegativeOrOdd.test(-2));

        System.out.println();
    }

    // ==========================================================================
    // 6. Optional
    // ==========================================================================

    record User(String name, Address address) {}
    record Address(String street, City city) {}
    record City(String name) {}

    static Optional<User> findUser(String name) {
        if ("alice".equals(name)) {
            return Optional.of(new User("Alice", new Address("123 Main St", new City("London"))));
        }
        if ("bob".equals(name)) {
            return Optional.of(new User("Bob", null));   // Bob has no address
        }
        return Optional.empty();
    }

    static void demonstrateOptional() {
        System.out.println("--- Optional ---");

        // Without Optional — null check chain
        User user = findUser("alice").orElse(null);
        String cityName1;
        if (user != null && user.address() != null && user.address().city() != null) {
            cityName1 = user.address().city().name();
        } else {
            cityName1 = "Unknown";
        }
        System.out.println("City (imperative): " + cityName1);

        // With Optional — clean chain
        String cityName2 = findUser("alice")
            .map(User::address)
            .map(Address::city)
            .map(City::name)
            .orElse("Unknown");
        System.out.println("City (functional): " + cityName2);

        // Bob has no address
        String bobCity = findUser("bob")
            .map(User::address)    // returns Optional.empty() because address is null
            .map(Address::city)
            .map(City::name)
            .orElse("Unknown");
        System.out.println("Bob's city: " + bobCity);

        // Unknown user
        String unknownCity = findUser("nobody")
            .map(User::address)
            .map(Address::city)
            .map(City::name)
            .orElse("Unknown");
        System.out.println("Nobody's city: " + unknownCity);

        // filter
        Optional<User> aliceInLondon = findUser("alice")
            .filter(u -> u.address() != null)
            .filter(u -> "London".equals(u.address().city().name()));
        System.out.println("Alice in London: " + aliceInLondon.isPresent());

        // orElseGet (lazy — only computed if empty)
        String result = findUser("nobody")
            .map(User::name)
            .orElseGet(() -> "Computed default: " + System.currentTimeMillis());
        System.out.println("orElseGet: " + result);

        // ifPresent
        findUser("alice").ifPresent(u -> System.out.println("Found user: " + u.name()));

        System.out.println();
    }

    // ==========================================================================
    // 7. Custom functional interface
    // ==========================================================================

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get() throws Exception;

        static <T> Supplier<T> wrap(ThrowingSupplier<T> ts) {
            return () -> {
                try {
                    return ts.get();
                } catch (Exception e) {
                    throw new RuntimeException("Wrapped exception", e);
                }
            };
        }
    }

    static void demonstrateCustomFunctionalInterfaces() {
        System.out.println("--- Custom Functional Interfaces ---");

        // TriFunction
        TriFunction<String, String, Integer, String> format =
            (prefix, name, count) -> prefix + name + " x" + count;
        System.out.println(format.apply("[TAG]", "Alice", 3));

        // ThrowingSupplier — wraps checked exceptions for use in streams
        List<String> numberStrings = Arrays.asList("1", "2", "bad", "4");
        List<String> validNumbers = numberStrings.stream()
            .filter(s -> {
                try {
                    Integer.parseInt(s);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            })
            .collect(Collectors.toList());
        System.out.println("Valid numbers: " + validNumbers);

        System.out.println();
    }

    // ==========================================================================
    // MAIN
    // ==========================================================================

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  FUNCTIONAL PROGRAMMING DEMO");
        System.out.println("========================================\n");

        demonstrateLambdas();
        demonstrateFunctionalInterfaces();
        demonstrateMethodReferences();
        demonstrateStreams();
        demonstrateComposition();
        demonstrateOptional();
        demonstrateCustomFunctionalInterfaces();
    }
}
