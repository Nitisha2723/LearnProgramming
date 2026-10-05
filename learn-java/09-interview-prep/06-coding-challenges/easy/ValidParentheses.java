import java.util.HashMap;
import java.util.Map;

/**
 * PROBLEM: Valid Parentheses
 * Given a string containing just the characters '(', ')', '{', '}', '[', ']',
 * determine if the input string is valid.
 *
 * A string is valid if:
 *   1. Open brackets must be closed by the same type of bracket.
 *   2. Open brackets must be closed in the correct order.
 *   3. Every close bracket has a corresponding open bracket of the same type.
 *
 * Examples:
 *   "()"      → true
 *   "()[]{}"  → true
 *   "(]"      → false
 *   "([)]"    → false
 *   "{[]}"    → true
 *   ""        → true (empty string is valid)
 *
 * APPROACH: Stack
 *   - Push every opening bracket onto the stack.
 *   - When we see a closing bracket, check if it matches the top of the stack.
 *     - If it matches → pop the stack.
 *     - If it doesn't match, or the stack is empty → invalid.
 *   - At the end, the stack must be empty (all opens were matched).
 *
 * WHY STACK?
 *   The last opened bracket must be the first one closed — LIFO order.
 *   A stack perfectly models this "most recently opened" tracking.
 *
 * TIME:  O(n) — each character is processed exactly once
 * SPACE: O(n) — worst case: all opening brackets "((((" fills the stack
 */
public class ValidParentheses {

    public boolean isValid(String s) {
        // Stack to track unmatched opening brackets
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();

        // Map closing bracket → its matching opening bracket
        Map<Character, Character> matching = new HashMap<>();
        matching.put(')', '(');
        matching.put(']', '[');
        matching.put('}', '{');

        for (char c : s.toCharArray()) {
            if (matching.containsValue(c)) {
                // It's an opening bracket — push it
                stack.push(c);
            } else if (matching.containsKey(c)) {
                // It's a closing bracket
                // Stack must be non-empty and top must match
                if (stack.isEmpty() || stack.peek() != matching.get(c)) {
                    return false;
                }
                stack.pop(); // Matched — remove the opening bracket
            }
            // Ignore any character that is not a bracket (problem says only brackets, but being safe)
        }

        // Valid only if all opening brackets were matched (stack is empty)
        return stack.isEmpty();
    }

    // -----------------------------------------------------------------------
    // Alternative: explicit if-else (clearer for interviews, avoids Map setup)
    // -----------------------------------------------------------------------
    public boolean isValidAlt(String s) {
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();

        for (char c : s.toCharArray()) {
            // Push opening brackets
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                // Closing bracket — stack must have a matching opener on top
                if (stack.isEmpty()) return false;

                char top = stack.pop();
                if (c == ')' && top != '(') return false;
                if (c == ']' && top != '[') return false;
                if (c == '}' && top != '{') return false;
            }
        }

        return stack.isEmpty();
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        ValidParentheses solution = new ValidParentheses();

        // Test cases: [input, expected]
        Object[][] tests = {
            {"()",     true},
            {"()[]{}",  true},
            {"(]",     false},
            {"([)]",   false},
            {"{[]}",   true},
            {"",       true},   // Edge: empty string
            {"(",      false},  // Edge: unmatched open
            {")",      false},  // Edge: unmatched close
            {"((((", false},   // Edge: all opens, no closes
            {"))))", false},   // Edge: all closes, no opens
            {"()[{}]", true},  // Nested mixed
        };

        System.out.println("=== Valid Parentheses ===");
        int passed = 0;
        for (Object[] test : tests) {
            String input = (String) test[0];
            boolean expected = (boolean) test[1];
            boolean result = solution.isValid(input);
            boolean ok = result == expected;
            if (ok) passed++;
            System.out.printf("  %-15s → %-5s  %s%n",
                "\"" + input + "\"", result, ok ? "✓" : "✗ (expected " + expected + ")");
        }
        System.out.printf("%nPassed: %d/%d%n", passed, tests.length);
    }
}
