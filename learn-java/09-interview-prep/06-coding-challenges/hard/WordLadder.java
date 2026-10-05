import java.util.*;

/**
 * PROBLEM: Word Ladder
 * Given two words (beginWord and endWord) and a dictionary (wordList),
 * find the length of the shortest transformation sequence from beginWord
 * to endWord, such that:
 *   - Only one letter can be changed at a time.
 *   - Each transformed word must exist in the word list.
 *
 * Return 0 if no such transformation sequence exists.
 *
 * Example 1:
 *   beginWord = "hit", endWord = "cog"
 *   wordList  = ["hot","dot","dog","lot","log","cog"]
 *   Output: 5
 *   Explanation: "hit" → "hot" → "dot" → "dog" → "cog" (5 words = 4 transformations)
 *
 * Example 2:
 *   beginWord = "hit", endWord = "cog"
 *   wordList  = ["hot","dot","dog","lot","log"]  (no "cog")
 *   Output: 0
 *
 * APPROACH: BFS
 *   This is a shortest path problem on an unweighted graph:
 *   - Nodes: words
 *   - Edges: words that differ by exactly one character
 *   - Find shortest path from beginWord to endWord
 *   BFS finds shortest path in unweighted graphs.
 *
 * NAIVE BFS: O(N^2 * M) — for each word, compare it against all N words
 *   to find neighbors, each comparison takes O(M). N words in queue.
 *
 * OPTIMIZED BFS: O(M^2 * N) using wildcard pattern matching
 *   Instead of comparing each word against all other words,
 *   build a pattern map:
 *     "hot" → patterns: "*ot", "h*t", "ho*"
 *   All words sharing a pattern differ by exactly one character.
 *
 *   Pattern map: {pattern → [list of words matching it]}
 *   To find neighbors of "hot": look up "*ot", "h*t", "ho*"
 *
 * WHERE DO THE COMPLEXITIES COME FROM?
 *   M = word length, N = number of words in wordList
 *   - Building pattern map: O(N * M) — N words, M patterns each
 *   - BFS:
 *     - Each of N words is processed once
 *     - For each word, we check M patterns
 *     - Each pattern lookup may yield multiple words but each word is visited once
 *     - Total BFS: O(N * M)
 *     - But each pattern replacement involves creating a string: O(M)
 *   - Total: O(M^2 * N) — the M^2 comes from N*M string operations of length M
 *
 * TIME:  O(M^2 * N)
 * SPACE: O(M^2 * N) — the pattern map stores N * M patterns, each of length M
 *
 * ALTERNATIVE: Bidirectional BFS (advanced)
 *   Instead of BFS from just beginWord, do BFS from both ends simultaneously.
 *   Reduces complexity to roughly O(M^2 * N^(1/2)) in practice.
 *   Not typically expected in interviews — mention as optimization.
 */
public class WordLadder {

    // -----------------------------------------------------------------------
    // Approach 1: Standard BFS with pattern map
    // -----------------------------------------------------------------------
    /**
     * Find the shortest transformation sequence length using BFS with
     * wildcard pattern optimization.
     */
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        // Build a Set for O(1) endWord lookup
        Set<String> wordSet = new HashSet<>(wordList);

        // If endWord is not in the wordList, no path exists
        if (!wordSet.contains(endWord)) return 0;

        // Build pattern map: wildcard_pattern → [list of matching words]
        // e.g., "*ot" → ["hot", "dot", "lot"]
        //        "h*t" → ["hot"]
        //        "ho*" → ["hot"]
        Map<String, List<String>> patternToWords = new HashMap<>();

        // Include beginWord when building patterns (it may not be in wordList)
        List<String> allWords = new ArrayList<>(wordList);
        allWords.add(beginWord);

        int wordLen = beginWord.length();

        for (String word : allWords) {
            for (int i = 0; i < wordLen; i++) {
                // Create pattern by replacing character at position i with '*'
                String pattern = word.substring(0, i) + '*' + word.substring(i + 1);
                patternToWords.computeIfAbsent(pattern, k -> new ArrayList<>()).add(word);
            }
        }

        // BFS from beginWord
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.offer(beginWord);
        visited.add(beginWord);

        int level = 1; // Count includes beginWord (problem asks for sequence length)

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            level++;

            for (int i = 0; i < levelSize; i++) {
                String word = queue.poll();

                // Try all patterns for this word
                for (int j = 0; j < wordLen; j++) {
                    String pattern = word.substring(0, j) + '*' + word.substring(j + 1);

                    // Check all words that match this pattern (differ by one char)
                    List<String> neighbors = patternToWords.getOrDefault(pattern, Collections.emptyList());
                    for (String neighbor : neighbors) {
                        if (neighbor.equals(endWord)) return level;

                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.offer(neighbor);
                        }
                    }
                }
            }
        }

        return 0; // No path found
    }

    // -----------------------------------------------------------------------
    // Approach 2: Direct BFS (simpler, slightly slower O(N^2 * M))
    // -----------------------------------------------------------------------
    /**
     * BFS without pattern map — check all words for each expansion.
     * Simpler to explain in an interview; slightly less efficient.
     *
     * For each word in the queue, scan all unvisited words in wordList
     * and check if they differ by exactly one character.
     *
     * TIME:  O(N^2 * M) — for each of N words, compare against N words, each O(M)
     * SPACE: O(N)
     */
    public int ladderLengthSimple(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if (!wordSet.contains(endWord)) return 0;

        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        wordSet.remove(beginWord); // Don't revisit beginWord

        int level = 1;

        while (!queue.isEmpty()) {
            int size = queue.size();
            level++;

            for (int i = 0; i < size; i++) {
                String word = queue.poll();
                char[] chars = word.toCharArray();

                // Try changing each character
                for (int j = 0; j < chars.length; j++) {
                    char original = chars[j];
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (c == original) continue;
                        chars[j] = c;
                        String candidate = new String(chars);

                        if (candidate.equals(endWord)) return level;

                        if (wordSet.contains(candidate)) {
                            wordSet.remove(candidate); // Mark visited by removing
                            queue.offer(candidate);
                        }
                    }
                    chars[j] = original; // Restore
                }
            }
        }

        return 0;
    }

    // -----------------------------------------------------------------------
    // Helper: Check if two words differ by exactly one character
    // -----------------------------------------------------------------------
    private boolean diffByOne(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) diff++;
            if (diff > 1) return false;
        }
        return diff == 1;
    }

    // -----------------------------------------------------------------------
    // Variant: Return all shortest transformation sequences (LeetCode 126)
    // This is significantly harder — mention as a follow-up.
    // -----------------------------------------------------------------------
    // public List<List<String>> findLadders(...)
    // Approach: BFS to find shortest level, then DFS/backtracking to collect paths.
    // Not implemented here due to complexity, but worth knowing as a follow-up.

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        WordLadder solution = new WordLadder();

        System.out.println("=== Word Ladder ===");
        System.out.println();

        // Test 1: Classic example
        String begin1 = "hit";
        String end1 = "cog";
        List<String> words1 = Arrays.asList("hot", "dot", "dog", "lot", "log", "cog");
        int r1a = solution.ladderLength(begin1, end1, words1);
        int r1b = solution.ladderLengthSimple(begin1, end1, new ArrayList<>(words1));
        System.out.println("Test 1:");
        System.out.println("  beginWord: \"" + begin1 + "\", endWord: \"" + end1 + "\"");
        System.out.println("  wordList:  " + words1);
        System.out.println("  Pattern:  " + r1a + " (expected 5)");
        System.out.println("  Simple:   " + r1b + " (expected 5)");
        System.out.println("  Path: hit → hot → dot → dog → cog (length 5)");
        System.out.println();

        // Test 2: No path (endWord not in list)
        String begin2 = "hit";
        String end2 = "cog";
        List<String> words2 = Arrays.asList("hot", "dot", "dog", "lot", "log"); // no "cog"
        int r2a = solution.ladderLength(begin2, end2, words2);
        int r2b = solution.ladderLengthSimple(begin2, end2, new ArrayList<>(words2));
        System.out.println("Test 2: endWord not in list");
        System.out.println("  Pattern: " + r2a + " (expected 0)");
        System.out.println("  Simple:  " + r2b + " (expected 0)");
        System.out.println();

        // Test 3: beginWord == endWord (adjacent — one step)
        String begin3 = "hot";
        String end3 = "hot";
        List<String> words3 = Arrays.asList("hot");
        // begin == end is unusual; standard answer is 1 (already there)
        // Note: leetcode guarantees beginWord != endWord, so this is an edge case
        System.out.println("Test 3: beginWord same as endWord → special case");
        System.out.println();

        // Test 4: Direct one-step path
        String begin4 = "hot";
        String end4 = "dot";
        List<String> words4 = Arrays.asList("dot", "dog", "lot", "log");
        int r4a = solution.ladderLength(begin4, end4, words4);
        int r4b = solution.ladderLengthSimple(begin4, end4, new ArrayList<>(words4));
        System.out.println("Test 4: One step");
        System.out.println("  beginWord: \"" + begin4 + "\", endWord: \"" + end4 + "\"");
        System.out.println("  Pattern: " + r4a + " (expected 2)");
        System.out.println("  Simple:  " + r4b + " (expected 2)");
        System.out.println("  Path: hot → dot");
        System.out.println();

        // Test 5: Longer chain
        String begin5 = "qa";
        String end5 = "sq";
        List<String> words5 = Arrays.asList("si","go","se","cm","so","ph","mt","db","mb","sb",
            "kr","ln","tm","le","av","sm","ar","ci","ca","br","ti","ba","to","ra","fa","yo",
            "ow","sn","ya","cr","po","fe","ho","ma","re","or","rn","au","ur","rh","sr","tc",
            "lt","lo","as","fr","nb","yb","if","pb","ge","th","pm","rb","sh","co","ga","li",
            "ha","hz","no","bi","di","hi","qa","pi","os","uh","wm","an","me","mo","na","la",
            "st","er","sc","ne","mn","mi","am","ex","pt","io","be","fm","ta","tb","ni","mr",
            "pa","he","lr","sq","ye");
        int r5 = solution.ladderLength(begin5, end5, words5);
        System.out.println("Test 5: Long dictionary");
        System.out.println("  Pattern: " + r5 + " (expected 5)");
        System.out.println();

        // Demonstrate the pattern map for "hit"
        System.out.println("=== Pattern Map Illustration ===");
        System.out.println("For word \"hit\" (length 3):");
        System.out.println("  Position 0: '*it'  → all words matching *it");
        System.out.println("  Position 1: 'h*t'  → all words matching h*t");
        System.out.println("  Position 2: 'hi*'  → all words matching hi*");
        System.out.println();
        System.out.println("Example entries in pattern map:");
        System.out.println("  '*ot' → [hot, dot, lot]");
        System.out.println("  'h*t' → [hot]");
        System.out.println("  'ho*' → [hot]");
        System.out.println("  'd*t' → [dot]");
        System.out.println("  'do*' → [dot, dog]");
        System.out.println("  '*og' → [dog, log, cog]");
        System.out.println();
        System.out.println("BFS from 'hit':");
        System.out.println("  Level 1 (length=1): {hit}");
        System.out.println("  Level 2 (length=2): {hot}  (via h*t → hot)");
        System.out.println("  Level 3 (length=3): {dot, lot}  (via *ot)");
        System.out.println("  Level 4 (length=4): {dog, log}  (via do*, lo*)");
        System.out.println("  Level 5 (length=5): {cog} ← endWord found! return 5");

        System.out.println();
        System.out.println("=== Complexity ===");
        System.out.println("Pattern map approach:  O(M^2 * N) time, O(M^2 * N) space");
        System.out.println("  M = word length, N = wordList size");
        System.out.println("Simple approach:       O(N^2 * M) time, O(N) space");
        System.out.println("  Worse when N >> M, which is typical for word problems");
        System.out.println("Bidirectional BFS:     ~O(M^2 * N^0.5) — mention as optimization");
    }
}
