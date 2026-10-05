import java.util.*;
import java.util.stream.Collectors;

/**
 * Exercise01_FrequencyCounter.java
 *
 * GOAL: Given a string of text, find and report the most frequent words.
 *
 * TASKS:
 * 1. Split the text into words (case-insensitive, ignore punctuation)
 * 2. Count frequency of each word using a HashMap
 * 3. Find the top N most frequent words
 * 4. Find all words that appear exactly once
 * 5. Find words that appear more than a given threshold
 *
 * BONUS:
 * - Ignore common "stop words" (the, a, an, is, ...)
 * - Sort ties alphabetically
 */
public class Exercise01_FrequencyCounter {

    private static final Set<String> STOP_WORDS = Set.of(
        "the", "a", "an", "is", "it", "in", "of", "and", "to", "that",
        "was", "he", "she", "they", "but", "for", "with", "as", "at",
        "be", "this", "from", "or", "have", "had", "his", "her", "on",
        "are", "were", "by", "not", "what", "so", "if", "all", "we",
        "i", "you", "my", "do", "did", "has", "will", "would", "been"
    );

    private static final String SAMPLE_TEXT =
        "Java is a high-level object-oriented programming language. " +
        "Java programs are compiled to bytecode that can run on any Java Virtual Machine. " +
        "Java was created by James Gosling at Sun Microsystems and released in 1995. " +
        "Java remains one of the most popular programming languages in the world. " +
        "Java is widely used for web applications, mobile applications, and enterprise systems. " +
        "The Java programming language is designed to be portable and platform-independent. " +
        "Java programs run on billions of devices worldwide.";

    // =========================================================
    // TODO 1: Count word frequencies
    //
    // - Split text into words using split("[\\s.,!?;:'\"()-]+")
    // - Convert to lowercase
    // - Skip empty strings and stop words
    // - Return a Map<String, Integer> of word -> count
    // =========================================================
    public static Map<String, Integer> countFrequencies(String text, boolean excludeStopWords) {
        // YOUR CODE HERE
        // Hint: Use String.toLowerCase() and split()
        // Hint: Use map.merge(word, 1, Integer::sum)
        return new HashMap<>();
    }

    // =========================================================
    // TODO 2: Get top N words by frequency
    //
    // Return a List of the N most frequent words (as "word: count" strings)
    // For ties in count, sort alphabetically by word
    // =========================================================
    public static List<String> getTopN(Map<String, Integer> frequencies, int n) {
        // YOUR CODE HERE
        // Hint: Use stream().sorted() with a Comparator
        // Hint: Map.Entry.comparingByValue().reversed() for descending by count
        // Hint: Then thenComparing(Map.Entry::getKey) for alphabetical ties
        return new ArrayList<>();
    }

    // =========================================================
    // TODO 3: Find hapax legomena (words appearing exactly once)
    //
    // A "hapax legomenon" is a word that appears only once in a text.
    // Return sorted alphabetically.
    // =========================================================
    public static List<String> getHapaxLegomena(Map<String, Integer> frequencies) {
        // YOUR CODE HERE
        // Hint: filter entries where value == 1
        return new ArrayList<>();
    }

    // =========================================================
    // TODO 4: Find words above threshold
    // =========================================================
    public static Map<String, Integer> getAboveThreshold(
            Map<String, Integer> frequencies, int threshold) {
        // YOUR CODE HERE
        return new HashMap<>();
    }

    // =========================================================
    // TODO 5: Get character frequency (bonus)
    //
    // Count frequency of each character (ignoring spaces)
    // =========================================================
    public static Map<Character, Integer> charFrequency(String text) {
        // YOUR CODE HERE
        return new HashMap<>();
    }

    // =========================================================
    // Main - Test your implementation
    // =========================================================
    public static void main(String[] args) {
        System.out.println("=== Frequency Counter ===\n");
        System.out.println("Text:\n" + SAMPLE_TEXT + "\n");

        // Test countFrequencies
        Map<String, Integer> allWords = countFrequencies(SAMPLE_TEXT, false);
        Map<String, Integer> noStopWords = countFrequencies(SAMPLE_TEXT, true);

        System.out.println("Total unique words (with stop words): " + allWords.size());
        System.out.println("Total unique words (without stop words): " + noStopWords.size());

        // Test getTopN
        System.out.println("\nTop 5 words (with stop words):");
        getTopN(allWords, 5).forEach(s -> System.out.println("  " + s));

        System.out.println("\nTop 5 words (without stop words):");
        getTopN(noStopWords, 5).forEach(s -> System.out.println("  " + s));

        // Test hapax legomena
        System.out.println("\nWords appearing exactly once (no stop words):");
        System.out.println("  " + getHapaxLegomena(noStopWords));

        // Test threshold
        System.out.println("\nWords appearing 3+ times (no stop words):");
        getAboveThreshold(noStopWords, 3).forEach((w, c) ->
            System.out.println("  " + w + ": " + c));

        // Expected output hints:
        // "java" should be the most frequent word
        // Count of "java" should be >= 5
    }
}
