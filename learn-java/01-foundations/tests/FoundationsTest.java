import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Your very first test file!
 *
 * Each @Test method verifies that your code works correctly.
 * Think of tests as "checking your homework automatically."
 *
 * BEGINNER NOTE: You don't need to understand every line yet.
 * Focus on: each test has a name that describes WHAT it checks.
 *
 * Read the AAA comments in each test to understand the structure:
 *   Arrange  → set up inputs
 *   Act      → do the operation
 *   Assert   → check the result
 */
@DisplayName("Foundations - Your First Tests")
public class FoundationsTest {

    // =========================================================================
    // INTEGER ARITHMETIC
    // =========================================================================

    /**
     * WHY we test this: Addition is the simplest operation, but typos happen.
     * Writing "a - b" instead of "a + b" is a real bug that tests catch instantly.
     */
    @Test
    @DisplayName("Integer addition: 3 + 4 should equal 7")
    void integerAddition() {
        // Arrange
        int a = 3;
        int b = 4;

        // Act
        int result = a + b;

        // Assert
        assertEquals(7, result,
            "3 + 4 should equal 7. Check that you used + not -");
    }

    /**
     * WHY we test this: Subtraction can produce negative numbers. New programmers
     * sometimes assume the result is always positive. This test confirms the sign.
     */
    @Test
    @DisplayName("Integer subtraction: 10 - 15 should equal -5")
    void integerSubtractionCanBeNegative() {
        // Arrange
        int a = 10;
        int b = 15;

        // Act
        int result = a - b;

        // Assert
        assertEquals(-5, result,
            "10 - 15 should be -5. Subtraction can produce negative results.");
    }

    /**
     * WHY we test this: Integer division TRUNCATES — it throws away the decimal part.
     * This is one of the most common beginner surprises in Java.
     * 7 / 2 in Java is 3, NOT 3.5!
     */
    @Test
    @DisplayName("Integer division truncates: 7 / 2 should equal 3, not 3.5")
    void integerDivisionTruncates() {
        // Arrange
        int numerator = 7;
        int denominator = 2;

        // Act
        int result = numerator / denominator;

        // Assert — this is the "beginner surprise" test
        assertEquals(3, result,
            "7 / 2 with int types gives 3, NOT 3.5! " +
            "Java drops the decimal part. Use double if you need fractions.");

        // Extra check: confirm it is NOT 4 (it does not round up)
        assertNotEquals(4, result,
            "Integer division truncates toward zero, it does not round.");
    }

    /**
     * WHY we test this: The modulo (%) operator gives the remainder after division.
     * It is widely used for checking even/odd, cycling through array indices, etc.
     * Many beginners confuse it with division.
     */
    @Test
    @DisplayName("Modulo: 10 % 3 should equal 1 (remainder after division)")
    void moduloGivesRemainder() {
        // Arrange & Act
        int remainder = 10 % 3;  // 10 = (3 * 3) + 1, so remainder is 1

        // Assert
        assertEquals(1, remainder,
            "10 % 3 = 1 because 10 / 3 = 3 with a remainder of 1.");
    }

    /**
     * WHY we test this: Even number detection using modulo is extremely common
     * in real programs (e.g., alternating row colors in a table). The pattern
     * "number % 2 == 0" is worth practicing until it is second nature.
     */
    @Test
    @DisplayName("Even number detection: 8 % 2 should equal 0")
    void evenNumberDetectionWithModulo() {
        // Arrange
        int evenNumber = 8;
        int oddNumber = 7;

        // Act & Assert
        assertEquals(0, evenNumber % 2,
            "Even numbers divided by 2 leave remainder 0.");
        assertEquals(1, oddNumber % 2,
            "Odd numbers divided by 2 leave remainder 1.");
    }

    // =========================================================================
    // TYPE CASTING
    // =========================================================================

    /**
     * WHY we test this: Casting from double to int is lossy — it drops the decimal,
     * it does NOT round. A common mistake is expecting rounding when there is none.
     * (int) 3.9 gives 3, not 4!
     */
    @Test
    @DisplayName("Casting double to int truncates (does not round): (int) 3.9 == 3")
    void castingDoubleToIntTruncates() {
        // Arrange
        double value = 3.9;

        // Act
        int truncated = (int) value;

        // Assert
        assertEquals(3, truncated,
            "(int) 3.9 should be 3. Casting truncates, it does NOT round. " +
            "Use Math.round() if you want rounding.");
    }

    /**
     * WHY we test this: Casting int to double is safe (no data loss), but it is
     * important for performing non-integer division. This is how you avoid the
     * integer-division-truncates problem shown above.
     */
    @Test
    @DisplayName("Casting int to double preserves value: (double) 7 / 2 == 3.5")
    void castingIntToDoubleEnablesDecimalDivision() {
        // Arrange
        int numerator = 7;
        int denominator = 2;

        // Act — casting ONE operand to double makes the whole expression double
        double result = (double) numerator / denominator;

        // Assert
        assertEquals(3.5, result, 0.0001,
            "(double) 7 / 2 should be 3.5. Casting to double enables decimal division.");
    }

    // =========================================================================
    // BOOLEAN LOGIC
    // =========================================================================

    /**
     * WHY we test this: Boolean expressions are the foundation of all if/while logic.
     * Testing them in isolation confirms your understanding of &&, ||, and !.
     */
    @Test
    @DisplayName("Boolean AND (&&): true && false should be false")
    void booleanAndOperator() {
        // Arrange
        boolean a = true;
        boolean b = false;

        // Act
        boolean result = a && b;

        // Assert
        assertFalse(result,
            "true && false is false. AND requires BOTH sides to be true.");
    }

    /**
     * WHY we test this: OR is easier to confuse with AND. This test reinforces
     * that OR only needs ONE side to be true.
     */
    @Test
    @DisplayName("Boolean OR (||): false || true should be true")
    void booleanOrOperator() {
        // Arrange
        boolean a = false;
        boolean b = true;

        // Act
        boolean result = a || b;

        // Assert
        assertTrue(result,
            "false || true is true. OR only needs ONE side to be true.");
    }

    /**
     * WHY we test this: The NOT operator (!) flips a boolean. Beginners sometimes
     * write confusing double-negatives. This confirms the basic behavior.
     */
    @Test
    @DisplayName("Boolean NOT (!): !true should be false, !false should be true")
    void booleanNotOperator() {
        assertTrue(!false, "!false should be true");
        assertFalse(!true, "!true should be false");
    }

    // =========================================================================
    // STRING OPERATIONS
    // =========================================================================

    /**
     * WHY we test this: String concatenation with + is fundamental in Java.
     * Testing it confirms you understand that "3" + "4" gives "34", not 7!
     * (String + String is concatenation, int + int is addition.)
     */
    @Test
    @DisplayName("String concatenation: \"Hello\" + \" \" + \"World\" == \"Hello World\"")
    void stringConcatenation() {
        // Arrange
        String greeting = "Hello";
        String space = " ";
        String name = "World";

        // Act
        String result = greeting + space + name;

        // Assert
        assertEquals("Hello World", result,
            "String concatenation with + joins the strings.");
    }

    /**
     * WHY we test this: Mixing String and int with + can surprise you.
     * "Score: " + 5 + 3 gives "Score: 53", NOT "Score: 8"!
     * Java evaluates left to right: first "Score: " + 5 = "Score: 5",
     * then "Score: 5" + 3 = "Score: 53".
     */
    @Test
    @DisplayName("String + int concatenates (does not add): \"Score: \" + 5 + 3 == \"Score: 53\"")
    void stringPlusIntConcatenates() {
        // Arrange & Act
        String result = "Score: " + 5 + 3;

        // Assert — this trips up many beginners!
        assertEquals("Score: 53", result,
            "\"Score: \" + 5 + 3 is \"Score: 53\", not \"Score: 8\". " +
            "Left-to-right: first String+int gives String, then String+int again.");
    }

    /**
     * WHY we test this: String.length() is used constantly. It is worth confirming
     * that it counts characters (including spaces), and that it is a method (length()),
     * not a field (unlike arrays, which use .length without parentheses).
     */
    @Test
    @DisplayName("String.length(): \"Hello\" has 5 characters")
    void stringLength() {
        // Arrange
        String word = "Hello";
        String withSpace = "Hello World";
        String empty = "";

        // Assert
        assertEquals(5, word.length(),
            "\"Hello\" has 5 characters.");
        assertEquals(11, withSpace.length(),
            "\"Hello World\" has 11 characters including the space.");
        assertEquals(0, empty.length(),
            "An empty string has length 0.");
    }

    /**
     * WHY we test this: Beginners often use == to compare strings and get
     * unexpected results. This test shows the correct way: .equals().
     * (== compares references; .equals() compares content.)
     */
    @Test
    @DisplayName("String comparison: use .equals(), not ==")
    void stringEqualityUsesEquals() {
        // Arrange
        String a = new String("hello");  // force a new object (not pool)
        String b = new String("hello");  // another new object with same content

        // Act & Assert
        // .equals() checks content — this is what you almost always want
        assertTrue(a.equals(b),
            "a.equals(b) is true because both contain \"hello\".");

        // DO NOT use == for string content comparison in real code.
        // It compares memory addresses, not content. String pooling can make
        // == work for literals, but it is unreliable and misleading.
        // Shown here only to illustrate the difference:
        // (new String("hello") == new String("hello")) is false
        assertFalse(a == b,
            "a == b is false for two separately created String objects. " +
            "Always use .equals() to compare string content!");
    }

    // =========================================================================
    // PARAMETERIZED TEST EXAMPLE
    // =========================================================================

    /**
     * WHY we test this: A parameterized test runs the same logic with many inputs
     * automatically. Here we verify that all these values are positive (> 0).
     * This is more efficient than writing five separate test methods.
     */
    @ParameterizedTest
    @DisplayName("Positive numbers are greater than zero")
    @ValueSource(ints = {1, 5, 42, 100, 999})
    void positiveNumbersAreGreaterThanZero(int number) {
        // Each value from @ValueSource is passed in as 'number'
        assertTrue(number > 0,
            number + " should be greater than zero.");
    }
}
