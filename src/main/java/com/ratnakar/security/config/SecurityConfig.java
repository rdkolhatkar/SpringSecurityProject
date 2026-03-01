package com.ratnakar.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsAuthenticationService;

    // Writing Security Filter Chain Configuration Code using lambada expressions
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(customizer -> customizer.disable());
        httpSecurity.authorizeHttpRequests(request -> request.anyRequest().authenticated());
        // httpSecurity.formLogin(Customizer.withDefaults());
        // As we have disabled the "formLogin" and we are using the "httpBasic", When hitting the endpoint on browser we will not get the login form instead we will get the login popup where we have to pass our username and password
        httpSecurity.httpBasic(Customizer.withDefaults());
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // SessionCreationPolicy.STATELESS : It tells Spring Security not to create or use HTTP sessions. Every request must contain authentication credentials because the server does not store login state. It is mainly used in REST APIs with JWT authentication.
        // For STATELESS Session Creation Policy, You don't have to provide the CSRF Token with your POST, PUT and DELETE Api requests.
        // When setting the SessionCreationPolicy as STATELESS we have to make sure that httpSecurity.formLogin is disabled.
        return httpSecurity.build();
    }
    // Writing Security Filter Chain Configuration Code in an Imperative Style Programming ( Without lambada expressions )
//    @Bean
    public SecurityFilterChain imperativeSecurityFilterChain(HttpSecurity http) throws Exception {

        // ---- Disable CSRF ----
        Customizer<CsrfConfigurer<HttpSecurity>> customizerCsrf =
                new Customizer<CsrfConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(CsrfConfigurer<HttpSecurity> csrfConfigurer) {
                        csrfConfigurer.disable();
                    }
                };
        http.csrf(customizerCsrf);


        // ---- Authorize All Requests ----
        Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> customizerHttp =
                new Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
                    @Override
                    public void customize(
                            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                        registry.anyRequest().authenticated();
                    }
                };
        http.authorizeHttpRequests(customizerHttp);


        // ---- Enable HTTP Basic ----
        Customizer<HttpBasicConfigurer<HttpSecurity>> customizerHttpBasic =
                new Customizer<HttpBasicConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(HttpBasicConfigurer<HttpSecurity> httpBasicConfigurer) {
                        // default configuration (same as Customizer.withDefaults())
                    }
                };
        http.httpBasic(customizerHttpBasic);


        // ---- Session Management - STATELESS ----
        Customizer<SessionManagementConfigurer<HttpSecurity>> customizerSession =
                new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
                    @Override
                    public void customize(SessionManagementConfigurer<HttpSecurity> sessionConfigurer) {
                        sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                    }
                };
        http.sessionManagement(customizerSession);


    /*
     SessionCreationPolicy.STATELESS:
     - Spring Security will NOT create or use HTTP sessions.
     - Every request must contain authentication credentials.
     - Mainly used in REST APIs (JWT / Token based authentication).
     - CSRF token is NOT required for POST, PUT, DELETE.
     - formLogin() must be disabled when using STATELESS.
    */

        return http.build();
    }

    // There is one more way of doing this, as http follows a builder pattern we can directly do the chaining.
//    @Bean
    public SecurityFilterChain chainingSecurityFilterChain(HttpSecurity httpSecurityBuilder) throws Exception {
        return httpSecurityBuilder.csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(request -> request.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    // By default Spring Security uses an interface called as 'UserDetailsService'.
    // In Spring Boot Security, UserDetailsService is used to load user-specific data (username, password, roles) from a data source during authentication.
    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails user = User
                .withDefaultPasswordEncoder()
                .username("User")
                .password("User@123")
                .roles("USER")
                .build();
        UserDetails admin = User
                .withDefaultPasswordEncoder()
                .username("Admin")
                .password("Admin@123")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    /*
        AuthenticationProvider is a core interface in Spring Security responsible for validating authentication requests.
        It contains authenticate() method where we implement custom credential validation logic and supports() method to define which authentication type it handles.
        It is invoked by AuthenticationManager during login processing.
    */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        //daoAuthenticationProvider.setUserDetailsPasswordService(userDetailsAuthenticationService);
        daoAuthenticationProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        return daoAuthenticationProvider;
    }

}
