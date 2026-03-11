package com.ratnakar.oauth.config;

/*
 * ═══════════════════════════════════════════════════════════════════════════
 * IMPORTS SECTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * All the imports below come from TWO Spring Boot dependencies in pom.xml:
 *
 * 1) Spring Security (handles authentication & authorization):
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-security</artifactId>
 *    </dependency>
 *
 * 2) OAuth2 Client (handles OAuth2 / Social Login like Google, GitHub etc.):
 *    <dependency>
 *        <groupId>org.springframework.boot</groupId>
 *        <artifactId>spring-boot-starter-oauth2-client</artifactId>
 *    </dependency>
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */

import org.springframework.context.annotation.Bean;
/*
 * @Bean Annotation — from dependency: spring-context (included in spring-boot-starter)
 * ─────────────────────────────────────────────────────────────────────────────
 * @Bean tells Spring Framework:
 *   "The object returned by this method should be managed by the Spring IoC
 *    (Inversion of Control) Container."
 *
 * What is IoC Container?
 *   It is Spring's object factory. Instead of you creating objects manually
 *   using 'new', Spring creates, manages and injects them for you automatically.
 *
 * Why is @Bean important?
 *   - Spring will call this method once at startup.
 *   - The returned object is stored in the container.
 *   - Any other class that needs this object will get it automatically (injected).
 */

import org.springframework.context.annotation.Configuration;
/*
 * @Configuration Annotation — from dependency: spring-context
 * ─────────────────────────────────────────────────────────────────────────────
 * Marks this class as a "Configuration Class" in Spring.
 *
 * What does that mean?
 *   - Spring will scan and load this class during application startup.
 *   - All @Bean methods inside this class will be registered in the IoC container.
 *   - Think of this class as a "Settings File" for your application.
 *
 * Without @Configuration, Spring won't treat this class as a source of beans.
 */

import org.springframework.security.config.Customizer;
/*
 * Customizer — from dependency: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * Customizer is a Functional Interface provided by Spring Security.
 *
 * What is a Functional Interface?
 *   An interface with exactly one abstract method. It can be used with
 *   lambda expressions.
 *
 * Customizer.withDefaults():
 *   - A static factory method that returns a Customizer which applies
 *     all the DEFAULT settings that Spring Security provides out of the box.
 *   - You don't need to manually configure every setting — withDefaults()
 *     handles the standard setup automatically.
 *
 * Example analogy:
 *   Like ordering a "standard combo meal" at a restaurant — you get
 *   all the default items without customizing each one separately.
 */

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/*
 * HttpSecurity — from dependency: spring-security-config
 * ─────────────────────────────────────────────────────────────────────────────
 * HttpSecurity is the MOST IMPORTANT class when configuring Spring Security.
 * It is a Builder class that lets you define security rules step by step.
 *
 * What can you configure with HttpSecurity?
 *   - Which URLs require login and which are publicly accessible
 *   - What type of login to use: Form Login, OAuth2, JWT, Basic Auth, etc.
 *   - CSRF (Cross-Site Request Forgery) protection settings
 *   - Session management
 *   - Logout behavior
 *   - Exception handling (e.g., redirect to login page on 401 error)
 *
 * Think of HttpSecurity as the "Security Rulebook" of your web application.
 * Every incoming HTTP request must pass through the rules defined here.
 */

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
/*
 * @EnableWebSecurity Annotation — from dependency: spring-security-web
 * ─────────────────────────────────────────────────────────────────────────────
 * This annotation ACTIVATES Spring Security for your entire web application.
 *
 * What does it do internally?
 *   - Imports all the core Spring Security infrastructure and configurations.
 *   - Tells Spring Boot: "Apply security filters to all HTTP requests."
 *   - Without this annotation, none of your security rules will take effect.
 *
 * In modern Spring Boot (3.x), Spring Security is auto-configured, but using
 * @EnableWebSecurity gives you full manual control over security settings.
 */

import org.springframework.security.web.SecurityFilterChain;
/*
 * SecurityFilterChain — Interface from dependency: spring-security-web
 * ─────────────────────────────────────────────────────────────────────────────
 * Spring Security works as a "Chain of Filters".
 *
 * What is a Filter Chain?
 *   Every incoming HTTP request passes through a series of filters (checkpoints)
 *   one by one before it reaches your Controller/API.
 *
 *   Request → [Filter 1] → [Filter 2] → [Filter 3] → ... → Your Controller
 *
 * Examples of filters in the chain:
 *   - UsernamePasswordAuthenticationFilter (checks login credentials)
 *   - OAuth2LoginAuthenticationFilter      (handles OAuth2 login flow)
 *   - BasicAuthenticationFilter            (handles Basic Auth headers)
 *
 * SecurityFilterChain is the INTERFACE that represents this entire chain.
 * When you define a @Bean of this type, Spring Security uses it to know
 * which filters to apply and in what order.
 *
 * Note: This approach replaced the old WebSecurityConfigurerAdapter
 * (which was deprecated in Spring Security 5.7+).
 */


/*
 * ═══════════════════════════════════════════════════════════════════════════
 * CLASS: OAuthSecurityConfig
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose:
 *   This class is the central security configuration for the application.
 *   It defines the security rules that govern how users authenticate and
 *   what they are allowed to access.
 *
 * What this config does:
 *   1. Requires ALL users to be authenticated before accessing any URL.
 *   2. Uses OAuth2 Login as the authentication method, which enables
 *      "Login with Google / GitHub / Facebook" style of authentication.
 *
 * How OAuth2 Login works (simplified flow):
 *   Step 1: User tries to access any page.
 *   Step 2: Spring Security sees the user is NOT logged in.
 *   Step 3: User is redirected to /login page.
 *   Step 4: User clicks "Login with Google" (or other provider).
 *   Step 5: User is redirected to Google's login page.
 *   Step 6: User logs in on Google.
 *   Step 7: Google sends an authorization code back to your app.
 *   Step 8: Spring Security exchanges the code for an access token.
 *   Step 9: User is now authenticated and redirected to the original page.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */

@Configuration
/*
 * @Configuration:
 *   Registers this class as a Spring Configuration class.
 *   Spring Boot will automatically detect and load it during startup
 *   via @ComponentScan (which is part of @SpringBootApplication).
 */

@EnableWebSecurity
/*
 * @EnableWebSecurity:
 *   Activates Spring Security's web security support for this application.
 *   Required to apply custom security rules defined in this class.
 *   Without this, the security configuration below would be IGNORED.
 */

public class OAuthSecurityConfig {

    /*
     * ═══════════════════════════════════════════════════════════════════════
     * METHOD: defaultSecurityFilterChain
     * ═══════════════════════════════════════════════════════════════════════
     *
     * Purpose:
     *   Defines and returns the SecurityFilterChain bean that Spring Security
     *   will use to apply security rules to every incoming HTTP request.
     *
     * Parameter:
     *   HttpSecurity httpSecurity — Spring automatically injects this object.
     *   It is the builder we use to configure all security rules step by step.
     *
     * Return type:
     *   SecurityFilterChain — the fully configured chain of security filters.
     *
     * throws Exception:
     *   HttpSecurity builder methods can throw checked exceptions internally,
     *   so we declare 'throws Exception' to avoid wrapping each line in
     *   try-catch blocks. Spring handles any exception at startup gracefully.
     *
     * ═══════════════════════════════════════════════════════════════════════
     */
    @Bean
    /*
     * @Bean:
     *   Tells Spring to manage the SecurityFilterChain object returned by
     *   this method. Spring Security automatically detects this bean and
     *   uses it as the security configuration for the entire application.
     */
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                /*
                 * .authorizeHttpRequests(...):
                 * ───────────────────────────────────────────────────────────────────
                 * This method sets up AUTHORIZATION rules — i.e., who can access
                 * which URLs in your application.
                 *
                 * It accepts a lambda expression:
                 *   auth -> auth.anyRequest().authenticated()
                 *
                 *   • 'auth' is an AuthorizationManagerRequestMatcherRegistry object.
                 *     (Spring injects it automatically inside the lambda.)
                 *     It is used to map URL patterns to their access rules.
                 *
                 *   • anyRequest()
                 *     → Matches ALL incoming HTTP requests regardless of the URL.
                 *     → This is a catch-all rule.
                 *
                 *   • authenticated()
                 *     → Only allows access if the user is already logged in.
                 *     → If the user is NOT logged in, Spring Security will
                 *       automatically redirect them to the login page.
                 *
                 * In simple words:
                 *   "Every single page and API endpoint in this application
                 *    requires the user to be logged in. No anonymous access allowed."
                 *
                 * Other options you could use instead of authenticated():
                 *   • permitAll()        → Allow everyone, no login required
                 *   • hasRole("ADMIN")   → Only users with ADMIN role can access
                 *   • denyAll()          → Block everyone from accessing
                 */

                .oauth2Login(Customizer.withDefaults());
        /*
         * .oauth2Login(...):
         * ───────────────────────────────────────────────────────────────────
         * Enables OAuth2-based login for the application.
         *
         * What is OAuth2 Login?
         *   OAuth2 is an industry-standard authorization protocol that allows
         *   users to log in to your app using their existing accounts on
         *   third-party providers like:
         *     → Google
         *     → GitHub
         *     → Facebook
         *     → Okta
         *     → Any OAuth2-compatible provider
         *
         * Customizer.withDefaults():
         *   Applies all default Spring Security settings for OAuth2 login:
         *     • Default login page is auto-generated at: /login
         *     • Handles redirect to provider (e.g., Google's login page)
         *     • Handles the callback/redirect URI after successful login
         *     • Manages token exchange with the OAuth2 provider
         *     • Creates the authenticated session for the user
         *
         * For this to work, you MUST add client credentials in
         * application.properties or application.yml. Example for Google:
         *
         *   spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
         *   spring.security.oauth2.client.registration.google.client-secret=YOUR_SECRET
         *   spring.security.oauth2.client.registration.google.scope=email,profile
         *
         * These credentials are obtained from Google Cloud Console (or the
         * respective provider's developer portal).
         */

        return httpSecurity.build();
        /*
         * httpSecurity.build():
         * ───────────────────────────────────────────────────────────────────
         * After configuring all security rules using the HttpSecurity builder,
         * .build() finalizes the configuration and creates the actual
         * SecurityFilterChain object.
         *
         * This SecurityFilterChain is then:
         *   1. Returned from this method.
         *   2. Registered as a Spring Bean (because of @Bean annotation).
         *   3. Automatically picked up by Spring Security.
         *   4. Applied to ALL incoming HTTP requests in the application.
         *
         * Think of it like: you described all your security rules using
         * HttpSecurity (the builder), and .build() officially locks them
         * in and starts enforcing them on every request.
         */
    }
}