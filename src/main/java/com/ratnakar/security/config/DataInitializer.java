package com.ratnakar.security.config;

/*
 =============================================================================
 IMPORTS SECTION EXPLAINED
 =============================================================================
*/

import com.ratnakar.security.config.DataSourceConfig.DataSourceContextHolder;
// DataSourceContextHolder is an inner static class inside DataSourceConfig.
// It uses ThreadLocal to store which DataSource (MYSQL or POSTGRESQL)
// should be used for the current request/thread.

import com.ratnakar.security.config.DataSourceConfig.DataSourceType;
// Enum defined inside DataSourceConfig.
// Used as a key to identify which DB to use (MYSQL or POSTGRESQL).

import com.ratnakar.security.model.Users;
// This is your JPA Entity class that represents the "users" table in DB.

import com.ratnakar.security.repository.AuthenticationRepository;
// This is your Spring Data JPA repository interface.
// It allows CRUD operations without writing SQL manually.

import org.springframework.beans.factory.annotation.Autowired;
// @Autowired tells Spring to inject the required bean automatically.

import org.springframework.beans.factory.annotation.Qualifier;
// @Qualifier is used when multiple beans of same type exist.
// Here we specifically inject mysqlDataSource.

import org.springframework.boot.CommandLineRunner;
// CommandLineRunner is a Spring Boot interface.
// Any class implementing this will run automatically after application starts.

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// BCryptPasswordEncoder is a password hashing class from Spring Security.
// It hashes passwords securely before storing in DB.

import org.springframework.stereotype.Component;
// @Component makes this class a Spring Bean.
// Spring automatically detects it during component scanning.

import javax.sql.DataSource;
// DataSource represents a database connection pool.

import java.sql.Connection;
// Represents a single database connection.

/**
 * ============================================================================
 * DataInitializer
 * ============================================================================
 *
 * This class runs automatically when the Spring Boot application starts.
 *
 * HOW?
 * Because it implements CommandLineRunner.
 *
 * WHAT IT DOES?
 * - Checks if MySQL is available.
 * - If MySQL is up → seeds initial users into DB.
 * - If MySQL is down → skips seeding (prevents accidental seeding in PostgreSQL).
 *
 * WHY THIS IS IMPORTANT?
 * Because your application supports FAILOVER (MySQL → PostgreSQL).
 * We do NOT want seed data to be inserted into failover DB accidentally.
 *
 * @Component → Registers this class as a Spring Bean.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /*
     =========================================================================
     DEPENDENCY INJECTION SECTION
     =========================================================================
    */

    @Autowired
    private AuthenticationRepository authenticationRepository;
    // Injects Spring Data JPA repository.
    // Used to save users into database.

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    // Injects BCryptPasswordEncoder bean from SecurityConfig.
    // Used to hash passwords securely before storing in DB.

    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;
    // Injects specifically the MySQL DataSource bean.
    // We do NOT use routingDataSource here.
    // We want raw MySQL connection to check availability.

    /*
     =========================================================================
     run() METHOD
     =========================================================================
     This method runs automatically when application starts.
     Because this class implements CommandLineRunner.
    */
    @Override
    public void run(String... args) {

        // Step 1: Check if MySQL is reachable.
        if (!isMySQLReachable()) {
            System.out.println("[DataInitializer] MySQL is unavailable — seeding skipped. " +
                    "Restart app when MySQL is up to seed users.");
            return; // Stop execution if MySQL is down.
        }

        try {
            /*
             IMPORTANT:
             We explicitly tell routing mechanism to use MYSQL.
             This ensures seed data goes ONLY to MySQL.
            */
            DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);

            /*
             ------------------------------------------------------------------
             Seed admin_user
             ------------------------------------------------------------------
            */
            if (!authenticationRepository.existsById(1)) {
                Users admin = new Users();
                admin.setId(1);
                admin.setUsername("admin_user");

                // Password is encoded using BCrypt.
                // Never store plain text passwords in DB.
                admin.setPassword(passwordEncoder.encode("admin123"));

                admin.setRole("ADMIN");

                authenticationRepository.save(admin);

                System.out.println("[DataInitializer] Seeded user: admin_user");
            }

            /*
             ------------------------------------------------------------------
             Seed normal_user
             ------------------------------------------------------------------
            */
            if (!authenticationRepository.existsById(2)) {
                Users user = new Users();
                user.setId(2);
                user.setUsername("normal_user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole("USER");
                authenticationRepository.save(user);
                System.out.println("[DataInitializer] Seeded user: normal_user");
            }

            /*
             ------------------------------------------------------------------
             Seed manager_user
             ------------------------------------------------------------------
            */
            if (!authenticationRepository.existsById(3)) {
                Users manager = new Users();
                manager.setId(3);
                manager.setUsername("manager_user");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setRole("MANAGER");
                authenticationRepository.save(manager);
                System.out.println("[DataInitializer] Seeded user: manager_user");
            }

            System.out.println("[DataInitializer] Seed check complete.");

        } catch (Exception e) {
            System.err.println("[DataInitializer] Seeding failed: " + e.getMessage());
        } finally {
            // Always clear ThreadLocal after use.
            // Prevents memory leaks in long running apps.
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /*
     =========================================================================
     MySQL Health Check Method
     =========================================================================
     This method directly checks MySQL availability.
    */
    private boolean isMySQLReachable() {

        try (Connection conn = mysqlDataSource.getConnection()) {
            // isValid(2) → checks connection within 2 seconds timeout.
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}