/**
 * Exercise01_FizzBuzz.java
 *
 * EXERCISE: Extended FizzBuzz
 *
 * Classic FizzBuzz extended with counting categories.
 *
 * RULES:
 * - Print "FizzBuzz" for numbers divisible by both 3 and 5
 * - Print "Fizz" for numbers divisible by only 3
 * - Print "Buzz" for numbers divisible by only 5
 * - Print the number itself for everything else
 *
 * BONUS: After printing all numbers 1-100, print a summary:
 *   Total Fizz:     X
 *   Total Buzz:     X
 *   Total FizzBuzz: X
 *   Total Numbers:  X (numbers printed as-is)
 *
 * EXPECTED OUTPUT (first few lines):
 *   1
 *   2
 *   Fizz
 *   4
 *   Buzz
 *   Fizz
 *   7
 *   8
 *   Fizz
 *   Buzz
 *   11
 *   Fizz
 *   13
 *   14
 *   FizzBuzz
 *   ...
 *
 * EXPECTED SUMMARY:
 *   Total Fizz:     27
 *   Total Buzz:     14
 *   Total FizzBuzz: 6
 *   Total Numbers:  53
 *   Grand Total:    100
 *
 * HINTS:
 * - Check FizzBuzz (divisible by 15) BEFORE Fizz and Buzz separately
 * - Divisible means the remainder is 0: n % 3 == 0
 * - Use int counters to track each category
 *
 * SKILLS PRACTICED:
 * - for loop
 * - if/else if/else
 * - Modulo operator (%)
 * - Counter variables
 */
public class Exercise01_FizzBuzz {

    public static void main(String[] args) {
        // TODO 1: Declare counter variables for each category
        //         int fizzCount    = 0;
        //         int buzzCount    = 0;
        //         int fizzBuzzCount = 0;
        //         int numberCount  = 0;

        // TODO 2: Write a for loop from 1 to 100 (inclusive)

            // TODO 3: Inside the loop, use if/else if/else to:
            //   - Check if divisible by BOTH 3 AND 5 first → print "FizzBuzz", increment fizzBuzzCount
            //   - Else check if divisible by 3 only        → print "Fizz", increment fizzCount
            //   - Else check if divisible by 5 only        → print "Buzz", increment buzzCount
            //   - Else                                      → print the number, increment numberCount

        // TODO 4: After the loop, print the summary:
        //   System.out.println("\n--- Summary ---");
        //   System.out.println("Total Fizz:     " + fizzCount);
        //   System.out.println("Total Buzz:     " + buzzCount);
        //   System.out.println("Total FizzBuzz: " + fizzBuzzCount);
        //   System.out.println("Total Numbers:  " + numberCount);
        //   int total = fizzCount + buzzCount + fizzBuzzCount + numberCount;
        //   System.out.println("Grand Total:    " + total);
    }
}
