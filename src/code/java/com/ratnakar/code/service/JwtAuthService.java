package com.ratnakar.code.service;

/*
 * ═══════════════════════════════════════════════════════════════════════════
 * IMPORTS SECTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Imports come from THREE sources:
 *
 * 1) JJWT (Java JWT Library) — from dependency in pom.xml:
 *    <dependency>
 *        <groupId>io.jsonwebtoken</groupId>
 *        <artifactId>jjwt-api</artifactId>
 *        <version>0.12.x</version>
 *    </dependency>
 *    <dependency>
 *        <groupId>io.jsonwebtoken</groupId>
 *        <artifactId>jjwt-impl</artifactId>
 *        <version>0.12.x</version>
 *    </dependency>
 *    <dependency>
 *        <groupId>io.jsonwebtoken</groupId>
 *        <artifactId>jjwt-jackson</artifactId>
 *        <version>0.12.x</version>
 *    </dependency>
 *
 * 2) Spring Security — from dependency:
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-security</artifactId>
 *    </dependency>
 *
 * 3) Java Standard Library (javax.crypto, java.security, java.util):
 *    Built into the JDK — no extra dependency needed.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */

import io.jsonwebtoken.Claims;
/*
 * Claims — Interface from: jjwt-api (io.jsonwebtoken)
 * ─────────────────────────────────────────────────────────────────────────────
 * Claims represents the PAYLOAD section of a JWT token.
 *
 * What is the JWT structure?
 *   A JWT has 3 parts separated by dots:
 *     [Header].[Payload].[Signature]
 *     eyJhbG...  .  eyJzdWIi...  .  SflKxwRJS...
 *
 * The Payload (Claims) contains key-value pairs called "claims":
 *   {
 *     "sub": "john@example.com",   ← subject (username)
 *     "iat": 1700000000,           ← issued at (when the token was created)
 *     "exp": 1700086400            ← expiration (when the token expires)
 *   }
 *
 * Claims interface provides getter methods to read these values:
 *   claims.getSubject()     → "john@example.com"
 *   claims.getIssuedAt()    → Date object
 *   claims.getExpiration()  → Date object
 *   claims.get("key")       → any custom claim value
 */

import io.jsonwebtoken.Jwts;
/*
 * Jwts — Class from: jjwt-api (io.jsonwebtoken)
 * ─────────────────────────────────────────────────────────────────────────────
 * Jwts is the MAIN ENTRY POINT of the JJWT library.
 * It acts as a factory/utility class that provides:
 *
 *   Jwts.builder()   → Creates a JwtBuilder to BUILD (generate) a new JWT token.
 *   Jwts.parser()    → Creates a JwtParserBuilder to PARSE (read/verify) a JWT token.
 *
 * Think of it like:
 *   Jwts.builder() = "I want to CREATE a new token"
 *   Jwts.parser()  = "I want to READ and VERIFY an existing token"
 *
 * This is the JJWT 0.12.x modern API.
 * Older versions used Jwts.parserBuilder() (now deprecated).
 */

import io.jsonwebtoken.security.Keys;
/*
 * Keys — Utility Class from: jjwt-api (io.jsonwebtoken.security)
 * ─────────────────────────────────────────────────────────────────────────────
 * Keys is a helper class from JJWT that provides safe, convenient methods
 * for creating cryptographic keys used for signing and verifying JWT tokens.
 *
 * Key method used here:
 *   Keys.hmacShaKeyFor(byte[] keyBytes)
 *     → Creates a SecretKey object from raw key bytes.
 *     → The key is used for HMAC-SHA signing algorithms (HS256, HS384, HS512).
 *     → Automatically picks the correct HMAC algorithm based on key size.
 *
 * Why use Keys.hmacShaKeyFor() instead of creating a key manually?
 *   It validates that the key length meets the minimum requirement for
 *   the chosen algorithm. For HS256, the key must be at least 256 bits (32 bytes).
 */

import io.jsonwebtoken.io.Decoders;
/*
 * Decoders — Utility Class from: jjwt-api (io.jsonwebtoken.io)
 * ─────────────────────────────────────────────────────────────────────────────
 * Decoders provides convenient decoders for converting encoded strings
 * back into raw byte arrays.
 *
 * Used here:
 *   Decoders.BASE64.decode(secretKey)
 *     → Decodes a Base64-encoded string back into a byte array.
 *
 * Why decode from Base64?
 *   The secretKey is stored as a Base64-encoded String (for easy storage/config).
 *   But cryptographic operations (like HMAC signing) need raw bytes.
 *   So we decode the Base64 string → get byte[] → create SecretKey from bytes.
 *
 * Flow:
 *   Raw bytes → Base64.encode() → Store as String (secretKey field)
 *   String (secretKey) → Decoders.BASE64.decode() → byte[] → Keys.hmacShaKeyFor() → SecretKey
 */

import org.springframework.security.core.userdetails.UserDetails;
/*
 * UserDetails — Interface from: spring-security-core
 * ─────────────────────────────────────────────────────────────────────────────
 * Spring Security's standard interface that represents an authenticated user.
 * Contains:
 *   - getUsername()    → the username
 *   - getPassword()    → the encoded password
 *   - getAuthorities() → the roles/permissions
 *   - isEnabled(), isAccountNonExpired(), etc. → account status flags
 *
 * Used here in validateToken() to compare the token's username with the
 * username in the UserDetails loaded from the database.
 */

import org.springframework.stereotype.Service;
/*
 * @Service — Annotation from: spring-context (spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * @Service is a specialization of @Component.
 * It marks this class as a "Service Layer" component in Spring's
 * layered architecture:
 *   Controller Layer  → handles HTTP requests
 *   Service Layer     → contains business logic  ← @Service goes here
 *   Repository Layer  → handles database access
 *
 * What @Service does:
 *   - Registers this class as a Spring Bean in the IoC container.
 *   - Allows it to be @Autowired into other classes (like JwtAuthFilter).
 *   - Has the same technical effect as @Component, but the name
 *     communicates the intent: "this class contains business logic."
 */

import javax.crypto.KeyGenerator;
/*
 * KeyGenerator — Class from: javax.crypto (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * KeyGenerator is a JDK cryptography class used to generate secret (symmetric)
 * cryptographic keys for algorithms like HmacSHA256, AES, DES, etc.
 *
 * Key methods:
 *   KeyGenerator.getInstance("HmacSHA256")  → get a KeyGenerator for HMAC-SHA256
 *   keyGen.generateKey()                    → generate a new random SecretKey
 *
 * "HmacSHA256":
 *   HMAC = Hash-based Message Authentication Code
 *   SHA256 = Secure Hash Algorithm 256-bit
 *   Together: a cryptographic algorithm that creates a fixed-size (256-bit)
 *   hash/signature using a secret key. Used to sign JWT tokens.
 */

import javax.crypto.SecretKey;
/*
 * SecretKey — Interface from: javax.crypto (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * SecretKey is a marker interface that represents a symmetric cryptographic key
 * (same key is used for both signing and verifying).
 *
 * It extends the java.security.Key interface.
 *
 * In JWT context:
 *   - The SecretKey is used to SIGN the JWT when creating it.
 *   - The SAME SecretKey is used to VERIFY the JWT signature when parsing it.
 *   - If the signature doesn't match (wrong key or tampered token),
 *     JJWT throws a SignatureException.
 *
 * Why SecretKey instead of Key?
 *   JJWT 0.12.x updated its API to use SecretKey (more specific type)
 *   instead of the broader Key interface for HMAC-based JWT operations.
 */

import java.security.Key;
/*
 * Key — Interface from: java.security (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * Key is the ROOT interface for all cryptographic keys in Java.
 * Both SecretKey (symmetric) and PublicKey/PrivateKey (asymmetric) extend it.
 *
 * Imported here because getKey() returns SecretKey (which is a Key),
 * but the import may be present for compatibility or reference purposes.
 */

import java.security.NoSuchAlgorithmException;
/*
 * NoSuchAlgorithmException — Checked Exception from: java.security (JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * Thrown by KeyGenerator.getInstance("HmacSHA256") if the requested
 * cryptographic algorithm is not available in the current JVM environment.
 *
 * In practice, "HmacSHA256" is always available in standard JVMs, so this
 * exception is caught and wrapped in a RuntimeException to avoid propagating
 * a checked exception through the call stack.
 */

import java.util.Base64;
/*
 * Base64 — Utility Class from: java.util (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * Base64 is a JDK utility class for encoding and decoding data using the
 * Base64 encoding scheme.
 *
 * What is Base64 encoding?
 *   A way to convert binary data (like raw cryptographic key bytes) into a
 *   printable ASCII string that can be safely stored in config files,
 *   passed in HTTP headers, or logged.
 *
 * Key methods:
 *   Base64.getEncoder().encodeToString(byte[])  → converts bytes → Base64 String
 *   Base64.getDecoder().decode(String)          → converts Base64 String → bytes
 *
 * Used here to:
 *   ENCODE: Convert the generated SecretKey bytes → Base64 String for storage.
 *   DECODE: Convert the stored Base64 String → bytes when creating SecretKey.
 */

import java.util.Date;
/*
 * Date — Class from: java.util (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * The classic Java class for representing a specific point in time
 * (date + time combined).
 *
 * Used here for:
 *   new Date(System.currentTimeMillis())                     → current date/time (token issue time)
 *   new Date(System.currentTimeMillis() + 1000 * 60 * 3)    → 3 minutes from now (token expiry)
 *   extractExpiration(token).before(new Date())              → check if expiry is before now (expired?)
 *
 * System.currentTimeMillis() returns the current time in MILLISECONDS since
 * January 1, 1970 (Unix epoch). So:
 *   1000 ms = 1 second
 *   1000 * 60 = 1 minute
 *   1000 * 60 * 3 = 3 minutes
 */

import java.util.HashMap;
/*
 * HashMap — Class from: java.util (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * HashMap is a key-value data structure (implements the Map interface).
 * It stores pairs of (key → value) with O(1) average time for get/put.
 *
 * Used here to create an empty 'claims' map:
 *   Map<String, Object> claims = new HashMap<>();
 *
 * This map can hold CUSTOM CLAIMS (extra data) to embed in the JWT payload.
 * For example: claims.put("role", "ADMIN") would add a role claim to the token.
 *
 * Here it's empty — only the standard claims (subject, issuedAt, expiration)
 * are added via the builder methods.
 */

import java.util.Map;
/*
 * Map — Interface from: java.util (Java Standard Library - JDK built-in)
 * ─────────────────────────────────────────────────────────────────────────────
 * Map is the standard Java interface for key-value data structures.
 * HashMap is one of its implementations.
 *
 * Using Map<String, Object> as the type (instead of HashMap directly)
 * follows the "program to interfaces" principle — the code depends on
 * the Map interface, not the specific HashMap implementation.
 * This makes it easy to swap HashMap for LinkedHashMap etc. if needed.
 */

import java.util.function.Function;
/*
 * Function — Functional Interface from: java.util.function (Java Standard Library)
 * ─────────────────────────────────────────────────────────────────────────────
 * Function<T, R> is a functional interface that represents a function
 * that takes one argument of type T and returns a result of type R.
 *
 * It has one abstract method:
 *   R apply(T t)  → applies the function to the given argument
 *
 * Used here in extractClaim():
 *   Function<Claims, T> claimResolver
 *
 * This means: "Pass in a function that takes a Claims object and returns
 * any type T." This makes extractClaim() reusable for extracting ANY
 * claim from the token — subject, expiration, or custom claims.
 *
 * Examples of how it's called:
 *   extractClaim(token, Claims::getSubject)    → extracts username (String)
 *   extractClaim(token, Claims::getExpiration) → extracts expiry (Date)
 *
 * Claims::getSubject is a METHOD REFERENCE — shorthand for:
 *   claims -> claims.getSubject()
 */


/*
 * ═══════════════════════════════════════════════════════════════════════════
 * CLASS: JwtAuthService
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose:
 *   This service class contains ALL JWT-related business logic for the app:
 *     1. Generating a secret key (for signing tokens)
 *     2. Creating (generating) JWT tokens for authenticated users
 *     3. Parsing (reading) JWT tokens to extract claims
 *     4. Validating JWT tokens (checking username match + expiry)
 *
 * What is JWT (JSON Web Token)?
 *   A JWT is a compact, self-contained token that carries user identity info.
 *   Structure:  [Header].[Payload].[Signature]
 *
 *   Header:    { "alg": "HS256", "typ": "JWT" }         → algorithm info
 *   Payload:   { "sub": "user@email.com", "exp": ... }   → user data/claims
 *   Signature: HMAC-SHA256(header + payload, secretKey)  → integrity proof
 *
 * How JWT Authentication works:
 *   1. User logs in with username + password.
 *   2. Server verifies credentials and calls generateJwtAuthToken(username).
 *   3. Server sends the JWT token back to the client.
 *   4. Client stores the token (localStorage, memory, etc.).
 *   5. Client sends the token in every request:
 *        Authorization: Bearer <token>
 *   6. JwtAuthFilter intercepts each request, calls this service to:
 *        a. extractUserNameFromToken(token) → get username
 *        b. validateToken(token, userDetails) → verify token is valid
 *   7. If valid → user is authenticated for that request.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
/*
 * @Service:
 *   Registers this class as a Spring Bean in the Service layer.
 *   Spring will create one instance of JwtAuthService and manage it.
 *   JwtAuthFilter and AuthController can @Autowire it to use its methods.
 */
public class JwtAuthService {

    private final String secretKey;
    /*
     * private final String secretKey:
     * ─────────────────────────────────────────────────────────────────────
     * Stores the Base64-encoded HMAC-SHA256 secret key as a String.
     *
     * Why store as String (not SecretKey directly)?
     *   - Strings are easy to store, log, and manage in configuration.
     *   - The actual SecretKey object is recreated from this String each time
     *     it's needed via getKey() / getSecurityKey().
     *
     * 'final' means this value is set once in the constructor and
     * cannot be changed — the same key is used for all tokens in this
     * application session.
     *
     * ⚠️ IMPORTANT NOTE for production:
     *   A new secretKey is generated EVERY TIME the application starts.
     *   This means all previously issued JWT tokens become INVALID after
     *   an application restart. For production, store the key in
     *   application.properties or a secret manager (e.g., AWS Secrets Manager).
     */

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * CONSTRUCTOR: JwtAuthService()
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Initializes the service by generating a new HMAC-SHA256 secret key
     *   when the application starts up.
     *   The generated key is stored in the 'secretKey' field.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    public JwtAuthService() {
        secretKey = generateSecretKey();
        /*
         * Calls generateSecretKey() and stores the result in the final field.
         * This happens ONCE when Spring creates the JwtAuthService bean at startup.
         * All JWT tokens generated during this app session will use this same key.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: generateSecretKey
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Generates a cryptographically secure random HMAC-SHA256 secret key
     *   and returns it as a Base64-encoded String.
     *
     * Returns:
     *   String — Base64-encoded representation of the generated secret key.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    public String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            /*
             * KeyGenerator.getInstance("HmacSHA256"):
             * ─────────────────────────────────────────────────────────────
             * Creates a KeyGenerator instance configured to generate keys
             * for the HmacSHA256 algorithm.
             *
             * "HmacSHA256":
             *   HMAC = Hash-based Message Authentication Code
             *   SHA256 = 256-bit Secure Hash Algorithm
             *   Together: produces a 256-bit (32-byte) cryptographic hash
             *   of data using a secret key. This is used to sign the JWT.
             *
             * getInstance() is a static factory method — standard Java pattern
             * for getting cryptography algorithm instances.
             *
             * Throws NoSuchAlgorithmException if "HmacSHA256" is unknown
             * (won't happen in standard JVMs).
             */

            SecretKey secretKey = keyGen.generateKey();
            /*
             * keyGen.generateKey():
             * ─────────────────────────────────────────────────────────────
             * Generates a new, random SecretKey for HmacSHA256.
             * The key is cryptographically random — generated using a secure
             * random number generator internally.
             *
             * For HmacSHA256, the generated key is typically 256 bits (32 bytes).
             * This meets the minimum key size requirement for HS256 JWT signing.
             */

            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
            /*
             * secretKey.getEncoded():
             *   Returns the raw key as a byte array (byte[]).
             *
             * Base64.getEncoder().encodeToString(byte[]):
             *   Converts the raw byte array into a Base64-encoded String.
             *
             * Why Base64?
             *   Raw bytes are binary data — not printable, not storable as plain text.
             *   Base64 encodes binary → text (only uses A-Z, a-z, 0-9, +, / characters).
             *   Result is a safe, readable String that can be stored in config files.
             *
             * Example output:
             *   "K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=" (looks like this)
             */

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error Generating the Key", e);
            /*
             * Wrapping NoSuchAlgorithmException in RuntimeException:
             * ─────────────────────────────────────────────────────────────
             * NoSuchAlgorithmException is a CHECKED exception — Java forces
             * you to either catch it or declare it with 'throws'.
             *
             * Since "HmacSHA256" is always available in standard JVMs,
             * this exception will practically never occur.
             *
             * We wrap it in RuntimeException (unchecked) so callers of
             * this method don't need to handle it explicitly.
             * If it somehow does occur, the app will fail fast at startup
             * with a clear error message: "Error Generating the Key".
             */
        }
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: generateJwtAuthToken
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Creates and returns a signed JWT token for the given username.
     *   This is called after successful login to issue a token to the user.
     *
     * Parameter:
     *   String username → the username to embed in the token's subject claim
     *
     * Returns:
     *   String — the compact, signed JWT token string
     *            (looks like: xxxxx.yyyyy.zzzzz)
     *
     * Token validity: 3 minutes from creation time.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    public String generateJwtAuthToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        /*
         * Map<String, Object> claims = new HashMap<>():
         * ─────────────────────────────────────────────────────────────────
         * Creates an empty map for CUSTOM CLAIMS to embed in the JWT payload.
         *
         * Custom claims are extra key-value pairs you can add to the token.
         * Examples:
         *   claims.put("role", "ADMIN")         → add user role
         *   claims.put("email", "a@example.com") → add email
         *   claims.put("userId", 42)             → add database ID
         *
         * Here the map is empty — only standard claims (subject, issuedAt,
         * expiration) are added via dedicated builder methods below.
         */

        return Jwts.builder()
                /*
                 * Jwts.builder():
                 *   Returns a JwtBuilder instance — a fluent builder for
                 *   constructing a JWT token step by step.
                 */

                .claims(claims)
                /*
                 * .claims(claims):
                 *   Sets the custom claims map as the JWT payload's claims.
                 *   JJWT 0.12.x modern method — replaces deprecated setClaims().
                 *   Here the map is empty, but standard claims below are added
                 *   separately via dedicated methods.
                 */

                .subject(username)
                /*
                 * .subject(username):
                 *   Sets the "sub" (subject) claim in the JWT payload.
                 *   The subject identifies WHOM the token is about — in this
                 *   case, the username (e.g., "john@example.com").
                 *
                 *   This is the primary claim used to identify the user when
                 *   the token is later parsed by extractUserNameFromToken().
                 *
                 *   JJWT 0.12.x modern method — replaces deprecated setSubject().
                 */

                .issuedAt(new Date(System.currentTimeMillis()))
                /*
                 * .issuedAt(date):
                 *   Sets the "iat" (issued at) claim — the timestamp when
                 *   the token was CREATED.
                 *
                 *   new Date(System.currentTimeMillis()):
                 *     Creates a Date object representing RIGHT NOW.
                 *     System.currentTimeMillis() returns current time in milliseconds.
                 *
                 *   JJWT 0.12.x modern method — replaces deprecated setIssuedAt().
                 */

                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                /*
                 * .expiration(date):
                 *   Sets the "exp" (expiration) claim — the timestamp when
                 *   the token EXPIRES and becomes invalid.
                 *
                 *   System.currentTimeMillis() + 1000 * 60 * 3:
                 *     Current time + 3 minutes (in milliseconds):
                 *       1000 ms = 1 second
                 *       1000 * 60 = 1 minute
                 *       1000 * 60 * 3 = 3 minutes
                 *
                 *   After 3 minutes, isTokenExpired() returns true and the
                 *   token is rejected by validateToken().
                 *
                 *   JJWT 0.12.x modern method — replaces deprecated setExpiration().
                 */

                .signWith(getKey())
                /*
                 * .signWith(key):
                 *   Signs the JWT with the secret key using HMAC-SHA256 algorithm.
                 *
                 *   Signing creates the THIRD PART of the JWT (the Signature):
                 *     Signature = HMAC-SHA256(base64(Header) + "." + base64(Payload), secretKey)
                 *
                 *   Why sign the token?
                 *     - Ensures the token has NOT been tampered with.
                 *     - When parsing, JJWT re-computes the signature and compares it.
                 *       If they don't match → the token was modified → rejected.
                 *
                 *   getKey() returns our SecretKey derived from the stored secretKey string.
                 *
                 *   JJWT 0.12.x modern method — replaces deprecated signWith(key, algorithm).
                 *   The algorithm (HS256) is auto-resolved from the SecretKey type.
                 */

                .compact();
        /*
         * .compact():
         *   Finalizes the JWT builder and returns the token as a compact
         *   String in the standard JWT format:
         *     base64url(Header) + "." + base64url(Payload) + "." + base64url(Signature)
         *
         *   Example output:
         *     "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImlhdCI6MTcwMDAwMDAwMH0.abc123"
         *
         *   This string is what gets sent to the client after successful login.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: getKey  (private)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Decodes the Base64-encoded secretKey String and creates a
     *   SecretKey object from the decoded bytes. Used when signing tokens.
     *
     * Returns:
     *   SecretKey — the cryptographic key object for signing JWT tokens.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        /*
         * Decoders.BASE64.decode(secretKey):
         * ─────────────────────────────────────────────────────────────────
         * Decodes the Base64-encoded secretKey String back into raw bytes.
         *
         * Why do we need to decode?
         *   The key was stored as a Base64 String for readability/storage.
         *   Cryptographic operations need raw binary bytes, not strings.
         *
         * Decoders.BASE64 is JJWT's own Base64 decoder utility (from jjwt-api).
         * It's equivalent to Java's Base64.getDecoder().decode() but is
         * provided by JJWT for convenience and consistency within the library.
         */

        return Keys.hmacShaKeyFor(keyBytes);
        /*
         * Keys.hmacShaKeyFor(keyBytes):
         * ─────────────────────────────────────────────────────────────────
         * Creates a SecretKey object from the raw key bytes, configured
         * for use with HMAC-SHA algorithms.
         *
         * JJWT auto-selects the SHA variant based on key length:
         *   32 bytes (256-bit) → HS256
         *   48 bytes (384-bit) → HS384
         *   64 bytes (512-bit) → HS512
         *
         * The returned SecretKey is used in:
         *   .signWith(getKey()) — to sign new JWT tokens
         *   .verifyWith(getSecurityKey()) — to verify existing JWT tokens
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: getSecurityKey  (private)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Same as getKey() — decodes the stored Base64 secretKey String and
     *   returns a SecretKey object. Used specifically when PARSING/VERIFYING
     *   JWT tokens in extractAllClaims().
     *
     * Note:
     *   getKey() and getSecurityKey() do the same thing. They can be
     *   consolidated into one method. Having two is a refactoring opportunity.
     *
     * Returns:
     *   SecretKey — the cryptographic key for verifying JWT token signatures.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    private SecretKey getSecurityKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
        /*
         * Same logic as getKey() — decodes Base64 String → byte[] → SecretKey.
         * Return type is explicitly SecretKey (JJWT 0.12.x requirement) instead
         * of the broader Key interface used in older JJWT versions.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: extractUserName
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Extracts the username (subject claim) from the JWT token.
     *
     * Parameter:
     *   String token → the JWT token string to parse
     *
     * Returns:
     *   String — the username stored in the token's "sub" claim
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
        /*
         * extractClaim(token, Claims::getSubject):
         * ─────────────────────────────────────────────────────────────────
         * Delegates to the generic extractClaim() method.
         *
         * Claims::getSubject is a METHOD REFERENCE:
         *   Short form of: claims -> claims.getSubject()
         *   Tells extractClaim() to apply the getSubject() function on
         *   the Claims object to extract the "sub" (subject/username) claim.
         *
         * Claims::getSubject matches the Function<Claims, String> signature:
         *   Takes a Claims → returns a String (the username)
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: extractClaim  (private, generic)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   A GENERIC utility method to extract ANY claim from a JWT token.
     *   Avoids code duplication — used by extractUserName(), extractExpiration().
     *
     * Type Parameter:
     *   <T> → The type of the claim value being extracted (String, Date, etc.)
     *
     * Parameters:
     *   String token                      → the JWT token to parse
     *   Function<Claims, T> claimResolver → a function that extracts a specific
     *                                       claim from the Claims object
     * Returns:
     *   T — the extracted claim value (type depends on what claimResolver returns)
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        /*
         * extractAllClaims(token):
         *   Parses the JWT token and returns the entire Claims (payload) object.
         *   All claims (subject, issuedAt, expiration, custom claims) are
         *   available in this Claims object.
         */

        return claimResolver.apply(claims);
        /*
         * claimResolver.apply(claims):
         * ─────────────────────────────────────────────────────────────────
         * Applies the passed-in Function to the Claims object.
         *
         * 'apply()' is the single abstract method of the Function<T, R> interface.
         * It executes the function logic with 'claims' as the input.
         *
         * Examples:
         *   If claimResolver = Claims::getSubject   → returns claims.getSubject()   (String)
         *   If claimResolver = Claims::getExpiration → returns claims.getExpiration() (Date)
         *
         * This pattern (Strategy Pattern) makes extractClaim() reusable for
         * extracting any type of claim without writing separate methods for each.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: extractAllClaims  (private)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Parses the JWT token string, verifies its signature, and returns
     *   the complete Claims (payload) object.
     *
     *   This is the CORE parsing method — all other extract methods
     *   ultimately call this one.
     *
     * Parameter:
     *   String token → the JWT token string to parse and verify
     *
     * Returns:
     *   Claims — the full payload of the verified JWT token
     *
     * Throws (automatically by JJWT):
     *   ExpiredJwtException      → if the token has expired
     *   SignatureException        → if the token signature is invalid
     *   MalformedJwtException     → if the token format is wrong
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                /*
                 * Jwts.parser():
                 *   Returns a JwtParserBuilder — a fluent builder for
                 *   configuring the JWT parser.
                 *   JJWT 0.12.x modern method — replaces deprecated Jwts.parserBuilder().
                 */

                .verifyWith(getSecurityKey())
                /*
                 * .verifyWith(secretKey):
                 *   Provides the SecretKey to use for VERIFYING the JWT signature.
                 *
                 *   When parsing, JJWT will:
                 *     1. Re-compute the HMAC-SHA256 signature using this key.
                 *     2. Compare it with the signature in the token.
                 *     3. If they match → token is authentic (not tampered).
                 *     4. If they don't match → throw SignatureException → reject.
                 *
                 *   JJWT 0.12.x modern method — replaces deprecated setSigningKey().
                 */

                .build()
                /*
                 * .build():
                 *   Finalizes the parser configuration and returns a JwtParser object.
                 *   After .build(), the parser is ready to parse tokens.
                 */

                .parseSignedClaims(token)
                /*
                 * .parseSignedClaims(token):
                 *   Parses the JWT token string, verifies the signature, and
                 *   returns a Jws<Claims> object (a JWT with Claims payload).
                 *
                 *   'Signed' means this method is designed for tokens with a
                 *   cryptographic signature (which is what we have with HMAC-SHA256).
                 *
                 *   JJWT 0.12.x modern method — replaces deprecated parseClaimsJwt().
                 *   (parseClaimsJwt() was for UNSIGNED tokens, which was a bug risk)
                 *
                 *   Automatically throws:
                 *     ExpiredJwtException   → if the "exp" claim is in the past
                 *     SignatureException     → if signature verification fails
                 */

                .getPayload();
        /*
         * .getPayload():
         *   Extracts and returns the Claims (payload) object from the
         *   parsed JWT token.
         *
         *   JJWT 0.12.x modern method — replaces deprecated getBody().
         *
         *   The returned Claims object contains all the data embedded
         *   in the token: subject, issuedAt, expiration, and any
         *   custom claims added during token creation.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: extractUserNameFromToken  (public)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Public-facing method to extract the username from a JWT token.
     *   Called by JwtAuthFilter to get the username from the incoming
     *   request's JWT token.
     *
     * Parameter:
     *   String token → the JWT token string
     *
     * Returns:
     *   String — the username embedded in the token
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    public String extractUserNameFromToken(String token) {
        return extractUserName(token);
        /*
         * Delegates to extractUserName() which in turn calls:
         *   extractClaim(token, Claims::getSubject)
         *     → extractAllClaims(token) → parse and verify token
         *     → getSubject() → return username string
         *
         * Having this separate public method provides a clean, descriptive
         * API for external callers (like JwtAuthFilter) without exposing
         * the internal extractUserName() method name.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: validateToken  (public)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Validates a JWT token by checking:
     *     1. The username in the token matches the UserDetails username.
     *     2. The token has NOT expired.
     *
     * Called by JwtAuthFilter after loading UserDetails from the database
     * to confirm the token belongs to this user and is still valid.
     *
     * Parameters:
     *   String token            → the JWT token to validate
     *   UserDetails userDetails → the user loaded from the database
     *
     * Returns:
     *   boolean → true if token is valid and belongs to this user
     *             false if username mismatch or token is expired
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserNameFromToken(token);
        /*
         * Extracts the username stored inside the JWT token's subject claim.
         * This is the username that was embedded when the token was created.
         */

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        /*
         * Two conditions that BOTH must be true for the token to be valid:
         *
         * 1. username.equals(userDetails.getUsername()):
         *    Checks that the username in the token MATCHES the username
         *    of the user loaded from the database.
         *
         *    Why check this?
         *      Prevents token substitution attacks — a valid token for
         *      user A cannot be used to authenticate as user B.
         *      .equals() is the standard Java String comparison method.
         *
         * 2. !isTokenExpired(token):
         *    Checks that the token has NOT expired.
         *    isTokenExpired() returns true if expired, so '!' negates it:
         *      true (not expired) → allow access
         *      false (expired)    → deny access
         *
         * Short-circuit evaluation:
         *   Java uses '&&' (AND) — if username doesn't match, the second
         *   condition (!isTokenExpired) is NOT evaluated (optimization).
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: isTokenExpired  (private)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Checks whether the JWT token has expired by comparing its
     *   expiration date with the current date/time.
     *
     * Parameter:
     *   String token → the JWT token to check
     *
     * Returns:
     *   boolean → true if the token IS expired (expiry date is before now)
     *             false if the token is still valid
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
        /*
         * extractExpiration(token):
         *   Extracts the "exp" (expiration) Date from the token's claims.
         *
         * .before(new Date()):
         *   Date.before(anotherDate) returns true if this date is BEFORE
         *   the other date.
         *
         *   new Date() represents RIGHT NOW (current date and time).
         *
         * So:
         *   extractExpiration(token).before(new Date())
         *   = "Is the token's expiry date BEFORE right now?"
         *   = true  → expiry was in the past → token IS expired
         *   = false → expiry is in the future → token is still valid
         *
         * Example:
         *   Token expiry: 2024-01-01 10:03:00
         *   Current time: 2024-01-01 10:05:00
         *   10:03 before 10:05? → true → token expired
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: extractExpiration  (private)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Extracts the expiration date from the JWT token's claims.
     *   Used by isTokenExpired() to check if the token has expired.
     *
     * Parameter:
     *   String token → the JWT token to parse
     *
     * Returns:
     *   Date — the expiration date/time embedded in the token's "exp" claim
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
        /*
         * extractClaim(token, Claims::getExpiration):
         *   Reuses the generic extractClaim() method.
         *
         *   Claims::getExpiration is a method reference:
         *     Short form of: claims -> claims.getExpiration()
         *     Returns the "exp" claim value as a java.util.Date object.
         *
         *   This Date is then used in isTokenExpired() to compare
         *   against the current time using Date.before(new Date()).
         */
    }
}