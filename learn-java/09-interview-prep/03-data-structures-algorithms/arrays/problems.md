# Array Problems — Interview Prep

Ten classic array problems that appear frequently in technical interviews.
Each problem includes brute-force and optimal approaches with full complexity analysis.

---

## Problem 1: Two Sum

**Difficulty:** Easy

### Problem Statement
Given an array of integers `nums` and an integer `target`, return the indices of the two numbers
that add up to `target`. You may assume that exactly one solution exists, and you may not use
the same element twice.

### Examples
```
Input:  nums = [2, 7, 11, 15], target = 9
Output: [0, 1]
Explanation: nums[0] + nums[1] = 2 + 7 = 9

Input:  nums = [3, 2, 4], target = 6
Output: [1, 2]

Input:  nums = [3, 3], target = 6
Output: [0, 1]
```

### Constraints
- 2 <= nums.length <= 10^4
- -10^9 <= nums[i] <= 10^9
- Exactly one valid answer exists

### Brute Force
Check every pair of indices (i, j) where i < j. If nums[i] + nums[j] == target, return [i, j].
- Time: O(n²)
- Space: O(1)

### Optimal Approach — HashMap
As you iterate through the array, for each element `nums[i]`:
1. Calculate `complement = target - nums[i]`
2. Check if `complement` already exists in the HashMap
3. If yes, return [index_of_complement, i]
4. If no, store `nums[i] -> i` in the HashMap and continue

**Why this works:** Instead of searching for the complement by scanning the rest of the array,
we pre-store values we've already seen. When we arrive at the second number, we instantly
find its partner in O(1).

- Time: O(n) — one pass through the array, each HashMap operation is O(1) average
- Space: O(n) — HashMap stores at most n entries

### Edge Cases
- Array of exactly 2 elements (must be the answer)
- Negative numbers (complement can be negative — HashMap handles this)
- Duplicate values as in [3, 3] with target 6 — works because we store index, not value

---

## Problem 2: Maximum Subarray (Kadane's Algorithm)

**Difficulty:** Medium

### Problem Statement
Given an integer array `nums`, find the contiguous subarray with the largest sum and return its sum.

### Examples
```
Input:  nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]
Output: 6
Explanation: Subarray [4, -1, 2, 1] has the largest sum = 6

Input:  nums = [1]
Output: 1

Input:  nums = [5, 4, -1, 7, 8]
Output: 23
```

### Constraints
- 1 <= nums.length <= 10^5
- -10^4 <= nums[i] <= 10^4

### Brute Force
Try every subarray starting at index i and ending at index j. Track the maximum sum seen.
- Time: O(n²) or O(n³) depending on implementation
- Space: O(1)

### Optimal Approach — Kadane's Algorithm
**DP Recurrence:** `maxEndingHere[i] = max(nums[i], maxEndingHere[i-1] + nums[i])`

Meaning: The best subarray ending at index i is either:
- Start fresh at index i (just take nums[i] alone)
- Extend the best subarray that ended at i-1 by including nums[i]

We choose the larger of the two.

**Step by step:**
1. Initialize `currentMax = nums[0]` and `globalMax = nums[0]`
2. For each element from index 1 onward:
   - `currentMax = max(nums[i], currentMax + nums[i])`
   - `globalMax = max(globalMax, currentMax)`
3. Return `globalMax`

**Why "start fresh" when currentMax + nums[i] < nums[i]?**
That means currentMax is negative. A negative prefix only makes the sum smaller,
so we discard it and start a new subarray from this element.

- Time: O(n) — single pass
- Space: O(1) — only two variables needed

### Edge Cases
- All negative numbers: answer is the largest (least negative) single element
- Single element: return it directly
- All same values: answer is n * value

---

## Problem 3: Rotate Array

**Difficulty:** Medium

### Problem Statement
Given an integer array `nums`, rotate the array to the right by `k` steps.
Modify the array in-place.

### Examples
```
Input:  nums = [1, 2, 3, 4, 5, 6, 7], k = 3
Output: [5, 6, 7, 1, 2, 3, 4]

Input:  nums = [-1, -100, 3, 99], k = 2
Output: [3, 99, -1, -100]
```

### Constraints
- 1 <= nums.length <= 10^5
- -2^31 <= nums[i] <= 2^31 - 1
- 0 <= k <= 10^5

### Brute Force
Rotate the array one step k times (each step moves the last element to the front).
- Time: O(n*k)
- Space: O(1)

Alternatively, copy the rotated result into a new array.
- Time: O(n)
- Space: O(n)

### Optimal Approach — Three-Reverse Trick
Rotating right by k is equivalent to:
1. Reverse the entire array
2. Reverse the first k elements
3. Reverse the remaining n-k elements

**Example with [1,2,3,4,5,6,7], k=3:**
```
Original:           [1, 2, 3, 4, 5, 6, 7]
Reverse all:        [7, 6, 5, 4, 3, 2, 1]
Reverse first k=3:  [5, 6, 7, 4, 3, 2, 1]
Reverse last n-k=4: [5, 6, 7, 1, 2, 3, 4]  ✓
```

**Important:** Handle k = k % n to avoid unnecessary full rotations when k >= n.

- Time: O(n) — each element is reversed at most twice
- Space: O(1) — in-place with only index variables

### Edge Cases
- k = 0: no rotation needed (handled automatically after k % n)
- k = n: full rotation, array unchanged (k % n = 0)
- k > n: k % n gives the effective rotation amount

---

## Problem 4: Find All Duplicates in an Array

**Difficulty:** Medium

### Problem Statement
Given an integer array `nums` of length n where all integers are in the range [1, n] and
each integer appears once or twice, return an array of all integers that appear twice.
Must run in O(n) time and use O(1) extra space (excluding the output).

### Examples
```
Input:  nums = [4, 3, 2, 7, 8, 2, 3, 1]
Output: [2, 3]

Input:  nums = [1, 1, 2]
Output: [1]

Input:  nums = [1]
Output: []
```

### Constraints
- n == nums.length
- 1 <= n <= 10^5
- 1 <= nums[i] <= n
- Each value appears once or twice

### Brute Force
Use a HashSet to track seen values. Add to result list when a duplicate is found.
- Time: O(n)
- Space: O(n)

### Optimal Approach — Index Marking (Negation)
Since values are in [1, n], each value can serve as an index into the array.
Use the sign of `nums[abs(nums[i]) - 1]` as a "visited" flag:

1. For each element `nums[i]`, compute `idx = abs(nums[i]) - 1`
2. If `nums[idx]` is negative, value `idx+1` has been seen before → it's a duplicate
3. If `nums[idx]` is positive, negate it: `nums[idx] = -nums[idx]` (mark as seen)

**Why it works:** Values are in [1, n], so `value - 1` gives a valid array index.
We "encode" visited information in the sign bit of the element at that index position.

**Example with [4,3,2,7,8,2,3,1]:**
```
i=0, val=4, idx=3: nums[3]=7 > 0, negate → nums[3]=-7
i=1, val=3, idx=2: nums[2]=2 > 0, negate → nums[2]=-2
i=2, val=abs(-2)=2, idx=1: nums[1]=3 > 0, negate → nums[1]=-3
i=3, val=abs(-7)=7, idx=6: nums[6]=3 > 0, negate → nums[6]=-3
i=4, val=8, idx=7: nums[7]=1 > 0, negate → nums[7]=-1
i=5, val=2, idx=1: nums[1]=-3 < 0 → 2 is a DUPLICATE
i=6, val=abs(-3)=3, idx=2: nums[2]=-2 < 0 → 3 is a DUPLICATE
i=7, val=abs(-1)=1, idx=0: nums[0]=4 > 0, negate → nums[0]=-4
```

- Time: O(n)
- Space: O(1) — we reuse the input array for marking

### Edge Cases
- Single element array: no duplicates possible
- All duplicates: every element appears twice
- Negative numbers: not possible given constraints (values are in [1, n])

---

## Problem 5: Merge Sorted Arrays

**Difficulty:** Easy

### Problem Statement
You are given two integer arrays `nums1` and `nums2`, sorted in non-decreasing order, and
two integers `m` and `n` representing the number of valid elements in each array.
Merge `nums2` into `nums1` as one sorted array in-place.
`nums1` has length `m + n`; the last `n` slots are zeros.

### Examples
```
Input:  nums1 = [1,2,3,0,0,0], m = 3, nums2 = [2,5,6], n = 3
Output: [1,2,2,3,5,6]

Input:  nums1 = [1], m = 1, nums2 = [], n = 0
Output: [1]

Input:  nums1 = [0], m = 0, nums2 = [1], n = 1
Output: [1]
```

### Constraints
- nums1.length == m + n
- nums2.length == n
- 0 <= m, n <= 200
- -10^9 <= nums1[i], nums2[j] <= 10^9

### Brute Force
Copy nums2 into the end of nums1, then sort nums1.
- Time: O((m+n) log(m+n))
- Space: O(1) ignoring sort's stack

### Optimal Approach — Two Pointers from the Back
Merging from the front would overwrite values in nums1. Instead, fill from the back:

1. Use pointer `p1 = m - 1` (last valid element of nums1)
2. Use pointer `p2 = n - 1` (last element of nums2)
3. Use pointer `p = m + n - 1` (last position to fill)
4. While both pointers are valid:
   - Compare nums1[p1] and nums2[p2]
   - Place the larger at nums1[p]
   - Decrement the corresponding pointer and p
5. If p2 >= 0 after the loop, copy remaining nums2 elements

**Why from the back?** nums1 has extra space at the end. Writing from the back ensures
we never overwrite a nums1 element we haven't compared yet.

- Time: O(m + n)
- Space: O(1) — in-place

### Edge Cases
- m = 0: nums1 contains only zeros, just copy nums2 (loop handles this)
- n = 0: nums1 already sorted, nothing to do
- All nums2 elements are smaller than all nums1 elements

---

## Problem 6: Binary Search

**Difficulty:** Easy

### Problem Statement
Given an array of integers `nums` sorted in ascending order and an integer `target`,
return the index of `target` if it exists in the array. Otherwise return -1.
Must be O(log n).

### Examples
```
Input:  nums = [-1, 0, 3, 5, 9, 12], target = 9
Output: 4

Input:  nums = [-1, 0, 3, 5, 9, 12], target = 2
Output: -1
```

### Constraints
- 1 <= nums.length <= 10^4
- -10^4 < nums[i], target < 10^4
- All integers in nums are unique and sorted

### Brute Force
Linear scan from left to right.
- Time: O(n)
- Space: O(1)

### Optimal Approach — Binary Search
1. Set `left = 0`, `right = nums.length - 1`
2. While `left <= right`:
   - Compute `mid = left + (right - left) / 2`  ← important: avoids integer overflow
   - If `nums[mid] == target`, return `mid`
   - If `nums[mid] < target`, search right half: `left = mid + 1`
   - If `nums[mid] > target`, search left half: `right = mid - 1`
3. Return -1 (not found)

**Duplicates variant — Find First Occurrence:**
When `nums[mid] == target`, don't return immediately. Instead set `right = mid - 1`
to continue searching the left half. After the loop, check if `nums[left] == target`.

**Duplicates variant — Find Last Occurrence:**
When `nums[mid] == target`, set `left = mid + 1` to search right half.
After the loop, check if `nums[right] == target`.

- Time: O(log n) — halves the search space each iteration
- Space: O(1)

### Edge Cases
- Single element array
- Target is first or last element
- Target not in array (return -1)
- Array with duplicates (standard binary search returns any match)

---

## Problem 7: Product of Array Except Self

**Difficulty:** Medium

### Problem Statement
Given an integer array `nums`, return an array `answer` such that `answer[i]` is equal to
the product of all elements in `nums` except `nums[i]`.
Must run in O(n) time. Do not use the division operation.

### Examples
```
Input:  nums = [1, 2, 3, 4]
Output: [24, 12, 8, 6]

Input:  nums = [-1, 1, 0, -3, 3]
Output: [0, 0, 9, 0, 0]
```

### Constraints
- 2 <= nums.length <= 10^5
- -30 <= nums[i] <= 30
- The product of any prefix or suffix fits in a 32-bit integer

### Brute Force
For each index i, multiply all elements except nums[i].
- Time: O(n²)
- Space: O(1) excluding output

### Optimal Approach — Left Pass / Right Pass
`answer[i] = (product of all elements to the LEFT of i) * (product of all elements to the RIGHT of i)`

**Two-pass approach:**
1. **Left pass:** `answer[i]` = product of nums[0..i-1]
   - answer[0] = 1 (no elements to the left)
   - answer[i] = answer[i-1] * nums[i-1]
2. **Right pass:** Multiply `answer[i]` by the running right product
   - Start with `rightProduct = 1`
   - Traverse from right to left: `answer[i] *= rightProduct`, then `rightProduct *= nums[i]`

**Example with [1,2,3,4]:**
```
Left pass:  [1, 1, 2, 6]      (products of elements to the left)
Right pass: [24, 12, 8, 6]    (multiply each by products to the right)
```

- Time: O(n) — two passes
- Space: O(1) extra space (the output array doesn't count as extra space)

### Edge Cases
- Array contains a zero: all positions except the zero position become 0
- Array contains two zeros: entire output is 0
- Negative numbers: product sign is handled correctly by multiplication

---

## Problem 8: Find the Missing Number

**Difficulty:** Easy

### Problem Statement
Given an array `nums` containing n distinct numbers in the range [0, n], return the one
number that is missing from the array.

### Examples
```
Input:  nums = [3, 0, 1]
Output: 2

Input:  nums = [0, 1]
Output: 2

Input:  nums = [9, 6, 4, 2, 3, 5, 7, 0, 1]
Output: 8
```

### Constraints
- n == nums.length
- 1 <= n <= 10^4
- 0 <= nums[i] <= n
- All numbers in nums are unique

### Brute Force
Sort the array and check where consecutive elements differ by more than 1.
- Time: O(n log n)
- Space: O(1)

### Optimal Approach 1 — Math (Gauss Formula)
The sum of 0..n is `n*(n+1)/2`. The missing number equals this expected sum minus the actual sum.

```
missingNumber = n*(n+1)/2 - sum(nums)
```

- Time: O(n)
- Space: O(1)

**Caveat:** For very large n, `n*(n+1)/2` could overflow an int. Use `long` arithmetic.

### Optimal Approach 2 — XOR
XOR all numbers from 0 to n with all elements of nums.
Each number that appears in both will cancel out (x XOR x = 0).
The missing number XORs with 0 and remains.

```java
int xor = 0;
for (int i = 0; i <= n; i++) xor ^= i;
for (int num : nums) xor ^= num;
return xor;  // the missing number
```

- Time: O(n)
- Space: O(1)

**XOR approach avoids any overflow risk.**

### Edge Cases
- Missing 0: the first element should be 0 but isn't
- Missing n: the largest value is absent
- n = 1: array has one element, either 0 or 1 is missing

---

## Problem 9: Move Zeros

**Difficulty:** Easy

### Problem Statement
Given an integer array `nums`, move all 0s to the end while maintaining the relative order
of non-zero elements. Modify the array in-place without making a copy.

### Examples
```
Input:  nums = [0, 1, 0, 3, 12]
Output: [1, 3, 12, 0, 0]

Input:  nums = [0]
Output: [0]
```

### Constraints
- 1 <= nums.length <= 10^4
- -2^31 <= nums[i] <= 2^31 - 1

### Brute Force
Copy all non-zero elements to a new array, then fill remaining positions with 0.
- Time: O(n)
- Space: O(n)

### Optimal Approach — Two Pointers
Use a `writePos` pointer that tracks where the next non-zero element should go:
1. Initialize `writePos = 0`
2. For each element: if non-zero, write it to `nums[writePos]` and increment `writePos`
3. After the loop, fill `nums[writePos..n-1]` with zeros

**Why this works:** writePos only advances when we place a non-zero value, so it always
points to the next "slot" for a non-zero element. All positions before writePos contain
the non-zero values in their original order.

**Swap variant (fewer writes):**
Instead of writing and then zeroing, swap non-zero elements with the writePos element.
This avoids the second pass but performs more writes overall when there are few zeros.

- Time: O(n)
- Space: O(1)

### Edge Cases
- No zeros: array unchanged (writePos reaches n, nothing is overwritten)
- All zeros: writePos stays 0, fills entire array with zeros
- Single element: handled correctly

---

## Problem 10: Container With Most Water

**Difficulty:** Medium

### Problem Statement
You are given an integer array `height` of length n. There are n vertical lines, where
the two endpoints of the i-th line are at (i, 0) and (i, height[i]).
Find two lines that together with the x-axis form a container that holds the most water.
Return the maximum amount of water a container can store.

### Examples
```
Input:  height = [1, 8, 6, 2, 5, 4, 8, 3, 7]
Output: 49
Explanation: Lines at index 1 (height 8) and index 8 (height 7) form the container.
             Width = 8 - 1 = 7, height = min(8, 7) = 7, area = 49.

Input:  height = [1, 1]
Output: 1
```

### Constraints
- n == height.length
- 2 <= n <= 10^5
- 0 <= height[i] <= 10^4

### Brute Force
Check every pair (i, j). Area = (j - i) * min(height[i], height[j]).
- Time: O(n²)
- Space: O(1)

### Optimal Approach — Two Pointers (Greedy)
Start with the widest possible container (left=0, right=n-1).
At each step, move the pointer on the side with the **shorter** line inward.

**Why move the shorter side?**
- The current area is limited by the shorter line.
- Moving the taller side inward can only reduce width while the height stays limited
  by the same (or shorter) line. The area can only decrease or stay the same.
- Moving the shorter side inward might find a taller line, possibly increasing the area.
- Therefore, we can safely discard the current shorter line and never miss the optimal pair.

**Step by step with [1, 8, 6, 2, 5, 4, 8, 3, 7]:**
```
left=0(h=1), right=8(h=7): area=1*7=7. Move left (shorter).
left=1(h=8), right=8(h=7): area=7*7=49. Move right (shorter).
left=1(h=8), right=7(h=3): area=3*6=18. Move right.
... continue until left >= right
```

- Time: O(n) — each pointer moves at most n times total
- Space: O(1)

### Edge Cases
- All lines same height: widest container wins
- Single tall line surrounded by short lines: greedy handles this
- Two elements: only one possible container
