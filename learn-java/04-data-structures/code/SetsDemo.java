import java.util.*;
import java.util.stream.Collectors;

/**
 * SetsDemo.java
 *
 * Comprehensive demonstration of Java Set operations:
 * - HashSet basics and uniqueness enforcement
 * - Set operations: union, intersection, difference
 * - LinkedHashSet: ordered uniqueness
 * - TreeSet: sorted unique elements
 * - Real-world deduplication scenarios
 */
public class SetsDemo {

    // =========================================================
    // 1. HashSet Basics
    // =========================================================

    static void hashSetBasics() {
        System.out.println("=== HashSet Basics ===\n");

        Set<String> tags = new HashSet<>();

        // Adding elements
        tags.add("java");
        tags.add("programming");
        tags.add("tutorial");
        tags.add("java");        // Duplicate! Not added.
        tags.add("JAVA");        // Different case — this IS added (Sets are case-sensitive)

        System.out.println("Tags: " + tags);
        System.out.println("Size (only 4, not 5): " + tags.size());

        // Checking membership — O(1) average
        System.out.println("\nContains 'java': " + tags.contains("java"));
        System.out.println("Contains 'python': " + tags.contains("python"));

        // Removing
        tags.remove("JAVA");
        System.out.println("After removing 'JAVA': " + tags);

        // Adding multiple
        tags.addAll(List.of("spring", "maven", "gradle", "java")); // "java" ignored
        System.out.println("After addAll: " + tags);

        // Iteration — no guaranteed order in HashSet!
        System.out.println("\nIterating (order not guaranteed):");
        for (String tag : tags) {
            System.out.print("  " + tag);
        }
        System.out.println();
    }

    // =========================================================
    // 2. Set Operations: Union, Intersection, Difference
    // =========================================================

    static void setOperations() {
        System.out.println("\n=== Set Operations ===\n");

        Set<Integer> setA = new HashSet<>(Set.of(1, 2, 3, 4, 5));
        Set<Integer> setB = new HashSet<>(Set.of(3, 4, 5, 6, 7));

        System.out.println("Set A: " + sorted(setA));
        System.out.println("Set B: " + sorted(setB));

        // Union: all elements from either set
        Set<Integer> union = new HashSet<>(setA);
        union.addAll(setB);
        System.out.println("\nUnion (A ∪ B): " + sorted(union));

        // Intersection: elements in both sets
        Set<Integer> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        System.out.println("Intersection (A ∩ B): " + sorted(intersection));

        // Difference: elements in A but not in B
        Set<Integer> differenceAB = new HashSet<>(setA);
        differenceAB.removeAll(setB);
        System.out.println("Difference (A - B): " + sorted(differenceAB));

        // Difference: elements in B but not in A
        Set<Integer> differenceBA = new HashSet<>(setB);
        differenceBA.removeAll(setA);
        System.out.println("Difference (B - A): " + sorted(differenceBA));

        // Symmetric Difference: elements in either but not both
        Set<Integer> symmetricDiff = new HashSet<>(union);
        symmetricDiff.removeAll(intersection);
        System.out.println("Symmetric Difference (A △ B): " + sorted(symmetricDiff));

        // Check subset
        Set<Integer> subset = new HashSet<>(Set.of(3, 4));
        System.out.println("\n{3,4} is subset of A: " + setA.containsAll(subset));
        System.out.println("{3,4} is subset of B: " + setB.containsAll(subset));

        // Check disjoint (no elements in common)
        Set<Integer> setC = new HashSet<>(Set.of(10, 20, 30));
        System.out.println("\nA and C are disjoint: " + Collections.disjoint(setA, setC));
        System.out.println("A and B are disjoint: " + Collections.disjoint(setA, setB));

        // NOTE: Always use copies for set operations — they modify the collection!
        // The original setA and setB are unchanged above because we operated on copies.
        System.out.println("\nSetA unchanged: " + sorted(setA));
        System.out.println("SetB unchanged: " + sorted(setB));
    }

    private static <T extends Comparable<T>> List<T> sorted(Set<T> set) {
        List<T> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    // =========================================================
    // 3. Deduplication Use Cases
    // =========================================================

    static void deduplicationExamples() {
        System.out.println("\n=== Deduplication Examples ===\n");

        // --- De-duplicating a list ---
        List<String> rawTags = List.of("java", "spring", "java", "maven", "spring",
                                       "junit", "java", "gradle");
        System.out.println("Raw tags: " + rawTags);

        // Fast dedup (loses order)
        Set<String> uniqueTagsSet = new HashSet<>(rawTags);
        System.out.println("Unique (HashSet, any order): " + uniqueTagsSet);

        // Dedup preserving insertion order
        Set<String> uniqueTagsOrdered = new LinkedHashSet<>(rawTags);
        System.out.println("Unique (LinkedHashSet, insertion order): " + uniqueTagsOrdered);

        // Dedup with stream
        List<String> uniqueList = rawTags.stream().distinct().collect(Collectors.toList());
        System.out.println("Unique (stream distinct): " + uniqueList);

        // --- Finding duplicates ---
        List<String> withDups = List.of("alice", "bob", "alice", "charlie", "bob", "bob");
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = withDups.stream()
            .filter(s -> !seen.add(s))  // add() returns false if already present
            .collect(Collectors.toSet());
        System.out.println("\nDuplicate names: " + duplicates);

        // --- Tracking visited URLs (web crawler) ---
        Set<String> visited = new HashSet<>();
        String[] urlsToCrawl = {
            "https://example.com",
            "https://example.com/page1",
            "https://example.com",       // Already visited
            "https://example.com/page2",
            "https://example.com/page1", // Already visited
        };

        System.out.println("\nCrawling URLs:");
        for (String url : urlsToCrawl) {
            if (visited.add(url)) {  // add() returns true if newly added
                System.out.println("  Crawling: " + url);
            } else {
                System.out.println("  SKIP (already visited): " + url);
            }
        }
        System.out.println("Total unique pages crawled: " + visited.size());
    }

    // =========================================================
    // 4. LinkedHashSet: Ordered Uniqueness
    // =========================================================

    static void linkedHashSetDemo() {
        System.out.println("\n=== LinkedHashSet: Ordered Uniqueness ===\n");

        // Scenario: user selects items but we don't want duplicates
        // AND we want to preserve the order of selection
        LinkedHashSet<String> selectedItems = new LinkedHashSet<>();
        selectedItems.add("Laptop");
        selectedItems.add("Mouse");
        selectedItems.add("Keyboard");
        selectedItems.add("Laptop");    // Duplicate — ignored
        selectedItems.add("Monitor");
        selectedItems.add("Mouse");     // Duplicate — ignored

        System.out.println("Selected items (insertion order, no duplicates):");
        selectedItems.forEach(item -> System.out.println("  " + item));

        // Recent searches — last N unique searches
        LinkedHashSet<String> recentSearches = new LinkedHashSet<>() {
            private static final int MAX_SIZE = 5;
            @Override
            public boolean add(String element) {
                remove(element);        // Remove if exists (to re-add at end)
                if (size() >= MAX_SIZE) {
                    remove(iterator().next());  // Remove oldest
                }
                return super.add(element);
            }
        };

        String[] searches = {"java", "python", "java", "go", "rust", "kotlin", "java"};
        for (String s : searches) {
            recentSearches.add(s);
            System.out.println("Search '" + s + "' → recent: " + recentSearches);
        }
    }

    // =========================================================
    // 5. TreeSet: Sorted Unique Elements
    // =========================================================

    static void treeSetDemo() {
        System.out.println("\n=== TreeSet: Sorted Unique Elements ===\n");

        // Default: natural order (alphabetical for strings)
        TreeSet<String> words = new TreeSet<>();
        words.addAll(List.of("banana", "apple", "cherry", "date", "elderberry", "apple"));

        System.out.println("Words (sorted, no dups): " + words);
        System.out.println("First: " + words.first());
        System.out.println("Last: " + words.last());

        // Navigation methods
        System.out.println("Words before 'cherry': " + words.headSet("cherry"));
        System.out.println("Words from 'cherry': " + words.tailSet("cherry"));
        System.out.println("Words from 'banana' to 'date': " + words.subSet("banana", "date"));

        System.out.println("Floor of 'cat': " + words.floor("cat"));     // Largest <= "cat"
        System.out.println("Ceiling of 'cat': " + words.ceiling("cat")); // Smallest >= "cat"
        System.out.println("Lower than 'cherry': " + words.lower("cherry")); // Strictly <
        System.out.println("Higher than 'cherry': " + words.higher("cherry")); // Strictly >

        // Custom order: integers sorted in reverse
        TreeSet<Integer> highScores = new TreeSet<>(Comparator.reverseOrder());
        highScores.addAll(List.of(75, 92, 88, 61, 99, 85, 73));

        System.out.println("\nHigh scores (descending): " + highScores);
        System.out.println("Top score: " + highScores.first());  // 99 (highest in reverse order)

        // Top 3 scores
        List<Integer> top3 = highScores.stream().limit(3).collect(Collectors.toList());
        System.out.println("Top 3 scores: " + top3);

        // Sorted unique words from text
        String passage = "to be or not to be that is the question";
        TreeSet<String> uniqueWords = new TreeSet<>(Arrays.asList(passage.split("\\s+")));
        System.out.println("\nUnique words (sorted): " + uniqueWords);
    }

    // =========================================================
    // 6. Real-World: Permission System
    // =========================================================

    enum Permission {
        READ, WRITE, EXECUTE, DELETE, ADMIN
    }

    static class Role {
        private final String name;
        private final Set<Permission> permissions;

        Role(String name, Permission... perms) {
            this.name = name;
            this.permissions = EnumSet.copyOf(Arrays.asList(perms));
        }

        boolean can(Permission p) { return permissions.contains(p); }
        Set<Permission> getPermissions() { return Collections.unmodifiableSet(permissions); }
        String getName() { return name; }
    }

    static void permissionSystemDemo() {
        System.out.println("\n=== Permission System ===\n");

        Role viewer = new Role("Viewer", Permission.READ);
        Role editor = new Role("Editor", Permission.READ, Permission.WRITE);
        Role admin  = new Role("Admin",  Permission.READ, Permission.WRITE,
                                         Permission.DELETE, Permission.EXECUTE, Permission.ADMIN);

        // Check permissions
        System.out.println("Viewer can READ: " + viewer.can(Permission.READ));
        System.out.println("Viewer can WRITE: " + viewer.can(Permission.WRITE));
        System.out.println("Admin can DELETE: " + admin.can(Permission.DELETE));

        // Permissions a user has (combining roles)
        Set<Permission> userPerms = new HashSet<>();
        userPerms.addAll(viewer.getPermissions());
        userPerms.addAll(editor.getPermissions()); // Set handles duplicates automatically
        System.out.println("\nUser with Viewer + Editor roles has: " + userPerms);

        // What permissions does editor have that viewer doesn't?
        Set<Permission> editorOnly = new HashSet<>(editor.getPermissions());
        editorOnly.removeAll(viewer.getPermissions());
        System.out.println("Editor-only permissions: " + editorOnly);
    }

    // =========================================================
    // Main
    // =========================================================

    public static void main(String[] args) {
        hashSetBasics();
        setOperations();
        deduplicationExamples();
        linkedHashSetDemo();
        treeSetDemo();
        permissionSystemDemo();
    }
}
