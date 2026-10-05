import java.util.Arrays;

/**
 * PROBLEM: Trapping Rain Water
 * Given n non-negative integers representing an elevation map where
 * the width of each bar is 1, compute how much water it can trap after raining.
 *
 * Example:
 *   Input:  [0,1,0,2,1,0,1,3,2,1,2,1]
 *   Output: 6
 *
 *   Visual:
 *              _
 *       _     | |_ _   _
 *      | |_ _||     | | |_
 *   ___|       |     |_|   |___
 *   0 1 0 2 1 0 1 3 2 1 2 1
 *         ^^^   ^ ^ ^   ^       ← water trapped
 *
 * KEY INSIGHT: Water at position i is limited by the minimum of:
 *   - maxLeft[i]:  maximum height to the LEFT of i (inclusive)
 *   - maxRight[i]: maximum height to the RIGHT of i (inclusive)
 *
 *   water[i] = max(0, min(maxLeft[i], maxRight[i]) - height[i])
 *
 * THREE APPROACHES (from O(n) space to O(1) space):
 *   1. Precompute maxLeft and maxRight arrays — O(n) time, O(n) space
 *   2. Two pointers — O(n) time, O(1) space [OPTIMAL]
 *   3. Stack-based — O(n) time, O(n) space (different perspective)
 */
public class TrappingRainWater {

    // -----------------------------------------------------------------------
    // APPROACH 1: Precomputed maxLeft and maxRight arrays
    // -----------------------------------------------------------------------
    /**
     * Precompute max heights from both sides.
     *
     * maxLeft[i]  = max height from index 0 to i (inclusive)
     * maxRight[i] = max height from index i to n-1 (inclusive)
     *
     * Water at i = max(0, min(maxLeft[i], maxRight[i]) - height[i])
     *
     * TRACE for height=[0,1,0,2,1,0,1,3,2,1,2,1]:
     *   maxLeft  = [0,1,1,2,2,2,2,3,3,3,3,3]
     *   maxRight = [3,3,3,3,3,3,3,3,2,2,2,1]
     *   water    = [0,0,1,0,1,2,1,0,0,1,0,0] → sum = 6
     *
     * TIME:  O(n) — three passes: maxLeft, maxRight, water sum
     * SPACE: O(n) — two auxiliary arrays
     */
    public int trapSpaceN(int[] height) {
        if (height == null || height.length < 3) return 0;

        int n = height.length;
        int[] maxLeft  = new int[n];
        int[] maxRight = new int[n];

        // Build maxLeft: max height from 0 to i
        maxLeft[0] = height[0];
        for (int i = 1; i < n; i++) {
            maxLeft[i] = Math.max(maxLeft[i - 1], height[i]);
        }

        // Build maxRight: max height from i to n-1
        maxRight[n - 1] = height[n - 1];
        for (int i = n - 2; i >= 0; i--) {
            maxRight[i] = Math.max(maxRight[i + 1], height[i]);
        }

        // Calculate trapped water at each position
        int totalWater = 0;
        for (int i = 0; i < n; i++) {
            // Water level is the minimum wall on either side
            // Subtract the column height to get actual water above the column
            int waterLevel = Math.min(maxLeft[i], maxRight[i]);
            totalWater += Math.max(0, waterLevel - height[i]);
        }

        return totalWater;
    }

    // -----------------------------------------------------------------------
    // APPROACH 2: Two Pointers — O(n) time, O(1) space [OPTIMAL]
    // -----------------------------------------------------------------------
    /**
     * Two pointer approach — optimal solution.
     *
     * KEY OBSERVATION: Water at position i depends on min(maxLeft[i], maxRight[i]).
     * If maxLeft[left] < maxRight[right], then we know the water at `left` is
     * limited by maxLeft (the smaller of the two sides), regardless of what
     * maxRight[left] actually is — it's at least height[right] which is ≥ maxLeft.
     *
     * Algorithm:
     *   - Maintain left pointer, right pointer, maxLeft, maxRight
     *   - While left < right:
     *     - If maxLeft <= maxRight: water at left = maxLeft - height[left]. Move left right.
     *     - Else: water at right = maxRight - height[right]. Move right left.
     *
     * WHY IS THIS CORRECT?
     *   Say maxLeft < maxRight (maxLeft is the limiting factor for the left pointer).
     *   We know maxRight >= maxRight_so_far >= maxLeft >= maxLeft_so_far.
     *   So water[left] = max(0, maxLeft - height[left]) is exact — maxRight won't
     *   constrain it any further because maxRight > maxLeft.
     *
     * TIME:  O(n) — single pass, each element visited once
     * SPACE: O(1) — only four scalar variables
     *
     * TRACE for height=[0,1,0,2,1,0,1,3,2,1,2,1]:
     * left=0,right=11, maxL=0,maxR=0: total=0
     * maxL(0)<=maxR(0): water=0-0=0. height[0]=0,maxL=0. left→1. total=0
     * maxL(0)<=maxR(0): height[1]=1,maxL=max(0,1)=1. water=1-1=0. left→2. total=0
     * maxL(1)<=maxR(0)? NO. So process right:
     * Actually let's shortcut — result is 6.
     */
    public int trap(int[] height) {
        if (height == null || height.length < 3) return 0;

        int left = 0;
        int right = height.length - 1;
        int maxLeft  = 0;
        int maxRight = 0;
        int totalWater = 0;

        while (left < right) {
            if (height[left] <= height[right]) {
                // Left side is the limiting factor
                if (height[left] >= maxLeft) {
                    // Current bar is taller than previous max — no water here
                    maxLeft = height[left];
                } else {
                    // Water trapped = maxLeft - height[left]
                    totalWater += maxLeft - height[left];
                }
                left++;
            } else {
                // Right side is the limiting factor
                if (height[right] >= maxRight) {
                    maxRight = height[right];
                } else {
                    totalWater += maxRight - height[right];
                }
                right--;
            }
        }

        return totalWater;
    }

    // -----------------------------------------------------------------------
    // APPROACH 3: Stack-based — O(n) time, O(n) space
    // -----------------------------------------------------------------------
    /**
     * Stack approach — computes water horizontally layer by layer.
     *
     * The stack maintains indices of bars in decreasing height order.
     * When we find a taller bar, we can compute the water trapped in
     * the "valley" between the current bar and the bar at the bottom of the stack.
     *
     * This approach is useful for understanding water calculation from a
     * different perspective (horizontal cross-sections vs vertical columns).
     *
     * TIME: O(n), SPACE: O(n)
     */
    public int trapStack(int[] height) {
        if (height == null || height.length < 3) return 0;

        java.util.Deque<Integer> stack = new java.util.ArrayDeque<>(); // stores indices
        int totalWater = 0;

        for (int right = 0; right < height.length; right++) {
            // Process while current bar is taller than the bar at top of stack
            while (!stack.isEmpty() && height[right] > height[stack.peek()]) {
                int bottom = stack.pop(); // The valley bottom

                if (stack.isEmpty()) break; // No left wall

                int left = stack.peek(); // Left wall index

                // Width between left wall and right wall
                int width = right - left - 1;
                // Height is bounded by the shorter of the two walls minus the valley bottom
                int boundedHeight = Math.min(height[left], height[right]) - height[bottom];

                totalWater += width * boundedHeight;
            }
            stack.push(right);
        }

        return totalWater;
    }

    // -----------------------------------------------------------------------
    // Visualization helper
    // -----------------------------------------------------------------------
    private void visualize(int[] height) {
        int maxH = Arrays.stream(height).max().getAsInt();
        System.out.println("  Elevation map:");
        for (int row = maxH; row >= 1; row--) {
            System.out.print("  " + row + " |");
            for (int h : height) {
                if (h >= row) System.out.print("█");
                else System.out.print("~");
            }
            System.out.println("|");
        }
        System.out.print("    +");
        for (int i = 0; i < height.length; i++) System.out.print("-");
        System.out.println("+");
        System.out.print("     ");
        for (int h : height) System.out.print(h);
        System.out.println();
        System.out.println("  (~ = water or air; █ = rock)");
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        TrappingRainWater solution = new TrappingRainWater();

        System.out.println("=== Trapping Rain Water ===");
        System.out.println();

        int[][] tests = {
            {0,1,0,2,1,0,1,3,2,1,2,1},  // expected 6
            {4,2,0,3,2,5},               // expected 9
            {3,0,2,0,4},                 // expected 7
            {1,0,1},                     // expected 1
            {1,2,3,4,5},                 // expected 0 (no valleys)
            {5,4,3,2,1},                 // expected 0 (no valleys)
            {0,0,0},                     // expected 0
            {3,0,0,2,0,4},              // expected 10
        };
        int[] expected = {6, 9, 7, 1, 0, 0, 0, 10};

        int passed = 0;
        for (int i = 0; i < tests.length; i++) {
            int r1 = solution.trapSpaceN(tests[i]);
            int r2 = solution.trap(tests[i]);
            int r3 = solution.trapStack(tests[i]);
            boolean ok = r1 == expected[i] && r2 == expected[i] && r3 == expected[i];
            if (ok) passed++;
            System.out.printf("height=%-35s expected=%-4d O(n)space=%-4d O(1)space=%-4d stack=%-4d %s%n",
                Arrays.toString(tests[i]), expected[i], r1, r2, r3, ok ? "✓" : "✗");
        }
        System.out.printf("%nPassed: %d/%d (all three approaches)%n", passed, tests.length);

        System.out.println();
        System.out.println("=== Visualization: [0,1,0,2,1,0,1,3,2,1,2,1] ===");
        solution.visualize(new int[]{0,1,0,2,1,0,1,3,2,1,2,1});
        System.out.println("  Trapped water: 6 units");

        System.out.println();
        System.out.println("=== O(n) space approach trace for [0,1,0,2,1,0,1,3,2,1,2,1] ===");
        int[] h = {0,1,0,2,1,0,1,3,2,1,2,1};
        int n = h.length;
        int[] mL = new int[n], mR = new int[n];
        mL[0] = h[0];
        for (int i = 1; i < n; i++) mL[i] = Math.max(mL[i-1], h[i]);
        mR[n-1] = h[n-1];
        for (int i = n-2; i >= 0; i--) mR[i] = Math.max(mR[i+1], h[i]);
        System.out.println("  height:   " + Arrays.toString(h));
        System.out.println("  maxLeft:  " + Arrays.toString(mL));
        System.out.println("  maxRight: " + Arrays.toString(mR));
        int[] water = new int[n];
        for (int i = 0; i < n; i++) water[i] = Math.max(0, Math.min(mL[i], mR[i]) - h[i]);
        System.out.println("  water:    " + Arrays.toString(water));
        System.out.println("  Total:    " + Arrays.stream(water).sum());

        System.out.println();
        System.out.println("=== Complexity Summary ===");
        System.out.println("  Precomputed arrays:  O(n) time, O(n) space  — easy to understand");
        System.out.println("  Two pointers:        O(n) time, O(1) space  — OPTIMAL, standard interview answer");
        System.out.println("  Stack:               O(n) time, O(n) space  — different perspective (horizontal)");
    }
}
