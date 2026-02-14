# 🔐 Spring Security – Complete End-to-End Guide (Beginner to Advanced)

> A complete step-by-step guide to understanding and implementing **Spring Security** in a Spring Boot application.
> This README covers everything from basics to advanced concepts in a structured and easy-to-understand manner.

---

# 📌 Table of Contents

1. What is Spring Security?
2. Why Do We Need Spring Security?
3. Core Security Concepts
4. How Spring Security Works Internally
5. Creating a Spring Boot Project
6. Adding Spring Security Dependency
7. Default Security Behavior
8. Custom Security Configuration
9. Authentication (In-Memory, JDBC, JPA)
10. Password Encoding
11. Authorization (Roles & Authorities)
12. Method-Level Security
13. Form-Based Authentication
14. HTTP Basic Authentication
15. JWT Authentication (Stateless Security)
16. CSRF Protection
17. CORS Configuration
18. Exception Handling
19. Security Best Practices
20. Interview Questions
21. Conclusion

---

# 1️⃣ What is Spring Security?

**Spring Security** is a powerful and highly customizable authentication and access-control framework for Spring applications.

It is part of the Spring ecosystem and integrates seamlessly with:

* Spring Boot
* Spring MVC
* Spring Data
* REST APIs

Official Website: [https://spring.io/projects/spring-security](https://spring.io/projects/spring-security)

---

# 2️⃣ Why Do We Need Spring Security?

In real-world applications, we must:

* Protect APIs
* Restrict user access
* Secure passwords
* Prevent hacking attacks
* Manage user sessions
* Handle roles and permissions

Spring Security provides:

* Authentication (Who are you?)
* Authorization (What can you access?)
* Protection against attacks (CSRF, Session fixation, etc.)
* Secure password storage

---

# 3️⃣ Core Security Concepts

## 🔹 Authentication

Verifying identity of a user.

Example:

* Username + Password
* OTP
* JWT Token

## 🔹 Authorization

Determining what an authenticated user is allowed to do.

Example:

* ADMIN can delete users
* USER can only view profile

## 🔹 Principal

The currently logged-in user.

## 🔹 GrantedAuthority

User permissions like:

* ROLE_ADMIN
* ROLE_USER

---

# 4️⃣ How Spring Security Works Internally

Spring Security works using **Filters**.

When a request comes:

1. Request → Security Filter Chain
2. Authentication Filter checks credentials
3. AuthenticationManager validates
4. SecurityContext stores user info
5. Authorization filter checks permissions
6. If valid → Request reaches Controller

Important Components:

* SecurityFilterChain
* AuthenticationManager
* UserDetailsService
* PasswordEncoder
* SecurityContextHolder

---

# 5️⃣ Creating a Spring Boot Project

## Using Spring Initializr

Go to:
[https://start.spring.io](https://start.spring.io)

Select:

* Project: Maven
* Language: Java
* Spring Boot: Latest stable
* Dependencies:

    * Spring Web
    * Spring Security
    * Spring Data JPA
    * H2 / MySQL

Generate and import into IDE.

---

# 6️⃣ Add Spring Security Dependency

If not added via initializer:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

# 7️⃣ Default Behavior of Spring Security

When you run application:

* All endpoints are secured.
* Default login page is generated.
* Default username: `user`
* Password: Printed in console.

---

# 8️⃣ Custom Security Configuration (Modern Approach)

Spring Boot 3 uses **SecurityFilterChain Bean** instead of WebSecurityConfigurerAdapter.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
```

---

# 9️⃣ Authentication Types

## 1️⃣ In-Memory Authentication

```java
@Bean
public UserDetailsService userDetailsService() {
    UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build();

    return new InMemoryUserDetailsManager(user);
}
```

---

## 2️⃣ Database Authentication (JPA)

### Step 1: Create Entity

```java
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String role;
}
```

### Step 2: Create Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### Step 3: Implement UserDetailsService

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
```

---

# 🔐 10️⃣ Password Encoding

Never store plain text passwords.

Use:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

BCrypt automatically salts and hashes passwords.

---

# 🔑 11️⃣ Authorization (Roles & Authorities)

### Role Example

```java
.hasRole("ADMIN")
```

Spring automatically converts to:

```
ROLE_ADMIN
```

### Authority Example

```java
.hasAuthority("READ_PRIVILEGE")
```

---

# 12️⃣ Method-Level Security

Enable:

```java
@EnableMethodSecurity
```

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() {
}
```

---

# 13️⃣ Form-Based Authentication

Used in web applications.

```java
.formLogin(form -> form
        .loginPage("/login")
        .permitAll()
)
```

You can customize:

* Login page
* Success URL
* Failure URL

---

# 14️⃣ HTTP Basic Authentication

Used mainly for REST APIs.

```java
.httpBasic(Customizer.withDefaults())
```

Client sends:

```
Authorization: Basic base64(username:password)
```

---

# 15️⃣ JWT Authentication (Stateless Security)

JWT = JSON Web Token

Used in modern REST APIs.

### Flow:

1. User logs in
2. Server validates credentials
3. Server generates JWT
4. Client stores token
5. Client sends token in header
6. Server validates token

### JWT Header:

```
Authorization: Bearer <token>
```

Benefits:

* No session
* Scalable
* Stateless

---

# 16️⃣ CSRF Protection

CSRF = Cross-Site Request Forgery

Enabled by default.

Disable for REST APIs:

```java
.csrf(csrf -> csrf.disable())
```

---

# 17️⃣ CORS Configuration

Needed for frontend + backend integration.

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000");
        }
    };
}
```

---

# 18️⃣ Exception Handling

Handle unauthorized access:

```java
.exceptionHandling(ex -> ex
        .accessDeniedPage("/access-denied")
)
```

---

# 19️⃣ Security Best Practices

✅ Always use HTTPS
✅ Hash passwords using BCrypt
✅ Use JWT for REST APIs
✅ Do not disable CSRF for forms
✅ Limit login attempts
✅ Use role-based access control
✅ Never expose internal error messages

---

# 20️⃣ Common Interview Questions

1. What is Spring Security?
2. Difference between Authentication and Authorization?
3. What is SecurityFilterChain?
4. What is JWT?
5. What is CSRF?
6. How does BCrypt work?
7. What is UserDetailsService?
8. What is stateless authentication?

---

# 21️⃣ Real-World Architecture

For Enterprise Applications:

* Spring Boot
* Spring Security
* JWT
* MySQL
* Redis (optional)
* OAuth2 (Google login)
* Role-based permissions

---

# 🎯 Conclusion

Spring Security is:

* Powerful
* Flexible
* Industry Standard
* Production Ready

It protects your application from:

* Unauthorized access
* Data leaks
* Session attacks
* Password attacks

Mastering Spring Security makes you a strong backend developer.

---

# 🚀 Next Steps

* Implement JWT project
* Integrate OAuth2 login
* Add Role-based dashboards
* Implement Refresh Tokens

---

# 📚 Recommended Learning Path

1. Understand Filters
2. Learn Authentication
3. Learn Authorization
4. Implement Database login
5. Implement JWT
6. Learn OAuth2
7. Build full production-ready security

---
