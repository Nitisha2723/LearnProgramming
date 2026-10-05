/**
 * PROBLEM: Reverse String
 * Write a function that reverses a string in-place.
 * The input is given as an array of characters. Do not allocate extra space.
 *
 * Examples:
 *   ['h','e','l','l','o'] → ['o','l','l','e','h']
 *   ['H','a','n','n','a','h'] → ['h','a','n','n','a','H']
 *
 * APPROACH: Two Pointers
 *   - Place one pointer at the start, one at the end.
 *   - Swap the characters at both pointers.
 *   - Move pointers toward the center: left++, right--
 *   - Stop when left >= right (pointers have crossed or met).
 *
 * WHY THIS WORKS:
 *   Each swap puts characters in their final reversed position.
 *   For an array of n elements, we need n/2 swaps.
 *   No extra memory needed — all swaps are in-place.
 *
 * TIME:  O(n) — we process each element once (n/2 swaps)
 * SPACE: O(1) — only two pointer variables; no auxiliary array
 *
 * COMPARISON with String approach (not in-place, not allowed):
 *   new StringBuilder(s).reverse().toString() — O(n) space, not in-place
 */
public class ReverseString {

    /**
     * Reverses the character array in-place.
     *
     * @param s the char array to reverse
     */
    public void reverseString(char[] s) {
        if (s == null || s.length <= 1) return; // Nothing to reverse

        int left = 0;
        int right = s.length - 1;

        while (left < right) {
            // Swap characters at left and right positions
            char temp = s[left];
            s[left] = s[right];
            s[right] = temp;

            // Move pointers toward center
            left++;
            right--;
        }
    }

    /**
     * Alternative: using XOR swap (no temp variable).
     * XOR swap is a micro-optimization — the temp variable approach is clearer.
     * Included here to illustrate the technique.
     *
     * NOTE: XOR swap fails when left == right (same element), so guard for it.
     *
     * @param s the char array to reverse
     */
    public void reverseStringXOR(char[] s) {
        if (s == null || s.length <= 1) return;

        int left = 0;
        int right = s.length - 1;

        while (left < right) {
            // XOR swap: a ^= b; b ^= a; a ^= b;
            // This works because: (a ^ b) ^ b = a, and (a ^ b) ^ a = b
            s[left]  ^= s[right];
            s[right] ^= s[left];
            s[left]  ^= s[right];

            left++;
            right--;
        }
    }

    /**
     * Bonus: Reverse a String (not char[]) — common variant
     * Returns a reversed copy of the string.
     * TIME: O(n), SPACE: O(n)
     */
    public String reverseStringObject(String s) {
        if (s == null || s.length() <= 1) return s;

        char[] chars = s.toCharArray();
        int left = 0;
        int right = chars.length - 1;

        while (left < right) {
            char temp = chars[left];
            chars[left] = chars[right];
            chars[right] = temp;
            left++;
            right--;
        }

        return new String(chars);
    }

    // -----------------------------------------------------------------------
    // Helper: Convert char array to string for display
    // -----------------------------------------------------------------------
    private String charArrToString(char[] arr) {
        return new String(arr);
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        ReverseString solution = new ReverseString();

        System.out.println("=== Reverse String ===");
        System.out.println();

        // Test 1: "hello" → "olleh"
        char[] s1 = {'h', 'e', 'l', 'l', 'o'};
        System.out.println("Input:    " + solution.charArrToString(s1));
        solution.reverseString(s1);
        System.out.println("Reversed: " + solution.charArrToString(s1));
        System.out.println("Expected: olleh");
        System.out.println();

        // Test 2: "Hannah" (palindrome — should be same when reversed... but case matters)
        char[] s2 = {'H', 'a', 'n', 'n', 'a', 'h'};
        System.out.println("Input:    " + solution.charArrToString(s2));
        solution.reverseString(s2);
        System.out.println("Reversed: " + solution.charArrToString(s2));
        System.out.println("Expected: hannaH");
        System.out.println();

        // Test 3: Single character
        char[] s3 = {'a'};
        System.out.println("Input:    " + solution.charArrToString(s3));
        solution.reverseString(s3);
        System.out.println("Reversed: " + solution.charArrToString(s3));
        System.out.println("Expected: a (unchanged)");
        System.out.println();

        // Test 4: Two characters
        char[] s4 = {'a', 'b'};
        System.out.println("Input:    " + solution.charArrToString(s4));
        solution.reverseString(s4);
        System.out.println("Reversed: " + solution.charArrToString(s4));
        System.out.println("Expected: ba");
        System.out.println();

        // Test 5: Even length
        char[] s5 = {'1', '2', '3', '4'};
        System.out.println("Input:    " + solution.charArrToString(s5));
        solution.reverseString(s5);
        System.out.println("Reversed: " + solution.charArrToString(s5));
        System.out.println("Expected: 4321");
        System.out.println();

        // Test 6: String version
        System.out.println("String version:");
        System.out.println("  reverseStringObject(\"abcde\") = " + solution.reverseStringObject("abcde"));
        System.out.println("  reverseStringObject(\"a\")     = " + solution.reverseStringObject("a"));
        System.out.println("  reverseStringObject(\"\")      = " + solution.reverseStringObject(""));

        System.out.println();
        System.out.println("=== Trace walkthrough: \"hello\" ===");
        System.out.println("left=0, right=4: swap 'h' and 'o' → 'o','e','l','l','h'");
        System.out.println("left=1, right=3: swap 'e' and 'l' → 'o','l','l','e','h'");
        System.out.println("left=2, right=2: left >= right, stop");
        System.out.println("Result: olleh ✓");
    }
}
