import java.util.*;

/**
 * PROBLEM: Group Anagrams
 * Given an array of strings, group the anagrams together.
 * Return the result in any order.
 *
 * Example:
 *   Input:  ["eat", "tea", "tan", "ate", "nat", "bat"]
 *   Output: [["bat"], ["nat", "tan"], ["ate", "eat", "tea"]]
 *
 * APPROACH: HashMap with sorted string as key
 *   KEY INSIGHT: All anagrams of a word, when sorted, produce the same string.
 *     "eat" sorted = "aet"
 *     "tea" sorted = "aet"   ← same key!
 *     "ate" sorted = "aet"   ← same key!
 *     "tan" sorted = "ant"
 *     "nat" sorted = "ant"   ← same key!
 *     "bat" sorted = "abt"
 *
 *   Use the sorted string as a HashMap key. All words that are anagrams
 *   of each other map to the same key and are grouped in the same list.
 *
 * TIME:  O(n * k log k)
 *          n = number of strings
 *          k = max length of a string
 *          Sorting each string: O(k log k)
 *          Processing all strings: O(n * k log k)
 *
 * SPACE: O(n * k) — storing all strings in the HashMap values
 *
 * ALTERNATIVE: Character frequency as key — O(n * k) time
 *   Instead of sorting, encode character frequencies:
 *   "eat" → [1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0]
 *   → key = "#1#0#0#0#1#0...#1..." (26 counts joined with a separator)
 *   This is O(n * k) time but more complex to implement.
 */
public class GroupAnagrams {

    /**
     * Approach 1: Sort each string to get the grouping key.
     *
     * TIME:  O(n * k log k) — dominant cost is sorting each string
     * SPACE: O(n * k)
     */
    public List<List<String>> groupAnagrams(String[] strs) {
        // Map: sorted_string → list of original strings
        Map<String, List<String>> map = new HashMap<>();

        for (String word : strs) {
            // Get the canonical form (sorted characters)
            char[] chars = word.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);

            // Group this word under the canonical key
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
            // Equivalent but more verbose:
            // if (!map.containsKey(key)) map.put(key, new ArrayList<>());
            // map.get(key).add(word);
        }

        return new ArrayList<>(map.values());
    }

    /**
     * Approach 2: Character frequency as key — O(n * k) time.
     *
     * Instead of sorting, encode the character frequency as a string key.
     * This avoids the O(k log k) sort and achieves O(k) per word.
     *
     * Key format: "1#0#0#0#1#0#0#0#0#0#0#0#0#0#0#0#0#0#0#1#..."
     * Each count is separated by '#' to prevent ambiguity:
     *   Without separator: "a=1, b=10" could be confused with "a=11, b=0"
     *   With '#': "1#10#0..." vs "11#0#..."  ← unambiguous
     *
     * TIME:  O(n * k)       — k operations per word, n words
     * SPACE: O(n * k)
     */
    public List<List<String>> groupAnagramsFreq(String[] strs) {
        Map<String, List<String>> map = new HashMap<>();

        for (String word : strs) {
            // Build frequency array
            int[] freq = new int[26];
            for (char c : word.toCharArray()) {
                freq[c - 'a']++;
            }

            // Build key from frequency array
            // Using '#' as separator prevents "1" + "0" == "10" ambiguity
            StringBuilder keyBuilder = new StringBuilder();
            for (int count : freq) {
                keyBuilder.append(count).append('#');
            }
            String key = keyBuilder.toString();

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
        }

        return new ArrayList<>(map.values());
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        GroupAnagrams solution = new GroupAnagrams();

        System.out.println("=== Group Anagrams ===");
        System.out.println();

        // Test 1: Classic example
        String[] test1 = {"eat", "tea", "tan", "ate", "nat", "bat"};
        System.out.println("Input: " + Arrays.toString(test1));
        List<List<String>> result1 = solution.groupAnagrams(test1);
        // Sort for deterministic output
        result1.forEach(Collections::sort);
        result1.sort(Comparator.comparing(l -> l.get(0)));
        System.out.println("Output (sort): " + result1);
        System.out.println("Expected: [[ate, eat, tea], [bat], [nat, tan]] (order may vary)");
        System.out.println();

        // Test 2: Single empty string
        String[] test2 = {""};
        System.out.println("Input: " + Arrays.toString(test2));
        System.out.println("Output: " + solution.groupAnagrams(test2));
        System.out.println("Expected: [[\"\"]]");
        System.out.println();

        // Test 3: All same string
        String[] test3 = {"a"};
        System.out.println("Input: " + Arrays.toString(test3));
        System.out.println("Output: " + solution.groupAnagrams(test3));
        System.out.println("Expected: [[\"a\"]]");
        System.out.println();

        // Test 4: No anagrams
        String[] test4 = {"abc", "def", "ghi"};
        System.out.println("Input: " + Arrays.toString(test4));
        List<List<String>> result4 = solution.groupAnagrams(test4);
        result4.sort(Comparator.comparing(l -> l.get(0)));
        System.out.println("Output: " + result4);
        System.out.println("Expected: [[abc], [def], [ghi]] (each in its own group)");
        System.out.println();

        // Test 5: All same anagram group
        String[] test5 = {"abc", "bca", "cab", "bac", "acb", "cba"};
        System.out.println("Input: " + Arrays.toString(test5));
        List<List<String>> result5 = solution.groupAnagrams(test5);
        result5.forEach(Collections::sort);
        System.out.println("Output: " + result5);
        System.out.println("Expected: [[abc, acb, bac, bca, cab, cba]] (all in one group)");
        System.out.println();

        // Compare both approaches
        System.out.println("--- Both approaches on test1 ---");
        List<List<String>> resultSort = solution.groupAnagrams(test1);
        List<List<String>> resultFreq = solution.groupAnagramsFreq(test1);
        resultSort.forEach(Collections::sort);
        resultFreq.forEach(Collections::sort);
        resultSort.sort(Comparator.comparing(l -> l.get(0)));
        resultFreq.sort(Comparator.comparing(l -> l.get(0)));
        System.out.println("Sort-based: " + resultSort);
        System.out.println("Freq-based: " + resultFreq);
        System.out.println("Same result: " + resultSort.equals(resultFreq));

        System.out.println();
        System.out.println("=== Key Building Trace ===");
        System.out.println("'eat' → sort → 'aet'");
        System.out.println("'tea' → sort → 'aet'  (same key → same group)");
        System.out.println("'ate' → sort → 'aet'  (same key → same group)");
        System.out.println("'tan' → sort → 'ant'");
        System.out.println("'nat' → sort → 'ant'  (same key → same group)");
        System.out.println("'bat' → sort → 'abt'  (unique key → own group)");
    }
}
