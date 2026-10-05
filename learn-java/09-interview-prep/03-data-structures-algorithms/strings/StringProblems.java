import java.util.*;

/**
 * Solutions to 10 classic string interview problems.
 *
 * Problems covered:
 *  1. Valid Anagram                                   — char frequency array O(n)
 *  2. Reverse String In Place                         — two pointers O(n)
 *  3. Palindrome Check                                — two pointers O(n)
 *  4. Longest Substring Without Repeating Characters  — sliding window O(n)
 *  5. String Compression                              — two pointer / run counting O(n)
 *  6. Valid Parentheses                               — stack O(n)
 *  7. Reverse Words in a String                       — split + join O(n)
 *  8. Roman to Integer                                — HashMap + scan O(n)
 *  9. Count and Say                                   — iterative generation O(n*m)
 * 10. Longest Common Prefix                           — vertical scanning O(S)
 */
public class StringProblems {

    // =========================================================================
    // Problem 1: Valid Anagram
    // =========================================================================

    /**
     * Determine whether string t is an anagram of string s.
     *
     * Character Frequency Array:
     *   Use an int[26] array indexed by (char - 'a').
     *   Increment for each character in s, decrement for each in t.
     *   If all counts are zero, t is an anagram of s.
     *
     * Time:  O(n) where n = length of s (assuming |s| == |t|)
     * Space: O(1) — fixed 26-element array
     *
     * @param s original string
     * @param t string to check
     * @return true if t is an anagram of s
     */
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) return false;

        int[] freq = new int[26];

        for (char c : s.toCharArray()) freq[c - 'a']++;
        for (char c : t.toCharArray()) freq[c - 'a']--;

        for (int count : freq) {
            if (count != 0) return false;
        }
        return true;
    }

    // =========================================================================
    // Problem 2: Reverse String In Place
    // =========================================================================

    /**
     * Reverse a character array in-place using two pointers.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param s character array to reverse in-place
     */
    public void reverseString(char[] s) {
        int left = 0;
        int right = s.length - 1;

        while (left < right) {
            char temp = s[left];
            s[left] = s[right];
            s[right] = temp;
            left++;
            right--;
        }
    }

    // =========================================================================
    // Problem 3: Palindrome Check
    // =========================================================================

    /**
     * Check if a string is a palindrome after removing non-alphanumeric chars
     * and ignoring case.
     *
     * Two Pointer Approach:
     *   Advance left past non-alphanumeric chars, retreat right past them.
     *   Compare the chars at both pointers (case-insensitive).
     *   If any mismatch is found, return false.
     *
     * Time:  O(n)
     * Space: O(1) — no extra string built
     *
     * @param s input string to check
     * @return true if s is a palindrome (ignoring non-alphanumeric chars and case)
     */
    public boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;

        while (left < right) {
            // Skip non-alphanumeric characters from the left
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }
            // Skip non-alphanumeric characters from the right
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }

            // Compare the two characters (case-insensitive)
            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    // =========================================================================
    // Problem 4: Longest Substring Without Repeating Characters
    // =========================================================================

    /**
     * Find the length of the longest substring with all unique characters.
     *
     * Sliding Window + HashMap:
     *   Maintain a window [left, right] of unique characters.
     *   The HashMap stores each character's most recent index.
     *   When s[right] was last seen at index i and i >= left (inside the window),
     *   jump left to i+1 instead of shrinking one step at a time.
     *
     * Time:  O(n) — each character processed once
     * Space: O(min(n, alphabet_size)) — HashMap bounded by unique chars
     *
     * @param s input string
     * @return length of longest substring without repeating characters
     */
    public int lengthOfLongestSubstring(String s) {
        // Maps each character to its most recently seen index
        Map<Character, Integer> lastSeen = new HashMap<>();
        int maxLen = 0;
        int left = 0; // left boundary of current window

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);

            // If c is in the window (last seen at index >= left), shrink from left
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
                // Jump left to one past the previous occurrence of c
                left = lastSeen.get(c) + 1;
            }

            // Update last seen index for c
            lastSeen.put(c, right);
            // Update max window size
            maxLen = Math.max(maxLen, right - left + 1);
        }

        return maxLen;
    }

    // =========================================================================
    // Problem 5: String Compression
    // =========================================================================

    /**
     * Compress a character array in-place using run-length encoding.
     * Return the new length.
     *
     * Two Pointer (Read/Write):
     *   i = read pointer scanning through the array
     *   w = write pointer tracking where to write the compressed result
     *   For each run of identical characters: write char, then write count if > 1.
     *   Count digits are written as separate characters.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param chars character array to compress in-place
     * @return new length of the compressed array
     */
    public int compress(char[] chars) {
        int w = 0; // write pointer
        int i = 0; // read pointer

        while (i < chars.length) {
            char currentChar = chars[i];
            int runLength = 0;

            // Count the length of the current run
            while (i < chars.length && chars[i] == currentChar) {
                i++;
                runLength++;
            }

            // Write the character
            chars[w++] = currentChar;

            // Write the count only if > 1 (runs of 1 are omitted per the problem)
            if (runLength > 1) {
                // Convert count to string and write each digit
                String countStr = Integer.toString(runLength);
                for (char digit : countStr.toCharArray()) {
                    chars[w++] = digit;
                }
            }
        }

        return w;
    }

    // =========================================================================
    // Problem 6: Valid Parentheses
    // =========================================================================

    /**
     * Determine if a string of brackets is valid (properly nested and matched).
     *
     * Stack Approach:
     *   Push opening brackets onto the stack.
     *   When encountering a closing bracket:
     *   - Stack is empty → no matching open bracket → false
     *   - Top of stack doesn't match → wrong type → false
     *   - Match found → pop the stack
     *   After processing, stack must be empty (all opened brackets were closed).
     *
     * Time:  O(n)
     * Space: O(n) — worst case all opening brackets
     *
     * @param s string containing only ()[]{}
     * @return true if the bracket string is valid
     */
    public boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                // Push opening brackets
                stack.push(c);
            } else {
                // Closing bracket: check for match
                if (stack.isEmpty()) return false;

                char top = stack.pop();
                if (c == ')' && top != '(') return false;
                if (c == ']' && top != '[') return false;
                if (c == '}' && top != '{') return false;
            }
        }

        // Valid only if all opening brackets were matched
        return stack.isEmpty();
    }

    // =========================================================================
    // Problem 7: Reverse Words in a String
    // =========================================================================

    /**
     * Reverse the order of words in a string.
     * Words are separated by at least one space. Return with single spaces, no leading/trailing.
     *
     * Split + Reverse + Join:
     *   trim() removes leading/trailing whitespace.
     *   split("\\s+") splits on any whitespace sequence (handles multiple spaces).
     *   Traverse the resulting array from back to front, joining with spaces.
     *
     * Time:  O(n)
     * Space: O(n) for the split array and StringBuilder
     *
     * @param s input string
     * @return string with word order reversed
     */
    public String reverseWords(String s) {
        String[] words = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (int i = words.length - 1; i >= 0; i--) {
            sb.append(words[i]);
            if (i > 0) sb.append(' ');
        }

        return sb.toString();
    }

    // =========================================================================
    // Problem 8: Roman to Integer
    // =========================================================================

    /**
     * Convert a Roman numeral string to an integer.
     *
     * HashMap + Left-to-Right Scan:
     *   Build a map of each Roman symbol to its value.
     *   Scan left to right. For each symbol:
     *   - If the current symbol's value is LESS than the next symbol's value,
     *     SUBTRACT the current value (e.g., I before V → subtract 1)
     *   - Otherwise ADD the current value
     *
     *   This handles all six subtraction cases: IV, IX, XL, XC, CD, CM.
     *
     * Time:  O(n)
     * Space: O(1) — map has exactly 7 entries
     *
     * @param s valid Roman numeral string in range [1, 3999]
     * @return integer value
     */
    public int romanToInt(String s) {
        Map<Character, Integer> values = new HashMap<>();
        values.put('I', 1);
        values.put('V', 5);
        values.put('X', 10);
        values.put('L', 50);
        values.put('C', 100);
        values.put('D', 500);
        values.put('M', 1000);

        int result = 0;

        for (int i = 0; i < s.length(); i++) {
            int currentVal = values.get(s.charAt(i));

            // Check if there's a next symbol and if it's larger (subtraction case)
            if (i + 1 < s.length() && values.get(s.charAt(i + 1)) > currentVal) {
                result -= currentVal;
            } else {
                result += currentVal;
            }
        }

        return result;
    }

    // =========================================================================
    // Problem 9: Count and Say
    // =========================================================================

    /**
     * Generate the nth term of the count-and-say sequence.
     *
     * The sequence:
     *   n=1: "1"
     *   n=2: "11"    (one 1)
     *   n=3: "21"    (two 1s)
     *   n=4: "1211"  (one 2, one 1)
     *   n=5: "111221" (one 1, one 2, two 1s)
     *
     * Iterative Generation:
     *   Start with "1", then iteratively build the next term from the current one.
     *   For each run of consecutive identical characters, record count + character.
     *   Use StringBuilder for efficient string construction.
     *
     * Time:  O(n * m) where m is the length of the nth term
     * Space: O(m) for the current string
     *
     * @param n term number (1-indexed)
     * @return the nth count-and-say string
     */
    public String countAndSay(int n) {
        String current = "1";

        for (int step = 1; step < n; step++) {
            StringBuilder next = new StringBuilder();
            int i = 0;

            while (i < current.length()) {
                char digit = current.charAt(i);
                int count = 0;

                // Count consecutive identical characters
                while (i < current.length() && current.charAt(i) == digit) {
                    i++;
                    count++;
                }

                // Append "count" then "digit"
                next.append(count);
                next.append(digit);
            }

            current = next.toString();
        }

        return current;
    }

    // =========================================================================
    // Problem 10: Longest Common Prefix
    // =========================================================================

    /**
     * Find the longest common prefix string among an array of strings.
     * Return "" if no common prefix exists.
     *
     * Vertical Scanning:
     *   Treat the first string as the candidate prefix.
     *   For each character position i, check if strs[j][i] equals strs[0][i]
     *   for every string j. If any string is too short or has a different character,
     *   return the prefix found so far.
     *
     * Time:  O(S) where S = total characters in all strings
     * Space: O(1)
     *
     * @param strs array of strings
     * @return longest common prefix, or "" if none
     */
    public String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";

        // Use the first string as our reference
        String reference = strs[0];

        for (int i = 0; i < reference.length(); i++) {
            char c = reference.charAt(i);

            // Check this character against all other strings
            for (int j = 1; j < strs.length; j++) {
                // String is shorter than position i, or character differs
                if (i >= strs[j].length() || strs[j].charAt(i) != c) {
                    // Return prefix up to (not including) position i
                    return reference.substring(0, i);
                }
            }
        }

        // All characters in reference matched → reference itself is the prefix
        return reference;
    }

    // =========================================================================
    // Main — Test Cases
    // =========================================================================

    public static void main(String[] args) {
        StringProblems sol = new StringProblems();

        System.out.println("=== Problem 1: Valid Anagram ===");
        System.out.println(sol.isAnagram("anagram", "nagaram")); // true
        System.out.println(sol.isAnagram("rat", "car"));          // false
        System.out.println(sol.isAnagram("ab", "a"));             // false (different lengths)

        System.out.println("\n=== Problem 2: Reverse String In Place ===");
        char[] arr1 = {'h', 'e', 'l', 'l', 'o'};
        sol.reverseString(arr1);
        System.out.println(new String(arr1)); // olleh

        char[] arr2 = {'H', 'a', 'n', 'n', 'a', 'h'};
        sol.reverseString(arr2);
        System.out.println(new String(arr2)); // hannaH

        System.out.println("\n=== Problem 3: Palindrome Check ===");
        System.out.println(sol.isPalindrome("A man, a plan, a canal: Panama")); // true
        System.out.println(sol.isPalindrome("race a car"));                       // false
        System.out.println(sol.isPalindrome(" "));                                 // true
        System.out.println(sol.isPalindrome("Was it a car or a cat I saw?"));     // true

        System.out.println("\n=== Problem 4: Longest Substring Without Repeating Characters ===");
        System.out.println(sol.lengthOfLongestSubstring("abcabcbb")); // 3 (abc)
        System.out.println(sol.lengthOfLongestSubstring("bbbbb"));    // 1 (b)
        System.out.println(sol.lengthOfLongestSubstring("pwwkew"));   // 3 (wke)
        System.out.println(sol.lengthOfLongestSubstring(""));         // 0

        System.out.println("\n=== Problem 5: String Compression ===");
        char[] c1 = {'a', 'a', 'b', 'b', 'c', 'c', 'c'};
        int len1 = sol.compress(c1);
        System.out.println(len1 + " → " + new String(c1, 0, len1)); // 6 → a2b2c3

        char[] c2 = {'a'};
        int len2 = sol.compress(c2);
        System.out.println(len2 + " → " + new String(c2, 0, len2)); // 1 → a

        char[] c3 = {'a', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b'};
        int len3 = sol.compress(c3);
        System.out.println(len3 + " → " + new String(c3, 0, len3)); // 4 → ab12

        System.out.println("\n=== Problem 6: Valid Parentheses ===");
        System.out.println(sol.isValid("()"));      // true
        System.out.println(sol.isValid("()[]{}")); // true
        System.out.println(sol.isValid("(]"));      // false
        System.out.println(sol.isValid("([)]"));    // false
        System.out.println(sol.isValid("{[]}"));    // true

        System.out.println("\n=== Problem 7: Reverse Words in a String ===");
        System.out.println(sol.reverseWords("the sky is blue"));    // "blue is sky the"
        System.out.println(sol.reverseWords("  hello world  "));    // "world hello"
        System.out.println(sol.reverseWords("a good   example"));   // "example good a"

        System.out.println("\n=== Problem 8: Roman to Integer ===");
        System.out.println(sol.romanToInt("III"));     // 3
        System.out.println(sol.romanToInt("LVIII"));   // 58
        System.out.println(sol.romanToInt("MCMXCIV")); // 1994
        System.out.println(sol.romanToInt("IV"));      // 4
        System.out.println(sol.romanToInt("IX"));      // 9

        System.out.println("\n=== Problem 9: Count and Say ===");
        for (int i = 1; i <= 6; i++) {
            System.out.println("n=" + i + ": " + sol.countAndSay(i));
        }
        // n=1: 1
        // n=2: 11
        // n=3: 21
        // n=4: 1211
        // n=5: 111221
        // n=6: 312211

        System.out.println("\n=== Problem 10: Longest Common Prefix ===");
        System.out.println(sol.longestCommonPrefix(new String[]{"flower", "flow", "flight"})); // "fl"
        System.out.println(sol.longestCommonPrefix(new String[]{"dog", "racecar", "car"}));    // ""
        System.out.println(sol.longestCommonPrefix(new String[]{"abc", "abc", "abc"}));        // "abc"
        System.out.println(sol.longestCommonPrefix(new String[]{""}));                           // ""
    }
}
