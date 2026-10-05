// SOLUTION: Exercise 3 — Calculator
// ===================================
// Study this only AFTER genuinely attempting Exercise03_Calculator.java yourself.

public class Exercise03_Solution {

    public static void main(String[] args) {
        // Solution to TODO 1: Declare two numbers
        int num1 = 17;
        int num2 = 5;

        System.out.println("--- Calculator: " + num1 + " and " + num2 + " ---");

        // Solution to TODO 2: Sum
        // The result is stored in a variable for clarity, then printed.
        int sum = num1 + num2;
        System.out.println("Sum:               " + num1 + " + " + num2 + " = " + sum);

        // Solution to TODO 3: Difference
        int difference = num1 - num2;
        System.out.println("Difference:        " + num1 + " - " + num2 + " = " + difference);

        // Solution to TODO 4: Product
        int product = num1 * num2;
        System.out.println("Product:           " + num1 + " * " + num2 + " = " + product);

        // Solution to TODO 5: Integer quotient
        // IMPORTANT: When both operands are int, Java uses integer division.
        // 17 / 5 = 3 (the .4 decimal part is discarded, NOT rounded)
        int intQuotient = num1 / num2;
        System.out.println("Integer quotient:  " + num1 + " / " + num2 + " = " + intQuotient);

        // Solution to TODO 6: Decimal quotient
        // To get 3.4 instead of 3, we must convert one operand to double.
        // (double) num1 is a "cast" — it temporarily treats num1 as a double.
        // Once one operand is a double, Java performs floating-point division.
        double decimalQuotient = (double) num1 / num2;
        System.out.println("Decimal quotient:  " + num1 + " / " + num2 + " = " + decimalQuotient);

        // Alternative: you could also write:
        //   double decimalQuotient = num1 / (double) num2;  // cast the other one
        //   double decimalQuotient = (double) num1 / (double) num2;  // cast both

        // Solution to TODO 7: Remainder (modulo)
        // 17 / 5 = 3 remainder 2, so 17 % 5 = 2
        int remainder = num1 % num2;
        System.out.println("Remainder:         " + num1 + " % " + num2 + " = " + remainder);

        // Solution to TODO 8: Evenly divisible check
        // num1 is evenly divisible by num2 if the remainder is 0.
        boolean isEvenlyDivisible = (num1 % num2 == 0);
        System.out.println("Evenly divisible?  " + isEvenlyDivisible);

        // Let's also verify the relationship: dividend = divisor * quotient + remainder
        // This is always true for integer division.
        System.out.println();
        System.out.println("Verification: " + num2 + " * " + intQuotient + " + " + remainder +
                           " = " + (num2 * intQuotient + remainder) + " (should equal " + num1 + ")");
    }
}
