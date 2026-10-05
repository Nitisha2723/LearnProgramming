import java.util.Arrays;

/**
 * PROBLEM: Valid Anagram
 * Given two strings s and t, return true if t is an anagram of s.
 * An anagram is a word formed by rearranging the letters of another word,
 * using all original letters exactly once.
 *
 * Examples:
 *   s = "anagram", t = "nagaram" → true
 *   s = "rat",     t = "car"     → false
 *   s = "listen",  t = "silent"  → true
 *   s = "",        t = ""        → true (both empty)
 *   s = "a",       t = "a"       → true
 *   s = "a",       t = "ab"      → false (different lengths)
 *
 * APPROACHES:
 *
 * 1. Sort both strings and compare — O(n log n) time, O(n) space
 * 2. Character frequency count — O(n) time, O(1) space [BEST for ASCII]
 * 3. HashMap frequency count — O(n) time, O(k) space [BEST for Unicode]
 *
 * RECOMMENDED APPROACH: Character frequency count (Approach 2)
 *   - Create an int[26] array (one slot per letter a-z)
 *   - For each char in s: increment count[c - 'a']
 *   - For each char in t: decrement count[c - 'a']
 *   - If all counts are 0 → anagram
 *   - If any count != 0 → not anagram
 *
 * TIME:  O(n) where n = length of s (or t)
 * SPACE: O(1) — the 26-element array is constant size regardless of input length
 */
public class ValidAnagram {

    /**
     * Approach 1: Sort both strings and compare.
     *
     * Two strings are anagrams if and only if their sorted versions are equal.
     *
     * TIME:  O(n log n) — due to sorting
     * SPACE: O(n)       — converting to char[] for sorting
     */
    public boolean isAnagramSort(String s, String t) {
        if (s.length() != t.length()) return false;

        char[] sArr = s.toCharArray();
        char[] tArr = t.toCharArray();
        Arrays.sort(sArr);
        Arrays.sort(tArr);

        return Arrays.equals(sArr, tArr);
    }

    /**
     * Approach 2: Character frequency count (optimal for lowercase English).
     *
     * Works when input contains only lowercase a-z characters.
     *
     * One-pass optimization: use a single array, increment for s, decrement for t.
     * If arrays are anagrams, every count ends at 0.
     *
     * TIME:  O(n) — two linear passes (one for s, one for t)
     * SPACE: O(1) — fixed-size array of 26
     */
    public boolean isAnagram(String s, String t) {
        // Quick check: anagrams must have the same length
        if (s.length() != t.length()) return false;

        int[] count = new int[26]; // count[0] = 'a', count[25] = 'z'

        // Count characters in s (increment)
        for (char c : s.toCharArray()) {
            count[c - 'a']++;
        }

        // Count characters in t (decrement)
        for (char c : t.toCharArray()) {
            count[c - 'a']--;
        }

        // If all counts are 0, every char in s has a matching char in t
        for (int freq : count) {
            if (freq != 0) return false;
        }

        return true;
    }

    /**
     * Approach 3: HashMap — works for any Unicode characters (not just a-z).
     *
     * Use this variant if the problem says "Unicode characters" or
     * "characters from any language."
     *
     * TIME:  O(n) — linear passes
     * SPACE: O(k) — k = number of distinct characters (bounded by alphabet size)
     */
    public boolean isAnagramUnicode(String s, String t) {
        if (s.length() != t.length()) return false;

        java.util.Map<Character, Integer> count = new java.util.HashMap<>();

        // Increment count for chars in s
        for (char c : s.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) + 1);
        }

        // Decrement count for chars in t
        for (char c : t.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) - 1);
        }

        // All counts must be 0
        for (int freq : count.values()) {
            if (freq != 0) return false;
        }

        return true;
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        ValidAnagram solution = new ValidAnagram();

        Object[][] tests = {
            {"anagram", "nagaram", true},
            {"rat",     "car",     false},
            {"listen",  "silent",  true},
            {"",        "",        true},   // both empty
            {"a",       "a",       true},   // single same char
            {"a",       "b",       false},  // single diff char
            {"a",       "ab",      false},  // different lengths
            {"aab",     "baa",     true},   // duplicate characters
            {"aab",     "bba",     false},  // different frequencies
            {"hello",   "olleh",   true},   // reversed
            {"hello",   "world",   false},  // different chars
        };

        System.out.println("=== Valid Anagram ===");
        System.out.println();
        System.out.printf("%-12s %-12s %-8s %-8s %-8s %s%n",
            "s", "t", "Expected", "Sort", "Count", "Unicode");
        System.out.println("  " + "-".repeat(62));

        int passed = 0;
        for (Object[] test : tests) {
            String s = (String) test[0];
            String t = (String) test[1];
            boolean expected = (boolean) test[2];

            boolean sort    = solution.isAnagramSort(s, t);
            boolean count   = solution.isAnagram(s, t);
            boolean unicode = solution.isAnagramUnicode(s, t);

            boolean ok = (sort == expected) && (count == expected) && (unicode == expected);
            if (ok) passed++;

            System.out.printf("%-12s %-12s %-8s %-8s %-8s %s%n",
                "\"" + s + "\"",
                "\"" + t + "\"",
                expected,
                sort,
                count,
                ok ? "✓" : "✗");
        }

        System.out.printf("%nAll three approaches match: %d/%d%n", passed, tests.length);

        System.out.println();
        System.out.println("=== Trace: isAnagram(\"anagram\", \"nagaram\") ===");
        System.out.println();
        System.out.println("Lengths: both 7 ✓");
        System.out.println();
        System.out.println("After processing 'anagram':");
        int[] count = new int[26];
        for (char c : "anagram".toCharArray()) count[c - 'a']++;
        for (int i = 0; i < 26; i++) {
            if (count[i] != 0) {
                System.out.printf("  count['%c'] = %d%n", (char)('a' + i), count[i]);
            }
        }
        System.out.println();
        System.out.println("After processing 'nagaram' (decrementing):");
        for (char c : "nagaram".toCharArray()) count[c - 'a']--;
        boolean allZero = true;
        for (int freq : count) if (freq != 0) { allZero = false; break; }
        System.out.println("  All counts zero? " + allZero + " → return " + allZero);
    }
}
