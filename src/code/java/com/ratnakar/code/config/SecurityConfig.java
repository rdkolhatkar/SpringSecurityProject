package com.ratnakar.code.config;


import com.ratnakar.code.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration:
 *
 *  PUBLIC  (no credentials required):
 *    POST /api/auth/register   — user registration
 *    GET  /api/products/**     — anyone can browse products
 *
 *  PROTECTED (Basic Auth required — username + BCrypt-verified password):
 *    POST   /api/products      — add product
 *    PUT    /api/products/{id} — update product
 *    DELETE /api/products/{id} — delete product
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    // ─────────────────────────────────────────────
    //  BCryptPasswordEncoder bean
    // ─────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────
    //  DaoAuthenticationProvider wires our
    //  UserDetailsService + BCrypt encoder together
    // ─────────────────────────────────────────────
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─────────────────────────────────────────────
    //  AuthenticationManager (needed by some tests)
    // ─────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─────────────────────────────────────────────
    //  HTTP Security – stateless Basic Auth
    // ─────────────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF – stateless REST API, no session cookies
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless — no HTTP session stored server-side
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // ── PUBLIC endpoints ──────────────────────────────
                .requestMatchers(HttpMethod.POST,   "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST,   "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/products").permitAll()

                // ── PROTECTED endpoints (valid Basic Auth required) ──
                .requestMatchers(HttpMethod.POST,   "/api/products").authenticated()
                .requestMatchers(HttpMethod.PUT,    "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                // Everything else also requires auth
                .anyRequest().authenticated()
            )

            // Enable HTTP Basic Authentication
            .httpBasic(Customizer.withDefaults())

            // Wire our custom UserDetailsService
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
