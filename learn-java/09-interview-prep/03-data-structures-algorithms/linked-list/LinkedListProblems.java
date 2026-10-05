import java.util.*;

/**
 * Solutions to 10 classic linked list interview problems.
 * Uses a static inner class Node to represent a singly linked list node.
 *
 * Problems covered:
 *  1.  Reverse a Linked List          — iterative and recursive
 *  2.  Detect Cycle                   — Floyd's tortoise and hare
 *  3.  Find Middle of Linked List     — fast/slow pointers
 *  4.  Merge Two Sorted Lists         — iterative with dummy head
 *  5.  Remove Nth Node From End       — one-pass two-pointer
 *  6.  Intersection of Two Lists      — length difference approach
 *  7.  Palindrome Linked List         — find middle + reverse + compare
 *  8.  Remove Duplicates (Sorted)     — single scan
 *  9.  Add Two Numbers                — simulate addition with carry
 * 10.  Reorder List                   — find middle + reverse + merge
 */
public class LinkedListProblems {

    // =========================================================================
    // Inner Class: Node
    // =========================================================================

    /**
     * A node in a singly linked list.
     */
    static class Node {
        int val;
        Node next;

        Node(int val) {
            this.val = val;
            this.next = null;
        }

        Node(int val, Node next) {
            this.val = val;
            this.next = next;
        }
    }

    // =========================================================================
    // Helper Utilities
    // =========================================================================

    /** Build a linked list from an array of values. */
    static Node buildList(int... values) {
        if (values.length == 0) return null;
        Node dummy = new Node(0);
        Node current = dummy;
        for (int v : values) {
            current.next = new Node(v);
            current = current.next;
        }
        return dummy.next;
    }

    /** Convert a linked list to a human-readable string. */
    static String listToString(Node head) {
        StringBuilder sb = new StringBuilder();
        Node curr = head;
        while (curr != null) {
            sb.append(curr.val);
            if (curr.next != null) sb.append(" -> ");
            curr = curr.next;
        }
        return sb.toString().isEmpty() ? "null" : sb.toString();
    }

    // =========================================================================
    // Problem 1: Reverse a Linked List
    // =========================================================================

    /**
     * Reverse a singly linked list iteratively.
     *
     * Algorithm:
     *   Traverse with three pointers: prev, curr, next.
     *   For each node, redirect curr.next to prev.
     *   Advance all three pointers before moving on.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param head first node of the list
     * @return head of the reversed list
     */
    public Node reverseListIterative(Node head) {
        Node prev = null;
        Node curr = head;

        while (curr != null) {
            Node next = curr.next; // save next before we overwrite it
            curr.next = prev;      // redirect pointer backward
            prev = curr;           // advance prev
            curr = next;           // advance curr
        }

        return prev; // prev is the new head (last node of original)
    }

    /**
     * Reverse a singly linked list recursively.
     *
     * Algorithm:
     *   Recurse to the tail (base case: head == null or head.next == null).
     *   On the way back up: make head.next.next = head (reverse the link).
     *   Set head.next = null (remove the old forward link).
     *
     * Time:  O(n)
     * Space: O(n) — call stack depth equals list length
     *
     * @param head first node of the list
     * @return head of the reversed list (the original tail)
     */
    public Node reverseListRecursive(Node head) {
        // Base case: empty or single node
        if (head == null || head.next == null) return head;

        // Recursively reverse everything after head; newHead is the original tail
        Node newHead = reverseListRecursive(head.next);

        // Make the node after head point back to head
        head.next.next = head;
        // Break the original forward link to avoid a cycle
        head.next = null;

        return newHead;
    }

    // =========================================================================
    // Problem 2: Detect Cycle
    // =========================================================================

    /**
     * Determine if a linked list has a cycle.
     *
     * Floyd's Tortoise and Hare:
     *   slow moves 1 step, fast moves 2 steps.
     *   If no cycle: fast reaches null.
     *   If there is a cycle: fast and slow must eventually meet because fast
     *   gains one step on slow per iteration inside the cycle.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param head first node of the list
     * @return true if the list has a cycle
     */
    public boolean hasCycle(Node head) {
        Node slow = head;
        Node fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;       // 1 step
            fast = fast.next.next;  // 2 steps

            if (slow == fast) {
                return true; // They met inside the cycle
            }
        }

        return false; // fast reached null → no cycle
    }

    // =========================================================================
    // Problem 3: Find Middle of Linked List
    // =========================================================================

    /**
     * Return the middle node of a linked list.
     * If there are two middle nodes, return the second one.
     *
     * Fast and Slow Pointers:
     *   When fast (moving 2 steps) reaches the end, slow (moving 1 step)
     *   is at the middle. For even-length lists, slow ends at the second
     *   middle because we check fast.next != null.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param head first node of the list
     * @return the middle node
     */
    public Node findMiddle(Node head) {
        Node slow = head;
        Node fast = head;

        // fast.next != null ensures slow lands on second middle for even-length lists
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }

    // =========================================================================
    // Problem 4: Merge Two Sorted Lists
    // =========================================================================

    /**
     * Merge two sorted linked lists into one sorted list.
     *
     * Dummy Head + Iterative Merge:
     *   A dummy (sentinel) node simplifies the first-node edge case — we never
     *   need to check if the result list is empty.
     *   Compare the front of both lists, append the smaller to result, advance.
     *   Append any remaining nodes from the longer list.
     *
     * Time:  O(m + n)
     * Space: O(1) — only the dummy node is allocated
     *
     * @param l1 head of first sorted list
     * @param l2 head of second sorted list
     * @return head of merged sorted list
     */
    public Node mergeTwoLists(Node l1, Node l2) {
        Node dummy = new Node(0); // sentinel to avoid special-casing first node
        Node current = dummy;

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

        // Attach any remaining nodes
        current.next = (l1 != null) ? l1 : l2;

        return dummy.next;
    }

    // =========================================================================
    // Problem 5: Remove Nth Node From End
    // =========================================================================

    /**
     * Remove the nth node from the end of the list in one pass.
     *
     * Two-Pointer One-Pass:
     *   Use a dummy head so we can handle removing the first node uniformly.
     *   Advance 'fast' n+1 steps ahead of 'slow' (starting from dummy).
     *   Move both together until fast == null.
     *   Now slow.next is the target node — skip it.
     *
     *   Why n+1 gap? We need slow to stop BEFORE the target, not AT it.
     *
     * Time:  O(L) — single pass
     * Space: O(1)
     *
     * @param head first node of the list
     * @param n    remove the nth node from the end (1-indexed)
     * @return head of the modified list
     */
    public Node removeNthFromEnd(Node head, int n) {
        Node dummy = new Node(0);
        dummy.next = head;

        Node slow = dummy;
        Node fast = dummy;

        // Advance fast by n+1 steps so slow stops one before the target
        for (int i = 0; i <= n; i++) {
            fast = fast.next;
        }

        // Move both until fast falls off the end
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }

        // slow is now just before the target node
        slow.next = slow.next.next; // skip the nth-from-end node

        return dummy.next;
    }

    // =========================================================================
    // Problem 6: Intersection of Two Linked Lists
    // =========================================================================

    /**
     * Find the node where two linked lists intersect, or null if they don't.
     *
     * Length Difference Approach:
     *   1. Compute lengths of both lists.
     *   2. Advance the pointer in the longer list by (lenA - lenB) steps.
     *   3. Advance both together until they point to the same node.
     *
     *   If they intersect, they share the same tail, so after equalizing the
     *   starting positions, they'll meet at the intersection in lockstep.
     *
     * Time:  O(m + n)
     * Space: O(1)
     *
     * @param headA head of first list
     * @param headB head of second list
     * @return intersection node, or null
     */
    public Node getIntersectionNode(Node headA, Node headB) {
        // Compute lengths
        int lenA = 0, lenB = 0;
        Node currA = headA, currB = headB;
        while (currA != null) { lenA++; currA = currA.next; }
        while (currB != null) { lenB++; currB = currB.next; }

        // Reset pointers to heads
        currA = headA;
        currB = headB;

        // Advance the longer list's pointer to equalize starting positions
        while (lenA > lenB) { currA = currA.next; lenA--; }
        while (lenB > lenA) { currB = currB.next; lenB--; }

        // Advance both until they meet (intersection) or both reach null (no intersection)
        while (currA != currB) {
            currA = currA.next;
            currB = currB.next;
        }

        return currA; // null if no intersection, intersection node otherwise
    }

    // =========================================================================
    // Problem 7: Palindrome Linked List
    // =========================================================================

    /**
     * Determine if a linked list is a palindrome in O(n) time and O(1) space.
     *
     * Three-Step Algorithm:
     *   1. Find the middle of the list (fast/slow pointers).
     *   2. Reverse the second half of the list.
     *   3. Compare first half with reversed second half.
     *
     *   For [1,2,2,1]:
     *   Middle = second node (val 2)
     *   Second half reversed: 1 -> 2
     *   Compare: 1==1, 2==2 → palindrome
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param head first node of the list
     * @return true if the list is a palindrome
     */
    public boolean isPalindrome(Node head) {
        if (head == null || head.next == null) return true;

        // Step 1: Find middle
        Node slow = head;
        Node fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // Step 2: Reverse the second half (starting from slow)
        Node secondHalf = reverseListIterative(slow);

        // Step 3: Compare first half with reversed second half
        Node left = head;
        Node right = secondHalf;
        boolean isPalin = true;

        while (right != null) {
            if (left.val != right.val) {
                isPalin = false;
                break;
            }
            left = left.next;
            right = right.next;
        }

        // Optionally restore the list (good practice)
        reverseListIterative(secondHalf);

        return isPalin;
    }

    // =========================================================================
    // Problem 8: Remove Duplicates from Sorted List
    // =========================================================================

    /**
     * Remove all duplicate nodes from a sorted linked list.
     * Each element should appear only once.
     *
     * Single Scan:
     *   When current.next has the same value as current, skip current.next.
     *   Otherwise advance current.
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param head first node of the sorted list
     * @return head of the deduplicated list
     */
    public Node deleteDuplicates(Node head) {
        Node current = head;

        while (current != null && current.next != null) {
            if (current.val == current.next.val) {
                // Skip the duplicate
                current.next = current.next.next;
            } else {
                // No duplicate, advance
                current = current.next;
            }
        }

        return head;
    }

    // =========================================================================
    // Problem 9: Add Two Numbers
    // =========================================================================

    /**
     * Add two numbers represented as reversed linked lists.
     * Return the sum as a reversed linked list.
     *
     * Simulate Grade-School Addition:
     *   Traverse both lists simultaneously. At each step:
     *   - Sum digits from l1 and l2 (use 0 if a list is exhausted) plus carry
     *   - New digit = sum % 10
     *   - New carry = sum / 10
     *   Continue until both lists are exhausted AND carry == 0.
     *
     * Time:  O(max(m, n))
     * Space: O(max(m, n)) for the result list
     *
     * @param l1 head of first reversed-digit number
     * @param l2 head of second reversed-digit number
     * @return head of the reversed-digit sum
     */
    public Node addTwoNumbers(Node l1, Node l2) {
        Node dummy = new Node(0);
        Node current = dummy;
        int carry = 0;

        while (l1 != null || l2 != null || carry != 0) {
            int digit1 = (l1 != null) ? l1.val : 0;
            int digit2 = (l2 != null) ? l2.val : 0;

            int sum = digit1 + digit2 + carry;
            carry = sum / 10;
            current.next = new Node(sum % 10);
            current = current.next;

            if (l1 != null) l1 = l1.next;
            if (l2 != null) l2 = l2.next;
        }

        return dummy.next;
    }

    // =========================================================================
    // Problem 10: Reorder List
    // =========================================================================

    /**
     * Reorder list so that L0->L1->...->Ln becomes L0->Ln->L1->Ln-1->L2->...
     * Modify in-place.
     *
     * Three-Step Algorithm:
     *   1. Find the middle of the list.
     *   2. Reverse the second half.
     *   3. Interleave (merge) the two halves.
     *
     *   Example: [1,2,3,4,5]
     *   Middle: node 3, second half: [4,5]
     *   Reversed second half: [5,4]
     *   Merge: 1->5->2->4->3
     *
     * Time:  O(n)
     * Space: O(1)
     *
     * @param head first node of the list (modified in-place)
     */
    public void reorderList(Node head) {
        if (head == null || head.next == null) return;

        // Step 1: Find middle
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // Step 2: Reverse second half (from slow.next onward)
        Node secondHalf = reverseListIterative(slow.next);
        slow.next = null; // disconnect first half from second

        // Step 3: Interleave first and reversed second halves
        Node first = head;
        Node second = secondHalf;

        while (second != null) {
            Node tmp1 = first.next;   // save next of first half
            Node tmp2 = second.next;  // save next of second half

            first.next = second;      // insert second half node
            second.next = tmp1;       // link to rest of first half

            first = tmp1;             // advance first pointer
            second = tmp2;            // advance second pointer
        }
    }

    // =========================================================================
    // Main — Test Cases
    // =========================================================================

    public static void main(String[] args) {
        LinkedListProblems sol = new LinkedListProblems();

        System.out.println("=== Problem 1: Reverse Linked List ===");
        Node list1 = buildList(1, 2, 3, 4, 5);
        System.out.println(listToString(sol.reverseListIterative(list1))); // 5->4->3->2->1

        Node list2 = buildList(1, 2, 3, 4, 5);
        System.out.println(listToString(sol.reverseListRecursive(list2))); // 5->4->3->2->1

        System.out.println(listToString(sol.reverseListIterative(null)));  // null
        System.out.println(listToString(sol.reverseListIterative(buildList(1)))); // 1

        System.out.println("\n=== Problem 2: Detect Cycle ===");
        Node noCycle = buildList(1, 2, 3, 4, 5);
        System.out.println(sol.hasCycle(noCycle)); // false

        // Build a cycle manually: 1->2->3->4->5->back to node 3
        Node cycleHead = new Node(1);
        Node n2 = new Node(2); Node n3 = new Node(3);
        Node n4 = new Node(4); Node n5 = new Node(5);
        cycleHead.next = n2; n2.next = n3; n3.next = n4; n4.next = n5; n5.next = n3; // cycle
        System.out.println(sol.hasCycle(cycleHead)); // true

        System.out.println("\n=== Problem 3: Find Middle ===");
        System.out.println(sol.findMiddle(buildList(1, 2, 3, 4, 5)).val);    // 3
        System.out.println(sol.findMiddle(buildList(1, 2, 3, 4, 5, 6)).val); // 4
        System.out.println(sol.findMiddle(buildList(1)).val);                  // 1

        System.out.println("\n=== Problem 4: Merge Two Sorted Lists ===");
        Node merged = sol.mergeTwoLists(buildList(1, 2, 4), buildList(1, 3, 4));
        System.out.println(listToString(merged)); // 1->1->2->3->4->4

        System.out.println(listToString(sol.mergeTwoLists(null, null)));       // null
        System.out.println(listToString(sol.mergeTwoLists(null, buildList(0)))); // 0

        System.out.println("\n=== Problem 5: Remove Nth From End ===");
        System.out.println(listToString(sol.removeNthFromEnd(buildList(1, 2, 3, 4, 5), 2))); // 1->2->3->5
        System.out.println(listToString(sol.removeNthFromEnd(buildList(1), 1)));               // null
        System.out.println(listToString(sol.removeNthFromEnd(buildList(1, 2), 1)));            // 1

        System.out.println("\n=== Problem 6: Intersection (simulated) ===");
        // Build: A: 4->1->8->4->5, B: 5->6->1->8->4->5 (intersect at node val=8)
        Node shared = new Node(8);
        shared.next = new Node(4); shared.next.next = new Node(5);
        Node headA = new Node(4); headA.next = new Node(1); headA.next.next = shared;
        Node headB = new Node(5); headB.next = new Node(6);
        headB.next.next = new Node(1); headB.next.next.next = shared;
        Node intersection = sol.getIntersectionNode(headA, headB);
        System.out.println(intersection != null ? intersection.val : "null"); // 8

        System.out.println("\n=== Problem 7: Palindrome Linked List ===");
        System.out.println(sol.isPalindrome(buildList(1, 2, 2, 1))); // true
        System.out.println(sol.isPalindrome(buildList(1, 2)));         // false
        System.out.println(sol.isPalindrome(buildList(1)));             // true
        System.out.println(sol.isPalindrome(buildList(1, 2, 3, 2, 1))); // true

        System.out.println("\n=== Problem 8: Remove Duplicates ===");
        System.out.println(listToString(sol.deleteDuplicates(buildList(1, 1, 2))));         // 1->2
        System.out.println(listToString(sol.deleteDuplicates(buildList(1, 1, 2, 3, 3))));  // 1->2->3
        System.out.println(listToString(sol.deleteDuplicates(buildList(1, 1, 1))));         // 1

        System.out.println("\n=== Problem 9: Add Two Numbers ===");
        // 342 + 465 = 807
        Node sum1 = sol.addTwoNumbers(buildList(2, 4, 3), buildList(5, 6, 4));
        System.out.println(listToString(sum1)); // 7->0->8

        // 9999999 + 9999 = 10009998
        Node sum2 = sol.addTwoNumbers(
            buildList(9, 9, 9, 9, 9, 9, 9),
            buildList(9, 9, 9, 9)
        );
        System.out.println(listToString(sum2)); // 8->9->9->9->0->0->0->1

        System.out.println("\n=== Problem 10: Reorder List ===");
        Node r1 = buildList(1, 2, 3, 4);
        sol.reorderList(r1);
        System.out.println(listToString(r1)); // 1->4->2->3

        Node r2 = buildList(1, 2, 3, 4, 5);
        sol.reorderList(r2);
        System.out.println(listToString(r2)); // 1->5->2->4->3
    }
}
