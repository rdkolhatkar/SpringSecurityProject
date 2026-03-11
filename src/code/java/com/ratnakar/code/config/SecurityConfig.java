package com.ratnakar.code.config;

/*
 * ═══════════════════════════════════════════════════════════════════════════
 * IMPORTS SECTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Imports come from FOUR sources:
 *
 * 1) Your own project classes:
 *    - com.ratnakar.code.service.CustomUserDetailsService  (your custom class)
 *    - com.ratnakar.code.config.JwtAuthFilter              (your custom class)
 *
 * 2) Lombok — from dependency:
 *    <dependency>
 *        <groupId>org.projectlombok</groupId>
 *        <artifactId>lombok</artifactId>
 *    </dependency>
 *
 * 3) Spring Framework — from dependency:
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-web</artifactId>
 *    </dependency>
 *
 * 4) Spring Security — from dependency:
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-security</artifactId>
 *    </dependency>
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */

import com.ratnakar.code.service.CustomUserDetailsService;
/*
 * CustomUserDetailsService — Your own service class (in this project)
 * ─────────────────────────────────────────────────────────────────────────────
 * This is your custom implementation of Spring Security's UserDetailsService.
 * Its job: load a user from the database by username and return a UserDetails
 * object containing credentials and roles.
 *
 * It is wired into DaoAuthenticationProvider so Spring Security knows HOW
 * to fetch user data when validating login credentials.
 */

import lombok.RequiredArgsConstructor;
/*
 * @RequiredArgsConstructor — Lombok Annotation from dependency: lombok
 * ─────────────────────────────────────────────────────────────────────────────
 * Lombok is a Java library that auto-generates boilerplate code at compile time.
 *
 * @RequiredArgsConstructor automatically generates a CONSTRUCTOR for all fields
 * that are declared as:
 *   - final fields, OR
 *   - fields annotated with @NonNull
 *
 * Without Lombok, you'd have to write this manually:
 *   public SecurityConfig(CustomUserDetailsService userDetailsService) {
 *       this.userDetailsService = userDetailsService;
 *   }
 *
 * With @RequiredArgsConstructor, Lombok generates this constructor for you.
 *
 * Why use constructor injection (final field) instead of @Autowired?
 *   - Constructor injection is the RECOMMENDED way in Spring (especially
 *     for mandatory dependencies).
 *   - It makes the class immutable and easier to unit test.
 *   - Spring automatically uses the generated constructor to inject the bean.
 */

import org.springframework.beans.factory.annotation.Autowired;
/*
 * @Autowired — Annotation from: spring-context (spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * Tells Spring to automatically inject the matching bean from the IoC container.
 * Used here to inject JwtAuthFilter (field injection style).
 *
 * Note: For JwtAuthFilter, @Autowired field injection is used instead of
 * constructor injection (via @RequiredArgsConstructor) to avoid potential
 * circular dependency issues at startup.
 */

import org.springframework.context.annotation.Bean;
/*
 * @Bean — Annotation from: spring-context (spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * Marks a method so its return value is registered as a Spring-managed Bean
 * in the IoC container. Spring calls these methods once at startup and stores
 * the returned objects. Other classes can then @Autowire / inject them.
 *
 * In this class, @Bean is used to register:
 *   - PasswordEncoder         → for hashing passwords with BCrypt
 *   - DaoAuthenticationProvider → for verifying credentials during login
 *   - AuthenticationManager   → for programmatic authentication (e.g., /login)
 *   - SecurityFilterChain     → for defining all HTTP security rules
 */

import org.springframework.context.annotation.Configuration;
/*
 * @Configuration — Annotation from: spring-context
 * ─────────────────────────────────────────────────────────────────────────────
 * Marks this class as a Spring configuration class — a "settings file".
 * Spring Boot scans and loads this class at startup and registers all
 * @Bean methods defined inside it into the IoC container.
 */

import org.springframework.http.HttpMethod;
/*
 * HttpMethod — Enum class from: spring-web (spring-boot-starter-web)
 * ─────────────────────────────────────────────────────────────────────────────
 * HttpMethod is an enum that represents the standard HTTP request methods:
 *   HttpMethod.GET     → Retrieve data
 *   HttpMethod.POST    → Create new data
 *   HttpMethod.PUT     → Update existing data
 *   HttpMethod.DELETE  → Delete data
 *   HttpMethod.PATCH   → Partial update
 *   HttpMethod.HEAD    → Like GET but returns only headers
 *   HttpMethod.OPTIONS → Describes communication options
 *
 * Used here with requestMatchers() to apply security rules to SPECIFIC
 * HTTP methods on a URL — e.g., GET /api/products is public but
 * POST /api/products requires authentication.
 */

import org.springframework.security.authentication.AuthenticationManager;
/*
 * AuthenticationManager — Interface from: spring-security-core
 * ─────────────────────────────────────────────────────────────────────────────
 * AuthenticationManager is the MAIN interface for performing authentication
 * in Spring Security.
 *
 * It has ONE method:
 *   Authentication authenticate(Authentication authentication)
 *
 * What it does:
 *   - Takes an Authentication object (e.g., username + password).
 *   - Verifies the credentials.
 *   - Returns a fully authenticated Authentication object if valid.
 *   - Throws AuthenticationException if credentials are wrong.
 *
 * Why expose it as a @Bean?
 *   - It is needed in your login endpoint (e.g., /api/auth/login) to
 *     programmatically authenticate the user and generate a JWT token.
 *   - Without this bean, you can't call authenticationManager.authenticate()
 *     from your AuthController.
 */

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
/*
 * DaoAuthenticationProvider — Class from: spring-security-core
 * ─────────────────────────────────────────────────────────────────────────────
 * DaoAuthenticationProvider is a concrete implementation of
 * AuthenticationProvider that authenticates users by:
 *   1. Loading user details from a database via UserDetailsService
 *      (your CustomUserDetailsService).
 *   2. Comparing the submitted password against the stored encoded password
 *      using a PasswordEncoder (BCryptPasswordEncoder here).
 *
 * "Dao" stands for Data Access Object — it fetches user data via your
 * UserDetailsService which queries the database.
 *
 * Flow when a user tries to log in:
 *   User submits username + password
 *       → DaoAuthenticationProvider.authenticate() is called
 *       → Calls CustomUserDetailsService.loadUserByUsername(username)
 *       → Gets UserDetails from DB
 *       → BCryptPasswordEncoder.matches(rawPassword, encodedPassword)
 *       → If match → authentication succeeds
 *       → If no match → throws BadCredentialsException
 */

import org.springframework.security.config.Customizer;
/*
 * Customizer — Functional Interface from: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * Used with Customizer.withDefaults() to apply Spring Security's default
 * settings for a feature without manually specifying every configuration option.
 *
 * Used here with .httpBasic(Customizer.withDefaults()) to enable HTTP Basic
 * Authentication with default settings.
 */

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
/*
 * AuthenticationConfiguration — Class from: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * AuthenticationConfiguration is a Spring Security class that provides access
 * to the auto-configured AuthenticationManager.
 *
 * Spring Security automatically creates and configures an AuthenticationManager
 * internally. AuthenticationConfiguration exposes it via:
 *   config.getAuthenticationManager()
 *
 * This is the modern Spring Security 5.4+ approach to obtaining the
 * AuthenticationManager without extending WebSecurityConfigurerAdapter
 * (which is now deprecated).
 */

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/*
 * HttpSecurity — Builder Class from: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * The central builder for configuring all web security settings:
 *   - URL access rules (who can access which endpoint)
 *   - Authentication mechanisms (Basic Auth, OAuth2, JWT, etc.)
 *   - CSRF protection
 *   - Session management
 *   - Filters (adding custom filters like JwtAuthFilter)
 *   - Exception handling
 *
 * All configurations are applied in a fluent/chained style.
 * .build() at the end finalizes and creates the SecurityFilterChain.
 */

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
/*
 * @EnableWebSecurity — Annotation from: spring-security-web
 * ─────────────────────────────────────────────────────────────────────────────
 * Activates Spring Security's web security support for the application.
 * Without this, none of the security rules defined in this class take effect.
 * This annotation also imports all necessary Spring Security infrastructure.
 */

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
/*
 * AbstractHttpConfigurer — Abstract Class from: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * Base class for all Spring Security configurers (like CsrfConfigurer,
 * FormLoginConfigurer, etc.).
 *
 * Used here as a METHOD REFERENCE to disable CSRF:
 *   .csrf(AbstractHttpConfigurer::disable)
 *
 * AbstractHttpConfigurer::disable is a method reference that calls the
 * disable() method on the CSRF configurer — effectively turning CSRF
 * protection OFF for this application.
 *
 * Why disable CSRF for REST APIs?
 *   CSRF (Cross-Site Request Forgery) protection is needed for browser-based
 *   apps that use session cookies. Since this is a stateless REST API using
 *   JWT tokens (not cookies), CSRF protection is unnecessary and can cause
 *   issues with POST/PUT/DELETE requests.
 */

import org.springframework.security.config.http.SessionCreationPolicy;
/*
 * SessionCreationPolicy — Enum from: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * Defines how Spring Security should manage HTTP sessions.
 *
 * Available options:
 *   SessionCreationPolicy.ALWAYS     → Always create a session
 *   SessionCreationPolicy.IF_REQUIRED → Create session only if required (default)
 *   SessionCreationPolicy.NEVER      → Never create, but use if one exists
 *   SessionCreationPolicy.STATELESS  → Never create or use sessions
 *
 * STATELESS is used here because:
 *   - This is a JWT-based REST API.
 *   - JWT tokens carry authentication info with every request.
 *   - No server-side session is needed — each request is self-contained.
 *   - Stateless APIs are more scalable (no session storage needed on server).
 */

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/*
 * BCryptPasswordEncoder — Class from: spring-security-crypto
 * ─────────────────────────────────────────────────────────────────────────────
 * BCryptPasswordEncoder is a password hashing implementation that uses the
 * BCrypt strong hashing algorithm.
 *
 * What is BCrypt?
 *   - A one-way hashing algorithm specifically designed for passwords.
 *   - Each hash includes a random "salt" — so the same password always
 *     produces a DIFFERENT hash every time. This prevents rainbow table attacks.
 *   - The hashing process is intentionally SLOW (configurable) to make
 *     brute-force attacks computationally expensive.
 *
 * Key methods:
 *   encoder.encode("rawPassword")                   → hashes the password
 *   encoder.matches("rawPassword", "storedHash")    → verifies a password
 *
 * NEVER store plain-text passwords in the database.
 * Always store the BCrypt hash and use .matches() for verification.
 *
 * Example:
 *   "password123"  →  "$2a$10$7EqJtq98hPqEX7fNZaFWoO..."  (BCrypt hash)
 */

import org.springframework.security.crypto.password.PasswordEncoder;
/*
 * PasswordEncoder — Interface from: spring-security-crypto
 * ─────────────────────────────────────────────────────────────────────────────
 * PasswordEncoder is the standard Spring Security interface for password
 * encoding and matching.
 *
 * Key methods defined in this interface:
 *   String encode(CharSequence rawPassword)
 *     → Encodes/hashes the raw password before storing in DB.
 *
 *   boolean matches(CharSequence rawPassword, String encodedPassword)
 *     → Checks if raw password matches the stored encoded password.
 *
 * BCryptPasswordEncoder is the most commonly used implementation of this
 * interface. By returning it as a PasswordEncoder @Bean, you keep the code
 * loosely coupled — you could swap BCrypt for another algorithm without
 * changing any other code.
 */

import org.springframework.security.web.SecurityFilterChain;
/*
 * SecurityFilterChain — Interface from: spring-security-web
 * ─────────────────────────────────────────────────────────────────────────────
 * Represents the chain of security filters applied to every HTTP request.
 * The @Bean of this type is Spring Security's main hook for applying your
 * custom security configuration to the application.
 *
 * Every request passes through this chain of filters before reaching
 * your controllers.
 */

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
/*
 * UsernamePasswordAuthenticationFilter — Class from: spring-security-web
 * ─────────────────────────────────────────────────────────────────────────────
 * This is a built-in Spring Security filter that handles form-based
 * username/password login (default login form at /login).
 *
 * Why is it referenced here?
 *   It is used as a POSITION MARKER in the filter chain:
 *     .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
 *
 *   This tells Spring Security:
 *     "Add my JwtAuthFilter to run BEFORE the UsernamePasswordAuthenticationFilter."
 *
 *   Why before?
 *     JwtAuthFilter must run first to check for a JWT token and set the
 *     authentication in SecurityContextHolder BEFORE Spring Security's
 *     default filters try to authenticate the request themselves.
 *     If JWT auth is already set, the later filters will skip re-authenticating.
 */


/*
 * ═══════════════════════════════════════════════════════════════════════════
 * CLASS: SecurityConfig
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose:
 *   Central security configuration class for the entire application.
 *   Defines which endpoints are public, which are protected, how passwords
 *   are encoded, how users are authenticated, and how JWT filters are applied.
 *
 * Security rules defined in this class:
 *
 *   PUBLIC (no credentials required):
 *     POST /api/auth/register   → user registration (anyone can register)
 *     POST /api/auth/login      → user login (anyone can attempt login)
 *     GET  /api/products/**     → anyone can browse/view products
 *     GET  /api/products        → anyone can list products
 *
 *   PROTECTED (valid JWT token required):
 *     POST   /api/products      → add a new product
 *     PUT    /api/products/{id} → update a product
 *     DELETE /api/products/{id} → delete a product
 *     Everything else           → requires authentication
 *
 * Authentication mechanism: JWT (stateless) + HTTP Basic Auth
 * Session policy: STATELESS (no server-side sessions)
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
/*
 * @Configuration:
 *   Marks this as a Spring configuration class.
 *   Spring Boot automatically scans and loads this at startup.
 *   All @Bean methods inside are registered in the IoC container.
 */

@EnableWebSecurity
/*
 * @EnableWebSecurity:
 *   Activates Spring Security's web security support.
 *   Required for any custom security configuration to take effect.
 */

@RequiredArgsConstructor
/*
 * @RequiredArgsConstructor (Lombok):
 *   Auto-generates a constructor for the 'final' field:
 *     private final CustomUserDetailsService userDetailsService;
 *
 *   Generated constructor (invisible but present at compile time):
 *     public SecurityConfig(CustomUserDetailsService userDetailsService) {
 *         this.userDetailsService = userDetailsService;
 *     }
 *
 *   Spring uses this constructor to inject CustomUserDetailsService automatically.
 *   This is constructor-based dependency injection — the recommended approach.
 */
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    /*
     * private final CustomUserDetailsService userDetailsService:
     * ─────────────────────────────────────────────────────────────────────
     * Declares CustomUserDetailsService as a required dependency.
     *
     * 'final' means:
     *   - This field MUST be set via the constructor (cannot be null).
     *   - Once set, it cannot be reassigned — making this class immutable.
     *
     * @RequiredArgsConstructor generates the constructor that populates this.
     * Spring injects the CustomUserDetailsService bean automatically.
     */

    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    /*
     * @Autowired JwtAuthFilter:
     * ─────────────────────────────────────────────────────────────────────
     * Spring automatically injects the JwtAuthFilter bean here.
     *
     * JwtAuthFilter is used in filterChain() to add it to the security
     * filter chain BEFORE UsernamePasswordAuthenticationFilter.
     *
     * Why @Autowired (field injection) instead of constructor injection here?
     *   JwtAuthFilter itself depends on Spring Security components, and
     *   SecurityConfig also configures Spring Security. Using constructor
     *   injection for both would create a circular dependency at startup.
     *   Field injection with @Autowired avoids this issue.
     */

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * BEAN: passwordEncoder
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Creates and registers a BCryptPasswordEncoder bean.
     *   Used throughout the app wherever passwords need to be
     *   hashed (during registration) or verified (during login).
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        /*
         * new BCryptPasswordEncoder():
         *   Creates a BCrypt password encoder with default strength (10 rounds).
         *   "10 rounds" means BCrypt performs 2^10 = 1024 iterations of hashing,
         *   making brute-force attacks very slow and computationally expensive.
         *
         *   Returned as PasswordEncoder interface (not the concrete class) for
         *   loose coupling — easy to swap the implementation later if needed.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * BEAN: authenticationProvider
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Creates and configures a DaoAuthenticationProvider bean.
     *   This wires together your CustomUserDetailsService (WHERE to find users)
     *   and BCryptPasswordEncoder (HOW to verify passwords).
     *
     *   Spring Security uses this provider internally when authenticating
     *   login requests.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        /*
         * new DaoAuthenticationProvider(userDetailsService):
         *   Spring Security 6.x modern approach — passes UserDetailsService
         *   directly via constructor injection.
         *
         *   This tells the provider: "When someone tries to log in with a
         *   username, use CustomUserDetailsService to load that user from DB."
         *
         *   Older approach (deprecated style) used:
         *     provider.setUserDetailsService(userDetailsService);
         *   Constructor injection is now preferred in Spring Security 6.x.
         */

        provider.setPasswordEncoder(passwordEncoder());
        /*
         * provider.setPasswordEncoder(passwordEncoder()):
         *   Tells the provider WHICH password encoder to use for verifying passwords.
         *
         *   When a user logs in:
         *     → Provider loads user from DB via CustomUserDetailsService.
         *     → Gets the stored BCrypt hash from UserDetails.
         *     → Calls passwordEncoder.matches(rawPassword, storedHash).
         *     → If true → login succeeds. If false → BadCredentialsException.
         *
         *   No constructor alternative exists for PasswordEncoder injection,
         *   so setter injection (setPasswordEncoder) is still used here.
         */

        return provider;
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * BEAN: authenticationManager
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Exposes Spring Security's internal AuthenticationManager as a @Bean
     *   so it can be @Autowired into other classes (e.g., your login controller).
     *
     * Why is this needed?
     *   In your login endpoint (/api/auth/login), you need to call:
     *     authenticationManager.authenticate(
     *         new UsernamePasswordAuthenticationToken(username, password)
     *     );
     *   This programmatically verifies credentials and generates a JWT on success.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        /*
         * config.getAuthenticationManager():
         *   AuthenticationConfiguration is auto-provided by Spring Security.
         *   It internally builds and configures the AuthenticationManager using
         *   the DaoAuthenticationProvider we defined above (picked up automatically).
         *
         *   By returning it as a @Bean, we make it injectable anywhere in the app:
         *     @Autowired
         *     AuthenticationManager authenticationManager;
         *
         *   throws Exception: getAuthenticationManager() declares a checked
         *   exception, so we propagate it with 'throws Exception'.
         */
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * BEAN: filterChain (Main Security Configuration)
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   This is the HEART of the security configuration.
     *   Defines all HTTP security rules, session policy, filters,
     *   URL access control, and authentication mechanisms.
     *
     * Parameter:
     *   HttpSecurity http — Spring injects this builder automatically.
     *   Used to configure all security rules step by step (fluent API).
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                /*
                 * .csrf(AbstractHttpConfigurer::disable):
                 * ─────────────────────────────────────────────────────────────
                 * Disables CSRF (Cross-Site Request Forgery) protection.
                 *
                 * What is CSRF?
                 *   An attack where a malicious website tricks a logged-in user's
                 *   browser into making unwanted requests to your server (using
                 *   the user's active session cookie).
                 *
                 * Why disable it here?
                 *   - This is a STATELESS REST API using JWT tokens.
                 *   - No session cookies are used (STATELESS policy below).
                 *   - CSRF attacks rely on session cookies — without cookies,
                 *     CSRF attacks are not possible.
                 *   - Keeping CSRF enabled on a stateless JWT API would block
                 *     all POST/PUT/DELETE requests unnecessarily.
                 *
                 * AbstractHttpConfigurer::disable is a method reference equivalent to:
                 *   .csrf(csrf -> csrf.disable())
                 */

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                /*
                 * .sessionManagement(...):
                 * ─────────────────────────────────────────────────────────────
                 * Configures how Spring Security handles HTTP sessions.
                 *
                 * SessionCreationPolicy.STATELESS:
                 *   - Spring Security will NEVER create or use an HTTP session.
                 *   - No JSESSIONID cookie will be created.
                 *   - Every request must be independently authenticated using JWT.
                 *   - This makes the API truly stateless and horizontally scalable
                 *     (requests can go to any server instance — no shared session state).
                 *
                 * sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                 *   'sm' is a SessionManagementConfigurer injected by Spring.
                 *   We call .sessionCreationPolicy() on it to set STATELESS.
                 */

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                /*
                 * .addFilterBefore(filter, referenceFilter):
                 * ─────────────────────────────────────────────────────────────
                 * Inserts JwtAuthFilter into the security filter chain at a
                 * position BEFORE UsernamePasswordAuthenticationFilter.
                 *
                 * Why before UsernamePasswordAuthenticationFilter?
                 *   JwtAuthFilter needs to run first so it can:
                 *     1. Extract the JWT token from the request.
                 *     2. Validate the token.
                 *     3. Set the authenticated user in SecurityContextHolder.
                 *
                 *   Once authentication is set in SecurityContextHolder, the
                 *   subsequent filters (including UsernamePasswordAuthenticationFilter)
                 *   will see the user as already authenticated and skip re-authentication.
                 *
                 * Filter chain order (simplified):
                 *   Request → [JwtAuthFilter] → [UsernamePasswordAuthFilter] → ... → Controller
                 */

                .authorizeHttpRequests(auth -> auth
                                /*
                                 * .authorizeHttpRequests(...):
                                 * ─────────────────────────────────────────────────────────
                                 * Defines URL-level AUTHORIZATION rules.
                                 * Rules are evaluated TOP-TO-BOTTOM — first matching rule wins.
                                 * Order matters: more specific rules should come before broader ones.
                                 *
                                 * 'auth' is an AuthorizationManagerRequestMatcherRegistry
                                 * injected by Spring inside the lambda.
                                 */

                                .requestMatchers(HttpMethod.POST,   "/api/auth/register").permitAll()
                                /*
                                 * .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll():
                                 * ─────────────────────────────────────────────────────────────────────
                                 * requestMatchers(method, pattern):
                                 *   Matches requests with the given HTTP method AND URL pattern.
                                 *
                                 * permitAll():
                                 *   Allows ALL users (authenticated or not) to access this endpoint.
                                 *   No login/token required.
                                 *
                                 * This rule: Anyone can POST to /api/auth/register to create an account.
                                 */

                                .requestMatchers(HttpMethod.POST,   "/api/auth/login").permitAll()
                                /*
                                 * Anyone can POST to /api/auth/login to submit credentials and
                                 * receive a JWT token. Must be public — user is not logged in yet.
                                 */

                                .requestMatchers(HttpMethod.GET,    "/api/products/**").permitAll()
                                /*
                                 * Anyone can GET any URL under /api/products/ (e.g., /api/products/1).
                                 * The ** wildcard matches any sub-path including multiple levels.
                                 * Public product browsing — no login required.
                                 */

                                .requestMatchers(HttpMethod.GET,    "/api/products").permitAll()
                                /*
                                 * Anyone can GET /api/products (the product listing endpoint).
                                 * Separate from the above because /api/products (no trailing path)
                                 * is a distinct URL pattern.
                                 */

                                .requestMatchers(HttpMethod.POST,   "/api/products").authenticated()
                                /*
                                 * .authenticated():
                                 *   Only allows users with a valid, verified JWT token.
                                 *   If no valid token is present → 401 Unauthorized is returned.
                                 *
                                 * This rule: Only authenticated users can create (POST) a new product.
                                 */

                                .requestMatchers(HttpMethod.PUT,    "/api/products/**").authenticated()
                                /*
                                 * Only authenticated users can update (PUT) any product.
                                 * ** matches any product ID, e.g., /api/products/42
                                 */

                                .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()
                                /*
                                 * Only authenticated users can delete (DELETE) any product.
                                 */

                                .anyRequest().authenticated()
                        /*
                         * .anyRequest().authenticated():
                         *   A catch-all rule for any URL/method not matched above.
                         *   ALL remaining requests must be authenticated.
                         *   This acts as a safety net — no unintentional public endpoints.
                         *
                         * IMPORTANT: This must always be the LAST rule. Any rules defined
                         * after this will be unreachable (first match wins).
                         */
                )

                .httpBasic(Customizer.withDefaults())
                /*
                 * .httpBasic(Customizer.withDefaults()):
                 * ─────────────────────────────────────────────────────────────
                 * Enables HTTP Basic Authentication as an additional auth mechanism.
                 *
                 * What is HTTP Basic Auth?
                 *   - The client sends credentials in the request header as:
                 *     Authorization: Basic base64(username:password)
                 *   - Spring Security decodes and verifies the credentials.
                 *
                 * Why keep Basic Auth alongside JWT?
                 *   - Useful for testing APIs with tools like Postman or curl.
                 *   - Can be used as a fallback auth mechanism.
                 *   - Clients that don't support JWT can still authenticate.
                 *
                 * Customizer.withDefaults():
                 *   Applies default HTTP Basic Auth settings — Spring will return a
                 *   401 Unauthorized with a "WWW-Authenticate: Basic" header when
                 *   an unauthenticated request hits a protected endpoint.
                 */

                .authenticationProvider(authenticationProvider());
        /*
         * .authenticationProvider(authenticationProvider()):
         * ─────────────────────────────────────────────────────────────
         * Registers the DaoAuthenticationProvider bean (defined above)
         * with Spring Security.
         *
         * This tells Spring Security: "When you need to authenticate a user,
         * use THIS provider — which will load users from DB via
         * CustomUserDetailsService and verify passwords with BCrypt."
         *
         * Spring Security can support MULTIPLE authentication providers.
         * Here we're explicitly registering our custom one to ensure
         * it is used for all authentication operations.
         */

        return http.build();
        /*
         * http.build():
         * ─────────────────────────────────────────────────────────────────
         * Finalizes all the security configurations applied above and builds
         * the SecurityFilterChain object.
         *
         * This SecurityFilterChain is then:
         *   1. Returned from this method.
         *   2. Registered as a Spring Bean (@Bean).
         *   3. Picked up by Spring Security automatically.
         *   4. Applied to ALL incoming HTTP requests in the application.
         */
    }
}