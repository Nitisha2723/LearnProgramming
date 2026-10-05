/**
 * PROBLEM: Palindrome Number
 * Given an integer x, return true if x is a palindrome integer.
 * A palindrome reads the same forwards and backwards.
 *
 * Examples:
 *   121   → true   (reads "121" both ways)
 *   -121  → false  (reads "-121" forward, "121-" backward — negative isn't palindrome)
 *   10    → false  (reads "10" forward, "01" backward)
 *   0     → true   (single digit, trivially palindrome)
 *
 * CONSTRAINT: Do not convert the integer to a string.
 *
 * KEY INSIGHT: Immediately reject negative numbers and numbers ending in 0
 * (except 0 itself), since they can't be palindromes.
 *
 * APPROACH: Reverse the second half of the number
 *   Instead of reversing the entire number (risks integer overflow),
 *   reverse only the second half and compare with the first half.
 *
 *   For 1221:
 *     Extract digits from the end: 1, 2 → build reversed half: 12
 *     Remaining first half: 12
 *     Compare: 12 == 12 → palindrome ✓
 *
 *   For 12321 (odd digits):
 *     Extract: 1, 2, 3 → reversed half: 321 (but we stop when reversedHalf > x)
 *     Actually: 1 → 12 → 123 (123 > 12, so stop after 12)
 *     Wait — let's re-trace:
 *     x=12321, rev=0: pop 1, x=1232, rev=1
 *     x=1232,  rev=1: pop 2, x=123,  rev=12
 *     x=123,   rev=12: rev > x? 12 < 123, so continue... wait, we stop when rev >= x
 *     x=123,   rev=12: pop 3, x=12,   rev=123
 *     Now x=12, rev=123: rev > x (123 > 12), so stop
 *     For odd length: compare x == rev / 10 (discard middle digit)
 *     12 == 123 / 10 → 12 == 12 ✓
 *
 * TIME:  O(log n) — we process half the digits (n/2 iterations, n = number of digits)
 * SPACE: O(1) — only a few variables
 *
 * ALTERNATIVE APPROACH: Convert to String (simpler but interviewer may ask you not to)
 *   String s = Integer.toString(x);
 *   Use two-pointer on the string.
 *   This is O(log n) time and O(log n) space (the string).
 */
public class PalindromeNumber {

    /**
     * Palindrome check without string conversion.
     * Reverses the second half of the number and compares with the first half.
     */
    public boolean isPalindrome(int x) {
        // Negative numbers are not palindromes
        if (x < 0) return false;

        // Numbers ending in 0 are not palindromes, except 0 itself.
        // (If last digit is 0, first digit would also need to be 0, impossible for n > 0)
        if (x != 0 && x % 10 == 0) return false;

        // Single digit numbers are always palindromes
        if (x < 10) return true;

        // Reverse the second half of x
        // We stop when reversedHalf >= x (we've processed half the digits)
        int reversedHalf = 0;
        while (x > reversedHalf) {
            int lastDigit = x % 10;       // Extract last digit
            reversedHalf = reversedHalf * 10 + lastDigit; // Append to reversed half
            x /= 10;                       // Remove last digit from x
        }

        // For even-length numbers: x == reversedHalf       (e.g., 1221 → 12 == 12)
        // For odd-length numbers:  x == reversedHalf / 10  (e.g., 12321 → 12 == 123/10 = 12)
        //   (The middle digit doesn't affect palindrome check, so we discard it)
        return x == reversedHalf || x == reversedHalf / 10;
    }

    /**
     * Alternative: String conversion approach.
     * Simpler and more readable. Use this if string conversion is allowed.
     *
     * TIME:  O(log n) — converting int to string and two-pointer
     * SPACE: O(log n) — the string representation
     */
    public boolean isPalindromeString(int x) {
        if (x < 0) return false;

        String s = Integer.toString(x);
        int left = 0;
        int right = s.length() - 1;

        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) return false;
            left++;
            right--;
        }
        return true;
    }

    /**
     * Alternative: Full reversal approach.
     * Reverses the entire number and compares.
     * Risk: integer overflow for large numbers (use long to guard against it).
     *
     * TIME:  O(log n), SPACE: O(1)
     */
    public boolean isPalindromeFullReverse(int x) {
        if (x < 0) return false;

        long reversed = 0;
        long original = x;
        long temp = x;

        while (temp > 0) {
            reversed = reversed * 10 + (temp % 10);
            temp /= 10;
        }

        return original == reversed;
    }

    // -----------------------------------------------------------------------
    // Trace walkthrough helper
    // -----------------------------------------------------------------------
    private void trace(int x) {
        System.out.printf("  Tracing isPalindrome(%d):%n", x);
        if (x < 0) { System.out.println("    → negative, return false"); return; }
        if (x != 0 && x % 10 == 0) { System.out.println("    → ends in 0, return false"); return; }
        if (x < 10) { System.out.println("    → single digit, return true"); return; }

        int rev = 0;
        int num = x;
        System.out.printf("    Start: x=%d, rev=%d%n", num, rev);
        while (num > rev) {
            int last = num % 10;
            rev = rev * 10 + last;
            num /= 10;
            System.out.printf("    Pop %d: x=%d, rev=%d%n", last, num, rev);
        }
        boolean result = (num == rev || num == rev / 10);
        System.out.printf("    x=%d, rev=%d → x==rev? %b, x==rev/10? %b → %b%n",
            num, rev, num == rev, num == rev / 10, result);
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        PalindromeNumber solution = new PalindromeNumber();

        Object[][] tests = {
            {121,      true},   // simple palindrome
            {-121,     false},  // negative
            {10,       false},  // ends in 0
            {0,        true},   // zero is palindrome
            {1,        true},   // single digit
            {11,       true},   // two same digits
            {12,       false},  // two different digits
            {1221,     true},   // even length palindrome
            {12321,    true},   // odd length palindrome
            {12345,    false},  // not palindrome
            {1000021,  false},  // ends in 1 but not palindrome
            {Integer.MAX_VALUE, false},  // 2147483647 — not palindrome
        };

        System.out.println("=== Palindrome Number ===");
        System.out.println();

        int passed = 0;
        for (Object[] test : tests) {
            int x = (int) test[0];
            boolean expected = (boolean) test[1];
            boolean result = solution.isPalindrome(x);
            boolean ok = result == expected;
            if (ok) passed++;
            System.out.printf("  %-15d → %-5s  %s%n",
                x, result, ok ? "✓" : "✗ (expected " + expected + ")");
        }
        System.out.printf("%nPassed: %d/%d%n", passed, tests.length);

        System.out.println();
        System.out.println("=== Trace Walkthrough ===");
        System.out.println();
        solution.trace(121);
        System.out.println();
        solution.trace(1221);
        System.out.println();
        solution.trace(12321);
        System.out.println();
        solution.trace(-121);
        System.out.println();
        solution.trace(10);
    }
}
