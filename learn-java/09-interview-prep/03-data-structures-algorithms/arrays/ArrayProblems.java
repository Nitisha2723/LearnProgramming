import java.util.*;

/**
 * Solutions to 10 classic array interview problems.
 * Each method includes time/space complexity and algorithm explanation.
 *
 * Problems covered:
 *  1. Two Sum                       — HashMap O(n)
 *  2. Maximum Subarray              — Kadane's Algorithm O(n)
 *  3. Rotate Array                  — Three-reverse trick O(n), O(1) space
 *  4. Find All Duplicates           — Index marking O(n), O(1) space
 *  5. Merge Sorted Arrays           — Two pointers from back O(m+n)
 *  6. Binary Search                 — O(log n)
 *  7. Product of Array Except Self  — Left/right pass O(n), O(1) extra
 *  8. Find Missing Number           — XOR approach O(n), O(1)
 *  9. Move Zeros                    — Two pointers O(n), O(1)
 * 10. Container With Most Water     — Two pointers greedy O(n)
 */
public class ArrayProblems {

    // =========================================================================
    // Problem 1: Two Sum
    // =========================================================================

    /**
     * Given an array of integers and a target, return indices of the two numbers
     * that add up to target.
     *
     * Algorithm (HashMap):
     *   For each element nums[i], compute complement = target - nums[i].
     *   If complement is already in the map, we found our pair.
     *   Otherwise, store nums[i] -> i in the map for future lookups.
     *
     * Time:  O(n) — single pass, each HashMap op is O(1) average
     * Space: O(n) — HashMap stores at most n entries
     *
     * @param nums   input array of integers
     * @param target desired sum
     * @return indices [i, j] such that nums[i] + nums[j] == target
     */
    public int[] twoSum(int[] nums, int target) {
        // Maps each value to its index for O(1) complement lookup
        Map<Integer, Integer> seen = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];

            if (seen.containsKey(complement)) {
                // Found: complement was seen at seen.get(complement), current at i
                return new int[]{seen.get(complement), i};
            }

            // Store this value mapped to its index
            seen.put(nums[i], i);
        }

        // Problem guarantees exactly one solution; this line is unreachable
        return new int[]{-1, -1};
    }

    // =========================================================================
    // Problem 2: Maximum Subarray (Kadane's Algorithm)
    // =========================================================================

    /**
     * Find the contiguous subarray with the largest sum.
     *
     * Kadane's Algorithm — DP Recurrence:
     *   maxEndingHere[i] = max(nums[i], maxEndingHere[i-1] + nums[i])
     *
     *   At each position i, the maximum subarray ending here is either:
     *   a) Just nums[i] alone (start a new subarray here)
     *   b) Extend the best subarray ending at i-1 by adding nums[i]
     *
     *   We choose the larger option. If currentMax becomes negative, it means
     *   any subarray ending here would only hurt a future subarray — so we
     *   "reset" by starting fresh at the next element (which is what taking
     *   max(nums[i], currentMax + nums[i]) achieves when currentMax < 0).
     *
     * Time:  O(n) — single pass
     * Space: O(1) — only two variables needed
     *
     * @param nums input array (may contain negative numbers)
     * @return maximum subarray sum
     */
    public int maxSubArray(int[] nums) {
        // currentMax: best sum of any subarray ending at this index
        int currentMax = nums[0];
        // globalMax: best sum seen across all positions
        int globalMax = nums[0];

        for (int i = 1; i < nums.length; i++) {
            // Either extend previous subarray or start a new one here
            // If currentMax < 0, starting fresh at nums[i] is always better
            currentMax = Math.max(nums[i], currentMax + nums[i]);

            // Update global best if we found a better subarray
            globalMax = Math.max(globalMax, currentMax);
        }

        return globalMax;
    }

    // =========================================================================
    // Problem 3: Rotate Array
    // =========================================================================

    /**
     * Rotate array to the right by k steps, in-place.
     *
     * Three-Reverse Trick:
     *   Rotating right by k is equivalent to:
     *   1. Reverse the entire array
     *   2. Reverse the first k elements
     *   3. Reverse the remaining n-k elements
     *
     *   Example: [1,2,3,4,5,6,7], k=3
     *   Reverse all:        [7,6,5,4,3,2,1]
     *   Reverse first 3:    [5,6,7,4,3,2,1]
     *   Reverse last 4:     [5,6,7,1,2,3,4] ✓
     *
     * Time:  O(n) — each element reversed at most twice
     * Space: O(1) — in-place, only index variables
     *
     * @param nums input array to rotate in-place
     * @param k    number of steps to rotate right
     */
    public void rotate(int[] nums, int k) {
        int n = nums.length;
        // Normalize k: rotating n times is a no-op
        k = k % n;
        if (k == 0) return;

        // Step 1: Reverse entire array
        reverse(nums, 0, n - 1);
        // Step 2: Reverse first k elements (they were the last k)
        reverse(nums, 0, k - 1);
        // Step 3: Reverse remaining n-k elements
        reverse(nums, k, n - 1);
    }

    /**
     * Reverse a portion of the array between indices start and end (inclusive).
     *
     * @param nums  the array
     * @param start starting index (inclusive)
     * @param end   ending index (inclusive)
     */
    private void reverse(int[] nums, int start, int end) {
        while (start < end) {
            int temp = nums[start];
            nums[start] = nums[end];
            nums[end] = temp;
            start++;
            end--;
        }
    }

    // =========================================================================
    // Problem 4: Find All Duplicates in an Array
    // =========================================================================

    /**
     * Find all integers that appear twice in an array where values are in [1, n].
     *
     * Index Marking (Negation) Technique:
     *   Since all values are in [1, n], each value v can be used as an index: v-1.
     *   We use the sign of nums[v-1] as a "visited" flag:
     *   - If nums[v-1] > 0: first time seeing v, negate nums[v-1] to mark "visited"
     *   - If nums[v-1] < 0: v has been seen before → it's a duplicate
     *
     *   We use abs() when reading values because we may have already negated them.
     *
     * Time:  O(n) — single pass
     * Space: O(1) — modifies input array temporarily; output list doesn't count
     *
     * @param nums array of length n with values in [1, n], each appearing 1 or 2 times
     * @return list of all values that appear exactly twice
     */
    public List<Integer> findDuplicates(int[] nums) {
        List<Integer> duplicates = new ArrayList<>();

        for (int i = 0; i < nums.length; i++) {
            // Get the absolute value (element might already be negated from earlier)
            int value = Math.abs(nums[i]);
            // This value maps to index value-1
            int idx = value - 1;

            if (nums[idx] < 0) {
                // nums[idx] already negated → we've seen 'value' before → duplicate!
                duplicates.add(value);
            } else {
                // First time seeing 'value': mark as visited by negating
                nums[idx] = -nums[idx];
            }
        }

        return duplicates;
    }

    // =========================================================================
    // Problem 5: Merge Sorted Arrays
    // =========================================================================

    /**
     * Merge nums2 into nums1 in-place. nums1 has length m+n; last n slots are zeros.
     *
     * Two Pointers from the Back:
     *   Filling from front would overwrite nums1 elements. Instead, fill from back:
     *   - p1 points to the last valid element of nums1 (index m-1)
     *   - p2 points to the last element of nums2 (index n-1)
     *   - p  points to the next slot to fill (index m+n-1)
     *
     *   At each step, place the larger of nums1[p1] and nums2[p2] at nums1[p].
     *   If nums2 still has remaining elements after the main loop, copy them over.
     *   (Remaining nums1 elements are already in place.)
     *
     * Time:  O(m + n)
     * Space: O(1) — in-place
     *
     * @param nums1 first sorted array with extra space at end (length m+n)
     * @param m     number of valid elements in nums1
     * @param nums2 second sorted array
     * @param n     number of elements in nums2
     */
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int p1 = m - 1;        // pointer for nums1's valid portion
        int p2 = n - 1;        // pointer for nums2
        int p = m + n - 1;     // pointer for writing into nums1

        // Merge from back to front
        while (p1 >= 0 && p2 >= 0) {
            if (nums1[p1] >= nums2[p2]) {
                nums1[p] = nums1[p1];
                p1--;
            } else {
                nums1[p] = nums2[p2];
                p2--;
            }
            p--;
        }

        // If nums2 still has elements, copy them (nums1 elements are already in place)
        while (p2 >= 0) {
            nums1[p] = nums2[p2];
            p2--;
            p--;
        }
    }

    // =========================================================================
    // Problem 6: Binary Search
    // =========================================================================

    /**
     * Search for a target value in a sorted array. Return its index or -1 if not found.
     *
     * Binary Search:
     *   Repeatedly halve the search space by comparing target to the middle element.
     *   Use mid = left + (right - left) / 2 instead of (left + right) / 2
     *   to prevent integer overflow when left and right are large.
     *
     * Time:  O(log n) — search space halves each iteration
     * Space: O(1)
     *
     * @param nums   sorted array of distinct integers
     * @param target value to search for
     * @return index of target in nums, or -1 if not found
     */
    public int search(int[] nums, int target) {
        int left = 0;
        int right = nums.length - 1;

        while (left <= right) {
            // Avoids integer overflow vs. (left + right) / 2
            int mid = left + (right - left) / 2;

            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                // Target is in the right half
                left = mid + 1;
            } else {
                // Target is in the left half
                right = mid - 1;
            }
        }

        return -1; // target not found
    }

    /**
     * Find the first occurrence of target in a sorted array with duplicates.
     * Returns -1 if not found.
     *
     * When target is found, don't return immediately — continue searching left half.
     *
     * @param nums   sorted array (may have duplicates)
     * @param target value to search for
     * @return index of first occurrence, or -1
     */
    public int searchFirstOccurrence(int[] nums, int target) {
        int left = 0, right = nums.length - 1;
        int result = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                result = mid;          // record this potential answer
                right = mid - 1;      // keep searching left for earlier occurrence
            } else if (nums[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return result;
    }

    // =========================================================================
    // Problem 7: Product of Array Except Self
    // =========================================================================

    /**
     * Return array where answer[i] = product of all elements except nums[i].
     * O(n) time. No division allowed.
     *
     * Left/Right Pass:
     *   answer[i] = (product of all elements LEFT of i) * (product of all elements RIGHT of i)
     *
     *   Pass 1 — Left products:
     *     answer[0] = 1 (nothing to the left of index 0)
     *     answer[i] = answer[i-1] * nums[i-1]
     *
     *   Pass 2 — Multiply by right products:
     *     Traverse right to left, maintaining a running right product.
     *     answer[i] *= rightProduct
     *     rightProduct *= nums[i]
     *
     * Example: nums = [1, 2, 3, 4]
     *   After left pass:  [1, 1, 2, 6]
     *   After right pass: [24, 12, 8, 6]
     *
     * Time:  O(n) — two passes
     * Space: O(1) extra (output array itself not counted as extra space)
     *
     * @param nums input array
     * @return array of products except self
     */
    public int[] productExceptSelf(int[] nums) {
        int n = nums.length;
        int[] answer = new int[n];

        // Pass 1: Fill answer[i] with the product of all elements to the LEFT of i
        answer[0] = 1; // No elements to the left of index 0
        for (int i = 1; i < n; i++) {
            // answer[i] = product of nums[0..i-1]
            // = answer[i-1] (product of nums[0..i-2]) * nums[i-1]
            answer[i] = answer[i - 1] * nums[i - 1];
        }

        // Pass 2: Multiply each answer[i] by the product of all elements to the RIGHT of i
        // rightProduct accumulates products from right to left
        int rightProduct = 1; // No elements to the right of the last index
        for (int i = n - 1; i >= 0; i--) {
            // answer[i] currently holds left product; multiply by right product
            answer[i] *= rightProduct;
            // Update right product to include nums[i] for the next position to the left
            rightProduct *= nums[i];
        }

        return answer;
    }

    // =========================================================================
    // Problem 8: Find Missing Number
    // =========================================================================

    /**
     * Given array of n distinct numbers in [0, n], find the missing number.
     *
     * XOR Approach:
     *   XOR all numbers from 0 to n, then XOR all elements of nums.
     *   Each number that appears in both will cancel out (x XOR x = 0).
     *   The missing number appears only once and remains.
     *
     *   Why XOR beats the math approach: avoids any risk of integer overflow
     *   that can occur when computing n*(n+1)/2 for large n.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param nums array of n distinct integers in range [0, n]
     * @return the missing integer
     */
    public int missingNumber(int[] nums) {
        int n = nums.length;
        // Start with XOR of complete range 0..n
        int xor = 0;
        for (int i = 0; i <= n; i++) {
            xor ^= i;
        }
        // XOR out all elements present in nums
        // Whatever remains is the missing number (it never got XORed twice)
        for (int num : nums) {
            xor ^= num;
        }
        return xor;
    }

    /**
     * Alternative: Gauss formula approach.
     * Missing = expectedSum - actualSum = n*(n+1)/2 - sum(nums)
     * Note: uses long to avoid overflow for large n.
     *
     * @param nums array of n distinct integers in range [0, n]
     * @return the missing integer
     */
    public int missingNumberMath(int[] nums) {
        long n = nums.length;
        long expectedSum = n * (n + 1) / 2;
        long actualSum = 0;
        for (int num : nums) actualSum += num;
        return (int)(expectedSum - actualSum);
    }

    // =========================================================================
    // Problem 9: Move Zeros
    // =========================================================================

    /**
     * Move all zeros in nums to the end while preserving the relative order of
     * non-zero elements. Modify in-place.
     *
     * Two Pointer Approach:
     *   writePos tracks where the next non-zero element should be placed.
     *   Scan through the array; whenever a non-zero element is found, write it
     *   at writePos and advance writePos.
     *   After the scan, fill positions [writePos, n-1] with zeros.
     *
     *   This performs at most n writes total and never reorders non-zero elements.
     *
     * Time:  O(n) — single pass plus filling zeros
     * Space: O(1) — in-place
     *
     * @param nums array to modify in-place
     */
    public void moveZeroes(int[] nums) {
        // writePos: index of the next available slot for a non-zero element
        int writePos = 0;

        // Move all non-zero elements to the front, preserving order
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                nums[writePos] = nums[i];
                writePos++;
            }
        }

        // Fill remaining positions with zeros
        while (writePos < nums.length) {
            nums[writePos] = 0;
            writePos++;
        }
    }

    // =========================================================================
    // Problem 10: Container With Most Water
    // =========================================================================

    /**
     * Given heights of n vertical lines, find two lines that form the container
     * holding the most water. Return the maximum area.
     *
     * Area formula: (right - left) * min(height[left], height[right])
     *
     * Two Pointer Greedy:
     *   Start with the widest possible container (left=0, right=n-1).
     *   At each step, move the pointer pointing to the SHORTER line inward.
     *
     *   Why move the shorter side?
     *   - Current area is limited by min(height[left], height[right]).
     *   - Moving the taller side inward: width decreases AND height is still
     *     limited by the shorter line → area can only decrease.
     *   - Moving the shorter side inward: width decreases but we might find
     *     a taller line that increases the height → area might increase.
     *   - So we can safely discard the shorter line without missing the optimum.
     *
     * Time:  O(n) — left and right pointers each move at most n times total
     * Space: O(1)
     *
     * @param height array of line heights
     * @return maximum water that can be contained
     */
    public int maxArea(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int maxWater = 0;

        while (left < right) {
            // Width is the distance between the two lines
            int width = right - left;
            // Height is limited by the shorter of the two lines
            int currentHeight = Math.min(height[left], height[right]);
            int currentArea = width * currentHeight;

            maxWater = Math.max(maxWater, currentArea);

            // Move the pointer on the shorter side inward
            // (moving the taller side can never improve the result)
            if (height[left] <= height[right]) {
                left++;
            } else {
                right--;
            }
        }

        return maxWater;
    }

    // =========================================================================
    // Main — Test Cases
    // =========================================================================

    public static void main(String[] args) {
        ArrayProblems sol = new ArrayProblems();

        System.out.println("=== Problem 1: Two Sum ===");
        System.out.println(Arrays.toString(sol.twoSum(new int[]{2, 7, 11, 15}, 9)));   // [0, 1]
        System.out.println(Arrays.toString(sol.twoSum(new int[]{3, 2, 4}, 6)));         // [1, 2]
        System.out.println(Arrays.toString(sol.twoSum(new int[]{3, 3}, 6)));             // [0, 1]

        System.out.println("\n=== Problem 2: Maximum Subarray (Kadane's) ===");
        System.out.println(sol.maxSubArray(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4})); // 6
        System.out.println(sol.maxSubArray(new int[]{1}));                                // 1
        System.out.println(sol.maxSubArray(new int[]{-3, -1, -2}));                      // -1 (all negative)

        System.out.println("\n=== Problem 3: Rotate Array ===");
        int[] r1 = {1, 2, 3, 4, 5, 6, 7};
        sol.rotate(r1, 3);
        System.out.println(Arrays.toString(r1)); // [5, 6, 7, 1, 2, 3, 4]

        int[] r2 = {-1, -100, 3, 99};
        sol.rotate(r2, 2);
        System.out.println(Arrays.toString(r2)); // [3, 99, -1, -100]

        int[] r3 = {1, 2, 3};
        sol.rotate(r3, 3);
        System.out.println(Arrays.toString(r3)); // [1, 2, 3] (k=n, no change)

        System.out.println("\n=== Problem 4: Find All Duplicates ===");
        System.out.println(sol.findDuplicates(new int[]{4, 3, 2, 7, 8, 2, 3, 1})); // [2, 3]
        System.out.println(sol.findDuplicates(new int[]{1, 1, 2}));                  // [1]
        System.out.println(sol.findDuplicates(new int[]{1}));                          // []

        System.out.println("\n=== Problem 5: Merge Sorted Arrays ===");
        int[] m1 = {1, 2, 3, 0, 0, 0};
        sol.merge(m1, 3, new int[]{2, 5, 6}, 3);
        System.out.println(Arrays.toString(m1)); // [1, 2, 2, 3, 5, 6]

        int[] m2 = {0};
        sol.merge(m2, 0, new int[]{1}, 1);
        System.out.println(Arrays.toString(m2)); // [1]

        System.out.println("\n=== Problem 6: Binary Search ===");
        System.out.println(sol.search(new int[]{-1, 0, 3, 5, 9, 12}, 9));   // 4
        System.out.println(sol.search(new int[]{-1, 0, 3, 5, 9, 12}, 2));   // -1
        System.out.println(sol.search(new int[]{5}, 5));                       // 0
        System.out.println(sol.searchFirstOccurrence(new int[]{1, 2, 2, 2, 3}, 2)); // 1

        System.out.println("\n=== Problem 7: Product Except Self ===");
        System.out.println(Arrays.toString(sol.productExceptSelf(new int[]{1, 2, 3, 4})));       // [24,12,8,6]
        System.out.println(Arrays.toString(sol.productExceptSelf(new int[]{-1, 1, 0, -3, 3}))); // [0,0,9,0,0]

        System.out.println("\n=== Problem 8: Missing Number ===");
        System.out.println(sol.missingNumber(new int[]{3, 0, 1}));                       // 2
        System.out.println(sol.missingNumber(new int[]{9, 6, 4, 2, 3, 5, 7, 0, 1}));    // 8
        System.out.println(sol.missingNumber(new int[]{0}));                               // 1
        System.out.println(sol.missingNumberMath(new int[]{3, 0, 1}));                   // 2

        System.out.println("\n=== Problem 9: Move Zeros ===");
        int[] z1 = {0, 1, 0, 3, 12};
        sol.moveZeroes(z1);
        System.out.println(Arrays.toString(z1)); // [1, 3, 12, 0, 0]

        int[] z2 = {0, 0, 1};
        sol.moveZeroes(z2);
        System.out.println(Arrays.toString(z2)); // [1, 0, 0]

        int[] z3 = {1, 2, 3};
        sol.moveZeroes(z3);
        System.out.println(Arrays.toString(z3)); // [1, 2, 3] (no zeros)

        System.out.println("\n=== Problem 10: Container With Most Water ===");
        System.out.println(sol.maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7})); // 49
        System.out.println(sol.maxArea(new int[]{1, 1}));                          // 1
        System.out.println(sol.maxArea(new int[]{4, 3, 2, 1, 4}));                // 16
    }
}
