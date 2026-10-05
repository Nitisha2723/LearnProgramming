/**
 * Exercise02_Solution.java
 *
 * SOLUTION: Loop Patterns
 */
public class Exercise02_Solution {

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
     * TASK A: Prints a star triangle with 'rows' rows.
     *
     * Pattern: row 1 → 1 star, row 2 → 2 stars, ..., row n → n stars
     *
     * Outer loop iterates over rows 1..rows.
     * Inner loop prints exactly 'row' stars.
     */
    static void printTriangle(int rows) {
        for (int row = 1; row <= rows; row++) {       // outer: which row
            for (int star = 1; star <= row; star++) { // inner: how many stars
                System.out.print("*");
            }
            System.out.println(); // newline after each row
        }
    }

    /**
     * TASK B: Prints all primes from 2 to max, then the count.
     */
    static void printPrimes(int max) {
        int count = 0;
        for (int n = 2; n <= max; n++) {
            if (isPrime(n)) {
                System.out.print(n + " ");
                count++;
            }
        }
        System.out.println();
        System.out.println("Count: " + count); // 25 primes up to 100
    }

    /**
     * Returns true if n is prime.
     *
     * KEY INSIGHT: Only need to check divisors up to sqrt(n).
     * If n = a × b and a <= b, then a <= sqrt(n).
     * So if no divisor up to sqrt(n) works, no divisor above it will either.
     */
    static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;   // 2 is the only even prime
        if (n % 2 == 0) return false; // all other evens are not prime

        // Check odd divisors from 3 up to sqrt(n)
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    /**
     * TASK C: Reverses digits by repeatedly extracting the last digit.
     *
     * Algorithm:
     *   - last digit of n = n % 10
     *   - remove last digit: n = n / 10
     *   - build reversed: reversed = reversed * 10 + lastDigit
     *
     * Example: n = 12345
     *   Step 1: lastDigit=5, reversed=5,    n=1234
     *   Step 2: lastDigit=4, reversed=54,   n=123
     *   Step 3: lastDigit=3, reversed=543,  n=12
     *   Step 4: lastDigit=2, reversed=5432, n=1
     *   Step 5: lastDigit=1, reversed=54321, n=0 → stop
     */
    static int reverseDigits(int n) {
        int reversed = 0;
        while (n > 0) {
            int lastDigit = n % 10;            // extract rightmost digit
            reversed = reversed * 10 + lastDigit; // shift left and append
            n = n / 10;                         // drop rightmost digit
        }
        return reversed;
    }
}
