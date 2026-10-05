import java.util.Arrays;

/**
 * Exercise04_Solution.java
 *
 * SOLUTION: Array Algorithms
 */
public class Exercise04_Solution {

    public static void main(String[] args) {
        // Test Problem 1: rotateLeft
        System.out.println("=== Problem 1: Rotate Left ===");
        int[] arr1 = {1, 2, 3, 4, 5};
        System.out.println("Original: " + Arrays.toString(arr1));
        System.out.println("k=2: " + Arrays.toString(rotateLeft(arr1, 2)));  // [3,4,5,1,2]
        System.out.println("k=0: " + Arrays.toString(rotateLeft(arr1, 0)));  // [1,2,3,4,5]
        System.out.println("k=5: " + Arrays.toString(rotateLeft(arr1, 5)));  // [1,2,3,4,5]
        System.out.println("k=7: " + Arrays.toString(rotateLeft(arr1, 7)));  // [3,4,5,1,2]

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
     * Rotates arr left by k positions, returning a new array.
     *
     * Example: {1,2,3,4,5}, k=2
     *   Elements from index 2 to end: {3,4,5} → go to front
     *   Elements from index 0 to 1:   {1,2}   → go to back
     *   Result: {3,4,5,1,2}
     *
     * KEY: k = k % n handles over-rotation (e.g., k=7 on length-5 = same as k=2)
     */
    static int[] rotateLeft(int[] arr, int k) {
        int n = arr.length;

        if (n == 0) return arr;

        k = k % n; // normalize k so it's in range [0, n-1]

        if (k == 0) return Arrays.copyOf(arr, n); // no rotation needed

        int[] result = new int[n];

        // Copy elements from index k to end → to the front of result
        // Example: k=2, arr={1,2,3,4,5}
        //   System.arraycopy(arr, 2, result, 0, 3) → result[0..2] = arr[2..4] = {3,4,5}
        System.arraycopy(arr, k, result, 0, n - k);

        // Copy elements from index 0 to k-1 → to the back of result
        //   System.arraycopy(arr, 0, result, 3, 2) → result[3..4] = arr[0..1] = {1,2}
        System.arraycopy(arr, 0, result, n - k, k);

        return result;
    }

    /**
     * Removes duplicates from a SORTED array in-place.
     * Returns the count of unique elements.
     *
     * Two-pointer technique:
     *   - readIndex scans forward looking for new unique values
     *   - writeIndex tracks where to place the next unique value
     *
     * Because the array is sorted, duplicates are always adjacent.
     * A new unique value is found when arr[readIndex] != arr[readIndex - 1].
     *
     * Example: {1, 1, 2, 3, 3, 3, 4, 5, 5}
     *   writeIndex starts at 1 (first element is always unique)
     *   readIndex = 1: arr[1]=1, arr[0]=1 → duplicate, skip
     *   readIndex = 2: arr[2]=2, arr[1]=1 → new! arr[1]=2, writeIndex=2
     *   readIndex = 3: arr[3]=3, arr[2]=3 → new! arr[2]=3, writeIndex=3
     *   readIndex = 4: arr[4]=3, arr[3]=3 → duplicate, skip
     *   readIndex = 5: arr[5]=3, arr[4]=3 → duplicate, skip
     *   readIndex = 6: arr[6]=4, arr[5]=3 → new! arr[3]=4, writeIndex=4
     *   readIndex = 7: arr[7]=5, arr[6]=4 → new! arr[4]=5, writeIndex=5
     *   readIndex = 8: arr[8]=5, arr[7]=5 → duplicate, skip
     *   Final: arr={1,2,3,4,5,...}, return 5
     */
    static int removeDuplicates(int[] arr) {
        if (arr.length == 0) return 0;

        int writeIndex = 1; // index where the next unique element will be written

        for (int readIndex = 1; readIndex < arr.length; readIndex++) {
            if (arr[readIndex] != arr[readIndex - 1]) {
                // Found a new unique value — write it
                arr[writeIndex] = arr[readIndex];
                writeIndex++;
            }
            // If equal to previous, it's a duplicate — just skip
        }

        return writeIndex; // equals the count of unique elements
    }

    /**
     * Returns indices of two numbers in arr that sum to target.
     *
     * Brute force: try every pair — O(n²) time, O(1) space.
     * (A hash map solution is O(n) but that's covered in Module 04.)
     *
     * Inner loop starts at i+1 to avoid using the same element twice
     * and to avoid checking the same pair twice.
     */
    static int[] twoSum(int[] arr, int target) {
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i] + arr[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1}; // problem guarantees a solution exists, but just in case
    }
}
