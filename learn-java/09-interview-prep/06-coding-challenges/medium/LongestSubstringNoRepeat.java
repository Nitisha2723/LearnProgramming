import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * PROBLEM: Longest Substring Without Repeating Characters
 * Given a string s, find the length of the longest substring without
 * repeating characters.
 *
 * Examples:
 *   "abcabcbb"  → 3   ("abc")
 *   "bbbbb"     → 1   ("b")
 *   "pwwkew"    → 3   ("wke")
 *   ""          → 0
 *   "abcdef"    → 6   (entire string)
 *   "dvdf"      → 3   ("vdf")
 *
 * APPROACH: Sliding Window
 *   Maintain a window [left, right] that always contains no duplicate characters.
 *   Expand the window by moving `right`.
 *   When a duplicate is found, shrink the window by moving `left` past the
 *   previous occurrence of the duplicate character.
 *
 * KEY DATA STRUCTURE: HashMap<Character, Integer>
 *   Maps each character to the index where it was LAST SEEN.
 *   When we encounter a character we've seen before, we can jump `left`
 *   directly to (lastSeen + 1) without scanning through the window.
 *
 * TRACE: "abcabcbb"
 *   left=0
 *   right=0: 'a' new. window=[a], max=1
 *   right=1: 'b' new. window=[ab], max=2
 *   right=2: 'c' new. window=[abc], max=3
 *   right=3: 'a' seen at 0, left=max(0, 0+1)=1. window=[bca], max=3
 *   right=4: 'b' seen at 1, left=max(1, 1+1)=2. window=[cab], max=3
 *   right=5: 'c' seen at 2, left=max(2, 2+1)=3. window=[abc], max=3
 *   right=6: 'b' seen at 4, left=max(3, 4+1)=5. window=[cb], max=3
 *   right=7: 'b' seen at 6, left=max(5, 6+1)=7. window=[b], max=3
 *   Result: 3
 *
 * WHY max(left, lastSeen + 1)?
 *   The duplicate might be BEHIND the current left pointer (outside the window).
 *   We don't want to move left backward — that would re-include characters we've
 *   already excluded. Taking max() ensures left only moves forward.
 *
 * TIME:  O(n) — right pointer visits each character once
 * SPACE: O(min(m, n)) where m = alphabet size (bounded by character set)
 *          For ASCII: O(128) = O(1)
 *          For Unicode: O(n) in the worst case
 */
public class LongestSubstringNoRepeat {

    /**
     * Sliding window with HashMap — O(n) time, O(min(m,n)) space.
     * Stores the last seen index of each character.
     */
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.isEmpty()) return 0;

        // Map each character to the index where it was last seen
        Map<Character, Integer> lastSeen = new HashMap<>();

        int maxLength = 0;
        int left = 0; // Left boundary of current window

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // If this character was seen inside our current window,
            // shrink the window by moving left past the previous occurrence
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
                left = lastSeen.get(c) + 1;
            }

            // Update the last seen index for this character
            lastSeen.put(c, right);

            // Update max if current window is larger
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }

    /**
     * Alternative: Using an int[128] array for ASCII characters.
     * Slightly faster than HashMap for ASCII-only input (direct array indexing).
     *
     * TIME: O(n), SPACE: O(1) — fixed 128-element array
     */
    public int lengthOfLongestSubstringArray(String s) {
        if (s == null || s.isEmpty()) return 0;

        // ASCII: 128 characters. Initialize to -1 (not seen).
        int[] lastSeen = new int[128];
        Arrays.fill(lastSeen, -1);

        int maxLength = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // If seen within current window, move left
            if (lastSeen[c] >= left) {
                left = lastSeen[c] + 1;
            }

            lastSeen[c] = right;
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }

    /**
     * Alternative: Using a HashSet for O(n) solution with simpler logic.
     * Instead of jumping left, we incrementally remove characters from the
     * window by advancing left one step at a time.
     *
     * Less efficient in the worst case (e.g., "aaaa" requires n moves of left
     * per character), but still O(n) overall because each character is added
     * and removed from the set at most once.
     *
     * TIME: O(n), SPACE: O(min(m,n))
     */
    public int lengthOfLongestSubstringSet(String s) {
        if (s == null || s.isEmpty()) return 0;

        java.util.Set<Character> window = new java.util.HashSet<>();
        int maxLength = 0;
        int left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // Shrink window from left until no duplicate
            while (window.contains(c)) {
                window.remove(s.charAt(left));
                left++;
            }

            window.add(c);
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }

    private int[] makeLastSeen() {
        int[] arr = new int[128];
        Arrays.fill(arr, -1);
        return arr;
    }

    // Re-implement the array version without the static field hack
    public int lengthOfLongestSubstringArrayClean(String s) {
        if (s == null || s.isEmpty()) return 0;
        int[] lastSeen = makeLastSeen();
        int maxLength = 0, left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (lastSeen[c] >= left) left = lastSeen[c] + 1;
            lastSeen[c] = right;
            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }

    // -----------------------------------------------------------------------
    // Detailed trace helper
    // -----------------------------------------------------------------------
    private void trace(String s) {
        System.out.println("  Tracing: \"" + s + "\"");
        Map<Character, Integer> lastSeen = new HashMap<>();
        int maxLength = 0, left = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            int oldLeft = left;

            if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
                left = lastSeen.get(c) + 1;
            }
            lastSeen.put(c, right);
            int windowLen = right - left + 1;
            maxLength = Math.max(maxLength, windowLen);

            System.out.printf("    right=%d c='%c' left: %d→%d  window=\"%s\" len=%d max=%d%n",
                right, c, oldLeft, left,
                s.substring(left, right + 1), windowLen, maxLength);
        }
        System.out.println("  Result: " + maxLength);
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        LongestSubstringNoRepeat solution = new LongestSubstringNoRepeat();

        Object[][] tests = {
            {"abcabcbb", 3},  // "abc"
            {"bbbbb",    1},  // "b"
            {"pwwkew",   3},  // "wke"
            {"",         0},  // empty
            {"a",        1},  // single char
            {"abcdef",   6},  // no repeats — entire string
            {"dvdf",     3},  // "vdf" — left jumps ahead
            {"aab",      2},  // "ab"
            {" ",        1},  // space character
            {"au",       2},  // "au"
            {"abba",     2},  // "ab" or "ba"
        };

        System.out.println("=== Longest Substring Without Repeating Characters ===");
        System.out.println();

        int passed = 0;
        System.out.printf("%-15s %-8s %-8s%n", "Input", "Expected", "Result");
        System.out.println("  " + "-".repeat(34));

        for (Object[] test : tests) {
            String s = (String) test[0];
            int expected = (int) test[1];
            int result = solution.lengthOfLongestSubstring(s);
            boolean ok = result == expected;
            if (ok) passed++;
            System.out.printf("%-15s %-8d %-8d %s%n",
                "\"" + s + "\"", expected, result, ok ? "✓" : "✗");
        }
        System.out.printf("%nPassed: %d/%d%n", passed, tests.length);

        System.out.println();
        System.out.println("=== Detailed Trace ===");
        System.out.println();
        solution.trace("abcabcbb");
        System.out.println();
        solution.trace("dvdf");
    }
}
