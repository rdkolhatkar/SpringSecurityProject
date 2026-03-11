package com.ratnakar.code.config;

/*
 * ═══════════════════════════════════════════════════════════════════════════
 * IMPORTS SECTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This class uses imports from FOUR different sources:
 *
 * 1) Your own project's service classes:
 *    - com.ratnakar.code.service.CustomUserDetailsService  (your custom class)
 *    - com.ratnakar.code.service.JwtAuthService            (your custom class)
 *
 * 2) Jakarta Servlet API — from dependency:
 *    <dependency>
 *        <groupId>jakarta.servlet</groupId>
 *        <artifactId>jakarta.servlet-api</artifactId>
 *    </dependency>
 *    (Included automatically via spring-boot-starter-web)
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
 * CustomUserDetailsService — Your own custom service class (in this project)
 * ─────────────────────────────────────────────────────────────────────────────
 * This is a service class you have written that implements Spring Security's
 * UserDetailsService interface.
 *
 * Its job is to load user information (like username, password, roles) from
 * the database when given a username.
 *
 * Spring Security uses this to verify if the user exists during authentication.
 */

import com.ratnakar.code.service.JwtAuthService;
/*
 * JwtAuthService — Your own custom service class (in this project)
 * ─────────────────────────────────────────────────────────────────────────────
 * This is a service class you have written that contains all JWT-related logic:
 *   - Extracting the username from a JWT token
 *   - Validating whether a token is valid and not expired
 *   - Generating new JWT tokens
 *
 * JWT = JSON Web Token — a compact, self-contained token used for
 * securely transmitting user identity information between parties.
 */

import jakarta.servlet.FilterChain;
/*
 * FilterChain — Interface from: Jakarta Servlet API (jakarta.servlet-api)
 * ─────────────────────────────────────────────────────────────────────────────
 * FilterChain represents the entire chain of filters that an HTTP request
 * must pass through before reaching the actual controller/servlet.
 *
 * Think of it as a pipeline:
 *   Request → [Filter 1] → [Filter 2] → [JwtAuthFilter] → ... → Controller
 *
 * filterChain.doFilter(request, response):
 *   This method passes the request forward to the NEXT filter in the chain.
 *   If you don't call this, the request will be BLOCKED and never reach
 *   the controller. Always call this at the end of your filter logic.
 */

import jakarta.servlet.ServletException;
/*
 * ServletException — Checked Exception from: Jakarta Servlet API
 * ─────────────────────────────────────────────────────────────────────────────
 * A general exception that can be thrown by any servlet or filter when it
 * encounters an error while processing a request.
 *
 * It is declared in the method signature (throws ServletException) to inform
 * the caller that this kind of exception might occur.
 */

import jakarta.servlet.http.HttpServletRequest;
/*
 * HttpServletRequest — Interface from: Jakarta Servlet API
 * ─────────────────────────────────────────────────────────────────────────────
 * Represents the incoming HTTP REQUEST from the client (browser/Postman/app).
 *
 * Using this interface, you can read:
 *   - Request headers (e.g., Authorization header containing the JWT token)
 *   - Request parameters (?key=value in the URL)
 *   - Request body (POST data)
 *   - HTTP method (GET, POST, PUT, DELETE)
 *   - Request URL and URI
 *   - Client IP address, session info, cookies, etc.
 *
 * In this filter, it is used to read the "Authorization" header.
 */

import jakarta.servlet.http.HttpServletResponse;
/*
 * HttpServletResponse — Interface from: Jakarta Servlet API
 * ─────────────────────────────────────────────────────────────────────────────
 * Represents the outgoing HTTP RESPONSE that will be sent back to the client.
 *
 * Using this interface, you can:
 *   - Set response status codes (200 OK, 401 Unauthorized, 403 Forbidden, etc.)
 *   - Set response headers
 *   - Write response body
 *
 * In this filter, it is passed through the filter chain without modification,
 * but it is required as a parameter for the doFilter method.
 */

import org.springframework.beans.factory.annotation.Autowired;
/*
 * @Autowired Annotation — from dependency: spring-context (spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * @Autowired tells Spring to automatically inject (provide) the required
 * dependency (object) into this class — you don't need to create it manually
 * using 'new'.
 *
 * Spring looks up its IoC container for a matching bean and injects it.
 *
 * Example without @Autowired (manual, old way):
 *   JwtAuthService jwtAuthService = new JwtAuthService(); // You create it
 *
 * Example with @Autowired (Spring way):
 *   @Autowired
 *   JwtAuthService jwtAuthService; // Spring creates and injects it for you
 *
 * This promotes loose coupling — your class doesn't need to know HOW to
 * create its dependencies, it just declares what it needs.
 */

import org.springframework.context.ApplicationContext;
/*
 * ApplicationContext — Interface from: spring-context (spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * ApplicationContext is the CENTRAL interface of Spring's IoC Container.
 * It is essentially the "container" that holds and manages all Spring Beans.
 *
 * What can you do with ApplicationContext?
 *   - Get any Spring Bean by its type or name: context.getBean(MyClass.class)
 *   - Access application configuration/properties
 *   - Publish and listen to application events
 *
 * Why is it used here instead of direct @Autowired for CustomUserDetailsService?
 *   To avoid a circular dependency issue that can occur when JwtAuthFilter,
 *   UserDetailsService, and SecurityConfig all depend on each other at startup.
 *   Using ApplicationContext.getBean() fetches the bean lazily (only when needed)
 *   rather than at startup, which avoids the circular dependency problem.
 */

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
/*
 * UsernamePasswordAuthenticationToken — Class from: spring-security-core
 * ─────────────────────────────────────────────────────────────────────────────
 * This class represents an AUTHENTICATION OBJECT in Spring Security.
 * It holds the authenticated user's information:
 *   - principal   → the UserDetails object (who the user is)
 *   - credentials → the password (set to null after authentication for security)
 *   - authorities → the roles/permissions of the user (e.g., ROLE_USER, ROLE_ADMIN)
 *
 * Once created and set in the SecurityContext, Spring Security treats the
 * current request as AUTHENTICATED — no further login is required for this request.
 *
 * Constructor used here:
 *   new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
 *     - 1st param: principal (UserDetails — who is logged in)
 *     - 2nd param: credentials (null — we don't store password after authentication)
 *     - 3rd param: authorities (user's roles/permissions)
 */

import org.springframework.security.core.context.SecurityContextHolder;
/*
 * SecurityContextHolder — Class from: spring-security-core
 * ─────────────────────────────────────────────────────────────────────────────
 * SecurityContextHolder is the place where Spring Security stores information
 * about the CURRENTLY AUTHENTICATED USER for the current request/thread.
 *
 * Think of it as a "security clipboard" that holds the logged-in user's details
 * for the duration of the current HTTP request.
 *
 * Important methods:
 *   SecurityContextHolder.getContext()               → get the current security context
 *   .getAuthentication()                             → get the current authenticated user
 *   .setAuthentication(authenticationToken)          → set the authenticated user
 *
 * How it works:
 *   - It uses ThreadLocal internally, meaning each thread (each request) has
 *     its own isolated security context.
 *   - After the request is done, the context is cleared automatically.
 */

import org.springframework.security.core.userdetails.UserDetails;
/*
 * UserDetails — Interface from: spring-security-core
 * ─────────────────────────────────────────────────────────────────────────────
 * UserDetails is a core Spring Security interface that represents a USER
 * in the security system.
 *
 * It provides the following information about the user:
 *   - getUsername()      → the username (used for login)
 *   - getPassword()      → the encoded password
 *   - getAuthorities()   → the list of roles/permissions (e.g., ROLE_USER)
 *   - isAccountNonExpired()   → is the account still valid?
 *   - isAccountNonLocked()    → is the account unlocked?
 *   - isCredentialsNonExpired() → are credentials still valid?
 *   - isEnabled()             → is the account active?
 *
 * Your CustomUserDetailsService returns an object that implements this interface
 * after loading user data from the database.
 */

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
/*
 * WebAuthenticationDetailsSource — Class from: spring-security-web
 * ─────────────────────────────────────────────────────────────────────────────
 * This class builds additional details about the HTTP request and attaches
 * them to the Authentication object.
 *
 * What details does it add?
 *   - Remote IP address of the client making the request
 *   - Session ID (if a session exists)
 *
 * Why is this useful?
 *   These details can be used for:
 *   - Logging and auditing (who logged in from which IP)
 *   - Security checks (e.g., restrict login from certain IPs)
 *
 * Usage:
 *   new WebAuthenticationDetailsSource().buildDetails(request)
 *   → Creates a WebAuthenticationDetails object from the current request
 *     and attaches it to the authentication token.
 */

import org.springframework.stereotype.Component;
/*
 * @Component Annotation — from dependency: spring-context (spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * @Component is a general-purpose Spring annotation that marks this class
 * as a "Spring-managed component" (a Spring Bean).
 *
 * When Spring Boot starts up, it scans all packages for classes annotated
 * with @Component (and its specializations like @Service, @Repository,
 * @Controller) and automatically registers them in the IoC container.
 *
 * By marking JwtAuthFilter as @Component:
 *   - Spring creates an instance of this filter automatically.
 *   - Spring Security picks it up and adds it to the filter chain.
 *   - It can receive @Autowired dependencies like any other Spring Bean.
 */

import org.springframework.web.filter.OncePerRequestFilter;
/*
 * OncePerRequestFilter — Abstract Class from: spring-web (spring-boot-starter-web)
 * ─────────────────────────────────────────────────────────────────────────────
 * OncePerRequestFilter is a base class provided by Spring that guarantees
 * your filter logic runs EXACTLY ONCE per HTTP request.
 *
 * Why is this needed?
 *   In some cases (e.g., request forwarding, error dispatching), a filter
 *   might get called multiple times for a single request. OncePerRequestFilter
 *   prevents that by internally tracking whether the filter has already run
 *   for the current request.
 *
 * How to use it?
 *   Extend this class and override the doFilterInternal() method.
 *   Your JWT validation logic goes inside doFilterInternal().
 *
 * This is the RECOMMENDED base class for writing custom filters in Spring Boot.
 */

import java.io.IOException;
/*
 * IOException — Checked Exception from: java.io (Java Standard Library)
 * ─────────────────────────────────────────────────────────────────────────────
 * IOException is thrown when an input/output operation fails — for example,
 * reading from or writing to a request/response stream.
 *
 * It is declared in the method signature (throws IOException) because the
 * filterChain.doFilter() method can throw it internally.
 */


/*
 * ═══════════════════════════════════════════════════════════════════════════
 * CLASS: JwtAuthFilter
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose:
 *   JwtAuthFilter is a custom Spring Security filter that intercepts EVERY
 *   incoming HTTP request and checks whether it contains a valid JWT token
 *   in the Authorization header.
 *
 * What is JWT (JSON Web Token)?
 *   JWT is a compact, URL-safe token that contains:
 *     - Header    : algorithm used for signing (e.g., HS256)
 *     - Payload   : claims/data (e.g., username, roles, expiry time)
 *     - Signature : ensures the token hasn't been tampered with
 *
 *   A typical JWT looks like:  xxxxx.yyyyy.zzzzz
 *
 * How is JWT sent in a request?
 *   The client sends the JWT in the HTTP request header like this:
 *     Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSJ9.abc123
 *
 * What this filter does (step by step):
 *   Step 1: Read the "Authorization" header from the incoming request.
 *   Step 2: Check if the header starts with "Bearer " — extract the token.
 *   Step 3: Extract the username from the JWT token.
 *   Step 4: Check if username exists and user is NOT already authenticated.
 *   Step 5: Load user details from the database using the username.
 *   Step 6: Validate the token (check signature + expiry).
 *   Step 7: If valid, create an Authentication object and set it in
 *           SecurityContextHolder → user is now authenticated for this request.
 *   Step 8: Pass the request forward to the next filter in the chain.
 *
 * Extends:
 *   OncePerRequestFilter — ensures this filter runs exactly once per request.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
/*
 * @Component:
 *   Marks this class as a Spring Bean so Spring Boot automatically detects,
 *   instantiates, and manages it. Also allows Spring Security to pick it up
 *   and include it in the security filter chain.
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    /*
     * extends OncePerRequestFilter:
     *   By extending this abstract class, JwtAuthFilter becomes a servlet
     *   filter that is guaranteed to execute only ONCE per HTTP request.
     *   We must override the doFilterInternal() method with our JWT logic.
     */

    @Autowired
    JwtAuthService jwtAuthService;
    /*
     * @Autowired JwtAuthService:
     * ─────────────────────────────────────────────────────────────────────
     * Spring automatically injects your JwtAuthService bean here.
     *
     * JwtAuthService is used in this filter for:
     *   1. extractUserNameFromToken(token) → Read the username from JWT payload
     *   2. validateToken(token, userDetails) → Verify the token is valid & not expired
     *
     * This avoids manually creating: new JwtAuthService()
     */

    @Autowired
    ApplicationContext context;
    /*
     * @Autowired ApplicationContext:
     * ─────────────────────────────────────────────────────────────────────
     * Spring injects the ApplicationContext (the IoC container) here.
     *
     * Why use ApplicationContext instead of directly @Autowiring
     * CustomUserDetailsService?
     *
     *   Direct @Autowiring of CustomUserDetailsService here can cause a
     *   CIRCULAR DEPENDENCY at startup:
     *     JwtAuthFilter → needs CustomUserDetailsService
     *     CustomUserDetailsService → needs PasswordEncoder
     *     SecurityConfig → needs JwtAuthFilter + UserDetailsService
     *   This circular chain can prevent the app from starting.
     *
     * Solution:
     *   Inject ApplicationContext instead, and use context.getBean() to
     *   fetch CustomUserDetailsService LAZILY (only when a request comes in),
     *   NOT at startup. This breaks the circular dependency.
     */

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: doFilterInternal
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   This is the CORE method of the filter — overridden from
     *   OncePerRequestFilter. It contains the actual JWT validation logic
     *   that runs for every incoming HTTP request.
     *
     * Parameters:
     *   HttpServletRequest request    → The incoming HTTP request (read headers)
     *   HttpServletResponse response  → The outgoing HTTP response
     *   FilterChain filterChain       → The chain of remaining filters to execute
     *
     * throws ServletException, IOException:
     *   Declared because filterChain.doFilter() can throw these exceptions.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Override
    /*
     * @Override:
     *   Indicates that this method overrides the abstract doFilterInternal()
     *   method defined in the parent class OncePerRequestFilter.
     *   The @Override annotation helps the compiler catch typos in method names.
     */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        /*
         * request.getHeader("Authorization"):
         * ─────────────────────────────────────────────────────────────────
         * Reads the value of the "Authorization" HTTP request header.
         *
         * What is the Authorization header?
         *   It is a standard HTTP header used to send authentication credentials.
         *   For JWT-based authentication, the client sends the token like this:
         *     Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.xyz
         *
         * If the header is missing, getHeader() returns null.
         *
         * authHeader will contain the full string: "Bearer <token>"
         * or null if the Authorization header was not sent.
         */

        String token = null;
        String userName = null;
        /*
         * Declaring token and userName as null initially.
         * These will be populated in the next if-block if a valid
         * Authorization header is present.
         */

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            /*
             * Checking two conditions:
             *
             * 1. authHeader != null
             *    → Ensures the Authorization header was actually sent with the request.
             *    → If null, there's no token to process — skip the block.
             *
             * 2. authHeader.startsWith("Bearer ")
             *    → The JWT token in Authorization headers always starts with "Bearer "
             *      (note the space after Bearer — it's part of the standard format).
             *    → This confirms the authentication scheme is Bearer token (JWT),
             *      not Basic Auth or any other scheme.
             *    → startsWith() is a standard Java String method.
             */

            token = authHeader.substring(7);
            /*
             * authHeader.substring(7):
             * ─────────────────────────────────────────────────────────────
             * Extracts only the JWT token part from the Authorization header
             * by removing the "Bearer " prefix (which is exactly 7 characters).
             *
             * Example:
             *   authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.abc.xyz"
             *   token      = "eyJhbGciOiJIUzI1NiJ9.abc.xyz"
             *
             * substring(7) is a standard Java String method that returns
             * the part of the string starting from index 7 (0-based).
             */

            userName = jwtAuthService.extractUserNameFromToken(token);
            /*
             * jwtAuthService.extractUserNameFromToken(token):
             * ─────────────────────────────────────────────────────────────
             * Calls your custom JwtAuthService to parse the JWT token and
             * extract the "subject" claim — which is the username stored inside
             * the token's payload when the token was originally created.
             *
             * JWT Payload example (decoded):
             *   {
             *     "sub": "john@example.com",   ← this is the username
             *     "iat": 1700000000,            ← issued at (timestamp)
             *     "exp": 1700086400             ← expiry time (timestamp)
             *   }
             *
             * After this line, userName will contain "john@example.com" (or
             * whatever username was encoded in the token).
             */
        }

        if(userName != null && SecurityContextHolder.getContext().getAuthentication()==null){
            /*
             * Two conditions checked here:
             *
             * 1. userName != null
             *    → We successfully extracted a username from the token.
             *    → If userName is null (token was invalid/missing), skip authentication.
             *
             * 2. SecurityContextHolder.getContext().getAuthentication() == null
             *    → Checks if the user is NOT already authenticated for this request.
             *    → getAuthentication() returns null if no authentication is set yet.
             *    → This prevents redundant re-authentication if it was already done
             *      by an earlier filter or mechanism in the chain.
             *    → This is an important optimization and safety check.
             */

            UserDetails userDetails = context.getBean(CustomUserDetailsService.class).loadUserByUsername(userName);
            /*
             * context.getBean(CustomUserDetailsService.class):
             * ─────────────────────────────────────────────────────────────
             * Fetches the CustomUserDetailsService bean from the Spring IoC
             * container at this point (lazily, during the request — not at startup).
             * This avoids circular dependency issues during app initialization.
             *
             * .loadUserByUsername(userName):
             * ─────────────────────────────────────────────────────────────
             * Calls your CustomUserDetailsService to load the full user details
             * from the DATABASE using the username extracted from the JWT token.
             *
             * Returns a UserDetails object containing:
             *   - Username, encoded password
             *   - Roles/authorities (e.g., ROLE_USER, ROLE_ADMIN)
             *   - Account status flags (enabled, locked, expired, etc.)
             *
             * Why load from DB even if token is valid?
             *   - To check if the user still exists and is active in the system.
             *   - The token might be valid but the user could have been deleted
             *     or disabled after the token was issued.
             */

            if(jwtAuthService.validateToken(token, userDetails)){
                /*
                 * jwtAuthService.validateToken(token, userDetails):
                 * ─────────────────────────────────────────────────────────
                 * Calls your JwtAuthService to validate the JWT token by:
                 *   1. Checking the token's username matches the loaded UserDetails username.
                 *   2. Checking the token has NOT expired (compares expiry time with now).
                 *   3. Verifying the token's signature is intact (not tampered with).
                 *
                 * Returns true  → token is valid, proceed with authentication.
                 * Returns false → token is invalid/expired, skip authentication.
                 */

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                /*
                 * new UsernamePasswordAuthenticationToken(principal, credentials, authorities):
                 * ─────────────────────────────────────────────────────────────────────────────
                 * Creates an Authentication object that represents the successfully
                 * authenticated user. This is the object Spring Security uses to know
                 * "who is currently logged in" for this request.
                 *
                 * Parameters:
                 *   1. userDetails               → principal: the logged-in user's details
                 *   2. null                      → credentials: password is set to null
                 *                                  (we don't need/store it after auth)
                 *   3. userDetails.getAuthorities() → authorities: the user's roles/permissions
                 *                                     (e.g., [ROLE_USER, ROLE_ADMIN])
                 *
                 * Note: Using the 3-argument constructor marks this token as
                 * "authenticated = true" internally — meaning Spring Security
                 * knows this is a fully authenticated token, not just a
                 * pre-authentication attempt.
                 */

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                /*
                 * new WebAuthenticationDetailsSource().buildDetails(request):
                 * ─────────────────────────────────────────────────────────────
                 * Creates a WebAuthenticationDetails object from the current
                 * HTTP request and attaches it to the authentication token.
                 *
                 * WebAuthenticationDetails contains:
                 *   - Remote IP address of the client
                 *   - Session ID (if session exists)
                 *
                 * Why attach these details?
                 *   - Useful for audit logging (track which IP authenticated)
                 *   - Can be used for IP-based security restrictions
                 *   - Spring Security events and listeners can use this info
                 */

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                /*
                 * SecurityContextHolder.getContext().setAuthentication(authenticationToken):
                 * ─────────────────────────────────────────────────────────────────────────
                 * This is the FINAL and MOST IMPORTANT step of authentication.
                 *
                 * What happens here:
                 *   - The authenticated user (authenticationToken) is stored in the
                 *     SecurityContext for the current thread/request.
                 *   - From this point onwards, Spring Security considers the user
                 *     as AUTHENTICATED for this request.
                 *   - Any downstream code (controllers, services) can retrieve the
                 *     authenticated user using:
                 *       SecurityContextHolder.getContext().getAuthentication()
                 *
                 * ThreadLocal behavior:
                 *   - SecurityContextHolder uses ThreadLocal storage internally.
                 *   - This means each HTTP request thread has its own isolated
                 *     security context — no data leaks between requests.
                 *   - The context is automatically cleared after the request completes.
                 */
            }
        }

        filterChain.doFilter(request, response);
        /*
         * filterChain.doFilter(request, response):
         * ─────────────────────────────────────────────────────────────────────
         * CRITICAL: This line passes the request and response to the NEXT
         * filter in the Spring Security filter chain.
         *
         * Why is this important?
         *   - Every filter MUST call doFilter() to pass the request forward.
         *   - If you skip this call, the request is BLOCKED — it will never
         *     reach your Controller, and the client gets no response.
         *
         * This is always the LAST line in the filter — after all your JWT
         * validation logic is complete, regardless of whether authentication
         * succeeded or failed. The next filter (or the servlet/controller)
         * will handle the request from here.
         *
         * If authentication failed (invalid token), the SecurityContext has no
         * Authentication set, so Spring Security will reject the request with
         * 401 Unauthorized before it reaches the controller.
         */
    }
}