import java.util.HashMap;
import java.util.Map;

/**
 * PROBLEM: Fibonacci Number
 * Return the nth Fibonacci number (0-indexed).
 *   F(0) = 0
 *   F(1) = 1
 *   F(n) = F(n-1) + F(n-2) for n >= 2
 *
 * Sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144...
 *
 * This file shows THREE approaches from worst to best:
 *   1. Naive Recursion     — O(2^n) time, O(n) space  [DON'T use in production]
 *   2. Memoized Recursion  — O(n) time, O(n) space     [Good; top-down DP]
 *   3. Iterative           — O(n) time, O(1) space     [Best; bottom-up DP]
 *
 * Understanding this progression is a classic interview demonstration of
 * how to optimize a solution step by step.
 */
public class Fibonacci {

    // -----------------------------------------------------------------------
    // APPROACH 1: Naive Recursive
    // -----------------------------------------------------------------------
    /**
     * Naive recursive Fibonacci.
     *
     * The recursive call tree for fib(6):
     *                    fib(6)
     *                  /        \
     *             fib(5)        fib(4)
     *            /      \      /      \
     *         fib(4)  fib(3) fib(3) fib(2)
     *         ...
     *
     * Notice fib(4) is computed TWICE, fib(3) is computed THREE times.
     * This redundancy grows exponentially.
     *
     * TIME:  O(2^n) — each call spawns 2 more; tree has ~2^n nodes
     * SPACE: O(n)   — maximum recursion depth is n (call stack frames)
     *
     * PROBLEM: fib(50) makes ~2^50 = 1 quadrillion recursive calls.
     *          This will hang forever in practice.
     */
    public long fibNaive(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fibNaive(n - 1) + fibNaive(n - 2);
    }

    // -----------------------------------------------------------------------
    // APPROACH 2: Memoized Recursion (Top-Down Dynamic Programming)
    // -----------------------------------------------------------------------
    /**
     * Memoized Fibonacci.
     *
     * Cache the result of each subproblem. If we've already computed fib(k),
     * return the cached value instead of recomputing.
     *
     * Call tree for fib(6) with memoization:
     *   fib(6) → compute fib(5) + fib(4)
     *   fib(5) → compute fib(4) + fib(3)
     *   fib(4) → compute fib(3) + fib(2)
     *   fib(3) → compute fib(2) + fib(1)
     *   fib(2) → compute fib(1) + fib(0)  ← base cases
     *   Back up: fib(2)=1, fib(3)=2, fib(4)=3, fib(5)=5, fib(6)=8
     *   When fib(5) needs fib(4): already cached → O(1) lookup
     *
     * TIME:  O(n) — each of n subproblems computed exactly once
     * SPACE: O(n) — memo table holds n entries + O(n) call stack depth
     *
     * This is "top-down DP" — we start from the top (fib(n)) and recurse
     * down, memoizing results as we return back up.
     */
    private Map<Integer, Long> memo = new HashMap<>();

    public long fibMemo(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (n == 0) return 0;
        if (n == 1) return 1;

        // Return cached result if available
        if (memo.containsKey(n)) {
            return memo.get(n);
        }

        // Compute, cache, and return
        long result = fibMemo(n - 1) + fibMemo(n - 2);
        memo.put(n, result);
        return result;
    }

    // -----------------------------------------------------------------------
    // APPROACH 3: Iterative (Bottom-Up Dynamic Programming)
    // -----------------------------------------------------------------------
    /**
     * Iterative Fibonacci — the best approach.
     *
     * Instead of recursing top-down, build up from base cases:
     *   fib(0) = 0
     *   fib(1) = 1
     *   fib(2) = fib(1) + fib(0) = 1
     *   fib(3) = fib(2) + fib(1) = 2
     *   ...
     *
     * We only ever need the previous TWO values, so we use two variables
     * instead of an array — O(1) space.
     *
     * Trace for n=6:
     *   prev2=0, prev1=1
     *   i=2: curr=1, prev2=1, prev1=1
     *   i=3: curr=2, prev2=1, prev1=2
     *   i=4: curr=3, prev2=2, prev1=3
     *   i=5: curr=5, prev2=3, prev1=5
     *   i=6: curr=8, prev2=5, prev1=8
     *   return 8 ✓
     *
     * TIME:  O(n) — single loop from 2 to n
     * SPACE: O(1) — only three variables (prev2, prev1, curr)
     *
     * This is "bottom-up DP" — we solve small subproblems first and
     * build up to the answer.
     */
    public long fibIterative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (n == 0) return 0;
        if (n == 1) return 1;

        long prev2 = 0; // fib(n-2)
        long prev1 = 1; // fib(n-1)

        for (int i = 2; i <= n; i++) {
            long curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }

        return prev1;
    }

    // -----------------------------------------------------------------------
    // BONUS: Matrix exponentiation — O(log n) time, O(1) space
    // -----------------------------------------------------------------------
    /**
     * Matrix exponentiation approach — O(log n) time.
     * Uses the identity:
     *   | F(n+1) |   | 1 1 |^n   | F(1) |
     *   | F(n)   | = | 1 0 |   * | F(0) |
     *
     * This is rarely needed in interviews but demonstrates mathematical insight.
     * Useful for computing fib(10^18) efficiently.
     */
    public long fibMatrix(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        if (n == 0) return 0;

        long[][] matrix = {{1, 1}, {1, 0}};
        long[][] result = matPow(matrix, n - 1);
        return result[0][0];
    }

    private long[][] matPow(long[][] m, int power) {
        if (power == 0) return new long[][]{{1, 0}, {0, 1}}; // identity
        if (power == 1) return m;

        long[][] half = matPow(m, power / 2);
        long[][] squared = matMul(half, half);

        if (power % 2 == 0) return squared;
        else return matMul(squared, m);
    }

    private long[][] matMul(long[][] a, long[][] b) {
        return new long[][]{
            {a[0][0]*b[0][0] + a[0][1]*b[1][0],  a[0][0]*b[0][1] + a[0][1]*b[1][1]},
            {a[1][0]*b[0][0] + a[1][1]*b[1][0],  a[1][0]*b[0][1] + a[1][1]*b[1][1]}
        };
    }

    // -----------------------------------------------------------------------
    // Test cases + comparison
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        Fibonacci solution = new Fibonacci();

        System.out.println("=== Fibonacci: Correctness Check ===");
        System.out.println();

        // Print first 15 Fibonacci numbers
        System.out.println("First 15 Fibonacci numbers (iterative):");
        System.out.print("  ");
        for (int i = 0; i <= 14; i++) {
            System.out.print(solution.fibIterative(i));
            if (i < 14) System.out.print(", ");
        }
        System.out.println();
        System.out.println("  Expected: 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377");
        System.out.println();

        // Verify all three approaches give the same result
        int[] testCases = {0, 1, 2, 5, 10, 20, 30};
        System.out.println("Comparison: Naive vs Memoized vs Iterative vs Matrix");
        System.out.printf("%-5s %-12s %-12s %-12s %-12s%n", "n", "Naive", "Memoized", "Iterative", "Matrix");
        System.out.println("  " + "-".repeat(53));
        for (int n : testCases) {
            long naive     = solution.fibNaive(n);      // OK for small n
            solution.memo.clear(); // Reset memo for clean test
            long memoized  = solution.fibMemo(n);
            long iterative = solution.fibIterative(n);
            long matrix    = solution.fibMatrix(n);
            System.out.printf("%-5d %-12d %-12d %-12d %-12d%n",
                n, naive, memoized, iterative, matrix);
        }

        System.out.println();
        System.out.println("=== Performance Comparison ===");
        System.out.println();

        // Show why naive is slow for larger n
        int largeN = 45;
        System.out.println("fib(45) performance:");

        long start = System.currentTimeMillis();
        long result = solution.fibNaive(largeN);
        long naiveTime = System.currentTimeMillis() - start;
        System.out.printf("  Naive:     %d (took %d ms)%n", result, naiveTime);

        start = System.currentTimeMillis();
        solution.memo.clear();
        result = solution.fibMemo(largeN);
        long memoTime = System.currentTimeMillis() - start;
        System.out.printf("  Memoized:  %d (took %d ms)%n", result, memoTime);

        start = System.currentTimeMillis();
        result = solution.fibIterative(largeN);
        long iterTime = System.currentTimeMillis() - start;
        System.out.printf("  Iterative: %d (took %d ms)%n", result, iterTime);

        System.out.println();
        System.out.println("Note: For n=50, naive would take minutes. For n=100, it would take");
        System.out.println("      longer than the age of the universe. Memoized and iterative are instant.");

        System.out.println();
        System.out.println("=== Complexity Summary ===");
        System.out.println("  Naive:       O(2^n) time, O(n) space  — unusable for n > 40");
        System.out.println("  Memoized:    O(n) time,   O(n) space  — good, but uses memory and call stack");
        System.out.println("  Iterative:   O(n) time,   O(1) space  — BEST for most interviews");
        System.out.println("  Matrix exp:  O(log n) time, O(1) space — useful for very large n (10^18)");
    }
}
