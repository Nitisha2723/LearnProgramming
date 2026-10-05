// OperatorsDemo.java
// Demonstrates all major Java operators with explanations.
//
// An operator is a symbol that tells Java to perform a specific operation
// on one or more values (called operands).
//
// Categories covered:
//   1. Arithmetic operators:   +  -  *  /  %
//   2. Comparison operators:   ==  !=  <  >  <=  >=
//   3. Logical operators:      &&  ||  !
//   4. Assignment operators:   =  +=  -=  *=  /=  %=
//   5. Increment/Decrement:    ++  --
//   6. String concatenation:   + (special behavior with Strings)
//   7. Operator precedence

public class OperatorsDemo {

    public static void main(String[] args) {

        // =====================================================================
        // 1. ARITHMETIC OPERATORS
        // =====================================================================

        int a = 17;
        int b = 5;

        // Addition: +
        int sum = a + b;
        System.out.println("17 + 5 = " + sum);       // 22

        // Subtraction: -
        int difference = a - b;
        System.out.println("17 - 5 = " + difference); // 12

        // Multiplication: *
        int product = a * b;
        System.out.println("17 * 5 = " + product);    // 85

        // Division: /
        // IMPORTANT: When both operands are integers, Java performs INTEGER DIVISION.
        // Integer division truncates the decimal part — it does NOT round.
        int quotient = a / b;
        System.out.println("17 / 5 = " + quotient);   // 3 (not 3.4!)

        // To get a decimal result, at least one operand must be a double:
        double exactQuotient = (double) a / b;  // cast one to double
        System.out.println("17.0 / 5 = " + exactQuotient);  // 3.4

        // Modulo (remainder): %
        // Returns the remainder after integer division.
        // 17 / 5 = 3 remainder 2, so 17 % 5 = 2
        int remainder = a % b;
        System.out.println("17 % 5 = " + remainder);  // 2

        // Common uses of modulo:
        // Check if a number is even: n % 2 == 0
        // Check if a number is divisible by x: n % x == 0
        System.out.println("Is 17 even? " + (a % 2 == 0));  // false (it's odd)
        System.out.println("Is 20 divisible by 4? " + (20 % 4 == 0));  // true

        System.out.println();

        // =====================================================================
        // 2. COMPARISON OPERATORS (return boolean: true or false)
        // =====================================================================

        int x = 10;
        int y = 20;

        // Equal to: ==
        // NOTE: Two equals signs! One equals (=) is assignment, two equals (==) is comparison.
        // Confusing = with == is a very common beginner mistake.
        System.out.println("10 == 20: " + (x == y));  // false
        System.out.println("10 == 10: " + (x == x));  // true

        // Not equal to: !=
        System.out.println("10 != 20: " + (x != y));  // true
        System.out.println("10 != 10: " + (x != x));  // false

        // Less than: <
        System.out.println("10 < 20: " + (x < y));    // true
        System.out.println("20 < 10: " + (y < x));    // false

        // Greater than: >
        System.out.println("10 > 20: " + (x > y));    // false
        System.out.println("20 > 10: " + (y > x));    // true

        // Less than or equal to: <=
        System.out.println("10 <= 10: " + (x <= 10)); // true
        System.out.println("10 <= 9: " + (x <= 9));   // false

        // Greater than or equal to: >=
        System.out.println("20 >= 20: " + (y >= 20)); // true
        System.out.println("20 >= 21: " + (y >= 21)); // false

        System.out.println();

        // =====================================================================
        // 3. LOGICAL OPERATORS (combine boolean expressions)
        // =====================================================================

        boolean isAdult = true;
        boolean hasTicket = false;
        boolean isVIP = true;

        // AND: &&
        // Returns true ONLY if BOTH operands are true.
        // If the left side is false, Java doesn't even evaluate the right side
        // (this is called "short-circuit evaluation").
        System.out.println("isAdult && hasTicket: " + (isAdult && hasTicket)); // false
        System.out.println("isAdult && isVIP: " + (isAdult && isVIP));         // true

        // OR: ||
        // Returns true if AT LEAST ONE operand is true.
        // If the left side is true, Java doesn't evaluate the right side.
        System.out.println("isAdult || hasTicket: " + (isAdult || hasTicket)); // true
        System.out.println("hasTicket || isVIP: " + (hasTicket || isVIP));     // true
        System.out.println("hasTicket || !isAdult: " + (hasTicket || !isAdult)); // false

        // NOT: !
        // Inverts a boolean: true becomes false, false becomes true.
        System.out.println("!isAdult: " + !isAdult);    // false
        System.out.println("!hasTicket: " + !hasTicket); // true

        // Combining logical operators — parentheses make the logic clear
        boolean canEnterVIPArea = isAdult && (hasTicket || isVIP);
        System.out.println("Can enter VIP area: " + canEnterVIPArea); // true

        System.out.println();

        // =====================================================================
        // 4. ASSIGNMENT OPERATORS
        // =====================================================================

        // Simple assignment: =
        // Evaluates the right side and stores the result in the left side.
        int count = 0;
        System.out.println("Initial count: " + count); // 0

        // Add-and-assign: +=
        // count += 5 is EXACTLY the same as count = count + 5
        count += 5;
        System.out.println("After count += 5: " + count); // 5

        // Subtract-and-assign: -=
        count -= 2;
        System.out.println("After count -= 2: " + count); // 3

        // Multiply-and-assign: *=
        count *= 4;
        System.out.println("After count *= 4: " + count); // 12

        // Divide-and-assign: /=
        count /= 3;
        System.out.println("After count /= 3: " + count); // 4

        // Modulo-and-assign: %=
        count %= 3;
        System.out.println("After count %= 3: " + count); // 1

        System.out.println();

        // =====================================================================
        // 5. INCREMENT AND DECREMENT OPERATORS
        // =====================================================================

        // These are so common (especially in loops) that Java has shorthand.

        int n = 10;

        // Pre-increment: ++n
        // Increments FIRST, then uses the value.
        int preIncResult = ++n;   // n becomes 11 FIRST, then preIncResult = 11
        System.out.println("After ++n (pre-increment): n=" + n + ", result=" + preIncResult);
        // n=11, result=11

        // Post-increment: n++
        // Uses the value FIRST, then increments.
        int postIncResult = n++;  // postIncResult = 11 FIRST, then n becomes 12
        System.out.println("After n++ (post-increment): n=" + n + ", result=" + postIncResult);
        // n=12, result=11

        // Pre-decrement: --n
        int preDecResult = --n;   // n becomes 11 FIRST, then preDecResult = 11
        System.out.println("After --n (pre-decrement): n=" + n + ", result=" + preDecResult);

        // Post-decrement: n--
        int postDecResult = n--;  // postDecResult = 11 FIRST, then n becomes 10
        System.out.println("After n-- (post-decrement): n=" + n + ", result=" + postDecResult);

        // In a standalone statement (not inside an expression), pre and post are identical:
        n++;   // same effect as ++n when used alone
        n--;   // same effect as --n when used alone

        System.out.println();

        // =====================================================================
        // 6. STRING CONCATENATION WITH +
        // =====================================================================

        // The + operator has special behavior with Strings: it joins them together.
        String firstName = "James";
        String lastName = "Gosling";
        String fullName = firstName + " " + lastName;
        System.out.println("Full name: " + fullName);  // James Gosling

        // You can concatenate non-String values — Java converts them automatically:
        int yearBorn = 1956;
        String info = firstName + " was born in " + yearBorn;
        System.out.println(info);  // James was born in 1956

        // WATCH OUT: + is evaluated left to right.
        // When mixed with numbers, the ORDER matters!
        System.out.println("Result: " + 1 + 2);    // "Result: 12" (String concat!)
        System.out.println("Result: " + (1 + 2));  // "Result: 3"  (arithmetic first, due to parentheses)
        System.out.println(1 + 2 + " Result");     // "3 Result" (1+2=3 first, then concat)

        System.out.println();

        // =====================================================================
        // 7. OPERATOR PRECEDENCE
        // =====================================================================

        // Operators follow a priority order, just like in mathematics.
        // Highest priority first:
        //   1. Parentheses ()
        //   2. Unary operators: ++, --, !, unary -
        //   3. Multiplication, division, modulo: *, /, %
        //   4. Addition, subtraction: +, -
        //   5. Comparison: <, >, <=, >=
        //   6. Equality: ==, !=
        //   7. Logical AND: &&
        //   8. Logical OR: ||
        //   9. Assignment: =, +=, -=, etc.

        // Example: without parentheses
        int result1 = 2 + 3 * 4;   // 3*4=12 first (higher precedence), then 2+12=14
        System.out.println("2 + 3 * 4 = " + result1);   // 14

        // With parentheses to override precedence:
        int result2 = (2 + 3) * 4; // 2+3=5 first (parentheses force it), then 5*4=20
        System.out.println("(2 + 3) * 4 = " + result2); // 20

        // Logical operator precedence: && before ||
        // This can cause subtle bugs if you don't use parentheses!
        boolean r1 = false || true && false;
        // && binds tighter: this is  false || (true && false)  = false || false = false
        boolean r2 = (false || true) && false;
        // parentheses override: (false || true) && false = true && false = false
        boolean r3 = false || (true && true);
        // This is clear and explicit: false || true = true
        System.out.println("false || true && false = " + r1);        // false
        System.out.println("(false || true) && false = " + r2);      // false
        System.out.println("false || (true && true) = " + r3);       // true

        // RULE OF THUMB: When in doubt, use parentheses.
        // They make your intent explicit and prevent precedence bugs.
        // Don't rely on memorising the full precedence table —
        // just add parentheses around any complex expression.

        System.out.println();

        System.out.println("--- Operator Summary ---");
        System.out.println("Arithmetic: +, -, *, /, %");
        System.out.println("Comparison: ==, !=, <, >, <=, >=  (return boolean)");
        System.out.println("Logical:    &&, ||, !  (combine booleans)");
        System.out.println("Assignment: =, +=, -=, *=, /=, %=");
        System.out.println("Increment:  ++, --");
        System.out.println("Precedence: () > */ % > +- > comparisons > && > ||");
    }
}
