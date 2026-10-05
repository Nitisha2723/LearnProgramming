/**
 * MethodsDemo.java
 *
 * Demonstrates method definition and usage patterns in Java:
 * - void methods (do something, return nothing)
 * - methods that return values
 * - method overloading (same name, different parameters)
 * - recursive methods
 * - pass-by-value (critical concept!)
 *
 * Read theory/02-methods.md before studying this file.
 *
 * How to run:
 *   javac MethodsDemo.java
 *   java MethodsDemo
 */
public class MethodsDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  METHODS DEMO");
        System.out.println("=================================================\n");

        demoVoidMethods();
        demoReturnValues();
        demoMethodOverloading();
        demoRecursion();
        demoPassByValue();
    }

    // =========================================================================
    // VOID METHODS
    // =========================================================================

    /**
     * A void method performs an action and returns nothing.
     * Notice: no return statement needed (though you can use bare "return;" to exit early).
     */
    static void demoVoidMethods() {
        System.out.println("--- Void Methods ---");

        // Call a simple void method
        printBanner("Java Methods", 40);
        printBanner("Short", 20);

        // Call with early return
        printIfInRange(50, 0, 100);   // prints
        printIfInRange(150, 0, 100);  // does nothing (out of range)
        printIfInRange(-5, 0, 100);   // does nothing (out of range)

        System.out.println();
    }

    /**
     * Prints a centered banner between dashes.
     *
     * @param title  the text to display
     * @param width  total width of the banner line
     */
    static void printBanner(String title, int width) {
        // Build the dashes
        String dashes = "-".repeat(width);
        System.out.println(dashes);

        // Center the title
        int padding = (width - title.length()) / 2;
        System.out.println(" ".repeat(padding) + title);
        System.out.println(dashes);
    }

    /**
     * Prints the value only if it falls within [min, max].
     * Demonstrates using return to exit a void method early.
     */
    static void printIfInRange(int value, int min, int max) {
        if (value < min || value > max) {
            return; // exit early — nothing to print
        }
        System.out.println("  Value " + value + " is within range [" + min + ", " + max + "]");
    }

    // =========================================================================
    // METHODS THAT RETURN VALUES
    // =========================================================================

    static void demoReturnValues() {
        System.out.println("--- Methods That Return Values ---");

        // The returned value can be stored, used directly, or passed to another method
        double celsius = 100.0;
        double fahrenheit = celsiusToFahrenheit(celsius);
        System.out.printf("%.1f°C = %.1f°F%n", celsius, fahrenheit);

        // Used directly in an expression
        System.out.printf("Body temperature: %.1f°F%n", celsiusToFahrenheit(37.0));

        // Compute and classify
        int n = 17;
        System.out.println(n + " is " + (isPrime(n) ? "prime" : "not prime"));
        System.out.println(28 + " is " + (isPrime(28) ? "prime" : "not prime"));

        // Using a method that returns a String
        System.out.println(describeNumber(0));
        System.out.println(describeNumber(7));
        System.out.println(describeNumber(-3));

        System.out.println();
    }

    /**
     * Converts Celsius to Fahrenheit.
     *
     * @param celsius temperature in Celsius
     * @return temperature in Fahrenheit
     */
    static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    /**
     * Checks whether an integer is prime.
     *
     * @param n the number to check (must be >= 2 to be prime)
     * @return true if n is prime, false otherwise
     */
    static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;

        // Only check odd divisors up to sqrt(n)
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    /**
     * Returns a description of a number's sign.
     * Demonstrates multiple return points in a method.
     */
    static String describeNumber(int n) {
        if (n > 0) return n + " is positive";
        if (n < 0) return n + " is negative";
        return n + " is zero";
    }

    // =========================================================================
    // METHOD OVERLOADING
    // Java allows multiple methods with the same name if the parameter list differs.
    // The compiler picks the right version based on the arguments you pass.
    // =========================================================================

    static void demoMethodOverloading() {
        System.out.println("--- Method Overloading ---");
        System.out.println("All three calls use the name 'calculateArea':");

        // Three versions of calculateArea for different shapes
        double circleArea    = calculateArea(5.0);              // radius
        double rectangleArea = calculateArea(4.0, 6.0);         // width, height
        double triangleArea  = calculateArea(3.0, 8.0, true);   // base, height, isTriangle flag

        System.out.printf("  Circle (r=5):         %.2f%n", circleArea);
        System.out.printf("  Rectangle (4×6):      %.2f%n", rectangleArea);
        System.out.printf("  Triangle (b=3, h=8):  %.2f%n", triangleArea);

        // The compiler distinguishes them by argument types and count:
        // calculateArea(5.0)           → 1 double  → circle
        // calculateArea(4.0, 6.0)      → 2 doubles → rectangle
        // calculateArea(3.0, 8.0, true)→ 2 doubles + boolean → triangle

        System.out.println();
    }

    /**
     * Calculates the area of a circle.
     *
     * @param radius the circle's radius
     * @return area = π × r²
     */
    static double calculateArea(double radius) {
        return Math.PI * radius * radius;
    }

    /**
     * Calculates the area of a rectangle.
     *
     * @param width  the rectangle's width
     * @param height the rectangle's height
     * @return area = width × height
     */
    static double calculateArea(double width, double height) {
        return width * height;
    }

    /**
     * Calculates the area of a triangle.
     * The boolean parameter distinguishes this from the rectangle version.
     *
     * @param base       the triangle's base
     * @param height     the triangle's height
     * @param isTriangle must be true (the flag just distinguishes the signature)
     * @return area = ½ × base × height
     */
    static double calculateArea(double base, double height, boolean isTriangle) {
        return 0.5 * base * height;
    }

    // =========================================================================
    // RECURSION
    // A method that calls itself. Every recursive method needs:
    //   1. A base case  — where it stops
    //   2. A recursive case — where it calls itself with a smaller problem
    // =========================================================================

    static void demoRecursion() {
        System.out.println("--- Recursion ---");

        // Factorial: n! = n × (n-1)!
        System.out.println("Factorials:");
        for (int i = 0; i <= 10; i++) {
            System.out.printf("  %2d! = %,d%n", i, factorial(i));
        }

        // Fibonacci sequence
        System.out.println("\nFibonacci sequence (first 10):");
        for (int i = 0; i < 10; i++) {
            System.out.print(fibonacci(i) + " ");
        }
        System.out.println();

        System.out.println();
    }

    /**
     * Computes n! (n factorial) recursively.
     *
     * Base case:    factorial(0) = 1, factorial(1) = 1
     * Recursive:    factorial(n) = n × factorial(n-1)
     *
     * Trace for factorial(4):
     *   factorial(4) = 4 × factorial(3)
     *                       = 3 × factorial(2)
     *                            = 2 × factorial(1)
     *                                      = 1  ← base case
     *                            = 2 × 1 = 2
     *                       = 3 × 2 = 6
     *   factorial(4) = 4 × 6 = 24
     *
     * @param n non-negative integer
     * @return n!
     */
    static long factorial(int n) {
        // BASE CASE: stop recursion here
        if (n <= 1) {
            return 1;
        }
        // RECURSIVE CASE: smaller problem
        return n * factorial(n - 1);
    }

    /**
     * Returns the nth Fibonacci number (0-indexed).
     * F(0)=0, F(1)=1, F(n) = F(n-1) + F(n-2)
     *
     * Note: this naive recursive implementation is exponential time —
     * fine for demonstration, but iterative is better for large n.
     */
    static int fibonacci(int n) {
        if (n <= 0) return 0;        // base case 1
        if (n == 1) return 1;        // base case 2
        return fibonacci(n - 1) + fibonacci(n - 2); // recursive case
    }

    // =========================================================================
    // PASS BY VALUE
    // THIS IS ONE OF THE MOST IMPORTANT JAVA CONCEPTS.
    // Java ALWAYS passes a copy of the value — never the original variable.
    // =========================================================================

    static void demoPassByValue() {
        System.out.println("--- Pass By Value ---");

        // EXPERIMENT 1: Passing a primitive
        System.out.println("Experiment 1: passing a primitive int");
        int original = 10;
        System.out.println("  Before call: original = " + original);
        tryToModifyPrimitive(original);
        System.out.println("  After call:  original = " + original); // Still 10!
        System.out.println("  → The method got a COPY. The original was not changed.");

        // EXPERIMENT 2: Passing an array (object reference)
        System.out.println("\nExperiment 2: passing an array");
        int[] numbers = {1, 2, 3, 4, 5};
        System.out.println("  Before call: numbers[0] = " + numbers[0]);
        modifyArrayContents(numbers);
        System.out.println("  After call:  numbers[0] = " + numbers[0]); // Changed to 99!
        System.out.println("  → The method got a COPY OF THE REFERENCE.");
        System.out.println("    It followed the reference to the array and modified the contents.");

        // EXPERIMENT 3: Reassigning the reference inside a method doesn't affect caller
        System.out.println("\nExperiment 3: reassigning the array reference inside a method");
        int[] data = {10, 20, 30};
        System.out.println("  Before call: data[0] = " + data[0]);
        tryToReassignArray(data);
        System.out.println("  After call:  data[0] = " + data[0]); // Still 10!
        System.out.println("  → Reassigning the local reference doesn't affect the caller's variable.");

        System.out.println();
    }

    /**
     * Attempts to modify a primitive — but only has a copy.
     */
    static void tryToModifyPrimitive(int x) {
        x = x * 100; // modifies the copy
        System.out.println("  Inside method: x = " + x); // 1000
    }

    /**
     * Modifies the CONTENTS of the array through the reference copy.
     * This DOES affect the caller's array.
     */
    static void modifyArrayContents(int[] arr) {
        arr[0] = 99; // follows the reference and changes the element
    }

    /**
     * Tries to make the local reference point to a different array.
     * This does NOT affect the caller's variable.
     */
    static void tryToReassignArray(int[] arr) {
        arr = new int[]{100, 200, 300}; // only changes the local copy of the reference
        System.out.println("  Inside method: arr[0] = " + arr[0]); // 100
    }
}
