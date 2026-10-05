/**
 * StringsDemo.java
 *
 * Demonstrates String concepts in Java:
 * - Strings are objects, not primitives
 * - Immutability in action
 * - == vs .equals() — the most common Java bug!
 * - All essential String methods
 * - StringBuilder for performance
 * - String.format() and printf
 * - Conversions between String and other types
 *
 * Read theory/04-strings-in-depth.md before studying this file.
 *
 * How to run:
 *   javac StringsDemo.java
 *   java StringsDemo
 */
public class StringsDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  STRINGS DEMO");
        System.out.println("=================================================\n");

        demoStringsAreObjects();
        demoImmutability();
        demoEqualsVsDoubleEquals();       // THE CRITICAL ONE
        demoLengthAndCharAccess();
        demoSearchingInStrings();
        demoExtractingParts();
        demoTransforming();
        demoSplittingAndJoining();
        demoStringBuilder();
        demoFormatting();
        demoConversions();
    }

    // -------------------------------------------------------------------------
    // SECTION 1: Strings Are Objects
    // -------------------------------------------------------------------------
    static void demoStringsAreObjects() {
        System.out.println("--- Strings Are Objects ---");

        // Primitive (stored by value, lowercase type)
        int age = 25;

        // String (stored as reference, capital S, it's a CLASS)
        String name = "Alice";

        // Because String is a class, you can call methods on it
        System.out.println("name = \"" + name + "\"");
        System.out.println("name.length() = " + name.length()); // calling a method!
        System.out.println("name.toUpperCase() = " + name.toUpperCase());

        // You can't call methods on a primitive
        // age.toString() would be a compile error
        // But you CAN call static methods:
        System.out.println("String.valueOf(age) = " + String.valueOf(age));

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 2: Immutability
    // String values never change — operations return NEW strings
    // -------------------------------------------------------------------------
    static void demoImmutability() {
        System.out.println("--- Immutability ---");

        String s = "Hello";
        System.out.println("Original: " + s);

        // toUpperCase() does NOT modify s — it returns a NEW string
        String upper = s.toUpperCase();
        System.out.println("After toUpperCase(): s is still \"" + s + "\"");
        System.out.println("The new string: \"" + upper + "\"");

        // Concatenation creates a new string — s is unchanged
        String longer = s + ", World!";
        System.out.println("After concatenation: s is still \"" + s + "\"");
        System.out.println("The new string: \"" + longer + "\"");

        // When you "change" a String variable, you're changing what it POINTS TO
        s = s.toUpperCase(); // s now points to "HELLO"
        System.out.println("After s = s.toUpperCase(): s is now \"" + s + "\"");
        System.out.println("(The original \"Hello\" object still exists in memory, just no longer referenced)");

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 3: == vs .equals() — THE MOST IMPORTANT DISTINCTION
    //
    //   ==         compares REFERENCES (memory addresses)
    //   .equals()  compares CONTENT (the actual characters)
    //
    // ALWAYS use .equals() to compare String content!
    // -------------------------------------------------------------------------
    static void demoEqualsVsDoubleEquals() {
        System.out.println("--- == vs .equals() --- *** READ THIS CAREFULLY ***");

        // String literals go into the String Pool — Java reuses the same object
        String a = "hello";
        String b = "hello";

        // new String() bypasses the pool — creates a new heap object
        String c = new String("hello");

        System.out.println("a = \"hello\" (pool)");
        System.out.println("b = \"hello\" (pool)");
        System.out.println("c = new String(\"hello\") (new heap object)");
        System.out.println();

        // == compares REFERENCES
        System.out.println("== compares references (memory addresses):");
        System.out.println("  a == b: " + (a == b)); // true  (same pool object)
        System.out.println("  a == c: " + (a == c)); // false (c is a different object)
        System.out.println("  b == c: " + (b == c)); // false

        // .equals() compares CONTENT
        System.out.println("\n.equals() compares content:");
        System.out.println("  a.equals(b): " + a.equals(b)); // true
        System.out.println("  a.equals(c): " + a.equals(c)); // true  ← correct!
        System.out.println("  b.equals(c): " + b.equals(c)); // true

        // Real-world danger: user input always creates new String objects
        String userInput = new String("quit"); // simulate Scanner input
        System.out.println("\nSimulated user input comparison:");
        System.out.println("  userInput == \"quit\":        " + (userInput == "quit")); // false! Bug!
        System.out.println("  userInput.equals(\"quit\"):   " + userInput.equals("quit")); // true

        // Pro tip: put the known string first to avoid NullPointerException
        String maybeNull = null;
        System.out.println("\nNull-safe comparison (known string first):");
        // maybeNull.equals("quit") would throw NullPointerException
        System.out.println("  \"quit\".equals(maybeNull): " + "quit".equals(maybeNull)); // false, no NPE

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 4: Length and Character Access
    // -------------------------------------------------------------------------
    static void demoLengthAndCharAccess() {
        System.out.println("--- Length and Character Access ---");

        String s = "Hello, World!";
        System.out.println("String: \"" + s + "\"");
        System.out.println("Indices:  " + getIndexString(s.length()));

        System.out.println("length():     " + s.length());    // 13
        System.out.println("charAt(0):    " + s.charAt(0));   // 'H'
        System.out.println("charAt(7):    " + s.charAt(7));   // 'W'
        System.out.println("charAt(12):   " + s.charAt(12));  // '!'
        System.out.println("isEmpty():    " + s.isEmpty());   // false
        System.out.println("\"\".isEmpty(): " + "".isEmpty()); // true
        System.out.println("\"  \".isBlank(): " + "  ".isBlank()); // true (Java 11+)

        System.out.println();
    }

    static String getIndexString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(i % 10);
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // SECTION 5: Searching Within Strings
    // -------------------------------------------------------------------------
    static void demoSearchingInStrings() {
        System.out.println("--- Searching Within Strings ---");

        String s = "The quick brown fox jumps over the lazy dog";
        System.out.println("String: \"" + s + "\"");

        System.out.println("\nindexOf:");
        System.out.println("  indexOf('o'):        " + s.indexOf('o'));       // 12
        System.out.println("  lastIndexOf('o'):    " + s.lastIndexOf('o'));   // 41
        System.out.println("  indexOf(\"fox\"):      " + s.indexOf("fox"));   // 16
        System.out.println("  indexOf(\"xyz\"):      " + s.indexOf("xyz"));   // -1 (not found)
        System.out.println("  indexOf('o', 13):    " + s.indexOf('o', 13)); // 17 (start from 13)

        System.out.println("\ncontains / starts / ends:");
        System.out.println("  contains(\"fox\"):     " + s.contains("fox"));          // true
        System.out.println("  startsWith(\"The\"):   " + s.startsWith("The"));        // true
        System.out.println("  endsWith(\"dog\"):     " + s.endsWith("dog"));          // true
        System.out.println("  contains(\"cat\"):     " + s.contains("cat"));          // false

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 6: Extracting Parts
    // -------------------------------------------------------------------------
    static void demoExtractingParts() {
        System.out.println("--- Extracting Parts (substring) ---");

        String s = "Hello, World!";
        //           0123456789012
        System.out.println("String: \"" + s + "\"");

        // substring(startIndex) — from startIndex to end
        System.out.println("substring(7):      \"" + s.substring(7) + "\"");  // "World!"

        // substring(startIndex, endIndex) — startIndex inclusive, endIndex EXCLUSIVE
        System.out.println("substring(7, 12):  \"" + s.substring(7, 12) + "\""); // "World"
        System.out.println("substring(0, 5):   \"" + s.substring(0, 5) + "\""); // "Hello"

        // Practical: extract parts of a formatted string
        String date = "2024-03-15";
        String year  = date.substring(0, 4);  // "2024"
        String month = date.substring(5, 7);  // "03"
        String day   = date.substring(8);     // "15"
        System.out.println("\nParsing date \"" + date + "\":");
        System.out.println("  Year:  " + year);
        System.out.println("  Month: " + month);
        System.out.println("  Day:   " + day);

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 7: Transforming Strings
    // -------------------------------------------------------------------------
    static void demoTransforming() {
        System.out.println("--- Transforming Strings ---");

        String s = "  Hello, World!  ";
        System.out.println("Original:       \"" + s + "\"");
        System.out.println("toLowerCase():  \"" + s.toLowerCase() + "\"");
        System.out.println("toUpperCase():  \"" + s.toUpperCase() + "\"");
        System.out.println("trim():         \"" + s.trim() + "\"");
        System.out.println("strip():        \"" + s.strip() + "\""); // Java 11+

        String t = "Hello, World!";
        System.out.println("\nOriginal:               \"" + t + "\"");
        System.out.println("replace('l', 'r'):       \"" + t.replace('l', 'r') + "\"");
        System.out.println("replace(\"World\", \"Java\"): \"" + t.replace("World", "Java") + "\"");

        // replaceAll uses regex
        String messy = "  too   many   spaces  ";
        System.out.println("replaceAll(\"\\\\s+\", \" \").trim(): \"" + messy.replaceAll("\\s+", " ").trim() + "\"");

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 8: Splitting and Joining
    // -------------------------------------------------------------------------
    static void demoSplittingAndJoining() {
        System.out.println("--- Splitting and Joining ---");

        // split() — break a string into an array
        String csv = "Alice,Bob,Charlie,Dave,Eve";
        String[] names = csv.split(",");
        System.out.println("CSV: \"" + csv + "\"");
        System.out.print("split(\",\"): ");
        for (String name : names) {
            System.out.print("[" + name + "]");
        }
        System.out.println();

        // split with a limit
        String limited = csv.split(",", 3)[2]; // "Charlie,Dave,Eve" — remaining after 2 splits
        System.out.println("split(\",\", 3)[2]: \"" + limited + "\"");

        // split on whitespace
        String sentence = "Java is fun to learn";
        String[] words = sentence.split("\\s+");
        System.out.println("\nSplitting \"" + sentence + "\" on whitespace:");
        System.out.println("Words: " + words.length);

        // String.join() — the inverse of split
        String joined = String.join(", ", names);
        System.out.println("\nString.join(\", \", names): \"" + joined + "\"");

        String hyphenated = String.join("-", "2024", "03", "15");
        System.out.println("String.join(\"-\", ...): \"" + hyphenated + "\"");

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 9: StringBuilder
    // Use when building a String in a loop or with many appends
    // -------------------------------------------------------------------------
    static void demoStringBuilder() {
        System.out.println("--- StringBuilder ---");

        // BAD approach: creates many intermediate String objects
        // (fine for small loops, very slow for large ones)
        String result1 = "";
        for (int i = 1; i <= 5; i++) {
            result1 += i + " "; // creates a new String each iteration!
        }
        System.out.println("Concatenation result: \"" + result1.trim() + "\"");

        // GOOD approach: StringBuilder is mutable, no intermediate objects
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i);
            if (i < 5) sb.append(", ");
        }
        String result2 = sb.toString();
        System.out.println("StringBuilder result: \"" + result2 + "\"");

        // Demonstrate StringBuilder methods
        System.out.println("\nStringBuilder methods:");
        StringBuilder demo = new StringBuilder("Hello");
        System.out.println("Start:            \"" + demo + "\"");

        demo.append(", World");
        System.out.println("append:           \"" + demo + "\"");

        demo.insert(5, "!");
        System.out.println("insert(5, '!'):   \"" + demo + "\"");

        demo.delete(5, 6);
        System.out.println("delete(5, 6):     \"" + demo + "\"");

        demo.reverse();
        System.out.println("reverse():        \"" + demo + "\"");

        demo.reverse(); // reverse back
        System.out.println("reverse() again:  \"" + demo + "\"");

        System.out.println("length():          " + demo.length());
        System.out.println("charAt(0):        '" + demo.charAt(0) + "'");

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 10: String.format() and printf
    // -------------------------------------------------------------------------
    static void demoFormatting() {
        System.out.println("--- String.format() and printf ---");

        String name = "Alice";
        int age = 30;
        double gpa = 3.857;

        // String.format — returns a formatted String
        String formatted = String.format("Student: %-10s | Age: %3d | GPA: %.2f", name, age, gpa);
        System.out.println(formatted);

        // printf — formats and prints directly (no newline added)
        System.out.printf("%-12s %-6s %6s%n", "Name", "Age", "GPA");
        System.out.printf("%-12s %-6s %6s%n", "------------", "------", "------");

        String[][] students = {
            {"Alice",   "30", "3.86"},
            {"Bob",     "22", "3.21"},
            {"Charlie", "25", "3.94"}
        };
        for (String[] student : students) {
            System.out.printf("%-12s %-6s %6s%n", student[0], student[1], student[2]);
        }

        // Format specifiers overview
        System.out.println("\nFormat specifier examples:");
        System.out.printf("%%s  (String):          |%s|%n", "hello");
        System.out.printf("%%10s (right-aligned):  |%10s|%n", "hello");
        System.out.printf("%%-10s (left-aligned):  |%-10s|%n", "hello");
        System.out.printf("%%d  (int):             |%d|%n", 42);
        System.out.printf("%%5d (width 5):         |%5d|%n", 42);
        System.out.printf("%%05d (zero-padded):    |%05d|%n", 42);
        System.out.printf("%%f  (double):          |%f|%n", 3.14159);
        System.out.printf("%%.2f (2 decimals):     |%.2f|%n", 3.14159);
        System.out.printf("%%10.2f (width+decimal):|%10.2f|%n", 3.14159);

        System.out.println();
    }

    // -------------------------------------------------------------------------
    // SECTION 11: Conversions
    // -------------------------------------------------------------------------
    static void demoConversions() {
        System.out.println("--- Conversions Between String and Other Types ---");

        // Other types → String
        System.out.println("Converting TO String:");
        System.out.println("  String.valueOf(42):      \"" + String.valueOf(42) + "\"");
        System.out.println("  String.valueOf(3.14):    \"" + String.valueOf(3.14) + "\"");
        System.out.println("  String.valueOf(true):    \"" + String.valueOf(true) + "\"");
        System.out.println("  Integer.toString(42):    \"" + Integer.toString(42) + "\"");
        System.out.println("  \"\" + 42:                 \"" + ("" + 42) + "\""); // works but less clear

        // String → other types
        System.out.println("\nConverting FROM String:");
        System.out.println("  Integer.parseInt(\"42\"):         " + Integer.parseInt("42"));
        System.out.println("  Double.parseDouble(\"3.14\"):     " + Double.parseDouble("3.14"));
        System.out.println("  Boolean.parseBoolean(\"true\"):   " + Boolean.parseBoolean("true"));
        System.out.println("  Long.parseLong(\"1234567890\"):   " + Long.parseLong("1234567890"));

        // Watch out: NumberFormatException
        System.out.println("\nHandling invalid conversions:");
        try {
            int bad = Integer.parseInt("not a number");
        } catch (NumberFormatException e) {
            System.out.println("  NumberFormatException caught: \"not a number\" is not an int");
        }

        try {
            double bad = Double.parseDouble("$9.99"); // $ sign causes the error
        } catch (NumberFormatException e) {
            System.out.println("  NumberFormatException caught: \"$9.99\" is not a plain double");
        }

        System.out.println();
    }
}
