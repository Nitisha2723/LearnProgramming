// DataTypesDemo.java
// Demonstrates all of Java's primitive data types and String.
//
// Java is "statically typed" — every variable has a type declared at compile time.
// This means the compiler knows the type of every variable and can catch type errors
// before your program ever runs.
//
// Java has 8 primitive types (built into the language, not objects):
//   byte, short, int, long, float, double, boolean, char
// Plus the commonly used class type: String

public class DataTypesDemo {

    public static void main(String[] args) {

        // =====================================================================
        // INTEGER TYPES (whole numbers, no decimal point)
        // =====================================================================

        // byte: 8 bits. Range: -128 to 127.
        // Use when memory is very constrained or when working with binary data.
        // Rarely used in everyday programming.
        byte myByte = 100;
        System.out.println("byte value: " + myByte);

        // short: 16 bits. Range: -32,768 to 32,767.
        // Rarely used in modern Java. int is usually preferred.
        short myShort = 30000;
        System.out.println("short value: " + myShort);

        // int: 32 bits. Range: -2,147,483,648 to 2,147,483,647 (about ±2.1 billion).
        // THE DEFAULT INTEGER TYPE IN JAVA.
        // Use int for counting, indexing, arithmetic — basically all whole-number work.
        int age = 25;
        int year = 2024;
        int negativNumber = -42;
        System.out.println("int age: " + age);
        System.out.println("int year: " + year);
        System.out.println("int negative: " + negativNumber);

        // long: 64 bits. Range: about ±9.2 quintillion.
        // Use when int is too small — e.g., file sizes, population counts, timestamps.
        // IMPORTANT: long literals end with 'L' (or 'l', but uppercase is clearer).
        // Without the L, Java treats the number as an int, which may overflow.
        long worldPopulation = 8_000_000_000L;  // 8 billion — exceeds int max
        long distanceToSunMeters = 149_597_870_700L;
        System.out.println("long world population: " + worldPopulation);
        System.out.println("long distance to sun (m): " + distanceToSunMeters);
        // Note: underscores in numeric literals (e.g., 8_000_000_000L) are allowed
        // in Java 7+. They are ignored by the compiler — purely for human readability.

        // =====================================================================
        // FLOATING-POINT TYPES (numbers with decimal points)
        // =====================================================================

        // float: 32 bits. Precision: about 6-7 significant digits.
        // Rarely used in modern Java. Use double instead.
        // IMPORTANT: float literals end with 'f' (or 'F').
        float temperature = 98.6f;
        System.out.println("float temperature: " + temperature);

        // double: 64 bits. Precision: about 15-16 significant digits.
        // THE DEFAULT FLOATING-POINT TYPE IN JAVA.
        // Use double for any decimal arithmetic.
        double pi = 3.141592653589793;
        double price = 19.99;
        double verySmall = 0.000000001;
        System.out.println("double pi: " + pi);
        System.out.println("double price: " + price);
        System.out.println("double very small: " + verySmall);

        // WARNING: Floating-point numbers are not exact in computers.
        // They are stored in binary (base 2), and many decimal fractions
        // (like 0.1) cannot be represented exactly in binary.
        // This is a fundamental property of IEEE 754 floating-point arithmetic.
        double a = 0.1;
        double b = 0.2;
        System.out.println("0.1 + 0.2 = " + (a + b));
        // Output: 0.30000000000000004 (NOT exactly 0.3!)
        // For exact decimal arithmetic (e.g., money), use BigDecimal instead.

        // =====================================================================
        // BOOLEAN TYPE (true or false only)
        // =====================================================================

        // boolean: stores exactly one of two values: true or false.
        // Used for flags, conditions, and control flow.
        boolean isStudent = true;
        boolean hasLicense = false;
        boolean isAdult = (age >= 18);   // result of a comparison expression
        System.out.println("boolean isStudent: " + isStudent);
        System.out.println("boolean hasLicense: " + hasLicense);
        System.out.println("boolean isAdult (age >= 18): " + isAdult);

        // =====================================================================
        // CHAR TYPE (a single character)
        // =====================================================================

        // char: 16 bits. Stores a single Unicode character.
        // IMPORTANT: char literals use SINGLE quotes, not double quotes.
        //   'A' is a char
        //   "A" is a String (different type!)
        char firstLetter = 'A';
        char lastLetter = 'Z';
        char digit = '7';
        char space = ' ';
        System.out.println("char firstLetter: " + firstLetter);
        System.out.println("char lastLetter: " + lastLetter);

        // chars are actually stored as numbers (Unicode code points).
        // You can do arithmetic with chars:
        char nextLetter = (char)(firstLetter + 1);  // 'A' + 1 = 'B'
        System.out.println("char after A: " + nextLetter);

        int charAsInt = firstLetter;   // 'A' is Unicode code point 65
        System.out.println("'A' as int: " + charAsInt);

        // =====================================================================
        // STRING TYPE (text — not a primitive, but used constantly)
        // =====================================================================

        // String is NOT a primitive type in Java — it is a class.
        // But it is so fundamental that it has special language support.
        // String literals use DOUBLE quotes.
        String greeting = "Hello, World!";
        String empty = "";
        String multiWord = "Java is a strongly typed language";

        System.out.println("String greeting: " + greeting);
        System.out.println("String length: " + greeting.length());  // 13

        // Strings are IMMUTABLE — once created, their content cannot change.
        // When you "modify" a String, you actually create a new String object.
        String original = "Hello";
        String modified = original + " World";  // creates a NEW String, doesn't change original
        System.out.println("original: " + original);   // still "Hello"
        System.out.println("modified: " + modified);   // "Hello World"

        // =====================================================================
        // TYPE CASTING (converting between types)
        // =====================================================================

        // Widening conversion (smaller → larger type): happens AUTOMATICALLY, no cast needed.
        // No data is lost because the larger type can hold all values of the smaller type.
        int intValue = 42;
        long longValue = intValue;     // int automatically widens to long
        double doubleValue = intValue; // int automatically widens to double
        System.out.println("int to long: " + longValue);
        System.out.println("int to double: " + doubleValue);

        // Narrowing conversion (larger → smaller type): REQUIRES an explicit cast.
        // Data CAN be lost! The programmer must acknowledge this risk explicitly.
        double bigDouble = 9.99;
        int truncated = (int) bigDouble;  // (int) is the cast — forces the conversion
        // The decimal part is TRUNCATED (dropped), NOT rounded.
        System.out.println("double 9.99 cast to int: " + truncated);  // prints 9, not 10!

        long bigLong = 1_000_000_000_000L;  // 1 trillion — too big for int
        int overflowed = (int) bigLong;     // data is lost! result is unpredictable
        System.out.println("1 trillion cast to int (data lost): " + overflowed);

        // =====================================================================
        // DEFAULT VALUES
        // =====================================================================

        // Local variables (declared inside a method) have NO default values.
        // You MUST initialize them before use. The compiler enforces this.
        // Example: the following would cause a compile error:
        //   int x;
        //   System.out.println(x);   // ERROR: variable x might not have been initialized

        // Instance and static variables (class fields) DO have defaults:
        //   int, byte, short, long → 0
        //   float, double          → 0.0
        //   boolean                → false
        //   char                   → '\u0000' (null character)
        //   String and objects     → null

        System.out.println("\n--- Summary of types ---");
        System.out.println("byte  : " + myByte     + " (8-bit integer, -128 to 127)");
        System.out.println("short : " + myShort    + " (16-bit integer)");
        System.out.println("int   : " + age        + " (32-bit integer, default for whole numbers)");
        System.out.println("long  : " + worldPopulation + " (64-bit integer, for very large numbers)");
        System.out.println("float : " + temperature + " (32-bit decimal, use 'f' suffix)");
        System.out.println("double: " + pi         + " (64-bit decimal, default for decimals)");
        System.out.println("boolean: " + isStudent + " (true or false)");
        System.out.println("char  : " + firstLetter + " (single character, use single quotes)");
        System.out.println("String: " + greeting   + " (text, use double quotes)");
    }
}
