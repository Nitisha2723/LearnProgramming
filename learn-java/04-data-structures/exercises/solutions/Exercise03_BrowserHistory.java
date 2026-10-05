import java.util.*;
import java.util.stream.Collectors;

/**
 * Solution to Exercise03_BrowserHistory
 */
public class Exercise03_BrowserHistory {

    static class BrowserHistory {
        private final Deque<String> backStack    = new ArrayDeque<>();
        private final Deque<String> forwardStack = new ArrayDeque<>();
        private String currentUrl;
        private final Map<String, Integer> visitCount = new HashMap<>();

        public BrowserHistory(String homepage) {
            this.currentUrl = homepage;
            visitCount.put(homepage, 1);
        }

        public void visit(String url) {
            backStack.push(currentUrl);
            forwardStack.clear();
            currentUrl = url;
            visitCount.merge(url, 1, Integer::sum);
        }

        public String back(int steps) {
            for (int i = 0; i < steps && !backStack.isEmpty(); i++) {
                forwardStack.push(currentUrl);
                currentUrl = backStack.pop();
            }
            return currentUrl;
        }

        public String forward(int steps) {
            for (int i = 0; i < steps && !forwardStack.isEmpty(); i++) {
                backStack.push(currentUrl);
                currentUrl = forwardStack.pop();
            }
            return currentUrl;
        }

        public String getCurrentUrl() { return currentUrl; }

        public List<String> getHistory() {
            // backStack is LIFO: most recent at top, oldest at bottom
            // We need to reverse it to get chronological order
            List<String> history = new ArrayList<>();
            List<String> backList = new ArrayList<>(backStack);
            Collections.reverse(backList);     // oldest first
            history.addAll(backList);
            history.add(currentUrl);
            return history;
        }

        public String getMostVisited() {
            return visitCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
        }

        public List<String> getTopNVisited(int n) {
            return visitCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }

        public boolean canGoBack()    { return !backStack.isEmpty(); }
        public boolean canGoForward() { return !forwardStack.isEmpty(); }

        public void printStatus() {
            System.out.println("Current: " + currentUrl);
            System.out.println("Can back: " + canGoBack() + ", Can forward: " + canGoForward());
        }
    }

    public static void main(String[] args) {
        BrowserHistory browser = new BrowserHistory("https://google.com");

        browser.visit("https://java.com");
        browser.visit("https://stackoverflow.com");
        browser.visit("https://github.com");
        browser.visit("https://docs.oracle.com");

        System.out.println("Current: " + browser.getCurrentUrl());
        System.out.println("History: " + browser.getHistory());

        String url = browser.back(2);
        System.out.println("\nAfter back(2): " + url);
        browser.printStatus();

        url = browser.forward(1);
        System.out.println("\nAfter forward(1): " + url);

        browser.visit("https://maven.apache.org");
        System.out.println("After visiting maven: " + browser.getCurrentUrl());
        System.out.println("Can go forward: " + browser.canGoForward());

        browser.back(100);
        System.out.println("\nAfter back(100): " + browser.getCurrentUrl());

        browser.visit("https://google.com");
        browser.visit("https://github.com");
        browser.visit("https://google.com");
        browser.visit("https://github.com");
        browser.visit("https://google.com");

        System.out.println("\nMost visited: " + browser.getMostVisited());
        System.out.println("Top 3 visited: " + browser.getTopNVisited(3));
    }
}
