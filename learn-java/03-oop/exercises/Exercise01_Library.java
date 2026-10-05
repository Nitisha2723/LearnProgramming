import java.util.ArrayList;
import java.util.List;

/**
 * Exercise 01: Library System
 * ============================================================
 *
 * BUILD A LIBRARY MANAGEMENT SYSTEM using proper OOP principles.
 *
 * LEARNING GOALS:
 * - Design classes from requirements (nouns → classes, verbs → methods)
 * - Apply encapsulation: private fields, controlled access
 * - Distinguish between business methods and raw getters/setters
 * - Handle collections of objects
 *
 * ============================================================
 * PART A: Implement the Book class
 * ============================================================
 *
 * A Book has:
 *   - title (String): cannot be null or empty
 *   - author (String): cannot be null or empty
 *   - isbn (String): 13-digit identifier, unique, cannot change
 *   - available (boolean): starts as true (not checked out)
 *
 * A Book can:
 *   - checkOut(): marks as not available; prints message
 *   - returnBook(): marks as available; prints message
 *   - getSummary(): returns "Title by Author [ISBN] - Available/Checked Out"
 *
 * DESIGN GUIDANCE:
 *   - isbn should be final (books don't change their ISBN)
 *   - available should be private with no public setter
 *     (only checkOut() and returnBook() should change it)
 *   - Override toString() to show meaningful output
 *
 * ============================================================
 * PART B: Implement the Library class
 * ============================================================
 *
 * A Library has:
 *   - name (String): the library's name
 *   - books (List<Book>): the collection of books
 *
 * A Library can:
 *   - addBook(Book book): adds a book to the collection
 *   - removeBook(String isbn): removes a book by ISBN
 *   - checkOut(String isbn, String memberName): checks out a book
 *   - returnBook(String isbn): returns a checked-out book
 *   - findByAuthor(String author): returns all books by that author
 *   - findByTitle(String title): returns books whose title contains the string
 *   - getAvailableBooks(): returns only available books
 *   - printCatalog(): prints all books with their status
 *
 * DESIGN GUIDANCE:
 *   - books list should be private
 *   - findByAuthor should return a new list (don't expose internals)
 *   - checkOut should handle: book not found, book already checked out
 *   - returnBook should handle: book not found, book already available
 *
 * ============================================================
 * STRETCH GOALS (if you finish early):
 * ============================================================
 *
 * 1. Add a Member class with memberId, name, and a list of currently borrowed books
 * 2. Enforce the rule that a member can only borrow 3 books at a time
 * 3. Add a dueDate to checkouts and a fine calculation for overdue books
 * 4. Add a WaitList for books that are checked out
 */
public class Exercise01_Library {

    public static void main(String[] args) {
        System.out.println("Exercise 01: Library System");
        System.out.println("=".repeat(50));

        // ====================================================================
        // TEST YOUR IMPLEMENTATION BY UNCOMMENTING THESE TESTS
        // ====================================================================

        // --- Test Book Creation ---
        // Book book1 = new Book("Clean Code", "Robert C. Martin", "9780132350884");
        // Book book2 = new Book("Effective Java", "Joshua Bloch", "9780134685991");
        // Book book3 = new Book("The Pragmatic Programmer", "Andrew Hunt", "9780201616224");
        // Book book4 = new Book("Refactoring", "Martin Fowler", "9780134757599");

        // System.out.println(book1.getSummary());
        // System.out.println(book2.getSummary());

        // --- Test Check Out and Return ---
        // book1.checkOut();
        // System.out.println(book1.getSummary()); // Should show "Checked Out"
        // book1.checkOut();  // Should print "already checked out"
        // book1.returnBook();
        // System.out.println(book1.getSummary()); // Should show "Available"

        // --- Test Library ---
        // Library library = new Library("City Public Library");
        // library.addBook(book1);
        // library.addBook(book2);
        // library.addBook(book3);
        // library.addBook(book4);

        // library.printCatalog();

        // --- Test Checkout ---
        // library.checkOut("9780132350884", "Alice");
        // library.checkOut("9780132350884", "Bob");  // Already checked out
        // library.checkOut("9999999999999", "Alice"); // Not found

        // --- Test Find Methods ---
        // System.out.println("\nAll books:");
        // library.printCatalog();
        // System.out.println("\nBooks by 'Robert C. Martin':");
        // library.findByAuthor("Robert C. Martin").forEach(b -> System.out.println("  " + b));
        // System.out.println("\nBooks with 'Java' in title:");
        // library.findByTitle("Java").forEach(b -> System.out.println("  " + b));
        // System.out.println("\nAvailable books:");
        // library.getAvailableBooks().forEach(b -> System.out.println("  " + b));

        System.out.println("\nImplement the Book and Library classes above main(), then uncomment the tests.");
    }

    // ====================================================================
    // IMPLEMENT YOUR CLASSES BELOW THIS LINE
    // ====================================================================

    // TODO: Implement Book class here
    // Remember:
    // - Fields should be private
    // - isbn should be final
    // - Only checkOut() and returnBook() should change availability
    // - Validate title, author, isbn in constructor

    // TODO: Implement Library class here
    // Remember:
    // - books list should be private
    // - findByAuthor returns a new list, not the internal one
    // - Proper error messages for not-found and invalid-state cases
}
