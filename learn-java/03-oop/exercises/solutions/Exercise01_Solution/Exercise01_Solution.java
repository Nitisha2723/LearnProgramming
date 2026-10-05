import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Exercise 01 Solution: Library System
 */
public class Exercise01_Solution {

    public static void main(String[] args) {
        System.out.println("Exercise 01 Solution: Library System");
        System.out.println("=".repeat(50));

        Book book1 = new Book("Clean Code", "Robert C. Martin", "9780132350884");
        Book book2 = new Book("Effective Java", "Joshua Bloch", "9780134685991");
        Book book3 = new Book("The Pragmatic Programmer", "Andrew Hunt", "9780201616224");
        Book book4 = new Book("Refactoring", "Martin Fowler", "9780134757599");
        Book book5 = new Book("Clean Architecture", "Robert C. Martin", "9780134494166");

        System.out.println("\n--- Book Tests ---");
        System.out.println(book1.getSummary());
        book1.checkOut();
        System.out.println(book1.getSummary());
        book1.checkOut();  // Already checked out
        book1.returnBook();
        System.out.println(book1.getSummary());

        System.out.println("\n--- Library Tests ---");
        Library library = new Library("City Public Library");
        library.addBook(book1);
        library.addBook(book2);
        library.addBook(book3);
        library.addBook(book4);
        library.addBook(book5);

        library.printCatalog();

        System.out.println("\nChecking out 'Clean Code':");
        library.checkOut("9780132350884", "Alice");
        library.checkOut("9780132350884", "Bob");  // Already out
        library.checkOut("9999999999999", "Alice"); // Not found

        System.out.println("\nBooks by Robert C. Martin:");
        library.findByAuthor("Robert C. Martin").forEach(b -> System.out.println("  " + b));

        System.out.println("\nBooks with 'Java' in title:");
        library.findByTitle("Java").forEach(b -> System.out.println("  " + b));

        System.out.println("\nAvailable books:");
        library.getAvailableBooks().forEach(b -> System.out.println("  " + b));

        System.out.println("\nReturning 'Clean Code':");
        library.returnBook("9780132350884");
        library.returnBook("9780132350884");  // Already available
    }

    // ====================================================================
    // Book class
    // ====================================================================

    static class Book {
        private final String isbn;  // Unique, immutable identifier
        private String title;
        private String author;
        private boolean available;

        public Book(String title, String author, String isbn) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }
            if (author == null || author.trim().isEmpty()) {
                throw new IllegalArgumentException("Author cannot be empty");
            }
            if (isbn == null || isbn.length() != 13) {
                throw new IllegalArgumentException("ISBN must be 13 characters");
            }
            this.title = title.trim();
            this.author = author.trim();
            this.isbn = isbn;
            this.available = true;  // New books start available
        }

        public void checkOut() {
            if (!available) {
                System.out.println("\"" + title + "\" is already checked out.");
                return;
            }
            available = false;
            System.out.println("\"" + title + "\" has been checked out.");
        }

        public void returnBook() {
            if (available) {
                System.out.println("\"" + title + "\" was not checked out.");
                return;
            }
            available = true;
            System.out.println("\"" + title + "\" has been returned. Thank you!");
        }

        public String getSummary() {
            return String.format("\"%s\" by %s [%s] - %s",
                                title, author, isbn,
                                available ? "Available" : "Checked Out");
        }

        public String getIsbn() { return isbn; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public boolean isAvailable() { return available; }

        @Override
        public String toString() {
            return getSummary();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Book)) return false;
            Book other = (Book) obj;
            return Objects.equals(isbn, other.isbn);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isbn);
        }
    }

    // ====================================================================
    // Library class
    // ====================================================================

    static class Library {
        private final String name;
        private final List<Book> books;

        public Library(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Library name cannot be empty");
            }
            this.name = name;
            this.books = new ArrayList<>();
        }

        public void addBook(Book book) {
            if (book == null) throw new IllegalArgumentException("Book cannot be null");
            if (findBookByIsbn(book.getIsbn()) != null) {
                System.out.println("Book with ISBN " + book.getIsbn() + " already in library.");
                return;
            }
            books.add(book);
            System.out.println("Added: \"" + book.getTitle() + "\"");
        }

        public boolean removeBook(String isbn) {
            Book book = findBookByIsbn(isbn);
            if (book == null) {
                System.out.println("Book with ISBN " + isbn + " not found.");
                return false;
            }
            books.remove(book);
            System.out.println("Removed: \"" + book.getTitle() + "\"");
            return true;
        }

        public void checkOut(String isbn, String memberName) {
            Book book = findBookByIsbn(isbn);
            if (book == null) {
                System.out.println("Book with ISBN " + isbn + " not found in library.");
                return;
            }
            System.out.print(memberName + " is checking out: ");
            book.checkOut();
        }

        public void returnBook(String isbn) {
            Book book = findBookByIsbn(isbn);
            if (book == null) {
                System.out.println("Book with ISBN " + isbn + " not found in library.");
                return;
            }
            book.returnBook();
        }

        public List<Book> findByAuthor(String author) {
            List<Book> result = new ArrayList<>();
            for (Book book : books) {
                if (book.getAuthor().equalsIgnoreCase(author)) {
                    result.add(book);
                }
            }
            return result;  // New list — doesn't expose internals
        }

        public List<Book> findByTitle(String titleFragment) {
            List<Book> result = new ArrayList<>();
            for (Book book : books) {
                if (book.getTitle().toLowerCase().contains(titleFragment.toLowerCase())) {
                    result.add(book);
                }
            }
            return result;
        }

        public List<Book> getAvailableBooks() {
            List<Book> result = new ArrayList<>();
            for (Book book : books) {
                if (book.isAvailable()) {
                    result.add(book);
                }
            }
            return result;
        }

        public void printCatalog() {
            System.out.println("\n" + name + " Catalog (" + books.size() + " books):");
            System.out.println("-".repeat(70));
            for (int i = 0; i < books.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, books.get(i).getSummary());
            }
            System.out.println("-".repeat(70));
        }

        // Private helper — finds a book by ISBN
        private Book findBookByIsbn(String isbn) {
            for (Book book : books) {
                if (book.getIsbn().equals(isbn)) {
                    return book;
                }
            }
            return null;
        }

        public String getName() { return name; }
        public int getBookCount() { return books.size(); }
    }
}
