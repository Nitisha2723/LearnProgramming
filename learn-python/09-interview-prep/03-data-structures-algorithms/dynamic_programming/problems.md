# Dynamic Programming Problems

8 dynamic programming problems with Python solutions.

| # | Problem | Technique | Complexity |
|---|---------|-----------|------------|
| 1 | Fibonacci Number | Iterative DP | O(n), O(1) |
| 2 | Climbing Stairs | Iterative DP (Fib) | O(n), O(1) |
| 3 | Coin Change | Bottom-up DP | O(n * amount) |
| 4 | Longest Common Subsequence | 2D DP | O(m*n) |
| 5 | 0/1 Knapsack | 2D DP | O(n * capacity) |
| 6 | Longest Increasing Subsequence | Patience sorting | O(n log n) |
| 7 | Word Break | DP with word set | O(n²) |
| 8 | House Robber | Linear DP | O(n), O(1) |

See `dp_problems.py` for complete implementations.

## DP Decision Framework

1. **Identify overlapping subproblems** — can you cache smaller answers?
2. **Define state** — what changes between subproblems? (index, remaining capacity, etc.)
3. **Write the recurrence** — how does dp[i] relate to dp[i-1], dp[i-2], etc.?
4. **Base cases** — what are the smallest valid states?
5. **Bottom-up vs. top-down** — both work; bottom-up is usually faster (no recursion overhead)
