/**
 * Exercise03_Methods.java
 *
 * EXERCISE: String and Array Methods
 *
 * Implement four methods that are commonly asked in interviews
 * and are great practice for String and array manipulation.
 *
 * =====================================================================
 * METHOD 1: isPalindrome(String s)
 * =====================================================================
 * A palindrome reads the same forwards and backwards.
 * "racecar" → true, "level" → true, "hello" → false
 *
 * IGNORE case: "Racecar" → true, "Level" → true
 * IGNORE non-letter characters: "A man a plan a canal Panama" → true
 *
 * EXPECTED RESULTS:
 *   isPalindrome("racecar")                    → true
 *   isPalindrome("hello")                      → false
 *   isPalindrome("Level")                      → true
 *   isPalindrome("A man a plan a canal Panama") → true
 *   isPalindrome("Was it a car or a cat I saw") → true
 *
 * HINTS:
 * - First, strip non-letters and convert to lowercase
 * - Then compare character at index i with character at index (length-1-i)
 * - Only need to check up to the midpoint
 *
 * =====================================================================
 * METHOD 2: reverseString(String s)
 * =====================================================================
 * Return the string reversed.
 * "hello" → "olleh", "Java" → "avaJ"
 *
 * HINTS:
 * - Use a loop from the last character to the first
 * - Use StringBuilder
 *
 * =====================================================================
 * METHOD 3: countVowels(String s)
 * =====================================================================
 * Count the number of vowels (a, e, i, o, u) in the string.
 * Case-insensitive: "Hello" → 2, "rhythm" → 0, "Beautiful" → 5
 *
 * HINTS:
 * - Convert to lowercase first
 * - Loop through each character
 * - Check if it equals 'a', 'e', 'i', 'o', or 'u'
 *
 * =====================================================================
 * METHOD 4: findMax(int[] arr)
 * =====================================================================
 * Return the maximum value in the array.
 * {3, 1, 4, 1, 5, 9, 2, 6} → 9
 *
 * Handle the edge case: what if the array is empty?
 * Throw IllegalArgumentException("Array must not be empty")
 *
 * SKILLS PRACTICED:
 * - String methods: charAt(), toLowerCase(), length()
 * - StringBuilder
 * - for loops over arrays and strings
 * - Method design and edge cases
 */
public class Exercise03_Methods {

    public static void main(String[] args) {
        System.out.println("=== isPalindrome ===");
        System.out.println("racecar: " + isPalindrome("racecar"));                    // true
        System.out.println("hello: " + isPalindrome("hello"));                        // false
        System.out.println("Level: " + isPalindrome("Level"));                        // true
        System.out.println("A man a plan a canal Panama: " +
                isPalindrome("A man a plan a canal Panama"));                          // true
        System.out.println("Was it a car or a cat I saw: " +
                isPalindrome("Was it a car or a cat I saw"));                          // true

        System.out.println("\n=== reverseString ===");
        System.out.println("hello → " + reverseString("hello"));       // olleh
        System.out.println("Java  → " + reverseString("Java"));        // avaJ
        System.out.println("abcde → " + reverseString("abcde"));       // edcba

        System.out.println("\n=== countVowels ===");
        System.out.println("Hello: " + countVowels("Hello"));           // 2
        System.out.println("rhythm: " + countVowels("rhythm"));         // 0
        System.out.println("Beautiful: " + countVowels("Beautiful"));   // 5

        System.out.println("\n=== findMax ===");
        System.out.println("findMax({3,1,4,1,5,9,2,6}): " + findMax(new int[]{3, 1, 4, 1, 5, 9, 2, 6})); // 9
        System.out.println("findMax({-5,-3,-8,-1}):      " + findMax(new int[]{-5, -3, -8, -1}));         // -1
        System.out.println("findMax({42}):               " + findMax(new int[]{42}));                      // 42

        // Test edge case
        System.out.print("findMax({}): ");
        try {
            findMax(new int[]{});
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException caught: " + e.getMessage());
        }
    }

    /**
     * Returns true if s is a palindrome (case-insensitive, letters only).
     */
    static boolean isPalindrome(String s) {
        // TODO 1: Build a cleaned version of s:
        //         - convert to lowercase
        //         - keep only letters (use Character.isLetter(ch))
        //         Hint: loop through s.toLowerCase() and build a StringBuilder

        // TODO 2: Compare characters from both ends working toward the middle
        //         for (int i = 0; i < cleaned.length() / 2; i++)
        //             if cleaned.charAt(i) != cleaned.charAt(cleaned.length() - 1 - i) → return false

        // TODO 3: Return true if no mismatch was found

        return false; // replace this
    }

    /**
     * Returns the string s with its characters in reverse order.
     */
    static String reverseString(String s) {
        // TODO: Use a for loop starting from s.length()-1 down to 0
        //       Append each character to a StringBuilder
        //       Return sb.toString()

        return ""; // replace this
    }

    /**
     * Returns the count of vowels (a,e,i,o,u) in s (case-insensitive).
     */
    static int countVowels(String s) {
        // TODO: int count = 0;
        //       Loop through s.toLowerCase()
        //       If the character is a vowel, increment count
        //       Return count

        return 0; // replace this
    }

    /**
     * Returns the maximum value in arr.
     * Throws IllegalArgumentException if arr is empty.
     */
    static int findMax(int[] arr) {
        // TODO: Check if arr.length == 0 → throw new IllegalArgumentException("Array must not be empty")
        //       int max = arr[0];
        //       Loop through arr, update max if current element > max
        //       Return max

        return 0; // replace this
    }
}
