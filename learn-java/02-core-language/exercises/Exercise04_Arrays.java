import java.util.Arrays;

/**
 * Exercise04_Arrays.java
 *
 * EXERCISE: Array Algorithms
 *
 * Three classic array manipulation problems.
 *
 * =====================================================================
 * PROBLEM 1: Rotate Array Left by K Positions
 * =====================================================================
 * Rotate the array to the LEFT by k positions.
 * {1, 2, 3, 4, 5}, k=2 → {3, 4, 5, 1, 2}
 * {1, 2, 3, 4, 5}, k=0 → {1, 2, 3, 4, 5}
 * {1, 2, 3, 4, 5}, k=5 → {1, 2, 3, 4, 5}  (full rotation = back to start)
 *
 * Return the rotated array as a NEW array (don't modify the original).
 *
 * HINTS:
 * - k = k % array.length to handle rotations larger than the array size
 * - The first k elements go to the end
 * - Use System.arraycopy() or Arrays.copyOfRange() to copy portions
 *
 * =====================================================================
 * PROBLEM 2: Remove Duplicates from a Sorted Array
 * =====================================================================
 * Given a SORTED array, return the count of unique elements.
 * The unique elements must be placed at the beginning of the array (in-place).
 *
 * Input:  {1, 1, 2, 3, 3, 3, 4, 5, 5}
 * After:  {1, 2, 3, 4, 5, _, _, _, _}  (underscore = don't care)
 * Return: 5 (the count of unique elements)
 *
 * HINTS:
 * - Use two pointers: a "write" pointer and a "read" pointer
 * - writeIndex starts at 1 (first element is always unique)
 * - If arr[readIndex] != arr[readIndex - 1], it's a new unique value
 *   → copy it to arr[writeIndex] and increment writeIndex
 *
 * =====================================================================
 * PROBLEM 3: Two Sum
 * =====================================================================
 * Given an array of integers and a target sum, find the indices of
 * the two numbers that add up to the target.
 * Return the indices as an int[] of length 2.
 * Assume exactly one solution exists.
 *
 * {2, 7, 11, 15}, target=9  → {0, 1}  (2 + 7 = 9)
 * {3, 2, 4},      target=6  → {1, 2}  (2 + 4 = 6)
 * {3, 3},         target=6  → {0, 1}  (3 + 3 = 6)
 *
 * HINTS:
 * - Use two nested for loops: outer i, inner j where j starts at i+1
 * - Check if arr[i] + arr[j] == target
 * - Return new int[]{i, j} when found
 *
 * SKILLS PRACTICED:
 * - Array copying and slicing
 * - Two-pointer technique
 * - Nested loops
 * - Return types (returning arrays from methods)
 */
public class Exercise04_Arrays {

    public static void main(String[] args) {
        // Test Problem 1: rotateLeft
        System.out.println("=== Problem 1: Rotate Left ===");
        int[] arr1 = {1, 2, 3, 4, 5};
        System.out.println("Original: " + Arrays.toString(arr1));
        System.out.println("k=2: " + Arrays.toString(rotateLeft(arr1, 2)));  // [3,4,5,1,2]
        System.out.println("k=0: " + Arrays.toString(rotateLeft(arr1, 0)));  // [1,2,3,4,5]
        System.out.println("k=5: " + Arrays.toString(rotateLeft(arr1, 5)));  // [1,2,3,4,5]
        System.out.println("k=7: " + Arrays.toString(rotateLeft(arr1, 7)));  // [3,4,5,1,2] (7%5=2)

        // Test Problem 2: removeDuplicates
        System.out.println("\n=== Problem 2: Remove Duplicates ===");
        int[] arr2 = {1, 1, 2, 3, 3, 3, 4, 5, 5};
        System.out.println("Input:  " + Arrays.toString(arr2));
        int uniqueCount = removeDuplicates(arr2);
        System.out.println("Unique count: " + uniqueCount);  // 5
        System.out.print("First " + uniqueCount + " elements: ");
        for (int i = 0; i < uniqueCount; i++) {
            System.out.print(arr2[i] + " ");  // 1 2 3 4 5
        }
        System.out.println();

        // Test Problem 3: twoSum
        System.out.println("\n=== Problem 3: Two Sum ===");
        System.out.println("twoSum({2,7,11,15}, 9):  "
                + Arrays.toString(twoSum(new int[]{2, 7, 11, 15}, 9)));   // [0, 1]
        System.out.println("twoSum({3,2,4}, 6):      "
                + Arrays.toString(twoSum(new int[]{3, 2, 4}, 6)));        // [1, 2]
        System.out.println("twoSum({3,3}, 6):        "
                + Arrays.toString(twoSum(new int[]{3, 3}, 6)));           // [0, 1]
    }

    /**
     * Returns a NEW array that is arr rotated left by k positions.
     *
     * @param arr the original array (not modified)
     * @param k   number of positions to rotate left
     * @return the rotated array
     */
    static int[] rotateLeft(int[] arr, int k) {
        // TODO 1: Handle edge cases
        //         if (arr.length == 0) return arr;
        //         k = k % arr.length; // handle k >= arr.length
        //         if (k == 0) return Arrays.copyOf(arr, arr.length);

        // TODO 2: Build the result array
        //         int[] result = new int[arr.length];
        //         Copy elements from index k to end: arr[k], arr[k+1], ..., arr[n-1]
        //         Then copy elements from index 0 to k-1: arr[0], ..., arr[k-1]
        //
        //         Hint: System.arraycopy(src, srcPos, dest, destPos, length)
        //           System.arraycopy(arr, k, result, 0, arr.length - k);
        //           System.arraycopy(arr, 0, result, arr.length - k, k);

        // TODO 3: return result;

        return new int[0]; // replace this
    }

    /**
     * Removes duplicates from a sorted array IN-PLACE.
     * The first uniqueCount elements of arr will contain the unique values.
     *
     * @param arr sorted array
     * @return count of unique elements
     */
    static int removeDuplicates(int[] arr) {
        // TODO 1: Handle edge case: if arr.length == 0, return 0

        // TODO 2: int writeIndex = 1;  // first element is always unique, start writing at index 1
        //
        //         for (int readIndex = 1; readIndex < arr.length; readIndex++) {
        //             if (arr[readIndex] != arr[readIndex - 1]) {
        //                 // found a new unique value
        //                 arr[writeIndex] = arr[readIndex];
        //                 writeIndex++;
        //             }
        //         }

        // TODO 3: return writeIndex; (this IS the count of unique elements)

        return 0; // replace this
    }

    /**
     * Finds indices of two numbers in arr that sum to target.
     * Assumes exactly one solution exists.
     *
     * @param arr    the input array
     * @param target the target sum
     * @return int[] of length 2 containing the two indices
     */
    static int[] twoSum(int[] arr, int target) {
        // TODO: Two nested loops
        //       for (int i = 0; i < arr.length - 1; i++) {
        //           for (int j = i + 1; j < arr.length; j++) {
        //               if (arr[i] + arr[j] == target) {
        //                   return new int[]{i, j};
        //               }
        //           }
        //       }
        //       return new int[]{-1, -1}; // should never reach here if problem guarantees a solution

        return new int[]{-1, -1}; // replace this
    }
}
