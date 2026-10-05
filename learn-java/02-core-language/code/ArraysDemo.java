import java.util.Arrays;

/**
 * ArraysDemo.java
 *
 * Demonstrates Java array concepts:
 * - Declaration and initialization (three ways)
 * - Iterating with for and for-each
 * - Searching and sorting
 * - Common operations: sum, average, min, max
 * - Two-dimensional arrays
 * - Arrays utility methods
 *
 * Read theory/03-arrays.md before studying this file.
 *
 * How to run:
 *   javac ArraysDemo.java
 *   java ArraysDemo
 */
public class ArraysDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  ARRAYS DEMO");
        System.out.println("=================================================\n");

        demoDeclarationAndInit();
        demoIteration();
        demoCommonOperations();
        demoSearching();
        demoSorting();
        demo2DArrays();
        demoArraysUtility();
        demoCommonPitfall();
    }

    // -------------------------------------------------------------------------
    // SECTION 1: Declaration and Initialization
    // Three ways to create an array
    // -------------------------------------------------------------------------
    static void demoDeclarationAndInit() {
        System.out.println("--- Declaration and Initialization ---");

        // Method 1: declare, then allocate (values default to 0)
        int[] scores;
        scores = new int[5];
        System.out.println("Method 1 (allocate only): " + Arrays.toString(scores));
        // Output: [0, 0, 0, 0, 0]  — default int value is 0

        // Method 2: declare and allocate together
        double[] prices = new double[3];
        System.out.println("Method 2 (allocate): " + Arrays.toString(prices));
        // Output: [0.0, 0.0, 0.0]

        // Method 3: array literal — declare, allocate, AND initialize
        int[] primes = {2, 3, 5, 7, 11, 13};
        System.out.println("Method 3 (literal): " + Arrays.toString(primes));

        // String array with defaults
        String[] names = new String[3];
        System.out.println("String array default: " + Arrays.toString(names));
        // Output: [null, null, null] — default object value is null

        // Populating after creation
        names[0] = "Alice";
        names[1] = "Bob";
        names[2] = "Charlie";
        System.out.println("After population: " + Arrays.toString(names));

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 2: Iterating Arrays
    // Both for loop (when index needed) and for-each (when only values needed)
    // -------------------------------------------------------------------------
    static void demoIteration() {
        System.out.println("--- Iteration ---");

        int[] temperatures = {22, 18, 25, 30, 27, 20, 15};
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // Traditional for — we need the index to access both arrays
        System.out.println("Weekly temperatures:");
        for (int i = 0; i < temperatures.length; i++) {
            System.out.printf("  %s: %d°C%n", days[i], temperatures[i]);
        }

        // for-each — cleaner when you only need values
        System.out.print("\nAll temperatures: ");
        for (int temp : temperatures) {
            System.out.print(temp + " ");
        }
        System.out.println("°C");

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 3: Common Operations — Sum, Average, Min, Max
    // -------------------------------------------------------------------------
    static void demoCommonOperations() {
        System.out.println("--- Common Operations ---");

        int[] scores = {85, 92, 78, 90, 88, 76, 95, 82};
        System.out.println("Scores: " + Arrays.toString(scores));

        // Sum
        int sum = computeSum(scores);
        System.out.println("Sum: " + sum);

        // Average — note the cast to double to avoid integer division
        double average = (double) sum / scores.length;
        System.out.printf("Average: %.2f%n", average);

        // Min and Max
        System.out.println("Min: " + findMin(scores));
        System.out.println("Max: " + findMax(scores));

        // Count elements meeting a condition
        int aboveAverage = 0;
        for (int score : scores) {
            if (score > average) aboveAverage++;
        }
        System.out.println("Above average: " + aboveAverage + " students");

        System.out.println();
    }

    static int computeSum(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }

    static int findMin(int[] array) {
        int min = array[0]; // start with first element
        for (int value : array) {
            if (value < min) min = value;
        }
        return min;
    }

    static int findMax(int[] array) {
        int max = array[0]; // start with first element
        for (int value : array) {
            if (value > max) max = value;
        }
        return max;
    }

    // -------------------------------------------------------------------------
    // SECTION 4: Searching
    // Linear search and binary search
    // -------------------------------------------------------------------------
    static void demoSearching() {
        System.out.println("--- Searching ---");

        int[] numbers = {5, 3, 8, 1, 9, 2, 7, 4, 6};
        System.out.println("Array: " + Arrays.toString(numbers));

        // Linear search — works on unsorted arrays
        int target = 9;
        int idx = linearSearch(numbers, target);
        System.out.println("Linear search for " + target + ": index " + idx); // 4

        int notFound = 42;
        System.out.println("Linear search for " + notFound + ": index " + linearSearch(numbers, notFound)); // -1

        // Binary search — array MUST be sorted first
        int[] sorted = Arrays.copyOf(numbers, numbers.length);
        Arrays.sort(sorted);
        System.out.println("Sorted for binary search: " + Arrays.toString(sorted));

        int bsResult = Arrays.binarySearch(sorted, target);
        System.out.println("Binary search for " + target + ": index " + bsResult);

        System.out.println();
    }

    /**
     * Linear (sequential) search — checks each element one by one.
     * Time complexity: O(n) — up to n comparisons.
     *
     * @return index of target, or -1 if not found
     */
    static int linearSearch(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i; // found at index i
            }
        }
        return -1; // convention: -1 means "not found"
    }

    // -------------------------------------------------------------------------
    // SECTION 5: Sorting
    // -------------------------------------------------------------------------
    static void demoSorting() {
        System.out.println("--- Sorting ---");

        int[] numbers = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Before sort: " + Arrays.toString(numbers));

        // Arrays.sort() uses a highly optimized algorithm (Dual-Pivot Quicksort)
        Arrays.sort(numbers);
        System.out.println("After sort:  " + Arrays.toString(numbers));

        // Sort a portion (indices 1 through 4, exclusive end)
        int[] partial = {5, 3, 8, 1, 9, 2, 7};
        System.out.println("Partial sort before: " + Arrays.toString(partial));
        Arrays.sort(partial, 1, 5); // sort indices 1, 2, 3, 4
        System.out.println("Partial sort after:  " + Arrays.toString(partial));

        // Sorting Strings — lexicographic order
        String[] names = {"Charlie", "Alice", "Eve", "Bob", "Dave"};
        System.out.println("Names before sort: " + Arrays.toString(names));
        Arrays.sort(names);
        System.out.println("Names after sort:  " + Arrays.toString(names));

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 6: Two-Dimensional Arrays
    // A matrix (grid) — array of arrays
    // -------------------------------------------------------------------------
    static void demo2DArrays() {
        System.out.println("--- Two-Dimensional Arrays ---");

        // Representing a 3×3 tic-tac-toe board
        char[][] board = {
            {'X', 'O', 'X'},
            {'O', 'X', 'O'},
            {'O', ' ', 'X'}
        };

        System.out.println("Tic-Tac-Toe board:");
        printBoard(board);

        // Accessing elements
        System.out.println("Center cell: " + board[1][1]); // 'X'
        System.out.println("Top-right:   " + board[0][2]); // 'X'

        // A grade matrix: 3 students × 4 tests
        int[][] grades = {
            {85, 90, 78, 92},  // Student 0
            {76, 88, 95, 80},  // Student 1
            {90, 85, 87, 93}   // Student 2
        };

        String[] students = {"Alice", "Bob", "Charlie"};

        System.out.println("\nStudent grade report:");
        System.out.printf("%-10s  T1  T2  T3  T4  Avg%n", "Student");
        System.out.println("-".repeat(38));

        for (int s = 0; s < grades.length; s++) {
            int total = 0;
            System.out.printf("%-10s", students[s]);
            for (int t = 0; t < grades[s].length; t++) {
                System.out.printf("  %2d", grades[s][t]);
                total += grades[s][t];
            }
            double avg = (double) total / grades[s].length;
            System.out.printf("  %.1f%n", avg);
        }

        System.out.println();
    }

    static void printBoard(char[][] board) {
        for (int row = 0; row < board.length; row++) {
            System.out.print("  ");
            for (int col = 0; col < board[row].length; col++) {
                if (col > 0) System.out.print("|");
                System.out.print(board[row][col]);
            }
            System.out.println();
            if (row < board.length - 1) {
                System.out.println("  -----");
            }
        }
    }

    // -------------------------------------------------------------------------
    // SECTION 7: Arrays Utility Methods
    // -------------------------------------------------------------------------
    static void demoArraysUtility() {
        System.out.println("--- java.util.Arrays Utility Methods ---");

        int[] a = {3, 1, 4, 1, 5, 9, 2, 6};

        // toString — essential for printing arrays (default toString is ugly)
        System.out.println("Arrays.toString(): " + Arrays.toString(a));

        // sort + binarySearch
        Arrays.sort(a);
        System.out.println("After sort: " + Arrays.toString(a));
        System.out.println("binarySearch(5): " + Arrays.binarySearch(a, 5));

        // fill
        int[] filled = new int[5];
        Arrays.fill(filled, 42);
        System.out.println("Arrays.fill(42): " + Arrays.toString(filled));

        // copyOf (pads with 0 if new length is larger)
        int[] copy = Arrays.copyOf(a, a.length);
        int[] larger = Arrays.copyOf(a, a.length + 3);
        System.out.println("copyOf: " + Arrays.toString(copy));
        System.out.println("copyOf (larger): " + Arrays.toString(larger));

        // copyOfRange (from inclusive, to exclusive)
        int[] slice = Arrays.copyOfRange(a, 2, 5);
        System.out.println("copyOfRange(2, 5): " + Arrays.toString(slice));

        // equals — ALWAYS use this, never ==
        int[] b = Arrays.copyOf(a, a.length);
        System.out.println("Arrays.equals(a, b): " + Arrays.equals(a, b)); // true
        System.out.println("a == b (wrong!):      " + (a == b));           // false

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 8: Common Pitfall — ArrayIndexOutOfBoundsException
    // -------------------------------------------------------------------------
    static void demoCommonPitfall() {
        System.out.println("--- Common Pitfall: ArrayIndexOutOfBoundsException ---");

        int[] data = {10, 20, 30, 40, 50};
        System.out.println("Array: " + Arrays.toString(data));
        System.out.println("Length: " + data.length + " → valid indices: 0 to " + (data.length - 1));

        // CORRECT loop — condition is i < length (not <=)
        System.out.print("Correct iteration: ");
        for (int i = 0; i < data.length; i++) { // i goes 0, 1, 2, 3, 4
            System.out.print(data[i] + " ");
        }
        System.out.println();

        // Demonstrate the error in a try-catch (so the program doesn't crash)
        System.out.print("Attempting data[5]: ");
        try {
            System.out.println(data[5]); // index 5 doesn't exist!
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("CAUGHT: " + e.getMessage());
            System.out.println("  → Remember: last valid index = array.length - 1 = " + (data.length - 1));
        }

        System.out.println();
    }
}
