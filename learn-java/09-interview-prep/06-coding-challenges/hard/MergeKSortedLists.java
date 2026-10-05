import java.util.*;

/**
 * PROBLEM: Merge K Sorted Lists
 * Given an array of k linked-lists, each sorted in ascending order,
 * merge all the linked-lists into one sorted linked-list and return it.
 *
 * Example:
 *   Input:  [[1→4→5], [1→3→4], [2→6]]
 *   Output: 1→1→2→3→4→4→5→6
 *
 * APPROACH: Min-Heap (PriorityQueue)
 *   The key insight: at any point, the next smallest element must be the
 *   head of one of the k lists. A min-heap lets us always find the minimum
 *   among k candidates in O(log k) time.
 *
 * Algorithm:
 *   1. Initialize a min-heap with the head node of each non-null list.
 *   2. Repeatedly:
 *      a. Poll the minimum node from the heap.
 *      b. Add it to the result linked list.
 *      c. If the polled node has a next node, push that next node onto the heap.
 *   3. Continue until the heap is empty.
 *
 * Why this works:
 *   - The heap always contains at most k nodes (one per list).
 *   - Each heap operation (poll/offer) is O(log k).
 *   - We perform exactly n total operations (one per node across all lists).
 *   - Total: O(n log k).
 *
 * TIME:  O(n log k) — n = total nodes, k = number of lists
 *   Compare with alternatives:
 *   - Naive (merge one by one): O(nk) — k-1 merges, each O(n)
 *   - Collect all, sort: O(n log n) — worse than log k since k <= n
 *
 * SPACE: O(k) — the heap holds at most k elements at any time
 *         O(1) extra if not counting output
 *
 * ALTERNATIVE: Divide and Conquer
 *   Recursively merge pairs of lists (like merge sort).
 *   Also O(n log k) but with O(log k) call stack.
 */
public class MergeKSortedLists {

    // -----------------------------------------------------------------------
    // Inner class: ListNode
    // -----------------------------------------------------------------------
    static class ListNode {
        int val;
        ListNode next;

        ListNode(int val) {
            this.val = val;
        }

        ListNode(int val, ListNode next) {
            this.val = val;
            this.next = next;
        }
    }

    // -----------------------------------------------------------------------
    // Approach 1: Min-Heap
    // -----------------------------------------------------------------------
    /**
     * Merge k sorted linked lists using a min-heap.
     *
     * @param lists array of k sorted linked list heads
     * @return head of the merged sorted linked list
     */
    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) return null;

        // Min-heap ordered by node value
        // If two nodes have the same value, either order is fine (result still sorted)
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>(
            (a, b) -> a.val - b.val
        );

        // Initialize heap with the head of each non-null list
        for (ListNode head : lists) {
            if (head != null) {
                minHeap.offer(head);
            }
        }

        // Dummy head to simplify building the result list
        ListNode dummy = new ListNode(-1);
        ListNode current = dummy;

        while (!minHeap.isEmpty()) {
            // Get the node with the smallest value
            ListNode smallest = minHeap.poll();

            // Append it to the result
            current.next = smallest;
            current = current.next;

            // If this node has a successor, add it to the heap
            // (it becomes a candidate for the next minimum)
            if (smallest.next != null) {
                minHeap.offer(smallest.next);
            }
        }

        return dummy.next;
    }

    // -----------------------------------------------------------------------
    // Approach 2: Divide and Conquer
    // -----------------------------------------------------------------------
    /**
     * Merge k sorted lists using divide and conquer.
     *
     * Pair up lists and merge each pair:
     *   Round 1: merge (list0,list1), (list2,list3), ...
     *   Round 2: merge results, ...
     *   ...
     *   log k rounds total, each round is O(n) total work → O(n log k)
     *
     * TIME:  O(n log k), SPACE: O(log k) call stack
     */
    public ListNode mergeKListsDivide(ListNode[] lists) {
        if (lists == null || lists.length == 0) return null;

        int length = lists.length;
        int interval = 1;

        // Iterative divide and conquer to avoid deep recursion
        while (interval < length) {
            for (int i = 0; i < length - interval; i += 2 * interval) {
                lists[i] = mergeTwoLists(lists[i], lists[i + interval]);
            }
            interval *= 2;
        }

        return lists[0];
    }

    /**
     * Helper: merge two sorted linked lists.
     * This is a fundamental operation — worth knowing cold.
     *
     * TIME: O(n + m) where n, m are lengths of the two lists
     */
    private ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode dummy = new ListNode(-1);
        ListNode current = dummy;

        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                current.next = l1;
                l1 = l1.next;
            } else {
                current.next = l2;
                l2 = l2.next;
            }
            current = current.next;
        }

        // Attach remaining nodes (at most one list has remaining nodes)
        current.next = (l1 != null) ? l1 : l2;

        return dummy.next;
    }

    // -----------------------------------------------------------------------
    // Helper: Build a linked list from an array
    // -----------------------------------------------------------------------
    private static ListNode buildList(int[] values) {
        if (values == null || values.length == 0) return null;

        ListNode dummy = new ListNode(-1);
        ListNode current = dummy;
        for (int val : values) {
            current.next = new ListNode(val);
            current = current.next;
        }
        return dummy.next;
    }

    // -----------------------------------------------------------------------
    // Helper: Convert linked list to string for display
    // -----------------------------------------------------------------------
    private static String listToString(ListNode head) {
        if (head == null) return "null";
        StringBuilder sb = new StringBuilder();
        ListNode curr = head;
        while (curr != null) {
            sb.append(curr.val);
            if (curr.next != null) sb.append("→");
            curr = curr.next;
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Helper: Verify result is sorted
    // -----------------------------------------------------------------------
    private static boolean isSorted(ListNode head) {
        if (head == null) return true;
        ListNode curr = head;
        while (curr.next != null) {
            if (curr.val > curr.next.val) return false;
            curr = curr.next;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        MergeKSortedLists solution = new MergeKSortedLists();

        System.out.println("=== Merge K Sorted Lists ===");
        System.out.println();

        // Test 1: Classic example
        System.out.println("--- Test 1: k=3 lists ---");
        ListNode[] lists1 = {
            buildList(new int[]{1, 4, 5}),
            buildList(new int[]{1, 3, 4}),
            buildList(new int[]{2, 6})
        };
        System.out.println("List 1: " + listToString(lists1[0]));
        System.out.println("List 2: " + listToString(lists1[1]));
        System.out.println("List 3: " + listToString(lists1[2]));
        ListNode result1 = solution.mergeKLists(lists1);
        System.out.println("Merged: " + listToString(result1));
        System.out.println("Expected: 1→1→2→3→4→4→5→6");
        System.out.println("Sorted: " + isSorted(result1));
        System.out.println();

        // Test 2: Empty array
        System.out.println("--- Test 2: Empty array ---");
        ListNode[] lists2 = {};
        ListNode result2 = solution.mergeKLists(lists2);
        System.out.println("Merged: " + listToString(result2));
        System.out.println("Expected: null");
        System.out.println();

        // Test 3: Array with one empty list
        System.out.println("--- Test 3: [[]] ---");
        ListNode[] lists3 = {null};
        ListNode result3 = solution.mergeKLists(lists3);
        System.out.println("Merged: " + listToString(result3));
        System.out.println("Expected: null");
        System.out.println();

        // Test 4: k=1
        System.out.println("--- Test 4: k=1 ---");
        ListNode[] lists4 = {buildList(new int[]{1, 2, 3})};
        ListNode result4 = solution.mergeKLists(lists4);
        System.out.println("Merged: " + listToString(result4));
        System.out.println("Expected: 1→2→3");
        System.out.println();

        // Test 5: Large k (performance test)
        System.out.println("--- Test 5: k=5 large lists ---");
        ListNode[] lists5 = {
            buildList(new int[]{1, 10, 20, 30}),
            buildList(new int[]{2, 12, 22, 32}),
            buildList(new int[]{3, 13, 23, 33}),
            buildList(new int[]{4, 14, 24, 34}),
            buildList(new int[]{5, 15, 25, 35})
        };
        ListNode result5 = solution.mergeKLists(lists5);
        System.out.println("Merged: " + listToString(result5));
        System.out.println("Sorted: " + isSorted(result5));
        System.out.println();

        // Test divide and conquer version
        System.out.println("--- Divide and Conquer on Test 1 ---");
        ListNode[] lists6 = {
            buildList(new int[]{1, 4, 5}),
            buildList(new int[]{1, 3, 4}),
            buildList(new int[]{2, 6})
        };
        ListNode result6 = solution.mergeKListsDivide(lists6);
        System.out.println("Merged: " + listToString(result6));
        System.out.println("Expected: 1→1→2→3→4→4→5→6");
        System.out.println("Sorted: " + isSorted(result6));

        System.out.println();
        System.out.println("=== Complexity Comparison ===");
        System.out.println("Naive (one by one):        O(nk)     — n total nodes, k lists");
        System.out.println("Collect + sort:            O(n log n)");
        System.out.println("Min-heap:                  O(n log k) — BEST practical approach");
        System.out.println("Divide and conquer:        O(n log k) — same asymptotic, less heap overhead");
        System.out.println();
        System.out.println("For k=3, n=8 (test 1):");
        System.out.println("  Heap operations: 8 polls + 5 offers = 13 operations, each O(log 3) ≈ O(1.58)");
    }
}
