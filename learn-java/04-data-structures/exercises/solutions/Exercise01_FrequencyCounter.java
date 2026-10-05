import java.util.*;
import java.util.stream.Collectors;

/**
 * Solution to Exercise01_FrequencyCounter
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

    public static Map<String, Integer> countFrequencies(String text, boolean excludeStopWords) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.toLowerCase().split("[\\s.,!?;:'\"()\\-]+")) {
            if (word.isEmpty()) continue;
            if (excludeStopWords && STOP_WORDS.contains(word)) continue;
            freq.merge(word, 1, Integer::sum);
        }
        return freq;
    }

    public static List<String> getTopN(Map<String, Integer> frequencies, int n) {
        return frequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                .thenComparing(Map.Entry.comparingByKey()))
            .limit(n)
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.toList());
    }

    public static List<String> getHapaxLegomena(Map<String, Integer> frequencies) {
        return frequencies.entrySet().stream()
            .filter(e -> e.getValue() == 1)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
    }

    public static Map<String, Integer> getAboveThreshold(
            Map<String, Integer> frequencies, int threshold) {
        return frequencies.entrySet().stream()
            .filter(e -> e.getValue() > threshold)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<Character, Integer> charFrequency(String text) {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toLowerCase().toCharArray()) {
            if (c != ' ') freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }

    public static void main(String[] args) {
        Map<String, Integer> allWords    = countFrequencies(SAMPLE_TEXT, false);
        Map<String, Integer> noStopWords = countFrequencies(SAMPLE_TEXT, true);

        System.out.println("Total unique words (with stop words): " + allWords.size());
        System.out.println("Total unique words (without stop words): " + noStopWords.size());

        System.out.println("\nTop 5 words (with stop words):");
        getTopN(allWords, 5).forEach(s -> System.out.println("  " + s));

        System.out.println("\nTop 5 words (without stop words):");
        getTopN(noStopWords, 5).forEach(s -> System.out.println("  " + s));

        System.out.println("\nWords appearing exactly once (no stop words):");
        System.out.println("  " + getHapaxLegomena(noStopWords));

        System.out.println("\nWords appearing 3+ times (no stop words):");
        getAboveThreshold(noStopWords, 2).entrySet().stream()
            .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
            .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
    }
}
