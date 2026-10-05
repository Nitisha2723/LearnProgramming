# Dynamic Programming — Interview Prep

Eight foundational DP problems. For each problem, we present:
- The problem and examples
- The recurrence relation
- Memoization (top-down) approach
- Tabulation (bottom-up) approach
- Complexity analysis

---

## What is Dynamic Programming?

DP is an optimization technique for problems that have:
1. **Overlapping subproblems** — the same subproblem is solved multiple times
2. **Optimal substructure** — the optimal solution to the whole problem can be built from
   optimal solutions to subproblems

**Two implementation strategies:**
- **Memoization (top-down):** Recursive solution with a cache. Natural to write. Start from the answer and break it into subproblems.
- **Tabulation (bottom-up):** Iterative. Fill a table from base cases upward. Often more space-efficient.

---

## Problem 1: Fibonacci

**Why it matters:** The canonical example of DP. Shows the exponential → linear transformation clearly.

### Problem Statement
Return the nth Fibonacci number: F(0)=0, F(1)=1, F(n)=F(n-1)+F(n-2).

### Examples
```
F(0) = 0
F(1) = 1
F(6) = 8
F(10) = 55
```

### Recurrence Relation
```
F(n) = F(n-1) + F(n-2)
F(0) = 0, F(1) = 1
```

### Why Naive Recursion is O(2^n)
Without caching, `F(n)` calls `F(n-1)` and `F(n-2)`.
`F(n-1)` calls `F(n-2)` and `F(n-3)`, etc.
`F(n-2)` is computed TWICE (once from F(n) and once from F(n-1)).
The recursion tree has 2^n nodes — exponential.

```
                 F(5)
               /      \
           F(4)        F(3)
          /    \       /   \
        F(3)  F(2)  F(2)  F(1)   ← F(3) and F(2) recomputed!
```

### Memoization (Top-Down)
Store computed results in a cache. Before computing F(n), check if it's already cached.

```java
Map<Integer, Long> memo = new HashMap<>();
long fib(int n) {
    if (n <= 1) return n;
    if (memo.containsKey(n)) return memo.get(n);
    long result = fib(n-1) + fib(n-2);
    memo.put(n, result);
    return result;
}
```
- Time: O(n) — each value computed once
- Space: O(n) — cache + call stack

### Tabulation (Bottom-Up)
Build from F(0) and F(1) up to F(n).

```java
long[] dp = new long[n + 1];
dp[0] = 0; dp[1] = 1;
for (int i = 2; i <= n; i++) dp[i] = dp[i-1] + dp[i-2];
return dp[n];
```
- Time: O(n), Space: O(n)

**Space optimization:** Only the last two values are needed → O(1) space.

---

## Problem 2: Climbing Stairs

**Difficulty:** Easy

### Problem Statement
You are climbing a staircase with n steps. You can climb 1 or 2 steps at a time.
How many distinct ways are there to climb to the top?

### Examples
```
n=1 → 1 way:  [1]
n=2 → 2 ways: [1,1] or [2]
n=3 → 3 ways: [1,1,1] [1,2] [2,1]
n=4 → 5 ways
n=5 → 8 ways
```

### Recurrence Relation
```
dp[i] = dp[i-1] + dp[i-2]
dp[1] = 1, dp[2] = 2
```

**Why this is Fibonacci:** To reach step i, you either came from step i-1 (took 1 step)
or from step i-2 (took 2 steps). The total ways = ways to reach i-1 + ways to reach i-2.
This is exactly the Fibonacci recurrence (with different base cases).

### Tabulation
```java
int[] dp = new int[n + 1];
dp[1] = 1; dp[2] = 2;
for (int i = 3; i <= n; i++) dp[i] = dp[i-1] + dp[i-2];
return dp[n];
```
- Time: O(n), Space: O(n) → O(1) with two variables

---

## Problem 3: House Robber

**Difficulty:** Medium

### Problem Statement
You are a robber planning to rob houses along a street. Each house has a certain amount of money.
Adjacent houses have security systems — robbing two adjacent houses triggers the alarm.
Given an integer array `nums` representing money in each house, return the maximum amount
you can rob without triggering the alarm.

### Examples
```
Input: [1, 2, 3, 1]  → Output: 4  (rob houses 0 and 2: 1+3)
Input: [2, 7, 9, 3, 1] → Output: 12 (rob houses 0, 2, 4: 2+9+1)
```

### Recurrence Relation
```
dp[i] = max(dp[i-1],  dp[i-2] + nums[i])
          ↑                ↑
      skip house i    rob house i (can't have robbed i-1)

Base cases:
dp[0] = nums[0]
dp[1] = max(nums[0], nums[1])
```

**Meaning of dp[i]:** The maximum money robable from houses 0..i.

### Tabulation
```java
int[] dp = new int[n];
dp[0] = nums[0];
dp[1] = Math.max(nums[0], nums[1]);
for (int i = 2; i < n; i++)
    dp[i] = Math.max(dp[i-1], dp[i-2] + nums[i]);
return dp[n-1];
```
- Time: O(n), Space: O(n) → O(1) with two variables

---

## Problem 4: Coin Change

**Difficulty:** Medium

### Problem Statement
You are given an integer array `coins` representing coin denominations and an integer `amount`.
Return the minimum number of coins needed to make up `amount`.
Return -1 if the amount cannot be made up.

### Examples
```
coins=[1,2,5], amount=11 → 3 (5+5+1)
coins=[2],     amount=3  → -1
coins=[1],     amount=0  → 0
```

### Recurrence Relation
```
dp[i] = min over all coin c where c <= i of (dp[i - c] + 1)
dp[0] = 0
dp[i] = infinity (initially)
```

**Meaning of dp[i]:** Minimum coins to make amount i.
**Key insight:** For each amount i and each coin c, if we use coin c, we need dp[i-c] more coins
for the remaining amount. We take the minimum over all valid coins.

### Tabulation
```java
int[] dp = new int[amount + 1];
Arrays.fill(dp, amount + 1);   // "infinity" — can't use Integer.MAX_VALUE (overflow on +1)
dp[0] = 0;
for (int i = 1; i <= amount; i++)
    for (int coin : coins)
        if (coin <= i)
            dp[i] = Math.min(dp[i], dp[i - coin] + 1);
return dp[amount] > amount ? -1 : dp[amount];
```
- Time: O(amount * |coins|), Space: O(amount)

---

## Problem 5: Longest Common Subsequence (LCS)

**Difficulty:** Medium

### Problem Statement
Given two strings `text1` and `text2`, return the length of their longest common subsequence.
A subsequence is a sequence that appears in the same relative order but not necessarily contiguous.

### Examples
```
text1="abcde", text2="ace" → 3 ("ace")
text1="abc",   text2="abc" → 3
text1="abc",   text2="def" → 0
```

### Recurrence Relation
```
dp[i][j] = length of LCS of text1[0..i-1] and text2[0..j-1]

If text1[i-1] == text2[j-1]:
    dp[i][j] = dp[i-1][j-1] + 1      (extend the common subsequence)
Else:
    dp[i][j] = max(dp[i-1][j], dp[i][j-1])  (skip char from either string)

Base cases: dp[0][j] = dp[i][0] = 0  (empty string has LCS of 0)
```

### DP Table Example
```
     ""  a  c  e
""  [ 0  0  0  0 ]
a   [ 0  1  1  1 ]
b   [ 0  1  1  1 ]
c   [ 0  1  2  2 ]
d   [ 0  1  2  2 ]
e   [ 0  1  2  3 ]  ← LCS length is 3
```

### Tabulation
```java
int[][] dp = new int[m+1][n+1];
for (int i = 1; i <= m; i++)
    for (int j = 1; j <= n; j++)
        if (text1.charAt(i-1) == text2.charAt(j-1))
            dp[i][j] = dp[i-1][j-1] + 1;
        else
            dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
return dp[m][n];
```
- Time: O(m * n), Space: O(m * n) → O(n) with row-rolling

---

## Problem 6: 0/1 Knapsack

**Difficulty:** Medium

### Problem Statement
Given n items, each with a weight `w[i]` and value `v[i]`, and a knapsack of capacity `W`,
find the maximum total value achievable without exceeding the weight capacity.
Each item can be taken at most once (0/1 knapsack).

### Examples
```
weights=[2,3,4,5], values=[3,4,5,6], W=5
→ 7 (take items with weight 2 and 3, values 3+4=7)
```

### Recurrence Relation
```
dp[i][w] = max value using first i items with capacity w

If w[i] > w:   dp[i][w] = dp[i-1][w]              (can't take item i)
Else:           dp[i][w] = max(dp[i-1][w],
                               dp[i-1][w - w[i]] + v[i])  (skip or take)

Base case: dp[0][w] = 0 for all w (no items → no value)
```

### DP Table
```
items={w2,v3},{w3,v4},{w4,v5},{w5,v6}, W=5

     w=0  1  2  3  4  5
i=0 [ 0   0  0  0  0  0 ]
i=1 [ 0   0  3  3  3  3 ]  (can take item 1 for w>=2)
i=2 [ 0   0  3  4  4  7 ]  (can take item 2 for w>=3; 3+4=7 at w=5)
i=3 [ 0   0  3  4  5  7 ]
i=4 [ 0   0  3  4  5  7 ]  → answer = 7
```

- Time: O(n * W), Space: O(n * W) → O(W) with single-row optimization

---

## Problem 7: Word Break

**Difficulty:** Medium

### Problem Statement
Given a string `s` and a dictionary of strings `wordDict`, return true if `s` can be
segmented into a space-separated sequence of dictionary words.

### Examples
```
s="leetcode", wordDict=["leet","code"] → true
s="applepenapple", wordDict=["apple","pen"] → true
s="catsandog", wordDict=["cats","dog","sand","and","cat"] → false
```

### Recurrence Relation
```
dp[i] = true if s[0..i-1] can be segmented using wordDict

dp[0] = true (empty string is trivially segmentable)
dp[i] = OR over all j < i of (dp[j] AND s[j..i-1] is in wordDict)
```

**Meaning:** dp[i] is true if there's some split point j where the prefix s[0..j-1]
is segmentable (dp[j] is true) AND the remaining s[j..i-1] is a dictionary word.

### Tabulation
```java
boolean[] dp = new boolean[n + 1];
dp[0] = true;
Set<String> dict = new HashSet<>(wordDict);
for (int i = 1; i <= n; i++)
    for (int j = 0; j < i; j++)
        if (dp[j] && dict.contains(s.substring(j, i))) {
            dp[i] = true;
            break;
        }
return dp[n];
```
- Time: O(n² * L) where L is max word length (substring check)
- Space: O(n + dict size)

---

## Problem 8: Longest Increasing Subsequence (LIS)

**Difficulty:** Medium

### Problem Statement
Given an integer array `nums`, return the length of the longest strictly increasing subsequence.

### Examples
```
nums=[10,9,2,5,3,7,101,18] → 4 (2,3,7,101 or 2,5,7,101)
nums=[0,1,0,3,2,3]         → 4
nums=[7,7,7,7,7]           → 1
```

### Approach 1 — O(n²) DP
```
dp[i] = length of LIS ending at index i

dp[i] = 1 + max(dp[j]) for all j < i where nums[j] < nums[i]
dp[i] = 1 if no such j exists

Answer = max(dp[i]) for all i
```

### Approach 2 — O(n log n) with Patience Sorting + Binary Search
Maintain a `tails` array where `tails[i]` is the smallest tail element of all increasing
subsequences of length i+1.

For each element x:
- If x > tails.last(): append x (extending the longest subsequence)
- Else: binary search in tails for the leftmost element >= x and replace it with x

The length of `tails` at the end is the LIS length. (Note: `tails` is NOT the actual LIS.)

**Why does replacing work?** We're tracking the smallest possible tail for each length.
Replacing a larger tail with a smaller one doesn't change the count of subsequences of
that length but enables longer future subsequences.

```java
List<Integer> tails = new ArrayList<>();
for (int num : nums) {
    int pos = Collections.binarySearch(tails, num);
    if (pos < 0) pos = -(pos + 1);  // insertion point
    if (pos == tails.size()) tails.add(num);
    else tails.set(pos, num);
}
return tails.size();
```
- Time: O(n log n), Space: O(n)
