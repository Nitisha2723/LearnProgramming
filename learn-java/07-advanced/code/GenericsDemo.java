/**
 * GenericsDemo.java
 *
 * Demonstrates Java generics in action:
 *   1. Generic Stack<T>
 *   2. Pair<A,B>
 *   3. Result<T> — a functional error-handling type
 *   4. Bounded type parameters (<T extends Comparable<T>>)
 *   5. Wildcards (?, ? extends T, ? super T)
 *   6. PECS — Producer Extends, Consumer Super
 */

import java.util.*;
import java.util.function.Function;

public class GenericsDemo {

    // ==========================================================================
    // 1. Generic Stack
    // ==========================================================================

    /**
     * A type-safe stack backed by a List.
     * Without generics, this would use Object, requiring casts everywhere.
     *
     * @param <T> The type of elements stored in this stack
     */
    static class Stack<T> {
        private final List<T> elements = new ArrayList<>();

        /** Push an element onto the top of the stack. */
        public void push(T element) {
            elements.add(element);
        }

        /**
         * Pop the top element off the stack.
         *
         * @throws NoSuchElementException if the stack is empty
         */
        public T pop() {
            if (isEmpty()) {
                throw new NoSuchElementException("Stack is empty");
            }
            return elements.remove(elements.size() - 1);
        }

        /**
         * Peek at the top element without removing it.
         *
         * @throws NoSuchElementException if the stack is empty
         */
        public T peek() {
            if (isEmpty()) {
                throw new NoSuchElementException("Stack is empty");
            }
            return elements.get(elements.size() - 1);
        }

        public boolean isEmpty() { return elements.isEmpty(); }
        public int size() { return elements.size(); }

        @Override
        public String toString() { return elements.toString(); }
    }

    // ==========================================================================
    // 2. Generic Pair
    // ==========================================================================

    /**
     * Holds two values of potentially different types.
     * Immutable — fields are final.
     *
     * @param <A> The type of the first element
     * @param <B> The type of the second element
     */
    static class Pair<A, B> {
        private final A first;
        private final B second;

        private Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        /** Static factory — cleaner than constructor syntax */
        public static <A, B> Pair<A, B> of(A first, B second) {
            return new Pair<>(first, second);
        }

        public A getFirst() { return first; }
        public B getSecond() { return second; }

        /** Swap the pair — note how the return type reflects the swap! */
        public Pair<B, A> swap() {
            return Pair.of(second, first);
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    // ==========================================================================
    // 3. Result<T> — Functional Error Handling
    // ==========================================================================

    /**
     * A Result type that represents either success (with a value) or failure (with an error).
     *
     * This pattern avoids:
     *   - Returning null on failure
     *   - Throwing checked exceptions for expected failures
     *   - Separate return codes
     *
     * Inspired by Rust's Result<T, E> and Haskell's Either.
     *
     * @param <T> The type of the success value
     */
    static class Result<T> {
        private final T value;
        private final String error;
        private final boolean success;

        private Result(T value, String error, boolean success) {
            this.value = value;
            this.error = error;
            this.success = success;
        }

        public static <T> Result<T> success(T value) {
            Objects.requireNonNull(value, "Success value cannot be null");
            return new Result<>(value, null, true);
        }

        public static <T> Result<T> failure(String error) {
            Objects.requireNonNull(error, "Error message cannot be null");
            return new Result<>(null, error, false);
        }

        public boolean isSuccess() { return success; }
        public boolean isFailure() { return !success; }

        public T getValue() {
            if (!success) throw new IllegalStateException("Cannot get value from a failed Result: " + error);
            return value;
        }

        public String getError() {
            if (success) throw new IllegalStateException("Cannot get error from a successful Result");
            return error;
        }

        /**
         * Transform the success value. If this is a failure, passes through unchanged.
         * Like Optional.map() but for Result.
         */
        public <R> Result<R> map(Function<T, R> mapper) {
            if (success) {
                try {
                    return Result.success(mapper.apply(value));
                } catch (Exception e) {
                    return Result.failure("Mapping failed: " + e.getMessage());
                }
            }
            return Result.failure(error);
        }

        /** Get the value or a default if this is a failure. */
        public T orElse(T defaultValue) {
            return success ? value : defaultValue;
        }

        @Override
        public String toString() {
            return success ? "Success(" + value + ")" : "Failure(" + error + ")";
        }
    }

    // ==========================================================================
    // 4. Bounded Type Parameters
    // ==========================================================================

    /**
     * Finds the maximum element in a list.
     *
     * <T extends Comparable<T>> means:
     *   - T can be any type
     *   - BUT T must implement Comparable<T>
     *   - This gives us access to compareTo()
     *
     * Works with Integer, String, Double, any Comparable type.
     */
    public static <T extends Comparable<T>> T findMax(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot find max of empty list");
        }
        T max = items.get(0);
        for (T item : items) {
            if (item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    /**
     * Sums a list of numbers using the Number superclass.
     *
     * <T extends Number> means T must be a Number.
     * This gives us access to doubleValue().
     */
    public static <T extends Number> double sum(List<T> numbers) {
        double total = 0.0;
        for (T number : numbers) {
            total += number.doubleValue();
        }
        return total;
    }

    // ==========================================================================
    // 5. Wildcards — PECS
    // ==========================================================================

    /**
     * PRODUCER — reads FROM the list → use ? extends T
     *
     * This works with List<Integer>, List<Double>, List<Number>, List<Object>
     * — any list where elements extend Number.
     */
    public static double sumProducer(List<? extends Number> numbers) {
        double total = 0.0;
        for (Number n : numbers) {     // we READ from the list
            total += n.doubleValue();
        }
        return total;
        // NOTE: we CANNOT add to numbers here — we don't know its exact type
    }

    /**
     * CONSUMER — writes TO the list → use ? super T
     *
     * This works with List<Integer>, List<Number>, List<Object>
     * — any list that can hold integers.
     */
    public static void addIntegers(List<? super Integer> list, int count) {
        for (int i = 1; i <= count; i++) {
            list.add(i);               // we WRITE to the list
        }
        // NOTE: reading from list gives Object, not Integer — we don't know the exact type
    }

    /**
     * Classic PECS example — copy from src to dst.
     *
     * src is a PRODUCER (we read from it)   → ? extends T
     * dst is a CONSUMER (we write to it)    → ? super T
     */
    public static <T> void copy(List<? super T> dst, List<? extends T> src) {
        for (T element : src) {
            dst.add(element);
        }
    }

    // ==========================================================================
    // MAIN — demonstrations
    // ==========================================================================

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  GENERICS DEMO");
        System.out.println("========================================\n");

        // --- Stack ---
        System.out.println("--- Generic Stack ---");
        Stack<String> wordStack = new Stack<>();
        wordStack.push("Hello");
        wordStack.push("World");
        wordStack.push("!");
        System.out.println("Stack: " + wordStack);
        System.out.println("Pop: " + wordStack.pop());
        System.out.println("Peek: " + wordStack.peek());
        System.out.println("Stack after: " + wordStack);

        Stack<Integer> numStack = new Stack<>();
        numStack.push(1);
        numStack.push(2);
        numStack.push(3);
        System.out.println("\nNumber stack: " + numStack);
        System.out.println("Sum by popping: " + (numStack.pop() + numStack.pop() + numStack.pop()));
        System.out.println();

        // --- Pair ---
        System.out.println("--- Generic Pair ---");
        Pair<String, Integer> nameAge = Pair.of("Alice", 30);
        System.out.println("Name: " + nameAge.getFirst() + ", Age: " + nameAge.getSecond());
        Pair<Integer, String> swapped = nameAge.swap();
        System.out.println("Swapped: " + swapped);

        Pair<Double, Double> londonCoords = Pair.of(51.5074, -0.1278);
        System.out.println("London: " + londonCoords);
        System.out.println();

        // --- Result ---
        System.out.println("--- Result<T> ---");
        Result<Integer> good = parseInteger("42");
        Result<Integer> bad = parseInteger("not a number");

        System.out.println("parseInteger(\"42\"): " + good);
        System.out.println("parseInteger(\"bad\"): " + bad);

        // Chain operations
        Result<String> processed = good
            .map(n -> n * 2)
            .map(n -> "Result is: " + n);
        System.out.println("Mapped: " + processed);

        Result<String> failProcessed = bad
            .map(n -> n * 2)           // skipped because bad is a failure
            .map(n -> "Result is: " + n);
        System.out.println("Mapped failure: " + failProcessed);

        System.out.println("orElse on failure: " + bad.orElse(-1));
        System.out.println();

        // --- Bounded Types ---
        System.out.println("--- Bounded Types ---");
        List<Integer> ints = Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6);
        System.out.println("Max integer: " + findMax(ints));

        List<String> words = Arrays.asList("banana", "apple", "cherry", "date");
        System.out.println("Max string: " + findMax(words));

        System.out.println("Sum of ints: " + sum(ints));
        System.out.println("Sum of doubles: " + sum(Arrays.asList(1.1, 2.2, 3.3)));
        System.out.println();

        // --- Wildcards (PECS) ---
        System.out.println("--- Wildcards (PECS) ---");
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.5, 2.5, 3.5);

        // Both work because both extend Number (PRODUCER — ? extends Number)
        System.out.println("Sum integers: " + sumProducer(integers));
        System.out.println("Sum doubles: " + sumProducer(doubles));

        // CONSUMER — adding to different list types
        List<Number> numberList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();
        addIntegers(numberList, 5);  // List<Number> can hold Integer
        addIntegers(objectList, 5);  // List<Object> can hold Integer
        System.out.println("Number list: " + numberList);
        System.out.println("Object list: " + objectList);

        // Copy: src produces, dst consumes
        List<Integer> source = Arrays.asList(10, 20, 30);
        List<Number> destination = new ArrayList<>();
        copy(destination, source);
        System.out.println("After copy: " + destination);
    }

    /** Helper: safely parse an integer, returning Result */
    private static Result<Integer> parseInteger(String s) {
        try {
            return Result.success(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Result.failure("Invalid integer: '" + s + "'");
        }
    }
}
