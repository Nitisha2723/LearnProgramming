package solid.srp;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 *  SRP CORRECT EXAMPLE — Single Responsibility Principle
 * ============================================================
 *
 *  THE FIX:
 *    We split the God-class UserService into FOUR focused classes,
 *    each with exactly ONE reason to change:
 *
 *    1. UserAuthService    — only changes when authentication logic changes
 *    2. UserEmailService   — only changes when email templates/providers change
 *    3. UserReportService  — only changes when report layout/format changes
 *    4. UserRepository     — only changes when persistence/DB strategy changes
 *
 *  BENEFITS OF THIS DESIGN:
 *    - Each class is small, focused, and easy to understand.
 *    - Unit-testing is straightforward: test UserAuthService with mock data,
 *      no need to set up email servers or databases.
 *    - Different teams can own different classes without stepping on each other.
 *    - Changes are localized — editing UserReportService cannot accidentally
 *      break UserAuthService.
 *    - Classes can be reused independently: UserEmailService can send emails
 *      for password resets, order confirmations, etc. without dragging in
 *      authentication logic.
 */
public class CorrectExample {

    // =========================================================================
    //  CLASS 1: UserRepository
    //
    //  SINGLE RESPONSIBILITY: Persist and retrieve User data.
    //  ONE REASON TO CHANGE: Database schema changes, ORM migration, new indexes.
    //
    //  This class knows NOTHING about emails, PDFs, or authentication logic.
    //  If the team moves from MySQL to PostgreSQL, only this class changes.
    // =========================================================================
    static class UserRepository {

        // In production this would be a JPA EntityManager or a DataSource.
        // Here we use a simple Map to keep the example self-contained.
        private final Map<String, String[]> store = new HashMap<>(); // username -> [hashedPw, email]

        /**
         * Persists a new user record.
         *
         * REASON TO CHANGE: DBA modifies the "users" table schema.
         * This method changes — and ONLY this method/class changes.
         */
        public void save(String username, String hashedPassword, String email) {
            store.put(username, new String[]{hashedPassword, email});
            System.out.println("[UserRepository] Saved user: " + username);
        }

        /**
         * Finds a user by username. Returns null if not found.
         *
         * REASON TO CHANGE: Query optimisation, ORM change, caching layer added.
         */
        public String[] findByUsername(String username) {
            return store.get(username); // [hashedPw, email] or null
        }

        /** Checks if a username already exists. */
        public boolean exists(String username) {
            return store.containsKey(username);
        }
    }

    // =========================================================================
    //  CLASS 2: UserAuthService
    //
    //  SINGLE RESPONSIBILITY: Authenticate users and manage session tokens.
    //  ONE REASON TO CHANGE: Security policy changes (new hashing algo, MFA, JWT).
    //
    //  Notice it DEPENDS ON UserRepository (via constructor injection).
    //  It does not know how the repository is implemented — DB, file, memory, etc.
    // =========================================================================
    static class UserAuthService {

        // Dependency injected — UserAuthService does not create UserRepository itself.
        // This keeps the two responsibilities cleanly separated and makes testing easy.
        private final UserRepository userRepository;

        public UserAuthService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        /**
         * Registers a new user — hashing is an auth concern, not a persistence concern.
         *
         * The repository receives the already-hashed password. If we switch hashing
         * algorithms, only this class changes — the repository never needs to know.
         */
        public void registerUser(String username, String rawPassword, String email) {
            if (userRepository.exists(username)) {
                throw new IllegalArgumentException("Username already taken: " + username);
            }
            String hashed = hashPassword(rawPassword);
            userRepository.save(username, hashed, email);
            System.out.println("[UserAuthService] Registered user: " + username);
        }

        /**
         * Verifies username/password. Returns true if valid.
         *
         * REASON TO CHANGE: Switch from SHA-256 to bcrypt, add MFA step, etc.
         */
        public boolean authenticate(String username, String rawPassword) {
            String[] data = userRepository.findByUsername(username);
            if (data == null) {
                System.out.println("[UserAuthService] Unknown user: " + username);
                return false;
            }
            boolean match = data[0].equals(hashPassword(rawPassword));
            System.out.println("[UserAuthService] Auth for " + username + ": " + match);
            return match;
        }

        /**
         * Generates a session token for an authenticated user.
         *
         * REASON TO CHANGE: Switch from UUID to JWT tokens.
         */
        public String generateSessionToken(String username) {
            // Real code would use UUID.randomUUID() or a JWT library
            return "TOKEN-" + username.toUpperCase() + "-" + System.currentTimeMillis();
        }

        // Private helper — hashing is an internal detail of authentication
        private String hashPassword(String raw) {
            // Real code: BCrypt.hashpw(raw, BCrypt.gensalt())
            return "hashed_" + raw;
        }
    }

    // =========================================================================
    //  CLASS 3: UserEmailService
    //
    //  SINGLE RESPONSIBILITY: Compose and send emails to users.
    //  ONE REASON TO CHANGE: Email templates change, SMTP provider changes,
    //   HTML layout is redesigned, localisation is added.
    //
    //  This class does NOT know how users are stored or authenticated.
    //  It receives all the data it needs via method parameters.
    // =========================================================================
    static class UserEmailService {

        // In production this would hold a reference to a JavaMail Session,
        // SendGrid client, or AWS SES client — injected via constructor.
        private final String smtpHost;

        public UserEmailService(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        /**
         * Sends a welcome email after successful registration.
         *
         * REASON TO CHANGE: Marketing redesigns the welcome email template.
         * Only this class and method change — auth and DB are untouched.
         */
        public void sendWelcomeEmail(String username, String email) {
            System.out.println("[UserEmailService] Connecting to " + smtpHost);
            System.out.println("[UserEmailService] TO: " + email);
            System.out.println("[UserEmailService] Subject: Welcome, " + username + "!");
            System.out.println("[UserEmailService] Body: <html>Hello " + username
                    + ", your account is active!</html>");
            System.out.println("[UserEmailService] Welcome email sent.");
        }

        /**
         * Sends a password-reset link email.
         *
         * REASON TO CHANGE: Reset link format changes, email template is updated.
         */
        public void sendPasswordResetEmail(String username, String email, String resetToken) {
            System.out.println("[UserEmailService] Sending password reset to: " + email);
            System.out.println("[UserEmailService] Reset URL: https://example.com/reset?token="
                    + resetToken);
        }
    }

    // =========================================================================
    //  CLASS 4: UserReportService
    //
    //  SINGLE RESPONSIBILITY: Generate formatted reports about users.
    //  ONE REASON TO CHANGE: Report format changes (PDF layout, new fields,
    //   branding update, switch from iText to Apache PDFBox).
    //
    //  This class receives the data it needs. It has no idea about SMTP or SQL.
    // =========================================================================
    static class UserReportService {

        /**
         * Generates a textual account summary (in reality, a PDF byte array).
         *
         * REASON TO CHANGE: Report template changes, legal team adds required fields,
         * the PDF library is swapped for a different one.
         */
        public byte[] generateAccountReport(String username, String email) {
            System.out.println("[UserReportService] Building report for: " + username);

            String content = buildReportLayout(username, email);
            System.out.println("[UserReportService] Report content:\n" + content);

            // In real code: iText / Apache PDFBox would serialize this to a PDF
            return content.getBytes();
        }

        /**
         * Formats user data as a human-readable display string.
         *
         * REASON TO CHANGE: UI/UX redesign, switch from table layout to card layout.
         * This is isolated from all other concerns.
         */
        public String formatUserForDisplay(String username, String email) {
            return String.format(
                "┌─────────────────────────────┐\n" +
                "│  User Profile               │\n" +
                "├─────────────────────────────┤\n" +
                "│  Name  : %-19s│\n" +
                "│  Email : %-19s│\n" +
                "└─────────────────────────────┘",
                username, email
            );
        }

        // Private helper — report layout logic stays inside the reporting class
        private String buildReportLayout(String username, String email) {
            return "====== USER ACCOUNT REPORT ======\n"
                 + "Username : " + username + "\n"
                 + "Email    : " + email + "\n"
                 + "Status   : Active\n"
                 + "Generated: " + java.time.LocalDate.now() + "\n"
                 + "=================================\n";
        }
    }

    // =========================================================================
    //  MAIN — Wiring the classes together and demonstrating usage
    //
    //  Notice that the orchestration code (main method) composes the services.
    //  In a real application, a Dependency Injection framework (Spring, Guice)
    //  would handle this wiring automatically.
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== SRP CORRECT EXAMPLE ===\n");

        // ----- Wire up dependencies (manual DI) -----
        UserRepository    repository    = new UserRepository();
        UserAuthService   authService   = new UserAuthService(repository);
        UserEmailService  emailService  = new UserEmailService("smtp.example.com:587");
        UserReportService reportService = new UserReportService();

        // ----- Register a new user -----
        // Auth service handles password hashing + delegates storage to repository.
        System.out.println("--- Step 1: Register user ---");
        authService.registerUser("diana", "mySecret42", "diana@example.com");
        System.out.println();

        // ----- Send welcome email -----
        // Email service only knows about email. It doesn't touch the DB.
        System.out.println("--- Step 2: Send welcome email ---");
        emailService.sendWelcomeEmail("diana", "diana@example.com");
        System.out.println();

        // ----- Authenticate -----
        System.out.println("--- Step 3: Authenticate ---");
        boolean ok = authService.authenticate("diana", "mySecret42");
        if (ok) {
            String token = authService.generateSessionToken("diana");
            System.out.println("Session token: " + token);
        }
        System.out.println();

        // ----- Generate report -----
        // Report service only knows about formatting. It receives data as parameters.
        System.out.println("--- Step 4: Generate report ---");
        byte[] pdf = reportService.generateAccountReport("diana", "diana@example.com");
        System.out.println("Report size: " + pdf.length + " bytes");
        System.out.println();

        // ----- Format for display -----
        System.out.println("--- Step 5: Display profile ---");
        System.out.println(reportService.formatUserForDisplay("diana", "diana@example.com"));
        System.out.println();

        // ----- Summary of benefits -----
        System.out.println("=== BENEFITS RECAP ===");
        System.out.println("+ UserAuthService   — change auth logic here only");
        System.out.println("+ UserEmailService  — change email templates here only");
        System.out.println("+ UserReportService — change report layout here only");
        System.out.println("+ UserRepository    — change DB access here only");
        System.out.println("+ Each class can be unit-tested independently");
        System.out.println("+ Different teams can own different classes");
        System.out.println("+ No accidental coupling between concerns");
    }
}
