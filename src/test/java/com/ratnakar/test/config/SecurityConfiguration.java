package com.ratnakar.test.config;

import org.springframework.beans.factory.annotation.Autowired;
// Used to inject required beans automatically.

import org.springframework.context.annotation.Bean;
// Marks a method as a Spring bean creator.

import org.springframework.context.annotation.Configuration;
// Marks this class as Spring configuration class.

import org.springframework.security.authentication.AuthenticationProvider;
// Core interface in Spring Security responsible for validating credentials.

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// Implementation of AuthenticationProvider.
// Used when authentication is done using database (DAO layer).

import org.springframework.security.config.Customizer;
// Used for lambda-based configuration customization.

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Main class used to configure web security settings.

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Enables Spring Security for this application.

import org.springframework.security.config.http.SessionCreationPolicy;
// Enum used to define session management strategy.

import org.springframework.security.core.userdetails.UserDetailsService;
// Interface used to load user details (username, password, roles) from DB.

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Password encoder using BCrypt hashing algorithm.

import org.springframework.security.web.SecurityFilterChain;
// Very important class.
// Represents the security filter chain that intercepts HTTP requests.

/*
 =============================================================================
 CLASS LEVEL EXPLANATION
 =============================================================================

 @Configuration:
 ----------------
 Tells Spring:
 "This class contains bean definitions."

 @EnableWebSecurity:
 -------------------
 Enables Spring Security support.
 Registers the SecurityFilterChain in application.

 Without this:
 Security will not work.
*/
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    /*
     =========================================================================
     Inject Custom UserDetailsService
     =========================================================================

     This is your DB-based authentication service.
     (UserAuthenticationService)

     Spring Security will use this to load user from database.
    */
    @Autowired
    private UserDetailsService userDetailsAuthenticationService;

    /*
     =========================================================================
     SECURITY FILTER CHAIN
     =========================================================================

     WHAT IS SecurityFilterChain?
     -----------------------------
     It is a chain of security filters that intercept every HTTP request.

     Example filters:
     - Authentication filter
     - Authorization filter
     - CSRF filter
     - Session filter

     Every incoming request goes through this chain.

     This replaces old WebSecurityConfigurerAdapter (deprecated).
    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        /*
         ----------------------------------------------------------------------
         1️⃣ Disable CSRF
         ----------------------------------------------------------------------

         CSRF = Cross Site Request Forgery

         Normally required for session-based apps.

         Since we are using:
         SessionCreationPolicy.STATELESS

         CSRF token is not required.
        */
        httpSecurity.csrf(customizer -> customizer.disable());

        /*
         ----------------------------------------------------------------------
         2️⃣ Authorization Rules
         ----------------------------------------------------------------------

         anyRequest().authenticated()

         Means:
         Every API requires login.
        */
        httpSecurity.authorizeHttpRequests(
                request -> request.anyRequest().authenticated()
        );

        /*
         ----------------------------------------------------------------------
         3️⃣ Enable HTTP Basic Authentication
         ----------------------------------------------------------------------

         When accessing API in browser:
         A popup appears asking for username and password.

         It sends credentials in Authorization header.

         Alternative:
         formLogin() → Shows login page.
        */
        httpSecurity.httpBasic(Customizer.withDefaults());

        /*
         ----------------------------------------------------------------------
         4️⃣ Session Management
         ----------------------------------------------------------------------

         STATELESS means:
         - No HTTP session will be created.
         - Server will NOT store login state.
         - Every request must contain credentials.

         Used mainly for REST APIs.
        */
        httpSecurity.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        /*
         Finally build and return the SecurityFilterChain.
        */
        return httpSecurity.build();
    }

    /*
     =========================================================================
     AUTHENTICATION PROVIDER
     =========================================================================

     WHAT IS AuthenticationProvider?
     --------------------------------
     It is responsible for validating username & password.

     Authentication Flow:

     1. User sends username & password
     2. AuthenticationManager calls AuthenticationProvider
     3. AuthenticationProvider:
        - Calls UserDetailsService
        - Compares password using PasswordEncoder
        - If correct → authentication success
    */
    @Bean
    public AuthenticationProvider authenticationProvider(){

        DaoAuthenticationProvider daoAuthenticationProvider =
                new DaoAuthenticationProvider();

        /*
         Set custom DB-based UserDetailsService.

         Without this:
         Spring might use in-memory authentication.
        */
        daoAuthenticationProvider
                .setUserDetailsService(userDetailsAuthenticationService);

        /*
         Set BCrypt password encoder.

         During login:
         - Raw password from request
         - Encoded password from DB

         BCrypt matches them securely.
        */
        daoAuthenticationProvider
                .setPasswordEncoder(new BCryptPasswordEncoder(12));

        return daoAuthenticationProvider;
    }

    /*
     =========================================================================
     PASSWORD ENCODER BEAN
     =========================================================================

     WHY BCrypt?
     ------------
     BCrypt is secure hashing algorithm.

     It:
     - Adds salt
     - Is slow (good for security)
     - Prevents brute force attacks

     12 = strength factor
     Higher value = more secure but slower.
    */
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }
}
