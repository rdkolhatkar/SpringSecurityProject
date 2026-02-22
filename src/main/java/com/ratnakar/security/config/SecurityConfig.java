package com.ratnakar.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(customizer -> customizer.disable());
        httpSecurity.authorizeHttpRequests(request -> request.anyRequest().authenticated());
        // httpSecurity.formLogin(Customizer.withDefaults());
        httpSecurity.httpBasic(Customizer.withDefaults());
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // SessionCreationPolicy.STATELESS : It tells Spring Security not to create or use HTTP sessions. Every request must contain authentication credentials because the server does not store login state. It is mainly used in REST APIs with JWT authentication.
        // For STATELESS Session Creation Policy, You don't have to provide the CSRF Token with your POST, PUT and DELETE Api requests.
        // When setting the SessionCreationPolicy as STATELESS we have to make sure that httpSecurity.formLogin is disabled.
        return httpSecurity.build();
    }
}
