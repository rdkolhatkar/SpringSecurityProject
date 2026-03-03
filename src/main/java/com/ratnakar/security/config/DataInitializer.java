package com.ratnakar.security.config;

import com.ratnakar.security.config.DataSourceConfig.DataSourceContextHolder;
import com.ratnakar.security.config.DataSourceConfig.DataSourceType;
import com.ratnakar.security.model.Users;
import com.ratnakar.security.repository.AuthenticationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * DataInitializer
 *
 * Runs once on application startup (CommandLineRunner).
 * Seeds the users table with BCrypt-hashed passwords.
 *
 * FAILOVER AWARE:
 * Before seeding, checks if MySQL is actually reachable.
 * If MySQL is down, seeding is skipped entirely — the app starts normally
 * and will seed on the next restart when MySQL is available.
 * This prevents DataInitializer from accidentally running against PostgreSQL
 * (which may already have its own seed data from postgresql_setup.sql).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("mysqlDataSource")
    private DataSource mysqlDataSource;

    @Override
    public void run(String... args) {
        // Only seed when MySQL (primary DB) is reachable.
        // Skip silently if MySQL is down — prevents seeding into PostgreSQL failover.
        if (!isMySQLReachable()) {
            System.out.println("[DataInitializer] MySQL is unavailable — seeding skipped. " +
                    "Restart app when MySQL is up to seed users.");
            return;
        }

        try {
            // Explicitly route seed operations to MySQL
            DataSourceContextHolder.setDataSourceType(DataSourceType.MYSQL);

            // Seed admin_user if not already present
            if (!authenticationRepository.existsById(1)) {
                Users admin = new Users();
                admin.setId(1);
                admin.setUsername("admin_user");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                authenticationRepository.save(admin);
                System.out.println("[DataInitializer] Seeded user: admin_user");
            }

            // Seed normal_user if not already present
            if (!authenticationRepository.existsById(2)) {
                Users user = new Users();
                user.setId(2);
                user.setUsername("normal_user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole("USER");
                authenticationRepository.save(user);
                System.out.println("[DataInitializer] Seeded user: normal_user");
            }

            // Seed manager_user if not already present
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
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * Checks if MySQL is reachable by attempting a direct connection.
     * Uses the raw mysqlDataSource (not the RoutingDataSource) to avoid
     * triggering failover logic during this health check.
     */
    private boolean isMySQLReachable() {
        try (Connection conn = mysqlDataSource.getConnection()) {
            return conn.isValid(2); // 2 second timeout
        } catch (Exception e) {
            return false;
        }
    }
}