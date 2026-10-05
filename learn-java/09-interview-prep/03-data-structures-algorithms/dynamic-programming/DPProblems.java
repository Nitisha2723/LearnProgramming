import java.util.*;

/**
 * DYNAMIC PROGRAMMING — Core Problems with Detailed Explanations
 *
 * Topics covered:
 *   1. Fibonacci (memoization vs tabulation)
 *   2. Climbing Stairs (1D DP)
 *   3. Coin Change (unbounded knapsack variant)
 *   4. Longest Common Subsequence (2D DP)
 *   5. Longest Increasing Subsequence (patience sorting / DP)
 *   6. 0/1 Knapsack
 *   7. Maximum Subarray (Kadane's algorithm)
 *   8. House Robber (non-adjacent elements)
 *   9. Edit Distance (string DP)
 *  10. Unique Paths (grid DP)
 *
 * KEY DP FRAMEWORK (ask yourself every time):
 *   1. What is the subproblem? dp[i] means "answer for input of size i"
 *   2. What is the recurrence? How does dp[i] relate to smaller subproblems?
 *   3. What is the base case?
 *   4. What is the final answer? (dp[n], max of dp, etc.)
 *   5. Time/space complexity?
 */
public class DPProblems {

    // ==========================================================================
    // 1. FIBONACCI (baseline for understanding memoization vs tabulation)
    // ==========================================================================

    /**
     * Fibonacci: naive recursive — O(2^n) time.
     * Classic example of overlapping subproblems — fib(5) calls fib(4) and fib(3),
     * and fib(4) also calls fib(3). Without caching, we recompute the same values
     * exponentially many times.
     */
    public int fibNaive(int n) {
        if (n <= 1) return n;
        return fibNaive(n - 1) + fibNaive(n - 2);
    }

    /**
     * Fibonacci: top-down DP (memoization) — O(n) time, O(n) space.
     * Cache results to avoid recomputation. This is the most natural first
     * step: take the recursive solution and add a memo table.
     */
    public int fibMemo(int n) {
        return fibHelper(n, new int[n + 1]);
    }

    private int fibHelper(int n, int[] memo) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];
        memo[n] = fibHelper(n - 1, memo) + fibHelper(n - 2, memo);
        return memo[n];
    }

    /**
     * Fibonacci: bottom-up DP (tabulation) — O(n) time, O(n) space.
     * Fill the table from the smallest subproblems up to n.
     * Often slightly faster in practice (no recursion overhead).
     */
    public int fibTabulation(int n) {
        if (n <= 1) return n;
        int[] dp = new int[n + 1];
        dp[0] = 0;
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }

    /**
     * Fibonacci: space-optimized — O(n) time, O(1) space.
     * Since dp[i] only depends on dp[i-1] and dp[i-2], we only need two variables.
     */
    public int fibOptimized(int n) {
        if (n <= 1) return n;
        int prev2 = 0, prev1 = 1;
        for (int i = 2; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // ==========================================================================
    // 2. CLIMBING STAIRS (exactly 1 or 2 steps at a time)
    // ==========================================================================

    /**
     * Climbing Stairs: how many distinct ways to climb n stairs?
     *
     * Subproblem: dp[i] = number of ways to reach stair i
     * Recurrence: dp[i] = dp[i-1] + dp[i-2]
     *   - From stair i-1, take 1 step
     *   - From stair i-2, take 2 steps
     * Base cases: dp[1]=1 (one way: take one step), dp[2]=2 (two ways: 1+1 or 2)
     *
     * This IS Fibonacci: climbStairs(n) == fib(n+1)
     *
     * TIME: O(n), SPACE: O(1)
     */
    public int climbStairs(int n) {
        if (n <= 2) return n;
        int prev2 = 1, prev1 = 2;
        for (int i = 3; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // ==========================================================================
    // 3. COIN CHANGE (minimum coins to make amount — unbounded knapsack variant)
    // ==========================================================================

    /**
     * Coin Change: given coin denominations and a target amount,
     * find the minimum number of coins needed.
     *
     * Example: coins=[1,5,11], amount=15 → 3 coins (5+5+5, not 11+1+1+1+1)
     * Example: coins=[1,2,5], amount=11 → 3 coins (5+5+1)
     *
     * Subproblem: dp[i] = minimum coins to make amount i
     * Recurrence: dp[i] = min over all coins c where c <= i: (dp[i-c] + 1)
     *   "To make amount i, try each coin c. If we use coin c, we need
     *    dp[i-c] more coins for the remaining amount."
     * Base case: dp[0] = 0 (zero coins to make amount 0)
     * Init: dp[1..amount] = infinity (not yet reachable)
     * Answer: dp[amount] (or -1 if still infinity)
     *
     * TIME: O(amount * coins.length)
     * SPACE: O(amount)
     */
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1); // Use amount+1 as "infinity" (can never exceed this)
        dp[0] = 0;

        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (coin <= i) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }

        return dp[amount] > amount ? -1 : dp[amount];
    }

    /**
     * Coin Change 2: count the number of ways to make the amount (combinations, not permutations).
     * Example: coins=[1,2,5], amount=5 → 4 ways
     *
     * KEY: To count COMBINATIONS (not permutations), iterate coins in outer loop,
     * amounts in inner loop. This ensures each coin combination is counted once.
     *
     * Compare with Coin Change 1 where order doesn't matter because we take min — same result.
     * But for counting combinations vs permutations, loop order matters!
     *
     * TIME: O(amount * coins.length), SPACE: O(amount)
     */
    public int coinChangeWays(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        dp[0] = 1; // One way to make amount 0: use no coins

        for (int coin : coins) {          // Outer: coins (ensures combinations)
            for (int i = coin; i <= amount; i++) {
                dp[i] += dp[i - coin];
            }
        }

        return dp[amount];
    }

    // ==========================================================================
    // 4. LONGEST COMMON SUBSEQUENCE (2D DP)
    // ==========================================================================

    /**
     * LCS: find the length of the longest subsequence common to both strings.
     * A subsequence is derived by deleting characters without changing their order.
     *
     * Example: "abcde" and "ace" → LCS="ace", length=3
     * Example: "abc" and "abc" → LCS="abc", length=3
     * Example: "abc" and "def" → LCS="", length=0
     *
     * Subproblem: dp[i][j] = LCS of text1[0..i-1] and text2[0..j-1]
     * Recurrence:
     *   if text1[i-1] == text2[j-1]: dp[i][j] = dp[i-1][j-1] + 1  (characters match)
     *   else: dp[i][j] = max(dp[i-1][j], dp[i][j-1])               (skip one character)
     * Base case: dp[0][j] = dp[i][0] = 0 (LCS of empty string is 0)
     *
     * WHY THIS WORKS: If the last chars match, they must be in the LCS (greedy claim).
     * If they don't match, at most one of them is in the LCS, so we try skipping each.
     *
     * TIME: O(m*n), SPACE: O(m*n) — can be reduced to O(min(m,n)) with rolling array
     */
    public int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
    }

    /**
     * Space-optimized LCS — O(min(m,n)) space.
     * Since dp[i][j] only depends on dp[i-1][j-1], dp[i-1][j], dp[i][j-1],
     * we only need two rows (or one row + one variable for the diagonal).
     */
    public int lcsSpaceOptimized(String text1, String text2) {
        if (text1.length() < text2.length()) {
            String tmp = text1; text1 = text2; text2 = tmp; // shorter in inner loop
        }
        int m = text1.length(), n = text2.length();
        int[] dp = new int[n + 1];

        for (int i = 1; i <= m; i++) {
            int prev = 0; // This is dp[i-1][j-1]
            for (int j = 1; j <= n; j++) {
                int temp = dp[j]; // Save dp[i-1][j] before overwriting
                if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                    dp[j] = prev + 1;
                } else {
                    dp[j] = Math.max(dp[j], dp[j - 1]);
                }
                prev = temp;
            }
        }

        return dp[n];
    }

    // ==========================================================================
    // 5. LONGEST INCREASING SUBSEQUENCE
    // ==========================================================================

    /**
     * LIS: find the length of the longest strictly increasing subsequence.
     *
     * Example: [10,9,2,5,3,7,101,18] → 4 (2,3,7,101 or 2,5,7,101)
     *
     * APPROACH 1: DP — O(n^2)
     * Subproblem: dp[i] = length of LIS ending at index i
     * Recurrence: dp[i] = max over j < i where nums[j] < nums[i]: (dp[j] + 1)
     *   "To find the LIS ending at i, extend the LIS ending at any earlier j (if nums[j]<nums[i])"
     * Base case: dp[i] = 1 (LIS of just the element itself)
     * Answer: max of dp[]
     *
     * TIME: O(n^2), SPACE: O(n)
     */
    public int lengthOfLIS(int[] nums) {
        if (nums == null || nums.length == 0) return 0;

        int n = nums.length;
        int[] dp = new int[n];
        Arrays.fill(dp, 1); // Each element is an LIS of length 1

        int maxLen = 1;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (nums[j] < nums[i]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            maxLen = Math.max(maxLen, dp[i]);
        }

        return maxLen;
    }

    /**
     * LIS: O(n log n) using patience sorting (binary search).
     *
     * Maintain a "tails" array where tails[i] is the smallest tail element
     * of all increasing subsequences of length i+1.
     * This array is always sorted, so we can binary search.
     *
     * The length of tails[] at the end equals the LIS length.
     *
     * Note: tails[] does NOT store the actual LIS, just tail values.
     *
     * TIME: O(n log n), SPACE: O(n)
     */
    public int lengthOfLISOptimal(int[] nums) {
        if (nums == null || nums.length == 0) return 0;

        List<Integer> tails = new ArrayList<>();

        for (int num : nums) {
            // Binary search for the leftmost position where tails[pos] >= num
            int lo = 0, hi = tails.size();
            while (lo < hi) {
                int mid = lo + (hi - lo) / 2;
                if (tails.get(mid) < num) lo = mid + 1;
                else hi = mid;
            }

            if (lo == tails.size()) {
                tails.add(num); // num extends the longest subsequence
            } else {
                tails.set(lo, num); // Replace to maintain smallest possible tail
            }
        }

        return tails.size();
    }

    // ==========================================================================
    // 6. 0/1 KNAPSACK
    // ==========================================================================

    /**
     * 0/1 Knapsack: given items with weights and values, and a knapsack
     * with capacity W, find the maximum value you can fit.
     * Each item can be used at most once (0/1 choice).
     *
     * Example:
     *   weights=[2,3,4,5], values=[3,4,5,6], capacity=5
     *   Best: take item 0 (w=2,v=3) + item 1 (w=3,v=4) → value=7
     *
     * Subproblem: dp[i][w] = max value using first i items with capacity w
     * Recurrence:
     *   Skip item i: dp[i][w] = dp[i-1][w]
     *   Take item i: dp[i][w] = dp[i-1][w-weights[i]] + values[i]  (if weights[i] <= w)
     *   dp[i][w] = max of the two
     * Base case: dp[0][w] = 0 (no items), dp[i][0] = 0 (no capacity)
     *
     * TIME: O(n*W), SPACE: O(n*W) → reducible to O(W)
     *
     * KEY DIFFERENCE from Coin Change (unbounded):
     *   - Coin Change: each coin usable unlimited times → inner loop goes forward
     *   - Knapsack: each item used at most once → need previous row (or go backward)
     */
    public int knapsack(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= capacity; w++) {
                dp[i][w] = dp[i - 1][w]; // Don't take item i-1
                if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(dp[i][w],
                        dp[i - 1][w - weights[i - 1]] + values[i - 1]); // Take item i-1
                }
            }
        }

        return dp[n][capacity];
    }

    /**
     * Space-optimized 0/1 Knapsack — O(W) space.
     *
     * Use a single 1D array. Iterate weights BACKWARD to avoid using an item twice.
     * (If we went forward, dp[w-weight[i]] would already reflect item i being used.)
     *
     * TIME: O(n*W), SPACE: O(W)
     */
    public int knapsackOptimized(int[] weights, int[] values, int capacity) {
        int[] dp = new int[capacity + 1];

        for (int i = 0; i < weights.length; i++) {
            // Iterate backward: ensures each item is considered at most once
            for (int w = capacity; w >= weights[i]; w--) {
                dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
            }
        }

        return dp[capacity];
    }

    // ==========================================================================
    // 7. MAXIMUM SUBARRAY (Kadane's algorithm)
    // ==========================================================================

    /**
     * Maximum Subarray: find the contiguous subarray with the largest sum.
     *
     * Example: [-2,1,-3,4,-1,2,1,-5,4] → 6 (subarray [4,-1,2,1])
     *
     * KADANE'S ALGORITHM:
     * dp[i] = maximum subarray sum ending at index i
     * Recurrence: dp[i] = max(nums[i], dp[i-1] + nums[i])
     *   "Either start a new subarray at i, or extend the previous best."
     *   If dp[i-1] is negative, we're better off starting fresh at nums[i].
     *
     * KEY INSIGHT: "Should I extend or restart?"
     *   Extend if the running sum is positive (it helps).
     *   Restart if the running sum is negative (it hurts).
     *
     * SPACE OPTIMIZATION: We only need the previous dp value, so O(1) space.
     *
     * TIME: O(n), SPACE: O(1)
     */
    public int maxSubArray(int[] nums) {
        int maxSum = nums[0];
        int currentSum = nums[0];

        for (int i = 1; i < nums.length; i++) {
            // Extend or restart
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            maxSum = Math.max(maxSum, currentSum);
        }

        return maxSum;
    }

    /**
     * Maximum Subarray with indices: return the subarray itself.
     */
    public int[] maxSubArrayWithIndices(int[] nums) {
        int maxSum = nums[0], currentSum = nums[0];
        int start = 0, end = 0, tempStart = 0;

        for (int i = 1; i < nums.length; i++) {
            if (nums[i] > currentSum + nums[i]) {
                currentSum = nums[i];
                tempStart = i;
            } else {
                currentSum += nums[i];
            }

            if (currentSum > maxSum) {
                maxSum = currentSum;
                start = tempStart;
                end = i;
            }
        }

        return Arrays.copyOfRange(nums, start, end + 1);
    }

    // ==========================================================================
    // 8. HOUSE ROBBER (non-adjacent elements)
    // ==========================================================================

    /**
     * House Robber: rob houses without triggering alarm (no two adjacent houses).
     * Maximize total amount robbed.
     *
     * Example: [2,7,9,3,1] → 12 (rob houses 0,2,4: 2+9+1=12)
     * Example: [1,2,3,1] → 4 (rob houses 0,2: 1+3=4)
     *
     * Subproblem: dp[i] = max money robbing houses 0..i
     * Recurrence: dp[i] = max(dp[i-1], dp[i-2] + nums[i])
     *   "Either skip house i (dp[i-1]), or rob house i (dp[i-2] + nums[i])"
     * Base cases: dp[0]=nums[0], dp[1]=max(nums[0], nums[1])
     *
     * TIME: O(n), SPACE: O(1) (only need last two values)
     */
    public int rob(int[] nums) {
        if (nums.length == 1) return nums[0];

        int prev2 = nums[0];
        int prev1 = Math.max(nums[0], nums[1]);

        for (int i = 2; i < nums.length; i++) {
            int curr = Math.max(prev1, prev2 + nums[i]);
            prev2 = prev1;
            prev1 = curr;
        }

        return prev1;
    }

    /**
     * House Robber II: houses are in a circle (first and last are adjacent).
     * Either rob without the first house, or without the last house.
     * Take the max of the two.
     *
     * TIME: O(n), SPACE: O(1)
     */
    public int robCircular(int[] nums) {
        if (nums.length == 1) return nums[0];
        if (nums.length == 2) return Math.max(nums[0], nums[1]);

        return Math.max(
            robRange(nums, 0, nums.length - 2), // Exclude last house
            robRange(nums, 1, nums.length - 1)  // Exclude first house
        );
    }

    private int robRange(int[] nums, int lo, int hi) {
        int prev2 = nums[lo];
        int prev1 = Math.max(nums[lo], nums[lo + 1]);
        for (int i = lo + 2; i <= hi; i++) {
            int curr = Math.max(prev1, prev2 + nums[i]);
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // ==========================================================================
    // 9. EDIT DISTANCE (Levenshtein distance)
    // ==========================================================================

    /**
     * Edit Distance: minimum number of operations (insert, delete, replace)
     * to transform word1 into word2.
     *
     * Example: "horse" → "ros" = 3 operations
     *   horse → rorse (replace 'h' with 'r')
     *   rorse → rose  (delete 'r')
     *   rose  → ros   (delete 'e')
     *
     * Subproblem: dp[i][j] = edit distance between word1[0..i-1] and word2[0..j-1]
     * Recurrence:
     *   if word1[i-1] == word2[j-1]: dp[i][j] = dp[i-1][j-1]  (no operation needed)
     *   else: dp[i][j] = 1 + min(
     *     dp[i-1][j],   // delete from word1 (move i backward)
     *     dp[i][j-1],   // insert into word1 (move j backward)
     *     dp[i-1][j-1]  // replace character
     *   )
     * Base cases:
     *   dp[i][0] = i (delete all i characters from word1)
     *   dp[0][j] = j (insert j characters to get word2)
     *
     * TIME: O(m*n), SPACE: O(m*n) → reducible to O(min(m,n))
     */
    public int minDistance(String word1, String word2) {
        int m = word1.length(), n = word2.length();
        int[][] dp = new int[m + 1][n + 1];

        // Base cases
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // Characters match — no operation
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],   // Replace
                                   Math.min(dp[i - 1][j],         // Delete
                                            dp[i][j - 1]));        // Insert
                }
            }
        }

        return dp[m][n];
    }

    // ==========================================================================
    // 10. UNIQUE PATHS (grid DP)
    // ==========================================================================

    /**
     * Unique Paths: robot starts at (0,0) and must reach (m-1,n-1) in a grid.
     * Can only move right or down. How many unique paths?
     *
     * Example: m=3, n=7 → 28 paths
     *
     * Subproblem: dp[i][j] = number of unique paths to cell (i,j)
     * Recurrence: dp[i][j] = dp[i-1][j] + dp[i][j-1]  (came from above or from left)
     * Base case: dp[0][j] = 1 (only one way to reach top row: go right)
     *            dp[i][0] = 1 (only one way to reach left column: go down)
     *
     * Mathematical solution: C(m+n-2, m-1) — choose which m-1 of the m+n-2 steps go down.
     *
     * TIME: O(m*n), SPACE: O(m*n) → reducible to O(n)
     */
    public int uniquePaths(int m, int n) {
        int[] dp = new int[n];
        Arrays.fill(dp, 1); // Top row: all 1s

        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[j] += dp[j - 1]; // dp[j] was "from above"; dp[j-1] is "from left"
            }
        }

        return dp[n - 1];
    }

    /**
     * Unique Paths with Obstacles: 0=open, 1=blocked.
     *
     * Same recurrence, but if grid[i][j]==1, dp[i][j]=0.
     *
     * TIME: O(m*n), SPACE: O(n)
     */
    public int uniquePathsWithObstacles(int[][] obstacleGrid) {
        int m = obstacleGrid.length, n = obstacleGrid[0].length;
        int[] dp = new int[n];
        dp[0] = obstacleGrid[0][0] == 1 ? 0 : 1;

        // Initialize first row
        for (int j = 1; j < n; j++) {
            dp[j] = (obstacleGrid[0][j] == 1) ? 0 : dp[j - 1];
        }

        for (int i = 1; i < m; i++) {
            // Update first column
            dp[0] = (obstacleGrid[i][0] == 1) ? 0 : dp[0];

            for (int j = 1; j < n; j++) {
                if (obstacleGrid[i][j] == 1) {
                    dp[j] = 0; // Blocked cell
                } else {
                    dp[j] += dp[j - 1];
                }
            }
        }

        return dp[n - 1];
    }

    // ==========================================================================
    // TEST MAIN
    // ==========================================================================
    public static void main(String[] args) {
        DPProblems dp = new DPProblems();

        System.out.println("=== DYNAMIC PROGRAMMING PROBLEMS ===");
        System.out.println();

        // 1. Fibonacci
        System.out.println("--- 1. Fibonacci ---");
        for (int n : new int[]{0, 1, 5, 10}) {
            System.out.printf("fib(%d): naive=%d  memo=%d  tab=%d  opt=%d%n",
                n, dp.fibNaive(n), dp.fibMemo(n), dp.fibTabulation(n), dp.fibOptimized(n));
        }
        System.out.println();

        // 2. Climbing Stairs
        System.out.println("--- 2. Climbing Stairs ---");
        for (int n : new int[]{1, 2, 3, 4, 5}) {
            System.out.printf("climbStairs(%d) = %d%n", n, dp.climbStairs(n));
        }
        System.out.println();

        // 3. Coin Change
        System.out.println("--- 3. Coin Change ---");
        System.out.printf("coins=[1,5,11] amount=15: %d  (expected 3: 5+5+5)%n",
            dp.coinChange(new int[]{1, 5, 11}, 15));
        System.out.printf("coins=[1,2,5] amount=11: %d  (expected 3: 5+5+1)%n",
            dp.coinChange(new int[]{1, 2, 5}, 11));
        System.out.printf("coins=[2] amount=3: %d  (expected -1: impossible)%n",
            dp.coinChange(new int[]{2}, 3));
        System.out.printf("coinChangeWays([1,2,5], 5): %d  (expected 4)%n",
            dp.coinChangeWays(new int[]{1, 2, 5}, 5));
        System.out.println();

        // 4. LCS
        System.out.println("--- 4. Longest Common Subsequence ---");
        System.out.printf("LCS('abcde','ace') = %d  (expected 3)%n",
            dp.longestCommonSubsequence("abcde", "ace"));
        System.out.printf("LCS('abc','abc') = %d  (expected 3)%n",
            dp.longestCommonSubsequence("abc", "abc"));
        System.out.printf("LCS('abc','def') = %d  (expected 0)%n",
            dp.longestCommonSubsequence("abc", "def"));
        System.out.println();

        // 5. LIS
        System.out.println("--- 5. Longest Increasing Subsequence ---");
        int[] nums1 = {10, 9, 2, 5, 3, 7, 101, 18};
        System.out.printf("LIS(%s): O(n^2)=%d  O(nlogn)=%d  (expected 4)%n",
            Arrays.toString(nums1), dp.lengthOfLIS(nums1), dp.lengthOfLISOptimal(nums1));
        int[] nums2 = {0, 1, 0, 3, 2, 3};
        System.out.printf("LIS(%s): O(n^2)=%d  O(nlogn)=%d  (expected 4)%n",
            Arrays.toString(nums2), dp.lengthOfLIS(nums2), dp.lengthOfLISOptimal(nums2));
        System.out.println();

        // 6. Knapsack
        System.out.println("--- 6. 0/1 Knapsack ---");
        int[] weights = {2, 3, 4, 5};
        int[] values = {3, 4, 5, 6};
        System.out.printf("knapsack(w=%s, v=%s, cap=5): %d  (expected 7)%n",
            Arrays.toString(weights), Arrays.toString(values),
            dp.knapsack(weights, values, 5));
        System.out.printf("knapsackOptimized: %d%n",
            dp.knapsackOptimized(weights, values, 5));
        System.out.println();

        // 7. Maximum Subarray
        System.out.println("--- 7. Maximum Subarray (Kadane's) ---");
        int[] arr1 = {-2, 1, -3, 4, -1, 2, 1, -5, 4};
        System.out.printf("maxSubArray(%s) = %d  (expected 6)%n",
            Arrays.toString(arr1), dp.maxSubArray(arr1));
        System.out.printf("maxSubArrayWithIndices = %s%n",
            Arrays.toString(dp.maxSubArrayWithIndices(arr1)));
        System.out.println();

        // 8. House Robber
        System.out.println("--- 8. House Robber ---");
        int[] houses1 = {2, 7, 9, 3, 1};
        System.out.printf("rob(%s) = %d  (expected 12)%n",
            Arrays.toString(houses1), dp.rob(houses1));
        System.out.printf("robCircular(%s) = %d  (expected 11)%n",
            Arrays.toString(houses1), dp.robCircular(houses1));
        System.out.println();

        // 9. Edit Distance
        System.out.println("--- 9. Edit Distance ---");
        System.out.printf("editDistance('horse','ros') = %d  (expected 3)%n",
            dp.minDistance("horse", "ros"));
        System.out.printf("editDistance('intention','execution') = %d  (expected 5)%n",
            dp.minDistance("intention", "execution"));
        System.out.println();

        // 10. Unique Paths
        System.out.println("--- 10. Unique Paths ---");
        System.out.printf("uniquePaths(3,7) = %d  (expected 28)%n",
            dp.uniquePaths(3, 7));
        System.out.printf("uniquePaths(3,2) = %d  (expected 3)%n",
            dp.uniquePaths(3, 2));
        int[][] grid = {{0,0,0},{0,1,0},{0,0,0}};
        System.out.printf("uniquePathsWithObstacles(3x3 with center blocked) = %d  (expected 2)%n",
            dp.uniquePathsWithObstacles(grid));
        System.out.println();

        System.out.println("=== DP INTERVIEW CHEATSHEET ===");
        System.out.println();
        System.out.println("STEP 1: Define subproblem dp[i] or dp[i][j]");
        System.out.println("STEP 2: Write the recurrence relation");
        System.out.println("STEP 3: Identify base cases");
        System.out.println("STEP 4: Fill the table (bottom-up) or use memoization (top-down)");
        System.out.println("STEP 5: Extract the final answer from dp[]");
        System.out.println();
        System.out.println("PATTERNS:");
        System.out.println("  1D DP:          Fibonacci, Climbing Stairs, House Robber");
        System.out.println("  2D DP:          LCS, Edit Distance, Knapsack");
        System.out.println("  Interval DP:    Burst Balloons, Matrix Chain Multiplication");
        System.out.println("  Tree DP:        House Robber III, Binary Tree Max Path Sum");
        System.out.println("  Bitmask DP:     Traveling Salesman, Shortest Path (Bellman-Ford variant)");
        System.out.println();
        System.out.println("COMPLEXITY TABLE:");
        System.out.printf("  %-30s %-15s %-10s%n", "Problem", "Time", "Space");
        System.out.println("  " + "-".repeat(55));
        String[][] table = {
            {"Fibonacci",                 "O(n)",     "O(1)"},
            {"Climbing Stairs",           "O(n)",     "O(1)"},
            {"Coin Change",               "O(n*W)",   "O(W)"},
            {"LCS",                       "O(m*n)",   "O(min(m,n))"},
            {"LIS (DP)",                  "O(n^2)",   "O(n)"},
            {"LIS (patience sort)",       "O(n log n)","O(n)"},
            {"Knapsack",                  "O(n*W)",   "O(W)"},
            {"Maximum Subarray",          "O(n)",     "O(1)"},
            {"House Robber",              "O(n)",     "O(1)"},
            {"Edit Distance",             "O(m*n)",   "O(min(m,n))"},
            {"Unique Paths",              "O(m*n)",   "O(n)"},
        };
        for (String[] row : table) {
            System.out.printf("  %-30s %-15s %-10s%n", row[0], row[1], row[2]);
        }
    }
}
