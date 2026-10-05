import java.util.*;

/**
 * PROBLEM: Merge Intervals
 * Given an array of intervals where intervals[i] = [start_i, end_i],
 * merge all overlapping intervals, and return the array of non-overlapping
 * intervals that cover all the intervals in the input.
 *
 * Examples:
 *   [[1,3],[2,6],[8,10],[15,18]] → [[1,6],[8,10],[15,18]]
 *   [[1,4],[4,5]]               → [[1,5]]    (touching = overlapping)
 *   [[1,4],[0,4]]               → [[0,4]]    (second contains first)
 *   [[1,4],[0,0]]               → [[0,0],[1,4]]  (no overlap)
 *   [[1,4]]                     → [[1,4]]    (single interval)
 *
 * APPROACH: Sort + Greedy
 *   1. Sort intervals by start time.
 *   2. Process intervals left to right.
 *      - If the current interval overlaps with the last merged interval
 *        (current.start <= lastMerged.end), extend the last merged interval's
 *        end to max(lastMerged.end, current.end).
 *      - Otherwise, add current interval to result (no overlap).
 *
 * WHY SORT FIRST?
 *   Without sorting, overlapping intervals may not be adjacent.
 *   After sorting by start time, if interval A comes before B (A.start <= B.start),
 *   they overlap if and only if A.end >= B.start.
 *   If they don't overlap, B.start > A.end and all subsequent intervals
 *   (sorted) also won't overlap with A.
 *
 * OVERLAP CONDITION: intervals[i].start <= intervals[i-1].end
 *   Note: [1,4] and [4,5] are considered overlapping (they share point 4).
 *   So we use <= not <.
 *
 * TRACE: [[1,3],[2,6],[8,10],[15,18]] (already sorted)
 *   Start: result=[]
 *   [1,3]: result empty → add [1,3]. result=[[1,3]]
 *   [2,6]: 2 <= 3 (overlap) → extend end: max(3,6)=6. result=[[1,6]]
 *   [8,10]: 8 > 6 (no overlap) → add [8,10]. result=[[1,6],[8,10]]
 *   [15,18]: 15 > 10 (no overlap) → add [15,18]. result=[[1,6],[8,10],[15,18]]
 *
 * TIME:  O(n log n) — dominated by sorting; merging is O(n)
 * SPACE: O(n) — result list (O(log n) if counting only extra space beyond output)
 */
public class MergeIntervals {

    /**
     * Merge overlapping intervals.
     *
     * @param intervals array of [start, end] pairs (may not be sorted)
     * @return array of merged non-overlapping intervals
     */
    public int[][] merge(int[][] intervals) {
        if (intervals == null || intervals.length <= 1) {
            return intervals;
        }

        // Step 1: Sort by start time
        // Comparator: intervals[i][0] is the start of interval i
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);

        // Step 2: Merge overlapping intervals
        List<int[]> merged = new ArrayList<>();

        for (int[] interval : intervals) {
            if (merged.isEmpty()) {
                // First interval — just add it
                merged.add(interval);
            } else {
                int[] last = merged.get(merged.size() - 1);

                if (interval[0] <= last[1]) {
                    // OVERLAP: current interval starts before (or at) the end of last merged
                    // Extend the end of last merged interval
                    last[1] = Math.max(last[1], interval[1]);
                    // Note: We don't need to update start — since intervals are sorted
                    // by start, current.start >= last.start, so last.start is already the minimum
                } else {
                    // NO OVERLAP: add current interval as a new merged interval
                    merged.add(interval);
                }
            }
        }

        return merged.toArray(new int[0][]);
    }

    /**
     * Variant: Count the number of meeting rooms needed (similar logic).
     * Given meeting intervals, find minimum number of conference rooms needed.
     *
     * APPROACH: Separate start and end times, use two-pointer sweep.
     * - Sort starts and ends independently.
     * - Sweep: if next start < next end → need new room; else → reuse a room.
     *
     * This is related to interval merging but different enough to note.
     */
    public int minMeetingRooms(int[][] intervals) {
        if (intervals == null || intervals.length == 0) return 0;

        int n = intervals.length;
        int[] starts = new int[n];
        int[] ends   = new int[n];

        for (int i = 0; i < n; i++) {
            starts[i] = intervals[i][0];
            ends[i]   = intervals[i][1];
        }

        Arrays.sort(starts);
        Arrays.sort(ends);

        int rooms = 0;
        int endPointer = 0;

        for (int startPointer = 0; startPointer < n; startPointer++) {
            if (starts[startPointer] < ends[endPointer]) {
                rooms++; // Need a new room
            } else {
                endPointer++; // Reuse a room that just freed up
            }
        }

        return rooms;
    }

    // -----------------------------------------------------------------------
    // Helper: Format int[][] for display
    // -----------------------------------------------------------------------
    private String format(int[][] arr) {
        if (arr == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append("[").append(arr[i][0]).append(",").append(arr[i][1]).append("]");
            if (i < arr.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean equals(int[][] a, int[][] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i][0] != b[i][0] || a[i][1] != b[i][1]) return false;
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Test cases
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        MergeIntervals solution = new MergeIntervals();

        System.out.println("=== Merge Intervals ===");
        System.out.println();

        Object[][] tests = {
            {new int[][]{{1,3},{2,6},{8,10},{15,18}}, new int[][]{{1,6},{8,10},{15,18}}},
            {new int[][]{{1,4},{4,5}},                 new int[][]{{1,5}}},
            {new int[][]{{1,4},{0,4}},                 new int[][]{{0,4}}},
            {new int[][]{{1,4},{0,0}},                 new int[][]{{0,0},{1,4}}},
            {new int[][]{{1,4}},                       new int[][]{{1,4}}},
            {new int[][]{{1,4},{2,3}},                 new int[][]{{1,4}}}, // contained
            {new int[][]{{2,3},{4,5},{6,7},{8,9},{1,10}}, new int[][]{{1,10}}}, // all merge
        };

        int passed = 0;
        for (Object[] test : tests) {
            int[][] input    = (int[][]) test[0];
            int[][] expected = (int[][]) test[1];
            int[][] result   = solution.merge(Arrays.stream(input)
                .map(int[]::clone).toArray(int[][]::new)); // Clone to avoid mutation
            boolean ok = solution.equals(result, expected);
            if (ok) passed++;
            System.out.println("Input:    " + solution.format(input));
            System.out.println("Expected: " + solution.format(expected));
            System.out.println("Result:   " + solution.format(result) + "  " + (ok ? "✓" : "✗"));
            System.out.println();
        }
        System.out.printf("Passed: %d/%d%n", passed, tests.length);

        System.out.println();
        System.out.println("=== Trace: [[1,3],[2,6],[8,10],[15,18]] ===");
        System.out.println("After sorting: [[1,3],[2,6],[8,10],[15,18]] (already sorted)");
        System.out.println();
        System.out.println("Process [1,3]:  merged empty → add [1,3]         merged=[[1,3]]");
        System.out.println("Process [2,6]:  2 <= 3 → overlap → extend end to max(3,6)=6  merged=[[1,6]]");
        System.out.println("Process [8,10]: 8 > 6  → no overlap → add [8,10] merged=[[1,6],[8,10]]");
        System.out.println("Process [15,18]:15 > 10 → no overlap → add [15,18] merged=[[1,6],[8,10],[15,18]]");
        System.out.println();

        System.out.println("=== Meeting Rooms ===");
        int[][] meetings = {{0,30},{5,10},{15,20}};
        System.out.println("Meetings: " + solution.format(meetings));
        System.out.println("Rooms needed: " + solution.minMeetingRooms(meetings));
        System.out.println("Expected: 2");
    }
}
