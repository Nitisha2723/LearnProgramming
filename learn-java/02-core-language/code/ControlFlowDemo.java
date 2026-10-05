/**
 * ControlFlowDemo.java
 *
 * Demonstrates ALL control flow constructs in Java using real-world scenarios.
 * Read theory/01-control-flow.md before studying this file.
 *
 * How to run:
 *   javac ControlFlowDemo.java
 *   java ControlFlowDemo
 */
public class ControlFlowDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  CONTROL FLOW DEMO");
        System.out.println("=================================================\n");

        demoIfElse();
        demoTernary();
        demoSwitch();
        demoWhileLoop();
        demoDoWhile();
        demoForLoop();
        demoForEachLoop();
        demoBreakAndContinue();
        demoNestedLoops();
    }

    // -------------------------------------------------------------------------
    // SECTION 1: if / else if / else
    // Scenario: Grade classifier
    // Real-world analogy: a traffic light — you pick exactly one action
    // -------------------------------------------------------------------------
    static void demoIfElse() {
        System.out.println("--- if/else: Grade Classifier ---");

        int[] testScores = {95, 83, 71, 65, 45};

        for (int score : testScores) {
            String grade;
            String feedback;

            // Each condition is mutually exclusive — order matters!
            // We test highest first. Once a branch is taken, the rest are skipped.
            if (score >= 90) {
                grade = "A";
                feedback = "Excellent work!";
            } else if (score >= 80) {
                grade = "B";
                feedback = "Good job, keep it up.";
            } else if (score >= 70) {
                grade = "C";
                feedback = "Passing, but room to improve.";
            } else if (score >= 60) {
                grade = "D";
                feedback = "Barely passing — review the material.";
            } else {
                grade = "F";
                feedback = "Did not pass. Please seek help.";
            }

            System.out.printf("Score: %3d | Grade: %s | %s%n", score, grade, feedback);
        }

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 2: Ternary operator
    // When to use: simple binary choices that fit on one line
    // When NOT to use: complex logic, nested ternaries
    // -------------------------------------------------------------------------
    static void demoTernary() {
        System.out.println("--- Ternary Operator ---");

        // GOOD USE: simple, readable
        int temperature = 22;
        String weatherAdvice = temperature > 20 ? "Wear a t-shirt" : "Bring a jacket";
        System.out.println("Temperature: " + temperature + "°C → " + weatherAdvice);

        // GOOD USE: categorizing a value
        int year = 2024;
        String leapYearStatus = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
                ? year + " is a leap year"
                : year + " is not a leap year";
        System.out.println(leapYearStatus);

        // BAD USE (shown here so you recognize it): nested ternary is unreadable
        // Don't write code like this:
        // String grade = score >= 90 ? "A" : score >= 80 ? "B" : score >= 70 ? "C" : "F";
        // Use if/else instead!

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 3: switch statement and modern switch expressions
    // Scenario: Day of the week
    // -------------------------------------------------------------------------
    static void demoSwitch() {
        System.out.println("--- switch Statement ---");

        // Traditional switch (works in all Java versions)
        System.out.println("Traditional switch:");
        for (int day = 1; day <= 7; day++) {
            String dayType;
            String dayName;

            switch (day) {
                case 1:
                    dayName = "Monday";
                    dayType = "Weekday";
                    break;
                case 2:
                    dayName = "Tuesday";
                    dayType = "Weekday";
                    break;
                case 3:
                    dayName = "Wednesday";
                    dayType = "Weekday";
                    break;
                case 4:
                    dayName = "Thursday";
                    dayType = "Weekday";
                    break;
                case 5:
                    dayName = "Friday";
                    dayType = "Weekday";
                    break;
                case 6:
                    dayName = "Saturday";
                    dayType = "Weekend";
                    break;
                case 7:
                    dayName = "Sunday";
                    dayType = "Weekend";
                    break;
                default:
                    dayName = "Unknown";
                    dayType = "Unknown";
            }

            System.out.printf("  Day %d: %-10s (%s)%n", day, dayName, dayType);
        }

        // Modern switch expression (Java 14+) — no break needed, no fall-through
        System.out.println("\nModern switch expression (Java 14+):");
        for (int day = 1; day <= 7; day++) {
            String dayName = switch (day) {
                case 1 -> "Monday";
                case 2 -> "Tuesday";
                case 3 -> "Wednesday";
                case 4 -> "Thursday";
                case 5 -> "Friday";
                case 6 -> "Saturday";
                case 7 -> "Sunday";
                default -> "Unknown";
            };

            // Multiple cases with the same result
            String dayType = switch (day) {
                case 1, 2, 3, 4, 5 -> "Weekday";
                case 6, 7           -> "Weekend";
                default             -> "Unknown";
            };

            System.out.printf("  Day %d: %-10s (%s)%n", day, dayName, dayType);
        }

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 4: while loop
    // Scenario: Number guessing game logic (simulated)
    // Real-world analogy: cashier scanning items — unknown count
    // -------------------------------------------------------------------------
    static void demoWhileLoop() {
        System.out.println("--- while Loop: Guessing Game Logic ---");

        int secretNumber = 42;
        int guess = 1; // simulate starting from 1 and incrementing
        int attempts = 0;
        int maxAttempts = 100;

        // We don't know how many iterations we'll need — use while
        while (guess != secretNumber && attempts < maxAttempts) {
            attempts++;
            if (guess < secretNumber) {
                guess += (secretNumber - guess) / 2 + 1; // binary search approach
            } else {
                guess -= (guess - secretNumber) / 2 + 1;
            }
        }

        if (guess == secretNumber) {
            System.out.println("Found " + secretNumber + " in " + attempts + " attempts!");
        } else {
            System.out.println("Could not find the number in " + maxAttempts + " attempts.");
        }

        // Another while example: countdown
        System.out.print("Countdown: ");
        int count = 5;
        while (count > 0) {
            System.out.print(count + " ");
            count--; // CRITICAL: must advance toward the termination condition
        }
        System.out.println("Blast off!");

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 5: do-while loop
    // Scenario: Input validation (simulated with an array of inputs)
    // Key property: body always executes at least once
    // -------------------------------------------------------------------------
    static void demoDoWhile() {
        System.out.println("--- do-while Loop: Input Validation ---");

        // Simulate a sequence of user inputs
        int[] simulatedInputs = {-5, 0, 150, 85}; // bad, bad, bad, valid
        int inputIndex = 0;
        int validScore = 0;

        // do-while ensures we always ask for input at least once
        do {
            int input = simulatedInputs[inputIndex++];
            System.out.println("  User entered: " + input);

            if (input >= 0 && input <= 100) {
                validScore = input;
                System.out.println("  Valid score accepted: " + validScore);
                break; // valid input received, exit loop
            } else {
                System.out.println("  Invalid! Score must be 0-100. Try again.");
            }
        } while (inputIndex < simulatedInputs.length);

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 6: for loop
    // Scenario: Multiplication table
    // Real-world analogy: assembly line with known production count
    // -------------------------------------------------------------------------
    static void demoForLoop() {
        System.out.println("--- for Loop: Multiplication Table ---");

        int multiplier = 7;

        // Anatomy: initialization ; condition ; update
        for (int i = 1; i <= 10; i++) {
            //     ^^^    ^^^^^^  ^^^
            //     init   check   step
            System.out.printf("%d × %2d = %3d%n", multiplier, i, multiplier * i);
        }

        // Counting backward
        System.out.print("\nCountdown: ");
        for (int i = 10; i >= 1; i--) {
            System.out.print(i + " ");
        }
        System.out.println("Ignition!");

        // Stepping by 2
        System.out.print("\nEven numbers 0-10: ");
        for (int i = 0; i <= 10; i += 2) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 7: for-each loop
    // Cleaner syntax when you only need values, not indices
    // -------------------------------------------------------------------------
    static void demoForEachLoop() {
        System.out.println("--- for-each Loop ---");

        String[] fruits = {"Apple", "Banana", "Cherry", "Date", "Elderberry"};

        // for-each: clean, no index needed
        System.out.println("Fruits (for-each):");
        for (String fruit : fruits) {
            System.out.println("  • " + fruit);
        }

        // Compare with traditional for when you need the index:
        System.out.println("\nFruits with position (traditional for, needs index):");
        for (int i = 0; i < fruits.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, fruits[i]);
        }

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 8: break and continue
    // Use sparingly — prefer clear loop conditions
    // -------------------------------------------------------------------------
    static void demoBreakAndContinue() {
        System.out.println("--- break and continue ---");

        // break: stop the loop early when a condition is met
        System.out.print("break example (stop at first negative): ");
        int[] mixedNumbers = {5, 3, 8, -2, 7, -4, 1};
        for (int num : mixedNumbers) {
            if (num < 0) {
                System.out.println("Found negative " + num + ", stopping.");
                break;
            }
            System.out.print(num + " ");
        }

        // continue: skip one iteration
        System.out.print("continue example (skip multiples of 3): ");
        for (int i = 1; i <= 15; i++) {
            if (i % 3 == 0) {
                continue; // skip this iteration
            }
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 9: nested for loops
    // Scenario: Star triangle pattern and multiplication grid
    // -------------------------------------------------------------------------
    static void demoNestedLoops() {
        System.out.println("--- Nested Loops ---");

        // Triangle pattern
        System.out.println("Star triangle:");
        int rows = 5;
        for (int row = 1; row <= rows; row++) {  // outer loop: controls rows
            for (int star = 1; star <= row; star++) { // inner loop: controls stars per row
                System.out.print("*");
            }
            System.out.println(); // move to next line after each row
        }

        // Multiplication table grid
        System.out.println("\nMultiplication table (1-5):");
        System.out.print("   "); // spacing for header
        for (int col = 1; col <= 5; col++) {
            System.out.printf("%4d", col);
        }
        System.out.println("\n   " + "----".repeat(5));

        for (int row = 1; row <= 5; row++) {
            System.out.printf("%3d|", row);
            for (int col = 1; col <= 5; col++) {
                System.out.printf("%4d", row * col);
            }
            System.out.println();
        }

        System.out.println();
    }
}
