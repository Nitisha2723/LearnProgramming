/**
 * Exercise02_Loops.java
 *
 * EXERCISE: Loop Patterns
 *
 * Three tasks that require different loop strategies.
 *
 * =====================================================================
 * TASK A: Print a triangle of stars
 * =====================================================================
 * Print a right-aligned triangle of stars where each row has
 * a number of stars equal to its row number (1-indexed), up to 6 rows.
 *
 * EXPECTED OUTPUT:
 *   *
 *   **
 *   ***
 *   ****
 *   *****
 *   ******
 *
 * HINT: Nested loops. Outer loop controls rows, inner loop prints stars.
 *
 * =====================================================================
 * TASK B: Find all prime numbers up to 100
 * =====================================================================
 * Print all prime numbers between 2 and 100, separated by spaces.
 * At the end, print how many primes were found.
 *
 * EXPECTED OUTPUT:
 *   2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97
 *   Count: 25
 *
 * HINT: Use a method isPrime(int n) — a number is prime if no integer
 * from 2 to sqrt(n) divides it evenly.
 *
 * =====================================================================
 * TASK C: Reverse a number's digits
 * =====================================================================
 * Given an integer, reverse its digits and print the result.
 * Test with: 12345 → 54321, 100 → 1, 9000 → 9
 *
 * EXPECTED OUTPUT:
 *   Original: 12345 → Reversed: 54321
 *   Original: 100   → Reversed: 1
 *   Original: 9000  → Reversed: 9
 *
 * HINT: Use the % operator to extract the last digit (n % 10),
 * then divide n by 10 (integer division) to remove it. Repeat with while.
 * Build the reversed number: reversed = reversed * 10 + lastDigit
 *
 * SKILLS PRACTICED:
 * - Nested for loops
 * - while loops
 * - Methods
 * - Integer arithmetic (%, /)
 */
public class Exercise02_Loops {

    public static void main(String[] args) {
        System.out.println("=== Task A: Star Triangle ===");
        printTriangle(6);

        System.out.println("\n=== Task B: Primes up to 100 ===");
        printPrimes(100);

        System.out.println("\n=== Task C: Reverse Digits ===");
        System.out.println("Original: 12345 → Reversed: " + reverseDigits(12345));
        System.out.println("Original: 100   → Reversed: " + reverseDigits(100));
        System.out.println("Original: 9000  → Reversed: " + reverseDigits(9000));
    }

    /**
     * TASK A: Prints a right-aligned triangle of stars.
     *
     * @param rows the number of rows in the triangle
     */
    static void printTriangle(int rows) {
        // TODO: Write two nested loops.
        //       Outer loop: row goes from 1 to rows (inclusive)
        //       Inner loop: print 'row' number of stars
        //       After inner loop: print newline with System.out.println()
    }

    /**
     * TASK B: Prints all prime numbers from 2 to max, then the count.
     *
     * @param max the upper bound (inclusive)
     */
    static void printPrimes(int max) {
        // TODO: Use a for loop from 2 to max.
        //       For each number, call isPrime(). If true, print it and increment a counter.
        //       After the loop, print the count.
    }

    /**
     * Helper for Task B: returns true if n is prime.
     *
     * @param n the number to check
     * @return true if n is prime
     */
    static boolean isPrime(int n) {
        // TODO: Return false if n < 2.
        //       Check all divisors from 2 to (int)Math.sqrt(n).
        //       If any divides n evenly (n % i == 0), return false.
        //       If none do, return true.
        return false; // replace this
    }

    /**
     * TASK C: Reverses the digits of a positive integer.
     *
     * @param n the integer to reverse
     * @return the number with digits reversed
     */
    static int reverseDigits(int n) {
        // TODO:
        //   int reversed = 0;
        //   while (n > 0) {
        //       int lastDigit = n % 10;           // extract last digit
        //       reversed = reversed * 10 + lastDigit; // append to reversed
        //       n = n / 10;                        // remove last digit
        //   }
        //   return reversed;
        return 0; // replace this
    }
}
