/**
 * Exercise03_Solution.java
 *
 * SOLUTION: String and Array Methods
 */
public class Exercise03_Solution {

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

        System.out.print("findMax({}): ");
        try {
            findMax(new int[]{});
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException caught: " + e.getMessage());
        }
    }

    /**
     * Returns true if s is a palindrome, ignoring case and non-letter characters.
     *
     * Approach:
     * 1. Strip non-letters and lowercase → cleaned string
     * 2. Compare from both ends toward the middle
     */
    static boolean isPalindrome(String s) {
        // Step 1: Build cleaned string (letters only, lowercase)
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toLowerCase().toCharArray()) {
            if (Character.isLetter(ch)) {
                sb.append(ch);
            }
        }
        String cleaned = sb.toString();

        // Step 2: Compare from both ends
        int left = 0;
        int right = cleaned.length() - 1;

        while (left < right) {
            if (cleaned.charAt(left) != cleaned.charAt(right)) {
                return false; // mismatch found — not a palindrome
            }
            left++;
            right--;
        }

        return true; // no mismatch found
    }

    /**
     * Returns the string s with characters reversed.
     *
     * Approach: iterate from last character to first, appending to StringBuilder.
     */
    static String reverseString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
        }
        return sb.toString();

        // Note: You could also write: return new StringBuilder(s).reverse().toString();
        // But implementing it manually is better practice.
    }

    /**
     * Counts vowels (a, e, i, o, u) in s, case-insensitive.
     */
    static int countVowels(String s) {
        int count = 0;
        String lower = s.toLowerCase();

        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                count++;
            }
        }

        return count;

        // Alternative with String.indexOf:
        // for (char ch : s.toLowerCase().toCharArray())
        //     if ("aeiou".indexOf(ch) != -1) count++;
    }

    /**
     * Returns the maximum value in arr.
     * Throws IllegalArgumentException if arr is empty.
     */
    static int findMax(int[] arr) {
        if (arr.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }

        int max = arr[0]; // safe now — we know arr has at least one element

        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }

        return max;
    }
}
