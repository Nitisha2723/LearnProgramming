import java.util.*;

/**
 * Exercise03_BrowserHistory.java
 *
 * GOAL: Implement a browser history manager using stacks.
 *
 * TASKS:
 * 1. Implement visit(url) — navigate to a new URL
 * 2. Implement back(steps) — go back N steps
 * 3. Implement forward(steps) — go forward N steps
 * 4. Implement getHistory() — return full navigation history
 * 5. Implement getMostVisited() — return the most visited URL
 *
 * RULES:
 * - Visiting a new URL clears the forward stack (just like real browsers)
 * - back/forward can't go beyond available history
 * - Track visit count per URL for getMostVisited()
 */
public class Exercise03_BrowserHistory {

    static class BrowserHistory {

        // TODO: Declare your data structures
        // You'll need:
        // - A stack for back history
        // - A stack for forward history
        // - The current URL
        // - A map to track visit counts per URL

        // YOUR FIELDS HERE

        public BrowserHistory(String homepage) {
            // TODO: Initialize fields
            // The homepage is the starting point (no back history)
        }

        // =======================================================
        // TODO 1: Visit a URL
        // - Push current URL to back stack
        // - Clear the forward stack (new navigation kills forward history)
        // - Update current URL
        // - Increment visit count for the new URL
        // =======================================================
        public void visit(String url) {
            // YOUR CODE HERE
        }

        // =======================================================
        // TODO 2: Go back N steps
        // - Go back as many steps as available (don't go below start)
        // - Return the URL we ended up at
        // =======================================================
        public String back(int steps) {
            // YOUR CODE HERE
            return "";
        }

        // =======================================================
        // TODO 3: Go forward N steps
        // - Go forward as many steps as available
        // - Return the URL we ended up at
        // =======================================================
        public String forward(int steps) {
            // YOUR CODE HERE
            return "";
        }

        // =======================================================
        // TODO 4: Get the current URL
        // =======================================================
        public String getCurrentUrl() {
            // YOUR CODE HERE
            return "";
        }

        // =======================================================
        // TODO 5: Get full browsing history (in order visited)
        // - Return a List of all URLs visited, oldest first
        // - Include the current URL at the end
        // - Don't include forward stack (unvisited future)
        //
        // Hint: The back stack is LIFO — you'll need to reverse it
        //       to get chronological order
        // =======================================================
        public List<String> getHistory() {
            // YOUR CODE HERE
            return new ArrayList<>();
        }

        // =======================================================
        // TODO 6: Get the most visited URL
        // =======================================================
        public String getMostVisited() {
            // YOUR CODE HERE
            return "";
        }

        // =======================================================
        // TODO 7 (BONUS): Get the N most visited URLs
        // Return sorted by visit count descending, then alphabetically for ties
        // =======================================================
        public List<String> getTopNVisited(int n) {
            // YOUR CODE HERE
            return new ArrayList<>();
        }

        public boolean canGoBack()    { return false; /* YOUR CODE */ }
        public boolean canGoForward() { return false; /* YOUR CODE */ }

        public void printStatus() {
            System.out.println("Current: " + getCurrentUrl());
            System.out.println("Can back: " + canGoBack() + ", Can forward: " + canGoForward());
        }
    }

    // =========================================================
    // Main — Test your implementation
    // =========================================================
    public static void main(String[] args) {
        System.out.println("=== Browser History ===\n");

        BrowserHistory browser = new BrowserHistory("https://google.com");

        // Navigate around
        browser.visit("https://java.com");
        browser.visit("https://stackoverflow.com");
        browser.visit("https://github.com");
        browser.visit("https://docs.oracle.com");

        System.out.println("Current: " + browser.getCurrentUrl());
        // Expected: https://docs.oracle.com

        System.out.println("History: " + browser.getHistory());
        // Expected: [google, java, stackoverflow, github, docs.oracle]

        // Go back 2 steps
        String url = browser.back(2);
        System.out.println("\nAfter back(2): " + url);
        // Expected: https://stackoverflow.com

        browser.printStatus();

        // Go forward 1 step
        url = browser.forward(1);
        System.out.println("\nAfter forward(1): " + url);
        // Expected: https://github.com

        // Visit a new URL — clears forward
        browser.visit("https://maven.apache.org");
        System.out.println("After visiting maven: " + browser.getCurrentUrl());
        System.out.println("Can go forward: " + browser.canGoForward());
        // Expected: false (forward stack cleared)

        // Try to go back more steps than available
        browser.back(100);  // Should stop at homepage
        System.out.println("\nAfter back(100): " + browser.getCurrentUrl());
        // Expected: https://google.com (can't go further back)

        // Visit multiple times to test getMostVisited
        browser.visit("https://google.com");
        browser.visit("https://github.com");
        browser.visit("https://google.com");
        browser.visit("https://github.com");
        browser.visit("https://google.com");

        System.out.println("\nMost visited: " + browser.getMostVisited());
        // Expected: https://google.com

        System.out.println("Top 3 visited: " + browser.getTopNVisited(3));
    }
}
