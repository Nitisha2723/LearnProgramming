import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CoreLanguageTest.java
 *
 * JUnit 5 tests covering all exercise solutions from Module 02.
 *
 * This file demonstrates BEST PRACTICE test writing:
 *   - Descriptive method names: methodName_scenario_expectedBehavior
 *   - One concept per test
 *   - Arrange-Act-Assert (AAA) pattern
 *   - @ParameterizedTest for testing multiple inputs cleanly
 *   - assertThrows for verifying exceptions
 *   - @DisplayName for human-readable test names
 *
 * To run these tests you need JUnit 5 on the classpath.
 * See tests/README.md for setup instructions.
 *
 * The methods under test are copied inline as static nested helpers
 * so this file compiles standalone without importing the exercise classes.
 */
@DisplayName("Core Language Concepts Tests")
public class CoreLanguageTest {

    // =========================================================================
    // SECTION 1: FizzBuzz Logic
    // =========================================================================

    @Test
    @DisplayName("fizzBuzzLabel returns Fizz for multiples of 3")
    void fizzBuzzLabel_multipleOf3_returnsFizz() {
        assertEquals("Fizz", fizzBuzzLabel(3));
        assertEquals("Fizz", fizzBuzzLabel(9));
        assertEquals("Fizz", fizzBuzzLabel(99));
    }

    @Test
    @DisplayName("fizzBuzzLabel returns Buzz for multiples of 5")
    void fizzBuzzLabel_multipleOf5_returnsBuzz() {
        assertEquals("Buzz", fizzBuzzLabel(5));
        assertEquals("Buzz", fizzBuzzLabel(25));
        assertEquals("Buzz", fizzBuzzLabel(50));
    }

    @Test
    @DisplayName("fizzBuzzLabel returns FizzBuzz for multiples of 15")
    void fizzBuzzLabel_multipleOf15_returnsFizzBuzz() {
        assertEquals("FizzBuzz", fizzBuzzLabel(15));
        assertEquals("FizzBuzz", fizzBuzzLabel(30));
        assertEquals("FizzBuzz", fizzBuzzLabel(45));
    }

    @Test
    @DisplayName("fizzBuzzLabel returns the number itself for non-multiples")
    void fizzBuzzLabel_notMultipleOf3Or5_returnsNumberAsString() {
        assertEquals("1",  fizzBuzzLabel(1));
        assertEquals("2",  fizzBuzzLabel(2));
        assertEquals("7",  fizzBuzzLabel(7));
        assertEquals("97", fizzBuzzLabel(97));
    }

    @ParameterizedTest
    @ValueSource(ints = {15, 30, 45, 60, 75, 90})
    @DisplayName("fizzBuzzLabel returns FizzBuzz for all multiples of 15 up to 90")
    void fizzBuzzLabel_allMultiplesOf15To90_returnFizzBuzz(int number) {
        assertEquals("FizzBuzz", fizzBuzzLabel(number));
    }

    // =========================================================================
    // SECTION 2: isPalindrome
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {"racecar", "level", "madam", "a", ""})
    @DisplayName("isPalindrome returns true for known palindromes")
    void isPalindrome_knownPalindromes_returnsTrue(String word) {
        assertTrue(isPalindrome(word));
    }

    @Test
    @DisplayName("isPalindrome is case-insensitive")
    void isPalindrome_mixedCase_returnsTrue() {
        assertTrue(isPalindrome("Level"));
        assertTrue(isPalindrome("RACECAR"));
        assertTrue(isPalindrome("Madam"));
    }

    @Test
    @DisplayName("isPalindrome ignores spaces and non-letter characters")
    void isPalindrome_withSpacesAndPunctuation_returnsTrue() {
        assertTrue(isPalindrome("A man a plan a canal Panama"));
        assertTrue(isPalindrome("Was it a car or a cat I saw"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "world", "java", "programming"})
    @DisplayName("isPalindrome returns false for non-palindromes")
    void isPalindrome_nonPalindromes_returnsFalse(String word) {
        assertFalse(isPalindrome(word));
    }

    // =========================================================================
    // SECTION 3: reverseString
    // =========================================================================

    @ParameterizedTest
    @CsvSource({
        "hello,  olleh",
        "Java,   avaJ",
        "abcde,  edcba",
        "a,      a",
        ",       ''"   // empty string reverses to empty string
    })
    @DisplayName("reverseString returns correctly reversed strings")
    void reverseString_variousInputs_returnsReversed(String input, String expected) {
        // CsvSource trims whitespace — this test covers the documented examples
        assertEquals(expected.trim(), reverseString(input == null ? "" : input.trim()));
    }

    @Test
    @DisplayName("reverseString of a single character returns the same character")
    void reverseString_singleCharacter_returnsSameCharacter() {
        assertEquals("x", reverseString("x"));
    }

    @Test
    @DisplayName("reverseString of an already-reversed string gives the original")
    void reverseString_applyTwice_returnsOriginal() {
        // Arrange
        String original = "programming";

        // Act
        String reversed = reverseString(original);
        String doubleReversed = reverseString(reversed);

        // Assert
        assertEquals(original, doubleReversed);
    }

    // =========================================================================
    // SECTION 4: countVowels
    // =========================================================================

    @Test
    @DisplayName("countVowels returns correct count for mixed case input")
    void countVowels_mixedCase_countsCaseInsensitively() {
        assertEquals(2, countVowels("Hello"));      // e, o
        assertEquals(5, countVowels("Beautiful"));  // e, a, u, i, u
    }

    @Test
    @DisplayName("countVowels returns 0 for strings with no vowels")
    void countVowels_noVowels_returnsZero() {
        assertEquals(0, countVowels("rhythm"));
        assertEquals(0, countVowels("gym"));
        assertEquals(0, countVowels("bcdfg"));
    }

    @Test
    @DisplayName("countVowels returns 0 for empty string")
    void countVowels_emptyString_returnsZero() {
        assertEquals(0, countVowels(""));
    }

    // =========================================================================
    // SECTION 5: findMax
    // =========================================================================

    @Test
    @DisplayName("findMax returns the maximum value in a normal array")
    void findMax_normalArray_returnsLargestValue() {
        // Arrange
        int[] numbers = {3, 1, 4, 1, 5, 9, 2, 6};

        // Act
        int result = findMax(numbers);

        // Assert
        assertEquals(9, result);
    }

    @Test
    @DisplayName("findMax works with all negative numbers")
    void findMax_allNegativeNumbers_returnsLeastNegative() {
        assertEquals(-1, findMax(new int[]{-5, -3, -8, -1}));
    }

    @Test
    @DisplayName("findMax with a single element returns that element")
    void findMax_singleElement_returnsThatElement() {
        assertEquals(42, findMax(new int[]{42}));
    }

    @Test
    @DisplayName("findMax throws IllegalArgumentException for empty array")
    void findMax_emptyArray_throwsIllegalArgumentException() {
        // assertThrows: first arg is the expected exception type,
        // second arg is a lambda that calls the code under test.
        // The test PASSES if and only if the lambda throws that exact exception.
        assertThrows(IllegalArgumentException.class, () -> {
            findMax(new int[]{});
        });
    }

    @Test
    @DisplayName("findMax exception message describes the problem")
    void findMax_emptyArray_exceptionMessageIsDescriptive() {
        // Capture the thrown exception to inspect its message
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> findMax(new int[]{})
        );
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isEmpty());
    }

    // =========================================================================
    // SECTION 6: rotateLeft (from Exercise 4)
    // =========================================================================

    @Test
    @DisplayName("rotateLeft by 2 positions moves first 2 elements to end")
    void rotateLeft_byTwo_movesFirstTwoToEnd() {
        // Arrange
        int[] input = {1, 2, 3, 4, 5};

        // Act
        int[] result = rotateLeft(input, 2);

        // Assert
        assertArrayEquals(new int[]{3, 4, 5, 1, 2}, result);
    }

    @Test
    @DisplayName("rotateLeft by 0 returns an equal array")
    void rotateLeft_byZero_returnsUnchangedArray() {
        int[] input = {1, 2, 3, 4, 5};
        assertArrayEquals(input, rotateLeft(input, 0));
    }

    @Test
    @DisplayName("rotateLeft by array length returns equal array (full cycle)")
    void rotateLeft_byFullLength_returnsUnchangedArray() {
        int[] input = {1, 2, 3, 4, 5};
        assertArrayEquals(input, rotateLeft(input, 5));
    }

    @Test
    @DisplayName("rotateLeft does not modify the original array")
    void rotateLeft_doesNotMutateOriginal() {
        int[] input = {1, 2, 3, 4, 5};
        int[] copy = Arrays.copyOf(input, input.length);
        rotateLeft(input, 2);
        assertArrayEquals(copy, input); // original unchanged
    }

    // =========================================================================
    // SECTION 7: twoSum (from Exercise 4)
    // =========================================================================

    @Test
    @DisplayName("twoSum finds indices of first pair summing to target")
    void twoSum_standardCase_returnsCorrectIndices() {
        assertArrayEquals(new int[]{0, 1}, twoSum(new int[]{2, 7, 11, 15}, 9));
    }

    @Test
    @DisplayName("twoSum works when answer is not at the beginning")
    void twoSum_targetPairInMiddle_returnsCorrectIndices() {
        assertArrayEquals(new int[]{1, 2}, twoSum(new int[]{3, 2, 4}, 6));
    }

    @Test
    @DisplayName("twoSum handles duplicate values")
    void twoSum_duplicateValues_returnsCorrectIndices() {
        assertArrayEquals(new int[]{0, 1}, twoSum(new int[]{3, 3}, 6));
    }

    // =========================================================================
    // INLINE IMPLEMENTATIONS
    // These mirror the solutions from exercises/solutions/ exactly.
    // In a real project, you'd import the actual classes instead.
    // =========================================================================

    private static String fizzBuzzLabel(int n) {
        if (n % 15 == 0) return "FizzBuzz";
        if (n % 3 == 0)  return "Fizz";
        if (n % 5 == 0)  return "Buzz";
        return String.valueOf(n);
    }

    private static boolean isPalindrome(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toLowerCase().toCharArray()) {
            if (Character.isLetter(ch)) sb.append(ch);
        }
        String cleaned = sb.toString();
        int left = 0, right = cleaned.length() - 1;
        while (left < right) {
            if (cleaned.charAt(left) != cleaned.charAt(right)) return false;
            left++; right--;
        }
        return true;
    }

    private static String reverseString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) sb.append(s.charAt(i));
        return sb.toString();
    }

    private static int countVowels(String s) {
        int count = 0;
        for (char ch : s.toLowerCase().toCharArray()) {
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') count++;
        }
        return count;
    }

    private static int findMax(int[] arr) {
        if (arr.length == 0) throw new IllegalArgumentException("Array must not be empty");
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) max = arr[i];
        }
        return max;
    }

    private static int[] rotateLeft(int[] arr, int k) {
        int n = arr.length;
        if (n == 0) return arr;
        k = k % n;
        if (k == 0) return Arrays.copyOf(arr, n);
        int[] result = new int[n];
        System.arraycopy(arr, k, result, 0, n - k);
        System.arraycopy(arr, 0, result, n - k, k);
        return result;
    }

    private static int[] twoSum(int[] arr, int target) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] + arr[j] == target) return new int[]{i, j};
            }
        }
        return new int[]{-1, -1};
    }
}
