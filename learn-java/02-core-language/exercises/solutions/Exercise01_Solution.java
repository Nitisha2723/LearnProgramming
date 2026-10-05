/**
 * Exercise01_Solution.java
 *
 * SOLUTION: Extended FizzBuzz
 *
 * KEY INSIGHT: Check for FizzBuzz (divisible by 15) FIRST.
 * If you check Fizz first, you'll enter the Fizz branch for numbers
 * like 15, 30, 45 — missing FizzBuzz.
 */
public class Exercise01_Solution {

    public static void main(String[] args) {
        // Counters for each category
        int fizzCount     = 0;
        int buzzCount     = 0;
        int fizzBuzzCount = 0;
        int numberCount   = 0;

        // Loop 1 to 100 inclusive
        for (int i = 1; i <= 100; i++) {
            // IMPORTANT: check FizzBuzz (divisible by BOTH 3 and 5) first!
            if (i % 15 == 0) {
                System.out.println("FizzBuzz");
                fizzBuzzCount++;
            } else if (i % 3 == 0) {
                System.out.println("Fizz");
                fizzCount++;
            } else if (i % 5 == 0) {
                System.out.println("Buzz");
                buzzCount++;
            } else {
                System.out.println(i);
                numberCount++;
            }
        }

        // Summary report
        System.out.println("\n--- Summary ---");
        System.out.println("Total Fizz:     " + fizzCount);      // 27
        System.out.println("Total Buzz:     " + buzzCount);      // 14
        System.out.println("Total FizzBuzz: " + fizzBuzzCount);  // 6
        System.out.println("Total Numbers:  " + numberCount);    // 53

        int total = fizzCount + buzzCount + fizzBuzzCount + numberCount;
        System.out.println("Grand Total:    " + total);           // 100
    }
}
