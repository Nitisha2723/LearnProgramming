package patterns.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DESIGN PATTERN: Strategy
 * ========================
 * Intent: Define a family of algorithms, encapsulate each one, and make them
 * interchangeable. Strategy lets the algorithm vary independently from clients
 * that use it.
 *
 * REAL-WORLD USE CASE: Sorting Algorithm Selection
 * -------------------------------------------------
 * Different sorting algorithms have different performance characteristics:
 *   - Bubble Sort: simple, O(n²) — good for tiny or nearly-sorted lists
 *   - Quick Sort:  O(n log n) average, O(n²) worst — good for most cases
 *   - Merge Sort:  O(n log n) guaranteed, O(n) space — good for large data
 *
 * The CONTEXT (DataSorter) can switch between strategies at runtime based on
 * data characteristics. The client doesn't need to know WHICH algorithm runs.
 *
 * ============================================================
 * THE PROBLEM WITHOUT STRATEGY:
 * ============================================================
 *
 * WITHOUT Strategy pattern, your DataSorter looks like this:
 *
 *   public void sort(List<T> data, String algorithm) {
 *       if (algorithm.equals("BUBBLE")) {
 *           // bubble sort code here (20+ lines)
 *       } else if (algorithm.equals("QUICK")) {
 *           // quick sort code here (40+ lines)
 *       } else if (algorithm.equals("MERGE")) {
 *           // merge sort code here (50+ lines)
 *       }
 *       // Adding a new algorithm = modify this method (violates Open/Closed Principle)
 *       // Testing each algorithm = test the whole DataSorter class
 *       // Sharing an algorithm = cannot reuse it in another class
 *   }
 *
 * WITH Strategy pattern:
 *   - Each algorithm is its own class (single responsibility)
 *   - DataSorter delegates to strategy.sort() — no if/else
 *   - New algorithm = new class, no changes to DataSorter
 *   - Each algorithm can be tested independently
 *   - Algorithms can be shared across different contexts
 *
 * ============================================================
 * WHERE YOU SEE STRATEGY IN JAVA:
 * ============================================================
 *   - java.util.Comparator<T>           — sort strategy for Collections.sort()
 *   - java.util.concurrent.RejectedExecutionHandler — what to do when thread pool full
 *   - javax.servlet.http.HttpServlet    — handleRequest() is a strategy
 *   - Spring's Resource/ResourceLoader  — different loading strategies
 *   - Java 8+ lambdas ARE strategies:   list.sort((a,b) -> a.compareTo(b))
 *
 * Generic type <T extends Comparable<T>> means this works for any type
 * that knows how to compare itself (Integer, String, Date, etc.)
 */

// =============================================================================
// FILE STRUCTURE:
//   1. SortingStrategy<T> interface          — Strategy interface
//   2. BubbleSortStrategy<T>                 — Concrete Strategy
//   3. QuickSortStrategy<T>                  — Concrete Strategy
//   4. MergeSortStrategy<T>                  — Concrete Strategy
//   5. DataSorter<T>                         — Context class
//   6. SortingStrategy (public class)        — Demo runner with main()
// =============================================================================

// -----------------------------------------------------------------------------
// STRATEGY INTERFACE: the algorithm contract
// -----------------------------------------------------------------------------
/**
 * SortingStrategy — the Strategy interface.
 *
 * All sorting algorithms implement this interface with a single method.
 * The context (DataSorter) only calls sort() — it doesn't care which
 * algorithm is behind the interface.
 *
 * WHY GENERIC? <T extends Comparable<T>>
 * This means: T must have a compareTo() method.
 * So we can sort Lists of Integer, String, Double, or any Comparable type.
 * The algorithm works for any comparable type without code duplication.
 */
interface SortAlgorithm<T extends Comparable<T>> {
    /**
     * Sort the given list in ascending order (in-place).
     *
     * @param list The list to sort. Modified in place (not a copy).
     */
    void sort(List<T> list);

    /**
     * Human-readable name of this algorithm (for logging/comparison)
     */
    String getAlgorithmName();

    /**
     * Big-O time complexity description (educational)
     */
    String getTimeComplexity();
}

// -----------------------------------------------------------------------------
// CONCRETE STRATEGY 1: Bubble Sort
// -----------------------------------------------------------------------------
/**
 * BubbleSortStrategy — simple, educational, for small/nearly-sorted data.
 *
 * ALGORITHM: Repeatedly step through the list, compare adjacent elements,
 * and swap them if they're in the wrong order. After each full pass, the
 * largest unsorted element "bubbles up" to its correct position.
 *
 * PERFORMANCE:
 *   Best case:  O(n)   — already sorted (with early exit optimization)
 *   Average:    O(n²)
 *   Worst case: O(n²)  — reverse sorted
 *   Space:      O(1)   — in-place, only a temp variable for swapping
 *
 * WHEN TO USE: Very small lists (< 20 elements) where simplicity matters,
 * or nearly-sorted lists (few elements out of place).
 */
class BubbleSortStrategy<T extends Comparable<T>> implements SortAlgorithm<T> {

    @Override
    public void sort(List<T> list) {
        int n = list.size();
        boolean swapped;

        // Outer loop: n-1 passes (after n-1 passes, list is sorted)
        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            // Inner loop: compare adjacent pairs
            // After pass i, the last i elements are already in their final place
            // so we only need to check up to n-1-i
            for (int j = 0; j < n - 1 - i; j++) {
                // Compare element at j with element at j+1
                if (list.get(j).compareTo(list.get(j + 1)) > 0) {
                    // They're in wrong order — swap them
                    T temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    swapped = true;
                }
            }

            // OPTIMIZATION: If no swaps occurred in this pass, the list is already sorted
            // This gives O(n) best-case for already-sorted inputs
            if (!swapped) break;
        }
    }

    @Override
    public String getAlgorithmName() { return "Bubble Sort"; }

    @Override
    public String getTimeComplexity() { return "O(n²) average/worst, O(n) best (with early exit)"; }
}

// -----------------------------------------------------------------------------
// CONCRETE STRATEGY 2: Quick Sort
// -----------------------------------------------------------------------------
/**
 * QuickSortStrategy — fast in practice, good for general-purpose sorting.
 *
 * ALGORITHM: Pick a pivot element, partition the list so all elements
 * smaller than pivot come before it and all larger come after. Then
 * recursively sort the two partitions.
 *
 * PERFORMANCE:
 *   Best case:  O(n log n) — balanced partitions
 *   Average:    O(n log n)
 *   Worst case: O(n²)      — sorted/reverse-sorted input with bad pivot choice
 *   Space:      O(log n)   — recursion stack depth
 *
 * WHEN TO USE: General purpose. Java's Arrays.sort() uses a variant of
 * QuickSort (dual-pivot) for primitive arrays.
 *
 * NOTE: We use median-of-three pivot selection to avoid worst-case on sorted input.
 */
class QuickSortStrategy<T extends Comparable<T>> implements SortAlgorithm<T> {

    @Override
    public void sort(List<T> list) {
        if (list.size() <= 1) return;
        quickSort(list, 0, list.size() - 1);
    }

    /**
     * Recursive QuickSort on the subarray from index 'low' to 'high' (inclusive).
     */
    private void quickSort(List<T> list, int low, int high) {
        if (low < high) {
            // Partition: place pivot in its final position and get its index
            int pivotIndex = partition(list, low, high);

            // Recursively sort the two halves
            quickSort(list, low, pivotIndex - 1);  // Left of pivot
            quickSort(list, pivotIndex + 1, high); // Right of pivot
        }
    }

    /**
     * Partition the subarray around a pivot.
     * After partitioning: all elements left of pivot < pivot < all elements right of pivot.
     *
     * We use the LAST element as the pivot (simple choice).
     * Real implementations use median-of-three for better pivot selection.
     *
     * @return The final index of the pivot element
     */
    private int partition(List<T> list, int low, int high) {
        T pivot = list.get(high); // Choose last element as pivot
        int i = low - 1; // i tracks the boundary of elements smaller than pivot

        for (int j = low; j < high; j++) {
            // If current element is <= pivot, it belongs in the left partition
            if (list.get(j).compareTo(pivot) <= 0) {
                i++;
                // Swap list[i] and list[j] to move smaller element to left partition
                T temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }

        // Place pivot in its final correct position (swap list[i+1] and list[high])
        T temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);

        return i + 1; // Return the pivot's final index
    }

    @Override
    public String getAlgorithmName() { return "Quick Sort"; }

    @Override
    public String getTimeComplexity() { return "O(n log n) average, O(n²) worst case"; }
}

// -----------------------------------------------------------------------------
// CONCRETE STRATEGY 3: Merge Sort
// -----------------------------------------------------------------------------
/**
 * MergeSortStrategy — guaranteed O(n log n), stable, good for large data.
 *
 * ALGORITHM: Divide the list in half, recursively sort each half, then
 * MERGE the two sorted halves back together. The merge step is key:
 * it compares elements from both halves and places the smaller one next.
 *
 * PERFORMANCE:
 *   Best case:  O(n log n)
 *   Average:    O(n log n)
 *   Worst case: O(n log n) — ALWAYS guaranteed, unlike QuickSort
 *   Space:      O(n)       — needs auxiliary arrays for merging
 *
 * WHEN TO USE:
 *   - Large datasets where O(n²) worst-case is unacceptable
 *   - Linked lists (QuickSort is poor on linked lists)
 *   - External sorting (data larger than RAM, on disk)
 *   - When STABLE sort is required (equal elements maintain original order)
 *
 * Java's Arrays.sort(Object[]) uses TimSort (hybrid Merge+Insertion sort).
 */
class MergeSortStrategy<T extends Comparable<T>> implements SortAlgorithm<T> {

    @Override
    public void sort(List<T> list) {
        if (list.size() <= 1) return;
        List<T> sorted = mergeSort(list);
        // Copy sorted result back into the original list
        for (int i = 0; i < sorted.size(); i++) {
            list.set(i, sorted.get(i));
        }
    }

    /**
     * Returns a NEW sorted list containing all elements from 'list'.
     * This is the divide-and-conquer recursion.
     */
    private List<T> mergeSort(List<T> list) {
        // BASE CASE: a list of 0 or 1 elements is already sorted
        if (list.size() <= 1) return new ArrayList<>(list);

        // DIVIDE: split list into two halves
        int mid = list.size() / 2;
        List<T> leftHalf = list.subList(0, mid);   // first half
        List<T> rightHalf = list.subList(mid, list.size()); // second half

        // RECURSE: sort each half independently
        List<T> sortedLeft = mergeSort(leftHalf);
        List<T> sortedRight = mergeSort(rightHalf);

        // CONQUER: merge the two sorted halves
        return merge(sortedLeft, sortedRight);
    }

    /**
     * Merge two sorted lists into one sorted list.
     *
     * This is the core of Merge Sort. We compare the front of each list
     * and pick the smaller element, advancing that pointer.
     *
     * Time: O(n+m) where n, m are sizes of left and right.
     */
    private List<T> merge(List<T> left, List<T> right) {
        List<T> result = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;

        // Compare elements from left and right, adding the smaller one
        while (i < left.size() && j < right.size()) {
            if (left.get(i).compareTo(right.get(j)) <= 0) {
                result.add(left.get(i));
                i++;
            } else {
                result.add(right.get(j));
                j++;
            }
        }

        // Add remaining elements from whichever list isn't exhausted yet
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));

        return result;
    }

    @Override
    public String getAlgorithmName() { return "Merge Sort"; }

    @Override
    public String getTimeComplexity() { return "O(n log n) guaranteed, O(n) space"; }
}

// -----------------------------------------------------------------------------
// CONTEXT CLASS: DataSorter — uses a SortingStrategy
// -----------------------------------------------------------------------------
/**
 * DataSorter — the Context class in the Strategy pattern.
 *
 * The Context:
 *   1. Holds a reference to a SortingStrategy
 *   2. Delegates the sorting WORK to the strategy
 *   3. Can CHANGE its strategy at runtime (changeStrategy())
 *   4. Optionally provides smart strategy selection based on data size
 *
 * CRITICAL: DataSorter depends on the SortingStrategy INTERFACE, not any
 * concrete class. This means you can inject any strategy, including future
 * ones that haven't been written yet (Dependency Inversion Principle).
 *
 * @param <T> Type of elements to sort (must be Comparable)
 */
class DataSorter<T extends Comparable<T>> {
    // Strategy reference — points to an interface, not a concrete class
    private SortAlgorithm<T> strategy;
    private int sortCount = 0;

    /**
     * Constructor: inject the strategy at creation time.
     * Strategy can also be changed later via changeStrategy().
     *
     * @param strategy Initial sorting strategy to use
     */
    public DataSorter(SortAlgorithm<T> strategy) {
        if (strategy == null) throw new IllegalArgumentException("Strategy cannot be null");
        this.strategy = strategy;
        System.out.println("[DataSorter] Created with strategy: " + strategy.getAlgorithmName());
    }

    /**
     * Sort the given list using the current strategy.
     * Modifies the list in place.
     *
     * @param list The list to sort
     */
    public void sort(List<T> list) {
        if (list == null || list.isEmpty()) return;

        System.out.printf("[DataSorter] Sorting %d elements with %s...%n",
                list.size(), strategy.getAlgorithmName());
        long startNs = System.nanoTime();

        strategy.sort(list); // Delegate to the current strategy — no if/else!

        long elapsedNs = System.nanoTime() - startNs;
        sortCount++;
        System.out.printf("[DataSorter] Done in %.3f ms%n", elapsedNs / 1_000_000.0);
    }

    /**
     * RUNTIME STRATEGY SWITCHING — the hallmark of the Strategy pattern.
     *
     * This is the key capability: we can change the algorithm while the
     * program is running, without changing any other code.
     *
     * Use cases:
     *   - User picks a sort algorithm in the UI
     *   - System automatically selects based on data size or characteristics
     *   - A/B testing different algorithms for performance comparison
     *
     * @param newStrategy The new strategy to use for future sort() calls
     */
    public void changeStrategy(SortAlgorithm<T> newStrategy) {
        if (newStrategy == null) throw new IllegalArgumentException("Strategy cannot be null");
        String oldName = this.strategy.getAlgorithmName();
        this.strategy = newStrategy;
        System.out.printf("[DataSorter] Strategy changed: %s → %s%n",
                oldName, newStrategy.getAlgorithmName());
    }

    /**
     * SMART STRATEGY SELECTION based on data size.
     *
     * This demonstrates how the context can automatically pick the right
     * strategy based on runtime information. The calling code just calls
     * sortWithAutoStrategy() and doesn't need to know about algorithms.
     *
     * Thresholds are approximate; real thresholds depend on hardware and data.
     */
    public void sortWithAutoStrategy(List<T> list) {
        int size = list.size();
        SortAlgorithm<T> autoStrategy;

        if (size <= 15) {
            // Small data: Bubble Sort's simplicity wins; overhead of QuickSort not worth it
            autoStrategy = new BubbleSortStrategy<>();
        } else if (size <= 10_000) {
            // Medium data: QuickSort's cache locality usually beats Merge Sort
            autoStrategy = new QuickSortStrategy<>();
        } else {
            // Large data: Guaranteed O(n log n) and no stack overflow risk
            autoStrategy = new MergeSortStrategy<>();
        }

        System.out.printf("[DataSorter] Auto-selected %s for %d elements%n",
                autoStrategy.getAlgorithmName(), size);
        changeStrategy(autoStrategy);
        sort(list);
    }

    public SortAlgorithm<T> getCurrentStrategy() { return strategy; }
    public int getSortCount() { return sortCount; }
}

// =============================================================================
// DEMO RUNNER
// =============================================================================
/**
 * Demonstrates the Strategy pattern with runtime algorithm switching.
 *
 * NOTE: This class is named SortingStrategyDemo (not SortingStrategy) because
 * SortingStrategy is already the name of the interface in this file.
 * Java requires the public class name to match the filename; since the file
 * is named SortingStrategy.java this class IS the file's public class.
 * We use a distinct name to avoid the conflict.
 */
public class SortingStrategy {

    /** Helper: creates a copy of an array as an ArrayList */
    @SafeVarargs
    private static <T> List<T> listOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("DESIGN PATTERN: Strategy");
        System.out.println("USE CASE: Sorting Algorithm Selection");
        System.out.println("=".repeat(65) + "\n");

        // -------------------------------------------------------
        // DEMO 1: Create sorter with Bubble Sort strategy
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: Bubble Sort on Small List ---");
        DataSorter<Integer> sorter = new DataSorter<Integer>(new BubbleSortStrategy<Integer>());

        List<Integer> small = listOf(64, 34, 25, 12, 22, 11, 90);
        System.out.println("Before: " + small);
        sorter.sort(small);
        System.out.println("After:  " + small);
        System.out.println("Complexity: " + sorter.getCurrentStrategy().getTimeComplexity());

        // -------------------------------------------------------
        // DEMO 2: RUNTIME switch to Quick Sort
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 2: Runtime Switch to Quick Sort ---");
        sorter.changeStrategy(new QuickSortStrategy<Integer>());

        List<Integer> medium = new ArrayList<>();
        for (int i = 100; i >= 1; i--) medium.add(i); // 100 down to 1

        System.out.println("Before (first 10): " + medium.subList(0, 10) + "...");
        sorter.sort(medium);
        System.out.println("After  (first 10): " + medium.subList(0, 10) + "...");
        System.out.println("Correctly sorted: " + isSorted(medium));

        // -------------------------------------------------------
        // DEMO 3: RUNTIME switch to Merge Sort
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 3: Runtime Switch to Merge Sort ---");
        sorter.changeStrategy(new MergeSortStrategy<Integer>());

        List<Integer> large = new ArrayList<>();
        for (int i = 500; i >= 1; i--) large.add(i);

        System.out.println("Before (first 5): " + large.subList(0, 5) + "...");
        sorter.sort(large);
        System.out.println("After  (first 5): " + large.subList(0, 5) + "...");
        System.out.println("Correctly sorted: " + isSorted(large));

        // -------------------------------------------------------
        // DEMO 4: Sorting Strings (shows generics working)
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 4: Sorting Strings with Strategy ---");
        // Separate sorter for a different type — strategy pattern is generic
        DataSorter<String> stringSorter = new DataSorter<String>(new QuickSortStrategy<String>());

        List<String> words = listOf("banana", "apple", "cherry", "date", "elderberry", "fig");
        System.out.println("Before: " + words);
        stringSorter.sort(words);
        System.out.println("After:  " + words);

        // -------------------------------------------------------
        // DEMO 5: Auto-Strategy Selection Based on Size
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 5: Auto Strategy Selection ---");
        DataSorter<Integer> autoSorter = new DataSorter<Integer>(new BubbleSortStrategy<Integer>());

        int[] sizes = {5, 50, 20_000};
        for (int size : sizes) {
            List<Integer> data = new ArrayList<>();
            for (int i = size; i >= 1; i--) data.add(i);
            System.out.println("\nData size: " + size);
            autoSorter.sortWithAutoStrategy(data);
            System.out.println("Sorted correctly: " + isSorted(data));
        }

        // -------------------------------------------------------
        // DEMO 6: Strategy as Lambda (Java 8+)
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 6: Strategy as Lambda (modern Java) ---");
        System.out.println("In modern Java, a SortingStrategy CAN be expressed as a lambda:");
        System.out.println("  DataSorter<Integer> lambdaSorter = new DataSorter<>(list -> list.sort(null));");
        System.out.println("  This shows strategies are just behavior capsules.");
        System.out.println("  Java's Comparator IS the Strategy pattern for ordering.");

        // An anonymous class implementing SortAlgorithm using Collections.sort
        SortAlgorithm<Integer> javaBuiltIn = new SortAlgorithm<Integer>() {
            @Override
            public void sort(List<Integer> list) { java.util.Collections.sort(list); }
            @Override
            public String getAlgorithmName() { return "Java Built-in TimSort"; }
            @Override
            public String getTimeComplexity() { return "O(n log n) stable (TimSort)"; }
        };

        DataSorter<Integer> timsortSorter = new DataSorter<Integer>(javaBuiltIn);
        List<Integer> test = listOf(5, 1, 4, 2, 8, 3, 7, 6);
        timsortSorter.sort(test);
        System.out.println("TimSort result: " + test);

        System.out.println("\n--- Strategy Pattern Summary ---");
        System.out.println("Total sorts performed: " + (sorter.getSortCount() + autoSorter.getSortCount()));
        System.out.println("Each algorithm is its own class: testable, reusable, replaceable");
        System.out.println("DataSorter never changed — new algorithms added without touching it");
        System.out.println("\nDone!");
    }

    /** Helper method to verify a list is sorted in ascending order */
    private static <T extends Comparable<T>> boolean isSorted(List<T> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).compareTo(list.get(i + 1)) > 0) return false;
        }
        return true;
    }
}
