package patterns.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DESIGN PATTERN: Builder
 * =======================
 * Intent: Separate the construction of a complex object from its representation,
 * allowing the same construction process to create different representations.
 *
 * REAL-WORLD USE CASE: HTTP Request Construction
 * -----------------------------------------------
 * HTTP requests have many fields: URL, method, headers, query parameters, body,
 * timeout, redirect policy, etc. Most are optional. This creates a problem:
 * how do you create objects with some-but-not-all optional fields?
 *
 * THE PROBLEM: "TELESCOPING CONSTRUCTORS" ANTI-PATTERN
 * -------------------------------------------------------
 * Without Builder, you'd write multiple constructor overloads:
 *
 *   HttpRequest(String url)
 *   HttpRequest(String url, String method)
 *   HttpRequest(String url, String method, Map<String,String> headers)
 *   HttpRequest(String url, String method, Map<String,String> headers, String body)
 *   HttpRequest(String url, String method, Map<String,String> headers,
 *               String body, int timeout)
 *   HttpRequest(String url, String method, Map<String,String> headers,
 *               String body, int timeout, boolean followRedirects)
 *
 * PROBLEMS with telescoping constructors:
 *   1. With N optional params, you need up to 2^N constructors (or N! overloads)
 *   2. Hard to read: new HttpRequest("http://...", "GET", null, null, 30, true)
 *      — what does 'true' mean? Have to count parameters.
 *   3. Easy to swap parameters of same type (bug: pass timeout as body)
 *   4. Order matters and changes between overloads — mistakes are silent
 *
 * JavaBeans (setters) solves readability but breaks immutability:
 *   - Object is in invalid state during construction (between calls)
 *   - Cannot be made thread-safe or final
 *
 * THE SOLUTION: Builder Pattern
 * ------------------------------
 * The Builder separates WHAT you want from HOW it gets built:
 *   - Fluent methods: each returns 'this' for chaining
 *   - Self-documenting: .timeout(30) is clear; position doesn't matter
 *   - Validated: build() checks required fields ONCE at construction time
 *   - Immutable result: HttpRequest is final, no setters
 *
 * When to use Builder:
 *   - 4+ parameters, especially with many optional ones
 *   - When you want an immutable object
 *   - When parameter combinations have validation rules
 *   - When construction steps must happen in a specific order
 */

// =============================================================================
// FILE STRUCTURE:
//   1. HttpRequest (immutable value object with Builder inner class)
//   2. HttpRequestBuilder (public demo class with main())
// =============================================================================

/**
 * Immutable HTTP Request object.
 *
 * IMMUTABILITY DESIGN:
 *   - All fields are 'final' — set once in constructor, never changed
 *   - No setters — no way to mutate after construction
 *   - Collections are wrapped in unmodifiableMap — defensive copy
 *   - Thread-safe: can be safely shared between threads without synchronization
 *
 * The only way to create an HttpRequest is through its inner Builder class.
 * This enforces the validation rules and ensures the object is always valid.
 */
class HttpRequest {

    // ------------------------------------------------------------------
    // REQUIRED FIELDS: build() will throw if these are missing
    // ------------------------------------------------------------------
    private final String url;           // Must be non-null, non-empty
    private final String method;        // GET, POST, PUT, DELETE, PATCH, etc.

    // ------------------------------------------------------------------
    // OPTIONAL FIELDS: have sensible defaults
    // ------------------------------------------------------------------
    private final Map<String, String> headers;     // HTTP headers (Content-Type, Auth, etc.)
    private final Map<String, String> queryParams; // URL query parameters (?key=value&...)
    private final String body;                     // Request body (for POST, PUT, PATCH)
    private final int timeoutMs;                   // Connection timeout in milliseconds
    private final boolean followRedirects;         // Whether to follow 301/302 redirects

    /**
     * Private constructor — only Builder can call this.
     * Having a private constructor forces all creation through the Builder,
     * which is where validation happens.
     */
    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        // Defensive copy: wrap in unmodifiable view so nobody can mutate our maps
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(builder.queryParams));
        this.body = builder.body;
        this.timeoutMs = builder.timeoutMs;
        this.followRedirects = builder.followRedirects;
    }

    // ------------------------------------------------------------------
    // Accessors — no setters (immutable)
    // ------------------------------------------------------------------
    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getBody() { return body; }
    public int getTimeoutMs() { return timeoutMs; }
    public boolean isFollowRedirects() { return followRedirects; }

    /**
     * Builds the full URL including query parameters.
     * Shows how a truly immutable object can compute derived values on demand.
     */
    public String buildFullUrl() {
        if (queryParams.isEmpty()) return url;

        StringBuilder sb = new StringBuilder(url).append("?");
        queryParams.forEach((key, value) ->
                sb.append(key).append("=").append(value).append("&"));
        // Remove trailing '&'
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpRequest{\n");
        sb.append("  method:          ").append(method).append("\n");
        sb.append("  url:             ").append(buildFullUrl()).append("\n");
        if (!headers.isEmpty()) {
            sb.append("  headers:         ").append(headers).append("\n");
        }
        if (body != null) {
            String truncated = body.length() > 50 ? body.substring(0, 50) + "..." : body;
            sb.append("  body:            ").append(truncated).append("\n");
        }
        sb.append("  timeoutMs:       ").append(timeoutMs).append("\n");
        sb.append("  followRedirects: ").append(followRedirects).append("\n");
        sb.append("}");
        return sb.toString();
    }

    // ==========================================================================
    // INNER BUILDER CLASS
    //
    // The Builder is a separate mutable object used only during construction.
    // It holds the same fields as HttpRequest but lets them be set individually.
    // Once build() is called, an immutable HttpRequest is created and the
    // Builder is discarded.
    //
    // WHY INNER CLASS?
    //   - Keeps Builder tightly coupled to HttpRequest (its sole purpose)
    //   - Builder can access HttpRequest's private constructor
    //   - Usage: new HttpRequest.Builder().url("...").build()
    // ==========================================================================
    /**
     * Fluent Builder for HttpRequest.
     *
     * FLUENT INTERFACE: Each setter returns 'this' (the Builder), allowing
     * method chaining: builder.url("...").method("GET").timeout(5000).build()
     *
     * This reads almost like natural language and is self-documenting.
     * Compare to: new HttpRequest("url", "GET", null, null, 5000, true)
     */
    public static class Builder {
        // Required — must be provided before build()
        private String url;
        private String method;

        // Optional — have defaults
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        private String body = null;
        private int timeoutMs = 30_000;         // 30 second default timeout
        private boolean followRedirects = true;  // Most HTTP clients follow redirects by default

        /**
         * Start building: constructor takes no required params.
         * Required fields are set via fluent methods and validated at build() time.
         *
         * ALTERNATIVE DESIGN: Some builders take required fields in the constructor:
         *   new HttpRequest.Builder(url, method)  — enforces them at compile time
         *   new HttpRequest.Builder()              — enforces them at runtime via build()
         *
         * Runtime validation is more flexible (e.g., URL built dynamically),
         * compile-time is safer (impossible to forget).
         */
        public Builder() {}

        // ------------------------------------------------------------------
        // REQUIRED FIELD SETTERS
        // ------------------------------------------------------------------

        /**
         * Sets the request URL.
         *
         * @param url The full URL (e.g., "https://api.example.com/users")
         * @return this Builder for chaining
         */
        public Builder url(String url) {
            this.url = url;
            return this; // Return 'this' for fluent chaining
        }

        /**
         * Sets the HTTP method.
         *
         * @param method "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"
         * @return this Builder for chaining
         */
        public Builder method(String method) {
            this.method = method == null ? null : method.toUpperCase();
            return this;
        }

        // ------------------------------------------------------------------
        // OPTIONAL FIELD SETTERS
        // ------------------------------------------------------------------

        /**
         * Adds a single HTTP header. Can be called multiple times.
         * Calling with the same name overwrites the previous value.
         *
         * @param name  Header name (e.g., "Content-Type", "Authorization")
         * @param value Header value (e.g., "application/json", "Bearer token123")
         * @return this Builder for chaining
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * Adds a query parameter to the URL. Can be called multiple times.
         * Results in: url?key1=value1&key2=value2
         *
         * @param name  Parameter name
         * @param value Parameter value (will NOT be URL-encoded in this demo)
         * @return this Builder for chaining
         */
        public Builder queryParam(String name, String value) {
            this.queryParams.put(name, value);
            return this;
        }

        /**
         * Sets the request body (for POST, PUT, PATCH requests).
         * GET requests with a body are technically allowed but discouraged.
         *
         * @param body Request body as a String (JSON, XML, form data, etc.)
         * @return this Builder for chaining
         */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * Sets the connection timeout in milliseconds.
         *
         * @param timeoutMs Timeout in milliseconds (0 = infinite, use with caution)
         * @return this Builder for chaining
         */
        public Builder timeout(int timeoutMs) {
            if (timeoutMs < 0) throw new IllegalArgumentException("Timeout cannot be negative");
            this.timeoutMs = timeoutMs;
            return this;
        }

        /**
         * Controls whether 301/302 HTTP redirects are followed automatically.
         *
         * @param followRedirects true to follow redirects (default), false to stop
         * @return this Builder for chaining
         */
        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        // ------------------------------------------------------------------
        // CONVENIENCE METHODS (build on top of primitives)
        // These demonstrate how a Builder can provide higher-level abstractions
        // ------------------------------------------------------------------

        /** Shortcut for method("GET") */
        public Builder GET() { return method("GET"); }

        /** Shortcut for method("POST") */
        public Builder POST() { return method("POST"); }

        /** Shortcut for method("PUT") */
        public Builder PUT() { return method("PUT"); }

        /** Shortcut for method("DELETE") */
        public Builder DELETE() { return method("DELETE"); }

        /** Shortcut for adding JSON Content-Type and Accept headers */
        public Builder json() {
            return header("Content-Type", "application/json")
                   .header("Accept", "application/json");
        }

        /** Shortcut for adding Bearer token authorization */
        public Builder bearerAuth(String token) {
            return header("Authorization", "Bearer " + token);
        }

        // ------------------------------------------------------------------
        // BUILD METHOD: validates and constructs the immutable HttpRequest
        // ------------------------------------------------------------------

        /**
         * Validates all fields and constructs an immutable HttpRequest.
         *
         * This is the "commit" point. Before this call, we're just collecting
         * configuration. After this call, we have a validated, immutable object.
         *
         * @return An immutable, validated HttpRequest
         * @throws IllegalStateException if required fields are missing or invalid
         */
        public HttpRequest build() {
            // Validate required fields
            if (url == null || url.isBlank()) {
                throw new IllegalStateException("URL is required. Call .url(\"https://...\") before build()");
            }
            if (method == null || method.isBlank()) {
                throw new IllegalStateException("HTTP method is required. Call .method(\"GET\") or .GET() before build()");
            }

            // Validate URL format (basic check)
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new IllegalStateException(
                    "URL must start with http:// or https://. Got: '" + url + "'");
            }

            // Validate method is a known HTTP method
            String[] validMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
            boolean validMethod = false;
            for (String m : validMethods) {
                if (m.equals(method)) { validMethod = true; break; }
            }
            if (!validMethod) {
                throw new IllegalStateException("Unknown HTTP method: '" + method + "'");
            }

            // Business rule: body should not be set for GET/HEAD/DELETE
            if (body != null && (method.equals("GET") || method.equals("HEAD"))) {
                System.out.println("WARNING: " + method + " requests with a body are unusual.");
            }

            // All validation passed — create the immutable HttpRequest
            return new HttpRequest(this);
        }
    }
}

// =============================================================================
// DEMO RUNNER
// =============================================================================
/**
 * Demonstrates the Builder pattern for constructing HTTP requests.
 *
 * Note: This demo creates HttpRequest objects but does NOT actually send HTTP
 * requests (no network calls). It demonstrates the construction pattern.
 */
public class HttpRequestBuilder {

    public static void main(String[] args) {
        System.out.println("=".repeat(65));
        System.out.println("DESIGN PATTERN: Builder");
        System.out.println("USE CASE: HTTP Request Construction");
        System.out.println("=".repeat(65) + "\n");

        // -------------------------------------------------------
        // DEMO 1: Simple GET request — minimal required fields
        // -------------------------------------------------------
        System.out.println("--- DEMO 1: Simple GET Request ---");
        HttpRequest simpleGet = new HttpRequest.Builder()
                .url("https://api.example.com/users")
                .GET()
                .build();

        System.out.println(simpleGet);

        // -------------------------------------------------------
        // DEMO 2: Complex GET with headers and query params
        // -------------------------------------------------------
        System.out.println("--- DEMO 2: Authenticated GET with Query Parameters ---");
        HttpRequest searchRequest = new HttpRequest.Builder()
                .url("https://api.example.com/users/search")
                .GET()
                .json()                          // sets Content-Type and Accept headers
                .bearerAuth("my-jwt-token-xyz")  // sets Authorization header
                .queryParam("name", "Alice")
                .queryParam("active", "true")
                .queryParam("limit", "25")
                .timeout(10_000)                 // 10 second timeout
                .followRedirects(false)
                .build();

        System.out.println(searchRequest);
        System.out.println("Full URL: " + searchRequest.buildFullUrl() + "\n");

        // -------------------------------------------------------
        // DEMO 3: POST request with JSON body
        // -------------------------------------------------------
        System.out.println("--- DEMO 3: POST Request with JSON Body ---");
        String jsonBody = "{\"username\":\"alice\",\"email\":\"alice@example.com\",\"role\":\"admin\"}";

        HttpRequest createUserRequest = new HttpRequest.Builder()
                .url("https://api.example.com/users")
                .POST()
                .json()
                .bearerAuth("admin-token-abc")
                .body(jsonBody)
                .timeout(15_000)
                .build();

        System.out.println(createUserRequest);

        // -------------------------------------------------------
        // DEMO 4: DELETE request
        // -------------------------------------------------------
        System.out.println("--- DEMO 4: DELETE Request ---");
        HttpRequest deleteRequest = new HttpRequest.Builder()
                .url("https://api.example.com/users/42")
                .DELETE()
                .header("Authorization", "Bearer admin-token")
                .header("X-Request-ID", "req-" + System.currentTimeMillis())
                .timeout(5_000)
                .build();

        System.out.println(deleteRequest);

        // -------------------------------------------------------
        // DEMO 5: Validation — missing required field
        // -------------------------------------------------------
        System.out.println("--- DEMO 5: Validation — Missing URL ---");
        try {
            HttpRequest invalid = new HttpRequest.Builder()
                    .GET()
                    // .url("...") intentionally omitted
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        // -------------------------------------------------------
        // DEMO 6: Validation — bad URL
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 6: Validation — Bad URL Format ---");
        try {
            HttpRequest invalid = new HttpRequest.Builder()
                    .url("ftp://invalid-protocol.com")  // Not http or https
                    .GET()
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        // -------------------------------------------------------
        // DEMO 7: Reusing a Builder (building multiple similar requests)
        // -------------------------------------------------------
        System.out.println("\n--- DEMO 7: Builder Reuse Pattern ---");
        System.out.println("Showing how to create a 'base' builder and specialize it:");

        // In real code you might create a helper method that returns a pre-configured Builder
        HttpRequest.Builder baseBuilder = new HttpRequest.Builder()
                .url("https://api.example.com/products")
                .GET()
                .bearerAuth("service-token")
                .json()
                .timeout(8_000);

        // Each call to build() creates a new, separate HttpRequest
        HttpRequest req1 = new HttpRequest.Builder()
                .url("https://api.example.com/products")
                .GET().json().bearerAuth("service-token").timeout(8_000)
                .queryParam("category", "electronics")
                .build();

        HttpRequest req2 = new HttpRequest.Builder()
                .url("https://api.example.com/products")
                .GET().json().bearerAuth("service-token").timeout(8_000)
                .queryParam("category", "clothing")
                .queryParam("size", "M")
                .build();

        System.out.println("Request 1: " + req1.buildFullUrl());
        System.out.println("Request 2: " + req2.buildFullUrl());

        System.out.println("\n--- Builder Pattern Summary ---");
        System.out.println("- All HttpRequest fields are final (immutable)");
        System.out.println("- Named fluent methods replace confusing positional params");
        System.out.println("- Validation is centralized in build()");
        System.out.println("- Cannot create invalid HttpRequest (constructor is private)");
        System.out.println("\nDone!");
    }
}
