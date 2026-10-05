package solid.srp;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 *  SOLID PRINCIPLE #1 — Single Responsibility Principle (SRP)
 * ============================================================
 *
 *  DEFINITION:
 *    "A class should have only one reason to change."
 *    — Robert C. Martin (Uncle Bob)
 *
 *  What does "reason to change" mean?
 *    A "reason to change" corresponds to a distinct STAKEHOLDER or ACTOR who cares
 *    about that part of the system. If the business rules change, that's one reason.
 *    If the email format changes, that's another. If the PDF layout changes, that's
 *    another. If the database schema changes, that's another.
 *
 *    When a single class handles all of these concerns, ANY of those stakeholders
 *    can trigger a change to this class. This means:
 *      1. The class is constantly changing — hard to stabilize.
 *      2. A change for one stakeholder can accidentally break something for another.
 *      3. The class becomes enormous and hard to understand.
 *      4. Unit testing is nearly impossible (how do you test user authentication
 *         without also setting up a mail server and database?).
 *
 *  THE VIOLATION BELOW:
 *    UserService does FIVE completely unrelated things:
 *      1. Authentication       — "reason to change": security policy changes
 *      2. Email sending        — "reason to change": email provider / template changes
 *      3. PDF report generation— "reason to change": report layout / branding changes
 *      4. Display formatting   — "reason to change": UI/UX requirements change
 *      5. Database persistence — "reason to change": database schema / ORM changes
 *
 *    This class has FIVE reasons to change. Each change risks breaking the others.
 */
public class ViolationExample {

    // -----------------------------------------------------------------------
    // Simulated "database" — a simple in-memory map standing in for a real DB.
    // In a real violation, the class would also hold JDBC connection logic.
    // -----------------------------------------------------------------------
    private static final Map<String, String> USER_DB = new HashMap<>();
    private static final Map<String, String> USER_EMAIL_DB = new HashMap<>();

    static {
        // Seed some fake users (username -> hashed password)
        USER_DB.put("alice", "hashed_p@ssword_alice");
        USER_DB.put("bob",   "hashed_p@ssword_bob");
        USER_EMAIL_DB.put("alice", "alice@example.com");
        USER_EMAIL_DB.put("bob",   "bob@example.com");
    }

    // =========================================================================
    //  RESPONSIBILITY #1: Authentication
    //  "Reason to change": Security team decides to switch hashing algorithm,
    //   add multi-factor auth, or change session token logic.
    // =========================================================================

    /**
     * Authenticates a user by checking username/password.
     *
     * WHY THIS IS A PROBLEM:
     *   If the security team decides to switch from SHA-256 to bcrypt, or add
     *   OAuth support, they must edit THIS class — the same class that also
     *   controls PDF generation. A mistake in the authentication change could
     *   corrupt the PDF logic and vice versa.
     */
    public boolean authenticate(String username, String password) {
        System.out.println("[UserService] Authenticating user: " + username);

        if (!USER_DB.containsKey(username)) {
            System.out.println("[UserService] User not found: " + username);
            return false;
        }

        // Simulating a password hash check (in real code: BCrypt.checkpw(...))
        String storedHash = USER_DB.get(username);
        String inputHash  = "hashed_p@ssword_" + password; // fake hashing
        boolean isValid   = storedHash.equals(inputHash);

        System.out.println("[UserService] Auth result for " + username + ": " + isValid);
        return isValid;
    }

    /**
     * Generates a session token for an authenticated user.
     *
     * WHY THIS IS A PROBLEM:
     *   Token generation is part of authentication responsibility. Yet it lives
     *   in the same class as PDF rendering. A change to token format (e.g., moving
     *   from UUID to JWT) forces editing the same class as email templates.
     */
    public String generateSessionToken(String username) {
        // Simplified fake token — real code would use JWT or UUID
        String token = "TOKEN-" + username.toUpperCase() + "-" + System.currentTimeMillis();
        System.out.println("[UserService] Generated token for " + username + ": " + token);
        return token;
    }

    // =========================================================================
    //  RESPONSIBILITY #2: Email Sending
    //  "Reason to change": Marketing changes email templates, company switches
    //   from SendGrid to Mailchimp, HTML layout is redesigned.
    // =========================================================================

    /**
     * Sends a welcome email to a newly registered user.
     *
     * WHY THIS IS A PROBLEM:
     *   The email template, subject line, and SMTP provider are completely
     *   unrelated to user authentication or database persistence. Yet here they
     *   all live together. If marketing wants to A/B test email subject lines,
     *   they have to ask a developer to modify the same class that handles logins.
     *
     *   Also, this class now has a transitive dependency on an email library.
     *   Any class that imports UserService also indirectly depends on email logic,
     *   even if it only cares about authentication.
     */
    public void sendWelcomeEmail(String username) {
        String email = USER_EMAIL_DB.get(username);
        if (email == null) {
            System.out.println("[UserService] No email address for: " + username);
            return;
        }

        // Simulating SMTP send — real code would use JavaMail or a library like SendGrid
        System.out.println("[UserService] Connecting to SMTP server smtp.example.com:587 ...");
        System.out.println("[UserService] Sending email to: " + email);
        System.out.println("[UserService] Subject: Welcome to Our Platform, " + username + "!");
        System.out.println("[UserService] Body: <html><body>Hello " + username
                + ", your account is ready!</body></html>");
        System.out.println("[UserService] Email sent successfully.");
    }

    /**
     * Sends a password-reset email.
     *
     * WHY THIS IS A PROBLEM:
     *   More email logic in the same class. The email template for password reset
     *   is a concern owned by the communications/design team, NOT the user management
     *   team. Combining them in one class creates cross-team coupling.
     */
    public void sendPasswordResetEmail(String username, String resetToken) {
        String email = USER_EMAIL_DB.get(username);
        System.out.println("[UserService] Connecting to SMTP server ...");
        System.out.println("[UserService] Sending password reset to: " + email);
        System.out.println("[UserService] Reset link: https://example.com/reset?token=" + resetToken);
    }

    // =========================================================================
    //  RESPONSIBILITY #3: PDF Report Generation
    //  "Reason to change": Design team changes report layout, legal team requires
    //   new fields, a different PDF library is chosen (iText vs Apache PDFBox).
    // =========================================================================

    /**
     * Generates a PDF report summarizing a user's account.
     *
     * WHY THIS IS A PROBLEM:
     *   PDF generation involves layout decisions, fonts, branding, page margins,
     *   headers, and footers. This is entirely separate from knowing whether a
     *   user's password is correct. If the PDF library (say, iText) releases a
     *   breaking change in its API, we must modify this class — the same class
     *   that handles SMTP connections.
     *
     *   Testing this is also painful: to unit-test a PDF layout change, you now
     *   have to mock the database AND the email server too.
     */
    public byte[] generateUserReport(String username) {
        System.out.println("[UserService] Generating PDF report for: " + username);

        // Simulated PDF bytes — real code would use iText or Apache PDFBox
        String pdfContent = buildReportContent(username);
        System.out.println("[UserService] PDF content:\n" + pdfContent);

        // Pretend we're serializing to PDF bytes
        byte[] fakePdfBytes = pdfContent.getBytes();
        System.out.println("[UserService] PDF generated: " + fakePdfBytes.length + " bytes");
        return fakePdfBytes;
    }

    /**
     * Builds the text content of the report.
     * (Simulates report layout logic that belongs in a separate reporting class.)
     */
    private String buildReportContent(String username) {
        return "====== USER ACCOUNT REPORT ======\n"
             + "Username  : " + username + "\n"
             + "Email     : " + USER_EMAIL_DB.getOrDefault(username, "N/A") + "\n"
             + "Status    : Active\n"
             + "Created   : 2024-01-15\n"
             + "================================\n";
    }

    // =========================================================================
    //  RESPONSIBILITY #4: Display Formatting
    //  "Reason to change": Frontend team changes how data is displayed,
    //   switches from JSON to XML, adds new UI fields.
    // =========================================================================

    /**
     * Formats a user's profile data as a display-ready string.
     *
     * WHY THIS IS A PROBLEM:
     *   Display formatting is a presentation-layer concern. It should be owned
     *   by the team working on the UI or API contract. Yet here it is, buried
     *   inside the same class that does database writes and PDF generation.
     *
     *   If the frontend team switches from showing "Alice" to showing "ALICE"
     *   (uppercased), they must ask a backend developer to modify UserService —
     *   potentially touching authentication code in the process.
     */
    public String formatUserForDisplay(String username) {
        String email = USER_EMAIL_DB.getOrDefault(username, "unknown@example.com");

        // Simulated formatting logic — in real code this might build JSON or HTML
        String formatted = String.format(
            "┌─────────────────────────────┐\n" +
            "│  User Profile               │\n" +
            "├─────────────────────────────┤\n" +
            "│  Name  : %-19s│\n" +
            "│  Email : %-19s│\n" +
            "│  Role  : %-19s│\n" +
            "└─────────────────────────────┘",
            username, email, "Standard User"
        );

        System.out.println("[UserService] Formatted user:\n" + formatted);
        return formatted;
    }

    /**
     * Formats a user's data as a JSON string for an API response.
     *
     * WHY THIS IS A PROBLEM:
     *   JSON serialization is yet another presentation concern. If the API version
     *   changes (v1 -> v2) and a field must be renamed, we're back in this class
     *   again, right next to the SMTP code. Every change increases the risk of
     *   introducing bugs in unrelated areas.
     */
    public String formatUserAsJson(String username) {
        String email = USER_EMAIL_DB.getOrDefault(username, "unknown@example.com");
        return "{ \"username\": \"" + username + "\", \"email\": \"" + email + "\" }";
    }

    // =========================================================================
    //  RESPONSIBILITY #5: Database Persistence
    //  "Reason to change": DBA changes the schema, team migrates from MySQL to
    //   PostgreSQL, a new ORM is adopted, indexing strategy changes.
    // =========================================================================

    /**
     * Saves a new user to the database.
     *
     * WHY THIS IS A PROBLEM:
     *   Database access (SQL queries, connection pooling, transaction management)
     *   is a persistence-layer concern, completely separate from business logic.
     *   If the team migrates from raw JDBC to Hibernate/JPA, every persistence
     *   call must change — but those changes are trapped inside UserService,
     *   right next to the email templates.
     *
     *   Also, unit-testing authentication now requires a running database, which
     *   makes tests slow and fragile.
     */
    public void saveUser(String username, String password, String email) {
        System.out.println("[UserService] Connecting to database jdbc:mysql://localhost:3306/users ...");
        System.out.println("[UserService] Executing: INSERT INTO users (username, password, email) VALUES (?, ?, ?)");

        // Simulated DB save — real code would use PreparedStatement or JPA
        String hashedPassword = "hashed_p@ssword_" + password;
        USER_DB.put(username, hashedPassword);
        USER_EMAIL_DB.put(username, email);

        System.out.println("[UserService] User saved to DB: " + username);
    }

    /**
     * Retrieves a user from the database by username.
     *
     * WHY THIS IS A PROBLEM:
     *   Query logic, result-set mapping, and error handling for DB failures
     *   are all persistence concerns. But here they live alongside email
     *   formatting code. A database schema change (e.g., renaming the column
     *   "username" to "user_name") forces a change here — touching all the
     *   other unrelated methods in this class.
     */
    public String findUser(String username) {
        System.out.println("[UserService] Executing: SELECT * FROM users WHERE username = ?");

        if (USER_DB.containsKey(username)) {
            String email = USER_EMAIL_DB.getOrDefault(username, "unknown");
            System.out.println("[UserService] Found user: " + username + " (" + email + ")");
            return username;
        } else {
            System.out.println("[UserService] User not found in DB: " + username);
            return null;
        }
    }

    // =========================================================================
    //  MAIN — Demonstrating the problem
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== SRP VIOLATION DEMO ===\n");

        ViolationExample service = new ViolationExample();

        // This single object does EVERYTHING. Notice how awkward it is to
        // use one class for all these unrelated operations.

        // 1. Save user (persistence responsibility)
        service.saveUser("charlie", "secret123", "charlie@example.com");
        System.out.println();

        // 2. Authenticate (authentication responsibility)
        boolean isValid = service.authenticate("charlie", "secret123");
        System.out.println("Login success: " + isValid);
        System.out.println();

        // 3. Send welcome email (email responsibility)
        service.sendWelcomeEmail("charlie");
        System.out.println();

        // 4. Generate PDF (reporting responsibility)
        byte[] pdf = service.generateUserReport("charlie");
        System.out.println("PDF size: " + pdf.length + " bytes");
        System.out.println();

        // 5. Format for display (presentation responsibility)
        System.out.println(service.formatUserForDisplay("charlie"));
        System.out.println();

        // The problem is clear: UserService has become a "God Object".
        // It knows too much and does too much. Any change to any part of
        // the system could require editing this class.
        //
        // CONSEQUENCE TABLE:
        // +----------------------------+----------------------------+
        // | What changes               | Who might break            |
        // +----------------------------+----------------------------+
        // | Switch SMTP provider       | Auth logic (same file!)    |
        // | Change DB schema           | PDF layout (same file!)    |
        // | Update PDF library version | Email templates (same!)    |
        // | Redesign JSON format       | Auth token logic (same!)   |
        // +----------------------------+----------------------------+
        //
        // See CorrectExample.java to see how SRP fixes this.
    }
}
